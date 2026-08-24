package com.bloodbridge.service.impl;

import com.bloodbridge.service.EmailTransportService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Standard SMTP transport implementation using Spring Mail and JavaMailSender.
 * Used for local development and direct SMTP connections.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailTransportServiceImpl implements EmailTransportService {

    private final JavaMailSender mailSender;

    @Value("${SPRING_MAIL_USERNAME:${MAIL_USERNAME:${spring.mail.username:}}}")
    private String fromEmail;

    @Value("${SPRING_MAIL_PASSWORD:${MAIL_PASSWORD:${spring.mail.password:}}}")
    private String mailPassword;

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "[MISSING_EMAIL]";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***" + email.substring(Math.max(0, atIndex));
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private String getSenderEmail() {
        if (fromEmail != null && !fromEmail.isBlank()) {
            return fromEmail.trim();
        }
        if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl) {
            String u = ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getUsername();
            if (u != null && !u.isBlank()) {
                return u.trim();
            }
        }
        return "noreply@bloodbridge.com";
    }

    @Override
    public boolean isConfigured() {
        return mailPassword != null && !mailPassword.isBlank() && !"your-gmail-app-password-here".equals(mailPassword.trim());
    }

    @Override
    public String getProviderName() {
        return "GMAIL_SMTP";
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent, String fromName, byte[] attachmentBytes, String attachmentFilename) {
        String maskedTo = maskEmail(to);
        log.info("[EMAIL-SMTP] Starting SMTP dispatch | Recipient: {} | Subject: {}", maskedTo, subject);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(getSenderEmail(), fromName != null && !fromName.isBlank() ? fromName : "BloodBridge Team");
            helper.setText(htmlContent, true);

            if (attachmentBytes != null && attachmentBytes.length > 0 && attachmentFilename != null) {
                helper.addAttachment(attachmentFilename, new ByteArrayResource(attachmentBytes));
            }

            long t0 = System.currentTimeMillis();
            mailSender.send(mimeMessage);
            long durationMs = System.currentTimeMillis() - t0;
            log.info("[EMAIL-SMTP] SMTP dispatch SUCCESS | Recipient: {} | Duration: {} ms", maskedTo, durationMs);
        } catch (Exception e) {
            log.error("[EMAIL-SMTP] SMTP dispatch FAILED | Recipient: {} | Error: {}", maskedTo, e.getMessage());
            throw new RuntimeException("SMTP delivery failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String textContent, String fromName) {
        String maskedTo = maskEmail(to);
        log.info("[EMAIL-SMTP] Starting Simple SMTP dispatch | Recipient: {} | Subject: {}", maskedTo, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(getSenderEmail());
            message.setSubject(subject);
            message.setText(textContent);

            long t0 = System.currentTimeMillis();
            mailSender.send(message);
            long durationMs = System.currentTimeMillis() - t0;
            log.info("[EMAIL-SMTP] Simple SMTP dispatch SUCCESS | Recipient: {} | Duration: {} ms", maskedTo, durationMs);
        } catch (Exception e) {
            log.error("[EMAIL-SMTP] Simple SMTP dispatch FAILED | Recipient: {} | Error: {}", maskedTo, e.getMessage());
            throw new RuntimeException("Simple SMTP delivery failed: " + e.getMessage(), e);
        }
    }
}
