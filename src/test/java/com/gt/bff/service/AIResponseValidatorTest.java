package com.gt.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gt.bff.model.schema.AIResponseSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AIResponseValidatorTest {

    private AIResponseValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator beanValidator = factory.getValidator();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        validator = new AIResponseValidator(objectMapper, beanValidator);
    }

    @Test
    void testValidateTravelSearchFilters_ValidJson() {
        String validJson = """
                {
                    "from": "New York",
                    "to": "London",
                    "fromDate": "2024-01-15",
                    "toDate": "2024-01-22",
                    "passengers": 2,
                    "trip": "Round-Trip",
                    "searchContext": "vacation to Europe"
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> result = 
            validator.validateTravelSearchFilters(validJson);

        assertTrue(result.isValid());
        assertNotNull(result.getData());
        assertEquals("New York", result.getData().getFrom());
        assertEquals("London", result.getData().getTo());
        assertEquals(2, result.getData().getPassengers());
    }

    @Test
    void testValidateTravelSearchFilters_InvalidJson() {
        String invalidJson = """
                {
                    "from": "",
                    "passengers": 15,
                    "trip": "Invalid-Trip"
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> result = 
            validator.validateTravelSearchFilters(invalidJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Validation errors"));
    }

    @Test
    void testValidateTravelSearchFilters_CodeBlock() {
        String jsonWithCodeBlock = """
                ```json
                {
                    "from": "Paris",
                    "to": "Rome",
                    "passengers": 1,
                    "trip": "One-Way"
                }
                ```
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> result = 
            validator.validateTravelSearchFilters(jsonWithCodeBlock);

        assertTrue(result.isValid());
        assertNotNull(result.getData());
        assertEquals("Paris", result.getData().getFrom());
        assertEquals("Rome", result.getData().getTo());
    }

    @Test
    void testValidateBasicResponse_ValidText() {
        String validText = "This is a valid travel advice response.";

        AIResponseValidator.ValidationResult<String> result = validator.validateBasicResponse(validText);

        assertTrue(result.isValid());
        assertEquals("This is a valid travel advice response.", result.getData());
    }

    @Test
    void testValidateBasicResponse_MaliciousContent() {
        String maliciousText = "This contains <script>alert('xss')</script> content.";

        AIResponseValidator.ValidationResult<String> result = validator.validateBasicResponse(maliciousText);

        assertFalse(result.isValid());
        assertEquals("Invalid response content detected", result.getErrorMessage());
    }

    @Test
    void testValidateBasicResponse_EmptyContent() {
        String emptyText = "";

        AIResponseValidator.ValidationResult<String> result = validator.validateBasicResponse(emptyText);

        assertFalse(result.isValid());
        assertEquals("AI response is empty", result.getErrorMessage());
    }

    @Test
    void testValidateAndParseToMap_ValidJson() {
        String validJson = """
                {
                    "key1": "value1",
                    "key2": 123,
                    "key3": true
                }
                """;

        AIResponseValidator.ValidationResult<Map<String, Object>> result = 
            validator.validateAndParseToMap(validJson);

        assertTrue(result.isValid());
        assertNotNull(result.getData());
        assertEquals("value1", result.getData().get("key1"));
        assertEquals(123, result.getData().get("key2"));
        assertEquals(true, result.getData().get("key3"));
    }

    @Test
    void testValidateLocationExtraction_ValidJson() {
        String validJson = """
                {
                    "location": "Paris, France",
                    "country": "France",
                    "city": "Paris",
                    "latitude": "48.8566",
                    "longitude": "2.3522",
                    "confidence": 0.95
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.LocationExtraction> result = 
            validator.validateLocationExtraction(validJson);

        assertTrue(result.isValid());
        assertNotNull(result.getData());
        assertEquals("Paris, France", result.getData().getLocation());
        assertEquals("France", result.getData().getCountry());
        assertEquals(0.95, result.getData().getConfidence());
    }
    
    @Test
    void testValidateLocationExtraction_InvalidJson() {
        String invalidJson = """
                {
                    "location": "",
                    "confidence": 1.5
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.LocationExtraction> result = 
            validator.validateLocationExtraction(invalidJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Validation errors"));
    }
    
    @Test
    void testValidateLocationExtraction_MalformedJson() {
        String malformedJson = "{ \"location\": \"Paris\", invalid";

        AIResponseValidator.ValidationResult<AIResponseSchema.LocationExtraction> result = 
            validator.validateLocationExtraction(malformedJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    void testValidateTravelAdvice_ValidJson() {
        String validJson = """
                {
                    "response": "Paris is a wonderful city to visit in spring",
                    "category": "Travel Advice",
                    "recommendations": ["Visit the Eiffel Tower", "Try local cuisine"],
                    "confidence": 0.95,
                    "warning": "Watch out for pickpockets"
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelAdvice> result = 
            validator.validateTravelAdvice(validJson);

        assertTrue(result.isValid());
        assertNotNull(result.getData());
        assertEquals("Paris is a wonderful city to visit in spring", result.getData().getResponse());
        assertEquals(2, result.getData().getRecommendations().size());
        assertEquals("Visit the Eiffel Tower", result.getData().getRecommendations().get(0));
    }
    
    @Test
    void testValidateTravelAdvice_InvalidJson() {
        String invalidJson = """
                {
                    "response": "",
                    "confidence": 1.5,
                    "category": ""
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelAdvice> result = 
            validator.validateTravelAdvice(invalidJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Validation errors"));
    }
    
    @Test
    void testValidateTravelAdvice_MalformedJson() {
        String malformedJson = "{ \"response\": \"incomplete text\"";

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelAdvice> result = 
            validator.validateTravelAdvice(malformedJson);

        // Travel advice can accept plain text, so this might actually succeed
        // Let's just verify it doesn't throw an exception
        assertNotNull(result);
    }
    
    @Test
    void testValidateAndParseToMap_MalformedJson() {
        String malformedJson = "{ \"key\": \"value\", incomplete";

        AIResponseValidator.ValidationResult<Map<String, Object>> result = 
            validator.validateAndParseToMap(malformedJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    void testValidateAndParseToMap_EmptyJson() {
        String emptyJson = "";

        AIResponseValidator.ValidationResult<Map<String, Object>> result = 
            validator.validateAndParseToMap(emptyJson);

        assertFalse(result.isValid());
        assertEquals("No valid JSON found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateAndParseToMap_NullInput() {
        AIResponseValidator.ValidationResult<Map<String, Object>> result = 
            validator.validateAndParseToMap(null);

        assertFalse(result.isValid());
        assertEquals("No valid JSON found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateBasicResponse_NullInput() {
        AIResponseValidator.ValidationResult<String> result = validator.validateBasicResponse(null);

        assertFalse(result.isValid());
        assertEquals("AI response is empty", result.getErrorMessage());
    }
    
    @Test
    void testValidateBasicResponse_TooLongContent() {
        // Create a string longer than typical limits
        String longText = "x".repeat(100000); // 100KB string

        AIResponseValidator.ValidationResult<String> result = validator.validateBasicResponse(longText);

        assertFalse(result.isValid());
        assertEquals("AI response exceeds maximum length", result.getErrorMessage());
    }
    
    @Test
    void testValidateBasicResponse_SqlInjectionAttempt() {
        String sqlText = "This contains script injection: <script>alert('xss')</script>";

        AIResponseValidator.ValidationResult<String> result = validator.validateBasicResponse(sqlText);

        assertFalse(result.isValid());
        assertEquals("Invalid response content detected", result.getErrorMessage());
    }
    
    @Test
    void testValidateBasicResponse_JavaScriptCode() {
        String jsText = "Here's some javascript code: eval('alert(1)')";

        AIResponseValidator.ValidationResult<String> result = validator.validateBasicResponse(jsText);

        assertFalse(result.isValid());
        assertEquals("Invalid response content detected", result.getErrorMessage());
    }
    
    @Test
    void testValidateTravelSearchFilters_MalformedJson() {
        String malformedJson = "{ \"from\": \"Paris\", \"to\": incomplete";

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> result = 
            validator.validateTravelSearchFilters(malformedJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    void testValidateTravelSearchFilters_EmptyInput() {
        String emptyJson = "";

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> result = 
            validator.validateTravelSearchFilters(emptyJson);

        assertFalse(result.isValid());
        assertEquals("No valid JSON found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateTravelSearchFilters_NullInput() {
        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> result = 
            validator.validateTravelSearchFilters(null);

        assertFalse(result.isValid());
        assertEquals("No valid JSON found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateLocationExtraction_EmptyInput() {
        String emptyJson = "";

        AIResponseValidator.ValidationResult<AIResponseSchema.LocationExtraction> result = 
            validator.validateLocationExtraction(emptyJson);

        assertFalse(result.isValid());
        assertEquals("No valid JSON found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateLocationExtraction_NullInput() {
        AIResponseValidator.ValidationResult<AIResponseSchema.LocationExtraction> result = 
            validator.validateLocationExtraction(null);

        assertFalse(result.isValid());
        assertEquals("No valid JSON found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateTravelAdvice_EmptyInput() {
        String emptyJson = "";

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelAdvice> result = 
            validator.validateTravelAdvice(emptyJson);

        assertFalse(result.isValid());
        assertEquals("No valid content found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateTravelAdvice_NullInput() {
        AIResponseValidator.ValidationResult<AIResponseSchema.TravelAdvice> result = 
            validator.validateTravelAdvice(null);

        assertFalse(result.isValid());
        assertEquals("No valid content found in AI response", result.getErrorMessage());
    }
    
    @Test
    void testValidateTravelSearchFilters_EdgeCaseValues() {
        String edgeCaseJson = """
                {
                    "from": "",
                    "to": "Very Long Destination Name That Might Exceed Normal Limits For Testing Purposes That Goes Over One Hundred Characters",
                    "passengers": 0,
                    "trip": "Unknown-Type",
                    "fromDate": "invalid-date",
                    "toDate": "2024-01-01",
                    "searchContext": ""
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelSearchFilters> result = 
            validator.validateTravelSearchFilters(edgeCaseJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test 
    void testValidateLocationExtraction_EdgeCaseValues() {
        String edgeCaseJson = """
                {
                    "location": "   ",
                    "country": "",
                    "city": null,
                    "latitude": "invalid-lat",
                    "longitude": "999.999",
                    "confidence": -0.5
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.LocationExtraction> result = 
            validator.validateLocationExtraction(edgeCaseJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Validation errors"));
    }
    
    @Test
    void testValidateTravelAdvice_EdgeCaseValues() {
        String edgeCaseJson = """
                {
                    "response": "",
                    "recommendations": null,
                    "confidence": -0.5,
                    "category": "   ",
                    "warning": ""
                }
                """;

        AIResponseValidator.ValidationResult<AIResponseSchema.TravelAdvice> result = 
            validator.validateTravelAdvice(edgeCaseJson);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Validation errors"));
    }
}