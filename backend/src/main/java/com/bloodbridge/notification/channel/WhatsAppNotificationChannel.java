package com.bloodbridge.notification.channel;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.notification.NotificationChannel;
import com.bloodbridge.notification.NotificationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Future-ready Strategy Channel for WhatsApp Messaging.
 */
@Component
@Slf4j
public class WhatsAppNotificationChannel implements NotificationChannel {

    @Override
    public DeliveryChannel getChannel() {
        return DeliveryChannel.WHATSAPP;
    }

    @Override
    public boolean isEnabled() {
        return false; // Ready for Phase 3 WhatsApp API integration
    }

    @Override
    public boolean send(NotificationPayload payload) {
        log.info("[WHATSAPP-CHANNEL-MOCK] WhatsApp Payload prepared for phone: {} | Text: {}",
                payload.getRecipientPhone(), payload.getMessage());
        return true;
    }
}
