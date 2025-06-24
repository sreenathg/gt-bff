package com.gt.bff.controller;

import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;
import com.gt.bff.model.dto.WeatherRequest;
import com.gt.bff.model.dto.WeatherResponse;
import com.gt.bff.service.GtService;
import com.gt.bff.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Weather Controller", description = "APIs for weather information")
@RequestMapping(
    value = "/api/v1/gt",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class GtBffController {
    private static final String BASE_PATH = "/api/v1/gt";

    private final GtService gtService;

    @PostConstruct
    public void init() {
        log.info("Initializing GtBffController with base path: {}", BASE_PATH);
    }

    @PostMapping("/forecast")
    @Operation(summary = "Get weather forecast",
            description = "Retrieves weather forecast for the specified location and date range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved weather forecast",
            content = @Content(schema = @Schema(implementation = WeatherResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<WeatherResponse> getWeatherForecast(
            @Valid @RequestBody WeatherRequest request) {
        return ResponseHelper.executeServiceOperation(
            () -> gtService.getWeatherForecast(request),
            "getWeatherForecast",
            "destination: " + request.getDestination()
        );
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get sample weather forecast",
            description = "Returns a sample weather forecast for demonstration purposes")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved sample weather forecast")
    public ResponseEntity<Map<String, Object>> getSampleWeather(
            @RequestParam(required = false, defaultValue = "San Francisco, CA") String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseHelper.executeServiceOperation(
            () -> gtService.getSampleWeather(location, date),
            "getSampleWeather",
            "location: " + location + ", date: " + date
        );
    }

    @PostMapping("/flights")
    @Operation(summary = "Plan a trip with flights",
            description = "Plan a trip with flight options")
    @ApiResponse(responseCode = "200", description = "Successfully planned travel")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<TravelResponse> planTripWithFlights(
            @Valid @RequestBody TravelRequest request) {
        return ResponseHelper.executeServiceOperation(
            () -> gtService.planTripWithFlights(request),
            "planTripWithFlights",
            "from: " + request.getOrigin() + " to: " + request.getDestination()
        );
    }
    
    @GetMapping("/search-filters")
    @Operation(summary = "Get search filters",
            description = "Returns available search filters for the application")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search filters")
    public ResponseEntity<Map<String, Object>> getSearchFilters() {
        return ResponseHelper.executeServiceOperation(
            () -> gtService.getSearchFilters(),
            "getSearchFilters"
        );
    }
}
