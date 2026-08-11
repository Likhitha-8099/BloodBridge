package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * DTO representing Admin Analytics metrics for Push & Multi-channel notifications.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Push & Multi-Channel Notification Analytics Metrics Payload")
public class PushAnalyticsResponse {

    @Schema(description = "Total emergency emails dispatched successfully")
    private Long emailsSent;

    @Schema(description = "Total email dispatch failures")
    private Long emailsFailed;

    @Schema(description = "Total WebSocket real-time alerts delivered")
    private Long webSocketDelivered;

    @Schema(description = "Total FCM Push notifications dispatched successfully")
    private Long pushSent;

    @Schema(description = "Total FCM Push notification failures")
    private Long pushFailed;

    @Schema(description = "FCM Push delivery success rate percentage (0-100%)", example = "98.5")
    private Double pushSuccessPercentage;

    @Schema(description = "Average push delivery latency in milliseconds", example = "124.5")
    private Double averagePushLatencyMs;

    @Schema(description = "Total exponential backoff retry attempts executed")
    private Long retryCount;

    @Schema(description = "Total invalid/unregistered FCM tokens automatically cleaned up")
    private Long invalidTokensRemoved;

    @Schema(description = "Top failure reasons map (Reason -> Count)")
    private Map<String, Long> topFailureReasons;
}
