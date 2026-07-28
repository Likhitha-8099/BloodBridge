package com.bloodbridge.service.impl;

import com.bloodbridge.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service implementation for asynchronous email operations utilizing Spring Mail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendEmail(String to, String subject, String content) {
        log.info("Initiating async email send to: {}, subject: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Email dispatched successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to deliver email to {}: {}", to, e.getMessage());
            // Safe fallback console logger: do not throw to crash main transaction
        }
    }

    @Override
    @Async
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
    @Async
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
    @Async
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
}
