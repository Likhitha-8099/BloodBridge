package com.bloodbridge.service.impl;

import com.bloodbridge.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for geographic distance calculation utilizing the Haversine formula.
 */
@Service
@Slf4j
public class LocationServiceImpl implements LocationService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            log.warn("[LOCATION-SERVICE] Missing coordinates for distance calculation (lat1={}, lon1={}, lat2={}, lon2={})",
                    lat1, lon1, lat2, lon2);
            return Double.MAX_VALUE;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(radLat1) * Math.cos(radLat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_KM * c;
        return Math.round(distance * 100.0) / 100.0;
    }

    @Override
    public boolean isWithinRadius(Double lat1, Double lon1, Double lat2, Double lon2, double radiusKm) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return false;
        }
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radiusKm;
    }
}
