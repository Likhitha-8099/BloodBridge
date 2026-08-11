package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Summary DTO for a matched donor in the Smart Donor Matching Pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorMatchSummaryDTO {
    private Long donorId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private BloodGroup bloodGroup;
    private double distanceKm;
    private LocalDate lastDonationDate;
    private double donorScore;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
}
