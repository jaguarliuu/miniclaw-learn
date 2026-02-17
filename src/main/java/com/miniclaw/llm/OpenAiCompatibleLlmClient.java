package com.miniclaw.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniclaw.llm.config.LlmProperties;
import com.miniclaw.llm.exception.LlmException;
import com.miniclaw.llm.model.LlmChunk;
import com.miniclaw.llm.model.LlmRequest;
import com.miniclaw.llm.model.LlmResponse;
import com.miniclaw.llm.model.ToolCall;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容的 LLM 客户端
 *
 * <p>支持 OpenAI、DeepSeek、通义千问、Ollama 等兼容 OpenAI API 的模型
 *
 * <p>功能：
 * <ul>
 *   <li>同步调用（chat）</li>
 *   <li>流式调用（stream）- 第 4.4 节实现</li>
 *   <li>Function Calling 支持</li>
 * </ul>
 */
@Slf4j
@Component
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final WebClient webClient;
    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        // 构建 WebClient
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            this.webClient = buildWebClient(properties.getEndpoint(), properties.getApiKey());
            log.info("LLM Client initialized: endpoint={}, model={}",
                properties.getEndpoint(), properties.getModel());
        } else {
            this.webClient = null;
            log.info("LLM Client created without endpoint — waiting for configuration");
        }
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        if (webClient == null) {
            throw new LlmException("LLM client not configured");
        }

        try {
            // 构建 API 请求
            ChatCompletionRequest apiRequest = buildApiRequest(request, false);

            // 发送请求
            String responseBody = webClient.post()
                .uri("/chat/completions")
                .bodyValue(apiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .block();

            // 解析响应
            return parseResponse(responseBody);

        } catch (Exception e) {
            log.error("LLM chat failed", e);
            throw new LlmException("LLM chat failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<LlmChunk> stream(LlmRequest request) {
        // 第 4.4 节实现流式调用
        throw new UnsupportedOperationException("Stream mode not implemented yet (will be in 4.4)");
    }

    /**
     * 构建 WebClient
     */
    private WebClient buildWebClient(String endpoint, String apiKey) {
        return WebClient.builder()
            .baseUrl(endpoint)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * 构建 API 请求
     */
    private ChatCompletionRequest buildApiRequest(LlmRequest request, boolean stream) {
        ChatCompletionRequest apiRequest = new ChatCompletionRequest();

        // 模型
        apiRequest.setModel(request.getModel() != null ? request.getModel() : properties.getModel());

        // 消息
        List<Map<String, Object>> messages = new ArrayList<>();
        for (LlmRequest.Message message : request.getMessages()) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", message.getRole());

            if (message.getContent() != null) {
                msg.put("content", message.getContent());
            }

            if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
                msg.put("tool_calls", message.getToolCalls());
            }

            if (message.getToolCallId() != null) {
                msg.put("tool_call_id", message.getToolCallId());
            }

            messages.add(msg);
        }
        apiRequest.setMessages(messages);

        // 参数
        apiRequest.setTemperature(request.getTemperature() != null ? request.getTemperature() : properties.getTemperature());
        apiRequest.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : properties.getMaxTokens());
        apiRequest.setStream(stream);

        // 工具
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            apiRequest.setTools(request.getTools());
        }

        if (request.getToolChoice() != null) {
            apiRequest.setToolChoice(request.getToolChoice());
        }

        return apiRequest;
    }

    /**
     * 解析 API 响应
     */
    private LlmResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choice = root.path("choices").get(0);
            JsonNode message = choice.path("message");

            // 解析内容
            String content = message.path("content").asText(null);

            // 解析工具调用
            List<ToolCall> toolCalls = null;
            if (message.has("tool_calls") && !message.path("tool_calls").isNull()) {
                toolCalls = new ArrayList<>();
                for (JsonNode tc : message.path("tool_calls")) {
                    ToolCall toolCall = ToolCall.builder()
                        .id(tc.path("id").asText())
                        .type(tc.path("type").asText())
                        .function(ToolCall.FunctionCall.builder()
                            .name(tc.path("function").path("name").asText())
                            .arguments(tc.path("function").path("arguments").asText())
                            .build())
                        .build();
                    toolCalls.add(toolCall);
                }
            }

            // 解析完成原因
            String finishReason = choice.path("finish_reason").asText(null);

            // 解析 token 使用
            LlmResponse.Usage usage = null;
            if (root.has("usage")) {
                usage = LlmResponse.Usage.builder()
                    .promptTokens(root.path("usage").path("prompt_tokens").asInt())
                    .completionTokens(root.path("usage").path("completion_tokens").asInt())
                    .totalTokens(root.path("usage").path("total_tokens").asInt())
                    .build();
            }

            return LlmResponse.builder()
                .content(content)
                .toolCalls(toolCalls)
                .finishReason(finishReason)
                .usage(usage)
                .build();

        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", responseBody, e);
            throw new LlmException("Failed to parse LLM response", e);
        }
    }

    /**
     * OpenAI API 请求模型
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class ChatCompletionRequest {
        private String model;
        private List<Map<String, Object>> messages;
        private Double temperature;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Boolean stream;
        private List<Map<String, Object>> tools;
        @JsonProperty("tool_choice")
        private Object toolChoice;
    }
}
