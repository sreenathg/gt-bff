package com.gt.bff.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApplicationProperties.class)
public class CorsConfig {

    private final ApplicationProperties applicationProperties;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        applicationProperties.getCors().getAllowedOrigins()
            .forEach(configuration::addAllowedOrigin);
        
        applicationProperties.getCors().getAllowedMethods()
            .forEach(configuration::addAllowedMethod);
            
        configuration.addAllowedHeader(applicationProperties.getCors().getAllowedHeaders());
        configuration.setAllowCredentials(applicationProperties.getCors().isAllowCredentials());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}