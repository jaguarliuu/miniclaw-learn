package com.miniclaw.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniclaw.llm.config.LlmProperties;
import com.miniclaw.llm.exception.LlmException;
import com.miniclaw.llm.model.LlmChunk;
import com.miniclaw.llm.model.LlmRequest;
import com.miniclaw.llm.model.LlmResponse;
import com.miniclaw.llm.model.ToolCall;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenAI 兼容的 LLM 客户端（支持多 Provider）
 *
 * <p>支持配置多个 LLM 提供商，动态切换模型
 *
 * <p>使用示例：
 * <pre>
 * // 使用默认 Provider
 * LlmResponse response = client.chat(request);
 *
 * // 指定 Provider
 * LlmResponse response = client.chat(request, "openai");
 *
 * // 流式输出
 * Flux<LlmChunk> stream = client.stream(request, "deepseek");
 * </pre>
 */
@Slf4j
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Provider ID -> WebClient 缓存
     */
    private final Map<String, WebClient> clientCache = new ConcurrentHashMap<>();

    /**
     * 构造函数（自动初始化所有 Provider）
     */
    public OpenAiCompatibleLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        // 初始化所有配置的 Provider
        properties.getProviders().forEach((providerId, config) -> {
            if (config.getEndpoint() != null && config.getApiKey() != null) {
                WebClient client = buildWebClient(config.getEndpoint(), config.getApiKey());
                clientCache.put(providerId, client);
                log.info("LLM Client initialized: provider={}, endpoint={}",
                    providerId, config.getEndpoint());
            }
        });
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        return chat(request, null);
    }

    /**
     * 同步调用（指定 Provider）
     *
     * @param request 请求
     * @param providerId Provider ID（null 使用默认）
     * @return 响应
     */
    public LlmResponse chat(LlmRequest request, String providerId) {
        WebClient webClient = resolveClient(providerId);
        if (webClient == null) {
            throw new LlmException("LLM client not configured");
        }

        String actualProviderId = providerId;
        if (actualProviderId == null) {
            actualProviderId = properties.getDefaultProviderId();
        }
        if (actualProviderId == null && !clientCache.isEmpty()) {
            actualProviderId = clientCache.keySet().iterator().next();
        }

        try {
            // 构建 API 请求
            ChatCompletionRequest apiRequest = buildApiRequest(request, actualProviderId, false);

            // 发送请求（带重试）
            String responseBody = webClient.post()
                .uri("/chat/completions")
                .bodyValue(apiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(10))
                    .filter(throwable -> isRetryableError(throwable))
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        new LlmException("Max retries exceeded")))
                .block();

            // 解析响应
            return parseResponse(responseBody);

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM chat failed", e);
            throw new LlmException("LLM chat failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<LlmChunk> stream(LlmRequest request) {
        return stream(request, null);
    }

    /**
     * 流式调用（指定 Provider）
     *
     * @param request 请求
     * @param providerId Provider ID（null 使用默认）
     * @return 流式响应
     */
    public Flux<LlmChunk> stream(LlmRequest request, String providerId) {
        WebClient webClient = resolveClient(providerId);
        if (webClient == null) {
            return Flux.error(new LlmException("LLM client not configured"));
        }

        String actualProviderId = providerId;
        if (actualProviderId == null) {
            actualProviderId = properties.getDefaultProviderId();
        }
        if (actualProviderId == null && !clientCache.isEmpty()) {
            actualProviderId = clientCache.keySet().iterator().next();
        }

        try {
            // 构建 API 请求（stream = true）
            ChatCompletionRequest apiRequest = buildApiRequest(request, actualProviderId, true);

            // 用于累积 tool_calls（流式模式下 arguments 是分片到达的）
            Map<Integer, ToolCallAccumulator> toolCallAccumulators = new HashMap<>();

            // 发送流式请求（带重试和降级）
            return webClient.post()
                .uri("/chat/completions")
                .bodyValue(apiRequest)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .filter(line -> !line.isBlank())  // 过滤空行
                .filter(line -> !line.equals("data: [DONE]"))  // 过滤结束标记
                .map(line -> {
                    try {
                        // 移除 "data: " 前缀
                        String json = line.startsWith("data: ") ? line.substring(6) : line;
                        return parseChunk(json, toolCallAccumulators);
                    } catch (Exception e) {
                        log.error("Failed to parse chunk: {}", line, e);
                        return LlmChunk.builder()
                            .done(true)
                            .build();
                    }
                })
                .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(5))
                    .filter(throwable -> isRetryableError(throwable))
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        new LlmException("Stream max retries exceeded")))
                .onErrorResume(error -> {
                    log.error("Stream failed, returning error chunk", error);
                    return Flux.just(LlmChunk.builder()
                        .done(true)
                        .finishReason("error")
                        .build());
                })
                .doOnError(error -> log.error("Stream error", error))
                .doOnComplete(() -> log.debug("Stream completed"));

        } catch (Exception e) {
            log.error("LLM stream failed", e);
            return Flux.error(new LlmException("LLM stream failed: " + e.getMessage(), e));
        }
    }

    /**
     * 解析 WebClient（支持指定 Provider）
     */
    private WebClient resolveClient(String providerId) {
        if (providerId != null) {
            WebClient client = clientCache.get(providerId);
            if (client != null) {
                return client;
            }
            log.warn("Provider not found: {}, falling back to default", providerId);
        }

        // 使用默认 Provider
        String defaultProviderId = properties.getDefaultProviderId();
        if (defaultProviderId != null) {
            WebClient client = clientCache.get(defaultProviderId);
            if (client != null) {
                return client;
            }
        }

        // 兜底：使用第一个可用的
        if (!clientCache.isEmpty()) {
            return clientCache.values().iterator().next();
        }

        return null;
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
     * 构建 API 请求（使用指定 Provider 的默认模型）
     */
    private ChatCompletionRequest buildApiRequest(LlmRequest request, String providerId, boolean stream) {
        ChatCompletionRequest apiRequest = new ChatCompletionRequest();

        // 模型（优先使用请求中的模型，否则使用 Provider 默认模型）
        String model = request.getModel();
        if (model == null) {
            model = properties.getDefaultModel(providerId);
        }
        apiRequest.setModel(model);

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
        apiRequest.setTemperature(request.getTemperature() != null ?
            request.getTemperature() : properties.getTemperature());
        apiRequest.setMaxTokens(request.getMaxTokens() != null ?
            request.getMaxTokens() : properties.getMaxTokens());
        apiRequest.setStream(stream);

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
     * 解析流式响应块
     */
    private LlmChunk parseChunk(String json, Map<Integer, ToolCallAccumulator> accumulators) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode choice = root.path("choices").get(0);
            JsonNode delta = choice.path("delta");

            // 解析内容增量
            String contentDelta = delta.path("content").asText(null);

            // 解析完成原因
            String finishReason = choice.path("finish_reason").asText(null);

            // 是否是最后一个 chunk
            boolean done = finishReason != null && !"null".equals(finishReason);

            // 解析工具调用增量
            List<ToolCall> toolCalls = null;
            if (delta.has("tool_calls")) {
                toolCalls = new ArrayList<>();
                for (JsonNode tc : delta.path("tool_calls")) {
                    int index = tc.path("index").asInt();

                    // 获取或创建累积器
                    ToolCallAccumulator accumulator = accumulators.computeIfAbsent(
                        index, k -> new ToolCallAccumulator()
                    );

                    // 累积工具调用信息
                    if (tc.has("id")) {
                        accumulator.id = tc.path("id").asText();
                    }
                    if (tc.has("type")) {
                        accumulator.type = tc.path("type").asText();
                    }
                    if (tc.has("function")) {
                        JsonNode function = tc.path("function");
                        if (function.has("name")) {
                            accumulator.functionName = function.path("name").asText();
                        }
                        if (function.has("arguments")) {
                            accumulator.argumentsBuilder.append(function.path("arguments").asText());
                        }
                    }

                    // 如果已完成，构建完整的 ToolCall
                    if (done && accumulator.id != null) {
                        ToolCall toolCall = ToolCall.builder()
                            .id(accumulator.id)
                            .type(accumulator.type)
                            .function(ToolCall.FunctionCall.builder()
                                .name(accumulator.functionName)
                                .arguments(accumulator.argumentsBuilder.toString())
                                .build())
                            .build();
                        toolCalls.add(toolCall);
                    }
                }
            }

            return LlmChunk.builder()
                .delta(contentDelta)
                .toolCalls(toolCalls)
                .finishReason(finishReason)
                .done(done)
                .build();

        } catch (Exception e) {
            log.error("Failed to parse chunk: {}", json, e);
            return LlmChunk.builder().done(true).build();
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

    /**
     * 工具调用累积器（用于流式模式）
     */
    private static class ToolCallAccumulator {
        String id;
        String type;
        String functionName;
        StringBuilder argumentsBuilder = new StringBuilder();
    }

    /**
     * 判断是否为可重试的错误（package-private for testing）
     */
    boolean isRetryableError(Throwable throwable) {
        // 网络超时
        if (throwable instanceof java.util.concurrent.TimeoutException) {
            return true;
        }

        // WebClient 网络错误
        if (throwable instanceof org.springframework.web.reactive.function.client.WebClientException) {
            String message = throwable.getMessage();
            // 5xx 服务器错误可重试
            if (message != null && (message.contains("500") || message.contains("502") ||
                message.contains("503") || message.contains("504"))) {
                return true;
            }
            // 连接错误可重试
            if (message != null && (message.contains("Connection") || message.contains("timeout"))) {
                return true;
            }
        }

        // 429 限流错误可重试
        if (throwable.getMessage() != null && throwable.getMessage().contains("429")) {
            return true;
        }

        return false;
    }
}
