package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the response for a donor's profile.
 * Contains core information, audit details, and computed eligibility indicators.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private BloodGroup bloodGroup;
    private Integer age;
    private Gender gender;
    private String city;
    private String state;
    private LocalDate lastDonationDate;
    private Boolean availableForDonation;
    private String medicalConditions;
    private Double weight;
    private Integer totalDonations;
    
    // Computed eligibility fields
    private Boolean eligible;
    private LocalDate nextEligibleDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
