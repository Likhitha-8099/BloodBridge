package com.bloodbridge.controller;

import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.service.CertificateService;
import com.bloodbridge.service.DonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Secure donor-facing REST controller for Module 2: Donation History & Certificate Generation.
 * <p>
 * All endpoints require authentication and enforce ownership so that one donor
 * cannot access another donor's donation records or certificates by changing an ID.
 * <p>
 * Base path {@code /api/v1/donor/donations} is protected by the security config rule
 * that restricts {@code /api/v1/donor/**} to ROLE_DONOR and ROLE_ADMIN.
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/donor/donations", "/api/donor/donations"})
@RequiredArgsConstructor
public class DonorDonationController {

    private final DonationService donationService;
    private final CertificateService certificateService;

    /**
     * Returns the authenticated donor's own donation history.
     * <p>
     * Requirements satisfied:
     * <ul>
     *   <li>Req-1: Returns donation ID, date, hospital, blood group, units, status, certificate ID.</li>
     *   <li>Req-6: Only returns donations belonging to the authenticated donor (ownership enforced
     *       inside {@link DonationService#getMyDonations(String)}).</li>
     * </ul>
     *
     * @param userDetails the authenticated user injected by Spring Security
     * @return list of {@link DonationSummaryResponse} for the caller's donations only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<List<DonationSummaryResponse>> getMyDonations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("Fetching donation history for authenticated user: {}", userDetails.getUsername());
        List<DonationSummaryResponse> donations = donationService.getMyDonations(userDetails.getUsername());
        return ResponseEntity.ok(donations);
    }

    /**
     * Downloads a professional PDF blood-donation certificate for the specified donation.
     * <p>
     * Requirements satisfied:
     * <ul>
     *   <li>Req-3 &amp; 4: Certificate generated from backend data; endpoint secured.</li>
     *   <li>Req-6: Ownership enforced inside {@link CertificateService#getCertificatePdfForDonor} —
     *       a donor who tries another donor's {@code donationId} receives HTTP 403.</li>
     *   <li>Req-2: Certificate is only generated when donation status is {@code COMPLETED}.
     *       Any other status (PENDING, REJECTED, CANCELLED, ACCEPTED, CONFIRMED) throws
     *       {@code InvalidDonationStateException} -&gt; HTTP 409.</li>
     * </ul>
     *
     * @param donationId  the ID of the completed donation
     * @param userDetails the authenticated user injected by Spring Security
     * @return PDF byte array as {@code application/pdf} attachment
     */
    @GetMapping("/{donationId}/certificate")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable Long donationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Certificate download requested for donation #{} by user {}",
                donationId, userDetails.getUsername());

        byte[] pdfBytes = certificateService.getCertificatePdfForDonor(donationId, userDetails.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"BloodBridge_Certificate_" + donationId + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(pdfBytes);
    }
}
