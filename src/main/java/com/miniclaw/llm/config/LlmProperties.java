package com.miniclaw.llm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 配置属性
 *
 * <p>从 application.yml 读取 LLM 相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "miniclaw.llm")
public class LlmProperties {

    /**
     * API 端点（如：https://api.openai.com/v1）
     */
    private String endpoint;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 默认模型
     */
    private String model;

    /**
     * 请求超时（秒）
     */
    private Integer timeout = 60;

    /**
     * 温度参数（0-2）
     */
    private Double temperature = 0.7;

    /**
     * 最大 token 数
     */
    private Integer maxTokens = 4096;
}
