package com.miniclaw.llm;

import com.miniclaw.llm.model.LlmChunk;
import com.miniclaw.llm.model.LlmRequest;
import com.miniclaw.llm.model.LlmResponse;
import reactor.core.publisher.Flux;

/**
 * LLM 客户端接口
 *
 * <p>定义 LLM 调用的标准接口，支持同步和流式两种模式
 *
 * <p>实现类：
 * <ul>
 *   <li>{@link OpenAiCompatibleLlmClient} - OpenAI 兼容客户端（4.3 节实现）</li>
 * </ul>
 */
public interface LlmClient {

    /**
     * 同步调用 LLM
     *
     * <p>等待 LLM 完整返回结果后再返回
     *
     * @param request LLM 请求
     * @return LLM 响应
     * @throws LlmException 调用失败时抛出
     */
    LlmResponse chat(LlmRequest request);

    /**
     * 流式调用 LLM
     *
     * <p>实时返回 LLM 生成的内容块
     *
     * @param request LLM 请求
     * @return LLM 响应流（Flux）
     * @throws LlmException 调用失败时抛出
     */
    Flux<LlmChunk> stream(LlmRequest request);
}
