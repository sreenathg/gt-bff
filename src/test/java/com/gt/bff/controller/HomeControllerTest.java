package com.gt.bff.controller;

import com.gt.bff.config.ApplicationProperties;
import com.gt.bff.model.dto.HealthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private ApplicationProperties applicationProperties;
    
    @Mock
    private ApplicationProperties.Api apiProperties;
    
    private HomeController homeController;
    
    private MockMvc mockMvc;
    
    private static final String APP_NAME = "gt-bff";
    private static final String ENV = "test";
    private static final String VERSION = "1.0.0";
    private static final String API_TITLE = "Test API";
    private static final String API_VERSION = "v1";
    
    @BeforeEach
    void setUp() {
        // Create controller instance
        homeController = new HomeController(applicationProperties);
        
        // Set @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(homeController, "appName", APP_NAME);
        ReflectionTestUtils.setField(homeController, "environment", ENV);
        
        // Set up MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }
    
    @Test
    void redirectToSwagger_ShouldRedirectToSwaggerUI() throws Exception {
        // Test all the paths that should redirect to Swagger UI
        String[] pathsToTest = {
            "/",
            "/api",
            "/api/",
            "/api/v1",
            "/api/v1/",
            "/api/v1/gt",
            "/api/v1/gt/"
        };
        
        for (String path : pathsToTest) {
            mockMvc.perform(get(path))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/swagger-ui.html"));
        }
    }
    
    @Test
    void healthCheck_ShouldReturnHealthStatus() {
        // Setup mocks
        when(applicationProperties.getVersion()).thenReturn(VERSION);
        
        // When
        ResponseEntity<HealthResponse> response = homeController.healthCheck();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getCacheControl().contains("max-age=30"));
        
        HealthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.getStatus());
        assertEquals(VERSION, body.getVersion());
        assertEquals(ENV, body.getEnvironment());
        assertNotNull(body.getTimestamp());
    }
    
    @Test
    void appInfo_ShouldReturnAppInformation() {
        // Setup mocks
        when(applicationProperties.getVersion()).thenReturn(VERSION);
        when(applicationProperties.getApi()).thenReturn(apiProperties);
        when(apiProperties.getTitle()).thenReturn(API_TITLE);
        when(apiProperties.getVersion()).thenReturn(API_VERSION);
        
        // When
        ResponseEntity<Map<String, String>> response = homeController.appInfo();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals(APP_NAME, body.get("name"));
        assertEquals(VERSION, body.get("version"));
        assertEquals(ENV, body.get("environment"));
        assertEquals("/swagger-ui.html", body.get("documentation"));
        assertEquals("/v3/api-docs", body.get("apiDocs"));
        assertEquals(API_TITLE, body.get("apiTitle"));
        assertEquals(API_VERSION, body.get("apiVersion"));
        assertNotNull(body.get("timestamp"));
    }
    
    @Test
    void healthCheck_ShouldHaveCorrectMediaType() throws Exception {
        when(applicationProperties.getVersion()).thenReturn(VERSION);
        
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    void appInfo_ShouldHaveCorrectMediaType() throws Exception {
        when(applicationProperties.getVersion()).thenReturn(VERSION);
        when(applicationProperties.getApi()).thenReturn(apiProperties);
        when(apiProperties.getTitle()).thenReturn(API_TITLE);
        when(apiProperties.getVersion()).thenReturn(API_VERSION);
        
        mockMvc.perform(get("/info"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    void healthCheck_ShouldContainCurrentTimestamp() {
        when(applicationProperties.getVersion()).thenReturn(VERSION);
        
        // When
        ResponseEntity<HealthResponse> response = homeController.healthCheck();
        HealthResponse body = response.getBody();
        
        // Then
        assertNotNull(body);
        assertTrue(Instant.now().toEpochMilli() - body.getTimestamp().toEpochMilli() < 1000);
    }
    
    @Test
    void appInfo_ShouldContainCurrentTimestamp() {
        when(applicationProperties.getVersion()).thenReturn(VERSION);
        when(applicationProperties.getApi()).thenReturn(apiProperties);
        when(apiProperties.getTitle()).thenReturn(API_TITLE);
        when(apiProperties.getVersion()).thenReturn(API_VERSION);
        
        // When
        ResponseEntity<Map<String, String>> response = homeController.appInfo();
        String timestamp = response.getBody().get("timestamp");
        
        // Then
        assertNotNull(timestamp);
        Instant parsedTimestamp = Instant.parse(timestamp);
        assertTrue(Instant.now().toEpochMilli() - parsedTimestamp.toEpochMilli() < 1000);
    }
}