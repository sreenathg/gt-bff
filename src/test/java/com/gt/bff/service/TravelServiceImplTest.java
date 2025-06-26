package com.gt.bff.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TravelServiceImplTest {

    @Test
    void travelService_ShouldBeInstantiable() {
        // Given & When
        TravelServiceImpl travelService = new TravelServiceImpl();
        
        // Then
        assertThat(travelService).isNotNull();
    }
}