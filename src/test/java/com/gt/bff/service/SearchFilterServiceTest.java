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
import static org.mockito.Mockito.when;

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
}