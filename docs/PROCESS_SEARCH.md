# Process Search Endpoint

## Overview

The Process Search endpoint processes natural language search input and returns enhanced search filters with comprehensive validation. This endpoint includes multi-layered security validation, content analysis, and travel-specific requirements including a minimum 5-word requirement for optimal search results.

---

## Endpoint Details

**URL:** `/api/v1/gt/process-search`  
**Method:** `POST`  
**Content-Type:** `application/json`

---

## Request Format

### Request Body
```json
{
  "searchInput": "Boston to Seattle for 2 people departing next Friday"
}
```

### Request Schema
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `searchInput` | string | Yes | 3-500 characters, minimum 5 words, travel-related content preferred |

---

## Validation Rules

### 1. Basic Validation (Jakarta Bean Validation)
- **@NotBlank**: Cannot be null, empty, or whitespace-only
- **@Size(min=3, max=500)**: Must be between 3 and 500 characters

### 2. Word Count Validation
- **Minimum 5 words**: Required for better travel search results
- **Split by whitespace**: Words are counted using `\s+` regex split
- **Example**: `"Boston to Seattle next Friday"` = 5 words ✅

### 3. Security Validation Patterns

#### SQL Injection Protection
**Pattern:** `(?i).*(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror).*`

**Detected Keywords:**
- SQL Commands: `union`, `select`, `insert`, `update`, `delete`, `drop`, `create`, `alter`
- Execution: `exec`, `execute`
- Scripting: `script`, `javascript`, `vbscript`
- Events: `onload`, `onerror`

#### Cross-Site Scripting (XSS) Protection
**Pattern:** `(?i).*(<script|</script|<iframe|</iframe|<object|</object|<embed|</embed|javascript:|vbscript:|onload=|onerror=|onclick=).*`

**Detected Elements:**
- Script tags: `<script>`, `</script>`
- Embed elements: `<iframe>`, `<object>`, `<embed>`
- Protocols: `javascript:`, `vbscript:`
- Event handlers: `onload=`, `onerror=`, `onclick=`

#### Excessive Special Characters
**Pattern:** `.*[<>\"'&;{}\\[\\]]{5,}.*`

**Detected:** 5 or more consecutive special characters: `< > " ' & ; { } [ ]`

### 4. Content Validation

#### Profanity Detection (Warning Only)
**Pattern:** `(?i).*(fuck|shit|damn|hell|ass|bitch|bastard|crap).*`
- **Action:** Non-blocking warning
- **Response:** Continues processing with warning message

#### Valid Character Set (Warning Only)
**Pattern:** `^[a-zA-Z0-9\\s\\-,.():/]+$`
- **Allowed:** Letters, numbers, spaces, and safe punctuation: `- , . ( ) : /`
- **Action:** Warning for unusual characters (non-blocking)

### 5. Travel-Specific Content Validation

#### Travel Keywords Detection
**Primary Keywords:** `to`, `from`, `flight`, `trip`, `travel`
- **Logic:** Checks for presence of travel-related terms
- **Action:** Warning if no travel keywords found in long inputs

#### Temporal Keywords Detection
**Time References:** `tomorrow`, `next`, `week`, `month`
**Months:** `january`, `february`, `march`, `april`, `may`, `june`, `july`, `august`, `september`, `october`, `november`, `december`

#### Passenger Information Detection
**Keywords:** `people`, `person`, `passenger`, `adult`, `child`
- **Action:** Suggests adding passenger details if missing

---

## Request Examples

### Valid Requests

#### Perfect Travel Query
```bash
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Round trip from Boston to Seattle departing Friday March 15th for 2 adults"}'
```

#### Basic Travel Query
```bash
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Flight from NYC to LAX tomorrow"}'
```

#### Exactly 5 Words (Minimum)
```bash
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle next Friday"}'
```

### Invalid Requests (Will Return 400 Error)

#### Too Short (Character Count)
```bash
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "ab"}'
```

#### Too Few Words (Less than 5)
```bash
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle now"}'
```

#### SQL Injection Attempt
```bash
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle; DROP TABLE users;"}'
```

