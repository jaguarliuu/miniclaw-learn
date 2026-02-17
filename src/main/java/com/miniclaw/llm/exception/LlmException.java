package com.miniclaw.llm.exception;

/**
 * LLM 异常基类
 *
 * <p>所有 LLM 相关异常的父类
 */
public class LlmException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    public LlmException(String message) {
        super(message);
        this.errorCode = "LLM_ERROR";
    }

    public LlmException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "LLM_ERROR";
    }

    public LlmException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
