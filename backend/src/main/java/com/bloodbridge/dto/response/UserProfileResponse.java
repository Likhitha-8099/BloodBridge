package com.bloodbridge.dto.response;

import com.bloodbridge.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing detailed user profile information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Profile Detailed Response Payload")
public class UserProfileResponse {

    @Schema(description = "User unique ID", example = "1")
    private Long id;

    @Schema(description = "Full name", example = "Jane Smith")
    private String fullName;

    @Schema(description = "Email address", example = "jane.smith@example.com")
    private String email;

    @Schema(description = "Contact phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Primary user role", example = "DONOR")
    private Role role;

    @Schema(description = "Profile image URL", example = "https://images.bloodbridge.com/profiles/user_1.jpg")
    private String profileImage;

    @Schema(description = "Gender", example = "FEMALE")
    private String gender;

    @Schema(description = "Date of Birth", example = "1995-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Street Address", example = "123 Healthcare Way")
    private String address;

    @Schema(description = "City", example = "Boston")
    private String city;

    @Schema(description = "State", example = "MA")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Postal Code", example = "02115")
    private String postalCode;

    @Schema(description = "Geographic latitude", example = "42.3601")
    private Double latitude;

    @Schema(description = "Geographic longitude", example = "-71.0589")
    private Double longitude;

    @Schema(description = "Account active status", example = "true")
    private Boolean active;

    @Schema(description = "Email verification status", example = "false")
    private Boolean emailVerified;

    @Schema(description = "Account registration timestamp", example = "2026-08-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Account last update timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime updatedAt;
}
