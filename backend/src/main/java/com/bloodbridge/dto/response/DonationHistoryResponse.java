package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DonationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an entry in a donor's donation history timeline.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Donation History Item Response Payload")
public class DonationHistoryResponse {

    @Schema(description = "Donation ID", example = "10")
    private Long id;

    @Schema(description = "Date of donation", example = "2026-05-15")
    private LocalDate donationDate;

    @Schema(description = "Hospital name where donation occurred", example = "Boston General Hospital")
    private String hospitalName;

    @Schema(description = "Units donated", example = "1")
    private Integer unitsDonated;

    @Schema(description = "Blood group donated", example = "O_POSITIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Donation lifecycle status", example = "COMPLETED")
    private DonationStatus status;

    @Schema(description = "Certificate URL extension point", example = "https://certificates.bloodbridge.com/cert_10.pdf")
    private String certificateUrl;

    @Schema(description = "Attending doctor or phlebotomist notes", example = "Successful donation. Hemoglobin levels: 14.5 g/dL.")
    private String doctorNotes;

    @Schema(description = "Type of donation (Whole Blood, Platelets, Plasma)", example = "WHOLE_BLOOD")
    private String donationType;

    @Schema(description = "Record creation timestamp", example = "2026-05-15T14:30:00")
    private LocalDateTime createdAt;
}