#### XSS Attempt
```bash
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Seattle <script>alert('"'"'hack'"'"')</script> to Boston"}'
```

---

## Response Format

### Success Response (200 OK)
```json
{
  "from": "Boston BOS",
  "to": "Seattle SEA",
  "fromDate": "2025-07-04",
  "toDate": "2025-07-11",
  "passengers": 2,
  "trip": "Round-Trip",
  "searchContext": "Boston to Seattle for 2 people departing next Friday"
}
```

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| `from` | string | Departure location with airport code |
| `to` | string | Destination location with airport code |
| `fromDate` | string | Departure date (ISO format: YYYY-MM-DD) |
| `toDate` | string | Return date (ISO format: YYYY-MM-DD) |
| `passengers` | integer | Number of passengers |
| `trip` | string | Trip type (Round-Trip, One-Way) |
| `searchContext` | string | Original search input for context |

---

## Error Responses

### Validation Error (400 Bad Request)
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

### Error Categories
| Category | Type | Blocking | Description |
|----------|------|----------|-------------|
| VALIDATION_ERROR | Error | Yes | Basic validation failures (length, format, word count) |
| SECURITY_ERROR | Error | Yes | Security pattern violations (SQL, XSS) |
| CONTENT_WARNING | Warning | No | Content suggestions (profanity, non-travel, missing info) |

---

## Validation Examples

### ✅ Valid Inputs (Pass All Validation)

#### Complete Travel Queries
```json
// Perfect travel query with all details
{"searchInput": "Round trip from Boston to Seattle departing Friday March 15th for 2 adults"}

// Basic travel query with location and timing
{"searchInput": "Flight from New York to Paris next month"}

// Simple travel query (exactly 5 words)
{"searchInput": "Boston to Seattle next Friday"}

// Travel query with passenger information
{"searchInput": "Trip from Chicago to Miami for 3 people"}

// International travel query
{"searchInput": "Flight from London to Tokyo tomorrow"}
```

### ❌ Invalid Inputs (Blocking Errors)

#### Length and Word Count Issues
```json
// Too short (character count)
{"searchInput": "ab"}

// Too few words (less than 5 words)
{"searchInput": "Boston to Seattle now"}

// Empty input
{"searchInput": ""}

// Too long (500+ characters)
{"searchInput": "Very long string that exceeds the 500 character limit..."}
```

#### Security Issues
```json
// SQL Injection attempts
{"searchInput": "Boston'; DROP TABLE users; --"}
{"searchInput": "Seattle to Boston UNION SELECT * FROM passwords"}

// XSS attempts
{"searchInput": "Seattle <script>alert('hack')</script> to Boston"}
{"searchInput": "Flight to <iframe src='evil.com'></iframe> destination"}

// Excessive special characters
{"searchInput": "Boston <<<<<>>>>>> Seattle for tomorrow"}
```

### ⚠️ Inputs with Warnings (Non-blocking)

#### Content Warnings
```json
// Contains profanity (allows processing with warning)
{"searchInput": "This damn flight from Boston to Seattle"}

// Unusual characters (Cyrillic 'o')
{"searchInput": "Boston тo Seattle next week please"}

// Non-travel content
{"searchInput": "I need to buy groceries and do laundry today"}

// Missing travel details
{"searchInput": "I want to go from Boston to Seattle"}
```

---

## Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Successfully processed search input |
| 400 | Bad Request | Validation failed or invalid input parameters |
| 500 | Internal Server Error | Server error occurred |

---

## Complete Validation Rules Reference

### Length Validations
| Rule | Min | Max | Action | Message |
|------|-----|-----|--------|---------| 
| Character Count | 3 | 500 | Block | "Search input must be between 3 and 500 characters" |
| Word Count | 5 | - | Block | "Search input must contain at least 5 words for better travel search results" |
| Empty Check | 1 | - | Block | "Search input cannot be blank" |

