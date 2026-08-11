package com.bloodbridge.dto.request;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for creating a new Donor Profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create Donor Profile Request Payload")
public class CreateDonorProfileRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    @Schema(description = "Donor email address", example = "donor@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotNull(message = "Blood group is required")
    @Schema(description = "Donor blood group", example = "O_POSITIVE")
    private BloodGroup bloodGroup;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 65, message = "Age cannot exceed 65")
    @Schema(description = "Donor age", example = "25")
    private Integer age;

    @NotNull(message = "Gender is required")
    @Schema(description = "Gender", example = "MALE")
    private Gender gender;

    @NotBlank(message = "City is required")
    @Schema(description = "Current city", example = "Boston")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(description = "Current state", example = "MA")
    private String state;

    @NotNull(message = "Weight is required")
    @Min(value = 50, message = "Weight must be at least 50 kg")
    @Schema(description = "Weight in Kilograms", example = "68.5")
    private Double weight;

    @Schema(description = "Height in Centimeters", example = "175.0")
    private Double height;

    @Schema(description = "Date of Birth", example = "1999-04-12")
    private LocalDate dateOfBirth;

    @Schema(description = "Existing medical conditions if any", example = "None")
    private String medicalConditions;

    @Schema(description = "Current active medications if any", example = "None")
    private String currentMedications;

    @Schema(description = "Preferred donation radius in KM", example = "25.0")
    private Double preferredDonationRadius;

    @Schema(description = "Latitude coordinate", example = "42.3601")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "-71.0589")
    private Double longitude;

    @Schema(description = "Last blood donation date", example = "2026-05-01")
    private LocalDate lastDonationDate;

    @Schema(description = "Available for donation flag", example = "true")
    private Boolean availableForDonation;

    @Schema(description = "Available for emergency blood calls", example = "true")
    private Boolean emergencyAvailable;
}
