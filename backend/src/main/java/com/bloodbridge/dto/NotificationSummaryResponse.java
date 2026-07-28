package com.bloodbridge.dto;

import com.bloodbridge.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a summary of a notification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSummaryResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType notificationType;
    private Boolean readStatus;
    private LocalDateTime createdAt;
}
