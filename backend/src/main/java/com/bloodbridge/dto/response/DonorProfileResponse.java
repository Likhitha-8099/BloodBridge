package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EligibilityStatus;
import com.bloodbridge.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing complete Donor Profile response details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Donor Profile Response Payload")
public class DonorProfileResponse {

    @Schema(description = "Donor profile ID", example = "1")
    private Long id;

    @Schema(description = "Associated user ID", example = "2")
    private Long userId;

    @Schema(description = "User full name", example = "Jane Doe")
    private String fullName;

    @Schema(description = "User email", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "User phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Blood group", example = "O_POSITIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Rh Factor", example = "POSITIVE")
    private String rhFactor;

    @Schema(description = "Age", example = "25")
    private Integer age;

    @Schema(description = "Gender", example = "FEMALE")
    private Gender gender;

    @Schema(description = "Date of birth", example = "1999-04-12")
    private LocalDate dateOfBirth;

    @Schema(description = "City", example = "Boston")
    private String city;

    @Schema(description = "State", example = "MA")
    private String state;

    @Schema(description = "Weight in KG", example = "68.5")
    private Double weight;

    @Schema(description = "Height in CM", example = "175.0")
    private Double height;

    @Schema(description = "Last donation date", example = "2026-05-01")
    private LocalDate lastDonationDate;

    @Schema(description = "Calculated next eligible donation date", example = "2026-06-26")
    private LocalDate nextEligibleDate;

    @Schema(description = "Availability for regular donation", example = "true")
    private Boolean availableForDonation;

    @Schema(description = "Availability for emergency blood calls", example = "true")
    private Boolean emergencyAvailable;

    @Schema(description = "Preferred donation radius in KM", example = "25.0")
    private Double preferredDonationRadius;

    @Schema(description = "Latitude coordinate", example = "42.3601")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "-71.0589")
    private Double longitude;

    @Schema(description = "Medical conditions", example = "None")
    private String medicalConditions;

    @Schema(description = "Current medications", example = "None")
    private String currentMedications;

    // --- NEW HEALTHCARE, LIFESTYLE & PREFERENCE RESPONSE FIELDS ---
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

    @Schema(description = "Calculated profile completion percentage", example = "95")
    private Integer profileCompletionPercentage;

    @Schema(description = "List of missing profile fields to complete")
    private java.util.List<String> missingFields;

    @Schema(description = "Total completed donations count", example = "5")
    private Integer totalDonations;

    @Schema(description = "Estimated lives saved", example = "15")
    private Integer livesSaved;

    @Schema(description = "Smart donor engagement score", example = "120")
    private Integer donorScore;

    @Schema(description = "Current eligibility status", example = "ELIGIBLE")
    private EligibilityStatus eligibilityStatus;

    @Schema(description = "Boolean flag indicating if donor is eligible to donate today", example = "true")
    private Boolean eligible;

    @Schema(description = "Days remaining until eligible to donate", example = "0")
    private Long daysUntilEligible;

    @Schema(description = "Configured donation cooldown period in days", example = "90")
    private Integer cooldownDays;

    @Schema(description = "Verification status", example = "VERIFIED")
    private String verificationStatus;

    @Schema(description = "Profile status", example = "ACTIVE")
    private String status;

    @Schema(description = "Created timestamp", example = "2026-08-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime updatedAt;
}
