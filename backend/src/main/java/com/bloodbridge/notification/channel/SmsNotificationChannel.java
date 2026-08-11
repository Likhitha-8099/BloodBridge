package com.bloodbridge.notification.channel;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.notification.NotificationChannel;
import com.bloodbridge.notification.NotificationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Future-ready Strategy Channel for SMS Alert Dispatch.
 */
@Component
@Slf4j
public class SmsNotificationChannel implements NotificationChannel {

    @Override
    public DeliveryChannel getChannel() {
        return DeliveryChannel.SMS;
    }

    @Override
    public boolean isEnabled() {
        return false; // Ready for Phase 3 SMS provider integration
    }

    @Override
    public boolean send(NotificationPayload payload) {
        log.info("[SMS-CHANNEL-MOCK] SMS Payload prepared for phone: {} | Text: {}",
                payload.getRecipientPhone(), payload.getMessage());
        return true;
    }
}
