# Validation System Documentation

## Overview

GT BFF implements a comprehensive multi-layered validation system that ensures data integrity, security, and user experience. The validation system operates at multiple levels: annotation-based validation, custom business logic, security pattern detection, and content analysis.

---

## Validation Architecture

### Multi-Layer Validation Approach

```
Request Input
    ↓
1. Jakarta Bean Validation (@Valid, @NotBlank, @Size)
    ↓
2. Custom Business Logic (SearchInputValidator)
    ↓
3. Security Pattern Detection (SQL, XSS, Special Chars)
    ↓
4. Content Analysis (Travel Context, Profanity, Format)
    ↓
Response (Success/Error with Details)
```

### Validation Flow Components

#### 1. Controller Layer Validation
- **Annotation-Based**: Jakarta Bean Validation annotations
- **Trigger**: `@Valid` annotation on request parameters
- **Scope**: Basic format and constraint validation

#### 2. Service Layer Validation  
- **Component**: `SearchInputValidator`
- **Scope**: Business logic, security, and content validation
- **Result**: `ValidationResult` with errors and warnings

#### 3. Exception Handling
- **Component**: `GlobalExceptionHandler`
- **Scope**: Error formatting and response generation
- **Output**: Structured error responses with details

---

## Validation Layers Detail

### Layer 1: Jakarta Bean Validation

#### Annotation-Based Validation
Applied at the DTO level using standard validation annotations:

```java
public class SearchRequest {
    @NotBlank(message = "Search input cannot be blank")
    @Size(min = 3, max = 500, message = "Search input must be between 3 and 500 characters")
    private String searchInput;
}
```

#### Available Annotations
| Annotation | Purpose | Parameters | Message |
|------------|---------|------------|---------|
| `@NotBlank` | Prevents null/empty/whitespace | - | "Search input cannot be blank" |
| `@Size` | Length constraints | min=3, max=500 | "Search input must be between 3 and 500 characters" |
| `@Valid` | Triggers nested validation | - | Activates validation cascade |

#### Validation Process
1. **Request Binding**: Spring automatically validates request bodies
2. **Constraint Checking**: Each annotation constraint is evaluated
3. **Error Collection**: Violations collected in `BindingResult`
4. **Exception Throwing**: `MethodArgumentNotValidException` on failures

### Layer 2: Security Validation

#### SQL Injection Protection
**Pattern:** `(?i).*(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror).*`

```java
private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
    "(?i).*(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror).*"
);
```

**Detected Keywords:**
- **SQL Commands**: `union`, `select`, `insert`, `update`, `delete`, `drop`
- **DDL Commands**: `create`, `alter`
- **Execution**: `exec`, `execute`
- **Scripting**: `script`, `javascript`, `vbscript`
- **Events**: `onload`, `onerror`

**Implementation:**
```java
if (SQL_INJECTION_PATTERN.matcher(trimmedInput).matches()) {
    errors.add("Search input contains potentially harmful SQL keywords");
    log.warn("Potential SQL injection attempt detected: {}", input);
}
```

#### Cross-Site Scripting (XSS) Protection
**Pattern:** `(?i).*(<script|</script|<iframe|</iframe|<object|</object|<embed|</embed|javascript:|vbscript:|onload=|onerror=|onclick=).*`

```java
private static final Pattern XSS_PATTERN = Pattern.compile(
    "(?i).*(<script|</script|<iframe|</iframe|<object|</object|<embed|</embed|javascript:|vbscript:|onload=|onerror=|onclick=).*"
);
```

**Detected Elements:**
- **Script Tags**: `<script>`, `</script>`
- **Embed Elements**: `<iframe>`, `<object>`, `<embed>`
- **Protocols**: `javascript:`, `vbscript:`
- **Event Handlers**: `onload=`, `onerror=`, `onclick=`

#### Special Character Validation
**Pattern:** `.*[<>\"'&;{}\\[\\]]{5,}.*`

```java
private static final Pattern EXCESSIVE_SPECIAL_CHARS = Pattern.compile(".*[<>\\\"'&;{}\\\\[\\\\]]{5,}.*");
```

**Detection Logic:**
- Identifies 5 or more consecutive special characters
- Characters: `< > " ' & ; { } [ ]`
- Purpose: Prevent potential injection attempts and malformed input

