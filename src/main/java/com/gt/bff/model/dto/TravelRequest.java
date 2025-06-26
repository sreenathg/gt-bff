package com.gt.bff.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for travel planning.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelRequest {

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;

    private LocalDate returnDate;

    private Integer numberOfTravelers;

    private String travelClass;

    private String preferences;
}
