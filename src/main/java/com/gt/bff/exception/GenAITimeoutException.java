package com.gt.bff.exception;

public class GenAITimeoutException extends GenAIException {
    private static final long serialVersionUID = 2L;

    public GenAITimeoutException(String message) {
        super(message);
    }

    public GenAITimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}