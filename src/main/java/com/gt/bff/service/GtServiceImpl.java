package com.gt.bff.service;

import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;
import com.gt.bff.model.dto.WeatherRequest;
import com.gt.bff.model.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Facade implementation that coordinates weather and travel services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GtServiceImpl implements GtService {

    private final WeatherService weatherService;
    private final TravelService travelService;
    
    @Override
    public WeatherResponse getWeatherForecast(WeatherRequest request) {
        return weatherService.getWeatherForecast(request);
    }
    
    @Override
    public Map<String, Object> getSampleWeather(String location, LocalDate date) {
        return weatherService.getSampleWeather(location, date);
    }
    
    @Override
    public TravelResponse planTripWithFlights(TravelRequest request) {
        return travelService.planTripWithFlights(request);
    }
    
    @Override
    public Map<String, Object> getSearchFilters(String searchInput) {
        return travelService.getSearchFilters(searchInput);
    }
}
