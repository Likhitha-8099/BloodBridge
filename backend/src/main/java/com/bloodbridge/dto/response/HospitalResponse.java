package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing detailed Hospital response information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Hospital Response Payload")
public class HospitalResponse {

    @Schema(description = "Hospital ID", example = "1")
    private Long id;

    @Schema(description = "Associated user ID", example = "3")
    private Long userId;

    @Schema(description = "Hospital name", example = "Boston General Hospital")
    private String hospitalName;

    @Schema(description = "Registration number", example = "MA-HOSP-99821")
    private String registrationNumber;

    @Schema(description = "License number", example = "LIC-2026-881")
    private String licenseNumber;

    @Schema(description = "License document URL", example = "https://documents.bloodbridge.com/licenses/lic_99821.pdf")
    private String licenseDocumentUrl;

    @Schema(description = "Hospital logo URL", example = "https://images.bloodbridge.com/logos/hosp_1.png")
    private String logoUrl;

    @Schema(description = "Hospital type", example = "GENERAL")
    private String hospitalType;

    @Schema(description = "Contact person", example = "Dr. Robert Vance")
    private String contactPerson;

    @Schema(description = "Official email", example = "info@bostongeneral.org")
    private String email;

    @Schema(description = "Phone number", example = "+16175550199")
    private String phoneNumber;

    @Schema(description = "Website", example = "https://www.bostongeneral.org")
    private String website;

    @Schema(description = "Address", example = "75 Francis Street")
    private String address;

    @Schema(description = "City", example = "Boston")
    private String city;

    @Schema(description = "State", example = "MA")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Postal Code", example = "02115")
    private String postalCode;

    @Schema(description = "Latitude", example = "42.3359")
    private Double latitude;

    @Schema(description = "Longitude", example = "-71.1070")
    private Double longitude;

    @Schema(description = "Verification boolean flag", example = "true")
    private Boolean verified;

    @Schema(description = "Verification status (PENDING, APPROVED, REJECTED)", example = "APPROVED")
    private String verificationStatus;

    @Schema(description = "Admin who verified the hospital", example = "admin@bloodbridge.com")
    private String verifiedBy;

    @Schema(description = "Verification timestamp", example = "2026-08-01T10:00:00")
    private LocalDateTime verifiedAt;

    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectionReason;

    @Schema(description = "Verification remarks or rejection reason", example = "License document verified successfully.")
    private String remarks;

    @Schema(description = "Emergency requests availability", example = "true")
    private Boolean emergencyAvailable;

    @Schema(description = "Operating hours", example = "24/7")
    private String operatingHours;

    @Schema(description = "Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Created timestamp", example = "2026-08-01T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime updatedAt;
}
