package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing notification statistics across the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatisticsResponse {

    private Long totalNotifications;
    private Long readNotifications;
    private Long unreadNotifications;
    private Long failedNotifications;
    private Long sentNotifications;
}
