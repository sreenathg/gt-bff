# GT BFF API Documentation

Welcome to the GT BFF (Backend for Frontend) API documentation. This documentation is organized into focused sections for easy navigation and comprehensive coverage.

## 🚀 Quick Start

**Base URL:** `http://localhost:8081`  
**Swagger UI:** http://localhost:8081/swagger-ui.html

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
```

---

## 📋 Documentation Index

### 🔍 Getting Started
- **[API_OVERVIEW.md](./API_OVERVIEW.md)** - Complete setup guide, configuration, and feature overview

### 📚 Endpoint Documentation
- **[SEARCH_FILTERS.md](./SEARCH_FILTERS.md)** - GET `/api/v1/gt/search-filters` endpoint
- **[PROCESS_SEARCH.md](./PROCESS_SEARCH.md)** - POST `/api/v1/gt/process-search` endpoint with comprehensive validation
- **[AIRPORTS.md](./AIRPORTS.md)** - GET `/api/v1/gt/airports` endpoint

### 🛠️ System Documentation  
- **[ERROR_HANDLING.md](./ERROR_HANDLING.md)** - Response formats, error codes, and data structures
- **[VALIDATION_SYSTEM.md](./VALIDATION_SYSTEM.md)** - Comprehensive input validation and security features

---

## 🎯 Navigation Guide

### For Developers Setting Up
1. Start with **[API_OVERVIEW.md](./API_OVERVIEW.md)** for complete setup and configuration
2. Review **[VALIDATION_SYSTEM.md](./VALIDATION_SYSTEM.md)** for security patterns
3. Check **[ERROR_HANDLING.md](./ERROR_HANDLING.md)** for response formats

### For API Users
1. **[SEARCH_FILTERS.md](./SEARCH_FILTERS.md)** - Basic search functionality
2. **[PROCESS_SEARCH.md](./PROCESS_SEARCH.md)** - Advanced search with validation (⭐ Key feature)
3. **[AIRPORTS.md](./AIRPORTS.md)** - Airport data access

### For Integration Teams
1. Use the Quick Test Commands above for initial testing
2. Review individual endpoint documentation for detailed examples
3. Check error handling patterns for robust integration

---

## 🔐 Key Features Highlight

- **5-Word Minimum Validation**: Travel search inputs require at least 5 words for optimal results
- **Security Protection**: Comprehensive SQL injection, XSS, and malicious content detection
- **Multi-Layer Validation**: Jakarta Bean Validation + custom business logic
- **Automated Airport Updates**: Daily downloads from GitHub with fallback URLs
- **Structured Error Responses**: Detailed validation feedback without information disclosure

---

## 📖 Documentation Architecture

This documentation was designed with a **split architecture** for better maintainability:

- **Endpoint-specific details** in individual files for focused information
- **Cross-cutting concerns** (errors, validation) in dedicated system documents  
- **Quick reference** and navigation maintained in overview and README files
- **Deep-dive information** available in specialized sections

Each document is self-contained but cross-references related sections for comprehensive coverage.

---

## 🏗️ Version

**Current Version:** 1.0.0

### Features Included
- Complete API implementation with comprehensive validation
- Airport data management with scheduled updates  
- Multi-layered security validation (SQL injection, XSS protection)
- Content-aware validation with 5-word minimum requirement
- Structured error handling with detailed feedback
- Split documentation architecture for better maintainability