package com.miniclaw.llm.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LlmChunk 测试类
 */
class LlmChunkTest {

    @Test
    void testChunkBuilder() {
        LlmChunk chunk = LlmChunk.builder()
            .delta("你好")
            .finishReason(null)
            .done(false)
            .build();

        assertEquals("你好", chunk.getDelta());
        assertNull(chunk.getFinishReason());
        assertFalse(chunk.isDone());
    }

    @Test
    void testDoneChunk() {
        LlmChunk chunk = LlmChunk.builder()
            .delta(null)
            .finishReason("stop")
            .done(true)
            .build();

        assertNull(chunk.getDelta());
        assertEquals("stop", chunk.getFinishReason());
        assertTrue(chunk.isDone());
    }

    @Test
    void testHasToolCalls() {
        // 没有工具调用
        LlmChunk chunk1 = LlmChunk.builder()
            .delta("普通内容")
            .build();
        assertFalse(chunk1.hasToolCalls());

        // 有工具调用
        ToolCall toolCall = ToolCall.builder()
            .id("call_123")
            .type("function")
            .build();

        LlmChunk chunk2 = LlmChunk.builder()
            .toolCalls(List.of(toolCall))
            .build();

        assertTrue(chunk2.hasToolCalls());
    }

    @Test
    void testToolCallFields() {
        LlmChunk chunk = LlmChunk.builder()
            .toolCallFunctionName("get_weather")
            .toolCallArgumentsDelta("{\"city\": \"北京\"}")
            .build();

        assertEquals("get_weather", chunk.getToolCallFunctionName());
        assertEquals("{\"city\": \"北京\"}", chunk.getToolCallArgumentsDelta());
    }
}
