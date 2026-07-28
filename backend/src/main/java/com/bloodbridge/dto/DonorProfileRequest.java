package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for creating or updating a donor profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorProfileRequest {

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private LocalDate lastDonationDate;

    private String medicalConditions;

    @NotNull(message = "Weight is required")
    private Double weight;

    @Builder.Default
    private Integer totalDonations = 0;

    @Builder.Default
    private Boolean availableForDonation = true;
}
