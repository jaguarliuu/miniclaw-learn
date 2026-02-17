package com.miniclaw.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniclaw.llm.config.LlmProperties;
import com.miniclaw.llm.exception.LlmException;
import com.miniclaw.llm.model.LlmRequest;
import com.miniclaw.llm.model.LlmResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

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
        properties.setEndpoint("https://api.openai.com/v1");
        properties.setApiKey("test-api-key");
        properties.setModel("gpt-4");
        properties.setTimeout(60);

        objectMapper = new ObjectMapper();
        client = new OpenAiCompatibleLlmClient(properties, objectMapper);
    }

    @Test
    void testClientInitialization() {
        assertNotNull(client);
        assertEquals("https://api.openai.com/v1", properties.getEndpoint());
        assertEquals("gpt-4", properties.getModel());
    }

    @Test
    void testClientWithoutEndpoint() {
        LlmProperties emptyProps = new LlmProperties();
        emptyProps.setEndpoint(null);

        OpenAiCompatibleLlmClient emptyClient = new OpenAiCompatibleLlmClient(emptyProps, objectMapper);

        // 应该抛出异常
        assertThrows(LlmException.class, () -> {
            emptyClient.chat(LlmRequest.builder()
                .messages(List.of(LlmRequest.Message.user("test")))
                .build());
        });
    }

    @Test
    void testStreamNotImplemented() {
        LlmRequest request = LlmRequest.builder()
            .messages(List.of(LlmRequest.Message.user("test")))
            .build();

        // 流式调用应该抛出 UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            client.stream(request);
        });
    }

    @Test
    void testPropertiesDefaultValues() {
        LlmProperties props = new LlmProperties();

        // 测试默认值
        assertEquals(60, props.getTimeout());
        assertEquals(0.7, props.getTemperature());
        assertEquals(4096, props.getMaxTokens());
    }

    @Test
    void testPropertiesSetters() {
        LlmProperties props = new LlmProperties();

        props.setEndpoint("https://api.deepseek.com/v1");
        props.setApiKey("sk-test");
        props.setModel("deepseek-chat");
        props.setTimeout(120);
        props.setTemperature(0.5);
        props.setMaxTokens(8192);

        assertEquals("https://api.deepseek.com/v1", props.getEndpoint());
        assertEquals("sk-test", props.getApiKey());
        assertEquals("deepseek-chat", props.getModel());
        assertEquals(120, props.getTimeout());
        assertEquals(0.5, props.getTemperature());
        assertEquals(8192, props.getMaxTokens());
    }
}
