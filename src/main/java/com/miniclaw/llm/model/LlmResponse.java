package com.miniclaw.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM 响应模型（同步调用）
 *
 * <p>封装 LLM API 同步调用的响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmResponse {

    /**
     * 响应内容（有 tool_calls 时可能为 null）
     */
    private String content;

    /**
     * 工具调用列表
     */
    private List<ToolCall> toolCalls;

    /**
     * 完成原因：stop, length, tool_calls 等
     */
    private String finishReason;

    /**
     * Token 使用统计
     */
    private Usage usage;

    /**
     * 是否有工具调用
     *
     * @return true 如果有工具调用
     */
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    /**
     * Token 使用统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Usage {

        /**
         * 输入 token 数
         */
        private Integer promptTokens;

        /**
         * 输出 token 数
         */
        private Integer completionTokens;

        /**
         * 总 token 数
         */
        private Integer totalTokens;
    }
}
