package com.miniclaw.llm.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LlmProperties 多模型配置测试
 */
class LlmPropertiesTest {

    @Test
    void testGetProvider() {
        LlmProperties properties = new LlmProperties();

        LlmProperties.ProviderConfig openai = new LlmProperties.ProviderConfig();
        openai.setEndpoint("https://api.openai.com/v1");
        openai.setApiKey("sk-test");
        openai.setModels(List.of("gpt-4", "gpt-3.5-turbo"));

        properties.getProviders().put("openai", openai);

        assertNotNull(properties.getProvider("openai"));
        assertEquals("https://api.openai.com/v1", properties.getProvider("openai").getEndpoint());
        assertNull(properties.getProvider("deepseek"));
    }

    @Test
    void testGetDefaultProvider() {
        LlmProperties properties = new LlmProperties();

        // 没有配置任何 Provider
        assertNull(properties.getDefaultProviderId());

        // 配置一个 Provider
        LlmProperties.ProviderConfig openai = new LlmProperties.ProviderConfig();
        properties.getProviders().put("openai", openai);

        // 应该返回第一个（但应该调用 getDefaultProviderConfig）
        assertNotNull(properties.getDefaultProviderConfig());

        // 指定默认
        properties.setDefaultProvider("openai");
        assertNotNull(properties.getDefaultProviderConfig());
    }

    @Test
    void testGetDefaultModel() {
        LlmProperties properties = new LlmProperties();

        LlmProperties.ProviderConfig deepseek = new LlmProperties.ProviderConfig();
        deepseek.setModels(List.of("deepseek-chat", "deepseek-coder"));
        deepseek.setDefaultModel("deepseek-coder");

        properties.getProviders().put("deepseek", deepseek);

        // 应该返回配置的默认模型
        assertEquals("deepseek-coder", properties.getDefaultModel("deepseek"));

        // 没有配置默认，返回列表第一个
        deepseek.setDefaultModel(null);
        assertEquals("deepseek-chat", properties.getDefaultModel("deepseek"));

        // Provider 不存在
        assertNull(properties.getDefaultModel("not-exist"));
    }

    @Test
    void testDefaultValues() {
        LlmProperties properties = new LlmProperties();

        assertEquals(0.7, properties.getTemperature());
        assertEquals(4096, properties.getMaxTokens());
        assertEquals(60, properties.getTimeout());
    }

    @Test
    void testMultipleProviders() {
        LlmProperties properties = new LlmProperties();

        LlmProperties.ProviderConfig openai = new LlmProperties.ProviderConfig();
        openai.setEndpoint("https://api.openai.com/v1");
        openai.setApiKey("sk-openai");
        openai.setModels(List.of("gpt-4"));

        LlmProperties.ProviderConfig deepseek = new LlmProperties.ProviderConfig();
        deepseek.setEndpoint("https://api.deepseek.com/v1");
        deepseek.setApiKey("sk-deepseek");
        deepseek.setModels(List.of("deepseek-chat"));

        properties.getProviders().put("openai", openai);
        properties.getProviders().put("deepseek", deepseek);
        properties.setDefaultProvider("deepseek");

        // 验证配置
        assertEquals(2, properties.getProviders().size());
        assertEquals("deepseek", properties.getDefaultProvider());
        assertEquals("https://api.openai.com/v1", properties.getProvider("openai").getEndpoint());
        assertEquals("https://api.deepseek.com/v1", properties.getProvider("deepseek").getEndpoint());
    }
}
