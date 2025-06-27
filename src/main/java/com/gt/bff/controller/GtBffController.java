package com.gt.bff.controller;

import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.service.SearchFilterService;
import com.gt.bff.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
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

    private final SearchFilterService searchFilterService;
    private final ApplicationProperties applicationProperties;
    private final ResourceLoader resourceLoader;
    
    private String travelExtractionPromptTemplate;

    @PostConstruct
    public void init() {
        log.info("Initializing GtBffController");
        loadTravelExtractionPromptTemplate();
    }
    
    private void loadTravelExtractionPromptTemplate() {
        try {
            String promptPath = applicationProperties.getGenai().getGoogle().getTravelExtractionPromptPath();
            Resource resource = resourceLoader.getResource(promptPath);
            travelExtractionPromptTemplate = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("Loaded travel extraction prompt template from: {}", promptPath);
        } catch (IOException e) {
            log.error("Failed to load travel extraction prompt template", e);
            throw new IllegalStateException("Failed to load travel extraction prompt template", e);
        }
    }

    @GetMapping("/search-filters")
    @Operation(summary = "Get search filters",
            description = "Returns available search filters for the application")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search filters")
    public ResponseEntity<Map<String, Object>> getSearchFilters(
            @RequestParam(required = false) String searchInput) {
        return ResponseHelper.executeServiceOperation(
            () -> generateSearchFilters(searchInput),
            "getSearchFilters",
            "searchInput: " + searchInput
        );
    }

    @PostMapping("/process-search")
    @Operation(summary = "Process search input",
            description = "Processes search input string and returns search filters")
    @ApiResponse(responseCode = "200", description = "Successfully processed search input")
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    public ResponseEntity<Map<String, Object>> processSearch(@RequestBody String searchInput) {
        return ResponseHelper.executeServiceOperation(
            () -> generateSearchFilters(searchInput),
            "processSearch",
            "searchInput: " + searchInput
        );
    }
    
    private Map<String, Object> generateSearchFilters(String searchInput) {
        log.info("Generating search filters with input: {}", searchInput);
        
        Map<String, Object> filters = createDefaultFilters();
        
        if (searchInput == null) {
            log.warn("Null search input provided, using default values");
            return filters;
        }
                    
        //enhanceFiltersWithAI(searchInput, filters);
        addSearchContext(searchInput, filters);
        
        return filters;
    }
    
    private Map<String, Object> createDefaultFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("from", DEFAULT_FROM_LOCATION);
        filters.put("to", DEFAULT_TO_LOCATION);
        filters.put("fromDate", LocalDate.now().plusDays(DEFAULT_DEPARTURE_DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("toDate", LocalDate.now().plusDays(DEFAULT_RETURN_DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("passengers", DEFAULT_PASSENGERS);
        filters.put("trip", DEFAULT_TRIP_TYPE);
        return filters;
    }
    
    private void enhanceFiltersWithAI(String searchInput, Map<String, Object> filters) {
        searchFilterService.enhanceFiltersWithAI(searchInput, travelExtractionPromptTemplate, filters);
    }
    
    private void addSearchContext(String searchInput, Map<String, Object> filters) {
        if (!filters.containsKey("searchContext")) {
            filters.put("searchContext", searchInput);
        }
    }
    
    @GetMapping("/airports")
    @Operation(summary = "Get airports with IATA codes",
            description = "Returns all airports that have valid IATA codes with city and country information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved airports")
    @ApiResponse(responseCode = "404", description = "Airports file not found")
    public ResponseEntity<String> getAirports() {
        try {
            Resource resource = resourceLoader.getResource("classpath:airportcodes/gt-airports.json");
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            String airportsJson = resource.getContentAsString(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(airportsJson);
        } catch (IOException e) {
            log.error("Failed to read airports file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
