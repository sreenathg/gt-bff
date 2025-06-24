package com.gt.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;
import com.gt.bff.model.dto.WeatherRequest;
import com.gt.bff.model.dto.WeatherResponse;
import com.gt.bff.service.GtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GtBffController.class)
class GtBffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GtService gtService;

    @Autowired
    private ObjectMapper objectMapper;

    private WeatherRequest weatherRequest;
    private WeatherResponse weatherResponse;
    private TravelRequest travelRequest;
    private TravelResponse travelResponse;

    @BeforeEach
    void setUp() {
        weatherRequest = WeatherRequest.builder()
                .contextId("test-123")
                .destination("Tokyo")
                .travelWindow("2024-01-01 to 2024-01-07")
                .build();

        weatherResponse = WeatherResponse.builder()
                .destination("Tokyo")
                .travelWindow("2024-01-01 to 2024-01-07")
                .forecast("Clear skies, 26–28°C")
                .temperature(25.5)
                .humidity(65.0)
                .windSpeed(12.3)
                .windDirection("NW")
                .timestamp(LocalDateTime.now())
                .build();

        travelRequest = TravelRequest.builder()
                .origin("New York")
                .destination("Los Angeles")
                .departureDate(LocalDate.of(2024, 6, 15))
                .returnDate(LocalDate.of(2024, 6, 22))
                .numberOfTravelers(2)
                .travelClass("BUSINESS")
                .build();

        travelResponse = TravelResponse.builder()
                .requestId("REQ123456789")
                .status("SUCCESS")
                .origin("New York")
                .destination("Los Angeles")
                .departureDateTime(LocalDate.of(2024, 6, 15).atStartOfDay())
                .returnDateTime(LocalDate.of(2024, 6, 22).atTime(18, 0))
                .numberOfTravelers(2)
                .travelClass("BUSINESS")
                .flightOptions(List.of())
                .hotelOptions(List.of())
                .activityOptions(List.of())
                .totalEstimatedCost(new BigDecimal("699.97"))
                .currency("USD")
                .build();
    }

    @Test
    void getWeatherForecast_ShouldReturnSuccess() throws Exception {
        // Given
        when(gtService.getWeatherForecast(any(WeatherRequest.class)))
                .thenReturn(weatherResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/gt/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weatherRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Tokyo"))
                .andExpect(jsonPath("$.forecast").value("Clear skies, 26–28°C"))
                .andExpect(jsonPath("$.temperature").value(25.5))
                .andExpect(jsonPath("$.humidity").value(65.0));
    }

    @Test
    void getWeatherForecast_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        WeatherRequest invalidRequest = WeatherRequest.builder()
                .contextId("")
                .destination("")
                .travelWindow("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/gt/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSampleWeather_ShouldReturnSuccess() throws Exception {
        // Given
        Map<String, Object> sampleWeather = new HashMap<>();
        sampleWeather.put("location", "San Francisco, CA");
        sampleWeather.put("temperature", 72.5);
        sampleWeather.put("conditions", "Sunny");

        when(gtService.getSampleWeather(anyString(), any()))
                .thenReturn(sampleWeather);

        // When & Then
        mockMvc.perform(get("/api/v1/gt/forecast")
                        .param("location", "San Francisco, CA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("San Francisco, CA"))
                .andExpect(jsonPath("$.temperature").value(72.5))
                .andExpect(jsonPath("$.conditions").value("Sunny"));
    }

    @Test
    void planTripWithFlights_ShouldReturnSuccess() throws Exception {
        // Given
        when(gtService.planTripWithFlights(any(TravelRequest.class)))
                .thenReturn(travelResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/gt/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("REQ123456789"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.origin").value("New York"))
                .andExpect(jsonPath("$.destination").value("Los Angeles"));
    }

    @Test
    void planTripWithFlights_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        TravelRequest invalidRequest = TravelRequest.builder()
                .origin("")
                .destination("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/gt/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSearchFilters_ShouldReturnSuccess() throws Exception {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("from", "New York");
        filters.put("to", "Los Angeles");
        filters.put("passengers", 2);

        when(gtService.getSearchFilters()).thenReturn(filters);

        // When & Then
        mockMvc.perform(get("/api/v1/gt/search-filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("New York"))
                .andExpect(jsonPath("$.to").value("Los Angeles"))
                .andExpect(jsonPath("$.passengers").value(2));
    }
}