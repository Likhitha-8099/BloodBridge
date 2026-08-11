package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing detailed Patient Profile information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Patient Profile Response Payload")
public class PatientProfileResponse {

    @Schema(description = "Patient profile ID", example = "1")
    private Long id;

    @Schema(description = "Associated user ID", example = "4")
    private Long userId;

    @Schema(description = "User full name", example = "Jane Doe")
    private String fullName;

    @Schema(description = "User email", example = "patient@bloodbridge.com")
    private String email;

    @Schema(description = "User phone number", example = "+16175550177")
    private String phoneNumber;

    @Schema(description = "Patient tracking code", example = "PAT-2026-104")
    private String patientCode;

    @Schema(description = "Age", example = "34")
    private Integer age;

    @Schema(description = "Gender", example = "FEMALE")
    private Gender gender;

    @Schema(description = "Blood group", example = "A_POSITIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Rh factor", example = "POSITIVE")
    private String rhFactor;

    @Schema(description = "Weight in kg", example = "62.0")
    private Double weight;

    @Schema(description = "Current medical condition", example = "Severe Anemia")
    private String medicalCondition;

    @Schema(description = "Diagnosis", example = "Acute Blood Loss Anemia")
    private String diagnosis;

    @Schema(description = "Doctor name", example = "Dr. Michael Chen")
    private String doctorName;

    @Schema(description = "Assigned hospital ID", example = "1")
    private Long hospitalId;

    @Schema(description = "Assigned hospital name", example = "Boston General Hospital")
    private String hospitalName;

    @Schema(description = "Emergency contact name", example = "David Smith")
    private String emergencyContactName;

    @Schema(description = "Emergency contact phone number", example = "+16175550188")
    private String emergencyContactNumber;

    @Schema(description = "Relationship to emergency contact", example = "SPOUSE")
    private String relationship;

    @Schema(description = "Address", example = "120 Beacon Street")
    private String address;

    @Schema(description = "City", example = "Boston")
    private String city;

    @Schema(description = "State", example = "MA")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Postal Code", example = "02116")
    private String postalCode;

    @Schema(description = "Latitude", example = "42.3551")
    private Double latitude;

    @Schema(description = "Longitude", example = "-71.0700")
    private Double longitude;

    @Schema(description = "Preferred hospital name", example = "Boston General Hospital")
    private String preferredHospital;

    @Schema(description = "Medical history summary", example = "No known drug allergies.")
    private String medicalHistory;

    @Schema(description = "Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Created timestamp", example = "2026-08-01T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime updatedAt;
}
