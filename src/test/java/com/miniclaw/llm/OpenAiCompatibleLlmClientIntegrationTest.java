package com.miniclaw.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniclaw.llm.config.LlmProperties;
import com.miniclaw.llm.model.LlmChunk;
import com.miniclaw.llm.model.LlmRequest;
import com.miniclaw.llm.model.LlmResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAiCompatibleLlmClient 集成测试
 *
 * <p>测试真实的 API 调用（需要 API Key）
 *
 * <p>运行方式：mvn test -Dtest=OpenAiCompatibleLlmClientIntegrationTest -Denv.api.key=your_key
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
class OpenAiCompatibleLlmClientIntegrationTest {

    private OpenAiCompatibleLlmClient client;
    private LlmProperties properties;

    @BeforeEach
    void setUp() {
        // 从环境变量读取配置
        properties = new LlmProperties();

        String endpoint = System.getenv("LLM_ENDPOINT");
        String apiKey = System.getenv("LLM_API_KEY");
        String model = System.getenv("LLM_MODEL");

        if (endpoint != null && apiKey != null) {
            LlmProperties.ProviderConfig provider = new LlmProperties.ProviderConfig();
            provider.setEndpoint(endpoint);
            provider.setApiKey(apiKey);
            provider.setModels(model != null ? List.of(model) : List.of());
            provider.setDefaultModel(model);

            properties.getProviders().put("default", provider);
            properties.setDefaultProvider("default");
        }
        properties.setTimeout(60);

        ObjectMapper objectMapper = new ObjectMapper();
        client = new OpenAiCompatibleLlmClient(properties, objectMapper);
    }

    @Test
    void testRealSyncChat() {
        LlmRequest request = LlmRequest.builder()
            .messages(List.of(
                LlmRequest.Message.user("用一句话介绍 OpenClaw")
            ))
            .build();

        LlmResponse response = client.chat(request);

        System.out.println("=== 同步调用结果 ===");
        System.out.println("内容: " + response.getContent());
        System.out.println("完成原因: " + response.getFinishReason());
        if (response.getUsage() != null) {
            System.out.println("Token 使用: " + response.getUsage().getTotalTokens());
        }

        assertNotNull(response.getContent());
        assertFalse(response.getContent().isBlank());
    }

    @Test
    void testRealStreamChat() {
        LlmRequest request = LlmRequest.builder()
            .messages(List.of(
                LlmRequest.Message.user("用一句话介绍 AI Agent")
            ))
            .build();

        Flux<LlmChunk> stream = client.stream(request);

        System.out.println("\n=== 流式调用结果 ===");

        StringBuilder fullContent = new StringBuilder();

        stream
            .doOnNext(chunk -> {
                if (chunk.getDelta() != null) {
                    System.out.print(chunk.getDelta());
                    fullContent.append(chunk.getDelta());
                }
                if (chunk.isDone()) {
                    System.out.println("\n[完成]");
                }
            })
            .blockLast(Duration.ofSeconds(30));  // 等待流式完成

        System.out.println("\n完整内容: " + fullContent.toString());
        assertFalse(fullContent.toString().isBlank());
    }
}
