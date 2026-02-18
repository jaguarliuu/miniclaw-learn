package com.miniclaw.llm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 多模型配置
 *
 * <p>支持配置多个 LLM 提供商，动态切换模型
 *
 * <p>示例配置：
 * <pre>
 * miniclaw:
 *   llm:
 *     providers:
 *       openai:
 *         endpoint: https://api.openai.com/v1
 *         api-key: ${OPENAI_API_KEY}
 *         models:
 *           - gpt-4
 *           - gpt-3.5-turbo
 *       deepseek:
 *         endpoint: https://api.deepseek.com/v1
 *         api-key: ${DEEPSEEK_API_KEY}
 *         models:
 *           - deepseek-chat
 *           - deepseek-coder
 *     default-provider: deepseek
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "miniclaw.llm")
public class LlmProperties {

    /**
     * Provider ID -> Provider 配置
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    /**
     * 默认 Provider ID
     */
    private String defaultProvider;

    /**
     * 默认温度
     */
    private Double temperature = 0.7;

    /**
     * 默认最大 token 数
     */
    private Integer maxTokens = 4096;

    /**
     * 请求超时时间（秒）
     */
    private Integer timeout = 60;

    /**
     * 单个 Provider 配置
     */
    @Data
    public static class ProviderConfig {
        /**
         * API 端点
         */
        private String endpoint;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * 支持的模型列表
         */
        private List<String> models = new ArrayList<>();

        /**
         * 默认模型（如果不指定，使用列表第一个）
         */
        private String defaultModel;
    }

    /**
     * 获取 Provider 配置
     *
     * @param providerId Provider ID
     * @return Provider 配置，如果不存在返回 null
     */
    public ProviderConfig getProvider(String providerId) {
        return providers.get(providerId);
    }

    /**
     * 获取默认 Provider ID
     *
     * @return 默认 Provider ID
     */
    public String getDefaultProviderId() {
        if (defaultProvider != null) {
            return defaultProvider;
        }
        // 如果没有指定默认，返回第一个
        return providers.isEmpty() ? null : providers.keySet().iterator().next();
    }

    /**
     * 获取默认 Provider 配置
     *
     * @return 默认 Provider 配置
     */
    public ProviderConfig getDefaultProviderConfig() {
        String providerId = getDefaultProviderId();
        return providerId != null ? providers.get(providerId) : null;
    }

    /**
     * 获取默认模型
     *
     * @param providerId Provider ID
     * @return 默认模型名称
     */
    public String getDefaultModel(String providerId) {
        ProviderConfig config = getProvider(providerId);
        if (config == null) {
            return null;
        }

        // 优先使用配置的默认模型
        if (config.getDefaultModel() != null) {
            return config.getDefaultModel();
        }

        // 否则使用列表第一个
        if (!config.getModels().isEmpty()) {
            return config.getModels().get(0);
        }

        return null;
    }
}
