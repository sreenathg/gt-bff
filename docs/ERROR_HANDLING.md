# Error Handling & Data Models

## Overview

GT BFF API follows consistent error response patterns across all endpoints. This document covers error response formats, HTTP status codes, data model definitions, and validation error structures.

---

## Error Response Structure

### Standard Error Response
All endpoints return errors in this consistent format:

```json
{
  "timestamp": "2025-06-27T10:30:00.000Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error description",
  "path": "/api/v1/gt/endpoint"
}
```

### Enhanced Error Response (With Details)
For validation errors and complex scenarios:

```json
{
  "timestamp": "2025-06-27T11:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Search input validation failed",
  "path": "/api/v1/gt/process-search",
  "details": {
    "errors": [
      "Search input must contain at least 5 words for better travel search results",
      "Search input contains potentially harmful SQL keywords"
    ],
    "warnings": [
      "Search input contains inappropriate language",
      "Consider adding date or passenger information for better results"
    ]
  }
}
```

### Error Response Fields
| Field | Type | Description | Always Present |
|-------|------|-------------|----------------|
| `timestamp` | string | Error occurrence time (ISO 8601 format) | Yes |
| `status` | integer | HTTP status code | Yes |
| `error` | string | HTTP status message | Yes |
| `message` | string | Human-readable error description | Yes |
| `path` | string | Request path that caused the error | Yes |
| `details` | object | Additional error information | No |

---

## HTTP Status Codes

### Success Codes
| Code | Status | Usage |
|------|--------|--------|
| 200 | OK | Request successful, data returned |

### Client Error Codes  
| Code | Status | Usage | Common Causes |
|------|--------|-------|---------------|
| 400 | Bad Request | Invalid request parameters/body | Validation failures, malformed JSON |
| 404 | Not Found | Resource not found | Airports file missing, invalid endpoint |

### Server Error Codes
| Code | Status | Usage | Common Causes |
|------|--------|-------|---------------|
| 500 | Internal Server Error | Server-side processing error | Service failures, unexpected exceptions |

---

## Validation Errors

### Validation Error Structure
```json
{
  "timestamp": "2025-06-27T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request", 
  "message": "Validation failed",
  "path": "/api/v1/gt/process-search",
  "details": [
    {
      "field": "searchInput",
      "rejectedValue": "ab",
      "message": "Search input must be at least 3 characters long"
    },
    {
      "field": "searchInput", 
      "rejectedValue": "ab",
      "message": "Search input must contain at least 5 words for better travel search results"
    }
  ]
}
```

### Custom Validation Error Structure
For complex validation scenarios (Process Search endpoint):

```json
{
  "timestamp": "2025-06-27T11:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Search input validation failed",
  "path": "/api/v1/gt/process-search",
  "details": {
    "errors": [
      "Search input must contain at least 5 words for better travel search results",
      "Search input contains potentially harmful SQL keywords"
    ],
    "warnings": [
      "Search input contains inappropriate language",
      "Consider adding date or passenger information for better results"
    ]
  }
}
```

### Validation Error Categories
| Category | Type | Blocking | Examples |
|----------|------|----------|----------|
| **Basic Validation** | Error | Yes | Length constraints, required fields, format validation |
| **Security Validation** | Error | Yes | SQL injection, XSS, excessive special characters |
| **Content Warnings** | Warning | No | Profanity, non-travel content, missing information |

---

## Data Models

### Request Models

#### SearchRequest
Used by Process Search endpoint:

```json
{
  "searchInput": "string"
}
```

**Validation Constraints:**
- `@NotBlank`: Cannot be empty or null
- `@Size(min=3, max=500)`: Must be between 3 and 500 characters
- **Word Count**: Minimum 5 words required
- **Security Patterns**: No SQL injection, XSS, or excessive special characters
- **Content Patterns**: Travel-related keywords preferred
- **Character Set**: Alphanumeric characters and safe punctuation only

```java
public class SearchRequest {
    @NotBlank(message = "Search input cannot be blank")
    @Size(min = 3, max = 500, message = "Search input must be between 3 and 500 characters")
    private String searchInput;
}
```

### Response Models

#### Search Filter Response
Returned by both search endpoints:

```json
{
  "from": "string",           // Departure location
  "to": "string",             // Destination location
  "fromDate": "string",       // Departure date (ISO format: YYYY-MM-DD)
  "toDate": "string",         // Return date (ISO format: YYYY-MM-DD)
  "passengers": "integer",    // Number of passengers (min: 1, max: 10)
  "trip": "string",           // Trip type (Round-Trip, One-Way)
  "searchContext": "string"   // Original search input (optional)
}
```

