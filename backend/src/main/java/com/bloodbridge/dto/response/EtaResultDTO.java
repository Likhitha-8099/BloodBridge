package com.bloodbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing calculated ETA, travel distance, arrival time, and Google Maps URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtaResultDTO {

    private Integer etaMinutes;
    private LocalDateTime estimatedArrivalTime;
    private Double travelDistanceKm;
    private String googleMapsUrl;
    private Boolean calculatedViaGoogleApi;
}
