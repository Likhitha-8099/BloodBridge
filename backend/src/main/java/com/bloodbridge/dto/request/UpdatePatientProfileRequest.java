package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for updating a Patient Profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update Patient Profile Request Payload")
public class UpdatePatientProfileRequest {

    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 120, message = "Age cannot exceed 120")
    @Schema(description = "Age in years", example = "35")
    private Integer age;

    @Schema(description = "Weight in kg", example = "64.0")
    private Double weight;

    @Schema(description = "Current medical condition", example = "Recovering Anemia")
    private String medicalCondition;

    @Schema(description = "Clinical diagnosis", example = "Post-op blood recovery")
    private String diagnosis;

    @Schema(description = "Doctor name", example = "Dr. Michael Chen")
    private String doctorName;

    @Schema(description = "Assigned hospital ID", example = "1")
    private Long hospitalId;

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

    @Schema(description = "Preferred hospital", example = "Boston General Hospital")
    private String preferredHospital;

    @Schema(description = "Medical history summary", example = "Updated medical history details")
    private String medicalHistory;
}
