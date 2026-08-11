package com.bloodbridge.provider;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Strategy provider for Mobile Push Notifications (Firebase FCM integration placeholder).
 */
@Slf4j
@Component
public class PushNotificationProvider implements NotificationProvider {

    @Override
    public boolean supports(DeliveryChannel channel) {
        return channel == DeliveryChannel.PUSH;
    }

    @Override
    public void send(Notification notification) {
        String recipient = notification.getRecipientUser() != null ? notification.getRecipientUser().getEmail() : "Unknown";
        log.info("[PUSH PROVIDER - FCM] Sending Push Notification to User: {} | Title: {}", recipient, notification.getTitle());

        // Firebase Cloud Messaging placeholder dispatch
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
    }
}
