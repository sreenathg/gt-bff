package com.gt.bff.controller;

import com.gt.bff.model.request.GtBffRequest;
import com.gt.bff.model.response.GtBffResponse;
import com.gt.bff.service.GtBffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gt")
public class GtBffController {

    @Autowired
    private GtBffService gtBffService;

    @PostMapping
    public GtBffResponse getWeather(@RequestBody GtBffRequest request) {
        return gtBffService.processWeatherContext(request);
    }

    @GetMapping("/weather")
    public Map<String, Object> getSampleWeather() {
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("location", "San Francisco, CA");
        sampleData.put("temperature", 72.5);
        sampleData.put("unit", "Fahrenheit");
        sampleData.put("conditions", "Sunny");
        sampleData.put("humidity", 65);
        sampleData.put("windSpeed", 8.3);
        sampleData.put("windDirection", "NW");
        sampleData.put("timestamp", LocalDateTime.now().toString());
        return sampleData;
    }

    @GetMapping("/searchFilters")
    public Map<String, Object> getSearchFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("from", "New York");
        filters.put("to", "Los Angeles");
        filters.put("fromDate", LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("toDate", LocalDateTime.now().plusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE));
        filters.put("passengers", 2);
        filters.put("trip", "RoundTrip");
        return filters;
    }
}
