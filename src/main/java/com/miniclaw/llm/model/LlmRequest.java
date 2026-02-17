package com.miniclaw.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * LLM 请求模型
 *
 * <p>封装 LLM API 调用所需的参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmRequest {

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 模型名称（可选，默认使用配置）
     */
    private String model;

    /**
     * 温度参数（0-2，越高越随机）
     */
    private Double temperature;

    /**
     * 最大 token 数
     */
    private Integer maxTokens;

    /**
     * 是否流式输出
     */
    private Boolean stream;

    /**
     * 工具定义列表（OpenAI Function Calling 格式）
     */
    private List<Map<String, Object>> tools;

    /**
     * 工具选择策略：auto / none / required
     */
    private String toolChoice;

    /**
     * 消息模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {

        /**
         * 角色：system, user, assistant, tool
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 工具调用列表（仅 assistant 角色）
         */
        private List<ToolCall> toolCalls;

        /**
         * 工具调用 ID（仅 tool 角色）
         */
        private String toolCallId;

        /**
         * 创建系统消息
         */
        public static Message system(String content) {
            return Message.builder()
                .role("system")
                .content(content)
                .build();
        }

        /**
         * 创建用户消息
         */
        public static Message user(String content) {
            return Message.builder()
                .role("user")
                .content(content)
                .build();
        }

        /**
         * 创建助手消息
         */
        public static Message assistant(String content) {
            return Message.builder()
                .role("assistant")
                .content(content)
                .build();
        }

        /**
         * 创建带工具调用的助手消息
         */
        public static Message assistantWithToolCalls(List<ToolCall> toolCalls) {
            return Message.builder()
                .role("assistant")
                .toolCalls(toolCalls)
                .build();
        }

        /**
         * 创建工具结果消息
         */
        public static Message toolResult(String toolCallId, String content) {
            return Message.builder()
                .role("tool")
                .toolCallId(toolCallId)
                .content(content)
                .build();
        }
    }
}