**Field Details:**
| Field | Type | Format | Constraints | Default |
|-------|------|--------|-------------|---------|
| `from` | string | Location with airport code | - | "San Francisco SFO" |
| `to` | string | Location with airport code | - | "London LHR" |
| `fromDate` | string | ISO date (YYYY-MM-DD) | Future date | Today + 7 days |
| `toDate` | string | ISO date (YYYY-MM-DD) | After fromDate | Today + 14 days |
| `passengers` | integer | Positive integer | 1-10 | 1 |
| `trip` | string | Enum value | Round-Trip, One-Way | "Round-Trip" |
| `searchContext` | string | Original input | Optional | Present if input provided |

#### Airport Response
Returned by Airports endpoint:

```json
[
  {
    "iata": "string",          // 3-letter IATA code (required)
    "city": "string",          // City name (required)
    "country": "string"        // Country code (required)
  }
]
```

**Field Details:**
| Field | Type | Format | Description | Example |
|-------|------|--------|-------------|---------|
| `iata` | string | 3 uppercase letters | IATA airport code | "JFK" |
| `city` | string | City name | Airport city location | "New York" |
| `country` | string | ISO country code | Country where airport is located | "US" |

### Error Models

#### Standard Error Response
```json
{
  "timestamp": "string",     // Error timestamp (ISO 8601)
  "status": "integer",       // HTTP status code
  "error": "string",         // HTTP status message
  "message": "string",       // Error description
  "path": "string"           // Request path
}
```

#### Enhanced Error Response
```json
{
  "timestamp": "string",     // Error timestamp (ISO 8601)
  "status": "integer",       // HTTP status code
  "error": "string",         // HTTP status message
  "message": "string",       // Error description
  "path": "string",          // Request path
  "details": {
    "errors": ["string"],    // Blocking validation errors
    "warnings": ["string"]  // Non-blocking validation warnings
  }
}
```

#### Validation Error Detail
```json
{
  "field": "string",         // Field name that failed validation
  "rejectedValue": "any",    // Value that was rejected
  "message": "string"        // Validation failure message
}
```

---

## Error Handling by Endpoint

### Search Filters Endpoint
| Error Type | Status | Response Format |
|------------|--------|----------------|
| Server Error | 500 | Standard Error Response |

**Example:**
```json
{
  "timestamp": "2025-06-27T10:30:00.000Z",
  "status": 500, 
  "error": "Internal Server Error",
  "message": "Failed to generate search filters",
  "path": "/api/v1/gt/search-filters"
}
```

### Process Search Endpoint
| Error Type | Status | Response Format |
|------------|--------|----------------|
| Validation Error | 400 | Enhanced Error Response |
| Server Error | 500 | Standard Error Response |

**Validation Error Example:**
```json
{
  "timestamp": "2025-06-27T11:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Search input validation failed",
  "path": "/api/v1/gt/process-search",
  "details": {
    "errors": [
      "Search input must contain at least 5 words for better travel search results"
    ],
    "warnings": []
  }
}
```

### Airports Endpoint
| Error Type | Status | Response Format |
|------------|--------|----------------|
| Not Found | 404 | Standard Error Response |
| Server Error | 500 | Standard Error Response |

**Not Found Example:**
```json
{
  "timestamp": "2025-06-27T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Airports data not available",
  "path": "/api/v1/gt/airports"
}
```

---

## Exception Handling Implementation

### Global Exception Handler
The application uses `@ControllerAdvice` for centralized exception handling:

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
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        List<Map<String, Object>> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> {
                Map<String, Object> errorDetail = new HashMap<>();
                errorDetail.put("field", error.getField());
                errorDetail.put("rejectedValue", error.getRejectedValue());
                errorDetail.put("message", error.getDefaultMessage());
                return errorDetail;
            })
            .collect(Collectors.toList());
            
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Validation failed")
                .path(request.getRequestURI())
                .details(Map.of("validationErrors", validationErrors))
                .build();
                
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
```

### Custom Exceptions

#### ValidationException
Used for comprehensive validation failures:

```java
public class ValidationException extends RuntimeException {
    private final List<String> errors;
    private final List<String> warnings;
    
