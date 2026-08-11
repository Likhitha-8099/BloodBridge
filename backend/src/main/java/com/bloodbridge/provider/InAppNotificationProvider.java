package com.bloodbridge.provider;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Strategy provider for In-App UI notifications.
 */
@Slf4j
@Component
public class InAppNotificationProvider implements NotificationProvider {

    @Override
    public boolean supports(DeliveryChannel channel) {
        return channel == DeliveryChannel.IN_APP;
    }

    @Override
    public void send(Notification notification) {
        log.info("[IN-APP PROVIDER] Registering In-App notification for user ID: {} | Title: {}",
                notification.getRecipientUser() != null ? notification.getRecipientUser().getId() : null, notification.getTitle());

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
    }
}
