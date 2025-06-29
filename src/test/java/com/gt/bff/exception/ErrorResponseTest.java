package com.gt.bff.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructor_WithAllFields_ShouldCreateErrorResponse() {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/test";
        Map<String, String> details = new HashMap<>();
        details.put("field", "error");
        
        ErrorResponse errorResponse = new ErrorResponse(timestamp, status, error, message, path, details);
        
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertEquals(details, errorResponse.getDetails());
    }

    @Test
    void constructor_WithoutDetails_ShouldCreateErrorResponse() {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String error = "Bad Request";
        String message = "Invalid input";
        String path = "/api/test";
        
        ErrorResponse errorResponse = new ErrorResponse(timestamp, status, error, message, path);
        
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertNull(errorResponse.getDetails());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyErrorResponse() {
        ErrorResponse errorResponse = new ErrorResponse();
        
        assertNull(errorResponse.getTimestamp());
        assertEquals(0, errorResponse.getStatus());
        assertNull(errorResponse.getError());
        assertNull(errorResponse.getMessage());
        assertNull(errorResponse.getPath());
        assertNull(errorResponse.getDetails());
    }

    @Test
    void builder_ShouldCreateErrorResponse() {
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = new HashMap<>();
        details.put("validation", "failed");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(422)
                .error("Unprocessable Entity")
                .message("Validation error")
                .path("/api/validate")
                .details(details)
                .build();
        
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(422, errorResponse.getStatus());
        assertEquals("Unprocessable Entity", errorResponse.getError());
        assertEquals("Validation error", errorResponse.getMessage());
        assertEquals("/api/validate", errorResponse.getPath());
        assertEquals(details, errorResponse.getDetails());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = new HashMap<>();
        details.put("code", "INVALID");
        
        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(500);
        errorResponse.setError("Internal Server Error");
        errorResponse.setMessage("Server error occurred");
        errorResponse.setPath("/internal");
        errorResponse.setDetails(details);
        
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("Server error occurred", errorResponse.getMessage());
        assertEquals("/internal", errorResponse.getPath());
        assertEquals(details, errorResponse.getDetails());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse errorResponse1 = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/test");
        ErrorResponse errorResponse2 = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/test");
        ErrorResponse errorResponse3 = new ErrorResponse(timestamp, 400, "Bad Request", "Invalid input", "/test");
        
        assertEquals(errorResponse1, errorResponse2);
        assertNotEquals(errorResponse1, errorResponse3);
    }

    @Test
    void hashCode_ShouldWorkCorrectly() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse errorResponse1 = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/test");
        ErrorResponse errorResponse2 = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/test");
        
        assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
    }

    @Test
    void toString_ShouldIncludeAllFields() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse errorResponse = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/test");
        
        String toString = errorResponse.toString();
        
        assertTrue(toString.contains("404"));
        assertTrue(toString.contains("Not Found"));
        assertTrue(toString.contains("Resource not found"));
        assertTrue(toString.contains("/test"));
    }
}