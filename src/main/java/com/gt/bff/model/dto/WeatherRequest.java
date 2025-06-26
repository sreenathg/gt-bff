package com.gt.bff.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for weather operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherRequest {

    @NotBlank(message = "Context ID is required")
    private String contextId;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Travel window is required")
    private String travelWindow;
}
