package com.gt.bff.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

/**
 * Utility class for handling common response patterns and exception handling.
 */
@Slf4j
public final class ResponseHelper {

    private ResponseHelper() {
        // Utility class
    }

    /**
     * Executes a service operation and handles common exceptions.
     * 
     * @param operation the operation to execute
     * @param operationName the name of the operation for logging
     * @param <T> the return type
     * @return the result wrapped in a ResponseEntity
     */
    public static <T> ResponseEntity<T> executeServiceOperation(
            Supplier<T> operation, 
            String operationName) {
        
        log.info("Executing operation: {}", operationName);
        
        try {
            T result = operation.get();
            log.debug("Successfully completed operation: {}", operationName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error executing operation: {}", operationName, e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "An error occurred while processing your request", 
                e
            );
        }
    }

    /**
     * Executes a service operation with additional logging context.
     * 
     * @param operation the operation to execute
     * @param operationName the name of the operation for logging
     * @param context additional context for logging
     * @param <T> the return type
     * @return the result wrapped in a ResponseEntity
     */
    public static <T> ResponseEntity<T> executeServiceOperation(
            Supplier<T> operation, 
            String operationName,
            String context) {
        
        log.info("Executing operation: {} with context: {}", operationName, context);
        
        try {
            T result = operation.get();
            log.debug("Successfully completed operation: {} with context: {}", operationName, context);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error executing operation: {} with context: {}", operationName, context, e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "An error occurred while processing your request", 
                e
            );
        }
    }
}