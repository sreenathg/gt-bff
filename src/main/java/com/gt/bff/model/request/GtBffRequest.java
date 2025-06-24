package com.gt.bff.model.request;

public class GtBffRequest {
    private String contextId;
    private String destination;
    private String travelWindow;

    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTravelWindow() { return travelWindow; }
    public void setTravelWindow(String travelWindow) { this.travelWindow = travelWindow; }
}