### Security Validations
| Type | Pattern | Action | Message | Logging |
|------|---------|--------|---------|---------| 
| SQL Injection | SQL Keywords | Block | "Search input contains potentially harmful SQL keywords" | WARN |
| XSS Attack | Script Tags | Block | "Search input contains potentially harmful script content" | WARN |
| Special Chars | 5+ consecutive | Block | "Search input contains excessive special characters" | - |

### Content Validations
| Type | Condition | Action | Message |
|------|-----------|--------|---------| 
| Profanity | Contains swear words | Warning | "Search input contains inappropriate language" |
| Character Set | Non-alphanumeric | Warning | "Search input contains unusual characters for travel search" |
| Travel Context | No travel keywords | Warning | "Search input doesn't appear to be travel-related" |
| Completeness | Missing date/passengers | Warning | "Consider adding date or passenger information for better results" |

---

## Implementation Details

### Controller Method
- **Class:** `GtBffController`
- **Method:** `processSearch()`
- **Validation:** Multi-layered validation with `SearchInputValidator`
- **Exception Handling:** `ValidationException` for validation failures

### Validation Process Flow
1. **Jakarta Bean Validation**: `@Valid` annotation triggers basic validation
2. **Custom Validation**: `SearchInputValidator.validateSearchInput()`
3. **Security Checks**: SQL injection, XSS, special character validation
4. **Content Analysis**: Travel relevance, profanity, character set validation
5. **Response Generation**: Success response or structured error with details

### Logging
- **Validation Warnings**: Logged at INFO level when validation passes with warnings
- **Security Attempts**: Potential SQL injection and XSS attempts logged at WARN level
- **Request Processing**: All requests logged for debugging and monitoring

---

## Testing

### Automated Test Coverage
The endpoint is comprehensively tested in `SearchInputValidatorTest.java` with 18 test cases:

#### Valid Input Tests
- Valid travel queries with proper format
- Exactly 5-word minimum validation
- Complete travel information validation

#### Security Tests  
- SQL injection detection and blocking
- XSS attack detection and blocking
- Excessive special character validation

#### Length Tests
- Character count validation (3-500 characters)
- Word count validation (minimum 5 words)
- Empty input validation

#### Content Tests
- Travel relevance analysis
- Profanity detection (warning only)
- Character set validation (warning only)
- Missing travel details detection

#### Edge Cases
- Multiple validation issues in single input
- Boundary condition testing
- Complex malicious input combinations

### Manual Testing Commands
```bash
# Test valid 5-word input
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle next Friday"}'

# Test word count validation failure
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle"}'

# Test security validation
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Boston to Seattle; DROP TABLE users;"}'

# Test comprehensive travel query
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Round trip flight from New York to London departing March 15th returning March 22nd for 2 adults"}'
```

---

## Usage in Applications

### JavaScript/Fetch
```javascript
async function processSearch(searchInput) {
  try {
    const response = await fetch('http://localhost:8081/api/v1/gt/process-search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ searchInput })
    });
    
    if (!response.ok) {
      const error = await response.json();
      console.error('Validation failed:', error.details);
      return null;
    }
    
    return await response.json();
  } catch (error) {
    console.error('Request failed:', error);
    return null;
  }
}

// Usage
const result = await processSearch('Flight from Boston to Seattle for 2 people next Friday');
```

### Python/Requests
```python
import requests

def process_search(search_input):
    url = 'http://localhost:8081/api/v1/gt/process-search'
    payload = {'searchInput': search_input}
    
    try:
        response = requests.post(url, json=payload)
        
        if response.status_code == 400:
            error_data = response.json()
            print(f"Validation failed: {error_data['details']['errors']}")
            return None
            
        response.raise_for_status()
        return response.json()
        
    except requests.exceptions.RequestException as e:
        print(f"Request failed: {e}")
        return None

# Usage
result = process_search('Round trip from Chicago to Miami next month for 3 adults')
```

---

## Related Documentation

- **[Search Filters](./SEARCH_FILTERS.md)** - Basic search filter endpoint
- **[Validation System](./VALIDATION_SYSTEM.md)** - Detailed validation architecture
- **[Error Handling](./ERROR_HANDLING.md)** - Common error response formats