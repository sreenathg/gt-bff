package com.gt.bff.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SearchInputValidatorTest {

    @InjectMocks
    private SearchInputValidator validator;

    @Test
    void validate_NullOrEmptyInput_ReturnsError() {
        // Test null input
        SearchInputValidator.ValidationResult nullResult = validator.validate(null);
        assertFalse(nullResult.isValid());
        assertTrue(nullResult.getErrors().contains("Search input cannot be empty"));
        assertTrue(nullResult.getWarnings().isEmpty());

        // Test empty string
        SearchInputValidator.ValidationResult emptyResult = validator.validate("");
        assertFalse(emptyResult.isValid());
        assertTrue(emptyResult.getErrors().contains("Search input cannot be empty"));
        assertTrue(emptyResult.getWarnings().isEmpty());

        // Test whitespace only
        SearchInputValidator.ValidationResult whitespaceResult = validator.validate("   ");
        assertFalse(whitespaceResult.isValid());
        assertTrue(whitespaceResult.getErrors().contains("Search input cannot be empty"));
        assertTrue(whitespaceResult.getWarnings().isEmpty());
    }

    @Test
    void validate_LengthConstraints_ValidatesCorrectly() {
        // Test minimum length
        String minLengthInput = "a".repeat(SearchInputValidator.MIN_LENGTH - 1);
        SearchInputValidator.ValidationResult minLengthResult = validator.validate(minLengthInput);
        assertFalse(minLengthResult.isValid());
        assertTrue(minLengthResult.getErrors().stream()
            .anyMatch(e -> e.contains("must be at least")));

        // Test maximum length
        String maxLengthInput = "a".repeat(SearchInputValidator.MAX_LENGTH + 1);
        SearchInputValidator.ValidationResult maxLengthResult = validator.validate(maxLengthInput);
        assertFalse(maxLengthResult.isValid());
        assertTrue(maxLengthResult.getErrors().stream()
            .anyMatch(e -> e.contains("cannot exceed")));
    }

    @Test
    void validate_WordCount_ValidatesCorrectly() {
        // Test minimum word count
        String minWordsInput = "one two three four"; // 4 words when MIN_WORDS is 5
        SearchInputValidator.ValidationResult wordCountResult = validator.validate(minWordsInput);
        assertFalse(wordCountResult.isValid());
        assertTrue(wordCountResult.getErrors().stream()
            .anyMatch(e -> e.contains("must contain at least")));
    }

    @ParameterizedTest
    @MethodSource("provideSecurityTestCases")
    void validate_SecurityChecks_DetectsThreats(String input, String expectedError) {
        SearchInputValidator.ValidationResult result = validator.validate(input);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> e.contains(expectedError)), 
            "Expected error containing: " + expectedError);
    }

    private static Stream<Arguments> provideSecurityTestCases() {
        return Stream.of(
            Arguments.of("SELECT * FROM users", "SQL injection"),
            Arguments.of("<script>alert('xss')</script>", "XSS"),
            Arguments.of("valid but with <<<<<<<<<<<<<<<<<<<<<<<<<<", "excessive special characters")
        );
    }

    @ParameterizedTest
    @MethodSource("provideWarningTestCases")
    void validate_WarningScenarios_GeneratesWarnings(String input, String expectedWarning) {
        SearchInputValidator.ValidationResult result = validator.validate(input);
        assertTrue(result.getWarnings().stream()
            .anyMatch(w -> w.contains(expectedWarning)), 
            "Expected warning containing: " + expectedWarning);
    }

    private static Stream<Arguments> provideWarningTestCases() {
        return Stream.of(
            Arguments.of("valid but with $%^&*", "unusual characters"),
            Arguments.of("this is a very long input without any specific related things for searching", "No travel-related keywords detected")
        );
    }

    @Test
    void validate_ValidInput_ReturnsValidResult() {
        String validInput = "I want to book a flight from New York to London in July";
        SearchInputValidator.ValidationResult result = validator.validate(validInput);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void validate_MultipleIssues_ReturnsAllRelevantErrorsAndWarnings() {
        String problematicInput = "SELECT * FROM users <script>alert('xss')</script> $%^&* short";
        SearchInputValidator.ValidationResult result = validator.validate(problematicInput);
        
        assertFalse(result.isValid());
        // Should have multiple errors
        assertTrue(result.getErrors().size() >= 2);
        // Should have warnings
        assertFalse(result.getWarnings().isEmpty());
    }

    @Test
    void validate_EdgeCaseInputs_HandlesGracefully() {
        // Input with exactly minimum length and words
        String edgeCaseInput = "a b c d e"; // 5 words, 9 characters with spaces
        SearchInputValidator.ValidationResult result = validator.validate(edgeCaseInput);
        
        // Should be valid but might have warnings
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_WithTravelKeywords_NoWarning() {
        // Input with travel keywords should not generate keyword warning
        String[] travelKeywords = {"to", "from", "flight", "trip", "travel", "hotel", "car rental", "vacation", "destination"};
        
        for (String keyword : travelKeywords) {
            String input = String.format("I need a %s for my trip", keyword);
            SearchInputValidator.ValidationResult result = validator.validate(input);
            assertFalse(result.getWarnings().stream()
                .anyMatch(w -> w.contains("No travel-related keywords detected")),
                "Should not warn about missing travel keywords when " + keyword + " is present");
        }
    }
}
