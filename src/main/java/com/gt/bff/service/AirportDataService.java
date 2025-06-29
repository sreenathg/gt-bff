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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Path outputPath = Paths.get("src/main/resources/airportcodes/airports.json");
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(jsonData);
            log.info("Downloaded airports data to: {}", outputPath.toAbsolutePath());
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
        List<ObjectNode> airportsList = new ArrayList<>();
        Set<String> seenIataCodes = new HashSet<>();
        
        if (rootNode.isObject()) {
            // First, collect all airports in a list
            rootNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode airport = rootNode.get(fieldName);
                JsonNode iataNode = airport.get("iata");
                if (iataNode == null || iataNode.isNull()) {
                    return; // Skip if no IATA code
                }
                String iataCode = iataNode.asText();
                
                // Get all required fields with null checks
                JsonNode nameNode = airport.get("name");
                JsonNode cityNode = airport.get("city");
                JsonNode countryNode = airport.get("country");
                
                // Skip if any required field is missing or empty
                if (nameNode == null || nameNode.isNull() || nameNode.asText().trim().isEmpty() ||
                    cityNode == null || cityNode.isNull() || cityNode.asText().trim().isEmpty() ||
                    countryNode == null || countryNode.isNull() || countryNode.asText().trim().isEmpty() ||
                    !iataCode.matches("^[A-Za-z]{3}$")) {
                    return;
                }
                
                // Convert to string and trim
                String name = nameNode.asText().trim();
                String city = cityNode.asText().trim();
                String country = countryNode.asText().trim();
                
                // Only process if IATA code is not seen before
                if (!seenIataCodes.contains(iataCode.toUpperCase())) {
                    seenIataCodes.add(iataCode.toUpperCase()); // Add to seen codes in uppercase for case-insensitive comparison
                    
                    ObjectNode filteredAirport = objectMapper.createObjectNode();
                    filteredAirport.put("code", iataCode);
                    filteredAirport.put("name", name);
                    filteredAirport.put("city", city);
                    filteredAirport.put("country", country);
                    
                    // Add searchText if all required fields are present
                    if (iataNode != null && nameNode != null && cityNode != null && countryNode != null) {
                        String searchText = String.format("%s %s %s %s", 
                            iataNode.asText(),
                            nameNode.asText(),
                            cityNode.asText(),
                            countryNode.asText());
                        filteredAirport.put("searchText", searchText);
                    } else {
                        filteredAirport.put("searchText", "");
                    }
                    
                    airportsList.add(filteredAirport);
                }
            });
            
            // Sort the list by code
            airportsList.sort((a, b) -> 
                a.get("code").asText().compareToIgnoreCase(b.get("code").asText())
            );
            
            // Convert sorted list to ArrayNode
            ArrayNode iataAirports = objectMapper.createArrayNode();
            airportsList.forEach(iataAirports::add);
        
            Path gtAirportsPath = Paths.get("src/main/resources/airportcodes/gt-airports.json");
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(iataAirports);
            
            try (java.io.BufferedWriter writer = Files.newBufferedWriter(gtAirportsPath, StandardCharsets.UTF_8)) {
                writer.write(jsonString);
                log.info("Extracted {} airports with IATA codes to: {}", iataAirports.size(), gtAirportsPath.toAbsolutePath());
            } catch (IOException e) {
                log.error("Error writing to file: {}", e.getMessage(), e);
                throw e;
            }
        }
    }
    
    public void downloadAirportDataManually() {
        log.info("Manual airport data download triggered");
        downloadAirportData();
    }
    
    /**
     * Reads and returns the contents of the gt-airports.json file.
     * @return JSON string containing airport data
     * @throws IOException if an I/O error occurs reading the file
     * @throws FileNotFoundException if the file does not exist
     */
    public String getAirportsJson() throws IOException {
        Path gtAirportsPath = Paths.get("src/main/resources/airportcodes/gt-airports.json");
        if (!Files.exists(gtAirportsPath)) {
            log.warn("Airports data file not found at: {}", gtAirportsPath.toAbsolutePath());
            throw new FileNotFoundException("Airports data file not found");
        }
        
        try {
            return new String(Files.readAllBytes(gtAirportsPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading airports data file: {}", e.getMessage(), e);
            throw e;
        }
    }
}