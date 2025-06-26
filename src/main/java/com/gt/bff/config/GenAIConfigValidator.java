package com.gt.bff.config;

import com.gt.bff.exception.GenAIConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenAIConfigValidator {

    private final ApplicationProperties applicationProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void validateGenAIConfiguration() {
        String apiKey = applicationProperties.getGenai().getGoogle().getApiKey();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            String errorMessage = "GenAI API key is not configured. Please set the GENAI_API_KEY environment variable or configure application.genai.google.api-key property.";
            log.error(errorMessage);
            throw new GenAIConfigurationException(errorMessage);
        }

        if (apiKey.startsWith("${") && apiKey.endsWith("}")) {
            String errorMessage = "GenAI API key environment variable is not resolved. Please ensure GENAI_API_KEY is set in your environment.";
            log.error(errorMessage);
            throw new GenAIConfigurationException(errorMessage);
        }

        log.info("GenAI configuration validation passed successfully");
    }
}