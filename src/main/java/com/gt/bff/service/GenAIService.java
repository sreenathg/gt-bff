package com.gt.bff.service;

/**
 * Interface for Generative AI operations
 */
public interface GenAIService {

    /**
     * Generates content using AI with default model
     *
     * @param prompt The prompt to send to the AI
     * @return Generated content as string
     */
    String generateContent(String prompt);

    /**
     * Generates content using AI with specified model
     *
     * @param model The model to use
     * @param prompt The prompt to send to the AI
     * @return Generated content as string
     */
    String generateContent(String model, String prompt);

    /**
     * Generates a simple text explanation using AI
     *
     * @param topic The topic to explain
     * @return AI-generated explanation
     */
    String explainTopic(String topic);

    /**
     * Processes travel-related queries using AI
     *
     * @param travelQuery The travel-related query
     * @return AI-generated travel advice or information
     */
    String processTravelQuery(String travelQuery);

    /**
     * Extracts geo location information from travel query
     *
     * @param query The travel query containing location information
     * @return Extracted or inferred location information
     */
    String extractGeoLocation(String query);

    /**
     * Checks if the AI service is available and ready to use
     *
     * @return true if initialized and ready, false otherwise
     */
    boolean isAvailable();
}