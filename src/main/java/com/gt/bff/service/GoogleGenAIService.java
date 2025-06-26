package com.gt.bff.service;

import com.gt.bff.config.ApplicationProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Service for Google Generative AI operations using REST API
 * This implementation uses direct HTTP calls to the Google GenAI API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleGenAIService implements GenAIService {

    private static final String BASE_API_URL = "https://generativelanguage.googleapis.com/v1/models/";
    private static final String CONTENT_TYPE = "application/json";
    private static final String NO_CONTENT_MESSAGE = "No content generated";
    private static final String FALLBACK_EXPLANATION = "Unable to generate explanation at this time.";
    private static final String FALLBACK_TRAVEL_RESPONSE = "Unable to process travel query at this time.";
    private static final String FALLBACK_LOCATION = "Unknown Location";

    private final ApplicationProperties applicationProperties;
    private final Timer genaiRequestTimer;
    private final Counter genaiRequestCounter;
    private final Counter genaiErrorCounter;
    private final Timer genaiTravelAdviceTimer;
    private final Timer genaiLocationExtractionTimer;
    private final MeterRegistry meterRegistry;
    private final AIResponseValidator aiResponseValidator;
    private boolean isInitialized = false;

    @PostConstruct
    public void init() {
        String apiKey = applicationProperties.getGenai().getGoogle().getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            isInitialized = true;
            log.info("GenAI service initialized successfully");
        } else {
            log.warn("GenAI API key not configured. GenAI features will be disabled.");
        }
    }

    /**
     * Generates content using Google GenAI REST API
     *
     * @param prompt The prompt to send to the AI
     * @return Generated content as string
     * @throws IllegalStateException if not initialized
     */
    @Override
    public String generateContent(String prompt) {
        return generateContent(applicationProperties.getGenai().getGoogle().getDefaultModel(), prompt);
    }

    /**
     * Generates content using Google GenAI REST API with specified model
     *
     * @param model The model to use (e.g., "gemini-2.5-flash")
     * @param prompt The prompt to send to the AI
     * @return Generated content as string
     * @throws IllegalStateException if not initialized
     */
    @Override
    public String generateContent(String model, String prompt) {
        if (!isInitialized) {
            throw new IllegalStateException("GenAI service is not initialized. Please check your API key configuration.");
        }

        genaiRequestCounter.increment();
        long startTime = System.nanoTime();
        try {
            log.debug("Generating content with model: {} and prompt: {}", model, prompt);

            String apiUrl = buildApiUrl(model);
            JSONObject requestBody = buildRequestBody(prompt);

            String result = makeApiCall(apiUrl, requestBody);
            genaiRequestTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
            return result;
        } catch (Exception e) {
            genaiErrorCounter.increment();
            genaiRequestTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
            log.error("Error generating content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate content: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a simple text explanation using AI with response validation
     *
     * @param topic The topic to explain
     * @return AI-generated explanation
     */
    @Override
    public String explainTopic(String topic) {
        String prompt = applicationProperties.getGenai().getGoogle().getExplainPromptTemplate().replace("{topic}", topic);
        try {
            String rawResult = generateContent(prompt);
            
            // Validate the AI response
            AIResponseValidator.ValidationResult<String> validationResult = aiResponseValidator.validateBasicResponse(rawResult);
            return validationResult.isValid() ? validationResult.getData() : FALLBACK_EXPLANATION;
        } catch (Exception e) {
            log.error("Failed to explain topic '{}': {}", topic, e.getMessage());
            return FALLBACK_EXPLANATION;
        }
    }

    /**
     * Processes travel-related queries using AI with response validation
     *
     * @param travelQuery The travel-related query
     * @return AI-generated travel advice or information
     */
    @Override
    public String processTravelQuery(String travelQuery) {
        long startTime = System.nanoTime();
        try {
            String prompt = applicationProperties.getGenai().getGoogle().getTravelAdvicePromptTemplate().replace("{query}", travelQuery);
            String rawResult = generateContent(prompt);
            
            // Validate the AI response
            AIResponseValidator.ValidationResult<String> validationResult = aiResponseValidator.validateBasicResponse(rawResult);
            String result = validationResult.isValid() ? validationResult.getData() : FALLBACK_TRAVEL_RESPONSE;
            
            genaiTravelAdviceTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
            return result;
        } catch (Exception e) {
            genaiTravelAdviceTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
            log.error("Failed to process travel query '{}': {}", travelQuery, e.getMessage());
            return FALLBACK_TRAVEL_RESPONSE;
        }
    }

    /**
     * Extracts geo location information from travel query with response validation
     *
     * @param query The travel query containing location information
     * @return Extracted or inferred location information
     */
    @Override
    public String extractGeoLocation(String query) {
        long startTime = System.nanoTime();
        try {
            String prompt = applicationProperties.getGenai().getGoogle().getLocationExtractionPromptTemplate().replace("{query}", query);
            String rawResult = generateContent(prompt);
            
            // Validate the AI response
            AIResponseValidator.ValidationResult<String> validationResult = aiResponseValidator.validateBasicResponse(rawResult);
            String result = validationResult.isValid() ? validationResult.getData() : FALLBACK_LOCATION;
            
            genaiLocationExtractionTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
            return result;
        } catch (Exception e) {
            genaiLocationExtractionTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
            log.error("Failed to extract geo location from query '{}': {}", query, e.getMessage());
            return FALLBACK_LOCATION;
        }
    }

    /**
     * Extracts text content from GenAI JSON response
     *
     * @param jsonResponse The JSON response from GenAI API
     * @return Extracted text content
     */
    private String extractTextFromResponse(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            if (response.has("candidates")) {
                JSONArray candidates = response.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    if (candidate.has("content")) {
                        JSONObject content = candidate.getJSONObject("content");
                        if (content.has("parts")) {
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                JSONObject part = parts.getJSONObject(0);
                                if (part.has("text")) {
                                    return part.getString("text");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing GenAI response: {}", e.getMessage());
        }
        return NO_CONTENT_MESSAGE;
    }

    /**
     * Checks if the GenAI service is available and ready to use
     *
     * @return true if initialized with API key, false otherwise
     */
    @Override
    public boolean isAvailable() {
        return isInitialized;
    }

    private String buildApiUrl(String model) {
        return BASE_API_URL + model + ":generateContent?key=" + applicationProperties.getGenai().getGoogle().getApiKey();
    }

    private JSONObject buildRequestBody(String prompt) {
        JSONObject requestBody = new JSONObject();

        JSONObject part = new JSONObject();
        part.put("text", prompt);

        JSONArray parts = new JSONArray();
        parts.put(part);

        JSONObject content = new JSONObject();
        content.put("parts", parts);

        JSONArray contents = new JSONArray();
        contents.put(content);

        requestBody.put("contents", contents);
        requestBody.put("generationConfig", buildGenerationConfig());

        return requestBody;
    }

    private JSONObject buildGenerationConfig() {
        ApplicationProperties.GenAI genaiConfig = applicationProperties.getGenai();
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", genaiConfig.getGoogle().getTemperature());
        generationConfig.put("topP", genaiConfig.getGoogle().getTopP());
        generationConfig.put("topK", genaiConfig.getGoogle().getTopK());
        return generationConfig;
    }

    private String makeApiCall(String apiUrl, JSONObject requestBody) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = configureConnection(url);

        sendRequest(connection, requestBody);
        return handleResponse(connection);
    }

    private HttpURLConnection configureConnection(URL url) throws IOException {
        ApplicationProperties.GenAI genaiConfig = applicationProperties.getGenai();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setDoOutput(true);
        connection.setConnectTimeout(genaiConfig.getGoogle().getConnectTimeout());
        connection.setReadTimeout(genaiConfig.getGoogle().getReadTimeout());
        return connection;
    }

    private void sendRequest(HttpURLConnection connection, JSONObject requestBody) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private String handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String responseBody = readInputStream(connection.getInputStream());
            String result = extractTextFromResponse(responseBody);
            log.debug("Successfully generated content");
            return result;
        } else {
            String errorResponse = readInputStream(connection.getErrorStream());
            log.error("GenAI API error ({}): {}", responseCode, errorResponse);
            throw new RuntimeException("GenAI API error: " + responseCode + " - " + errorResponse);
        }
    }

    private String readInputStream(java.io.InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}