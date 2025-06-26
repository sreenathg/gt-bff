package com.gt.bff.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Timer genaiRequestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("genai.request.duration")
                .description("Time taken for GenAI API requests")
                .tag("service", "google-genai")
                .register(meterRegistry);
    }

    @Bean
    public Counter genaiRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("genai.request.total")
                .description("Total number of GenAI API requests")
                .tag("service", "google-genai")
                .register(meterRegistry);
    }

    @Bean
    public Counter genaiErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("genai.request.errors")
                .description("Total number of GenAI API errors")
                .tag("service", "google-genai")
                .register(meterRegistry);
    }

    @Bean
    public Timer genaiSearchFilterTimer(MeterRegistry meterRegistry) {
        return Timer.builder("genai.search.filter.duration")
                .description("Time taken for GenAI search filter processing")
                .tag("operation", "search-filter")
                .register(meterRegistry);
    }

    @Bean
    public Timer genaiTravelAdviceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("genai.travel.advice.duration")
                .description("Time taken for GenAI travel advice processing")
                .tag("operation", "travel-advice")
                .register(meterRegistry);
    }

    @Bean
    public Timer genaiLocationExtractionTimer(MeterRegistry meterRegistry) {
        return Timer.builder("genai.location.extraction.duration")
                .description("Time taken for GenAI location extraction processing")
                .tag("operation", "location-extraction")
                .register(meterRegistry);
    }
}