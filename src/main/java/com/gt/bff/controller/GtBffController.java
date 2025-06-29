package com.gt.bff.controller;

import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.service.AirportDataService;
import com.gt.bff.service.SearchFilterService;
import com.gt.bff.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "GT BFF", description = "GT BFF API")
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
    private final AirportDataService airportDataService;
    private final boolean enableAIEnhancement;
    private String travelExtractionPromptTemplate;

    public GtBffController(SearchFilterService searchFilterService,
                         ApplicationProperties applicationProperties,
                         ResourceLoader resourceLoader,
                         AirportDataService airportDataService) {
        this.searchFilterService = searchFilterService;
        this.applicationProperties = applicationProperties;
        this.resourceLoader = resourceLoader;
        this.airportDataService = airportDataService;
        this.enableAIEnhancement = applicationProperties.getGenai().getGoogle().isEnableAIEnhancement();
        log.info("Initializing GtBffController with AI enhancement: {}", this.enableAIEnhancement);
    }
    
    @PostConstruct
    public void init() {
        log.info("Initializing GtBffController with AI enhancement: {}", enableAIEnhancement);
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
        log.info("Generating search filters with input: {}", searchInput);
        return ResponseHelper.executeServiceOperation(
            () -> generateSearchFilters(searchInput),
            "processSearch",
            "searchInput: " + searchInput
        );
    }
    
    private Map<String, Object> generateSearchFilters(String searchInput) {
        
        Map<String, Object> filters = createDefaultFilters();
        
        if (searchInput == null) {
            log.warn("Null search input provided, using default values");
            return filters;
        }
                    
        enhanceFiltersWithAI(searchInput, filters);
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
       if(enableAIEnhancement){ 
            log.info("Enhancing filters with AI for input: {}", searchInput);
            searchFilterService.enhanceFiltersWithAI(searchInput, travelExtractionPromptTemplate, filters);
       }else{
            log.info("AI enhancement is disabled");
       }
    }
    
    private void addSearchContext(String searchInput, Map<String, Object> filters) {
        log.info("Adding search context to filters");
        if (!filters.containsKey("searchContext")) {
            filters.put("searchContext", searchInput);
        }
    }

    @GetMapping("/airports")
    @Operation(summary = "Get airport data",
            description = "Returns a list of airports with their IATA codes and locations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved airport data")
    public ResponseEntity<String> getAirports() {
        try {
            String airportsJson = airportDataService.getAirportsJson();
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(airportsJson);
        } catch (FileNotFoundException e) {
            log.warn("Airports data file not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to read airports data", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}