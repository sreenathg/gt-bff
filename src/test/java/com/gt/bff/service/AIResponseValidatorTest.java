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
}