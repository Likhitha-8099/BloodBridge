package com.bloodbridge.service.impl;

import com.bloodbridge.service.EmailTransportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * High-performance HTTPS REST API email transport implementation.
 * Bypasses cloud egress SMTP port blocking (ports 25, 465, 587) by sending emails over HTTPS Port 443.
 * Supports Resend and Brevo transactional email APIs with full attachment support.
 */
@Service
@Slf4j
public class HttpApiEmailTransportServiceImpl implements EmailTransportService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${EMAIL_PROVIDER:${app.email.provider:smtp}}")
    private String configuredProvider;

    @Value("${RESEND_API_KEY:${EMAIL_API_KEY:${app.email.resend.api-key:}}}")
    private String resendApiKey;

    @Value("${EMAIL_FROM:${RESEND_FROM_EMAIL:${app.email.from:BloodBridge <onboarding@resend.dev>}}}")
    private String resendFrom;

    @Value("${BREVO_API_KEY:${app.email.brevo.api-key:}}}")
    private String brevoApiKey;

    @Value("${BREVO_FROM_EMAIL:${app.email.brevo.from:insureai2@gmail.com}}}")
    private String brevoFrom;

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "[MISSING_EMAIL]";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***" + email.substring(Math.max(0, atIndex));
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    @Override
    public boolean isConfigured() {
        if ("brevo".equalsIgnoreCase(configuredProvider) || (brevoApiKey != null && !brevoApiKey.isBlank())) {
            return brevoApiKey != null && !brevoApiKey.isBlank();
        }
        return resendApiKey != null && !resendApiKey.isBlank();
    }

    @Override
    public String getProviderName() {
        if ("brevo".equalsIgnoreCase(configuredProvider)) {
            return "BREVO_HTTPS_API";
        }
        return "RESEND_HTTPS_API";
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent, String fromName, byte[] attachmentBytes, String attachmentFilename) {
        if ("brevo".equalsIgnoreCase(configuredProvider)) {
            sendViaBrevo(to, subject, htmlContent, true, fromName, attachmentBytes, attachmentFilename);
        } else {
            sendViaResend(to, subject, htmlContent, true, fromName, attachmentBytes, attachmentFilename);
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String textContent, String fromName) {
        if ("brevo".equalsIgnoreCase(configuredProvider)) {
            sendViaBrevo(to, subject, textContent, false, fromName, null, null);
        } else {
            sendViaResend(to, subject, textContent, false, fromName, null, null);
        }
    }

    private void sendViaResend(String to, String subject, String content, boolean isHtml, String fromName, byte[] attachmentBytes, String attachmentFilename) {
        String maskedTo = maskEmail(to);
        log.info("[EMAIL-API] Starting Resend HTTPS API dispatch | Recipient: {} | Subject: {}", maskedTo, subject);

        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("Resend API Key is not configured. Set RESEND_API_KEY environment variable.");
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            
            // Sender resolution
            String sender = (resendFrom != null && !resendFrom.isBlank()) ? resendFrom.trim() : "BloodBridge <onboarding@resend.dev>";
            payload.put("from", sender);
            payload.put("to", Collections.singletonList(to));
            payload.put("subject", subject);

            if (isHtml) {
                payload.put("html", content);
            } else {
                payload.put("text", content);
            }

            if (attachmentBytes != null && attachmentBytes.length > 0 && attachmentFilename != null) {
                Map<String, String> attachment = new HashMap<>();
                attachment.put("filename", attachmentFilename);
                attachment.put("content", Base64.getEncoder().encodeToString(attachmentBytes));
                payload.put("attachments", Collections.singletonList(attachment));
            }

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(12))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            long t0 = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latencyMs = System.currentTimeMillis() - t0;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[EMAIL-API] Resend HTTPS API dispatch SUCCESS | Recipient: {} | Status: {} | Duration: {} ms",
                        maskedTo, response.statusCode(), latencyMs);
            } else {
                log.error("[EMAIL-API] Resend HTTPS API dispatch FAILED | Recipient: {} | Status: {} | Response: {}",
                        maskedTo, response.statusCode(), response.body());
                throw new RuntimeException("Resend API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            log.error("[EMAIL-API] Resend HTTPS API Exception: {} | Recipient: {}", e.getMessage(), maskedTo);
            throw new RuntimeException("Failed to send email via Resend HTTPS API: " + e.getMessage(), e);
        }
    }

    private void sendViaBrevo(String to, String subject, String content, boolean isHtml, String fromName, byte[] attachmentBytes, String attachmentFilename) {
        String maskedTo = maskEmail(to);
        log.info("[EMAIL-API] Starting Brevo HTTPS API dispatch | Recipient: {} | Subject: {}", maskedTo, subject);

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            throw new IllegalStateException("Brevo API Key is not configured. Set BREVO_API_KEY environment variable.");
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            
            Map<String, String> sender = new HashMap<>();
            sender.put("name", fromName != null && !fromName.isBlank() ? fromName : "BloodBridge");
            sender.put("email", (brevoFrom != null && !brevoFrom.isBlank()) ? brevoFrom.trim() : "insureai2@gmail.com");
            payload.put("sender", sender);

            payload.put("to", Collections.singletonList(Collections.singletonMap("email", to)));
            payload.put("subject", subject);

            if (isHtml) {
                payload.put("htmlContent", content);
            } else {
                payload.put("textContent", content);
            }

            if (attachmentBytes != null && attachmentBytes.length > 0 && attachmentFilename != null) {
                Map<String, String> attachment = new HashMap<>();
                attachment.put("name", attachmentFilename);
                attachment.put("content", Base64.getEncoder().encodeToString(attachmentBytes));
                payload.put("attachment", Collections.singletonList(attachment));
            }

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(12))
                    .header("api-key", brevoApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            long t0 = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latencyMs = System.currentTimeMillis() - t0;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[EMAIL-API] Brevo HTTPS API dispatch SUCCESS | Recipient: {} | Status: {} | Duration: {} ms",
                        maskedTo, response.statusCode(), latencyMs);
            } else {
                log.error("[EMAIL-API] Brevo HTTPS API dispatch FAILED | Recipient: {} | Status: {} | Response: {}",
                        maskedTo, response.statusCode(), response.body());
                throw new RuntimeException("Brevo API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            log.error("[EMAIL-API] Brevo HTTPS API Exception: {} | Recipient: {}", e.getMessage(), maskedTo);
            throw new RuntimeException("Failed to send email via Brevo HTTPS API: " + e.getMessage(), e);
        }
    }
}
