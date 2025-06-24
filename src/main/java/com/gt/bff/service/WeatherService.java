package com.gt.bff.service;

import com.gt.bff.model.dto.WeatherRequest;
import com.gt.bff.model.dto.WeatherResponse;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service interface for weather-related operations.
 */
public interface WeatherService {
    
    /**
     * Gets weather forecast for a specific request.
     * 
     * @param request the weather request
     * @return weather response with forecast data
     */
    WeatherResponse getWeatherForecast(WeatherRequest request);
    
    /**
     * Gets sample weather data for demonstration.
     * 
     * @param location the location
     * @param date the date (optional)
     * @return sample weather data
     */
    Map<String, Object> getSampleWeather(String location, LocalDate date);
}