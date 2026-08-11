package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for registering/creating a Hospital profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create Hospital Profile Request Payload")
public class CreateHospitalRequest {

    @NotBlank(message = "Hospital name is required")
    @Schema(description = "Full hospital or blood bank name", example = "Boston General Hospital")
    private String hospitalName;

    @NotBlank(message = "Registration number is required")
    @Schema(description = "Government registration/license ID", example = "MA-HOSP-99821")
    private String registrationNumber;

    @Schema(description = "Medical license number", example = "LIC-2026-881")
    private String licenseNumber;

    @Schema(description = "Hospital type (GENERAL, SPECIALTY, BLOOD_BANK, TRAUMA_CENTER)", example = "GENERAL")
    private String hospitalType;

    @Schema(description = "Primary contact person name", example = "Dr. Robert Vance")
    private String contactPerson;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Hospital emergency phone number", example = "+16175550199")
    private String phoneNumber;

    @Schema(description = "Hospital emergency phone number", example = "+16175550199")
    private String contactPhone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Official hospital contact email", example = "info@bostongeneral.org")
    private String email;

    @Schema(description = "Official hospital contact email", example = "info@bostongeneral.org")
    private String contactEmail;

    @Schema(description = "Hospital website URL", example = "https://www.bostongeneral.org")
    private String website;

    @NotBlank(message = "Address is required")
    @Schema(description = "Street address", example = "75 Francis Street")
    private String address;

    @NotBlank(message = "City is required")
    @Schema(description = "City", example = "Boston")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(description = "State", example = "MA")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Postal Code", example = "02115")
    private String postalCode;

    @Schema(description = "Latitude coordinate", example = "42.3359")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "-71.1070")
    private Double longitude;

    @Schema(description = "Operating hours string", example = "24/7")
    private String operatingHours;

    @Schema(description = "Available for emergency blood requests", example = "true")
    private Boolean emergencyAvailable;
}
