package com.bloodbridge.strategy;

import com.bloodbridge.entity.DonorProfile;
import org.springframework.stereotype.Component;

/**
 * Strategy component for calculating availability score based on regular and emergency availability flags.
 */
@Component
public class AvailabilityStrategy {

    public double calculateAvailabilityScore(DonorProfile donor) {
        if (donor == null) {
            return 0.0;
        }

        double score = 0.0;
        if (Boolean.TRUE.equals(donor.getAvailableForDonation())) {
            score += 60.0;
        }
        if (Boolean.TRUE.equals(donor.getEmergencyAvailable())) {
            score += 40.0;
        }

        return score;
    }
}