    public ValidationException(String message, List<String> errors, List<String> warnings) {
        super(message);
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
    }
}
```

---

## Client Error Handling

### JavaScript/Fetch Example
```javascript
async function handleApiCall(url, options = {}) {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      const errorData = await response.json();
      
      // Handle validation errors
      if (response.status === 400 && errorData.details) {
        console.error('Validation Errors:', errorData.details.errors);
        console.warn('Validation Warnings:', errorData.details.warnings);
        throw new ValidationError(errorData);
      }
      
      // Handle other client errors
      if (response.status >= 400 && response.status < 500) {
        throw new ClientError(errorData);
      }
      
      // Handle server errors
      throw new ServerError(errorData);
    }
    
    return await response.json();
  } catch (error) {
    if (error instanceof TypeError) {
      // Network error
      throw new NetworkError('Failed to connect to server');
    }
    throw error;
  }
}

// Custom error classes
class ApiError extends Error {
  constructor(errorData) {
    super(errorData.message);
    this.status = errorData.status;
    this.path = errorData.path;
    this.timestamp = errorData.timestamp;
  }
}

class ValidationError extends ApiError {
  constructor(errorData) {
    super(errorData);
    this.errors = errorData.details?.errors || [];
    this.warnings = errorData.details?.warnings || [];
  }
}

class ClientError extends ApiError {}
class ServerError extends ApiError {}
class NetworkError extends Error {}
```

### Python/Requests Example
```python
import requests
from typing import List, Optional

class APIError(Exception):
    def __init__(self, message: str, status: int, path: str, timestamp: str):
        super().__init__(message)
        self.status = status
        self.path = path
        self.timestamp = timestamp

class ValidationError(APIError):
    def __init__(self, message: str, status: int, path: str, timestamp: str, 
                 errors: List[str], warnings: List[str]):
        super().__init__(message, status, path, timestamp)
        self.errors = errors or []
        self.warnings = warnings or []

def handle_api_request(url: str, method: str = 'GET', **kwargs):
    try:
        response = requests.request(method, url, **kwargs)
        
        if not response.ok:
            error_data = response.json()
            
            if response.status_code == 400 and 'details' in error_data:
                raise ValidationError(
                    message=error_data['message'],
                    status=error_data['status'],
                    path=error_data['path'],
                    timestamp=error_data['timestamp'],
                    errors=error_data['details'].get('errors', []),
                    warnings=error_data['details'].get('warnings', [])
                )
            
            raise APIError(
                message=error_data['message'],
                status=error_data['status'],
                path=error_data['path'],
                timestamp=error_data['timestamp']
            )
        
        return response.json()
        
    except requests.exceptions.RequestException as e:
        raise ConnectionError(f"Failed to connect to API: {e}")
```

---

## Error Logging

### Server-Side Logging
The application logs errors at appropriate levels:

```java
// Validation warnings (non-blocking)
log.warn("Search input validation warnings: {}", validationResult.getWarnings());

// Security attempts
log.warn("Potential SQL injection attempt detected: {}", input);
log.warn("Potential XSS attempt detected: {}", input);

// Service errors
log.error("Failed to download airport data: {}", e.getMessage(), e);

// General errors
log.error("Unexpected error in controller: {}", e.getMessage(), e);
```

### Error Log Formats
```
WARN  - Search input validation warnings: [Search input contains inappropriate language]
WARN  - Potential SQL injection attempt detected: Boston'; DROP TABLE users; --
ERROR - Failed to download airport data: Connection timeout after 30 seconds
ERROR - Unexpected error in processSearch: java.lang.NullPointerException
```

---

## Testing Error Scenarios

### Manual Error Testing
```bash
# Test validation error (too few words)
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle"}'

# Test security validation (SQL injection)
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston'; DROP TABLE users; --"}'

# Test 404 error (when airports file doesn't exist)
curl "http://localhost:8081/api/v1/gt/airports"

# Test malformed JSON
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle for five people"'  # Missing closing brace
```

### Integration Test Examples
```java
@Test
void processSearch_WithInvalidInput_ShouldReturnValidationError() {
    SearchRequest request = new SearchRequest();
    request.setSearchInput("ab");  // Too short, too few words
    
    ResponseEntity<Map> response = restTemplate.postForEntity(
        "/api/v1/gt/process-search", request, Map.class);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsKey("details");
    
    Map<String, Object> details = (Map<String, Object>) response.getBody().get("details");
    List<String> errors = (List<String>) details.get("errors");
    
    assertThat(errors).contains("Search input must contain at least 5 words for better travel search results");
}
```

---

## Related Documentation

- **[Process Search](./PROCESS_SEARCH.md)** - Detailed validation error examples
- **[Validation System](./VALIDATION_SYSTEM.md)** - Comprehensive validation architecture
- **[API Overview](./API_OVERVIEW.md)** - General API information