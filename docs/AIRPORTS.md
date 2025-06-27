# Airports Endpoint

## Overview

The Airports endpoint returns all airports that have valid IATA codes with city and country information. The data is automatically maintained through a scheduled background service that downloads and processes airport information from GitHub repositories.

---

## Endpoint Details

**URL:** `/api/v1/gt/airports`  
**Method:** `GET`  
**Content-Type:** `application/json`

---

## Request Format

### Basic Request
```bash
curl -X GET "http://localhost:8081/api/v1/gt/airports"
```

**No parameters required** - This endpoint returns all available airport data.

---

## Response Format

### Success Response (200 OK)
```json
[
  {
    "iata": "SFO",
    "city": "San Francisco",
    "country": "US"
  },
  {
    "iata": "LAX",
    "city": "Los Angeles", 
    "country": "US"
  },
  {
    "iata": "JFK",
    "city": "New York",
    "country": "US"
  },
  {
    "iata": "LHR",
    "city": "London",
    "country": "GB"
  },
  {
    "iata": "CDG",
    "city": "Paris",
    "country": "FR"
  },
  {
    "iata": "NRT",
    "city": "Tokyo",
    "country": "JP"
  }
]
```

### Response Fields
| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `iata` | string | Three-letter IATA airport code | "JFK" |
| `city` | string | City name where airport is located | "New York" |
| `country` | string | Country code (ISO format) | "US" |

---

## Data Characteristics

### IATA Code Requirements
- **Format:** Exactly 3 letters
- **Standard:** International Air Transport Association codes
- **Examples:** SFO, LAX, JFK, LHR, CDG, NRT
- **Validation:** Only airports with valid IATA codes are included

### Geographic Coverage
- **Global:** Airports from all countries and continents
- **Major Airports:** All major international airports included
- **Regional Airports:** Many regional airports with IATA codes
- **Country Codes:** ISO country code format (US, GB, FR, JP, etc.)

### Data Quality
- **Filtered Data:** Only airports with complete information (IATA, city, country)
- **No Duplicates:** Each IATA code appears only once
- **Validated Fields:** All required fields present and non-empty
- **Current Data:** Updated daily from authoritative source

---

## Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Successfully retrieved airports data |
| 404 | Not Found | Airports file not found or not yet downloaded |
| 500 | Internal Server Error | Error reading airports file |

---

## Error Responses

### File Not Found (404)
```json
{
  "timestamp": "2025-06-27T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Airports data not available",
  "path": "/api/v1/gt/airports"
}
```

**Common Causes:**
- Application started but daily download hasn't completed yet
- Airport data service failed to download or process data
- Resource file missing from classpath

### Internal Server Error (500)
```json
{
  "timestamp": "2025-06-27T10:30:00.000Z", 
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to read airports data",
  "path": "/api/v1/gt/airports"
}
```

**Common Causes:**
- File system permissions issue
- Corrupted airport data file
- Resource loading failure

---

## Data Source & Updates

### Primary Data Source
**URL:** https://raw.githubusercontent.com/mwgg/Airports/master/airports.json  
**Repository:** mwgg/Airports (GitHub)  
**Format:** JSON with comprehensive airport information

### Fallback Data Source  
**URL:** https://raw.githubusercontent.com/mwgg/Airports/refs/heads/master/airports.json  
**Purpose:** Ensures reliability if primary URL is unavailable

### Update Schedule
- **Automatic Updates:** Daily at 2:00 AM server time
- **Startup Update:** Async download when application starts
- **Cron Expression:** `0 0 2 * * *` (2 AM every day)
- **Background Service:** `AirportDataService`

### Data Processing Pipeline
1. **Download:** Fetch complete airport database from GitHub
2. **Filter:** Keep only airports with valid IATA codes
3. **Extract:** Select only `iata`, `city`, and `country` fields
4. **Validate:** Ensure all required fields are present
5. **Save:** Store to `src/main/resources/airportcodes/gt-airports.json`
6. **Serve:** Make available via REST endpoint

---

## Security Features

