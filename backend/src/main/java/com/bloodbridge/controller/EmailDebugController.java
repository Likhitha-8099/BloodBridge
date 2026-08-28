package com.bloodbridge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final com.bloodbridge.service.impl.HttpApiEmailTransportServiceImpl httpApiTransport;
    private final com.bloodbridge.service.impl.SmtpEmailTransportServiceImpl smtpTransport;

    @Value("${SPRING_MAIL_HOST:${MAIL_HOST:${spring.mail.host:smtp.gmail.com}}}")
    private String mailHost;

    @Value("${SPRING_MAIL_PORT:${MAIL_PORT:${spring.mail.port:587}}}")
    private int mailPort;

    @Value("${SPRING_MAIL_USERNAME:${MAIL_USERNAME:${spring.mail.username:}}}")
    private String mailUsername;

    @Value("${SPRING_MAIL_PASSWORD:${MAIL_PASSWORD:${spring.mail.password:}}}")
    private String mailPassword;

    @Value("${EMAIL_PROVIDER:${app.email.provider:smtp}}")
    private String emailProvider;

    @Value("${RESEND_API_KEY:${EMAIL_API_KEY:${app.email.resend.api-key:}}}")
    private String resendApiKey;

    @Value("${EMAIL_FROM:${RESEND_FROM_EMAIL:${app.email.from:BloodBridge <onboarding@resend.dev>}}}")
    private String resendFrom;

    @Value("${BREVO_API_KEY:${app.email.brevo.api-key:}}")
    private String brevoApiKey;

    @Value("${BREVO_FROM_EMAIL:${app.email.brevo.from:insureai2@gmail.com}}")
    private String brevoFrom;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        String sanitizedPassword = mailPassword != null ? mailPassword.trim().replace(" ", "") : "";
        boolean pwdSet = !sanitizedPassword.isEmpty() && !"your-gmail-app-password-here".equals(sanitizedPassword);
        boolean resendSet = resendApiKey != null && !resendApiKey.isBlank();
        boolean brevoSet = brevoApiKey != null && !brevoApiKey.isBlank();

        log.info("================================================================================");
        log.info("✅ EmailDebugController Loaded Successfully:");
        log.info(" - EMAIL_PROVIDER      : {}", emailProvider);
        log.info(" - Active Transport    : {}", getActiveTransport().getProviderName());
        log.info(" - RESEND Configured   : {}", resendSet);
        log.info(" - BREVO Configured    : {}", brevoSet);
        log.info(" - SMTP Configured     : {}", pwdSet);
        log.info(" - MAIL_HOST           : {}", mailHost);
        log.info(" - MAIL_PORT           : {}", mailPort);
        log.info(" - MAIL_USERNAME       : {}", maskEmail(mailUsername));
        log.info("================================================================================");
    }

    private com.bloodbridge.service.EmailTransportService getActiveTransport() {
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

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "NOT_CONFIGURED";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(0, at));
        return email.charAt(0) + "***" + email.substring(at);
    }

    @Operation(summary = "Probe Outbound SMTP Network Connectivity", description = "Tests TCP socket connectivity from host to Gmail SMTP ports 465 and 587.")
    @GetMapping("/smtp-probe")
    public ResponseEntity<Map<String, Object>> probeSmtpPorts() {
        Map<String, Object> report = new HashMap<>();
        report.put("targetHost", mailHost);
        report.put("configuredPort", mailPort);
        report.put("configuredMode", mailPort == 465 ? "SMTPS" : "STARTTLS");
        report.put("isUsernameConfigured", mailUsername != null && !mailUsername.isBlank());

        // Probe Port 465
        Map<String, Object> port465Result = new HashMap<>();
        try (java.net.Socket socket = new java.net.Socket()) {
            long t0 = System.currentTimeMillis();
            socket.connect(new java.net.InetSocketAddress(mailHost, 465), 5000);
            port465Result.put("reachable", true);
            port465Result.put("latencyMs", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            port465Result.put("reachable", false);
            port465Result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        report.put("port465Probe", port465Result);

        // Probe Port 587
        Map<String, Object> port587Result = new HashMap<>();
        try (java.net.Socket socket = new java.net.Socket()) {
            long t0 = System.currentTimeMillis();
            socket.connect(new java.net.InetSocketAddress(mailHost, 587), 5000);
            port587Result.put("reachable", true);
            port587Result.put("latencyMs", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            port587Result.put("reachable", false);
            port587Result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        report.put("port587Probe", port587Result);

        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Check SMTP and HTTPS Email Configuration", description = "Returns Email transport configuration, JavaMailSender status, API status, and template availability.")
    @GetMapping({"/smtp-status", "/email-status"})
    public ResponseEntity<Map<String, Object>> getSmtpStatus() {
        Map<String, Object> status = new HashMap<>();
        String sanitizedPassword = mailPassword != null ? mailPassword.trim().replace(" ", "") : "";
        boolean isPasswordDefault = "your-gmail-app-password-here".equals(sanitizedPassword);
        boolean isPasswordEmpty = sanitizedPassword.isEmpty();
        boolean isPasswordValid = !isPasswordEmpty && !isPasswordDefault;

        com.bloodbridge.service.EmailTransportService activeTransport = getActiveTransport();
        boolean isResendConfigured = resendApiKey != null && !resendApiKey.isBlank();
        boolean isBrevoConfigured = brevoApiKey != null && !brevoApiKey.isBlank();

        status.put("emailProvider", emailProvider);
        status.put("activeTransport", activeTransport.getProviderName());
        status.put("isTransportConfigured", activeTransport.isConfigured());
        status.put("isHttpApiConfigured", httpApiTransport.isConfigured());
        status.put("isResendConfigured", isResendConfigured);
        status.put("isBrevoConfigured", isBrevoConfigured);
        status.put("resendFrom", resendFrom);
        status.put("brevoFrom", brevoFrom);
        status.put("mailHost", mailHost);
        status.put("mailPort", mailPort);
        status.put("mailUsername", maskEmail(mailUsername));
        status.put("isSmtpPasswordConfigured", isPasswordValid);
        status.put("passwordStatus", isPasswordValid ? "CONFIGURED" : "EMPTY");
        status.put("smtpMode", mailPort == 465 ? "SMTPS" : "STARTTLS");
        status.put("javaMailSenderBean", mailSender != null ? mailSender.getClass().getName() : "MISSING");

        boolean templateExists = false;
        try {
            ClassPathResource resource = new ClassPathResource("templates/emergency-alert.html");
            templateExists = resource.exists();
        } catch (Exception ignored) {}
        status.put("emergencyTemplateExists", templateExists);

        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Send Test Emergency Email", description = "Triggers a test emergency HTML email dispatch via the active configured email transport to verify the pipeline.")
    @RequestMapping(value = "/test-email", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> sendTestEmail(
            @RequestParam(required = false) String to,
            @RequestBody(required = false) Map<String, String> body) {

        String candidate = (body != null && body.containsKey("to") && !body.get("to").isBlank())
                ? body.get("to")
                : to;

        com.bloodbridge.service.EmailTransportService activeTransport = getActiveTransport();

        // Default recipient resolution
        String recipientEmail = (candidate != null && !candidate.isBlank())
                ? candidate.trim()
                : (mailUsername != null && !mailUsername.isBlank() ? mailUsername : "test@bloodbridge.com");
        String maskedRecipient = maskEmail(recipientEmail);

        log.info("================================================================================");
        log.info("[DEBUG-ENDPOINT] Test email dispatch initiated | Active Transport: {} | Recipient: {}",
                activeTransport.getProviderName(), maskedRecipient);

        Map<String, Object> report = new HashMap<>();
        report.put("recipient", maskedRecipient);
        report.put("emailProvider", emailProvider);
        report.put("activeTransport", activeTransport.getProviderName());
        report.put("isConfigured", activeTransport.isConfigured());

        if (!activeTransport.isConfigured()) {
            report.put("status", "FAILED_CONFIGURATION");
            if ("resend".equalsIgnoreCase(emailProvider) || activeTransport.getProviderName().contains("RESEND")) {
                report.put("error", "RESEND_API_KEY environment variable is not configured.");
                report.put("fixInstruction", "Configure RESEND_API_KEY in your cloud deployment settings (e.g. Render). Also ensure EMAIL_PROVIDER=resend.");
            } else if ("brevo".equalsIgnoreCase(emailProvider) || activeTransport.getProviderName().contains("BREVO")) {
                report.put("error", "BREVO_API_KEY environment variable is not configured.");
                report.put("fixInstruction", "Configure BREVO_API_KEY in your cloud deployment settings. Also ensure EMAIL_PROVIDER=brevo.");
            } else {
                report.put("error", "SMTP credentials not configured or blocked in cloud environment.");
                report.put("fixInstruction", "In cloud environments, standard SMTP (ports 587/465) is blocked by cloud firewalls. Set EMAIL_PROVIDER=resend and configure RESEND_API_KEY.");
            }
            log.error("[DEBUG-ENDPOINT] Configuration Check Failed for Transport: {}", activeTransport.getProviderName());
            return ResponseEntity.badRequest().body(report);
        }

        // Stage 2: Verify Template
        String htmlBody = "";
        try {
            ClassPathResource resource = new ClassPathResource("templates/emergency-alert.html");
            if (!resource.exists()) {
                log.error("[DEBUG-ENDPOINT] Classpath resource 'templates/emergency-alert.html' does not exist.");
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
                    .replace("{location}", "Diagnostic Test Location")
                    .replace("{hospitalAddress}", "123 Healthcare Ave")
                    .replace("{requiredByDate}", "Immediate")
                    .replace("{reason}", "Email Diagnostic Verification")
                    .replace("{loginUrl}", frontendUrl + "/login");
            report.put("templateLoaded", true);
        } catch (Exception e) {
            log.error("[DEBUG-ENDPOINT] Failed to load emergency HTML template", e);
            report.put("status", "FAILED_TEMPLATE_READ");
            report.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(report);
        }

        // Stage 3: Dispatch via active transport
        try {
            long t0 = System.currentTimeMillis();
            String subject = "🚨 [TEST] BloodBridge Emergency Alert Pipeline Verification";
            activeTransport.sendHtmlEmail(recipientEmail, subject, htmlBody, "BloodBridge Team", null, null);
            long latencyMs = System.currentTimeMillis() - t0;

            log.info("[DEBUG-ENDPOINT] Test email sent successfully via {} to {} ({} ms)",
                    activeTransport.getProviderName(), maskedRecipient, latencyMs);
            log.info("================================================================================");

            report.put("status", "SUCCESS");
            report.put("message", "Test email was successfully sent via " + activeTransport.getProviderName() + " to " + maskedRecipient);
            report.put("latencyMs", latencyMs);

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("[DEBUG-ENDPOINT] Email Transmission Failure via {}: {}", activeTransport.getProviderName(), e.getMessage(), e);

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            report.put("status", "FAILED_EMAIL_TRANSMISSION");
            report.put("error", e.getMessage());
            report.put("exceptionType", e.getClass().getName());
            report.put("stackTrace", sw.toString());

            return ResponseEntity.internalServerError().body(report);
        }
    }
}
