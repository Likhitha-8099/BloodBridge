package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing user notification count metrics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Notification Count Response Payload")
public class NotificationCountResponse {

    @Schema(description = "Unread notifications count", example = "3")
    private long unreadCount;

    @Schema(description = "Total notifications count", example = "15")
    private long totalCount;
}
