package com.bloodbridge.service;

/**
 * Service interface defining email dispatch capabilities.
 */
public interface EmailService {

    /**
     * Dispatches a raw text email asynchronously.
     *
     * @param to      the recipient's email address
     * @param subject the email subject
     * @param content the email body content
     */
    void sendEmail(String to, String subject, String content);

    /**
     * Sends a stylized donation confirmation email.
     *
     * @param to           the recipient's email address
     * @param donorName    the donor's full name
     * @param patientName  the patient's full name
     * @param hospitalName the hospital name hosting the donation
     */
    void sendDonationConfirmationEmail(String to, String donorName, String patientName, String hospitalName);

    /**
     * Sends a new blood request alert email to hospitals.
     *
     * @param to            the hospital's email address
     * @param bloodGroup    the blood group required
     * @param unitsRequired the units of blood needed
     */
    void sendBloodRequestEmail(String to, String bloodGroup, Integer unitsRequired);

    /**
     * Sends a match notification alert email to donors.
     *
     * @param to               the donor's email address
     * @param bloodGroupNeeded the blood group required
     * @param hospitalName     the hospital needing the blood
     */
    void sendMatchNotificationEmail(String to, String bloodGroupNeeded, String hospitalName);
}
