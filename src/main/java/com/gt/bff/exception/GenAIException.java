package com.gt.bff.exception;

public class GenAIException extends RuntimeException {

    public GenAIException(String message) {
        super(message);
    }

    public GenAIException(String message, Throwable cause) {
        super(message, cause);
    }
}