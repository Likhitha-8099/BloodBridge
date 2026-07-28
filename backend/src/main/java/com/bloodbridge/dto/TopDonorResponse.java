package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing top donor projection details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopDonorResponse {
    private String donorName;
    private BloodGroup bloodGroup;
    private Integer totalDonations;
}
