package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing blood compatibility search results.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityResponse {

    private BloodGroup requestedBloodGroup;
    private List<BloodGroup> compatibleDonors;
}
