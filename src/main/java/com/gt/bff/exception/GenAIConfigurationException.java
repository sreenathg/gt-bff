package com.gt.bff.exception;

public class GenAIConfigurationException extends GenAIException {
    private static final long serialVersionUID = 4L;

    public GenAIConfigurationException(String message) {
        super(message);
    }

    public GenAIConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}