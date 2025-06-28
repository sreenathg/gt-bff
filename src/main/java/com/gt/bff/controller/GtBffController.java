package com.gt.bff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.gt.bff.validation.SearchInputValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Validated
@RestController
@Tag(name = "Travel Controller", description = "APIs for travel information")
@RequestMapping(
    value = "/api/v1/gt",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class GtBffController {
    private static final String DEFAULT_FROM_LOCATION = "San Francisco SFO";
    private static final String DEFAULT_TO_LOCATION = "London LHR";
    private static final int DEFAULT_PASSENGERS = 1;
    private static final String DEFAULT_TRIP_TYPE = "Round-Trip";
    private static final int DEFAULT_DEPARTURE_DAYS = 7;
    private static final int DEFAULT_RETURN_DAYS = 14;

    private final SearchInputValidator searchInputValidator;
    private final ResourceLoader resourceLoader;

    public GtBffController(SearchInputValidator searchInputValidator, ResourceLoader resourceLoader) {
        this.searchInputValidator = searchInputValidator;
        this.resourceLoader = resourceLoader;
    }
    
    @GetMapping("/search-filters")
    @Operation(summary = "Get search filters",
            description = "Returns available search filters for the application")
    public ResponseEntity<Map<String, Object>> getSearchFilters(
            @RequestParam(required = false) String searchInput) {
        return ResponseEntity.ok(generateSearchFilters(searchInput));
    }

    @PostMapping("/process-search")
    @Operation(summary = "Process search input and return search filters")
    @ApiResponse(responseCode = "200", description = "Successfully processed search input")
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    public ResponseEntity<?> processSearch(@RequestBody Map<String, String> request) {
        String searchInput = request.get("searchInput");
        
        // Validate input
        SearchInputValidator.ValidationResult validationResult = searchInputValidator.validate(searchInput);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(String.join("; ", validationResult.getErrors()));
        }
        
        // Generate search filters
        Map<String, Object> response = generateSearchFilters(searchInput);
        
        // Add warnings to response if any
        if (!validationResult.getWarnings().isEmpty()) {
            response.put("warnings", validationResult.getWarnings());
        }
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> generateSearchFilters(String searchInput) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("from", DEFAULT_FROM_LOCATION);
        filters.put("to", DEFAULT_TO_LOCATION);
        filters.put("fromDate", LocalDate.now().plusDays(DEFAULT_DEPARTURE_DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("toDate", LocalDate.now().plusDays(DEFAULT_RETURN_DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("passengers", DEFAULT_PASSENGERS);
        filters.put("trip", DEFAULT_TRIP_TYPE);
        
        if (searchInput != null && !searchInput.trim().isEmpty()) {
            filters.put("searchContext", searchInput);
        }
        
        return filters;
    }
    
    @GetMapping("/airports")
    @Operation(summary = "Get airports list", description = "Returns the list of airports in JSON format")
    public ResponseEntity<String> getAirports() {
        try {
            Resource resource = resourceLoader.getResource("classpath:airportcodes/gt-airports.json");
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(content);
        } catch (IOException e) {
            log.error("Error reading airports.json", e);
            return ResponseEntity.internalServerError().body("Error reading airports data");
        }
    }
}
