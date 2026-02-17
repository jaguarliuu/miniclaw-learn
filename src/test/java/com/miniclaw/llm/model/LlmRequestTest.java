package com.miniclaw.llm.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LlmRequest 测试类
 */
class LlmRequestTest {

    @Test
    void testMessageCreation() {
        // 测试系统消息
        LlmRequest.Message systemMessage = LlmRequest.Message.system("你是一个助手");
        assertEquals("system", systemMessage.getRole());
        assertEquals("你是一个助手", systemMessage.getContent());
        assertNull(systemMessage.getToolCalls());
        assertNull(systemMessage.getToolCallId());

        // 测试用户消息
        LlmRequest.Message userMessage = LlmRequest.Message.user("你好");
        assertEquals("user", userMessage.getRole());
        assertEquals("你好", userMessage.getContent());

        // 测试助手消息
        LlmRequest.Message assistantMessage = LlmRequest.Message.assistant("你好！");
        assertEquals("assistant", assistantMessage.getRole());
        assertEquals("你好！", assistantMessage.getContent());

        // 测试工具结果消息
        LlmRequest.Message toolMessage = LlmRequest.Message.toolResult("call_123", "结果");
        assertEquals("tool", toolMessage.getRole());
        assertEquals("call_123", toolMessage.getToolCallId());
        assertEquals("结果", toolMessage.getContent());
    }

    @Test
    void testMessageWithToolCalls() {
        ToolCall toolCall = ToolCall.builder()
            .id("call_123")
            .type("function")
            .function(ToolCall.FunctionCall.builder()
                .name("get_weather")
                .arguments("{\"city\": \"北京\"}")
                .build())
            .build();

        LlmRequest.Message message = LlmRequest.Message.assistantWithToolCalls(List.of(toolCall));

        assertEquals("assistant", message.getRole());
        assertNull(message.getContent());
        assertNotNull(message.getToolCalls());
        assertEquals(1, message.getToolCalls().size());
        assertEquals("call_123", message.getToolCalls().get(0).getId());
    }

    @Test
    void testRequestBuilder() {
        LlmRequest request = LlmRequest.builder()
            .messages(List.of(
                LlmRequest.Message.system("你是一个助手"),
                LlmRequest.Message.user("你好")
            ))
            .model("gpt-4")
            .temperature(0.7)
            .maxTokens(1000)
            .stream(false)
            .build();

        assertEquals(2, request.getMessages().size());
        assertEquals("gpt-4", request.getModel());
        assertEquals(0.7, request.getTemperature());
        assertEquals(1000, request.getMaxTokens());
        assertFalse(request.getStream());
    }
}
