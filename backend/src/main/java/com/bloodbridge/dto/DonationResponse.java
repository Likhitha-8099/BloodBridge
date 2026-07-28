package com.bloodbridge.dto;

import com.bloodbridge.enums.DonationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the detailed response of a donation record.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationResponse {

    private Long id;
    private Long donorId;
    private String donorName;
    private Long patientId;
    private String patientName;
    private Long requestId;
    private Long hospitalId;
    private String hospitalName;
    private Long matchResultId;
    private LocalDate donationDate;
    private Integer unitsDonated;
    private String remarks;
    private DonationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
