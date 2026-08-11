package com.bloodbridge.dto.response;

import com.bloodbridge.enums.EmailDeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO carrying retry queue item details for the Admin Retry Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryDashboardItemDTO {

    private Long id;
    private Long emergencyRequestId;
    private Long donorId;
    private String recipientEmail;
    private Integer attemptNumber;
    private String failureReason;
    private String smtpError;
    private LocalDateTime retryTimestamp;
    private LocalDateTime nextRetryTimestamp;
    private EmailDeliveryStatus currentStatus;
}
