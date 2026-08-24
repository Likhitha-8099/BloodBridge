package com.bloodbridge.service;

/**
 * Transport interface decoupling high-level BloodBridge email workflows
 * from the underlying delivery mechanism (SMTP vs HTTPS REST API).
 */
public interface EmailTransportService {

    /**
     * Dispatches an HTML email with optional binary attachment (e.g. PDF donation certificate).
     *
     * @param to                 recipient email address
     * @param subject            email subject
     * @param htmlContent        rendered HTML content
     * @param fromName           display name of sender
     * @param attachmentBytes    optional raw attachment bytes (nullable)
     * @param attachmentFilename optional attachment filename (nullable)
     */
    void sendHtmlEmail(String to, String subject, String htmlContent, String fromName, byte[] attachmentBytes, String attachmentFilename);

    /**
     * Dispatches a plain text email.
     *
     * @param to          recipient email address
     * @param subject     email subject
     * @param textContent plain text content
     * @param fromName    display name of sender
     */
    void sendSimpleEmail(String to, String subject, String textContent, String fromName);

    /**
     * Returns true if the transport has valid credentials configured.
     */
    boolean isConfigured();

    /**
     * Returns the name of the transport provider (e.g. "RESEND_HTTPS", "BREVO_HTTPS", "GMAIL_SMTP").
     */
    String getProviderName();
}
