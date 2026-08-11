package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Top-level response DTO representing the complete 10-Stage Smart Donor Matching Pipeline execution result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartDonorMatchingPipelineDTO {

    private Long bloodRequestId;
    private Long hospitalId;
    private BloodGroup bloodGroupNeeded;
    private Integer unitsRequired;

    private DonorTierGroupDTO groupA; // Immediate Matches (0 - 50 KM)
    private DonorTierGroupDTO groupB; // Nearby Compatible (50 - 75 KM)
    private DonorTierGroupDTO groupC; // Extended Compatible (75 - 100 KM)
    private DonorTierGroupDTO groupD; // Emergency Broadcast (> 100 KM)

    private long totalCompatibleDonors;
    private long totalEligibleDonors;
    private long totalFilteredDonors;
    private long matchingDurationMs;

    private LocalDateTime timestamp;
}
