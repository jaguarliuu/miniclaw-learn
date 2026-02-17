package com.miniclaw.llm.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LlmResponse 测试类
 */
class LlmResponseTest {

    @Test
    void testResponseBuilder() {
        LlmResponse response = LlmResponse.builder()
            .content("你好！有什么我可以帮助你的吗？")
            .finishReason("stop")
            .usage(LlmResponse.Usage.builder()
                .promptTokens(10)
                .completionTokens(15)
                .totalTokens(25)
                .build())
            .build();

        assertEquals("你好！有什么我可以帮助你的吗？", response.getContent());
        assertEquals("stop", response.getFinishReason());
        assertNotNull(response.getUsage());
        assertEquals(10, response.getUsage().getPromptTokens());
        assertEquals(15, response.getUsage().getCompletionTokens());
        assertEquals(25, response.getUsage().getTotalTokens());
    }

    @Test
    void testHasToolCalls() {
        // 没有工具调用
        LlmResponse response1 = LlmResponse.builder()
            .content("普通响应")
            .build();
        assertFalse(response1.hasToolCalls());

        // 有工具调用
        ToolCall toolCall = ToolCall.builder()
            .id("call_123")
            .type("function")
            .build();

        LlmResponse response2 = LlmResponse.builder()
            .toolCalls(List.of(toolCall))
            .finishReason("tool_calls")
            .build();

        assertTrue(response2.hasToolCalls());
        assertEquals("tool_calls", response2.getFinishReason());
    }

    @Test
    void testToolCallsCanBeNull() {
        LlmResponse response = LlmResponse.builder()
            .content("响应内容")
            .build();

        assertNull(response.getToolCalls());
        assertFalse(response.hasToolCalls());
    }
}
