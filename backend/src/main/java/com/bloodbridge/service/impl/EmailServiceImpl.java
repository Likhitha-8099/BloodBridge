package com.bloodbridge.service.impl;

import com.bloodbridge.dto.EmergencyMailDto;
import com.bloodbridge.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Service implementation for asynchronous email operations utilizing Spring Mail and MimeMessageHelper.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final com.bloodbridge.repository.EmailNotificationRepository emailNotificationRepository;

    @Value("${SPRING_MAIL_HOST:${MAIL_HOST:${spring.mail.host:smtp.gmail.com}}}")
    private String mailHost;

    @Value("${SPRING_MAIL_PORT:${MAIL_PORT:${spring.mail.port:587}}}")
    private int mailPort;

    @Value("${SPRING_MAIL_USERNAME:${MAIL_USERNAME:${spring.mail.username:}}}")
    private String fromEmail;

    @Value("${SPRING_MAIL_PASSWORD:${MAIL_PASSWORD:${spring.mail.password:}}}")
    private String mailPassword;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private volatile String cachedEmergencyHtmlTemplate = null;

    /**
     * Safely masks an email address for production logging (e.g. l***@gmail.com).
     */
    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "[MISSING_EMAIL]";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(0, atIndex));
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private boolean isSmtpPasswordConfigured() {
        return mailPassword != null && !mailPassword.isBlank() && !"your-gmail-app-password-here".equals(mailPassword.trim());
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        boolean pwdSet = isSmtpPasswordConfigured();
        log.info("================================================================================");
        log.info("[EMAIL] EmailServiceImpl Initialized with Spring Mail configuration:");
        log.info("[EMAIL]  - SMTP Host           : {}", mailHost);
        log.info("[EMAIL]  - SMTP Port           : {}", mailPort);
        log.info("[EMAIL]  - Sender / From Email : {}", fromEmail);
        log.info("[EMAIL]  - SMTP Configured     : {}", pwdSet);
        log.info("[EMAIL]  - JavaMailSender Bean : {}", mailSender != null ? mailSender.getClass().getName() : "NULL");
        getEmergencyHtmlTemplate();
        log.info("[EMAIL]  - Emergency Template  : {}", cachedEmergencyHtmlTemplate != null ? "CACHED" : "UNAVAILABLE");
        log.info("================================================================================");
    }

    private String getEmergencyHtmlTemplate() {
        if (cachedEmergencyHtmlTemplate != null) {
            return cachedEmergencyHtmlTemplate;
        }
        synchronized (this) {
            if (cachedEmergencyHtmlTemplate != null) {
                return cachedEmergencyHtmlTemplate;
            }
            try {
                ClassPathResource resource = new ClassPathResource("templates/emergency-alert.html");
                if (resource.exists()) {
                    cachedEmergencyHtmlTemplate = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                log.error("Failed to pre-cache emergency HTML template", e);
            }
            return cachedEmergencyHtmlTemplate;
        }
    }

    private final java.util.Set<String> processedEmailKeys = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @Override
    @Async("emergencyEmailExecutor")
    public void sendEmergencyAlert(EmergencyMailDto mailDto) {
        String recipientEmail = mailDto != null ? mailDto.getToEmail() : null;
        String donorName = mailDto != null ? mailDto.getDonorName() : "Valued Donor";
        String subject = "🚨 Emergency Blood Required - BloodBridge";
        Long reqId = mailDto != null ? mailDto.getRequestId() : null;
        Long donorId = mailDto != null ? mailDto.getDonorId() : null;

        // Idempotency check: prevent duplicate emergency alert emails to the same donor for the same request
        if (reqId != null && donorId != null) {
            String idempotencyKey = reqId + "_" + donorId + "_EMERGENCY_ALERT";
            if (!processedEmailKeys.add(idempotencyKey)) {
                log.info("[EMAIL-DONOR-SKIP] Duplicate emergency email request ignored for Key: {}", idempotencyKey);
                return;
            }
        }

        if (mailDto == null || recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("[EMAIL-EMERGENCY-FAILED] Failed to send emergency request email: missing or blank recipient email for Request #{}, Donor #{}",
                    reqId != null ? reqId : "N/A", donorId != null ? donorId : "N/A");
            return;
        }

        long startTime = System.currentTimeMillis();
        String maskedRecipient = maskEmail(recipientEmail);
        try {
            log.info("[EMAIL] Sending emergency notification | Recipient: {} | Request #{}, Donor #{} | SMTP configured: {}",
                    maskedRecipient, reqId != null ? reqId : "N/A", donorId != null ? donorId : "N/A", isSmtpPasswordConfigured());

            String htmlTemplate = getEmergencyHtmlTemplate();
            if (htmlTemplate == null || htmlTemplate.isBlank()) {
                log.error("[EMAIL] Emergency notification delivery failed: emergency HTML template not available | Recipient: {}", maskedRecipient);
                recordEmailNotification(reqId, donorId, recipientEmail, false, 0, "Emergency HTML template missing");
                return;
            }

            String formattedBloodGroup = mailDto.getBloodGroup() != null ?
                    mailDto.getBloodGroup().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "Emergency";

            String locationStr = (mailDto.getCity() != null ? mailDto.getCity() : "") +
                    (mailDto.getState() != null && !mailDto.getState().isBlank() ? ", " + mailDto.getState() : "");

            String htmlBody = htmlTemplate
                    .replace("{donorName}", donorName)
                    .replace("{hospitalName}", mailDto.getHospitalName() != null ? mailDto.getHospitalName() : "BloodBridge Partner Hospital")
                    .replace("{bloodGroup}", formattedBloodGroup)
                    .replace("{unitsRequired}", mailDto.getUnitsRequired() != null ? String.valueOf(mailDto.getUnitsRequired()) : "1")
                    .replace("{urgencyLevel}", mailDto.getUrgencyLevel() != null ? mailDto.getUrgencyLevel() : "HIGH")
                    .replace("{location}", !locationStr.isBlank() ? locationStr : "Local Region")
                    .replace("{hospitalAddress}", mailDto.getHospitalAddress() != null ? mailDto.getHospitalAddress() : "See App Dashboard")
                    .replace("{requiredByDate}", mailDto.getRequiredByDate() != null ? mailDto.getRequiredByDate() : "Immediate")
                    .replace("{reason}", mailDto.getReason() != null ? mailDto.getReason() : "Emergency Requirement")
                    .replace("{loginUrl}", mailDto.getLoginUrl() != null ? mailDto.getLoginUrl() : (frontendUrl + "/donor/requests"));

            String senderEmail = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "noreply@bloodbridge.com";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setFrom(senderEmail, "BloodBridge Team");
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            long durationMs = System.currentTimeMillis() - startTime;

            log.info("[EMAIL] Emergency alert email sent successfully | Recipient: {} | Duration: {} ms",
                    maskedRecipient, durationMs);

            recordEmailNotification(reqId, donorId, recipientEmail, true, durationMs, null);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[EMAIL] Emergency notification delivery failed: {} ({}) | Recipient: {} | Request #{}, Donor #{}",
                    e.getClass().getSimpleName(), e.getMessage(), maskedRecipient, reqId != null ? reqId : "N/A", donorId != null ? donorId : "N/A");

            recordEmailNotification(reqId, donorId, recipientEmail, false, durationMs, e.getMessage());
            if (reqId != null && donorId != null) {
                processedEmailKeys.remove(reqId + "_" + donorId + "_EMERGENCY_ALERT");
            }
        }
    }

    private void recordEmailNotification(Long reqId, Long donorId, String email, boolean success, long durationMs, String failureReason) {
        if (reqId == null || donorId == null || emailNotificationRepository == null) {
            return;
        }
        try {
            com.bloodbridge.entity.EmailNotification notif = emailNotificationRepository.findByEmergencyRequestIdAndDonorId(reqId, donorId)
                    .orElseGet(() -> com.bloodbridge.entity.EmailNotification.builder()
                            .emergencyRequestId(reqId)
                            .donorId(donorId)
                            .email(email)
                            .deliveryAttempts(0)
                            .build());

            notif.setStatus(success ? com.bloodbridge.enums.EmailDeliveryStatus.SENT : com.bloodbridge.enums.EmailDeliveryStatus.FAILED);
            notif.setDeliveryAttempts(notif.getDeliveryAttempts() + 1);
            if (success) {
                notif.setSentAt(java.time.LocalDateTime.now());
                notif.setSmtpResponseTimeMs(durationMs);
                notif.setFailureReason(null);
            } else {
                notif.setFailureReason(failureReason != null && failureReason.length() > 950 ? failureReason.substring(0, 950) : failureReason);
            }
            emailNotificationRepository.save(notif);
        } catch (Exception ex) {
            log.warn("[EMAIL-DB-WARN] Could not record email notification status for Request #{}, Donor #{}: {}", reqId, donorId, ex.getMessage());
        }
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendHospitalApproval(String toEmail, String hospitalName) {
        log.info("[EMAIL] Sending hospital approval notification | Recipient: {}", maskEmail(toEmail));
        String subject = "BloodBridge - Hospital Account Approved!";
        String content = String.format(
                "Dear Administrator,\n\n" +
                "We are pleased to inform you that your hospital account for '%s' has been approved and verified by the BloodBridge team.\n\n" +
                "You may now log in to manage blood inventory and publish emergency blood requests.\n\n" +
                "Thank you,\nBloodBridge Team",
                hospitalName
        );
        sendEmail(toEmail, subject, content);
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendDonationConfirmation(String toEmail, String donorName, String hospitalName, String bloodGroup) {
        log.info("[EMAIL] Sending donation confirmation notification | Recipient: {}", maskEmail(toEmail));
        String subject = "BloodBridge - Thank You for Your Lifesaving Donation!";
        String content = String.format(
                "Dear %s,\n\n" +
                "Thank you for donating %s blood at %s. Your contribution is vital in saving lives.\n\n" +
                "Best regards,\nBloodBridge Team",
                donorName, bloodGroup, hospitalName
        );
        sendEmail(toEmail, subject, content);
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendEmail(String to, String subject, String content) {
        String maskedRecipient = maskEmail(to);
        log.info("[EMAIL] Initiating async simple email dispatch | Recipient: {} | Subject: {} | Thread: {}",
                maskedRecipient, subject, Thread.currentThread().getName());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom((fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "noreply@bloodbridge.com");
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("[EMAIL] Simple email dispatched successfully | Recipient: {}", maskedRecipient);
        } catch (Exception e) {
            log.error("[EMAIL] Email delivery failed: {} ({}) | Recipient: {}",
                    e.getClass().getSimpleName(), e.getMessage(), maskedRecipient);
        }
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendDonationConfirmationEmail(String to, String donorName, String patientName, String hospitalName) {
        String subject = "Blood Bridge - Donation Details Confirmed";
        String content = String.format(
                "Dear user,\n\n" +
                "This email is to confirm the donation schedule:\n" +
                "- Donor Name: %s\n" +
                "- Patient Name: %s\n" +
                "- Hospital: %s\n\n" +
                "Please coordinate with the hospital for donation execution.\n\n" +
                "Best regards,\nBlood Bridge Team",
                donorName, patientName, hospitalName
        );
        sendEmail(to, subject, content);
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendBloodRequestEmail(String to, String bloodGroup, Integer unitsRequired) {
        String subject = "Blood Bridge - New Blood Request Assigned";
        String content = String.format(
                "Dear Hospital Administrator,\n\n" +
                "A new blood request has been assigned to your hospital needing:\n" +
                "- Blood Group: %s\n" +
                "- Units Required: %d\n\n" +
                "Please review the request details in the application dashboard.\n\n" +
                "Best regards,\nBlood Bridge Team",
                bloodGroup, unitsRequired
        );
        sendEmail(to, subject, content);
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendMatchNotificationEmail(String to, String bloodGroupNeeded, String hospitalName) {
        String subject = "Blood Bridge - Eligible Blood Request Match";
        String content = String.format(
                "Dear Donor,\n\n" +
                "You have been identified as an eligible matched donor for a verified request at:\n" +
                "- Hospital: %s\n" +
                "- Blood Group Needed: %s\n\n" +
                "Please log in to your account to review and accept/decline the donation request.\n\n" +
                "Best regards,\nBlood Bridge Team",
                hospitalName, bloodGroupNeeded
        );
        sendEmail(to, subject, content);
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendDonationCertificateEmail(
            String toEmail,
            String donorName,
            String hospitalName,
            String bloodGroup,
            Integer units,
            String donationDate,
            String certificateId,
            byte[] pdfBytes
    ) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[EMAIL] Recipient email is null or blank for Certificate ID {}", certificateId);
            return;
        }

        // Idempotency check: prevent sending duplicate certificate emails for the same certificate ID
        if (certificateId != null && !certificateId.isBlank()) {
            String idempotencyKey = certificateId + "_CERTIFICATE_EMAIL";
            if (!processedEmailKeys.add(idempotencyKey)) {
                log.info("[EMAIL] Duplicate certificate email suppressed for key: {}", idempotencyKey);
                return;
            }
        }

        String maskedRecipient = maskEmail(toEmail);
        log.info("[EMAIL] Sending donation certificate email | Recipient: {} | Certificate ID: {}",
                maskedRecipient, certificateId);

        String subject = "BloodBridge Donation Certificate";
        String formattedBg = bloodGroup != null ? bloodGroup.replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "N/A";
        String displayDonor = donorName != null ? donorName : "Valued Donor";
        String displayHospital = hospitalName != null ? hospitalName : "Partner Hospital";
        String displayCertId = certificateId != null ? certificateId : "CERT-BB";

        String content = String.format(
                "Dear %s,\n\n" +
                "Your blood donation at %s has been successfully recorded as COMPLETED.\n\n" +
                "Donation Summary:\n" +
                "- Donor Name: %s\n" +
                "- Hospital Name: %s\n" +
                "- Blood Group: %s\n" +
                "- Units Donated: %d\n" +
                "- Donation Date: %s\n" +
                "- Certificate ID: %s\n\n" +
                "Your official BloodBridge donation certificate is attached to this email. Thank you for saving lives!\n\n" +
                "Best regards,\nBloodBridge Team",
                displayDonor, displayHospital, displayDonor, displayHospital, formattedBg,
                units != null ? units : 1, donationDate != null ? donationDate : "Recent", displayCertId
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setFrom((fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "noreply@bloodbridge.com");
            helper.setSubject(subject);
            helper.setText(content);

            if (pdfBytes != null && pdfBytes.length > 0) {
                String filename = "BloodBridge_Certificate_" + (certificateId != null ? certificateId : "Donation") + ".pdf";
                helper.addAttachment(filename, new org.springframework.core.io.ByteArrayResource(pdfBytes));
            }

            mailSender.send(message);
            log.info("[EMAIL] Certificate PDF email successfully delivered | Recipient: {} | Certificate ID: {}",
                    maskedRecipient, certificateId);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to deliver donation certificate email: {} ({}) | Recipient: {} | Certificate ID: {}",
                    e.getClass().getSimpleName(), e.getMessage(), maskedRecipient, certificateId);
        }
    }

    @Override
    @Async("emergencyEmailExecutor")
    public void sendDonorAcceptanceEmailToHospital(
            String toHospitalEmail,
            String hospitalName,
            String donorName,
            String bloodGroup,
            Long requestId,
            Integer unitsRequired,
            Double distanceKm,
            String acceptedAtStr
    ) {
        if (toHospitalEmail == null || toHospitalEmail.isBlank()) {
            log.error("[EMAIL] Donor acceptance notification failed: Missing hospital email address | Request ID: #{}", requestId);
            return;
        }

        String idempotencyKey = requestId + "_" + (donorName != null ? donorName : "DONOR") + "_ACCEPTANCE";
        if (!processedEmailKeys.add(idempotencyKey)) {
            log.info("[EMAIL] Duplicate acceptance email ignored for Key: {}", idempotencyKey);
            return;
        }

        String maskedRecipient = maskEmail(toHospitalEmail);
        log.info("[EMAIL] Sending donor acceptance notification | Recipient: {} | Request ID: #{}, Donor: {}, Hospital: {}",
                maskedRecipient, requestId, donorName, hospitalName);

        try {
            String formattedBloodGroup = bloodGroup != null ? bloodGroup.replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "N/A";
            String subject = "Donor Accepted Your Emergency Blood Request #" + requestId + " - BloodBridge";

            String htmlBody = "<html><body style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6;\">" +
                    "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
                    "<h2 style=\"color: #e53935;\">🩸 Donor Accepted Emergency Blood Request</h2>" +
                    "<p>Hello <strong>" + (hospitalName != null ? hospitalName : "Hospital Administrator") + "</strong>,</p>" +
                    "<p>A registered donor has accepted your emergency blood request on BloodBridge.</p>" +
                    "<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0;\">" +
                    "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Request ID:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">#" + requestId + "</td></tr>" +
                    "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Donor Name:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">" + (donorName != null ? donorName : "Valued Donor") + "</td></tr>" +
                    "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Blood Group:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">" + formattedBloodGroup + "</td></tr>" +
                    "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Units Required:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">" + (unitsRequired != null ? unitsRequired : 1) + "</td></tr>" +
                    "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Distance:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">" + (distanceKm != null ? String.format("%.1f KM", distanceKm) : "Local Region") + "</td></tr>" +
                    "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Status:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><span style=\"color: #2e7d32; font-weight: bold;\">ACCEPTED</span></td></tr>" +
                    "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Accepted At:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">" + (acceptedAtStr != null ? acceptedAtStr : "Just now") + "</td></tr>" +
                    "</table>" +
                    "<p style=\"margin-top: 25px;\"><a href=\"" + frontendUrl + "/hospital/dashboard\" style=\"background-color: #e53935; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold;\">Open Hospital Dashboard</a></p>" +
                    "<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">" +
                    "<p style=\"font-size: 12px; color: #777;\">BloodBridge Emergency Blood Network System Notification</p>" +
                    "</div></body></html>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String senderEmail = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "noreply@bloodbridge.com";
            helper.setTo(toHospitalEmail);
            helper.setSubject(subject);
            helper.setFrom(senderEmail, "BloodBridge Network");
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

            log.info("[EMAIL] Donor acceptance email sent successfully | Recipient: {} | Request ID: #{}",
                    maskedRecipient, requestId);
        } catch (Exception e) {
            log.error("[EMAIL] Donor acceptance email delivery failed: {} ({}) | Recipient: {} | Request ID: #{}",
                    e.getClass().getSimpleName(), e.getMessage(), maskedRecipient, requestId);
            processedEmailKeys.remove(idempotencyKey);
        }
    }
}
