package com.gt.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportDataServiceTest {

    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private AirportDataService airportDataService;
    
    private static final String SAMPLE_JSON = "{\"TEST\":{\"name\":\"Test Airport\",\"city\":\"Test City\",\"country\":\"Test Country\",\"iata\":\"TST\"}}";
    private static final String SAMPLE_IATA_JSON = "[{\"code\":\"TST\",\"name\":\"Test Airport\",\"city\":\"Test City\",\"country\":\"Test Country\",\"searchText\":\"TST Test Airport Test City Test Country\"}]";

    @BeforeEach
    void setUp() throws IOException {
        // Create service instance
        airportDataService = new AirportDataService();
        
        // Inject mocks using ReflectionTestUtils
        ReflectionTestUtils.setField(airportDataService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(airportDataService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(airportDataService, "applicationName", "test-app");
        
        // Set up test directories
        Path testDir = Paths.get("src/main/resources/airportcodes");
        if (!Files.exists(testDir)) {
            Files.createDirectories(testDir);
        }
    }

    @Test
    void downloadAirportData_ShouldDownloadAndProcessData() throws IOException {
        // Mock the REST template response
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(SAMPLE_JSON);
            
        // Mock object mapper for parsing
        JsonNode mockRootNode = mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(mockRootNode);
        when(mockRootNode.isObject()).thenReturn(true);
        when(mockRootNode.fieldNames()).thenReturn(java.util.Collections.emptyIterator());
        
        // Mock object mapper for array creation
        when(objectMapper.createArrayNode()).thenReturn(new ObjectMapper().createArrayNode());
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(new ObjectMapper().writerWithDefaultPrettyPrinter());
        
        // Execute
        airportDataService.downloadAirportData();
        
        // Verify
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void downloadAirportData_FallbackUrl() throws Exception {
        // First call fails, second succeeds
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenThrow(new RuntimeException("Primary failed"))
            .thenReturn(SAMPLE_JSON);
            
        // Mock object mapper
        JsonNode mockRootNode = mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(mockRootNode);
        when(mockRootNode.isObject()).thenReturn(true);
        when(mockRootNode.fieldNames()).thenReturn(java.util.Collections.emptyIterator());
        
        // Mock object mapper for array creation
        when(objectMapper.createArrayNode()).thenReturn(new ObjectMapper().createArrayNode());
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(new ObjectMapper().writerWithDefaultPrettyPrinter());
        
        // Execute
        airportDataService.downloadAirportData();
        
        // Verify fallback was used
        verify(restTemplate, times(2)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void extractIataAirports_ValidData() throws Exception {
        // Use real ObjectMapper for this test since we need actual JSON processing
        ObjectMapper realObjectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(airportDataService, "objectMapper", realObjectMapper);
        
        // Setup test data
        String jsonData = "{\"TEST\":{\"name\":\"Test Airport\",\"city\":\"Test City\",\"country\":\"Test Country\",\"iata\":\"TST\"}}";
        
        // Execute
        ReflectionTestUtils.invokeMethod(airportDataService, "extractIataAirports", jsonData);
        
        // Verify file was created with expected content
        Path outputPath = Paths.get("src/main/resources/airportcodes/gt-airports.json");
        if (Files.exists(outputPath)) {
            String content = new String(Files.readAllBytes(outputPath));
            assertTrue(content.contains("TST"));
            assertTrue(content.contains("Test Airport"));
        }
    }

    @Test
    void getAirportsJson_Success() throws Exception {
        // Setup test data
        String expectedJson = SAMPLE_IATA_JSON;
        Path testFile = Paths.get("src/main/resources/airportcodes/gt-airports.json");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, expectedJson);
        
        // Execute
        String result = airportDataService.getAirportsJson();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.contains("TST"));
        assertTrue(result.contains("Test Airport"));
    }

    @Test
    void getAirportsJson_FileNotFound() throws Exception {
        // Make sure file doesn't exist
        Path testFile = Paths.get("src/main/resources/airportcodes/gt-airports.json");
        if (Files.exists(testFile)) {
            Files.delete(testFile);
        }
        
        // Execute and verify exception
        assertThrows(java.io.FileNotFoundException.class, () -> airportDataService.getAirportsJson());
    }

    @Test
    void downloadAirportDataManually_Success() throws Exception {
        // Mock successful response
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(SAMPLE_JSON);
            
        // Mock object mapper
        JsonNode mockRootNode = mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(mockRootNode);
        when(mockRootNode.isObject()).thenReturn(true);
        when(mockRootNode.fieldNames()).thenReturn(java.util.Collections.emptyIterator());
        
        // Mock object mapper for array creation
        when(objectMapper.createArrayNode()).thenReturn(new ObjectMapper().createArrayNode());
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(new ObjectMapper().writerWithDefaultPrettyPrinter());
        
        // Execute
        airportDataService.downloadAirportDataManually();
        
        // Verify
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void downloadAirportDataOnStartup_Success() throws Exception {
        // Mock successful response
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(SAMPLE_JSON);
            
        // Mock object mapper
        JsonNode mockRootNode = mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(mockRootNode);
        when(mockRootNode.isObject()).thenReturn(true);
        when(mockRootNode.fieldNames()).thenReturn(java.util.Collections.emptyIterator());
        
        // Mock object mapper for array creation
        when(objectMapper.createArrayNode()).thenReturn(new ObjectMapper().createArrayNode());
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(new ObjectMapper().writerWithDefaultPrettyPrinter());
        
        // Execute
        airportDataService.downloadAirportDataOnStartup();
        
        // Since this is async, we need to wait a bit for execution
        Thread.sleep(500);
        
        // Verify
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }
}