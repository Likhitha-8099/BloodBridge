package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing matching engine statistics for the admin dashboard.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingStatisticsResponse {
    private Long totalMatches;
    private Long acceptedMatches;
    private Long rejectedMatches;
    private Long activeMatches;
    private Double matchingSuccessRate;
}
