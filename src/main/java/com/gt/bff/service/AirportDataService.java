package com.gt.bff.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class AirportDataService {

    private static final String PRIMARY_URL = "https://raw.githubusercontent.com/mwgg/Airports/master/airports.json";
    private static final String FALLBACK_URL = "https://raw.githubusercontent.com/mwgg/Airports/refs/heads/master/airports.json";
    
    @Value("${spring.application.name:gt-bff}")
    private String applicationName;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AirportDataService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    public void downloadAirportData() {
        log.info("Starting scheduled airport data download");
        
        try {
            createAirportCodesDirectory();
            String jsonData = downloadJsonData();
            saveJsonToFile(jsonData);
            extractIataAirports(jsonData);
            log.info("Airport data download completed successfully");
        } catch (Exception e) {
            log.error("Failed to download airport data: {}", e.getMessage(), e);
        }
    }
    
    private void createAirportCodesDirectory() throws IOException {
        Path airportCodesPath = Paths.get("src/main/resources/airportcodes");
        if (!Files.exists(airportCodesPath)) {
            Files.createDirectories(airportCodesPath);
            log.info("Created airportcodes directory: {}", airportCodesPath.toAbsolutePath());
        }
    }
    
    private String downloadJsonData() {
        try {
            log.info("Attempting to download from primary URL: {}", PRIMARY_URL);
            return restTemplate.getForObject(PRIMARY_URL, String.class);
        } catch (Exception e) {
            log.warn("Primary URL failed, trying fallback URL: {}", FALLBACK_URL);
            try {
                return restTemplate.getForObject(FALLBACK_URL, String.class);
            } catch (Exception fallbackException) {
                log.error("Both primary and fallback URLs failed");
                throw new RuntimeException("Failed to download airport data from both URLs", fallbackException);
            }
        }
    }
    
    private void saveJsonToFile(String jsonData) throws IOException {
        Path filePath = Paths.get("src/main/resources/airportcodes/airports.json");
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(jsonData);
            log.info("Airport data saved to: {}", filePath.toAbsolutePath());
        }
    }
    
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void downloadAirportDataOnStartup() {
        log.info("Application startup airport data download triggered");
        downloadAirportData();
    }
    
    private void extractIataAirports(String jsonData) throws IOException {
        log.info("Starting IATA airport extraction");
        
        JsonNode rootNode = objectMapper.readTree(jsonData);
        ArrayNode iataAirports = objectMapper.createArrayNode();
        
        if (rootNode.isObject()) {
            rootNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode airport = rootNode.get(fieldName);
                JsonNode iataNode = airport.get("iata");
                if (iataNode != null && !iataNode.isNull() && !iataNode.asText().isEmpty()) {
                    ObjectNode filteredAirport = objectMapper.createObjectNode();
                    filteredAirport.put("iata", iataNode.asText());
                    
                    JsonNode cityNode = airport.get("city");
                    if (cityNode != null && !cityNode.isNull()) {
                        filteredAirport.put("city", cityNode.asText());
                    }
                    
                    JsonNode countryNode = airport.get("country");
                    if (countryNode != null && !countryNode.isNull()) {
                        filteredAirport.put("country", countryNode.asText());
                    }
                    
                    iataAirports.add(filteredAirport);
                }
            });
        }
        
        Path gtAirportsPath = Paths.get("src/main/resources/airportcodes/gt-airports.json");
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(iataAirports);
        try (FileWriter writer = new FileWriter(gtAirportsPath.toFile())) {
            writer.write(jsonString);
            log.info("Extracted {} airports with IATA codes to: {}", iataAirports.size(), gtAirportsPath.toAbsolutePath());
        }
    }
    
    public void downloadAirportDataManually() {
        log.info("Manual airport data download triggered");
        downloadAirportData();
    }
}