package com.gt.bff.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WeatherRequest {
    @NotBlank(message = "Context ID is required")
    private String contextId;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotBlank(message = "Travel window is required")
    private String travelWindow;
}
