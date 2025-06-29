package com.gt.bff.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void resourceNotFoundException_ShouldCreateWithMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void resourceNotFoundException_ShouldCreateWithResourceDetails() {
        String resourceName = "User";
        String fieldName = "id";
        Object fieldValue = 123;
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);
        
        String expectedMessage = "User not found with id : '123'";
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void genAIException_ShouldCreateWithMessage() {
        String message = "GenAI service error";
        GenAIException exception = new GenAIException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void genAIException_ShouldCreateWithMessageAndCause() {
        String message = "GenAI service error";
        RuntimeException cause = new RuntimeException("API error");
        GenAIException exception = new GenAIException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void genAIConfigurationException_ShouldCreateWithMessage() {
        String message = "Configuration error";
        GenAIConfigurationException exception = new GenAIConfigurationException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void genAIConfigurationException_ShouldCreateWithMessageAndCause() {
        String message = "Configuration error";
        RuntimeException cause = new RuntimeException("Config issue");
        GenAIConfigurationException exception = new GenAIConfigurationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void genAIRateLimitException_ShouldCreateWithMessage() {
        String message = "Rate limit exceeded";
        GenAIRateLimitException exception = new GenAIRateLimitException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void genAIRateLimitException_ShouldCreateWithMessageAndCause() {
        String message = "Rate limit exceeded";
        RuntimeException cause = new RuntimeException("Too many requests");
        GenAIRateLimitException exception = new GenAIRateLimitException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void genAITimeoutException_ShouldCreateWithMessage() {
        String message = "Request timeout";
        GenAITimeoutException exception = new GenAITimeoutException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void genAITimeoutException_ShouldCreateWithMessageAndCause() {
        String message = "Request timeout";
        RuntimeException cause = new RuntimeException("Connection timeout");
        GenAITimeoutException exception = new GenAITimeoutException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exceptions_ShouldBeInstanceOfRuntimeException() {
        assertTrue(new ResourceNotFoundException("test") instanceof RuntimeException);
        assertTrue(new GenAIException("test") instanceof RuntimeException);
        assertTrue(new GenAIConfigurationException("test") instanceof RuntimeException);
        assertTrue(new GenAIRateLimitException("test") instanceof RuntimeException);
        assertTrue(new GenAITimeoutException("test") instanceof RuntimeException);
    }

    @Test
    void exceptions_ShouldBeInstanceOfCorrectParent() {
        assertTrue(new GenAIConfigurationException("test") instanceof GenAIException);
        assertTrue(new GenAIRateLimitException("test") instanceof GenAIException);
        assertTrue(new GenAITimeoutException("test") instanceof GenAIException);
    }
}