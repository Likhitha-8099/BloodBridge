package com.bloodbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates telemetry and donor lists for a specific distance tier group (Stage 4 & Stage 7).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorTierGroupDTO {
    private String groupName; // e.g. "Group A"
    private String sectionTitle; // e.g. "Immediate Matches"
    private String distanceRange; // e.g. "0 - 50 KM"
    private long donorCount;
    private double averageDistanceKm;
    private double estimatedResponseProbability;
    private long acceptedCount;
    private long pendingCount;
    private String notificationSubjectTemplate;
    private String notificationMessageTemplate;

    @Builder.Default
    private List<DonorMatchSummaryDTO> donors = new ArrayList<>();
}
