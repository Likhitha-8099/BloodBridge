package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object representing profile update request details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Profile Update Request Payload")
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    @Schema(description = "User's full legal name", example = "Jane Smith")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Contact phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Gender (MALE, FEMALE, OTHER)", example = "FEMALE")
    private String gender;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "1995-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Street address", example = "123 Healthcare Way")
    private String address;

    @Schema(description = "City", example = "Boston")
    private String city;

    @Schema(description = "State / Province", example = "MA")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Postal Code / Zip", example = "02115")
    private String postalCode;

    @Schema(description = "Geographic latitude coordinate", example = "42.3601")
    private Double latitude;

    @Schema(description = "Geographic longitude coordinate", example = "-71.0589")
    private Double longitude;
}
