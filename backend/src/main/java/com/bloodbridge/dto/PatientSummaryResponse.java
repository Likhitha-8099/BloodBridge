package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing a summary of a patient's profile.
 * Typically used in list and search responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientSummaryResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Integer age;
    private Gender gender;
    private BloodGroup bloodGroup;
    private String city;
    private String state;
    private String emergencyContactName;
    private String emergencyContactNumber;
}
