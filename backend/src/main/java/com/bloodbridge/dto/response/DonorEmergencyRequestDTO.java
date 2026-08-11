package com.bloodbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Emergency Blood Requests matched specifically for an authenticated donor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorEmergencyRequestDTO {

    private Long requestId;
    private Long matchedDonorId;
    private String hospitalName;
    private String hospitalAddress;
    private String bloodGroup;
    private Integer unitsRequired;
    private String priority;
    private Double distanceKm;
    private String matchingGroup;
    private Double hospitalLatitude;
    private Double hospitalLongitude;
    private LocalDateTime createdAt;
    private LocalDateTime expiryTime;
    private String hospitalPhone;
    private Boolean confirmed;
    private LocalDateTime confirmedAt;
    private String requestStatus;
    private String fulfillmentInstructions;
    private String status;
    private String googleMapsUrl;
}
