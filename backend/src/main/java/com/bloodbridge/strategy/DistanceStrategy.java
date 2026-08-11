package com.bloodbridge.strategy;

import org.springframework.stereotype.Component;

/**
 * Strategy component for calculating Haversine distance and distance proximity scores.
 */
@Component
public class DistanceStrategy {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double DEFAULT_MAX_RADIUS_KM = 20.0;

    /**
     * Calculates geographical Haversine distance in KM between two coordinates.
     */
    public double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 5.0; // Default simulated distance
        }

        double latDiff = Math.toRadians(lat2 - lat1);
        double lonDiff = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((EARTH_RADIUS_KM * c) * 10.0) / 10.0;
    }

    /**
     * Estimates travel time in minutes based on distance (assuming avg speed 30 km/h in city traffic).
     */
    public int estimateTravelTimeMinutes(double distanceKm) {
        return (int) Math.ceil((distanceKm / 30.0) * 60.0);
    }

    /**
     * Calculates normalized distance proximity score (0.0 to 100.0).
     * Donors closer to 0 KM receive 100.0, linear drop-off to 0 at maxRadiusKm.
     */
    public double calculateDistanceScore(double distanceKm, Double maxRadiusKm) {
        double maxRadius = (maxRadiusKm != null && maxRadiusKm > 0) ? maxRadiusKm : DEFAULT_MAX_RADIUS_KM;
        if (distanceKm >= maxRadius) {
            return 0.0;
        }
        return Math.max(0.0, Math.round(((1.0 - (distanceKm / maxRadius)) * 100.0) * 10.0) / 10.0);
    }
}
