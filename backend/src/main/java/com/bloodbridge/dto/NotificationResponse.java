package com.bloodbridge.dto;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the detailed response of a notification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long recipientUserId;
    private String recipientEmail;
    private String title;
    private String message;
    private NotificationType notificationType;
    private DeliveryChannel deliveryChannel;
    private NotificationStatus status;
    private Boolean readStatus;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
