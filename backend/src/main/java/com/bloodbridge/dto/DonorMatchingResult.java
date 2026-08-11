package com.bloodbridge.dto;

import com.bloodbridge.entity.DonorProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the results and telemetry of a smart donor matching evaluation run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorMatchingResult {

    private List<DonorProfile> matchedDonors;

    @Builder.Default
    private Map<Long, Double> donorDistances = new HashMap<>();

    private long totalEvaluatedCount;
    private long compatibleCount;
    private long withinRadiusCount;
}
