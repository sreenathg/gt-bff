package com.gt.bff.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsTest {

    @Test
    void apiPaths_ShouldHaveCorrectConstants() {
        assertEquals("/api/v1", ApiPaths.API_V1);
        assertEquals("/", ApiPaths.ROOT);
        assertEquals("/health", ApiPaths.HEALTH);
        assertEquals("/info", ApiPaths.INFO);
        assertEquals("/docs", ApiPaths.DOCS);
    }

    @Test
    void apiPaths_BuildPath_ShouldConcatenatePaths() {
        String result = ApiPaths.buildPath(ApiPaths.API_V1, ApiPaths.HEALTH);
        assertEquals("/api/v1/health", result);
        
        String complexPath = ApiPaths.buildPath(ApiPaths.API_V1, "/search", "/filters");
        assertEquals("/api/v1/search/filters", complexPath);
    }

    @Test
    void apiPaths_BuildPath_WithEmptyArray_ShouldReturnEmpty() {
        String result = ApiPaths.buildPath();
        assertEquals("", result);
    }

    @Test
    void apiPaths_BuildPath_WithSinglePath_ShouldReturnSamePath() {
        String result = ApiPaths.buildPath("/single");
        assertEquals("/single", result);
    }

    @Test
    void apiPaths_BuildPath_WithNullPath_ShouldHandleNull() {
        String result = ApiPaths.buildPath(ApiPaths.API_V1, null, ApiPaths.HEALTH);
        assertEquals("/api/v1null/health", result);
    }

    @Test
    void responseStatus_ShouldHaveCorrectConstants() {
        assertEquals("SUCCESS", ResponseStatus.SUCCESS);
        assertEquals("ERROR", ResponseStatus.ERROR);
        assertEquals("PARTIAL", ResponseStatus.PARTIAL);
        assertEquals("PENDING", ResponseStatus.PENDING);
    }

    @Test
    void travelClass_ShouldHaveCorrectConstants() {
        assertEquals("ECONOMY", TravelClass.ECONOMY);
        assertEquals("PREMIUM_ECONOMY", TravelClass.PREMIUM_ECONOMY);
        assertEquals("BUSINESS", TravelClass.BUSINESS);
        assertEquals("FIRST", TravelClass.FIRST);
    }

    @Test
    void apiPaths_ShouldHavePrivateConstructor() throws Exception {
        Constructor<ApiPaths> constructor = ApiPaths.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        // Constructor should be accessible but doesn't need to throw exception
        constructor.setAccessible(true);
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    void responseStatus_ShouldHavePrivateConstructor() throws Exception {
        Constructor<ResponseStatus> constructor = ResponseStatus.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        constructor.setAccessible(true);
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    void travelClass_ShouldHavePrivateConstructor() throws Exception {
        Constructor<TravelClass> constructor = TravelClass.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        constructor.setAccessible(true);
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    void constantClasses_ShouldBeFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(ApiPaths.class.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isFinal(ResponseStatus.class.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isFinal(TravelClass.class.getModifiers()));
    }

    @Test
    void constants_ShouldBePublicStaticFinal() throws Exception {
        // Test ApiPaths constants
        assertTrue(java.lang.reflect.Modifier.isPublic(ApiPaths.class.getField("API_V1").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(ApiPaths.class.getField("API_V1").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isFinal(ApiPaths.class.getField("API_V1").getModifiers()));
        
        // Test ResponseStatus constants
        assertTrue(java.lang.reflect.Modifier.isPublic(ResponseStatus.class.getField("SUCCESS").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(ResponseStatus.class.getField("SUCCESS").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isFinal(ResponseStatus.class.getField("SUCCESS").getModifiers()));
        
        // Test TravelClass constants
        assertTrue(java.lang.reflect.Modifier.isPublic(TravelClass.class.getField("ECONOMY").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(TravelClass.class.getField("ECONOMY").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isFinal(TravelClass.class.getField("ECONOMY").getModifiers()));
    }

    @Test
    void responseStatus_AllConstants_ShouldBeUnique() {
        String[] statuses = {
            ResponseStatus.SUCCESS,
            ResponseStatus.ERROR,
            ResponseStatus.PARTIAL,
            ResponseStatus.PENDING
        };
        
        for (int i = 0; i < statuses.length; i++) {
            for (int j = i + 1; j < statuses.length; j++) {
                assertNotEquals(statuses[i], statuses[j], 
                    "Constants should be unique: " + statuses[i] + " vs " + statuses[j]);
            }
        }
    }

    @Test
    void travelClass_AllConstants_ShouldBeUnique() {
        String[] classes = {
            TravelClass.ECONOMY,
            TravelClass.PREMIUM_ECONOMY,
            TravelClass.BUSINESS,
            TravelClass.FIRST
        };
        
        for (int i = 0; i < classes.length; i++) {
            for (int j = i + 1; j < classes.length; j++) {
                assertNotEquals(classes[i], classes[j], 
                    "Constants should be unique: " + classes[i] + " vs " + classes[j]);
            }
        }
    }
}