package com.gt.bff.service;

import com.gt.bff.model.schema.AIResponseSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class SearchFilterServiceTest {

    @Mock
    private GenAIService genAIService;

    @Mock
    private AIResponseValidator aiResponseValidator;

    @InjectMocks
    private SearchFilterService searchFilterService;

    private Map<String, Object> filters;
    private String promptTemplate;

    @BeforeEach
    void setUp() {
        filters = new HashMap<>();
        filters.put("from", "Default City");
        filters.put("passengers", 1);

        promptTemplate = "Extract travel info: {searchInput}";
    }

    @Test
    void enhanceFiltersWithAI_WithValidResponse_ShouldEnhanceFilters() throws Exception {
        // Given
        String searchInput = "Boston to Seattle for 2 people";
        String aiResponse = "{\"from\":\"Boston\",\"to\":\"Seattle\",\"passengers\":2}";

        AIResponseSchema.TravelSearchFilters validFilters = new AIResponseSchema.TravelSearchFilters();
        validFilters.setFrom("Boston");
        validFilters.setTo("Seattle");
        validFilters.setPassengers(2);

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> structuredResult = 
            AIResponseValidator.ValidationResult.success(validFilters);

        when(genAIService.generateContent(anyString())).thenReturn(aiResponse);
        when(aiResponseValidator.validateTravelSearchFilters(aiResponse)).thenReturn(structuredResult);

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("from")).isEqualTo("Boston");
        assertThat(filters.get("to")).isEqualTo("Seattle");
        assertThat(filters.get("passengers")).isEqualTo(2);
    }

    @Test
    void enhanceFiltersWithAI_WithNullResponse_ShouldKeepOriginalFilters() {
        // Given
        String searchInput = "Boston to Seattle";
        when(genAIService.generateContent(anyString())).thenReturn(null);

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("from")).isEqualTo("Default City");
        assertThat(filters.get("passengers")).isEqualTo(1);
    }

    @Test
    void enhanceFiltersWithAI_WithException_ShouldKeepOriginalFilters() {
        // Given
        String searchInput = "Boston to Seattle";
        when(genAIService.generateContent(anyString())).thenThrow(new RuntimeException("AI service error"));

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("from")).isEqualTo("Default City");
        assertThat(filters.get("passengers")).isEqualTo(1);
    }

    @Test
    void enhanceFiltersWithAI_WithStructuredResponseWithDates_ShouldConvertDatesToString() {
        // Given
        String searchInput = "New York to London from Jan 15 to Jan 22";
        String aiResponse = "valid json response";

        AIResponseSchema.TravelSearchFilters validFilters = new AIResponseSchema.TravelSearchFilters();
        validFilters.setFrom("New York");
        validFilters.setTo("London");
        validFilters.setFromDate(LocalDate.of(2024, 1, 15));
        validFilters.setToDate(LocalDate.of(2024, 1, 22));
        validFilters.setTrip("Round-Trip");
        validFilters.setSearchContext("vacation trip");

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> structuredResult = 
            AIResponseValidator.ValidationResult.success(validFilters);

        when(genAIService.generateContent(anyString())).thenReturn(aiResponse);
        when(aiResponseValidator.validateTravelSearchFilters(aiResponse)).thenReturn(structuredResult);

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("from")).isEqualTo("New York");
        assertThat(filters.get("to")).isEqualTo("London");
        assertThat(filters.get("fromDate")).isEqualTo("2024-01-15");
        assertThat(filters.get("toDate")).isEqualTo("2024-01-22");
        assertThat(filters.get("trip")).isEqualTo("Round-Trip");
        assertThat(filters.get("searchContext")).isEqualTo("vacation trip");
    }

    @Test
    void enhanceFiltersWithAI_WithInvalidStructuredResponseValidMap_ShouldFallbackToMapParsing() {
        // Given
        String searchInput = "Paris to Rome";
        String aiResponse = "generic map response";

        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("from", "Paris");
        mapResponse.put("destination", "Rome");
        mapResponse.put("validKey", "validValue");

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> failedStructured = 
            AIResponseValidator.ValidationResult.failure("Structured validation failed");
        AIResponseValidator.ValidationResult<Map<String, Object>> successfulMap = 
            AIResponseValidator.ValidationResult.success(mapResponse);

        when(genAIService.generateContent(anyString())).thenReturn(aiResponse);
        when(aiResponseValidator.validateTravelSearchFilters(aiResponse)).thenReturn(failedStructured);
        when(aiResponseValidator.validateAndParseToMap(aiResponse)).thenReturn(successfulMap);

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("from")).isEqualTo("Paris");
        assertThat(filters.get("destination")).isEqualTo("Rome");
        assertThat(filters.get("validKey")).isEqualTo("validValue");
        
        verify(aiResponseValidator).validateTravelSearchFilters(aiResponse);
        verify(aiResponseValidator).validateAndParseToMap(aiResponse);
    }

    @Test
    void enhanceFiltersWithAI_WithBothValidationsFailed_ShouldKeepOriginalFilters() {
        // Given
        String searchInput = "Invalid query";
        String aiResponse = "invalid response";

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> failedStructured = 
            AIResponseValidator.ValidationResult.failure("Structured validation failed");
        AIResponseValidator.ValidationResult<Map<String, Object>> failedMap = 
            AIResponseValidator.ValidationResult.failure("Map validation failed");

        when(genAIService.generateContent(anyString())).thenReturn(aiResponse);
        when(aiResponseValidator.validateTravelSearchFilters(aiResponse)).thenReturn(failedStructured);
        when(aiResponseValidator.validateAndParseToMap(aiResponse)).thenReturn(failedMap);

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("from")).isEqualTo("Default City");
        assertThat(filters.get("passengers")).isEqualTo(1);
        assertThat(filters).hasSize(2); // No new filters added
    }

    @Test
    void enhanceFiltersWithAI_WithMapFiltering_ShouldFilterInvalidKeysAndValues() {
        // Given
        String searchInput = "test input";
        String aiResponse = "map response";

        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("validKey", "validValue");
        mapResponse.put("invalid-key", "value"); // Contains dash - invalid
        mapResponse.put("123invalid", "value"); // Starts with number - invalid
        mapResponse.put("", "value"); // Empty key - invalid
        mapResponse.put("key1", null); // Null value - invalid
        mapResponse.put("key2", "value<script>"); // Contains malicious character - invalid
        mapResponse.put("key3", "value\"with'quotes"); // Contains quotes - invalid
        mapResponse.put("key4", "x".repeat(201)); // Too long - invalid
        mapResponse.put("tooLongKeyNameThatExceedsFiftyCharactersLimitForSecurityReasons", "value"); // Too long key - invalid

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> failedStructured = 
            AIResponseValidator.ValidationResult.failure("Structured validation failed");
        AIResponseValidator.ValidationResult<Map<String, Object>> successfulMap = 
            AIResponseValidator.ValidationResult.success(mapResponse);

        when(genAIService.generateContent(anyString())).thenReturn(aiResponse);
        when(aiResponseValidator.validateTravelSearchFilters(aiResponse)).thenReturn(failedStructured);
        when(aiResponseValidator.validateAndParseToMap(aiResponse)).thenReturn(successfulMap);

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("validKey")).isEqualTo("validValue");
        assertThat(filters).doesNotContainKeys("invalid-key", "123invalid", "", "key1", "key2", "key3", "key4", "tooLongKeyNameThatExceedsFiftyCharactersLimitForSecurityReasons");
        // Should still have original filters plus valid new one
        assertThat(filters.get("from")).isEqualTo("Default City");
        assertThat(filters.get("passengers")).isEqualTo(1);
    }

    @Test
    void enhanceFiltersWithAI_WithPromptTemplateReplacement_ShouldReplaceSearchInputPlaceholder() {
        // Given
        String searchInput = "special search query";
        String customTemplate = "Extract from: {searchInput} and analyze {searchInput}";
        String expectedPrompt = "Extract from: special search query and analyze special search query";

        when(genAIService.generateContent(expectedPrompt)).thenReturn("{}");
        when(aiResponseValidator.validateTravelSearchFilters("{}"))
            .thenReturn(AIResponseValidator.ValidationResult.failure("Empty response"));
        when(aiResponseValidator.validateAndParseToMap("{}"))
            .thenReturn(AIResponseValidator.ValidationResult.failure("Empty response"));

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, customTemplate, filters);

        // Then
        verify(genAIService).generateContent(expectedPrompt);
    }

    @Test
    void enhanceFiltersWithAI_WithNullFieldsInStructuredResponse_ShouldOnlyMergeNonNullFields() {
        // Given
        String searchInput = "partial info query";
        String aiResponse = "partial response";

        AIResponseSchema.TravelSearchFilters validFilters = new AIResponseSchema.TravelSearchFilters();
        validFilters.setFrom("Boston");
        // Leave other fields as null

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> structuredResult = 
            AIResponseValidator.ValidationResult.success(validFilters);

        when(genAIService.generateContent(anyString())).thenReturn(aiResponse);
        when(aiResponseValidator.validateTravelSearchFilters(aiResponse)).thenReturn(structuredResult);

        // When
        searchFilterService.enhanceFiltersWithAI(searchInput, promptTemplate, filters);

        // Then
        assertThat(filters.get("from")).isEqualTo("Boston");
        assertThat(filters.get("passengers")).isEqualTo(1); // Original value preserved
        assertThat(filters).doesNotContainKeys("to", "fromDate", "toDate", "trip", "searchContext");
    }
}