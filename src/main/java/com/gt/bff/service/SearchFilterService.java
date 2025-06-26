package com.gt.bff.service;

import com.gt.bff.model.schema.AIResponseSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for enhancing search filters using AI with schema validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchFilterService {

    private static final String SEARCH_INPUT_PLACEHOLDER = "{searchInput}";

    private final GenAIService genAIService;
    private final AIResponseValidator aiResponseValidator;

    /**
     * Enhances search filters using AI-generated content with schema validation.
     *
     * @param searchInput the user's search input
     * @param promptTemplate the prompt template to use
     * @param filters the filters map to enhance
     */
    public void enhanceFiltersWithAI(String searchInput, String promptTemplate, Map<String, Object> filters) {
        try {
            String prompt = promptTemplate.replace(SEARCH_INPUT_PLACEHOLDER, searchInput);
            String aiResponse = genAIService.generateContent(prompt);
            log.debug("GenAI response for search filters: {}", aiResponse);
            
            if (aiResponse != null) {
                validateAndMergeAIResponse(aiResponse, filters);
            }
        } catch (Exception e) {
            log.error("Error calling GenAI service, using fallback values: {}", e.getMessage());
        }
    }

    /**
     * Validates AI response against schema and merges valid values into filters.
     *
     * @param aiResponse the AI-generated response
     * @param filters the filters map to update
     */
    private void validateAndMergeAIResponse(String aiResponse, Map<String, Object> filters) {
        // Try structured validation first
        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> structuredResult = 
            aiResponseValidator.validateTravelSearchFilters(aiResponse);
            
        if (structuredResult.isValid()) {
            mergeStructuredFilters(structuredResult.getData(), filters);
            log.info("Successfully validated and merged structured GenAI response into filters");
            return;
        }
        
        log.debug("Structured validation failed: {}, trying generic map parsing", structuredResult.getErrorMessage());
        
        // Fallback to generic map parsing with validation
        AIResponseValidator.ValidationResult<Map<String, Object>> mapResult = 
            aiResponseValidator.validateAndParseToMap(aiResponse);
            
        if (mapResult.isValid()) {
            mergeMapFilters(mapResult.getData(), filters);
            log.info("Successfully validated and merged generic GenAI response into filters");
        } else {
            log.error("Failed to validate GenAI response: {}, using fallback values", mapResult.getErrorMessage());
        }
    }
    
    /**
     * Merges structured travel search filters into the filters map
     */
    private void mergeStructuredFilters(AIResponseSchema.TravelSearchFilters travelFilters, Map<String, Object> filters) {
        if (travelFilters.getFrom() != null) {
            filters.put("from", travelFilters.getFrom());
        }
        if (travelFilters.getTo() != null) {
            filters.put("to", travelFilters.getTo());
        }
        if (travelFilters.getFromDate() != null) {
            filters.put("fromDate", travelFilters.getFromDate().toString());
        }
        if (travelFilters.getToDate() != null) {
            filters.put("toDate", travelFilters.getToDate().toString());
        }
        if (travelFilters.getPassengers() != null) {
            filters.put("passengers", travelFilters.getPassengers());
        }
        if (travelFilters.getTrip() != null) {
            filters.put("trip", travelFilters.getTrip());
        }
        if (travelFilters.getSearchContext() != null) {
            filters.put("searchContext", travelFilters.getSearchContext());
        }
    }
    
    /**
     * Merges generic map filters with validation
     */
    private void mergeMapFilters(Map<String, Object> aiFilters, Map<String, Object> filters) {
        aiFilters.forEach((key, value) -> {
            if (value != null && isValidFilterKey(key) && isValidFilterValue(value)) {
                filters.put(key, value);
            }
        });
    }
    
    /**
     * Validates filter key to prevent injection
     */
    private boolean isValidFilterKey(String key) {
        return key != null && key.matches("^[a-zA-Z][a-zA-Z0-9_]*$") && key.length() <= 50;
    }
    
    /**
     * Validates filter value to ensure it's safe
     */
    private boolean isValidFilterValue(Object value) {
        if (value == null) {
            return false;
        }
        
        String valueStr = value.toString();
        return valueStr.length() <= 200 && !valueStr.matches(".*[<>\"'&;].*");
    }
}