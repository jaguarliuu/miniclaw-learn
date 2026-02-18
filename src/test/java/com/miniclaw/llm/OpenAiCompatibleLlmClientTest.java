package com.miniclaw.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniclaw.llm.config.LlmProperties;
import com.miniclaw.llm.exception.LlmException;
import com.miniclaw.llm.model.LlmChunk;
import com.miniclaw.llm.model.LlmRequest;
import com.miniclaw.llm.model.LlmResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OpenAiCompatibleLlmClient 测试类
 */
@ExtendWith(MockitoExtension.class)
class OpenAiCompatibleLlmClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private LlmProperties properties;
    private ObjectMapper objectMapper;
    private OpenAiCompatibleLlmClient client;

    @BeforeEach
    void setUp() {
        properties = new LlmProperties();

        // 配置单个 Provider（测试用）
        LlmProperties.ProviderConfig openai = new LlmProperties.ProviderConfig();
        openai.setEndpoint("https://api.openai.com/v1");
        openai.setApiKey("test-api-key");
        openai.setModels(List.of("gpt-4", "gpt-3.5-turbo"));
        openai.setDefaultModel("gpt-4");

        properties.getProviders().put("openai", openai);
        properties.setDefaultProvider("openai");
        properties.setTimeout(60);

        objectMapper = new ObjectMapper();
        client = new OpenAiCompatibleLlmClient(properties, objectMapper);
    }

    @Test
    void testClientInitialization() {
        assertNotNull(client);
        assertEquals("https://api.openai.com/v1",
            properties.getProvider("openai").getEndpoint());
        assertEquals("gpt-4",
            properties.getDefaultModel("openai"));
    }

    @Test
    void testClientWithoutProvider() {
        LlmProperties emptyProps = new LlmProperties();

        OpenAiCompatibleLlmClient emptyClient = new OpenAiCompatibleLlmClient(emptyProps, objectMapper);

        // 应该抛出异常
        assertThrows(LlmException.class, () -> {
            emptyClient.chat(LlmRequest.builder()
                .messages(List.of(LlmRequest.Message.user("test")))
                .build());
        });
    }

    @Test
    void testStreamReturnsFlux() {
        LlmRequest request = LlmRequest.builder()
            .messages(List.of(LlmRequest.Message.user("test")))
            .build();

        // 4.4 已实现流式调用，返回 Flux
        Flux<LlmChunk> stream = client.stream(request);
        assertNotNull(stream);
    }

    @Test
    void testPropertiesDefaultValues() {
        LlmProperties props = new LlmProperties();

        // 测试默认值
        assertEquals(60, props.getTimeout());
        assertEquals(0.7, props.getTemperature());
        assertEquals(4096, props.getMaxTokens());
    }
}
