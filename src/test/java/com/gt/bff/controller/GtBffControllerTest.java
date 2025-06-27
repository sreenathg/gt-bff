package com.gt.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.service.SearchFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GtBffControllerTest {

    @Mock
    private SearchFilterService searchFilterService;
    
    @Mock
    private ApplicationProperties applicationProperties;
    
    @Mock
    private ResourceLoader resourceLoader;
    
    @Mock
    private Resource resource;

    @InjectMocks
    private GtBffController gtBffController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
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
        // When & Then - No need to mock GenAI since null input returns early
        mockMvc.perform(get("/api/v1/gt/search-filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("San Francisco SFO"))
                .andExpect(jsonPath("$.to").value("London LHR"))
                .andExpect(jsonPath("$.passengers").value(1))
                .andExpect(jsonPath("$.trip").value("Round-Trip"));
    }

    @Test
    void getSearchFilters_WithSearchInput_ShouldHandleErrors() throws Exception {
        // Given - Mock a scenario where service fails (which will happen due to null template)
        // This test verifies that the controller handles errors gracefully and returns fallback values

        // When & Then
        mockMvc.perform(get("/api/v1/gt/search-filters")
                        .param("searchInput", "Boston to Seattle for 2 people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("San Francisco SFO"))
                .andExpect(jsonPath("$.to").value("London LHR"))
                .andExpect(jsonPath("$.passengers").value(1))
                .andExpect(jsonPath("$.trip").value("Round-Trip"))
                .andExpect(jsonPath("$.searchContext").value("Boston to Seattle for 2 people"));
    }

    @Test
    void getAirports_WhenFileExists_ShouldReturnAirportsJson() throws Exception {
        // Given
        String expectedJson = "[{\"iata\":\"SFO\",\"city\":\"San Francisco\",\"country\":\"US\"},{\"iata\":\"LAX\",\"city\":\"Los Angeles\",\"country\":\"US\"}]";
        when(resourceLoader.getResource("classpath:airportcodes/gt-airports.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getContentAsString(any())).thenReturn(expectedJson);

        // When & Then
        mockMvc.perform(get("/api/v1/gt/airports"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("SFO")))
                .andExpect(content().string(containsString("San Francisco")));
    }

    @Test
    void getAirports_WhenFileNotExists_ShouldReturn404() throws Exception {
        // Given
        when(resourceLoader.getResource("classpath:airportcodes/gt-airports.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/gt/airports"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAirports_WhenIOException_ShouldReturn500() throws Exception {
        // Given
        when(resourceLoader.getResource("classpath:airportcodes/gt-airports.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getContentAsString(any())).thenThrow(new java.io.IOException("File read error"));

        // When & Then
        mockMvc.perform(get("/api/v1/gt/airports"))
                .andExpect(status().isInternalServerError());
    }
}