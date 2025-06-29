package com.gt.bff.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ResponseHelperTest {

    @Test
    void executeServiceOperation_Success_ShouldReturnOkResponse() {
        String expectedResult = "Success";
        
        ResponseEntity<String> response = ResponseHelper.executeServiceOperation(
            () -> expectedResult,
            "testOperation"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    void executeServiceOperation_WithException_ShouldThrowResponseStatusException() {
        RuntimeException testException = new RuntimeException("Test error");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ResponseHelper.executeServiceOperation(
                () -> {
                    throw testException;
                },
                "testOperation"
            );
        });
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("An error occurred while processing your request", exception.getReason());
        assertEquals(testException, exception.getCause());
    }

    @Test
    void executeServiceOperation_WithContext_Success_ShouldReturnOkResponse() {
        Integer expectedResult = 42;
        String context = "userId: 123";
        
        ResponseEntity<Integer> response = ResponseHelper.executeServiceOperation(
            () -> expectedResult,
            "getUserData",
            context
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    void executeServiceOperation_WithContext_WithException_ShouldThrowResponseStatusException() {
        RuntimeException testException = new RuntimeException("Database error");
        String context = "searchInput: test query";
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ResponseHelper.executeServiceOperation(
                () -> {
                    throw testException;
                },
                "searchOperation",
                context
            );
        });
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("An error occurred while processing your request", exception.getReason());
        assertEquals(testException, exception.getCause());
    }

    @Test
    void executeServiceOperation_WithNullResult_ShouldReturnOkResponseWithNull() {
        ResponseEntity<Object> response = ResponseHelper.executeServiceOperation(
            () -> null,
            "nullOperation"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void executeServiceOperation_WithContext_WithNullResult_ShouldReturnOkResponseWithNull() {
        ResponseEntity<Object> response = ResponseHelper.executeServiceOperation(
            () -> null,
            "nullOperation",
            "context: empty"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void executeServiceOperation_WithComplexObject_ShouldReturnCorrectResponse() {
        TestData expectedData = new TestData("test", 123);
        
        ResponseEntity<TestData> response = ResponseHelper.executeServiceOperation(
            () -> expectedData,
            "complexOperation"
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedData, response.getBody());
        assertEquals("test", response.getBody().getName());
        assertEquals(123, response.getBody().getValue());
    }

    @Test
    void executeServiceOperation_WithRuntimeException_ShouldPreserveCause() {
        IllegalArgumentException cause = new IllegalArgumentException("Invalid argument");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ResponseHelper.executeServiceOperation(
                () -> {
                    throw cause;
                },
                "validationOperation"
            );
        });
        
        assertEquals(cause, exception.getCause());
    }

    @Test
    void executeServiceOperation_WithCheckedExceptionWrapper_ShouldPreserveCause() {
        RuntimeException wrappedException = new RuntimeException("Wrapped exception");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            ResponseHelper.executeServiceOperation(
                () -> {
                    throw wrappedException;
                },
                "wrappedOperation"
            );
        });
        
        assertEquals(wrappedException, exception.getCause());
    }

    // Helper class for testing complex objects
    private static class TestData {
        private final String name;
        private final int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestData testData = (TestData) obj;
            return value == testData.value && name.equals(testData.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + value;
        }
    }
}