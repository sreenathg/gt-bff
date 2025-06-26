# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GT BFF is a Spring Boot 3.5.3 Backend for Frontend (BFF) service built with Java 17 and Maven. It provides a unified API layer for weather forecasting, travel planning, and AI-powered travel assistance functionality.

## Build and Development Commands

### Building the Application
```bash
mvn clean install
```

### Running the Application
```bash
mvn spring-boot:run
```

### Running with Specific Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Running Tests
```bash
mvn clean test
```

### Running a Single Test
```bash
mvn test -Dtest=ClassNameTest
mvn test -Dtest=ClassNameTest#methodName
```

## Application Architecture

### Core Package Structure
- `com.gt.bff.config` - Configuration classes including ApplicationProperties, CORS setup, GenAI configuration validation, and metrics config
- `com.gt.bff.controller` - REST controllers with OpenAPI documentation (GtBffController, HomeController)
- `com.gt.bff.service` - Business logic layer with service interfaces and implementations including AI services (GenAIService, GoogleGenAIService, TravelService, SearchFilterService)
- `com.gt.bff.model.dto` - Data Transfer Objects for API requests/responses
- `com.gt.bff.model.schema` - Schema definitions for AI response validation
- `com.gt.bff.exception` - Global exception handling with custom error responses and GenAI-specific exceptions
- `com.gt.bff.constants` - Application constants (ApiPaths, TravelClass, ResponseStatus)
- `com.gt.bff.util` - Utility classes (ResponseHelper, TravelJsonExtractor, AIResponseValidator)

### Key Dependencies and Frameworks
- **Spring Boot 3.5.3** with Web, Validation, and Actuator starters
- **Lombok** for boilerplate code reduction (annotations: @Data, @Builder, @RequiredArgsConstructor, @Slf4j)
- **MapStruct** for object mapping between DTOs and entities
- **SpringDoc OpenAPI** for automatic API documentation generation
- **Jackson** for JSON serialization/deserialization
- **Jakarta Bean Validation** for request/response validation
- **Google GenAI** for AI-powered travel query processing and assistance
- **Micrometer** for metrics collection and monitoring
- **JSON processing** for structured data handling and parsing

### Configuration Management
- Primary configuration in `application.yml` with structured properties
- Custom properties defined in `ApplicationProperties` class using `@ConfigurationProperties`
- Weather forecasts configured per destination in YAML properties

### API Design Patterns
- RESTful endpoints following `/api/v1/gt` base path convention
- Comprehensive OpenAPI documentation with @Operation, @ApiResponse annotations
- Request/response validation using Jakarta Bean Validation
- Structured error responses via GlobalExceptionHandler

### Service Layer Architecture
- Service interfaces with concrete implementations (TravelService, GenAIService)
- AI-powered services (GoogleGenAIService, SearchFilterService) for intelligent query processing
- Mock data generation for development/testing purposes
- JSON extraction and validation services (TravelJsonExtractor, AIResponseValidator)
- Logging at both controller and service layers for request tracking

## Application Configuration

### Server Configuration
- Runs on port 8081
- Session timeout: 30 minutes
- Graceful shutdown enabled
- Custom error page configuration

### API Documentation
- Swagger UI available at: `http://localhost:8081/swagger-ui.html`
- OpenAPI docs at: `http://localhost:8081/v3/api-docs`

### Monitoring Endpoints
- Actuator endpoints at `/actuator`
- Health check: `http://localhost:8081/actuator/health`
- Metrics and Prometheus endpoints enabled

### Logging Configuration
- Application logs to `logs/application.log`
- Root level: INFO, application level: DEBUG
- Log rotation: 10MB max size, 7 days history

## Development Guidelines

### Code Style and Conventions
- Uses Google Java Format (mentioned in README)
- Lombok annotations for reducing boilerplate
- Builder pattern for complex DTOs
- Structured logging with SLF4J

### Testing Approach
- Spring Boot Test framework included
- Test structure should follow Maven conventions in `src/test/java`

### Error Handling
- Global exception handler provides consistent error responses
- Custom exceptions (ResourceNotFoundException, GenAIException, GenAITimeoutException, GenAIRateLimitException, GenAIConfigurationException)
- Validation errors automatically formatted into structured responses
- AI service specific error handling with timeout and rate limit management

### API Endpoint Patterns
All endpoints follow these patterns:
- Weather forecasting: `/api/v1/gt/forecast` (GET/POST)
- Travel planning: `/api/v1/gt/flights` (POST)
- Search filters: `/api/v1/gt/search-filters` (GET)
- Search processing: `/api/v1/gt/process-search` (POST)

## Key Implementation Details

### Weather Service
- Destination-based forecast lookup via ApplicationProperties
- Mock weather data generation for development
- Support for Tokyo, London, New York with fallback defaults

### Travel Planning Service
- Mock flight, hotel, and activity option generation
- Comprehensive response objects with nested builder patterns
- Cost estimation and availability simulation

### Configuration Properties
- Centralized configuration via `@ConfigurationProperties`
- Type-safe property binding with nested configuration classes
- Weather forecast configuration per destination in YAML
- AI/GenAI service configuration with API keys and model parameters
- CORS configuration for cross-origin requests
- Custom prompts stored in `src/main/resources/prompts/` directory

### AI Integration
- GenAI service configuration for travel query processing
- Custom prompt templates for travel extraction and advice
- Location extraction and travel planning assistance
- Configurable model parameters (temperature, top-p, top-k)