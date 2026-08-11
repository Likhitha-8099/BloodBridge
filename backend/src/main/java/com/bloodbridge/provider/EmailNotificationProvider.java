package com.bloodbridge.provider;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

/**
 * Strategy provider for Email notifications using Spring Mail and HTML templates.
 */
@Slf4j
@Component
public class EmailNotificationProvider implements NotificationProvider {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public boolean supports(DeliveryChannel channel) {
        return channel == DeliveryChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        String recipientEmail = notification.getRecipientUser() != null ? notification.getRecipientUser().getEmail() : null;
        log.info("[EMAIL PROVIDER] Dispatching email notification to: {} | Subject: {}", recipientEmail, notification.getTitle());

        if (recipientEmail == null || recipientEmail.isBlank()) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setLastFailureReason("Recipient email is missing");
            return;
        }

        try {
            if (mailSender != null) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(recipientEmail);
                helper.setSubject(notification.getTitle());
                helper.setText(buildHtmlEmailBody(notification), true);
                mailSender.send(mimeMessage);
                log.info("[EMAIL PROVIDER] Email successfully sent via JavaMailSender to {}", recipientEmail);
            } else {
                log.info("[EMAIL PROVIDER - SIMULATION] MailSender not configured. Email logged: To={}, Title={}, Content={}",
                        recipientEmail, notification.getTitle(), notification.getMessage());
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("[EMAIL PROVIDER ERROR] Failed to send email to {}: {}", recipientEmail, e.getMessage(), e);
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
