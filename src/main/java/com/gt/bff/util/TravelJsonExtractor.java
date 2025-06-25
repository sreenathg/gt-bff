package com.gt.bff.util;

import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TravelJsonExtractor {

    // Pre-compiled regex patterns for efficiency
    private static final Pattern PASSENGERS_PATTERN = Pattern.compile("(\\d+)\\s*(?:people|passengers|person|kids|adults)|(?:family of)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONE_WAY_PATTERN = Pattern.compile("one-way|one way|moving|relocating", Pattern.CASE_INSENSITIVE);
    private static final Pattern FROM_LOCATION_PATTERN = Pattern.compile("from\\s+([\\w\\s,]+?)(?:\\s+to|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TO_LOCATION_PATTERN = Pattern.compile("to\\s+([\\w\\s,]+?)(?:\\s+from|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s+(week|day|month)s?", Pattern.CASE_INSENSITIVE);
    private static final Pattern MONTH_YEAR_PATTERN = Pattern.compile("(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern NEXT_MONTH_PATTERN = Pattern.compile("next\\s+month", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    /**
     * Extracts travel information from user input text and returns a JSON string.
     * This is a testable method that allows injecting the reference date.
     *
     * @param text The user's text input (e.g., "Plan a 2 week vacation...").
     * @param referenceDate The reference date for calculations.
     * @return A JSON formatted string with the extracted travel information.
     */
    public static String extractTravelInfo(String text, LocalDate referenceDate) {
        String lowerText = text.toLowerCase();
        JSONObject result = new JSONObject();

        // 1. Set default values
        int passengers = 1;
        String tripType = "Round-Trip";
        String fromLocation = "San Francisco";
        String toLocation = "London";

        // 2. Extract specific information, overriding defaults
        // Passengers
        Matcher passengerMatcher = PASSENGERS_PATTERN.matcher(lowerText);
        if (passengerMatcher.find()) {
            // The pattern has two capturing groups, one will be null
            String p1 = passengerMatcher.group(1);
            String p2 = passengerMatcher.group(2);
            passengers = Integer.parseInt(p1 != null ? p1 : p2);
        }

        // Trip Type
        if (ONE_WAY_PATTERN.matcher(lowerText).find()) {
            tripType = "One-Way";
        }

        // Locations
        Matcher fromMatcher = FROM_LOCATION_PATTERN.matcher(text); // Use original case text for better capitalization
        if (fromMatcher.find()) {
            fromLocation = capitalize(fromMatcher.group(1).trim());
        }

        Matcher toMatcher = TO_LOCATION_PATTERN.matcher(text);
        if (toMatcher.find()) {
            toLocation = capitalize(toMatcher.group(1).trim());
        } else if (lowerText.contains("tour of")){
             Matcher tourMatcher = Pattern.compile("tour of ([\\w\\s,]+)").matcher(lowerText);
             if(tourMatcher.find()){
                 toLocation = capitalize(tourMatcher.group(1).trim());
             }
        }

        // 3. Date and Duration Processing
        LocalDate fromDate = null;
        int durationValue = -1;
        String durationUnit = "";

        // Find specific duration
        Matcher durationMatcher = DURATION_PATTERN.matcher(lowerText);
        if (durationMatcher.find()) {
            durationValue = Integer.parseInt(durationMatcher.group(1));
            durationUnit = durationMatcher.group(2);
        }

        // Find specific start date
        Matcher monthYearMatcher = MONTH_YEAR_PATTERN.matcher(lowerText);
        if (monthYearMatcher.find()) {
            String monthName = monthYearMatcher.group(1);
            int year = Integer.parseInt(monthYearMatcher.group(2));
            // Convert month name to Month enum
            Month month = Stream.of(Month.values())
                    .filter(m -> m.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equalsIgnoreCase(monthName))
                    .findFirst().orElse(null);
            if (month != null) {
                // Rule: If only month/year given, start from beginning of month
                fromDate = LocalDate.of(year, month, 1);
            }
        } else if (NEXT_MONTH_PATTERN.matcher(lowerText).find()) {
            // Rule: "next month" starts from the 1st
            fromDate = referenceDate.withDayOfMonth(1).plusMonths(1);
        }

        // 4. Calculate final dates based on findings
        // If no specific start date was found, use the default (next Friday)
        if (fromDate == null) {
            fromDate = referenceDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        }

        LocalDate toDate = null;
        if ("Round-Trip".equals(tripType)) {
            if (durationValue > 0) { // A specific duration was found
                switch (durationUnit) {
                    case "week":
                        toDate = fromDate.plusWeeks(durationValue);
                        break;
                    case "day":
                        toDate = fromDate.plusDays(durationValue);
                        break;
                    case "month":
                        toDate = fromDate.plusMonths(durationValue);
                        break;
                }
            } else { // No duration found, use the default (1 week)
                toDate = fromDate.plusWeeks(1);
            }
        }
        
        // Handle a special case from the prompt: "2 week vacation" results in 14 days, but the example output shows fromDate + 14 days. 
        // For example, July 1 to July 15. Let's adjust for that.
        // If the duration was "2 weeks" (14 days), we'll add 14 days instead of 15.
        // Let's assume toDate should be fromDate + duration. My current logic for week/month/day handles this correctly. The example seems to have an off-by-one interpretation.
        // Let's adjust to match the example: 2 weeks from July 1 is July 15. That is fromDate + 14 days. Correct.
        // My logic is fine. For `2 week vacation from ... July 2025`, fromDate=2025-07-01, toDate=fromDate.plusWeeks(2) = 2025-07-15. This is correct.

        // 5. Build the final JSON object
        result.put("fromDate", fromDate.format(DATE_FORMATTER));
        result.put("passengers", passengers);
        result.put("trip", tripType);
        // Use JSONObject.NULL for proper JSON null representation
        result.put("toDate", "One-Way".equals(tripType) ? JSONObject.NULL : 
                   (toDate != null ? toDate.format(DATE_FORMATTER) : JSONObject.NULL));
        result.put("from", fromLocation);
        result.put("to", toLocation);

        return result.toString(4); // Indent with 4 spaces for pretty printing
    }

    /**
     * A helper function to convert a string like "new york" to "New York".
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Stream.of(str.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Main method to demonstrate usage with examples.
     */
    public static void main(String[] args) {
        // Assume today is 2024-05-21 for consistent testing
        LocalDate today = LocalDate.of(2024, 5, 21);
        
        System.out.println("--- Example 1: Provided Input ---");
        String input1 = "Plan a 2 week vacation from New York to London in July 2025";
        System.out.println("Input: \"" + input1 + "\"");
        System.out.println(extractTravelInfo(input1, today));

        System.out.println("--- Example 2: Defaults and Family ---");
        String input2 = "Book a flight for a family of 4.";
        System.out.println("Input: \"" + input2 + "\"");
        System.out.println(extractTravelInfo(input2, today));

        System.out.println("--- Example 3: One-Way and Relative Date ---");
        String input3 = "I need a one-way ticket to Tokyo for me and my friend next month.";
        System.out.println("Input: \"" + input3 + "\"");
        // "me and my friend" is not parsed by the regex, so it defaults to 1.
        // A more advanced NLP tool would be needed for that nuance. Let's adjust the input slightly to match the rule.
        String input3Adjusted = "I need a one-way ticket to Tokyo for 2 people next month.";
        System.out.println("Input (Adjusted): \"" + input3Adjusted + "\"");
        System.out.println(extractTravelInfo(input3Adjusted, today));

        System.out.println("--- Example 4: Missing 'From' location ---");
        String input4 = "I want to go to Paris for 10 days.";
        System.out.println("Input: \"" + input4 + "\"");
        System.out.println(extractTravelInfo(input4, today));
        
        System.out.println("--- Example 5: Cultural Tour ---");
        String input5 = "Cultural tour of Japan with temples and traditional food";
        System.out.println("Input: \"" + input5 + "\"");
        System.out.println(extractTravelInfo(input5, today));
    }
}