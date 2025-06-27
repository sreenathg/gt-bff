package com.gt.bff.exception;

public class GenAIException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GenAIException(String message) {
        super(message);
    }

    public GenAIException(String message, Throwable cause) {
        super(message, cause);
    }
}