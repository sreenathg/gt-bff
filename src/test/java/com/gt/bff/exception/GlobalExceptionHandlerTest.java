package com.gt.bff.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        // Setup method - mocks will be configured in individual tests
    }

    @Test
    void handleNoHandlerFoundException_ShouldReturn404() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", null);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoHandlerFoundException(ex, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("The requested resource was not found", response.getBody().getMessage());
    }

    @Test
    void handleMethodNotSupportedException_ShouldReturn405() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test");
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodNotSupportedException(ex, httpServletRequest);
        
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(405, response.getBody().getStatus());
        assertEquals("Method Not Allowed", response.getBody().getError());
        assertEquals("The requested method is not supported for this endpoint", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(ex, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptions_ShouldReturn400() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("object", "field", "error message");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getDetails());
        assertEquals("error message", response.getBody().getDetails().get("field"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void handleConstraintViolation_ShouldReturn400() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        ConstraintViolationException ex = new ConstraintViolationException("Constraint violation", Set.of(violation));
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolation(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Constraint violation", response.getBody().getMessage());
    }

    @Test
    void handleGenAITimeoutException_ShouldReturn408() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        GenAITimeoutException ex = new GenAITimeoutException("Timeout occurred");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenAITimeoutException(ex, webRequest);
        
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(408, response.getBody().getStatus());
        assertEquals("AI service request timed out. Please try again later.", response.getBody().getMessage());
    }

    @Test
    void handleGenAIRateLimitException_ShouldReturn429() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        GenAIRateLimitException ex = new GenAIRateLimitException("Rate limit exceeded");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenAIRateLimitException(ex, webRequest);
        
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(429, response.getBody().getStatus());
        assertEquals("AI service rate limit exceeded. Please try again later.", response.getBody().getMessage());
    }

    @Test
    void handleGenAIConfigurationException_ShouldReturn503() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        GenAIConfigurationException ex = new GenAIConfigurationException("Configuration error");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenAIConfigurationException(ex, webRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("AI service is temporarily unavailable due to configuration issues.", response.getBody().getMessage());
    }

    @Test
    void handleGenAIException_ShouldReturn502() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        GenAIException ex = new GenAIException("GenAI error");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenAIException(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().getStatus());
        assertEquals("AI service encountered an error. Please try again later.", response.getBody().getMessage());
    }

    @Test
    void handleGlobalException_ShouldReturn500() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        Exception ex = new Exception("Unexpected error");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalException(ex, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void handleGlobalException_ActuatorEndpoint_ShouldReturn500WithoutLogging() {
        Exception ex = new Exception("Actuator error");
        when(webRequest.getDescription(false)).thenReturn("uri=/actuator/health");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalException(ex, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("uri=/actuator/health", response.getBody().getPath());
    }

    @Test
    void handleGlobalException_FaviconRequest_ShouldReturn500WithoutLogging() {
        Exception ex = new Exception("favicon.ico not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/favicon.ico");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalException(ex, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}