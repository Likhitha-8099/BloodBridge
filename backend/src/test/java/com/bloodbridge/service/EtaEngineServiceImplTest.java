package com.bloodbridge.service;

import com.bloodbridge.dto.response.EtaResultDTO;
import com.bloodbridge.service.impl.EtaEngineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EtaEngineServiceImplTest {

    private LocationService locationService;
    private EtaEngineService etaEngineService;

    @BeforeEach
    void setUp() {
        locationService = mock(LocationService.class);
        etaEngineService = new EtaEngineServiceImpl(locationService);
    }

    @Test
    void calculateEta_CalculatesEtaAndGoogleMapsUrl() {
        when(locationService.calculateDistance(12.9716, 77.5946, 12.9698, 77.7499)).thenReturn(10.0);

        EtaResultDTO result = etaEngineService.calculateEta(12.9716, 77.5946, 12.9698, 77.7499);

        assertNotNull(result);
        assertEquals(10.0, result.getTravelDistanceKm());
        assertTrue(result.getEtaMinutes() >= 5);
        assertTrue(result.getGoogleMapsUrl().contains("google.com/maps/dir/?api=1&destination=12.969800,77.749900"));
    }
}
