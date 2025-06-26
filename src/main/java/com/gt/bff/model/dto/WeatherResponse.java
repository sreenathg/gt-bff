package com.gt.bff.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for weather response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response containing weather forecast information")
public class WeatherResponse {

    @NotBlank(message = "Destination is required")
    @Schema(description = "Destination location", example = "Tokyo, Japan")
    private String destination;

    @NotBlank(message = "Travel window is required")
    @Schema(description = "Travel window in ISO format", example = "2023-06-01/2023-06-07")
    private String travelWindow;

    @NotBlank(message = "Forecast is required")
    @Schema(description = "Weather forecast description", example = "Sunny with a high of 28°C")
    private String forecast;

    @Schema(description = "Temperature in Celsius", example = "25.5")
    private Double temperature;

    @Schema(description = "Humidity percentage", example = "65.0")
    private Double humidity;

    @Schema(description = "Wind speed in km/h", example = "12.3")
    private Double windSpeed;

    @Schema(description = "Wind direction", example = "NW")
    private String windDirection;

    @Schema(description = "Additional weather information")
    private Map<String, Object> additionalInfo;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp of the forecast", example = "2023-05-25T14:30:00")
    private LocalDateTime timestamp;
}
