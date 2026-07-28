package com.bloodbridge.dto;

import com.bloodbridge.enums.DonationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object representing a summary of a donation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationSummaryResponse {

    private Long id;
    private String donorName;
    private String patientName;
    private String hospitalName;
    private LocalDate donationDate;
    private Integer unitsDonated;
    private DonationStatus status;
}
