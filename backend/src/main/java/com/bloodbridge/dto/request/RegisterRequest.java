package com.bloodbridge.dto.request;

import com.bloodbridge.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a user registration request payload.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
@Schema(description = "User Registration Request Payload")
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    @Schema(description = "User's full legal name", example = "John Doe")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    @Schema(description = "Unique user email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
        message = "Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)"
    )
    @Schema(description = "Account password matching security rules", example = "BloodBridge@2026")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Contact phone number", example = "+1234567890")
    private String phoneNumber;

    @NotNull(message = "Role is required")
    @Schema(description = "User role (DONOR, PATIENT, HOSPITAL, ADMIN)", example = "DONOR")
    private Role role;

    // --- Optional Hospital Profile Onboarding Fields ---
    private String hospitalName;
    private String hospitalType;
    private String registrationNumber;
    private String emergencyPhoneNumber;
    private String website;

    // --- Optional Donor Profile & Healthcare Onboarding Fields ---
    private com.bloodbridge.enums.BloodGroup bloodGroup;
    private com.bloodbridge.enums.Gender gender;
    private Integer age;
    private LocalDate dateOfBirth;
    private Double weight;
    private Double height;
    private Double bmi;

    private String country;
    private String state;
    private String district;
    private String city;
    private String postalCode;
    private String address;
    private String landmark;
    private Double latitude;
    private Double longitude;

    private String alternatePhoneNumber;
    private String aadhaarNumber;
    private String govtIdType;
    private String govtIdNumber;
    private String occupation;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String emergencyContactRelationship;

    private Double hemoglobin;
    private String bloodPressure;
    private Integer pulseRate;
    private String medicalConditions;
    private String currentMedications;
    private String allergies;
    private String covidHistory;
    private String travelHistory;

    private Boolean smoking;
    private Boolean alcohol;
    private Boolean drugUsage;
    private Boolean pregnancy;
    private Boolean breastfeeding;
    private Boolean recentSurgery;
    private Boolean recentTattoo;
    private Boolean recentVaccination;
    private Boolean recentFever;

    private Boolean emergencyAvailable;
    private Double preferredDonationRadius;
    private String preferredHospitals;
    private String preferredContactMethod;
    private String availableDays;
    private String availableTimeSlots;
    private Boolean willingDonatePlatelets;
    private Boolean willingDonatePlasma;
    private Boolean rareBloodDonor;
    private Boolean pushNotificationEnabled;
}
