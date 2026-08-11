package com.bloodbridge.service;

import com.bloodbridge.service.impl.LocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocationServiceImpl Haversine distance calculations.
 */
class LocationServiceImplTest {

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl();
    }

    @Test
    void calculateDistance_SameLocation_ReturnsZero() {
        double dist = locationService.calculateDistance(12.9716, 77.5946, 12.9716, 77.5946);
        assertEquals(0.0, dist, 0.01);
    }

    @Test
    void calculateDistance_KnownCoordinates_ReturnsAccurateDistance() {
        // Bangalore (12.9716, 77.5946) to Whitefield Bangalore (12.9698, 77.7499) ~16.8 KM
        double dist = locationService.calculateDistance(12.9716, 77.5946, 12.9698, 77.7499);
        assertTrue(dist > 15.0 && dist < 19.0, "Expected distance ~16.8 KM, got " + dist);
    }

    @Test
    void isWithinRadius_Within50Km_ReturnsTrue() {
        // 16.8 KM is within 50 KM radius
        boolean within = locationService.isWithinRadius(12.9716, 77.5946, 12.9698, 77.7499, 50.0);
        assertTrue(within);
    }

    @Test
    void isWithinRadius_Exceeds50Km_ReturnsFalse() {
        // Bangalore to Mysore (~140 KM)
        boolean within = locationService.isWithinRadius(12.9716, 77.5946, 12.2958, 76.6394, 50.0);
        assertFalse(within);
    }

    @Test
    void calculateDistance_NullCoordinates_ReturnsMaxDistance() {
        double dist = locationService.calculateDistance(null, 77.5946, 12.9716, 77.5946);
        assertEquals(Double.MAX_VALUE, dist);
    }
}
