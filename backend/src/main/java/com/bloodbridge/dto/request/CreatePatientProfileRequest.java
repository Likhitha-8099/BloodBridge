package com.bloodbridge.dto.request;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for creating a Patient Profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create Patient Profile Request Payload")
public class CreatePatientProfileRequest {

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 120, message = "Age cannot exceed 120")
    @Schema(description = "Patient age in years", example = "34")
    private Integer age;

    @NotNull(message = "Gender is required")
    @Schema(description = "Gender enum", example = "FEMALE")
    private Gender gender;

    @NotNull(message = "Blood group is required")
    @Schema(description = "Blood group enum", example = "A_POSITIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Rh factor", example = "POSITIVE")
    private String rhFactor;

    @Schema(description = "Weight in kg", example = "62.0")
    private Double weight;

    @Schema(description = "Current medical condition", example = "Severe Anemia")
    private String medicalCondition;

    @Schema(description = "Clinical diagnosis", example = "Acute Blood Loss Anemia")
    private String diagnosis;

    @Schema(description = "Attending doctor name", example = "Dr. Michael Chen")
    private String doctorName;

    @Schema(description = "Assigned hospital ID", example = "1")
    private Long hospitalId;

    @NotBlank(message = "Emergency contact name is required")
    @Schema(description = "Emergency contact full name", example = "David Smith")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact number is required")
    @Schema(description = "Emergency contact phone number", example = "+16175550188")
    private String emergencyContactNumber;

    @Schema(description = "Relationship to emergency contact", example = "SPOUSE")
    private String relationship;

    @Schema(description = "Street address", example = "120 Beacon Street")
    private String address;

    @NotBlank(message = "City is required")
    @Schema(description = "City", example = "Boston")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(description = "State", example = "MA")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Postal code", example = "02116")
    private String postalCode;

    @Schema(description = "Latitude coordinate", example = "42.3551")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "-71.0700")
    private Double longitude;

    @Schema(description = "Preferred hospital name", example = "Boston General Hospital")
    private String preferredHospital;

    @Schema(description = "Detailed medical history summary", example = "No known drug allergies. Previous minor surgery in 2022.")
    private String medicalHistory;
}
