package com.miniclaw.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用模型
 *
 * <p>OpenAI Function Calling 的工具调用数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCall {

    /**
     * 工具调用 ID（OpenAI 生成）
     */
    private String id;

    /**
     * 工具类型（目前只有 function）
     */
    private String type;

    /**
     * 函数调用信息
     */
    private FunctionCall function;

    /**
     * 函数调用信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FunctionCall {

        /**
         * 函数名称
         */
        private String name;

        /**
         * 函数参数（JSON 字符串）
         */
        private String arguments;
    }
}
