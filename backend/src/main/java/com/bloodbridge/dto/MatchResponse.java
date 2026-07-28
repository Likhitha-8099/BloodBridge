package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for representing a detailed match result.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponse {

    private Long id;
    private Long requestId;
    private Long donorId;
    private String donorName;
    private BloodGroup donorBloodGroup;
    private String donorCity;
    private Integer compatibilityScore;
    private LocalDateTime matchedAt;
    private MatchStatus status;
}
