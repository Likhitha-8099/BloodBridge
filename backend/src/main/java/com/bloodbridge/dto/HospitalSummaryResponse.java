package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing a summary of a hospital profile.
 * Typically returned during searches and list lookups.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalSummaryResponse {

    private Long id;
    private String hospitalName;
    private String registrationNumber;
    private String email;
    private String phoneNumber;
    private String city;
    private String state;
    private Boolean verified;
}
