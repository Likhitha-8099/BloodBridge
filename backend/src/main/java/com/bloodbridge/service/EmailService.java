package com.bloodbridge.service;

import com.bloodbridge.dto.EmergencyMailDto;

/**
 * Service interface defining email dispatch capabilities for BloodBridge.
 */
public interface EmailService {

    /**
     * Dispatches a stylized HTML emergency alert email asynchronously to a matched donor.
     *
     * @param mailDto details of the emergency request and recipient donor
     */
    void sendEmergencyAlert(EmergencyMailDto mailDto);

    /**
     * Dispatches a hospital approval notification email.
     *
     * @param toEmail      recipient hospital email address
     * @param hospitalName approved hospital name
     */
    void sendHospitalApproval(String toEmail, String hospitalName);

    /**
     * Dispatches a donation confirmation email to donor.
     *
     * @param toEmail      recipient donor email address
     * @param donorName    donor full name
     * @param hospitalName hospital name hosting donation
     * @param bloodGroup   donated blood group
     */
    void sendDonationConfirmation(String toEmail, String donorName, String hospitalName, String bloodGroup);

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

    /**
     * Dispatches an email with the completed blood donation certificate PDF as an attachment.
     *
     * @param toEmail      recipient donor email address
     * @param donorName    donor full name
     * @param hospitalName hospital name hosting donation
     * @param bloodGroup   donated blood group
     * @param units        units donated
     * @param donationDate completion date
     * @param certificateId unique certificate verification ID
     * @param pdfBytes     PDF byte array content
     */
    void sendDonationCertificateEmail(String toEmail, String donorName, String hospitalName, String bloodGroup, Integer units, String donationDate, String certificateId, byte[] pdfBytes);

    /**
     * Dispatches a donor acceptance notification email asynchronously to a hospital administrator.
     *
     * @param toHospitalEmail recipient hospital email address
     * @param hospitalName    hospital name
     * @param donorName       donor full name
     * @param bloodGroup      accepted blood group
     * @param requestId       emergency request ID
     * @param unitsRequired   units of blood needed
     * @param distanceKm      distance in kilometers
     * @param acceptedAtStr   acceptance timestamp string
     */
    void sendDonorAcceptanceEmailToHospital(String toHospitalEmail, String hospitalName, String donorName, String bloodGroup, Long requestId, Integer unitsRequired, Double distanceKm, String acceptedAtStr);
}
