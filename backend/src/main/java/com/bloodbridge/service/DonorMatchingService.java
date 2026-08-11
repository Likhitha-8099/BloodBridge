package com.bloodbridge.service;

import com.bloodbridge.dto.DonorMatchingResult;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;

/**
 * Service interface for smart donor matching and eligibility evaluation.
 */
public interface DonorMatchingService {

    /**
     * Evaluates all registered donors against the 8 production eligibility rules
     * for a given emergency blood request.
     *
     * @param request the emergency blood request
     * @return result object containing matched donors and evaluation telemetry
     */
    DonorMatchingResult evaluateEligibleDonors(BloodRequest request);

    /**
     * Helper to check eligibility of a single donor profile for a request.
     *
     * @param donor donor profile to evaluate
     * @param request emergency blood request
     * @return true if eligible under all 8 rules, false otherwise
     */
    boolean isDonorEligible(DonorProfile donor, BloodRequest request);
}
