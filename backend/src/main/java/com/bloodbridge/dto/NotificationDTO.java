package com.bloodbridge.dto;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing notification information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private Long recipientUserId;
    private String recipientRole;
    private String title;
    private String message;
    private NotificationType notificationType;
    private DeliveryChannel deliveryChannel;
    private String priority;
    private NotificationStatus status;
    private Long donorId;
    private String donorName;
    private String bloodGroup;
    private Long requestId;
    private Long hospitalId;
    private String hospitalName;
    private Long patientId;
    private String patientName;
    private String actionUrl;
    private Boolean readStatus;
    @JsonProperty("isRead")
    private Boolean isRead;
    private String relatedEntityType;
    private Long relatedEntityId;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
