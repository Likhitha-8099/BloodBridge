package com.bloodbridge.service;

import com.bloodbridge.entity.Donation;

/**
 * Service interface for generating blood donation PDF certificates.
 */
public interface CertificateService {

    /**
     * Generates a professional PDF certificate for a completed blood donation.
     *
     * @param donation the completed donation entity
     * @return byte array containing PDF document data
     */
    byte[] generateCertificatePdf(Donation donation);

    /**
     * Retrieves or generates a PDF certificate for a given donation ID after validating ownership.
     *
     * @param donationId   the donation ID
     * @param userEmail the authenticated user's email
     * @return byte array containing PDF document data
     */
    byte[] getCertificatePdfForDonor(Long donationId, String userEmail);
}
