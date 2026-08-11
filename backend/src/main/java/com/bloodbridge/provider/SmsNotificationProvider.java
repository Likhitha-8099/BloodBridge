package com.bloodbridge.provider;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Strategy provider placeholder for SMS notifications (Twilio / AWS SNS integration).
 */
@Slf4j
@Component
public class SmsNotificationProvider implements NotificationProvider {

    @Override
    public boolean supports(DeliveryChannel channel) {
        return channel == DeliveryChannel.SMS || channel == DeliveryChannel.WHATSAPP;
    }

    @Override
    public void send(Notification notification) {
        String phone = notification.getRecipientUser() != null ? notification.getRecipientUser().getPhoneNumber() : "Unknown";
        log.info("[SMS/WHATSAPP PROVIDER - PLACEHOLDER] Dispatching SMS to Phone: {} | Text: {}", phone, notification.getMessage());

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
    }
}
