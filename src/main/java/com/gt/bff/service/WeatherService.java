package com.gt.bff.service;

import com.gt.bff.model.dto.WeatherRequest;
import com.gt.bff.model.dto.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class WeatherService {
    
    public WeatherResponse getWeatherForecast(WeatherRequest request) {
        try {
            log.info("Processing weather request for destination: {}", request.getDestination());
            
            String forecast = getForecastForDestination(request.getDestination());
            
            return WeatherResponse.builder()
                    .destination(request.getDestination())
                    .travelWindow(request.getTravelWindow())
                    .forecast(forecast)
                    .timestamp(LocalDateTime.now())
                    .additionalInfo(getAdditionalWeatherInfo(request.getDestination()))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing weather request: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private String getForecastForDestination(String destination) {
        // In a real application, this would call an external weather service
        if ("Tokyo".equalsIgnoreCase(destination)) {
            return "Clear skies, 26–28°C";
        } else if ("London".equalsIgnoreCase(destination)) {
            return "Partly cloudy, 18–22°C";
        } else if ("New York".equalsIgnoreCase(destination)) {
            return "Sunny, 24–28°C";
        }
        return "Sunny and 25°C";
    }
    
    private Map<String, Object> getAdditionalWeatherInfo(String destination) {
        // In a real application, this would fetch additional weather data
        return Map.of(
            "humidity", 65,
            "windSpeed", 8.3,
            "windDirection", "NW",
            "recommendation", getRecommendation(destination)
        );
    }
    
    private String getRecommendation(String destination) {
        if ("Tokyo".equalsIgnoreCase(destination)) {
            return "Perfect weather for sightseeing!";
        } else if ("London".equalsIgnoreCase(destination)) {
            return "Might want to bring an umbrella";
        }
        return "Enjoy your day!";
    }
}
