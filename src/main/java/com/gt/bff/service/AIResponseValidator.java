package com.gt.bff.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gt.bff.model.schema.AIResponseSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service for validating AI responses against defined schemas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIResponseValidator {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    
    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    private static final Pattern JSON_CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");
    private static final Pattern MALICIOUS_PATTERN = Pattern.compile("(?i)(script|javascript|<|>|eval|exec|system|cmd)", Pattern.CASE_INSENSITIVE);
    
    /**
     * Validates and parses travel search filters from AI response
     */
    public ValidationResult<AIResponseSchema.TravelSearchFilters> validateTravelSearchFilters(String aiResponse) {
        try {
            String cleanJson = extractAndCleanJson(aiResponse);
            if (cleanJson == null) {
                return ValidationResult.failure("No valid JSON found in AI response");
            }
            
            if (containsMaliciousContent(cleanJson)) {
                log.warn("Malicious content detected in AI response");
                return ValidationResult.failure("Invalid response content detected");
            }
            
            AIResponseSchema.TravelSearchFilters filters = objectMapper.readValue(cleanJson, AIResponseSchema.TravelSearchFilters.class);
            
            Set<ConstraintViolation<AIResponseSchema.TravelSearchFilters>> violations = validator.validate(filters);
            if (!violations.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                violations.forEach(v -> errors.append(v.getMessage()).append("; "));
                return ValidationResult.failure("Validation errors: " + errors.toString());
            }
            
            return ValidationResult.success(filters);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse travel search filters from AI response: {}", e.getMessage());
            return ValidationResult.failure("Invalid JSON format in AI response");
        } catch (Exception e) {
            log.error("Unexpected error validating travel search filters: {}", e.getMessage());
            return ValidationResult.failure("Validation failed due to unexpected error");
        }
    }
    
    /**
     * Validates and parses location extraction from AI response
     */
    public ValidationResult<AIResponseSchema.LocationExtraction> validateLocationExtraction(String aiResponse) {
        try {
            String cleanJson = extractAndCleanJson(aiResponse);
            if (cleanJson == null) {
                return ValidationResult.failure("No valid JSON found in AI response");
            }
            
            if (containsMaliciousContent(cleanJson)) {
                log.warn("Malicious content detected in AI response");
                return ValidationResult.failure("Invalid response content detected");
            }
            
            AIResponseSchema.LocationExtraction location = objectMapper.readValue(cleanJson, AIResponseSchema.LocationExtraction.class);
            
            Set<ConstraintViolation<AIResponseSchema.LocationExtraction>> violations = validator.validate(location);
            if (!violations.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                violations.forEach(v -> errors.append(v.getMessage()).append("; "));
                return ValidationResult.failure("Validation errors: " + errors.toString());
            }
            
            return ValidationResult.success(location);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse location extraction from AI response: {}", e.getMessage());
            return ValidationResult.failure("Invalid JSON format in AI response");
        } catch (Exception e) {
            log.error("Unexpected error validating location extraction: {}", e.getMessage());
            return ValidationResult.failure("Validation failed due to unexpected error");
        }
    }
    
    /**
     * Validates and parses travel advice from AI response
     */
    public ValidationResult<AIResponseSchema.TravelAdvice> validateTravelAdvice(String aiResponse) {
        try {
            String cleanJson = extractAndCleanJson(aiResponse);
            if (cleanJson == null) {
                // For travel advice, plain text is also acceptable
                if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                    if (containsMaliciousContent(aiResponse)) {
                        log.warn("Malicious content detected in AI response");
                        return ValidationResult.failure("Invalid response content detected");
                    }
                    
                    AIResponseSchema.TravelAdvice advice = new AIResponseSchema.TravelAdvice();
                    advice.setResponse(aiResponse.trim());
                    advice.setCategory("general");
                    advice.setConfidence(0.8);
                    
                    Set<ConstraintViolation<AIResponseSchema.TravelAdvice>> violations = validator.validate(advice);
                    if (!violations.isEmpty()) {
                        StringBuilder errors = new StringBuilder();
                        violations.forEach(v -> errors.append(v.getMessage()).append("; "));
                        return ValidationResult.failure("Validation errors: " + errors.toString());
                    }
                    
                    return ValidationResult.success(advice);
                }
                return ValidationResult.failure("No valid content found in AI response");
            }
            
            if (containsMaliciousContent(cleanJson)) {
                log.warn("Malicious content detected in AI response");
                return ValidationResult.failure("Invalid response content detected");
            }
            
            AIResponseSchema.TravelAdvice advice = objectMapper.readValue(cleanJson, AIResponseSchema.TravelAdvice.class);
            
            Set<ConstraintViolation<AIResponseSchema.TravelAdvice>> violations = validator.validate(advice);
            if (!violations.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                violations.forEach(v -> errors.append(v.getMessage()).append("; "));
                return ValidationResult.failure("Validation errors: " + errors.toString());
            }
            
            return ValidationResult.success(advice);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse travel advice from AI response: {}", e.getMessage());
            return ValidationResult.failure("Invalid JSON format in AI response");
        } catch (Exception e) {
            log.error("Unexpected error validating travel advice: {}", e.getMessage());
            return ValidationResult.failure("Validation failed due to unexpected error");
        }
    }
    
    /**
     * Validates basic AI response content for security and structure
     */
    public ValidationResult<String> validateBasicResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return ValidationResult.failure("AI response is empty");
        }
        
        if (containsMaliciousContent(aiResponse)) {
            log.warn("Malicious content detected in AI response");
            return ValidationResult.failure("Invalid response content detected");
        }
        
        if (aiResponse.length() > 10000) {
            return ValidationResult.failure("AI response exceeds maximum length");
        }
        
        return ValidationResult.success(aiResponse.trim());
    }
    
    /**
     * Safely converts AI response to Map for backward compatibility
     */
    public ValidationResult<Map<String, Object>> validateAndParseToMap(String aiResponse) {
        try {
            String cleanJson = extractAndCleanJson(aiResponse);
            if (cleanJson == null) {
                return ValidationResult.failure("No valid JSON found in AI response");
            }
            
            if (containsMaliciousContent(cleanJson)) {
                log.warn("Malicious content detected in AI response");
                return ValidationResult.failure("Invalid response content detected");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(cleanJson, Map.class);
            
            return ValidationResult.success(responseMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response to map: {}", e.getMessage());
            return ValidationResult.failure("Invalid JSON format in AI response");
        } catch (Exception e) {
            log.error("Unexpected error parsing AI response to map: {}", e.getMessage());
            return ValidationResult.failure("Parsing failed due to unexpected error");
        }
    }
    
    /**
     * Extracts JSON from code blocks or plain text
     */
    private String extractAndCleanJson(String response) {
        if (response == null || response.trim().isEmpty()) {
            return null;
        }
        
        // Try to extract from code blocks first
        java.util.regex.Matcher matcher = JSON_CODE_BLOCK_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // If no code blocks, try to find JSON-like content
        String trimmed = response.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }
        
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed;
        }
        
        return null;
    }
    
    /**
     * Checks for potentially malicious content in AI responses
     */
    private boolean containsMaliciousContent(String content) {
        if (content == null) {
            return false;
        }
        
        return MALICIOUS_PATTERN.matcher(content).find();
    }
    
    /**
     * Result wrapper for validation operations
     */
    public static class ValidationResult<T> {
        private final boolean valid;
        private final T data;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, T data, String errorMessage) {
            this.valid = valid;
            this.data = data;
            this.errorMessage = errorMessage;
        }
        
        public static <T> ValidationResult<T> success(T data) {
            return new ValidationResult<>(true, data, null);
        }
        
        public static <T> ValidationResult<T> failure(String errorMessage) {
            return new ValidationResult<>(false, null, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public T getData() {
            return data;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}