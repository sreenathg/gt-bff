# GT BFF (Backend for Frontend) Service

A Spring Boot-based Backend for Frontend (BFF) service for the GT application that provides weather forecasting and travel planning functionality.

## Prerequisites

- Java 17+
- Maven 3.6.3+
- Spring Boot 3.5.3

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/sreenathg/gt-bff.git
   cd gt-bff
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Set up environment variables (optional)**
   ```bash
   Set properties application.genai.google.api-key in application-local.properties to enable Google GenAI integration
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - API Base URL: `http://localhost:8081/api/v1/gt`
   - Swagger UI: `http://localhost:8081/swagger-ui.html`
   - API Docs: `http://localhost:8081/v3/api-docs`
   - Actuator Health: `http://localhost:8081/actuator/health`

## Documentation

📖 **Complete API Documentation**: [`docs/`](./docs/)

- **[API Overview](./docs/API_OVERVIEW.md)** - Complete setup guide and feature overviewATION_SYSTEM.md)** - Security and validation features

## Project Structure

```
src/main/java/com/gt/bff/
├── config/              # Configuration classes (CORS, ApplicationProperties)
├── controller/          # REST controllers with OpenAPI documentation
├── exception/           # Global exception handling
├── model/dto/           # Data Transfer Objects for API requests/responses
├── service/             # Business logic layer with interfaces and implementations
├── util/                # Utility classes (ResponseHelper, SearchInputValidator)
└── constants/           # Application constants

src/main/resources/
├── application.yml      # Main configuration file
├── prompts/             # AI prompt templates
├── airportcodes/        # Airport data files (auto-generated)
└── static/              # Static resources

docs/                    # Complete API documentation
├── README.md            # Documentation navigation
├── API_OVERVIEW.md      # Setup and configuration guide
├── SEARCH_FILTERS.md    # Search filters endpoint
├── PROCESS_SEARCH.md    # Advanced search with validation
├── AIRPORTS.md          # Airport data endpoint
├── ERROR_HANDLING.md    # Error formats and data models
└── VALIDATION_SYSTEM.md # Security and validation architecture
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

### Key Technologies
- **Spring Boot 3.5.3** with Web, Validation, and Actuator starters
- **Lombok** for boilerplate code reduction
- **MapStruct** for object mapping
- **SpringDoc OpenAPI** for API documentation
- **Jakarta Bean Validation** for input validation
- **Google GenAI** for AI-powered travel assistance and query processing
- **Micrometer** for metrics collection and Prometheus integration
- **JSON processing** for structured data handling


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
