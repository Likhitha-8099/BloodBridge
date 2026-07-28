package com.bloodbridge.mapper;

import com.bloodbridge.dto.NotificationResponse;
import com.bloodbridge.dto.NotificationSummaryResponse;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating notification entities to response DTOs.
 */
@Component
public class NotificationMapper {

    /**
     * Maps a {@link Notification} entity to a detailed {@link NotificationResponse}.
     *
     * @param notification the notification entity
     * @return the mapped response DTO
     */
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        User recipient = notification.getRecipientUser();

        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientUserId(recipient != null ? recipient.getId() : null)
                .recipientEmail(recipient != null ? recipient.getEmail() : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType())
                .deliveryChannel(notification.getDeliveryChannel())
                .status(notification.getStatus())
                .readStatus(notification.getReadStatus())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * Maps a {@link Notification} entity to a simplified {@link NotificationSummaryResponse}.
     *
     * @param notification the notification entity
     * @return the mapped summary response DTO
     */
    public NotificationSummaryResponse toSummaryResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationSummaryResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType())
                .readStatus(notification.getReadStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
