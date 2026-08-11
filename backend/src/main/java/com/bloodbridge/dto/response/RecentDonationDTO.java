package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a recent completed blood donation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Recent Donation Summary Payload")
public class RecentDonationDTO {

    @Schema(description = "Donation ID", example = "501")
    private Long id;

    @Schema(description = "Donor Name", example = "John Smith")
    private String donorName;

    @Schema(description = "Blood Group", example = "A_POSITIVE")
    private String bloodGroup;

    @Schema(description = "Donation Date", example = "2026-07-28T14:00:00")
    private LocalDateTime donationDate;

    @Schema(description = "Donation Status", example = "COMPLETED")
    private String status;
}
