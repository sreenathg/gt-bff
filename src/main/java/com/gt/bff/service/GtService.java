package com.gt.bff.service;

/**
 * Main service interface that coordinates weather and travel services.
 * This acts as a facade to provide a unified interface for the GT BFF application.
 */
public interface GtService extends WeatherService, TravelService {
}