### Path Traversal Protection
- **Spring ResourceLoader:** Uses Spring's ResourceLoader for secure file access
- **Classpath Resources:** Files accessed via classpath, not direct filesystem paths
- **No User Input:** Endpoint accepts no user parameters that could affect file access
- **Fixed Resource Path:** Hard-coded resource path prevents directory traversal

### Implementation Security
```java
@GetMapping("/airports")
public ResponseEntity<String> getAirports() {
    // Secure resource loading - no path traversal vulnerability
    Resource resource = resourceLoader.getResource("classpath:airportcodes/gt-airports.json");
    
    if (!resource.exists()) {
        return ResponseEntity.notFound().build();
    }
    
    String airportsJson = resource.getContentAsString(StandardCharsets.UTF_8);
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(airportsJson);
}
```

---

## Usage Examples

### JavaScript/Fetch
```javascript
async function getAirports() {
  try {
    const response = await fetch('http://localhost:8081/api/v1/gt/airports');
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const airports = await response.json();
    return airports;
  } catch (error) {
    console.error('Failed to fetch airports:', error);
    return [];
  }
}

// Usage
const airports = await getAirports();
console.log(`Loaded ${airports.length} airports`);

// Find specific airport
const jfk = airports.find(airport => airport.iata === 'JFK');
console.log(jfk); // {iata: "JFK", city: "New York", country: "US"}
```

### Python/Requests
```python
import requests

def get_airports():
    """Fetch all airports with IATA codes."""
    try:
        response = requests.get('http://localhost:8081/api/v1/gt/airports')
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Failed to fetch airports: {e}")
        return []

# Usage
airports = get_airports()
print(f"Loaded {len(airports)} airports")

# Filter by country
us_airports = [airport for airport in airports if airport['country'] == 'US']
print(f"US airports: {len(us_airports)}")

# Create lookup dictionary
airport_lookup = {airport['iata']: airport for airport in airports}
lax_info = airport_lookup.get('LAX')
print(lax_info)  # {'iata': 'LAX', 'city': 'Los Angeles', 'country': 'US'}
```

### Java/Spring RestTemplate
```java
@Service
public class AirportService {
    private final RestTemplate restTemplate;
    
    public List<Airport> getAllAirports() {
        try {
            String url = "http://localhost:8081/api/v1/gt/airports";
            Airport[] airports = restTemplate.getForObject(url, Airport[].class);
            return Arrays.asList(airports != null ? airports : new Airport[0]);
        } catch (Exception e) {
            log.error("Failed to fetch airports", e);
            return Collections.emptyList();
        }
    }
    
    public Optional<Airport> findByIataCode(String iataCode) {
        return getAllAirports().stream()
                .filter(airport -> airport.getIata().equalsIgnoreCase(iataCode))
                .findFirst();
    }
}

// Airport DTO
@Data
public class Airport {
    private String iata;
    private String city;
    private String country;
}
```

### cURL Examples
```bash
# Basic request
curl -X GET "http://localhost:8081/api/v1/gt/airports"

# Pretty print JSON (with jq)
curl -s "http://localhost:8081/api/v1/gt/airports" | jq '.'

# Count total airports
curl -s "http://localhost:8081/api/v1/gt/airports" | jq 'length'

# Filter US airports
curl -s "http://localhost:8081/api/v1/gt/airports" | jq '.[] | select(.country == "US")'

# Find specific airport
curl -s "http://localhost:8081/api/v1/gt/airports" | jq '.[] | select(.iata == "JFK")'

# Get all unique countries
curl -s "http://localhost:8081/api/v1/gt/airports" | jq -r '.[].country' | sort -u
```

---

## Common Use Cases

### 1. Airport Code Validation
```javascript
async function validateAirportCode(iataCode) {
  const airports = await getAirports();
  return airports.some(airport => 
    airport.iata.toUpperCase() === iataCode.toUpperCase()
  );
}

// Usage
const isValid = await validateAirportCode('JFK'); // true
const isInvalid = await validateAirportCode('XYZ'); // false
```

### 2. Autocomplete/Search
```javascript
function searchAirports(query, airports) {
  const searchTerm = query.toLowerCase();
  return airports.filter(airport =>
    airport.iata.toLowerCase().includes(searchTerm) ||
    airport.city.toLowerCase().includes(searchTerm) ||
    airport.country.toLowerCase().includes(searchTerm)
  );
}

// Usage
const results = searchAirports('New', airports);
// Returns airports with "New" in IATA, city, or country
```

