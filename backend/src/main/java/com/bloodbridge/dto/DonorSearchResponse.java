package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object representing a search result for matching donors.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorSearchResponse {

    private String fullName;
    private String email;
    private String phoneNumber;
    private BloodGroup bloodGroup;
    private String city;
    private String state;
    private Boolean availableForDonation;
    private Boolean eligible;
    private LocalDate nextEligibleDate;
}
