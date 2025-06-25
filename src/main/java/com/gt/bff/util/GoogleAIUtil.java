package com.gt.bff.util;

import com.gt.bff.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
 * Utility class for Google Generative AI operations using REST API
 * This implementation uses direct HTTP calls to the Google GenAI API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAIUtil implements GenAIService {

    private static final String BASE_API_URL = "https://generativelanguage.googleapis.com/v1/models/";
    private static final String CONTENT_TYPE = "application/json";
    private static final String NO_CONTENT_MESSAGE = "No content generated";
    private static final String FALLBACK_EXPLANATION = "Unable to generate explanation at this time.";
    private static final String FALLBACK_TRAVEL_RESPONSE = "Unable to process travel query at this time.";
    private static final String FALLBACK_LOCATION = "Unknown Location";

    private final ApplicationProperties applicationProperties;
    private boolean isInitialized = false;

    @PostConstruct
    public void init() {
        String apiKey = applicationProperties.getGenai().getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            isInitialized = true;
            log.info("GenAI utility initialized successfully");
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
    public String generateContent(String prompt) {
        return generateContent(applicationProperties.getGenai().getDefaultModel(), prompt);
    }

    /**
     * Generates content using Google GenAI REST API with specified model
     *
     * @param model The model to use (e.g., "gemini-2.5-flash")
     * @param prompt The prompt to send to the AI
     * @return Generated content as string
     * @throws IllegalStateException if not initialized
     */
    public String generateContent(String model, String prompt) {
        if (!isInitialized) {
            throw new IllegalStateException("GenAI utility is not initialized. Please check your API key configuration.");
        }

        try {
            log.debug("Generating content with model: {} and prompt: {}", model, prompt);
            
            String apiUrl = buildApiUrl(model);
            JSONObject requestBody = buildRequestBody(prompt);
            
            return makeApiCall(apiUrl, requestBody);
        } catch (Exception e) {
            log.error("Error generating content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate content: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a simple text explanation using AI
     *
     * @param topic The topic to explain
     * @return AI-generated explanation
     */
    public String explainTopic(String topic) {
        String prompt = applicationProperties.getGenai().getExplainPromptTemplate().replace("{topic}", topic);
        try {
            return generateContent(prompt);
        } catch (Exception e) {
            log.error("Failed to explain topic '{}': {}", topic, e.getMessage());
            return FALLBACK_EXPLANATION;
        }
    }

    /**
     * Processes travel-related queries using AI
     *
     * @param travelQuery The travel-related query
     * @return AI-generated travel advice or information
     */
    public String processTravelQuery(String travelQuery) {
        String prompt = applicationProperties.getGenai().getTravelAdvicePromptTemplate().replace("{query}", travelQuery);
        try {
            return generateContent(prompt);
        } catch (Exception e) {
            log.error("Failed to process travel query '{}': {}", travelQuery, e.getMessage());
            return FALLBACK_TRAVEL_RESPONSE;
        }
    }

    /**
     * Extracts geo location information from travel query
     *
     * @param query The travel query containing location information
     * @return Extracted or inferred location information
     */
    public String extractGeoLocation(String query) {
        String prompt = applicationProperties.getGenai().getLocationExtractionPromptTemplate().replace("{query}", query);
        try {
            return generateContent(prompt);
        } catch (Exception e) {
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
     * Checks if the GenAI utility is available and ready to use
     *
     * @return true if initialized with API key, false otherwise
     */
    public boolean isAvailable() {
        return isInitialized;
    }

    private String buildApiUrl(String model) {
        return BASE_API_URL + model + ":generateContent?key=" + applicationProperties.getGenai().getApiKey();
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
        generationConfig.put("temperature", genaiConfig.getTemperature());
        generationConfig.put("topP", genaiConfig.getTopP());
        generationConfig.put("topK", genaiConfig.getTopK());
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
        connection.setConnectTimeout(genaiConfig.getConnectTimeout());
        connection.setReadTimeout(genaiConfig.getReadTimeout());
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