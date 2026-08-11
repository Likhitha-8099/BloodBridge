package com.bloodbridge.service;

import com.bloodbridge.controller.DonorDonationController;
import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.exception.InvalidDonationStateException;
import com.bloodbridge.exception.UnauthorizedDonationAccessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DonorDonationController} — Module 2: Donation History & Certificate Generation.
 * Covers:
 * <ul>
 *   <li>Donation history retrieval for authenticated donor</li>
 *   <li>Donor ownership enforcement</li>
 *   <li>Certificate generation for completed donations</li>
 *   <li>Certificate denial for rejected, cancelled, pending, incomplete donations</li>
 *   <li>Unauthorized certificate access detection</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class DonorDonationControllerTest {

    @Mock
    private DonationService donationService;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private DonorDonationController controller;

    @Mock
    private UserDetails ownerPrincipal;

    @Mock
    private UserDetails attackerPrincipal;

    private DonationSummaryResponse completedSummary;
    private DonationSummaryResponse pendingSummary;
    private DonationSummaryResponse rejectedSummary;
    private DonationSummaryResponse cancelledSummary;
    private byte[] fakePdfBytes;

    @BeforeEach
    void setUp() {
        when(ownerPrincipal.getUsername()).thenReturn("owner@example.com");
        when(attackerPrincipal.getUsername()).thenReturn("attacker@example.com");

        fakePdfBytes = new byte[]{ '%', 'P', 'D', 'F', '-', '1', '.', '4' };

        completedSummary = DonationSummaryResponse.builder()
                .id(1L)
                .hospitalName("City Hospital")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .donationDate(LocalDate.now().minusDays(5))
                .unitsDonated(1)
                .status(DonationStatus.COMPLETED)
                .certificateId("CERT-BB-2026-000001")
                .certificateAvailable(true)
                .build();

        pendingSummary = DonationSummaryResponse.builder()
                .id(2L)
                .hospitalName("City Hospital")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .donationDate(LocalDate.now())
                .unitsDonated(1)
                .status(DonationStatus.ACCEPTED)
                .certificateAvailable(false)
                .build();

        rejectedSummary = DonationSummaryResponse.builder()
                .id(3L)
                .hospitalName("City Hospital")
                .bloodGroup(BloodGroup.A_POSITIVE)
                .donationDate(LocalDate.now().minusDays(10))
                .unitsDonated(1)
                .status(DonationStatus.REJECTED)
                .certificateAvailable(false)
                .build();

        cancelledSummary = DonationSummaryResponse.builder()
                .id(4L)
                .hospitalName("City Hospital")
                .bloodGroup(BloodGroup.A_POSITIVE)
                .donationDate(LocalDate.now().minusDays(12))
                .unitsDonated(1)
                .status(DonationStatus.CANCELLED)
                .certificateAvailable(false)
                .build();
    }

    // ─────────────────────── Donation History Tests ───────────────────────

    @Test
    @DisplayName("1. Donation History: Returns list of donations for authenticated donor")
    void testGetMyDonations_ReturnsHistory() {
        when(donationService.getMyDonations("owner@example.com"))
                .thenReturn(List.of(completedSummary, pendingSummary));

        ResponseEntity<List<DonationSummaryResponse>> response =
                controller.getMyDonations(ownerPrincipal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
        assertThat(response.getBody().get(0).getStatus()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(response.getBody().get(0).getCertificateAvailable()).isTrue();
    }

    @Test
    @DisplayName("2. Donation History: Returns empty list when no donations exist")
    void testGetMyDonations_EmptyList() {
        when(donationService.getMyDonations("owner@example.com")).thenReturn(List.of());

        ResponseEntity<List<DonationSummaryResponse>> response =
                controller.getMyDonations(ownerPrincipal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("3. Donation History Fields: Completed donation includes certificateAvailable=true")
    void testDonationSummaryFields_CompletedHasCertificate() {
        when(donationService.getMyDonations("owner@example.com"))
                .thenReturn(List.of(completedSummary));

        ResponseEntity<List<DonationSummaryResponse>> response =
                controller.getMyDonations(ownerPrincipal);

        DonationSummaryResponse dto = response.getBody().get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getHospitalName()).isEqualTo("City Hospital");
        assertThat(dto.getBloodGroup()).isEqualTo(BloodGroup.O_POSITIVE);
        assertThat(dto.getUnitsDonated()).isEqualTo(1);
        assertThat(dto.getCertificateId()).startsWith("CERT-BB-");
        assertThat(dto.getCertificateAvailable()).isTrue();
    }

    @Test
    @DisplayName("4. Donation History Fields: Rejected donation has certificateAvailable=false")
    void testDonationSummaryFields_RejectedNoCertificate() {
        when(donationService.getMyDonations("owner@example.com"))
                .thenReturn(List.of(rejectedSummary));

        ResponseEntity<List<DonationSummaryResponse>> response =
                controller.getMyDonations(ownerPrincipal);

        DonationSummaryResponse dto = response.getBody().get(0);
        assertThat(dto.getStatus()).isEqualTo(DonationStatus.REJECTED);
        assertThat(dto.getCertificateAvailable()).isFalse();
        assertThat(dto.getCertificateId()).isNull();
    }

    @Test
    @DisplayName("5. Donation History Fields: Cancelled donation has certificateAvailable=false")
    void testDonationSummaryFields_CancelledNoCertificate() {
        when(donationService.getMyDonations("owner@example.com"))
                .thenReturn(List.of(cancelledSummary));

        ResponseEntity<List<DonationSummaryResponse>> response =
                controller.getMyDonations(ownerPrincipal);

        DonationSummaryResponse dto = response.getBody().get(0);
        assertThat(dto.getStatus()).isEqualTo(DonationStatus.CANCELLED);
        assertThat(dto.getCertificateAvailable()).isFalse();
    }

    @Test
    @DisplayName("6. Donation History Fields: Pending/Accepted donation has certificateAvailable=false")
    void testDonationSummaryFields_PendingNoCertificate() {
        when(donationService.getMyDonations("owner@example.com"))
                .thenReturn(List.of(pendingSummary));

        ResponseEntity<List<DonationSummaryResponse>> response =
                controller.getMyDonations(ownerPrincipal);

        DonationSummaryResponse dto = response.getBody().get(0);
        assertThat(dto.getStatus()).isEqualTo(DonationStatus.ACCEPTED);
        assertThat(dto.getCertificateAvailable()).isFalse();
    }

    // ─────────────────────── Certificate Download Tests ──────────────────

    @Test
    @DisplayName("7. Certificate Download: Owner receives PDF for completed donation")
    void testDownloadCertificate_OwnerCompletedDonation() {
        when(certificateService.getCertificatePdfForDonor(1L, "owner@example.com"))
                .thenReturn(fakePdfBytes);

        ResponseEntity<byte[]> response = controller.downloadCertificate(1L, ownerPrincipal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();
        assertThat(new String(response.getBody(), 0, 4)).isEqualTo("%PDF");
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/pdf");
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .contains("BloodBridge_Certificate_1.pdf");
    }

    @Test
    @DisplayName("8. Certificate Access: Unauthorized attacker throws UnauthorizedDonationAccessException")
    void testDownloadCertificate_AttackerThrowsUnauthorized() {
        when(certificateService.getCertificatePdfForDonor(1L, "attacker@example.com"))
                .thenThrow(new UnauthorizedDonationAccessException(
                        "You are not authorized to view or download this donation certificate."));

        assertThrows(UnauthorizedDonationAccessException.class,
                () -> controller.downloadCertificate(1L, attackerPrincipal));
    }

    @Test
    @DisplayName("9. Certificate Generation: Rejected donation throws InvalidDonationStateException")
    void testDownloadCertificate_RejectedDonation() {
        when(certificateService.getCertificatePdfForDonor(3L, "owner@example.com"))
                .thenThrow(new InvalidDonationStateException(
                        "Certificates are only generated for COMPLETED blood donations."));

        assertThrows(InvalidDonationStateException.class,
                () -> controller.downloadCertificate(3L, ownerPrincipal));
    }

    @Test
    @DisplayName("10. Certificate Generation: Cancelled donation throws InvalidDonationStateException")
    void testDownloadCertificate_CancelledDonation() {
        when(certificateService.getCertificatePdfForDonor(4L, "owner@example.com"))
                .thenThrow(new InvalidDonationStateException(
                        "Certificates are only generated for COMPLETED blood donations."));

        assertThrows(InvalidDonationStateException.class,
                () -> controller.downloadCertificate(4L, ownerPrincipal));
    }

    @Test
    @DisplayName("11. Certificate Generation: Pending/Accepted donation throws InvalidDonationStateException")
    void testDownloadCertificate_PendingDonation() {
        when(certificateService.getCertificatePdfForDonor(2L, "owner@example.com"))
                .thenThrow(new InvalidDonationStateException(
                        "Certificates are only generated for COMPLETED blood donations."));

        assertThrows(InvalidDonationStateException.class,
                () -> controller.downloadCertificate(2L, ownerPrincipal));
    }

    @Test
    @DisplayName("12. Ownership: donationService.getMyDonations is called with the principal's email")
    void testOwnershipCheck_ServiceCalledWithCorrectEmail() {
        when(donationService.getMyDonations("owner@example.com")).thenReturn(List.of());

        controller.getMyDonations(ownerPrincipal);

        verify(donationService, times(1)).getMyDonations("owner@example.com");
        verify(donationService, never()).getMyDonations("attacker@example.com");
    }
}

