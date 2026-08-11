package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for updating Hospital profile details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update Hospital Profile Request Payload")
public class UpdateHospitalRequest {

    @Schema(description = "Hospital name", example = "Boston General Hospital & Trauma Center")
    private String hospitalName;

    @Schema(description = "Contact person name", example = "Dr. Sarah Connor")
    private String contactPerson;

    @Schema(description = "Phone number", example = "+16175550199")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Schema(description = "Official email", example = "contact@bostongeneral.org")
    private String email;

    @Schema(description = "Website URL", example = "https://www.bostongeneral.org")
    private String website;

    @Schema(description = "Address", example = "75 Francis Street")
    private String address;

    @Schema(description = "City", example = "Boston")
    private String city;

    @Schema(description = "State", example = "MA")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Postal code", example = "02115")
    private String postalCode;

    @Schema(description = "Latitude", example = "42.3359")
    private Double latitude;

    @Schema(description = "Longitude", example = "-71.1070")
    private Double longitude;

    @Schema(description = "Operating hours", example = "24/7")
    private String operatingHours;

    @Schema(description = "Emergency availability", example = "true")
    private Boolean emergencyAvailable;
}
