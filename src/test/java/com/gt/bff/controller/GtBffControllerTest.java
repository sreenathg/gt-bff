package com.gt.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.service.SearchFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import com.gt.bff.service.AirportDataService;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class GtBffControllerTest {

    @Mock
    private SearchFilterService searchFilterService;
    
    @Mock
    private ApplicationProperties applicationProperties;
    
    @Mock
    private ApplicationProperties.GenAI genAI;
    
    @Mock
    private ApplicationProperties.GenAI.Google google;
    
    @Mock
    private AirportDataService airportDataService;
    
    @Mock
    private ResourceLoader resourceLoader;

    private GtBffController gtBffController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this).close();
        
        // Setup mock behavior for nested properties - only what's needed for controller construction
        when(applicationProperties.getGenai()).thenReturn(genAI);
        when(genAI.getGoogle()).thenReturn(google);
        when(google.isEnableAIEnhancement()).thenReturn(false);

        // Create controller instance manually with mocks
        gtBffController = new GtBffController(
            searchFilterService,
            applicationProperties,
            resourceLoader,
            airportDataService
        );

        // Configure ObjectMapper for Java 8 date/time types
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Initialize MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(gtBffController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getSearchFilters_WithNullInput_ShouldReturnFallbackValues() throws Exception {
        // Act & Assert - No need to mock prompt path as it's not used with null input
        mockMvc.perform(get("/api/v1/gt/search-filters"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.from").value("San Francisco SFO"))
               .andExpect(jsonPath("$.to").value("London LHR"));
    }

    @Test
    void getSearchFilters_WithSearchInput_ShouldHandleErrors() throws Exception {
        // Act & Assert
        // No need to mock anything since we're testing the error handling
        // and the controller should handle the case when enhanceFiltersWithAI is not called
        mockMvc.perform(get("/api/v1/gt/search-filters")
               .param("searchInput", "test input"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.from").exists())
               .andExpect(jsonPath("$.to").exists());
    }

    @Test
    void getAirports_WhenFileExists_ShouldReturnAirportsJson() throws Exception {
        // Arrange
        String jsonContent = "[{\"iata\":\"SFO\",\"name\":\"San Francisco\"}]";
        when(airportDataService.getAirportsJson()).thenReturn(jsonContent);

        // Act & Assert
        String response = mockMvc.perform(get("/api/v1/gt/airports"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andReturn().getResponse().getContentAsString();
               
        // Parse the response to handle any escaping
        ObjectMapper mapper = new ObjectMapper();
        String actualJson = mapper.readValue(response, String.class);
        
        // Verify the content matches our expected JSON
        assertEquals(jsonContent, actualJson);
    }

    @Test
    void getAirports_WhenFileNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(airportDataService.getAirportsJson())
            .thenThrow(new FileNotFoundException("Airports file not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/gt/airports"))
               .andExpect(status().isNotFound());
    }

    @Test
    void getAirports_WhenIOException_ShouldReturn500() throws Exception {
        // Arrange
        when(airportDataService.getAirportsJson())
            .thenThrow(new IOException("IO Error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/gt/airports"))
               .andExpect(status().isInternalServerError());
    }
}