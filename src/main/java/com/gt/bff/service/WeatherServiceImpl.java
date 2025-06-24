package com.gt.bff.service;

import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.model.dto.WeatherRequest;
import com.gt.bff.model.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of weather service operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final ApplicationProperties properties;
    
    @Override
    public WeatherResponse getWeatherForecast(WeatherRequest request) {
        log.info("Getting weather forecast for destination: {}", request.getDestination());
        
        String forecast = properties.getForecastForDestination(request.getDestination());
        
        return WeatherResponse.builder()
                .destination(request.getDestination())
                .travelWindow(request.getTravelWindow())
                .forecast(forecast)
                .temperature(25.5)
                .humidity(65.0)
                .windSpeed(12.3)
                .windDirection("NW")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public Map<String, Object> getSampleWeather(String location, LocalDate date) {
        log.info("Generating sample weather data for location: {}, date: {}", location, date);
        
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("location", location);
        sampleData.put("temperature", 72.5);
        sampleData.put("unit", "Fahrenheit");
        sampleData.put("conditions", "Sunny");
        sampleData.put("humidity", 65);
        sampleData.put("windSpeed", 8.3);
        sampleData.put("windDirection", "NW");
        sampleData.put("date", (date != null) ? date.toString() : LocalDate.now().toString());
        sampleData.put("timestamp", LocalDateTime.now().toString());
        
        return sampleData;
    }
}