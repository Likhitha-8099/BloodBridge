package com.bloodbridge.service;

/**
 * Service interface for calculating geographic distances and radius-based location filtering
 * using the Haversine formula.
 */
public interface LocationService {

    /**
     * Calculates the great-circle distance between two geographic coordinates in kilometers
     * using the Haversine formula.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in kilometers
     */
    double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2);

    /**
     * Evaluates whether two coordinates are within a specified radius in kilometers.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @param radiusKm Maximum allowed distance radius in kilometers
     * @return true if distance <= radiusKm, false otherwise
     */
    boolean isWithinRadius(Double lat1, Double lon1, Double lat2, Double lon2, double radiusKm);
}
