# GT BFF API Documentation

## Search API

### Process Search

Processes a natural language search query and returns structured search filters.

**Endpoint:** `POST /api/v1/gt/process-search`

**Request Body:**
```json
{
  "searchInput": "string"
}
```

**Validation Rules:**
- `searchInput` must be between 3 and 500 characters long
- `searchInput` must contain at least 5 words
- Input is checked for potential security threats (SQL injection, XSS, etc.)

**Responses:**

**200 OK** - Successful response
```json
{
  "from": "string",
  "to": "string",
  "fromDate": "yyyy-MM-dd",
  "toDate": "yyyy-MM-dd",
  "passengers": number,
  "trip": "string",
  "searchContext": "string"
}
```

**400 Bad Request** - Invalid input
```json
{
  "timestamp": "dd-MM-yyyy hh:mm:ss",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message describing validation failure",
  "path": "uri=/api/v1/gt/process-search"
}
```

**Example Requests:**

1. Valid request:
```bash
curl -X POST http://localhost:8081/api/v1/gt/process-search \
  -H "Content-Type: application/json" \
  -d '{"searchInput":"I want to book a flight from San Francisco to New York for 2 passengers next week"}'
```

2. Invalid request (too short):
```bash
curl -X POST http://localhost:8081/api/v1/gt/process-search \
  -H "Content-Type: application/json" \
  -d '{"searchInput":"hi"}'
```

## Search Filters API

### Get Search Filters

Returns default search filters, optionally enhanced by search input.

**Endpoint:** `GET /api/v1/gt/search-filters`

**Query Parameters:**
- `searchInput` (optional): Natural language search input

**Responses:**

**200 OK**
```json
{
  "from": "string",
  "to": "string",
  "fromDate": "yyyy-MM-dd",
  "toDate": "yyyy-MM-dd",
  "passengers": number,
  "trip": "string",
  "searchContext": "string"
}
```

**Example Request:**
```bash
curl "http://localhost:8081/api/v1/gt/search-filters?searchInput=flight%20to%20london"
```

## Error Handling

All endpoints follow RESTful error handling patterns with appropriate HTTP status codes and JSON error responses. Common status codes include:

- `400 Bad Request`: Invalid input parameters
- `404 Not Found`: Requested resource not found
- `500 Internal Server Error`: Server-side error

Error responses follow this format:
```json
{
  "timestamp": "dd-MM-yyyy hh:mm:ss",
  "status": number,
  "error": "string",
  "message": "string",
  "path": "string"
}
```
