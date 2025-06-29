package com.gt.bff.service;

import com.gt.bff.config.ApplicationProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleGenAIServiceTest {

    @Mock
    private ApplicationProperties applicationProperties;
    
    @Mock
    private ApplicationProperties.GenAI genAi;
    
    @Mock
    private ApplicationProperties.GenAI.Google genAiGoogle;
    
    @Mock
    private Timer genaiRequestTimer;
    
    @Mock
    private Counter genaiRequestCounter;
    
    @Mock
    private Counter genaiErrorCounter;
    
    @Mock
    private Timer genaiTravelAdviceTimer;
    
    @Mock
    private Timer genaiLocationExtractionTimer;
    
    @Mock
    private AIResponseValidator aiResponseValidator;
    
    @InjectMocks
    private GoogleGenAIService googleGenAIService;
    
    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_MODEL = "test-model";
    
    @BeforeEach
    void setUp() {
        when(applicationProperties.getGenai()).thenReturn(genAi);
        when(genAi.getGoogle()).thenReturn(genAiGoogle);
    }
    
    @Test
    void init_WithValidApiKey_SetsInitializedToTrue() {
        when(genAiGoogle.getApiKey()).thenReturn(TEST_API_KEY);
        
        googleGenAIService.init();
        
        assertTrue((boolean) ReflectionTestUtils.getField(googleGenAIService, "isInitialized"));
    }
    
    @Test
    void init_WithNullApiKey_KeepsInitializedFalse() {
        when(genAiGoogle.getApiKey()).thenReturn(null);
        
        googleGenAIService.init();
        
        assertFalse((boolean) ReflectionTestUtils.getField(googleGenAIService, "isInitialized"));
    }
    
    @Test
    void init_WithEmptyApiKey_KeepsInitializedFalse() {
        when(genAiGoogle.getApiKey()).thenReturn("");
        
        googleGenAIService.init();
        
        assertFalse((boolean) ReflectionTestUtils.getField(googleGenAIService, "isInitialized"));
    }
    
    @Test
    void isAvailable_WhenInitialized_ReturnsTrue() {
        when(genAiGoogle.getApiKey()).thenReturn(TEST_API_KEY);
        
        googleGenAIService.init();
        
        assertTrue(googleGenAIService.isAvailable());
    }
    
    @Test
    void isAvailable_WhenNotInitialized_ReturnsFalse() {
        when(genAiGoogle.getApiKey()).thenReturn(null);
        
        googleGenAIService.init();
        
        assertFalse(googleGenAIService.isAvailable());
    }
    
    @Test
    void generateContent_WhenNotInitialized_ThrowsIllegalStateException() {
        when(genAiGoogle.getApiKey()).thenReturn(null);
        googleGenAIService.init();
        
        assertThrows(IllegalStateException.class, 
            () -> googleGenAIService.generateContent("test prompt"));
    }
    
    @Test
    void explainTopic_WhenNotInitialized_ReturnsFallbackExplanation() {
        when(genAiGoogle.getApiKey()).thenReturn(null);
        when(genAiGoogle.getExplainPromptTemplate()).thenReturn("Explain {topic}");
        googleGenAIService.init();
        
        String result = googleGenAIService.explainTopic("test topic");
        
        assertEquals("Unable to generate explanation at this time.", result);
    }
    
    @Test
    void processTravelQuery_WhenNotInitialized_ReturnsFallbackResponse() {
        when(genAiGoogle.getApiKey()).thenReturn(null);
        when(genAiGoogle.getTravelAdvicePromptTemplate()).thenReturn("Travel advice for {query}");
        googleGenAIService.init();
        
        String result = googleGenAIService.processTravelQuery("best places in Paris");
        
        assertEquals("Unable to process travel query at this time.", result);
    }
    
    @Test
    void extractGeoLocation_WhenNotInitialized_ReturnsFallbackLocation() {
        when(genAiGoogle.getApiKey()).thenReturn(null);
        when(genAiGoogle.getLocationExtractionPromptTemplate()).thenReturn("Extract location from {query}");
        googleGenAIService.init();
        
        String result = googleGenAIService.extractGeoLocation("hotels near Eiffel Tower");
        
        assertEquals("Unknown Location", result);
    }
    
}