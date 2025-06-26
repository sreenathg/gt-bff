package com.gt.bff.model.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Schema classes for validating AI responses
 */
public class AIResponseSchema {

    /**
     * Schema for travel search filter extraction
     */
    @Data
    @NoArgsConstructor
    public static class TravelSearchFilters {
        @Size(max = 100, message = "From location must be less than 100 characters")
        private String from;
        
        @Size(max = 100, message = "To location must be less than 100 characters")
        private String to;
        
        @JsonProperty("fromDate")
        private LocalDate fromDate;
        
        @JsonProperty("toDate")
        private LocalDate toDate;
        
        @jakarta.validation.constraints.Min(value = 1, message = "Passengers must be at least 1")
        @jakarta.validation.constraints.Max(value = 10, message = "Passengers cannot exceed 10")
        private Integer passengers;
        
        @Pattern(regexp = "^(One-Way|Round-Trip)$", message = "Trip type must be 'One-Way' or 'Round-Trip'")
        private String trip;
        
        @Size(max = 500, message = "Search context must be less than 500 characters")
        private String searchContext;
    }

    /**
     * Schema for location extraction response
     */
    @Data
    @NoArgsConstructor
    public static class LocationExtraction {
        @NotBlank(message = "Location name is required")
        @Size(max = 100, message = "Location name must be less than 100 characters")
        private String location;
        
        @Size(max = 100, message = "Country must be less than 100 characters")
        private String country;
        
        @Size(max = 100, message = "City must be less than 100 characters")
        private String city;
        
        @Pattern(regexp = "^-?\\d{1,3}\\.\\d{1,10}$", message = "Invalid latitude format")
        private String latitude;
        
        @Pattern(regexp = "^-?\\d{1,3}\\.\\d{1,10}$", message = "Invalid longitude format")
        private String longitude;
        
        @jakarta.validation.constraints.Min(value = 0, message = "Confidence must be between 0 and 1")
        @jakarta.validation.constraints.Max(value = 1, message = "Confidence must be between 0 and 1")
        private Double confidence;
    }

    /**
     * Schema for travel advice response
     */
    @Data
    @NoArgsConstructor
    public static class TravelAdvice {
        @NotBlank(message = "Response text is required")
        @Size(max = 2000, message = "Response text must be less than 2000 characters")
        private String response;
        
        @Size(max = 100, message = "Category must be less than 100 characters")
        private String category;
        
        private List<@Size(max = 200, message = "Recommendation must be less than 200 characters") String> recommendations;
        
        @jakarta.validation.constraints.Min(value = 0, message = "Confidence must be between 0 and 1")
        @jakarta.validation.constraints.Max(value = 1, message = "Confidence must be between 0 and 1")
        private Double confidence;
        
        @Size(max = 200, message = "Warning must be less than 200 characters")
        private String warning;
    }

    /**
     * Generic AI response wrapper
     */
    @Data
    @NoArgsConstructor
    public static class GenericResponse {
        @NotNull(message = "Response type is required")
        @Pattern(regexp = "^(travel_search|location_extraction|travel_advice|explanation)$", 
                message = "Invalid response type")
        private String type;
        
        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content must be less than 5000 characters")
        private String content;
        
        @jakarta.validation.constraints.Min(value = 0, message = "Confidence must be between 0 and 1")
        @jakarta.validation.constraints.Max(value = 1, message = "Confidence must be between 0 and 1")
        private Double confidence;
        
        @Size(max = 100, message = "Error message must be less than 100 characters")
        private String error;
        
        private Object data;
    }
}