package com.miniclaw.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniclaw.llm.config.LlmProperties;
import com.miniclaw.llm.model.LlmRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 错误处理和重试测试
 */
class OpenAiCompatibleLlmClientRetryTest {

    private OpenAiCompatibleLlmClient client;
    private LlmProperties properties;

    @Mock
    private WebClient mockWebClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        properties = new LlmProperties();

        LlmProperties.ProviderConfig openai = new LlmProperties.ProviderConfig();
        openai.setEndpoint("https://api.openai.com/v1");
        openai.setApiKey("test-key");
        openai.setModels(List.of("gpt-4"));
        openai.setDefaultModel("gpt-4");

        properties.getProviders().put("openai", openai);
        properties.setDefaultProvider("openai");
        properties.setTimeout(60);

        ObjectMapper objectMapper = new ObjectMapper();
        client = new OpenAiCompatibleLlmClient(properties, objectMapper);
    }

    @Test
    void testIsRetryableError_Timeout() {
        assertTrue(client.isRetryableError(new java.util.concurrent.TimeoutException()));
    }

    @Test
    void testIsRetryableError_ServerError() {
        WebClientResponseException error500 = WebClientResponseException.create(
            500, "Internal Server Error", null, null, null);
        assertTrue(client.isRetryableError(error500));

        WebClientResponseException error502 = WebClientResponseException.create(
            502, "Bad Gateway", null, null, null);
        assertTrue(client.isRetryableError(error502));

        WebClientResponseException error503 = WebClientResponseException.create(
            503, "Service Unavailable", null, null, null);
        assertTrue(client.isRetryableError(error503));
    }

    @Test
    void testIsRetryableError_RateLimit() {
        WebClientResponseException error429 = WebClientResponseException.create(
            429, "Too Many Requests", null, null, null);
        assertTrue(client.isRetryableError(error429));
    }

    @Test
    void testIsNotRetryableError_ClientError() {
        WebClientResponseException error400 = WebClientResponseException.create(
            400, "Bad Request", null, null, null);
        assertFalse(client.isRetryableError(error400));

        WebClientResponseException error401 = WebClientResponseException.create(
            401, "Unauthorized", null, null, null);
        assertFalse(client.isRetryableError(error401));

        WebClientResponseException error404 = WebClientResponseException.create(
            404, "Not Found", null, null, null);
        assertFalse(client.isRetryableError(error404));
    }

    @Test
    void testIsNotRetryableError_Generic() {
        assertFalse(client.isRetryableError(new RuntimeException("Generic error")));
        assertFalse(client.isRetryableError(new NullPointerException()));
    }
}
