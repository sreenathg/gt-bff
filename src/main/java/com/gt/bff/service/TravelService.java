package com.gt.bff.service;

import com.gt.bff.model.dto.TravelRequest;
import com.gt.bff.model.dto.TravelResponse;

import java.util.Map;

/**
 * Service interface for travel-related operations.
 */
public interface TravelService {
    
    /**
     * Plans a trip with flight options.
     * 
     * @param request the travel request
     * @return travel response with flight, hotel, and activity options
     */
    TravelResponse planTripWithFlights(TravelRequest request);
    
    /**
     * Gets available search filters for travel planning.
     * 
     * @param searchInput optional search input to customize filters
     * @return search filters data
     */
    Map<String, Object> getSearchFilters(String searchInput);
}