package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the detailed response of a patient's profile.
 * Contains user context, patient metrics, emergency contact details, and audit timestamps.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Integer age;
    private Gender gender;
    private BloodGroup bloodGroup;
    private String address;
    private String city;
    private String state;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String medicalHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
