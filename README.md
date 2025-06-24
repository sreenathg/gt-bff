# GT BFF (Backend for Frontend) Service

A Spring Boot-based Backend for Frontend (BFF) service for the GT application.

## Features

- RESTful API endpoints for weather data
- Input validation
- Global exception handling
- API documentation with Swagger UI
- Structured logging
- Health check endpoint

## Prerequisites

- Java 17+
- Maven 3.6.3+
- Spring Boot 2.7.0

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
   - API Base URL: `http://localhost:8081/api`
   - Swagger UI: `http://localhost:8081/api/swagger-ui.html`
   - API Docs: `http://localhost:8081/api/v3/api-docs`
   - Actuator Health: `http://localhost:8081/api/actuator/health`

## API Endpoints

### Get Weather Forecast
- **URL**: `/api/v1/weather/forecast`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "contextId": "123",
    "destination": "Tokyo",
    "travelWindow": "2024-01-01 to 2024-01-07"
  }
  ```
- **Response**:
  ```json
  {
    "destination": "Tokyo",
    "travelWindow": "2024-01-01 to 2024-01-07",
    "forecast": "Clear skies, 26–28°C",
    "timestamp": "2024-01-01T12:00:00",
    "additionalInfo": {
      "humidity": 65,
      "windSpeed": 8.3,
      "windDirection": "NW",
      "recommendation": "Perfect weather for sightseeing!"
    }
  }
  ```

### Get Sample Weather
- **URL**: `/api/v1/weather/sample`
- **Method**: `GET`
- **Response**:
  ```json
  {
    "location": "San Francisco, CA",
    "temperature": 72.5,
    "unit": "Fahrenheit",
    "conditions": "Sunny",
    "humidity": 65,
    "windSpeed": 8.3,
    "windDirection": "NW",
    "timestamp": "2024-01-01T12:00:00"
  }
  ```

## Project Structure

```
src/main/java/com/gt/bff/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── exception/           # Custom exceptions and handlers
├── model/               # Domain models
├── repository/          # Data access layer
├── service/             # Business logic
└── util/                # Utility classes
```

## Development

### Build and Run Tests
```bash
mvn clean test
```

### Run with Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Code Style
This project uses Google Java Format. Please ensure your code is formatted before committing.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
