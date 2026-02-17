package com.miniclaw.llm.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ToolCall 测试类
 */
class ToolCallTest {

    @Test
    void testToolCallBuilder() {
        ToolCall.FunctionCall function = ToolCall.FunctionCall.builder()
            .name("get_weather")
            .arguments("{\"city\": \"北京\"}")
            .build();

        ToolCall toolCall = ToolCall.builder()
            .id("call_123")
            .type("function")
            .function(function)
            .build();

        assertEquals("call_123", toolCall.getId());
        assertEquals("function", toolCall.getType());
        assertNotNull(toolCall.getFunction());
        assertEquals("get_weather", toolCall.getFunction().getName());
        assertEquals("{\"city\": \"北京\"}", toolCall.getFunction().getArguments());
    }

    @Test
    void testFunctionCallBuilder() {
        ToolCall.FunctionCall function = ToolCall.FunctionCall.builder()
            .name("read_file")
            .arguments("{\"path\": \"/tmp/test.txt\"}")
            .build();

        assertEquals("read_file", function.getName());
        assertEquals("{\"path\": \"/tmp/test.txt\"}", function.getArguments());
    }

    @Test
    void testNestedBuilder() {
        // 测试嵌套构建器
        ToolCall toolCall = ToolCall.builder()
            .id("call_abc")
            .type("function")
            .function(ToolCall.FunctionCall.builder()
                .name("execute_command")
                .arguments("{\"cmd\": \"ls -la\"}")
                .build())
            .build();

        assertNotNull(toolCall);
        assertNotNull(toolCall.getFunction());
        assertEquals("execute_command", toolCall.getFunction().getName());
    }
}
