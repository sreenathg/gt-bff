package com.gt.bff.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SearchInputValidator {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 500;
    private static final int MIN_WORDS = 5;
    
    // Security patterns
    private static final Pattern SQL_INJECTION_PATTERN = 
        Pattern.compile("(?i).*(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror).*");
    private static final Pattern XSS_PATTERN = 
        Pattern.compile("(?i).*(<script|</script|<iframe|</iframe|<object|</object|<embed|</embed|javascript:|vbscript:|onload=|onerror=|onclick=).*");
    private static final Pattern EXCESS_SPECIAL_CHARS_PATTERN = 
        Pattern.compile(".*[<>\"'&;{}\\[\\]]{5,}.*");
    
    // Content patterns
    private static final Pattern VALID_CHARACTERS_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9\\s\\-,\\.()://]+$");
    private static final Pattern TRAVEL_KEYWORDS_PATTERN = 
        Pattern.compile("(?i)\\b(to|from|flight|trip|travel|hotel|car rental|vacation|destination)\\b");
    
    public ValidationResult validate(String input) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check for null or empty input
        if (input == null || input.trim().isEmpty()) {
            errors.add("Search input cannot be empty");
            return new ValidationResult(false, errors, warnings);
        }
        
        // Check length constraints
        if (input.length() < MIN_LENGTH) {
            errors.add(String.format("Input must be at least %d characters long", MIN_LENGTH));
        } else if (input.length() > MAX_LENGTH) {
            errors.add(String.format("Input cannot exceed %d characters", MAX_LENGTH));
        }
        
        // Check word count
        String[] words = input.trim().split("\\s+");
        if (words.length < MIN_WORDS) {
            errors.add(String.format("Input must contain at least %d words", MIN_WORDS));
        }
        
        // Check for security issues
        if (SQL_INJECTION_PATTERN.matcher(input).matches()) {
            errors.add("Input contains potential SQL injection attempt");
        }
        
        if (XSS_PATTERN.matcher(input).matches()) {
            errors.add("Input contains potential XSS attack attempt");
        }
        
        if (EXCESS_SPECIAL_CHARS_PATTERN.matcher(input).matches()) {
            errors.add("Input contains excessive special characters");
        }
        
        // Check for content warnings
        if (!VALID_CHARACTERS_PATTERN.matcher(input).matches()) {
            warnings.add("Input contains unusual characters");
        }
        
        if (!TRAVEL_KEYWORDS_PATTERN.matcher(input).find() && input.length() > 20) {
            warnings.add("No travel-related keywords detected in input");
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
            this.warnings = warnings != null ? warnings : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
    }
}
