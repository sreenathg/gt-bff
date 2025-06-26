package com.gt.bff.exception;

public class GenAIConfigurationException extends GenAIException {

    public GenAIConfigurationException(String message) {
        super(message);
    }

    public GenAIConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}