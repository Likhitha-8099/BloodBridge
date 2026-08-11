package com.bloodbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO carrying real-time statistics and accepted donor records for hospital dashboard updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalEmergencyLiveStatsDTO {

    private Long emergencyRequestId;
    private String hospitalName;
    private String bloodGroupNeeded;
    private Integer unitsRequired;
    private Integer remainingUnitsNeeded;
    private String requestStatus;

    private Long totalEligibleDonors;
    private Long matchedDonors;
    private Long emailsSent;
    private Long emailsDelivered;
    private Long emailsFailed;
    private Long pendingCount;
    private Long acceptedCount;
    private Long rejectedCount;
    private Long travellingCount;
    private Long reachedHospitalCount;
    private Integer unitsCollected;

    private Double acceptanceRate;
    private Double responseRate;
    private Double averageEta;
    private Double averageResponseTimeSeconds;
    private Long totalExecutionTimeMs;

    // Phase 3D.1 Hospital Dashboard 4 Tier Telemetry Sections (Stage 7)
    private DonorTierGroupDTO immediateMatches;     // Group A (0 - 50 KM)
    private DonorTierGroupDTO nearbyCompatible;     // Group B (50 - 75 KM)
    private DonorTierGroupDTO extendedCompatible;   // Group C (75 - 100 KM)
    private DonorTierGroupDTO emergencyBroadcast;   // Group D (> 100 KM)

    private List<EmergencyResponseDTO> acceptedDonors;
}