### Layer 3: Content Validation

#### Word Count Validation
**Requirement:** Minimum 5 words for optimal travel search results

```java
// Word count validation
String[] words = trimmedInput.split("\\s+");
if (words.length < 5) {
    errors.add("Search input must contain at least 5 words for better travel search results");
}
```

**Implementation Details:**
- **Split Pattern**: `\\s+` (one or more whitespace characters)
- **Counting Logic**: Array length after split
- **Minimum Requirement**: 5 words
- **Error Type**: Blocking (prevents processing)

#### Profanity Detection
**Pattern:** `(?i).*(fuck|shit|damn|hell|ass|bitch|bastard|crap).*`

```java
private static final Pattern PROFANITY_PATTERN = Pattern.compile(
    "(?i).*(fuck|shit|damn|hell|ass|bitch|bastard|crap).*"
);

if (PROFANITY_PATTERN.matcher(trimmedInput).matches()) {
    warnings.add("Search input contains inappropriate language");
}
```

**Behavior:**
- **Action**: Warning only (non-blocking)
- **Purpose**: Content quality feedback
- **Processing**: Request continues with warning

#### Character Set Validation
**Pattern:** `^[a-zA-Z0-9\\s\\-,.():/]+$`

```java
private static final Pattern VALID_TRAVEL_CHARS = Pattern.compile("^[a-zA-Z0-9\\\\s\\\\-,.():/]+$");

if (!VALID_TRAVEL_CHARS.matcher(trimmedInput).matches()) {
    warnings.add("Search input contains unusual characters for travel search");
}
```

**Allowed Characters:**
- **Letters**: a-z, A-Z
- **Numbers**: 0-9
- **Whitespace**: spaces
- **Punctuation**: `- , . ( ) : /`

### Layer 4: Travel-Specific Validation

#### Travel Context Analysis
```java
private void validateTravelContent(String input, List<String> warnings) {
    String lowerInput = input.toLowerCase();
    
    // Check for travel-related keywords
    boolean hasLocationKeywords = lowerInput.contains("to") || 
                                lowerInput.contains("from") || 
                                lowerInput.contains("flight") ||
                                lowerInput.contains("trip") ||
                                lowerInput.contains("travel");
    
    if (!hasLocationKeywords && input.length() > 20) {
        warnings.add("Search input doesn't appear to be travel-related");
    }
}
```

#### Travel Keywords Detection
**Primary Keywords:** `to`, `from`, `flight`, `trip`, `travel`
- **Purpose**: Identify travel-related content
- **Logic**: Case-insensitive substring matching
- **Action**: Warning if no travel keywords found in longer inputs

#### Temporal Information Validation
**Time References:** `tomorrow`, `next`, `week`, `month`
**Months:** `january`, `february`, `march`, `april`, `may`, `june`, `july`, `august`, `september`, `october`, `november`, `december`

```java
boolean hasTimeKeywords = lowerInput.contains("tomorrow") ||
                        lowerInput.contains("next") ||
                        lowerInput.contains("week") ||
                        lowerInput.contains("month") ||
                        // ... month names
```

#### Passenger Information Detection
**Keywords:** `people`, `person`, `passenger`, `adult`, `child`

```java
boolean hasPassengerKeywords = lowerInput.contains("people") ||
                             lowerInput.contains("person") ||
                             lowerInput.contains("passenger") ||
                             lowerInput.contains("adult") ||
                             lowerInput.contains("child");

if (input.length() > 30 && !hasTimeKeywords && !hasPassengerKeywords) {
    warnings.add("Consider adding date or passenger information for better results");
}
```

---

## Validation Result Structure

### ValidationResult Class
```java
public static class ValidationResult {
    private final boolean valid;           // Overall validation status
    private final List<String> errors;     // Blocking validation errors
    private final List<String> warnings;   // Non-blocking suggestions
    
    public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
    }
    
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
}
```

### Result Processing Logic
```java
boolean isValid = errors.isEmpty();

if (isValid && !warnings.isEmpty()) {
    log.info("Search input validation passed with warnings: {}", warnings);
}

return new ValidationResult(isValid, errors, warnings);
```

---

## Complete Validation Rules Reference

