# GT BFF API Documentation

## Authentication

Currently, no authentication is required for the APIs.

---

## 📋 Complete Documentation Index

This documentation is organized into focused sections for better navigation and maintenance:

### 📚 Endpoint Documentation
- **[Search Filters](./SEARCH_FILTERS.md)** - GET `/api/v1/gt/search-filters` endpoint
- **[Process Search](./PROCESS_SEARCH.md)** - POST `/api/v1/gt/process-search` endpoint with comprehensive validation
- **[Airports](./AIRPORTS.md)** - GET `/api/v1/gt/airports` endpoint

### 🛠️ System Documentation  
- **[Error Handling & Data Models](./ERROR_HANDLING.md)** - Response formats, error codes, and data structures
- **[Validation System](./VALIDATION_SYSTEM.md)** - Comprehensive input validation and security features

### 📝 Documentation Navigation Guide

#### For Developers
- Start with this **API Overview** for setup and configuration
- Review **[Validation System](./VALIDATION_SYSTEM.md)** for security patterns
- Check **[Error Handling](./ERROR_HANDLING.md)** for response formats

#### For API Users
- **[Search Filters](./SEARCH_FILTERS.md)** - Basic search functionality
- **[Process Search](./PROCESS_SEARCH.md)** - Advanced search with validation
- **[Airports](./AIRPORTS.md)** - Airport data access

#### For Integration
- Use the Quick Test Commands below for initial testing
- Review individual endpoint documentation for detailed examples
- Check error handling patterns for robust integration

---

## 🚀 Quick Reference

### Available Endpoints
| Endpoint | Method | Description | Documentation |
|----------|--------|-------------|---------------|
| `/api/v1/gt/search-filters` | GET | Get search filters with optional AI enhancement | [📖 Details](./SEARCH_FILTERS.md) |
| `/api/v1/gt/process-search` | POST | Process natural language search with validation | [📖 Details](./PROCESS_SEARCH.md) |
| `/api/v1/gt/airports` | GET | Get airports with IATA codes | [📖 Details](./AIRPORTS.md) |

### Quick Test Commands
```bash
# Get search filters
curl "http://localhost:8081/api/v1/gt/search-filters"

# Process search input (minimum 5 words required)
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Flight from New York to Paris next month for 2 adults"}'

# Get airports data
curl "http://localhost:8081/api/v1/gt/airports"

# Check health
curl "http://localhost:8081/actuator/health"
```

---

## 🔐 Key Features

### Comprehensive Validation System
- **5-Word Minimum**: Travel search inputs require at least 5 words for optimal results
- **Security Protection**: SQL injection, XSS, and malicious content detection
- **Content Analysis**: Travel context validation and content appropriateness checking
- **Multi-Layer Validation**: Jakarta Bean Validation + custom business logic

### Security Features
- **Input Sanitization**: Pattern-based detection of harmful content
- **Path Traversal Protection**: Secure file access using Spring ResourceLoader
- **Structured Error Responses**: Detailed validation feedback without information disclosure
- **Security Logging**: Monitoring and audit trail for potential attacks

### Data Management
- **Automated Airport Updates**: Daily downloads from GitHub with fallback URLs
- **Data Filtering**: Only airports with valid IATA codes included
- **Reliable Data Source**: Comprehensive global airport database

---

## Available Endpoints

| Endpoint | Method | Description | Documentation |
|----------|--------|-------------|---------------|
| `/api/v1/gt/search-filters` | GET | Get search filters with optional AI enhancement | [Search Filters](./SEARCH_FILTERS.md) |
| `/api/v1/gt/process-search` | POST | Process natural language search with validation | [Process Search](./PROCESS_SEARCH.md) |
| `/api/v1/gt/airports` | GET | Get airports with IATA codes | [Airports](./AIRPORTS.md) |
| `/actuator/health` | GET | Application health status | Built-in Spring Boot endpoint |

---

## Quick Start

### Test All Endpoints
```bash
# Get search filters
curl "http://localhost:8081/api/v1/gt/search-filters"

# Process search input
curl -X POST "http://localhost:8081/api/v1/gt/process-search" \
  -H "Content-Type: application/json" \
  -d '{"searchInput": "Flight from New York to Paris next month for 2 adults"}'

# Get airports data
curl "http://localhost:8081/api/v1/gt/airports"

# Check health
curl "http://localhost:8081/actuator/health"
```

---

## Common Documentation

- **[Error Handling & Data Models](./ERROR_HANDLING.md)** - Response formats, error codes, and data structures
- **[Validation System](./VALIDATION_SYSTEM.md)** - Comprehensive input validation and security features

---

## Configuration

### Application Profiles
- **Local:** `local` - Development profile with enhanced logging
- **Default:** Standard configuration for production

### Server Configuration
- **Port:** 8081
- **Session Timeout:** 30 minutes
- **Graceful Shutdown:** Enabled

### Logging
- **Application Logs:** `logs/application.log`
- **Log Level:** INFO (root), DEBUG (application)
- **Log Rotation:** 10MB max size, 7 days retention

---

## Development

### Running the Application
Use the provided restart script:
```bash
./restart-server.sh
```

This script will:
1. Clear console
2. Kill any process on port 8081
3. Clean up logs
4. Run `mvn clean install`
5. Start server with local profile

### Manual Commands
```bash
# Build
mvn clean install

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run tests
mvn test
```

---

## Background Services

### Airport Data Service
**Schedule:** Daily at 2:00 AM  
**Startup:** Async download on application start  
**Source:** https://raw.githubusercontent.com/mwgg/Airports/master/airports.json  
**Fallback:** https://raw.githubusercontent.com/mwgg/Airports/refs/heads/master/airports.json

**Process:**
1. Downloads complete airport database
2. Filters airports with valid IATA codes
3. Extracts only `iata`, `city`, and `country` fields
4. Saves to `src/main/resources/airportcodes/gt-airports.json`
5. Makes data available via REST endpoint

---

## Security Features

### Input Validation & Security
- **Comprehensive Input Validation**: Multi-layered validation with `SearchInputValidator`
- **SQL Injection Protection**: Pattern-based detection and blocking
- **XSS Prevention**: Script tag and JavaScript code detection
- **Content Filtering**: Profanity detection and inappropriate content warnings
- **Length Constraints**: 3-500 character limits enforced
- **Format Validation**: Alphanumeric characters and safe punctuation only

### File System Security
- **Spring ResourceLoader**: Secure file access without path traversal vulnerabilities
- **No Direct Filesystem Access**: Uses classpath resources for security
- **Safe Resource Loading**: Prevents directory traversal attacks

---

## Versioning

API versioning is handled through URL path (`/api/v1/gt/*`). Future versions can be added as `/api/v2/gt/*` etc.

---

## 🏗️ Version History

### Version 1.0.0
- Initial API implementation with comprehensive validation
- Airport data management with scheduled updates  
- Multi-layered security validation (SQL injection, XSS protection)
- Content-aware validation with 5-word minimum requirement
- Structured error handling with detailed feedback
- Split documentation architecture for better maintainability

### Key Features Introduced
- **Travel Search Validation**: Minimum 5-word requirement for optimal search results
- **Security Patterns**: Comprehensive SQL injection and XSS protection
- **Content Analysis**: Travel context validation and appropriateness checking
- **Multi-Layer Architecture**: Jakarta Bean Validation + custom business logic
- **Airport Data Service**: Automated daily updates with fallback URLs
- **Structured Documentation**: Organized endpoint-specific documentation