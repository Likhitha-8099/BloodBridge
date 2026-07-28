package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing an eligible donor search result with a compatibility score.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorMatchResponse {

    private Long donorId;
    private String donorName;
    private BloodGroup bloodGroup;
    private Integer age;
    private Gender gender;
    private String city;
    private String state;
    private Double weight;
    private Integer totalDonations;
    private Integer compatibilityScore;
}