### Blocking Validations (Errors)
| Rule | Constraint | Message | Pattern/Logic |
|------|------------|---------|---------------|
| **Not Blank** | Cannot be null/empty/whitespace | "Search input cannot be blank" | `@NotBlank` |
| **Character Length** | 3-500 characters | "Search input must be between 3 and 500 characters" | `@Size(min=3, max=500)` |
| **Word Count** | Minimum 5 words | "Search input must contain at least 5 words for better travel search results" | `split("\\s+").length >= 5` |
| **SQL Injection** | No SQL keywords | "Search input contains potentially harmful SQL keywords" | Regex pattern matching |
| **XSS Attack** | No script elements | "Search input contains potentially harmful script content" | Regex pattern matching |
| **Special Characters** | Max 4 consecutive | "Search input contains excessive special characters" | Regex pattern matching |

### Non-Blocking Validations (Warnings)
| Rule | Condition | Message | Action |
|------|-----------|---------|--------|
| **Profanity** | Contains swear words | "Search input contains inappropriate language" | Log warning, continue |
| **Character Set** | Non-standard characters | "Search input contains unusual characters for travel search" | Log warning, continue |
| **Travel Context** | No travel keywords | "Search input doesn't appear to be travel-related" | Suggest improvement |
| **Missing Details** | No date/passenger info | "Consider adding date or passenger information for better results" | Suggest completion |

---

## Validation Implementation

### SearchInputValidator Component
```java
@Component
@Slf4j
public class SearchInputValidator {
    
    public ValidationResult validateSearchInput(String input) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 1. Basic validation
        if (input == null || input.trim().isEmpty()) {
            errors.add("Search input cannot be empty");
            return new ValidationResult(false, errors, warnings);
        }
        
        String trimmedInput = input.trim();
        
        // 2. Length validation
        validateLength(trimmedInput, errors);
        
        // 3. Word count validation
        validateWordCount(trimmedInput, errors);
        
        // 4. Security validation
        validateSecurity(trimmedInput, errors);
        
        // 5. Content validation
        validateContent(trimmedInput, warnings);
        
        // 6. Travel-specific validation
        validateTravelContent(trimmedInput, warnings);
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings);
    }
}
```

### Controller Integration
```java
@PostMapping("/process-search")
public ResponseEntity<Map<String, Object>> processSearch(@RequestBody @Valid SearchRequest searchRequest) {
    // Custom validation
    SearchInputValidator.ValidationResult validationResult = 
        searchInputValidator.validateSearchInput(searchRequest.getSearchInput());
    
    if (!validationResult.isValid()) {
        throw new ValidationException(
            "Search input validation failed", 
            validationResult.getErrors(), 
            validationResult.getWarnings()
        );
    }
    
    // Log warnings if any
    if (validationResult.hasWarnings()) {
        log.warn("Search input validation warnings: {}", validationResult.getWarnings());
    }
    
    return processValidatedInput(searchRequest.getSearchInput());
}
```

---

## Exception Handling

### ValidationException
Custom exception for comprehensive validation failures:

```java
public class ValidationException extends RuntimeException {
    private final List<String> errors;
    private final List<String> warnings;
    
    public ValidationException(String message, List<String> errors, List<String> warnings) {
        super(message);
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
    }
    
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
}
```

### Global Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("errors", ex.getErrors());
        details.put("warnings", ex.getWarnings());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .details(details)
                .build();
                
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
```

---

## Testing Strategy

### Comprehensive Test Coverage
The validation system includes 18 test cases covering all validation scenarios:

#### Test Categories
1. **Valid Input Tests** (5 tests)
   - Perfect travel queries
   - Minimum word count validation
   - Travel context validation
   - Complete information validation

2. **Security Tests** (3 tests)  
   - SQL injection detection
   - XSS attack detection
   - Excessive special characters

3. **Length Tests** (4 tests)
   - Character count validation
   - Word count validation
   - Empty input validation
   - Maximum length validation

4. **Content Tests** (4 tests)
   - Profanity detection (warning)
   - Character set validation (warning)
   - Travel relevance analysis
   - Missing information detection

5. **Edge Case Tests** (2 tests)
   - Multiple validation issues
   - Complex scenarios

### Test Implementation Example
```java
@Test
void validateSearchInput_WithExactlyFiveWords_ShouldPass() {
    // Given
    String fiveWordInput = "Boston to Seattle next Friday";
    
    // When
    SearchInputValidator.ValidationResult result = validator.validateSearchInput(fiveWordInput);
    
    // Then
    assertTrue(result.isValid());
    assertFalse(result.getErrors().contains("Search input must contain at least 5 words for better travel search results"));
}

