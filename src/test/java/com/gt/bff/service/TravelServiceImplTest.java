package com.gt.bff.service;

import com.gt.bff.constants.ResponseStatus;
import com.gt.bff.constants.TravelClass;
import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TravelServiceImplTest {

    private TravelServiceImpl travelService;
    private TravelRequest travelRequest;

    @BeforeEach
    void setUp() {
        travelService = new TravelServiceImpl();
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
        assertThat(response.getTravelClass()).isEqualTo(TravelClass.ECONOMY);
        assertThat(response.getReturnDateTime()).isEqualTo(LocalDate.of(2024, 7, 4).atTime(18, 0));
    }

    @Test
    void getSearchFilters_ShouldReturnValidFilters() {
        // When
        Map<String, Object> filters = travelService.getSearchFilters();

        // Then
        assertThat(filters).isNotNull();
        assertThat(filters.get("from")).isEqualTo("New York");
        assertThat(filters.get("to")).isEqualTo("Los Angeles");
        assertThat(filters.get("passengers")).isEqualTo(2);
        assertThat(filters.get("trip")).isEqualTo("RoundTrip");
        assertThat(filters.get("fromDate")).isNotNull();
        assertThat(filters.get("toDate")).isNotNull();
    }
}