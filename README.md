# GT BFF (Backend for Frontend) Service

A Spring Boot-based Backend for Frontend (BFF) service for the GT application that provides weather forecasting and travel planning functionality.

## Features

- RESTful API endpoints for weather forecasting
- Travel planning with flight options
- AI-powered search filters and query processing
- Input validation with Jakarta Bean Validation
- Global exception handling
- API documentation with Swagger UI/OpenAPI 3
- Structured logging with SLF4J
- Health check and monitoring endpoints with Actuator
- CORS configuration for cross-origin requests
- Custom configuration properties management

## Prerequisites

- Java 17+
- Maven 3.6.3+
- Spring Boot 3.5.3

## Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd gt-bff
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8081/api/v1/gt`
   - Swagger UI: `http://localhost:8081/swagger-ui.html`
   - API Docs: `http://localhost:8081/v3/api-docs`
   - Actuator Health: `http://localhost:8081/actuator/health`

## API Endpoints

### Weather Forecast
- **POST** `/api/v1/gt/forecast` - Get weather forecast for a destination
- **GET** `/api/v1/gt/forecast` - Get sample weather forecast

### Travel Planning
- **POST** `/api/v1/gt/flights` - Plan a trip with flight options

### Search & Filters
- **GET** `/api/v1/gt/search-filters` - Get available search filters
- **POST** `/api/v1/gt/process-search` - Process search input and extract filters

### Example: Get Weather Forecast
```bash
curl -X POST http://localhost:8081/api/v1/gt/forecast \
  -H "Content-Type: application/json" \
  -d '{
    "contextId": "123",
    "destination": "Tokyo",
    "travelWindow": "2024-01-01 to 2024-01-07"
  }'
```

### Example: Plan Trip with Flights
```bash
curl -X POST http://localhost:8081/api/v1/gt/flights \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "San Francisco",
    "destination": "Tokyo",
    "departureDate": "2024-06-01",
    "returnDate": "2024-06-07",
    "passengers": 2
  }'
```

## Project Structure

```
src/main/java/com/gt/bff/
├── config/              # Configuration classes (CORS, ApplicationProperties)
├── controller/          # REST controllers with OpenAPI documentation
├── exception/           # Global exception handling
├── model/dto/           # Data Transfer Objects for API requests/responses
├── service/             # Business logic layer with interfaces and implementations
├── util/                # Utility classes (ResponseHelper)
└── constants/           # Application constants

src/main/resources/
├── application.yml      # Main configuration file
├── prompts/             # AI prompt templates
└── static/              # Static resources
```

## Development

### Build and Run Tests
```bash
mvn clean test
```

### Run with Specific Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run Single Test
```bash
mvn test -Dtest=ClassNameTest
mvn test -Dtest=ClassNameTest#methodName
```

### Configuration
- Server runs on port 8081
- Custom application properties defined in `ApplicationProperties` class
- CORS enabled for localhost:3000 and localhost:8080
- Logging configured with file rotation (logs/application.log)

### Key Technologies
- **Spring Boot 3.5.3** with Web, Validation, and Actuator starters
- **Lombok** for boilerplate code reduction
- **MapStruct** for object mapping
- **SpringDoc OpenAPI** for API documentation
- **Jakarta Bean Validation** for input validation

### Code Style
This project uses Google Java Format. Please ensure your code is formatted before committing.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
