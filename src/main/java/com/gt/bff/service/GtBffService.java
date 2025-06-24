package com.gt.bff.service;

import com.gt.bff.model.request.GtBffRequest;
import com.gt.bff.model.response.GtBffResponse;
import org.springframework.stereotype.Service;

@Service
public class GtBffService {
    public GtBffResponse processWeatherContext(GtBffRequest request) {
        String forecast = "Sunny and 25°C";
        if (request.getDestination() != null && "Tokyo".equalsIgnoreCase(request.getDestination())) {
            forecast = "Clear skies, 26–28°C";
        }

        return new GtBffResponse(
            request.getDestination(), 
            request.getTravelWindow(), 
            forecast
        );
    }
}