@Test  
void validateSearchInput_WithSQLInjection_ShouldFail() {
    // Given
    String sqlInput = "Boston'; DROP TABLE users; --";
    
    // When
    SearchInputValidator.ValidationResult result = validator.validateSearchInput(sqlInput);
    
    // Then
    assertFalse(result.isValid());
    assertTrue(result.getErrors().contains("Search input contains potentially harmful SQL keywords"));
}
```

---

## Performance Considerations

### Validation Performance
- **Pattern Matching**: Compiled regex patterns for efficiency
- **Early Exit**: Validation stops at first critical error for basic checks
- **Logging Overhead**: Minimal logging for security events only
- **Memory Usage**: Lightweight ValidationResult objects

### Optimization Techniques
```java
// Pre-compiled patterns for performance
private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("...", Pattern.CASE_INSENSITIVE);

// Early exit for null/empty inputs
if (input == null || input.trim().isEmpty()) {
    errors.add("Search input cannot be empty");
    return new ValidationResult(false, errors, warnings);
}

// Efficient string operations
String trimmedInput = input.trim();
String lowerInput = trimmedInput.toLowerCase();
```

---

## Security Considerations

### Defense in Depth
1. **Input Validation**: Multiple validation layers
2. **Pattern Detection**: Comprehensive security patterns
3. **Logging**: Security event logging for monitoring
4. **Error Handling**: Safe error messages without information disclosure

### Security Logging
```java
// Security events logged for monitoring
log.warn("Potential SQL injection attempt detected: {}", input);
log.warn("Potential XSS attempt detected: {}", input);

// Validation warnings logged for analysis
log.info("Search input validation passed with warnings: {}", warnings);
```

### Secure Implementation
- **No Information Leakage**: Error messages don't reveal system internals
- **Pattern Obfuscation**: Security patterns not exposed in responses
- **Safe Defaults**: Fail-safe validation behavior
- **Audit Trail**: All security events logged for review

---

## Configuration & Customization

### Adding New Validation Rules
```java
// 1. Add new pattern
private static final Pattern NEW_PATTERN = Pattern.compile("...");

// 2. Add validation method
private void validateNewRule(String input, List<String> errors) {
    if (NEW_PATTERN.matcher(input).matches()) {
        errors.add("Custom validation message");
    }
}

// 3. Integrate in main validation method
public ValidationResult validateSearchInput(String input) {
    // ... existing validation
    validateNewRule(trimmedInput, errors);
    // ... rest of validation
}
```

### Customizing Error Messages
```java
// Externalize messages to properties
@Value("${validation.word-count.message:Search input must contain at least 5 words}")
private String wordCountMessage;

// Use in validation
if (words.length < minWords) {
    errors.add(wordCountMessage);
}
```

---

## Monitoring & Metrics

### Validation Metrics
- **Success Rate**: Percentage of inputs passing validation
- **Error Types**: Distribution of validation failures
- **Security Events**: Count of potential attacks detected
- **Warning Frequency**: Non-blocking validation issues

### Implementation Example
```java
@Component
public class ValidationMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordValidationResult(ValidationResult result) {
        Counter.builder("validation.attempts")
                .tag("status", result.isValid() ? "success" : "failure")
                .register(meterRegistry)
                .increment();
                
        if (!result.isValid()) {
            result.getErrors().forEach(error -> 
                Counter.builder("validation.errors")
                        .tag("type", categorizeError(error))
                        .register(meterRegistry)
                        .increment()
            );
        }
    }
}
```

---

## Related Documentation

- **[Process Search](./PROCESS_SEARCH.md)** - Endpoint using comprehensive validation
- **[Error Handling](./ERROR_HANDLING.md)** - Error response formats and handling
- **[API Overview](./API_OVERVIEW.md)** - General API architecture