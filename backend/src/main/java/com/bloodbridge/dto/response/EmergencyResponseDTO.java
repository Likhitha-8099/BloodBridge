package com.bloodbridge.dto.response;

import com.bloodbridge.enums.EmergencyResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO carrying emergency response information, donor details, ETA, and Google Maps Navigation link.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyResponseDTO {

    private Long id;
    private Long emergencyRequestId;
    private Long donorId;
    private String donorName;
    private String donorPhone;
    private String donorEmail;
    private String donorBloodGroup;
    private EmergencyResponseStatus status;
    private Double distanceKm;
    private Integer etaMinutes;
    private Long responseTimeSeconds;
    private String googleMapsUrl;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime createdAt;
}
