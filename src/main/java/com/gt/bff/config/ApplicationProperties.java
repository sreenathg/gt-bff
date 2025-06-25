package com.gt.bff.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the application.
 * Properties are loaded from application.yml with the 'application' prefix.
 */
@Data
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    
    /**
     * API related properties.
     */
    @Data
    public static class Api {
        private String version;
        private String title;
        private String description;
    }
    
    /**
     * Weather service related properties.
     */
    @Data
    public static class Weather {
        @JsonProperty("default")
        private String defaultForecast;
        private String tokyo;
        private String london;
        private String newyork;
    }
    
    /**
     * Cache configuration properties.
     */
    @Data
    public static class Cache {
        private boolean enabled;
        private long ttl;
    }
    
    /**
     * CORS configuration properties.
     */
    @Data
    public static class Cors {
        private java.util.List<String> allowedOrigins;
        private java.util.List<String> allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
    }
    
    private String version = "1.0.0";
    private final Weather weather = new Weather();
    private final Api api = new Api();
    private final Cache cache = new Cache();
    private final Cors cors = new Cors();
    
    /**
     * Gets the forecast for a specific destination.
     * 
     * @param destination the destination
     * @return the forecast for the destination, or default if not found
     */
    public String getForecastForDestination(String destination) {
        if (destination == null) {
            return weather.getDefaultForecast();
        }
        
        return switch (destination.toLowerCase()) {
            case "tokyo" -> weather.getTokyo();
            case "london" -> weather.getLondon();
            case "new york", "newyork" -> weather.getNewyork();
            default -> weather.getDefaultForecast();
        };
    }
}
