package com.gt.bff.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FlightSearchRequest {
    @NotBlank(message = "From location is required")
    private String from;
    
    @NotBlank(message = "To location is required")
    private String to;
    
    @NotNull(message = "From date is required")
    @FutureOrPresent(message = "From date must be today or in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fromDate;
    
    @NotNull(message = "To date is required")
    @FutureOrPresent(message = "To date must be today or in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate toDate;
    
    @Min(value = 1, message = "Number of passengers must be at least 1")
    @Max(value = 10, message = "Number of passengers cannot exceed 10")
    private int passengers;
    
    @NotBlank(message = "Trip type is required")
    @Pattern(regexp = "ONE_WAY|ROUND_TRIP|MULTI_CITY", 
             message = "Trip type must be ONE_WAY, ROUND_TRIP, or MULTI_CITY")
    private String trip;
}
