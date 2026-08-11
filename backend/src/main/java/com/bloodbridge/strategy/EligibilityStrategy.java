package com.bloodbridge.strategy;

import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.enums.EligibilityStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Strategy component for evaluating donor clinical eligibility.
 */
@Component
public class EligibilityStrategy {

    public EligibilityStatus evaluateEligibility(DonorProfile donor) {
        if (donor == null) {
            return EligibilityStatus.PERMANENTLY_DEFERRED;
        }

        // Deactivated status
        if ("DEACTIVATED".equalsIgnoreCase(donor.getStatus())) {
            return EligibilityStatus.PERMANENTLY_DEFERRED;
        }

        // Medical condition checks
        if (donor.getMedicalConditions() != null) {
            String cond = donor.getMedicalConditions().toLowerCase();
            if (cond.contains("hiv") || cond.contains("hepatitis") || cond.contains("cancer") || cond.contains("heart disease")) {
                return EligibilityStatus.PERMANENTLY_DEFERRED;
            }
        }

        // Age & Weight checks
        if (donor.getAge() == null || donor.getAge() < 18 || donor.getAge() > 65) {
            return EligibilityStatus.TEMPORARILY_DEFERRED;
        }

        if (donor.getWeight() == null || donor.getWeight() < 50.0) {
            return EligibilityStatus.TEMPORARILY_DEFERRED;
        }

        // 56-day interval check
        if (donor.getLastDonationDate() != null) {
            LocalDate eligibleDate = donor.getLastDonationDate().plusDays(56);
            if (LocalDate.now().isBefore(eligibleDate)) {
                return EligibilityStatus.TEMPORARILY_DEFERRED;
            }
        }

        return EligibilityStatus.ELIGIBLE;
    }
}
