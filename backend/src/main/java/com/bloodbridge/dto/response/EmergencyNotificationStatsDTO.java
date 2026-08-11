package com.bloodbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO carrying telemetry and dispatch statistics for an emergency blood request notification run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyNotificationStatsDTO {

    private Long emergencyRequestId;
    private Long totalDonorsEvaluated;
    private Long compatibleDonors;
    private Long within50KmDonors;
    private Long emailsQueued;
    private Long emailsSent;
    private Long emailsFailed;
    private Long emailsSkipped;
    private Long totalExecutionTimeMs;
    private Double averageSmtpTimeMs;
}
