package com.gt.bff.service;

import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.constants.ResponseStatus;
import com.gt.bff.constants.TravelClass;
import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;
import com.gt.bff.util.GenAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelServiceImplTest {

    @Mock
    private GenAIService genAIService;
    
    @Mock
    private ResourceLoader resourceLoader;
    
    @Mock
    private Resource resource;
    
    private ApplicationProperties applicationProperties;
    private TravelServiceImpl travelService;
    private TravelRequest travelRequest;

    @BeforeEach
    void setUp() throws IOException {
        // Setup concrete ApplicationProperties
        applicationProperties = new ApplicationProperties();
        ApplicationProperties.GenAI genaiProps = new ApplicationProperties.GenAI();
        genaiProps.setTravelExtractionPromptPath("classpath:prompts/travel-extraction-prompt.txt");
        // Use reflection to set the genai property
        try {
            java.lang.reflect.Field genaiField = ApplicationProperties.class.getDeclaredField("genai");
            genaiField.setAccessible(true);
            genaiField.set(applicationProperties, genaiProps);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup ApplicationProperties", e);
        }
        
        // Setup mock ResourceLoader and Resource
        String mockPromptTemplate = "Extract travel information from user input: {searchInput}";
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockPromptTemplate);
        
        // Initialize service - this will trigger PostConstruct
        travelService = new TravelServiceImpl(genAIService, applicationProperties, resourceLoader);
        
        try {
            // Manually call the PostConstruct method since it won't be called in unit tests
            java.lang.reflect.Method postConstruct = TravelServiceImpl.class.getDeclaredMethod("loadPromptTemplates");
            postConstruct.setAccessible(true);
            postConstruct.invoke(travelService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test", e);
        }
        travelRequest = TravelRequest.builder()
                .origin("New York")
                .destination("Los Angeles")
                .departureDate(LocalDate.of(2024, 6, 15))
                .returnDate(LocalDate.of(2024, 6, 22))
                .numberOfTravelers(2)
                .travelClass(TravelClass.BUSINESS)
                .preferences("Window seat")
                .build();
    }

    @Test
    void planTripWithFlights_ShouldReturnValidResponse() {
        // When
        TravelResponse response = travelService.planTripWithFlights(travelRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).startsWith("REQ");
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        assertThat(response.getOrigin()).isEqualTo("New York");
        assertThat(response.getDestination()).isEqualTo("Los Angeles");
        assertThat(response.getDepartureDateTime()).isEqualTo(LocalDate.of(2024, 6, 15).atStartOfDay());
        assertThat(response.getReturnDateTime()).isEqualTo(LocalDate.of(2024, 6, 22).atTime(18, 0));
        assertThat(response.getNumberOfTravelers()).isEqualTo(2);
        assertThat(response.getTravelClass()).isEqualTo(TravelClass.BUSINESS);
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getTotalEstimatedCost()).isEqualTo(new BigDecimal("699.97"));
        
        // Verify flight options
        assertThat(response.getFlightOptions()).hasSize(1);
        TravelResponse.FlightOption flightOption = response.getFlightOptions().get(0);
        assertThat(flightOption.getId()).isEqualTo("FL123");
        assertThat(flightOption.getAirline()).isEqualTo("Sample Airlines");
        assertThat(flightOption.getDepartureAirport()).isEqualTo("New York Airport");
        assertThat(flightOption.getArrivalAirport()).isEqualTo("Los Angeles Airport");
        assertThat(flightOption.getPrice()).isEqualTo(new BigDecimal("299.99"));
        
        // Verify hotel options
        assertThat(response.getHotelOptions()).hasSize(1);
        TravelResponse.HotelOption hotelOption = response.getHotelOptions().get(0);
        assertThat(hotelOption.getName()).isEqualTo("Grand Los Angeles Hotel");
        assertThat(hotelOption.getStarRating()).isEqualTo(4);
        assertThat(hotelOption.getRoomTypes()).hasSize(2);
        
        // Verify activity options
        assertThat(response.getActivityOptions()).hasSize(1);
        TravelResponse.ActivityOption activityOption = response.getActivityOptions().get(0);
        assertThat(activityOption.getName()).isEqualTo("City Tour of Los Angeles");
        assertThat(activityOption.getLocation()).isEqualTo("Los Angeles");
    }

    @Test
    void planTripWithFlights_WithMinimalRequest_ShouldUseDefaults() {
        // Given
        TravelRequest minimalRequest = TravelRequest.builder()
                .origin("Boston")
                .destination("Seattle")
                .departureDate(LocalDate.of(2024, 7, 1))
                .build();

        // When
        TravelResponse response = travelService.planTripWithFlights(minimalRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNumberOfTravelers()).isEqualTo(1);
    }
    
    @Test
    void getSearchFilters_WithValidInput_ShouldReturnFilters() throws Exception {
        // Given
        String searchInput = "Plan a 2 week vacation from New York to London in July 2025";
        String mockAiResponse = """
            {
                "fromDate": "2025-07-01",
                "passengers": 2,
                "trip": "Round-Trip",
                "toDate": "2025-07-15",
                "from": "New York",
                "to": "London"
            }
            """;
            
        // Mock GenAI service response
        Mockito.when(genAIService.generateContent(Mockito.anyString()))
               .thenReturn(mockAiResponse);
        
        // When
        Map<String, Object> result = travelService.getSearchFilters(searchInput);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("from")).isEqualTo("New York");
        assertThat(result.get("to")).isEqualTo("London");
        assertThat(result.get("trip")).isEqualTo("Round-Trip");
        assertThat(result.get("passengers")).isEqualTo(2);
        assertThat(result.get("fromDate")).isEqualTo("2025-07-01");
        assertThat(result.get("toDate")).isEqualTo("2025-07-15");
        
        // Verify GenAI service was called with the expected prompt
        Mockito.verify(genAIService).generateContent(Mockito.contains(searchInput));
    }
    
    @Test
    void getSearchFilters_WithGenAIError_ShouldReturnFallbackValues() {
        // Given
        String searchInput = "invalid search";
        
        // Mock GenAI service to throw an exception
        Mockito.when(genAIService.generateContent(Mockito.anyString()))
               .thenThrow(new RuntimeException("AI service error"));
        
        // When
        Map<String, Object> result = travelService.getSearchFilters(searchInput);
        
        // Then - verify fallback values are used
        assertThat(result)
            .isNotNull()
            .containsEntry("from", "New York")
            .containsEntry("to", "Los Angeles")
            .containsEntry("passengers", 1)
            .containsEntry("trip", "Round-Trip")
            .containsKey("fromDate")
            .containsKey("toDate")
            .containsKey("searchContext");
        
        // Verify the error was logged
        Mockito.verify(genAIService).generateContent(Mockito.anyString());
    }
    
    @Test
    void getSearchFilters_WithNullInput_ShouldHandleGracefully() {
        // When
        Map<String, Object> result = travelService.getSearchFilters(null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("from")).isEqualTo("New York");
        assertThat(result.get("to")).isEqualTo("Los Angeles");
        assertThat(result.get("fromDate")).isNotNull();
        assertThat(result.get("toDate")).isNotNull();
    }
}