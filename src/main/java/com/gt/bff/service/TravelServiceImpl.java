package com.gt.bff.service;

import com.gt.bff.constants.ResponseStatus;
import com.gt.bff.constants.TravelClass;
import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of travel service operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {
    
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
    public Map<String, Object> getSearchFilters() {
        log.info("Generating search filters");
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("from", "New York");
        filters.put("to", "Los Angeles");
        filters.put("fromDate", LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("toDate", LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("passengers", 2);
        filters.put("trip", "RoundTrip");
        
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