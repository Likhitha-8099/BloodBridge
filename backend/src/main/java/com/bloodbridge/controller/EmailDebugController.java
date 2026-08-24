package com.bloodbridge.controller;

import com.bloodbridge.dto.EmergencyMailDto;
import com.bloodbridge.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Temporary REST controller for debugging and validating the SMTP Email pipeline independently of donor matching.
 * Provides endpoints to inspect SMTP properties and trigger test email dispatches.
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/debug", "/api/debug"})
@RequiredArgsConstructor
@Tag(name = "Email Debug API", description = "Endpoints for testing and validating Gmail SMTP email delivery")
public class EmailDebugController {

    private final JavaMailSender mailSender;
    private final EmailService emailService;

    @Value("${SPRING_MAIL_HOST:${MAIL_HOST:${spring.mail.host:smtp.gmail.com}}}")
    private String mailHost;

    @Value("${SPRING_MAIL_PORT:${MAIL_PORT:${spring.mail.port:587}}}")
    private int mailPort;

    @Value("${SPRING_MAIL_USERNAME:${MAIL_USERNAME:${spring.mail.username:}}}")
    private String mailUsername;

    @Value("${SPRING_MAIL_PASSWORD:${MAIL_PASSWORD:${spring.mail.password:}}}")
    private String mailPassword;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        String sanitizedPassword = mailPassword != null ? mailPassword.trim().replace(" ", "") : "";
        boolean pwdSet = !sanitizedPassword.isEmpty() && !"your-gmail-app-password-here".equals(sanitizedPassword);
        log.info("================================================================================");
        log.info("✅ EmailDebugController Loaded Successfully:");
        log.info(" - MAIL_HOST           : {}", mailHost);
        log.info(" - MAIL_PORT           : {}", mailPort);
        log.info(" - MAIL_USERNAME       : {}", mailUsername);
        log.info(" - Password configured : {}", pwdSet);
        log.info("================================================================================");
    }

    @Operation(summary = "Check SMTP Configuration", description = "Returns Spring Mail configuration, JavaMailSender status, and template availability.")
    @GetMapping("/smtp-status")
    public ResponseEntity<Map<String, Object>> getSmtpStatus() {
        Map<String, Object> status = new HashMap<>();
        String sanitizedPassword = mailPassword != null ? mailPassword.trim().replace(" ", "") : "";
        boolean isPasswordDefault = "your-gmail-app-password-here".equals(sanitizedPassword);
        boolean isPasswordEmpty = sanitizedPassword.isEmpty();
        boolean isPasswordValid = !isPasswordEmpty && !isPasswordDefault;

        status.put("mailHost", mailHost);
        status.put("mailPort", mailPort);
        status.put("mailUsername", mailUsername);
        status.put("isPasswordConfigured", isPasswordValid);
        status.put("passwordStatus", isPasswordValid ? "CONFIGURED (Length: " + sanitizedPassword.length() + ")" :
                (isPasswordDefault ? "USING_DEFAULT_PLACEHOLDER ('your-gmail-app-password-here')" : "EMPTY"));
        status.put("javaMailSenderBean", mailSender != null ? mailSender.getClass().getName() : "MISSING");

        boolean templateExists = false;
        try {
            ClassPathResource resource = new ClassPathResource("templates/emergency-alert.html");
            templateExists = resource.exists();
        } catch (Exception ignored) {}
        status.put("emergencyTemplateExists", templateExists);

        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Send Test Emergency Email", description = "Triggers a test emergency HTML email dispatch via SMTP to verify the pipeline.")
    @RequestMapping(value = "/test-email", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> sendTestEmail(
            @RequestParam(required = false) String to,
            @RequestBody(required = false) Map<String, String> body) {

        String recipientEmail = (body != null && body.containsKey("to") && !body.get("to").isBlank())
                ? body.get("to")
                : (to != null && !to.isBlank() ? to : mailUsername);

        log.info("================================================================================");
        log.info("Stage 1: Controller invoked for test email dispatch. Recipient: {}", recipientEmail);

        Map<String, Object> report = new HashMap<>();
        report.put("recipient", recipientEmail);
        report.put("sender", mailUsername);
        report.put("mailHost", mailHost);
        report.put("mailPort", mailPort);

        boolean isPasswordDefault = "your-gmail-app-password-here".equals(mailPassword);
        boolean isPasswordEmpty = mailPassword == null || mailPassword.isBlank();
        boolean isPasswordValid = !isPasswordEmpty && !isPasswordDefault;

        report.put("isPasswordConfigured", isPasswordValid);
        if (!isPasswordValid) {
            report.put("status", "FAILED_CONFIGURATION");
            report.put("error", isPasswordDefault ?
                    "MAIL_PASSWORD is still set to placeholder 'your-gmail-app-password-here'. Update spring.mail.password or set MAIL_PASSWORD environment variable with a valid Gmail App Password." :
                    "MAIL_PASSWORD is empty.");
            report.put("fixInstruction", "1. Enable 2-Step Verification on Gmail account. 2. Generate a 16-character App Password at https://myaccount.google.com/apppasswords. 3. Set MAIL_PASSWORD environment variable.");
            log.error("[DEBUG-ENDPOINT] Configuration Check Failed: Invalid/default MAIL_PASSWORD.");
            return ResponseEntity.badRequest().body(report);
        }

        // Stage 2: Verify Template
        String htmlBody = "";
        try {
            ClassPathResource resource = new ClassPathResource("templates/emergency-alert.html");
            if (!resource.exists()) {
                log.error("Stage 2 Failed: Classpath resource 'templates/emergency-alert.html' does not exist.");
                report.put("status", "FAILED_TEMPLATE");
                report.put("error", "Classpath resource 'templates/emergency-alert.html' does not exist.");
                return ResponseEntity.internalServerError().body(report);
            }
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            htmlBody = template
                    .replace("{donorName}", "Test Donor")
                    .replace("{hospitalName}", "BloodBridge Test Hospital")
                    .replace("{bloodGroup}", "A+")
                    .replace("{unitsRequired}", "2")
                    .replace("{urgencyLevel}", "EMERGENCY")
                    .replace("{location}", "Test City, Test State")
                    .replace("{hospitalAddress}", "123 Healthcare Ave")
                    .replace("{requiredByDate}", "Immediate")
                    .replace("{reason}", "SMTP Diagnostic Verification")
                    .replace("{loginUrl}", frontendUrl + "/login");
            report.put("templateLoaded", true);
            log.info("Stage 2: Template loaded successfully ({} chars)", htmlBody.length());
        } catch (Exception e) {
            log.error("Stage 2 Exception: Failed to load emergency HTML template", e);
            report.put("status", "FAILED_TEMPLATE_READ");
            report.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(report);
        }

        // Stage 3 & 4: MimeMessage Creation & SMTP Dispatch
        try {
            String safeRecipient = recipientEmail != null ? recipientEmail : "";
            String safeSender = mailUsername != null && !mailUsername.isBlank() ? mailUsername : "noreply@bloodbridge.com";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(safeRecipient);
            helper.setSubject("🚨 [TEST] BloodBridge Emergency Alert Pipeline Verification");
            helper.setFrom(safeSender, "BloodBridge System");
            helper.setText(htmlBody, true);
            log.info("Stage 3: MimeMessage created successfully with sender: {}", mailUsername);

            log.info("Stage 4: Connecting & Authenticating with SMTP server {}:{}...", mailHost, mailPort);
            mailSender.send(message);

            log.info("Stage 5: Email sent successfully via SMTP to {}", recipientEmail);
            log.info("================================================================================");
            report.put("status", "SUCCESS");
            report.put("message", "Test emergency HTML email was successfully sent via SMTP to " + recipientEmail);

            // Also trigger async service method to ensure async path functions
            EmergencyMailDto mailDto = EmergencyMailDto.builder()
                    .toEmail(recipientEmail)
                    .donorName("Test Donor (Async)")
                    .hospitalName("BloodBridge Test Hospital")
                    .bloodGroup("A_POSITIVE")
                    .unitsRequired(2)
                    .urgencyLevel("EMERGENCY")
                    .city("Test City")
                    .state("Test State")
                    .requiredByDate("Immediate")
                    .reason("Async Pipeline Test")
                    .loginUrl(frontendUrl + "/login")
                    .build();
            emailService.sendEmergencyAlert(mailDto);

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("[DEBUG-ENDPOINT] SMTP Transmission Failure: {}", e.getMessage(), e);

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            report.put("status", "FAILED_SMTP_TRANSMISSION");
            report.put("error", e.getMessage());
            report.put("exceptionType", e.getClass().getName());
            report.put("stackTrace", sw.toString());

            if (e.getMessage() != null && e.getMessage().contains("535")) {
                report.put("fixInstruction", "SMTP 535 Error: Gmail rejected login credentials. Check if your 16-character App Password has spaces or if 2-Factor Authentication is enabled on " + mailUsername);
            }

            return ResponseEntity.internalServerError().body(report);
        }
    }
}
