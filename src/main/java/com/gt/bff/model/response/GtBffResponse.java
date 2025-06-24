package com.gt.bff.model.response;

public class GtBffResponse {
    private String destination;
    private String travelWindow;
    private String forecast;

    public GtBffResponse(String destination, String travelWindow, String forecast) {
        this.destination = destination;
        this.travelWindow = travelWindow;
        this.forecast = forecast;
    }

    public String getDestination() { return destination; }
    public String getTravelWindow() { return travelWindow; }
    public String getForecast() { return forecast; }
}
