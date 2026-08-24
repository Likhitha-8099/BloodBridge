package com.bloodbridge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Spring Mail Configuration establishing JavaMailSender bean with property bindings.
 * Supports standard MAIL_* and SPRING_MAIL_* environment variables with Gmail STARTTLS and App Password sanitization.
 */
@Configuration
@Slf4j
public class MailConfig {

    @Value("${SPRING_MAIL_HOST:${MAIL_HOST:${spring.mail.host:smtp.gmail.com}}}")
    private String host;

    @Value("${SPRING_MAIL_PORT:${MAIL_PORT:${spring.mail.port:587}}}")
    private int port;

    @Value("${SPRING_MAIL_USERNAME:${MAIL_USERNAME:${spring.mail.username:your_email@gmail.com}}}")
    private String username;

    @Value("${SPRING_MAIL_PASSWORD:${MAIL_PASSWORD:${spring.mail.password:}}}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String starttlsEnable;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host != null && !host.isBlank() ? host.trim() : "smtp.gmail.com");
        mailSender.setPort(port > 0 ? port : 587);
        mailSender.setUsername(username != null && !username.isBlank() ? username.trim() : "your_email@gmail.com");

        // Sanitize password: trim whitespace and strip space separators often copied from Google App Password UI
        String sanitizedPassword = password != null ? password.trim().replace(" ", "") : "";
        mailSender.setPassword(sanitizedPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        boolean pwdSet = !sanitizedPassword.isEmpty() && !"your-gmail-app-password-here".equals(sanitizedPassword);
        log.info("[MAIL-CONFIG] JavaMailSender bean initialized successfully:");
        log.info(" - Host               : {}", host);
        log.info(" - Port               : {}", port);
        log.info(" - Sender Email       : {}", username);
        log.info(" - Password Configured: {}", pwdSet);
        log.info(" - STARTTLS Enabled   : {}", starttlsEnable);

        return mailSender;
    }
}
