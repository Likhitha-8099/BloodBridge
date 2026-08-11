package com.bloodbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO carrying delivery analytics metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAnalyticsDTO {

    private long emailsSent;
    private long emailsDelivered;
    private long emailsFailed;
    private long emailsPending;

    private long popupSent;
    private long popupDelivered;
    private long popupFailed;

    private long retryCount;
    private double averageSmtpTimeMs;
    private double averageWebSocketTimeMs;

    private double averageDonorResponseTimeSeconds;
    private String fastestDonorName;
    private Long fastestDonorResponseTimeSeconds;
    private String slowestDonorName;
    private Long slowestDonorResponseTimeSeconds;
    private double averageEtaMinutes;
}
