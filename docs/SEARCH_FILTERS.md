# Search Filters Endpoint

## Overview

The Search Filters endpoint returns available search filters for travel planning with optional AI enhancement. This endpoint provides default travel search parameters that can be enhanced with natural language input.

---

## Endpoint Details

**URL:** `/api/v1/gt/search-filters`  
**Method:** `GET`  
**Content-Type:** `application/json`

---

## Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `searchInput` | string | No | Natural language search input for AI enhancement |

### Query Parameter Details

#### searchInput (Optional)
- **Type:** Query parameter
- **Format:** Natural language text
- **Purpose:** Provides context for AI-enhanced filter generation
- **Example:** `"Boston to Seattle for 2 people"`

---

## Request Examples

### Basic Request (No Parameters)
```bash
curl -X GET "http://localhost:8081/api/v1/gt/search-filters"
```

### Enhanced Request (With Search Input)
```bash
curl -X GET "http://localhost:8081/api/v1/gt/search-filters?searchInput=Boston%20to%20Seattle%20for%202%20people"
```

### URL Encoded Example
```bash
curl -X GET "http://localhost:8081/api/v1/gt/search-filters?searchInput=Flight%20from%20New%20York%20to%20London%20next%20Friday"
```

---

## Response Format

### Success Response (200 OK)

```json
{
  "from": "San Francisco SFO",
  "to": "London LHR",
  "fromDate": "2025-07-04",
  "toDate": "2025-07-11",
  "passengers": 1,
  "trip": "Round-Trip",
  "searchContext": "Boston to Seattle for 2 people"
}
```

### Response Fields

| Field | Type | Description | Default Value |
|-------|------|-------------|---------------|
| `from` | string | Departure location with airport code | "San Francisco SFO" |
| `to` | string | Destination location with airport code | "London LHR" |
| `fromDate` | string | Departure date (ISO format: YYYY-MM-DD) | 7 days from today |
| `toDate` | string | Return date (ISO format: YYYY-MM-DD) | 14 days from today |
| `passengers` | integer | Number of passengers | 1 |
| `trip` | string | Trip type | "Round-Trip" |
| `searchContext` | string | Original search input (if provided) | Only if searchInput provided |

---

## Default Values

The endpoint returns these default values when no search input is provided:

- **From Location:** San Francisco SFO
- **To Location:** London LHR
- **Departure Date:** Current date + 7 days
- **Return Date:** Current date + 14 days
- **Passengers:** 1
- **Trip Type:** Round-Trip

---

## Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Successfully retrieved search filters |
| 500 | Internal Server Error | Server error occurred |

---

## Error Responses

### Internal Server Error (500)
```json
{
  "timestamp": "2025-06-27T10:30:00.000Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred while processing the request",
  "path": "/api/v1/gt/search-filters"
}
```

---

## Usage Examples

### JavaScript/Fetch
```javascript
// Basic request
fetch('http://localhost:8081/api/v1/gt/search-filters')
  .then(response => response.json())
  .then(data => console.log(data));

// Enhanced request with search input
const searchInput = encodeURIComponent('Flight from Boston to Paris next month');
fetch(`http://localhost:8081/api/v1/gt/search-filters?searchInput=${searchInput}`)
  .then(response => response.json())
  .then(data => console.log(data));
```

### Python/Requests
```python
import requests

# Basic request
response = requests.get('http://localhost:8081/api/v1/gt/search-filters')
filters = response.json()

# Enhanced request
params = {'searchInput': 'Round trip from NYC to Tokyo for 2 people'}
response = requests.get('http://localhost:8081/api/v1/gt/search-filters', params=params)
enhanced_filters = response.json()
```

### Java/Spring RestTemplate
```java
RestTemplate restTemplate = new RestTemplate();

// Basic request
String url = "http://localhost:8081/api/v1/gt/search-filters";
Map<String, Object> filters = restTemplate.getForObject(url, Map.class);

// Enhanced request
String enhancedUrl = url + "?searchInput=" + URLEncoder.encode("Boston to Seattle next Friday", "UTF-8");
Map<String, Object> enhancedFilters = restTemplate.getForObject(enhancedUrl, Map.class);
```

---

## AI Enhancement (Future Feature)

Currently, the AI enhancement feature is commented out in the implementation. When enabled, the `searchInput` parameter will be processed through AI to extract:

- **Location Information:** Origin and destination cities/airports
- **Date Preferences:** Departure and return dates
- **Passenger Details:** Number of travelers
- **Trip Type:** Round-trip vs one-way preferences

### Expected AI-Enhanced Response
```json
{
  "from": "Boston BOS",
  "to": "Seattle SEA",
  "fromDate": "2025-07-11",
  "toDate": "2025-07-18",
  "passengers": 2,
  "trip": "Round-Trip",
  "searchContext": "Boston to Seattle for 2 people next Friday"
}
```

---

## Implementation Notes

### Controller Method
- **Class:** `GtBffController`
- **Method:** `getSearchFilters()`
- **Validation:** No input validation required (optional parameter)
- **Logging:** Request parameters logged for debugging

### Service Integration
- **Service:** `SearchFilterService`
- **AI Enhancement:** Currently disabled (commented out)
- **Default Generation:** Uses `createDefaultFilters()` method

### Date Calculation
- Departure date: `LocalDate.now().plusDays(7)`
- Return date: `LocalDate.now().plusDays(14)`
- Format: ISO Local Date (YYYY-MM-DD)

---

## Testing

### Manual Testing
```bash
# Test basic functionality
curl "http://localhost:8081/api/v1/gt/search-filters"

# Test with search input
curl "http://localhost:8081/api/v1/gt/search-filters?searchInput=Miami%20to%20Denver%20next%20month"

# Test with complex search input
curl "http://localhost:8081/api/v1/gt/search-filters?searchInput=Round%20trip%20from%20Chicago%20to%20San%20Francisco%20departing%20March%2015th%20for%203%20adults"
```

### Automated Testing
The endpoint is covered by integration tests in `GtBffControllerTest.java`:
- Basic functionality test
- Search input parameter test
- Response format validation
- Default value verification

---

## Related Endpoints

- **[Process Search](./PROCESS_SEARCH.md)** - Enhanced search processing with validation
- **[Error Handling](./ERROR_HANDLING.md)** - Common error response formats