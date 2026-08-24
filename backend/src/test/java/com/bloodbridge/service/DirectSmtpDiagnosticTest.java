package com.bloodbridge.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class DirectSmtpDiagnosticTest {

    @Test
    public void testDirectSmtpConnection() {
        System.out.println("================================================================================");
        System.out.println("Starting Direct SMTP Diagnostic Test with Local .env Configuration...");

        String host = "smtp.gmail.com";
        int port = 465;
        String username = null;
        String password = null;

        try {
            Path envPath = Path.of(".env");
            if (!Files.exists(envPath)) {
                envPath = Path.of("backend/.env");
            }
            if (!Files.exists(envPath)) {
                envPath = Path.of("../backend/.env");
            }
            if (Files.exists(envPath)) {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("MAIL_PORT=")) {
                        try {
                            port = Integer.parseInt(trimmed.substring("MAIL_PORT=".length()).trim());
                        } catch (Exception ignored) {}
                    }
                    if (trimmed.startsWith("MAIL_USERNAME=")) {
                        username = trimmed.substring("MAIL_USERNAME=".length()).trim();
                    }
                    if (trimmed.startsWith("MAIL_PASSWORD=")) {
                        password = trimmed.substring("MAIL_PASSWORD=".length()).trim().replace(" ", "");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not read .env: " + e.getMessage());
        }

        if (username == null || password == null) {
            System.out.println("Skipping direct test - no credentials found in .env");
            return;
        }

        System.out.println("Target Server: " + host + ":" + port);
        System.out.println("Username: " + username.charAt(0) + "***@" + username.substring(username.indexOf('@') + 1));
        System.out.println("Password configured: " + (!password.isEmpty()));

        // Test Port 465 (SSL)
        testSmtpWithConfig(host, 465, username, password, true);

        // Test Port 587 (STARTTLS)
        testSmtpWithConfig(host, 587, username, password, false);
    }

    private void testSmtpWithConfig(String host, int port, String username, String password, boolean isSsl) {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Testing Connection to " + host + ":" + port + " (SSL=" + isSsl + ")");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        if (isSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.starttls.required", "false");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            props.put("mail.smtp.ssl.enable", "false");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "8000");
        props.put("mail.smtp.timeout", "8000");
        props.put("mail.smtp.writetimeout", "8000");

        try {
            mailSender.testConnection();
            System.out.println(">>> SUCCESS! mailSender.testConnection() SUCCEEDED on port " + port + "!");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(username, "BloodBridge Diagnostic");
            helper.setTo(username);
            helper.setSubject("🚨 BloodBridge Local Diagnostic Test Email (Port " + port + ")");
            helper.setText("<h1>Diagnostic Email</h1><p>SMTP connection & dispatch test succeeded on port " + port + ".</p>", true);

            mailSender.send(message);
            System.out.println(">>> SUCCESS! mailSender.send() SENT REAL EMAIL to " + username + " on port " + port + "!");
        } catch (Exception e) {
            System.err.println(">>> FAILED on port " + port + ": " + e.getClass().getName() + " - " + e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("    Caused by: " + cause.getClass().getName() + " - " + cause.getMessage());
            }
        }
    }
}
