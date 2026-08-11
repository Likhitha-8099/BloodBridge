package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for updating an existing Donor Profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update Donor Profile Request Payload")
public class UpdateDonorProfileRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    @Schema(description = "Donor email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    private String fullName;
    private String phoneNumber;
    private com.bloodbridge.enums.BloodGroup bloodGroup;

    @Schema(description = "City", example = "Cambridge")
    private String city;

    @Schema(description = "State", example = "MA")
    private String state;

    @Min(value = 50, message = "Weight must be at least 50 kg")
    @Schema(description = "Weight in Kilograms", example = "70.0")
    private Double weight;

    @Schema(description = "Height in Centimeters", example = "175.0")
    private Double height;

    @Schema(description = "Medical conditions", example = "None")
    private String medicalConditions;

    @Schema(description = "Current medications", example = "None")
    private String currentMedications;

    @Schema(description = "Last recorded donation date", example = "2026-05-10")
    private LocalDate lastDonationDate;

    @Schema(description = "Preferred donation radius in KM", example = "30.0")
    private Double preferredDonationRadius;

    @Schema(description = "Latitude coordinate", example = "42.3736")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "-71.1097")
    private Double longitude;

    // --- NEW HEALTHCARE, LIFESTYLE & PREFERENCE FIELDS ---
    private String alternatePhoneNumber;
    private String aadhaarNumber;
    private String govtIdType;
    private String govtIdNumber;
    private String occupation;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String emergencyContactRelationship;
    private String country;
    private String district;
    private String postalCode;
    private String address;
    private String landmark;
    private Double bmi;
    private Double hemoglobin;
    private String bloodPressure;
    private Integer pulseRate;
    private Boolean smoking;
    private Boolean alcohol;
    private Boolean drugUsage;
    private Boolean pregnancy;
    private Boolean breastfeeding;
    private Boolean recentSurgery;
    private Boolean recentTattoo;
    private Boolean recentVaccination;
    private Boolean recentFever;
    private String allergies;
    private String covidHistory;
    private String travelHistory;
    private String preferredHospitals;
    private String preferredContactMethod;
    private String availableDays;
    private String availableTimeSlots;
    private Boolean willingDonatePlatelets;
    private Boolean willingDonatePlasma;
    private Boolean rareBloodDonor;
    private Boolean pushNotificationEnabled;
    private Boolean emergencyAvailable;
    private Boolean availableForDonation;
}
