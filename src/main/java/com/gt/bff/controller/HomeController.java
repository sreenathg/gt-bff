package com.gt.bff.controller;

import com.gt.bff.constants.ApiPaths;
import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.model.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controller for handling root endpoints and application information.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Hidden // Hide this controller from Swagger documentation
public class HomeController {

    private final ApplicationProperties applicationProperties;

    @Value("${spring.application.name:gt-bff}")
    private String appName;

    @Value("${spring.profiles.active:local}")
    private String environment;

    /**
     * Redirects root and common API paths to Swagger UI.
     * @return Redirect to Swagger UI
     */
    @GetMapping({
        ApiPaths.ROOT,
        "/api",
        "/api/",
        "/api/v1",
        "/api/v1/",
        "/api/v1/gt",
        "/api/v1/gt/"
    })
    @Operation(
        summary = "API Documentation",
        description = "Redirects to the Swagger UI for API documentation"
    )
    @ApiResponse(
        responseCode = "302",
        description = "Redirect to Swagger UI"
    )
    public ResponseEntity<Void> redirectToSwagger() {
        log.debug("Redirecting to Swagger UI");
        return ResponseEntity
            .status(org.springframework.http.HttpStatus.FOUND)
            .header("Location", "/swagger-ui.html")
            .build();
    }

    /**
     * Health check endpoint for the application.
     * @return Health status of the application
     */
    @GetMapping(ApiPaths.HEALTH)
    @Operation(
        summary = "Health Check",
        description = "Returns the health status of the application"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Application is healthy",
        content = @Content(schema = @Schema(implementation = HealthResponse.class))
    )
    public ResponseEntity<HealthResponse> healthCheck() {
        log.debug("Health check endpoint called");
        HealthResponse response = HealthResponse.builder()
            .status("UP")
            .version(applicationProperties.getVersion())
            .timestamp(Instant.now())
            .environment(environment)
            .build();

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
            .body(response);
    }

    /**
     * Provides basic information about the application.
     * @return Application information
     */
    @GetMapping(ApiPaths.INFO)
    @Operation(
        summary = "Application Information",
        description = "Returns basic information about the application"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Application information",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    public ResponseEntity<Map<String, String>> appInfo() {
        log.debug("Application info endpoint called");
        return ResponseEntity.ok(Map.of(
            "name", appName,
            "version", applicationProperties.getVersion(),
            "environment", environment,
            "timestamp", Instant.now().toString(),
            "documentation", "/swagger-ui.html",
            "apiDocs", "/v3/api-docs",
            "apiTitle", applicationProperties.getApi().getTitle(),
            "apiVersion", applicationProperties.getApi().getVersion()
        ));
    }
}
