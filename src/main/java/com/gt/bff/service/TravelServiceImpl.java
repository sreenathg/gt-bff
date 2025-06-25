package com.gt.bff.service;

import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.constants.ResponseStatus;
import com.gt.bff.constants.TravelClass;
import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;
import com.gt.bff.util.GenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Implementation of travel service operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {
    
    private final GenAIService genAIService;
    private final ApplicationProperties applicationProperties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String travelExtractionPromptTemplate;
    
    @jakarta.annotation.PostConstruct
    private void loadPromptTemplates() {
        try {
            Resource resource = resourceLoader.getResource(applicationProperties.getGenai().getTravelExtractionPromptPath());
            travelExtractionPromptTemplate = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("Loaded travel extraction prompt template from: {}", applicationProperties.getGenai().getTravelExtractionPromptPath());
        } catch (IOException e) {
            log.error("Failed to load travel extraction prompt template", e);
            throw new IllegalStateException("Failed to load travel extraction prompt template", e);
        }
    }
    
    @Override
    public TravelResponse planTripWithFlights(TravelRequest request) {
        log.info("Planning trip with flights from {} to {} on {}", 
                request.getOrigin(), 
                request.getDestination(), 
                request.getDepartureDate());
        
        TravelResponse.FlightOption flightOption = createSampleFlightOption(request);
        TravelResponse.HotelOption hotelOption = createSampleHotelOption(request);
        TravelResponse.ActivityOption activityOption = createSampleActivityOption(request);
        
        return TravelResponse.builder()
                .requestId("REQ" + System.currentTimeMillis())
                .status(ResponseStatus.SUCCESS)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .departureDateTime(request.getDepartureDate().atStartOfDay())
                .returnDateTime(request.getReturnDate() != null ? 
                    request.getReturnDate().atTime(18, 0) : 
                    request.getDepartureDate().plusDays(3).atTime(18, 0))
                .numberOfTravelers(request.getNumberOfTravelers() != null ? 
                    request.getNumberOfTravelers() : 1)
                .travelClass(request.getTravelClass() != null ? request.getTravelClass() : TravelClass.ECONOMY)
                .flightOptions(List.of(flightOption))
                .hotelOptions(List.of(hotelOption))
                .activityOptions(List.of(activityOption))
                .totalEstimatedCost(new BigDecimal("699.97"))
                .currency("USD")
                .build();
    }
    
    @Override
    public Map<String, Object> getSearchFilters(String searchInput) {
        log.info("Generating search filters with input: {}", searchInput);
        
        // Fallback values
        Map<String, Object> filters = new HashMap<>();
        filters.put("from", "New York");
        filters.put("to", "Los Angeles");
        filters.put("fromDate", LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("toDate", LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("passengers", 1);
        filters.put("trip", "Round-Trip");
        
        if (searchInput == null) {
            log.warn("Null search input provided, using default values");
            return filters;
        }
        
        try {
            // Invoke GenAI to enhance search filter generation
            String prompt = travelExtractionPromptTemplate.replace("{searchInput}", searchInput);

            String aiResponse = genAIService.generateContent(prompt);
            log.debug("GenAI response for search filters: {}", aiResponse);
            
            if (aiResponse != null) {
                try {
                    // Clean up the response by removing Markdown code blocks if present
                    String cleanResponse = aiResponse.replaceAll("```(json)?\\s*", "").trim();
                    
                    // Parse JSON response from GenAI into Map
                    Map<String, Object> aiFilters = objectMapper.readValue(cleanResponse, 
                        new TypeReference<Map<String, Object>>() {});
                    
                    // Only update filters with non-null values from AI
                    aiFilters.forEach((key, value) -> {
                        if (value != null) {
                            filters.put(key, value);
                        }
                    });
                    
                    log.info("Successfully parsed GenAI response into filters");
                } catch (Exception e) {
                    log.error("Failed to parse GenAI response, using fallback values: {}", e.getMessage());
                    // Keep the default values set at the beginning of the method
                }
            }
        } catch (Exception e) {
            log.error("Error calling GenAI service, using fallback values: {}", e.getMessage());
            // Keep the default values set at the beginning of the method
        }
        
        // Add search context for potential manual processing if not already present
        if (!filters.containsKey("searchContext")) {
            filters.put("searchContext", searchInput);
        }
        
        return filters;
    }
    
    private TravelResponse.FlightOption createSampleFlightOption(TravelRequest request) {
        return TravelResponse.FlightOption.builder()
                .id("FL123")
                .airline("Sample Airlines")
                .flightNumber("SA123")
                .departureAirport(request.getOrigin() + " Airport")
                .arrivalAirport(request.getDestination() + " Airport")
                .departureTime(request.getDepartureDate().atTime(10, 0))
                .arrivalTime(request.getDepartureDate().atTime(12, 30))
                .price(new BigDecimal("299.99"))
                .currency("USD")
                .travelClass(request.getTravelClass() != null ? request.getTravelClass() : TravelClass.ECONOMY)
                .availableSeats(24)
                .build();
    }
    
    private TravelResponse.HotelOption createSampleHotelOption(TravelRequest request) {
        return TravelResponse.HotelOption.builder()
                .id("HTL456")
                .name("Grand " + request.getDestination() + " Hotel")
                .address("123 Main St, " + request.getDestination())
                .starRating(4)
                .pricePerNight(new BigDecimal("199.99"))
                .currency("USD")
                .amenities(List.of("Free WiFi", "Swimming Pool", "Restaurant"))
                .roomTypes(List.of(
                    TravelResponse.RoomType.builder()
                        .type("Standard")
                        .maxOccupancy(2)
                        .availableRooms(5)
                        .price(new BigDecimal("199.99"))
                        .build(),
                    TravelResponse.RoomType.builder()
                        .type("Deluxe")
                        .maxOccupancy(3)
                        .availableRooms(3)
                        .price(new BigDecimal("299.99"))
                        .build()
                ))
                .build();
    }
    
    private TravelResponse.ActivityOption createSampleActivityOption(TravelRequest request) {
        return TravelResponse.ActivityOption.builder()
                .id("ACT789")
                .name("City Tour of " + request.getDestination())
                .description("A guided tour of the city's main attractions")
                .location(request.getDestination())
                .startTime(request.getDepartureDate().plusDays(1).atTime(9, 0))
                .endTime(request.getDepartureDate().plusDays(1).atTime(13, 0))
                .price(new BigDecimal("49.99"))
                .currency("USD")
                .availableSpots(15)
                .build();
    }
}