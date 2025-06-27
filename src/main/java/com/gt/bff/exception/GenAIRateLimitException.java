package com.gt.bff.exception;

public class GenAIRateLimitException extends GenAIException {
    private static final long serialVersionUID = 3L;

    public GenAIRateLimitException(String message) {
        super(message);
    }

    public GenAIRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}