### 3. Country-Based Filtering
```javascript
function getAirportsByCountry(countryCode, airports) {
  return airports.filter(airport => 
    airport.country.toUpperCase() === countryCode.toUpperCase()
  );
}

// Usage
const usAirports = getAirportsByCountry('US', airports);
const ukAirports = getAirportsByCountry('GB', airports);
```

### 4. Travel Route Planning
```javascript
function findRouteAirports(fromCity, toCity, airports) {
  const from = airports.filter(airport =>
    airport.city.toLowerCase().includes(fromCity.toLowerCase())
  );
  const to = airports.filter(airport =>
    airport.city.toLowerCase().includes(toCity.toLowerCase())
  );
  
  return { departure: from, arrival: to };
}

// Usage
const route = findRouteAirports('New York', 'Los Angeles', airports);
```

---

## Performance Considerations

### Data Size
- **Typical Size:** 3,000-8,000 airports globally
- **Response Size:** ~200KB-500KB JSON
- **Transfer Time:** <1 second on typical connections
- **Memory Usage:** Minimal server-side memory impact

### Caching Recommendations
- **Client-Side Caching:** Cache response for 24 hours
- **HTTP Caching:** Consider adding Cache-Control headers
- **Local Storage:** Store in browser localStorage for web apps
- **Database Caching:** Import into application database for frequent use

### Optimization Tips
```javascript
// Client-side caching example
class AirportCache {
  static CACHE_KEY = 'airports_data';
  static CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours
  
  static async getAirports() {
    const cached = localStorage.getItem(this.CACHE_KEY);
    
    if (cached) {
      const { data, timestamp } = JSON.parse(cached);
      if (Date.now() - timestamp < this.CACHE_DURATION) {
        return data;
      }
    }
    
    // Fetch fresh data
    const airports = await fetchAirportsFromAPI();
    localStorage.setItem(this.CACHE_KEY, JSON.stringify({
      data: airports,
      timestamp: Date.now()
    }));
    
    return airports;
  }
}
```

---

## Monitoring & Troubleshooting

### Health Checks
```bash
# Check if airports endpoint is responding
curl -f "http://localhost:8081/api/v1/gt/airports" > /dev/null && echo "OK" || echo "FAIL"

# Check data freshness (if timestamp available in logs)
curl -s "http://localhost:8081/actuator/health" | jq '.status'
```

### Common Issues

#### 1. 404 Error on Startup
**Cause:** Airport data not yet downloaded  
**Solution:** Wait for background download to complete or restart application

#### 2. Empty Response
**Cause:** Data processing failed  
**Solution:** Check application logs for AirportDataService errors

#### 3. Slow Response
**Cause:** Large dataset or server load  
**Solution:** Implement client-side caching or pagination

### Logging
The airport data service logs important events:
```
INFO  - Airport data downloaded successfully, found 6843 airports
INFO  - Extracted 3247 airports with valid IATA codes
INFO  - Airport data saved to gt-airports.json
ERROR - Failed to download airport data: Connection timeout
```

---

## Testing

### Manual Testing
```bash
# Test basic functionality
curl "http://localhost:8081/api/v1/gt/airports"

# Test response format
curl -s "http://localhost:8081/api/v1/gt/airports" | jq 'type'  # should return "array"

# Test data structure
curl -s "http://localhost:8081/api/v1/gt/airports" | jq '.[0] | keys'  # should show ["city", "country", "iata"]

# Test for required fields
curl -s "http://localhost:8081/api/v1/gt/airports" | jq '.[] | select(.iata == null or .city == null or .country == null)'  # should return empty
```

### Integration Testing
The endpoint is tested in the main controller test suite:
- Response format validation
- Status code verification
- Error condition handling
- Data structure validation

---

## Related Documentation

- **[Process Search](./PROCESS_SEARCH.md)** - Uses airport data for validation
- **[Search Filters](./SEARCH_FILTERS.md)** - May reference airport codes
- **[Error Handling](./ERROR_HANDLING.md)** - Common error response formats