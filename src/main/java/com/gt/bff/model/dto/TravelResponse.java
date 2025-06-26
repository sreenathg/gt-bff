package com.gt.bff.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for travel planning.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelResponse {

    private String requestId;
    private String status;
    private String origin;
    private String destination;
    private LocalDateTime departureDateTime;
    private LocalDateTime returnDateTime;
    private Integer numberOfTravelers;
    private String travelClass;
    private List<FlightOption> flightOptions;
    private List<HotelOption> hotelOptions;
    private List<ActivityOption> activityOptions;
    private BigDecimal totalEstimatedCost;
    private String currency;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightOption {
        private String id;
        private String airline;
        private String flightNumber;
        private String departureAirport;
        private String arrivalAirport;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private BigDecimal price;
        private String currency;
        @JsonProperty("class")
        private String travelClass;
        private Integer availableSeats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelOption {
        private String id;
        private String name;
        private String address;
        private Integer starRating;
        private BigDecimal pricePerNight;
        private String currency;
        private List<String> amenities;
        private List<RoomType> roomTypes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomType {
        private String type;
        private Integer maxOccupancy;
        private Integer availableRooms;
        private BigDecimal price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityOption {
        private String id;
        private String name;
        private String description;
        private String location;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal price;
        private String currency;
        private Integer availableSpots;
    }
}
