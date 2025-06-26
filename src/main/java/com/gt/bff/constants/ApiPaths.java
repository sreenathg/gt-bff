package com.gt.bff.constants;

public final class ApiPaths {

    private ApiPaths() {
        // Private constructor to prevent instantiation
    }

    // API version
    public static final String API_V1 = "/api/v1";

    // Home controller paths
    public static final String ROOT = "/";
    public static final String HEALTH = "/health";
    public static final String INFO = "/info";
    public static final String DOCS = "/docs";

    // Build paths
    public static String buildPath(String... paths) {
        return String.join("", paths);
    }
}
