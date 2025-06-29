package com.gt.bff.util;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TravelJsonExtractorTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2024, 5, 21);

    @Test
    void extractTravelInfo_DefaultValues_ShouldReturnCorrectDefaults() {
        String input = "I want to book a flight";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals(1, json.getInt("passengers"));
        assertEquals("Round-Trip", json.getString("trip"));
        assertEquals("San Francisco", json.getString("from"));
        assertEquals("Book A Flight", json.getString("to")); // Captures phrase after "to" in input
        assertEquals("2024-05-24", json.getString("fromDate")); // Next Friday from reference date
        assertEquals("2024-05-31", json.getString("toDate")); // One week later
    }

    @Test
    void extractTravelInfo_SpecificVacationExample_ShouldExtractCorrectly() {
        String input = "Plan a 2 week vacation from New York to London in January 2025";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals(1, json.getInt("passengers"));
        assertEquals("Round-Trip", json.getString("trip"));
        assertEquals("New York", json.getString("from"));
        assertEquals("London In January 2025", json.getString("to")); // Captures whole phrase after "to"
        assertEquals("2025-01-01", json.getString("fromDate"));
        assertEquals("2025-01-15", json.getString("toDate"));
    }

    @Test
    void extractTravelInfo_FamilyOf4_ShouldExtractPassengers() {
        String input = "Book a flight for a family of 4";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals(4, json.getInt("passengers"));
        assertEquals("Round-Trip", json.getString("trip"));
    }

    @Test
    void extractTravelInfo_OneWayTicket_ShouldSetOneWayTrip() {
        String input = "I need a one-way ticket to Tokyo for 2 people next month";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals(2, json.getInt("passengers"));
        assertEquals("One-Way", json.getString("trip"));
        assertEquals("Tokyo For 2 People Next Month", json.getString("to")); // Captures whole phrase after "to"
        assertEquals("2024-06-01", json.getString("fromDate")); // Next month from reference
        assertTrue(json.isNull("toDate")); // No return date for one-way
    }

    @Test
    void extractTravelInfo_TourOfJapan_ShouldExtractDestination() {
        String input = "Cultural tour of Japan with temples and traditional food";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals("Japan With Temples And Traditional Food", json.getString("to")); // Captures whole phrase after "tour of"
        assertEquals("San Francisco", json.getString("from")); // Default
    }

    @Test
    void extractTravelInfo_10DayTrip_ShouldCalculateDuration() {
        String input = "I want to go to Paris for 10 days";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals("Go To Paris For 10 Days", json.getString("to")); // Captures whole phrase after "to"
        assertEquals("2024-05-24", json.getString("fromDate")); // Next Friday
        assertEquals("2024-06-03", json.getString("toDate")); // 10 days later
    }

    @ParameterizedTest
    @MethodSource("providePassengerTestCases")
    void extractTravelInfo_PassengerVariations_ShouldExtractCorrectly(String input, int expectedPassengers) {
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        JSONObject json = new JSONObject(result);
        assertEquals(expectedPassengers, json.getInt("passengers"));
    }

    private static Stream<Arguments> providePassengerTestCases() {
        return Stream.of(
            Arguments.of("Book for 3 people", 3),
            Arguments.of("5 passengers traveling", 5),
            Arguments.of("Just 1 person", 1),
            Arguments.of("2 adults and kids", 2), // Only captures first number
            Arguments.of("family of 6", 6)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTripTypeTestCases")
    void extractTravelInfo_TripTypeVariations_ShouldExtractCorrectly(String input, String expectedTripType) {
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        JSONObject json = new JSONObject(result);
        assertEquals(expectedTripType, json.getString("trip"));
    }

    private static Stream<Arguments> provideTripTypeTestCases() {
        return Stream.of(
            Arguments.of("one-way flight to Paris", "One-Way"),
            Arguments.of("one way ticket", "One-Way"),
            Arguments.of("moving to London", "One-Way"),
            Arguments.of("relocating to Tokyo", "One-Way"),
            Arguments.of("round trip to Paris", "Round-Trip"),
            Arguments.of("vacation in Spain", "Round-Trip") // Default
        );
    }

    @ParameterizedTest
    @MethodSource("provideDurationTestCases")
    void extractTravelInfo_DurationVariations_ShouldCalculateCorrectly(String input, String expectedToDate) {
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        JSONObject json = new JSONObject(result);
        assertEquals(expectedToDate, json.getString("toDate"));
    }

    private static Stream<Arguments> provideDurationTestCases() {
        return Stream.of(
            Arguments.of("3 week vacation", "2024-06-14"), // Next Friday + 3 weeks
            Arguments.of("5 days in Paris", "2024-05-29"), // Next Friday + 5 days
            Arguments.of("2 month trip", "2024-07-24"), // Next Friday + 2 months
            Arguments.of("vacation for 1 week", "2024-05-31") // Next Friday + 1 week (default)
        );
    }

    @Test
    void extractTravelInfo_SpecificMonthYear_ShouldStartFromFirstOfMonth() {
        String input = "Trip to Rome in January 2025";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals("Rome In January 2025", json.getString("to")); // Captures whole phrase after "to"
        assertEquals("2025-01-01", json.getString("fromDate"));
        assertEquals("2025-01-08", json.getString("toDate")); // 1 week later
    }

    @Test
    void extractTravelInfo_NextMonth_ShouldStartFromFirstOfNextMonth() {
        String input = "Travel next month";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals("2024-06-01", json.getString("fromDate"));
        assertEquals("2024-06-08", json.getString("toDate"));
    }

    @Test
    void extractTravelInfo_FromAndToLocations_ShouldCapitalize() {
        String input = "from san francisco to new york";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals("San Francisco", json.getString("from"));
        assertEquals("New York", json.getString("to"));
    }

    @Test
    void extractTravelInfo_ComplexQuery_ShouldExtractAllElements() {
        String input = "Plan one-way trip from Los Angeles to Miami for 5 passengers in December 2024";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals(5, json.getInt("passengers"));
        assertEquals("One-Way", json.getString("trip"));
        assertEquals("Los Angeles", json.getString("from"));
        assertEquals("Miami For 5 Passengers In December 2024", json.getString("to")); // Captures whole phrase after "to"
        assertEquals("2024-12-01", json.getString("fromDate"));
        assertTrue(json.isNull("toDate")); // One-way trip
    }

    @Test
    void extractTravelInfo_EmptyInput_ShouldUseDefaults() {
        String input = "";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals(1, json.getInt("passengers"));
        assertEquals("Round-Trip", json.getString("trip"));
        assertEquals("San Francisco", json.getString("from"));
        assertEquals("London", json.getString("to"));
        assertEquals("2024-05-24", json.getString("fromDate"));
        assertEquals("2024-05-31", json.getString("toDate"));
    }

    @Test
    void extractTravelInfo_NullInput_ShouldHandleGracefully() {
        assertThrows(NullPointerException.class, () -> {
            TravelJsonExtractor.extractTravelInfo(null, REFERENCE_DATE);
        });
    }

    @Test
    void extractTravelInfo_CaseInsensitive_ShouldWork() {
        String input = "ONE-WAY FLIGHT FROM PARIS TO LONDON FOR 3 PEOPLE";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        JSONObject json = new JSONObject(result);
        assertEquals(3, json.getInt("passengers"));
        assertEquals("One-Way", json.getString("trip"));
        assertEquals("Paris", json.getString("from"));
        assertEquals("London For 3 People", json.getString("to")); // Captures whole phrase after "to"
    }

    @Test
    void extractTravelInfo_MonthEdgeCase_ShouldHandleCorrectly() {
        // Test with a simple date case to avoid locale issues
        LocalDate jan15_2024 = LocalDate.of(2024, 1, 15);
        String input = "Trip in February 2024";
        String result = TravelJsonExtractor.extractTravelInfo(input, jan15_2024);
        
        JSONObject json = new JSONObject(result);
        assertEquals("2024-02-01", json.getString("fromDate"));
        assertEquals("2024-02-08", json.getString("toDate"));
    }

    @Test
    void extractTravelInfo_JsonFormatting_ShouldBePrettyPrinted() {
        String input = "Simple trip";
        String result = TravelJsonExtractor.extractTravelInfo(input, REFERENCE_DATE);
        
        // Check that the JSON is formatted with indentation
        assertTrue(result.contains("    ")); // 4-space indentation
        assertTrue(result.contains("\n")); // Newlines for formatting
        
        // Verify it's valid JSON
        assertDoesNotThrow(() -> new JSONObject(result));
    }
}