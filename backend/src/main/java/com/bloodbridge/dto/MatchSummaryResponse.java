package com.bloodbridge.dto;

import com.bloodbridge.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing a summary of a match result.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSummaryResponse {

    private Long id;
    private Long requestId;
    private Long donorId;
    private String donorName;
    private Integer compatibilityScore;
    private MatchStatus status;
}
