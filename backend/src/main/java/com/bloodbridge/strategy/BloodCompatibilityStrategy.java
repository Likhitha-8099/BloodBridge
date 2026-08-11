package com.bloodbridge.strategy;

import com.bloodbridge.enums.BloodGroup;
import org.springframework.stereotype.Component;

/**
 * Strategy component for evaluating ABO + Rh blood group compatibility.
 */
@Component
public class BloodCompatibilityStrategy {

    /**
     * Calculates compatibility score (0.0 to 100.0) between donor blood group and patient/needed blood group.
     *
     * @param donorBg donor blood group
     * @param neededBg required blood group
     * @return compatibility score (100.0 for exact/compatible match, 0.0 for incompatible)
     */
    public double calculateCompatibilityScore(BloodGroup donorBg, BloodGroup neededBg) {
        if (donorBg == null || neededBg == null) {
            return 0.0;
        }

        if (donorBg == neededBg) {
            return 100.0; // Exact match
        }

        if (isCompatible(donorBg, neededBg)) {
            return 90.0; // Compatible match (e.g. O- to A+)
        }

        return 0.0;
    }

    /**
     * Complete ABO + Rh compatibility matrix verification.
     */
    public boolean isCompatible(BloodGroup donorBg, BloodGroup neededBg) {
        if (donorBg == null || neededBg == null) return false;
        if (donorBg == neededBg) return true;

        // O Negative is Universal Donor
        if (donorBg == BloodGroup.O_NEGATIVE) return true;

        switch (neededBg) {
            case O_POSITIVE:
                return donorBg == BloodGroup.O_NEGATIVE;
            case A_POSITIVE:
                return donorBg == BloodGroup.A_NEGATIVE || donorBg == BloodGroup.O_POSITIVE || donorBg == BloodGroup.O_NEGATIVE;
            case A_NEGATIVE:
                return donorBg == BloodGroup.O_NEGATIVE;
            case B_POSITIVE:
                return donorBg == BloodGroup.B_NEGATIVE || donorBg == BloodGroup.O_POSITIVE || donorBg == BloodGroup.O_NEGATIVE;
            case B_NEGATIVE:
                return donorBg == BloodGroup.O_NEGATIVE;
            case AB_POSITIVE:
                // Universal Recipient: accepts any donor blood group
                return true;
            case AB_NEGATIVE:
                return donorBg == BloodGroup.A_NEGATIVE || donorBg == BloodGroup.B_NEGATIVE || donorBg == BloodGroup.O_NEGATIVE;
            default:
                return false;
        }
    }

    public String getCompatibilityReason(BloodGroup donorBg, BloodGroup neededBg) {
        if (donorBg == neededBg) {
            return "Exact blood group match (" + donorBg + ")";
        }
        if (isCompatible(donorBg, neededBg)) {
            return "Clinically compatible match (" + donorBg + " -> " + neededBg + ")";
        }
        return "Incompatible blood group (" + donorBg + " cannot donate to " + neededBg + ")";
    }
}
