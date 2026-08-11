package com.bloodbridge.strategy;

import com.bloodbridge.entity.DonorProfile;
import org.springframework.stereotype.Component;

/**
 * Strategy component for computing multi-weighted overall match scores.
 */
@Component
public class DonorScoreStrategy {

    // Configurable weights (sum to 1.0 / 100%)
    private static final double COMPATIBILITY_WEIGHT = 0.40; // 40%
    private static final double DISTANCE_WEIGHT = 0.25;      // 25%
    private static final double AVAILABILITY_WEIGHT = 0.15;  // 15%
    private static final double DONATION_HISTORY_WEIGHT = 0.10; // 10%
    private static final double PROFILE_COMPLETION_WEIGHT = 0.05; // 5%
    private static final double EMERGENCY_AVAILABILITY_WEIGHT = 0.05; // 5%

    public double calculateOverallMatchScore(
            double compatibilityScore,
            double distanceScore,
            double availabilityScore,
            DonorProfile donor
    ) {
        double historyScore = Math.min(100.0, (donor.getTotalDonations() != null ? donor.getTotalDonations() : 0) * 20.0);
        double completionScore = (donor.getDonorScore() != null) ? Math.min(100.0, donor.getDonorScore()) : 80.0;
        double emergencyScore = Boolean.TRUE.equals(donor.getEmergencyAvailable()) ? 100.0 : 0.0;

        double overallScore = (compatibilityScore * COMPATIBILITY_WEIGHT)
                + (distanceScore * DISTANCE_WEIGHT)
                + (availabilityScore * AVAILABILITY_WEIGHT)
                + (historyScore * DONATION_HISTORY_WEIGHT)
                + (completionScore * PROFILE_COMPLETION_WEIGHT)
                + (emergencyScore * EMERGENCY_AVAILABILITY_WEIGHT);

        return Math.round(overallScore * 10.0) / 10.0;
    }
}
