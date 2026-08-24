package com.bloodbridge.provider;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.service.EmailTransportService;
import com.bloodbridge.service.impl.HttpApiEmailTransportServiceImpl;
import com.bloodbridge.service.impl.SmtpEmailTransportServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Strategy provider for Email notifications using dual SMTP and HTTPS REST API transport.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationProvider implements NotificationProvider {

    private final SmtpEmailTransportServiceImpl smtpTransport;
    private final HttpApiEmailTransportServiceImpl httpApiTransport;

    @Value("${EMAIL_PROVIDER:${app.email.provider:smtp}}")
    private String emailProvider;

    @Override
    public boolean supports(DeliveryChannel channel) {
        return channel == DeliveryChannel.EMAIL;
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "[MISSING_EMAIL]";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***" + email.substring(Math.max(0, atIndex));
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private EmailTransportService getActiveTransport() {
        if ("resend".equalsIgnoreCase(emailProvider) || "brevo".equalsIgnoreCase(emailProvider) || "api".equalsIgnoreCase(emailProvider)) {
            return httpApiTransport;
        }
        if ("smtp".equalsIgnoreCase(emailProvider)) {
            return smtpTransport;
        }
        if (smtpTransport.isConfigured()) {
            return smtpTransport;
        }
        if (httpApiTransport.isConfigured()) {
            return httpApiTransport;
        }
        return smtpTransport;
    }

    @Override
    public void send(Notification notification) {
        String recipientEmail = notification.getRecipientUser() != null ? notification.getRecipientUser().getEmail() : null;
        String maskedRecipient = maskEmail(recipientEmail);
        log.info("[EMAIL] Dispatching notification | Recipient: {} | Subject: {}", maskedRecipient, notification.getTitle());

        if (recipientEmail == null || recipientEmail.isBlank()) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setLastFailureReason("Recipient email is missing");
            return;
        }

        try {
            EmailTransportService transport = getActiveTransport();
            transport.sendHtmlEmail(recipientEmail, notification.getTitle(), buildHtmlEmailBody(notification), "BloodBridge System", null, null);
            log.info("[EMAIL] Notification email successfully sent via {} | Recipient: {}", transport.getProviderName(), maskedRecipient);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("[EMAIL] Failed to send notification email: {} ({}) | Recipient: {}",
                    e.getClass().getSimpleName(), e.getMessage(), maskedRecipient);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setLastFailureReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setNextRetryTime(LocalDateTime.now().plusMinutes(5));
        }
    }

    private String buildHtmlEmailBody(Notification notification) {
        return "<!DOCTYPE html><html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; padding: 24px; border-radius: 8px; border-top: 4px solid #e11d48;'>" +
                "<h2 style='color: #e11d48;'>" + notification.getTitle() + "</h2>" +
                "<p style='color: #334155; font-size: 16px;'>" + notification.getMessage() + "</p>" +
                "<hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 20px 0;'/>" +
                "<p style='color: #94a3b8; font-size: 12px;'>Blood Bridge Emergency Network &bull; Production Healthcare System</p>" +
                "</div></body></html>";
    }
}
