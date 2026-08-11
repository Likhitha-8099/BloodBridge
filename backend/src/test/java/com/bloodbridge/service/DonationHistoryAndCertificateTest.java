package com.bloodbridge.service;

import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.InvalidDonationStateException;
import com.bloodbridge.exception.UnauthorizedDonationAccessException;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.CertificateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Module 2: PDF Certificate Generation, Ownership Enforcement & Status Validation.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class DonationHistoryAndCertificateTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    private User ownerUser;
    private User otherUser;
    private DonorProfile ownerDonor;
    private Hospital hospital;
    private Donation completedDonation;
    private Donation pendingDonation;
    private Donation rejectedDonation;
    private Donation cancelledDonation;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder().id(100L).email("owner.donor@example.com").fullName("Alice Donor").role(Role.DONOR).build();
        otherUser = User.builder().id(200L).email("other.donor@example.com").fullName("Bob Attacker").role(Role.DONOR).build();

        ownerDonor = DonorProfile.builder().id(10L).user(ownerUser).bloodGroup(BloodGroup.O_POSITIVE).build();

        hospital = Hospital.builder().id(5L).hospitalName("City General Hospital").build();

        completedDonation = Donation.builder()
                .id(1L)
                .donor(ownerDonor)
                .hospital(hospital)
                .donationDate(LocalDate.now().minusDays(5))
                .unitsDonated(1)
                .status(DonationStatus.COMPLETED)
                .certificateId("CERT-BB-2026-000001")
                .build();

        pendingDonation = Donation.builder()
                .id(2L)
                .donor(ownerDonor)
                .hospital(hospital)
                .donationDate(LocalDate.now())
                .unitsDonated(1)
                .status(DonationStatus.ACCEPTED)
                .build();

        rejectedDonation = Donation.builder()
                .id(3L)
                .donor(ownerDonor)
                .hospital(hospital)
                .donationDate(LocalDate.now().minusDays(10))
                .unitsDonated(1)
                .status(DonationStatus.REJECTED)
                .build();

        cancelledDonation = Donation.builder()
                .id(4L)
                .donor(ownerDonor)
                .hospital(hospital)
                .donationDate(LocalDate.now().minusDays(12))
                .unitsDonated(1)
                .status(DonationStatus.CANCELLED)
                .build();
    }

    @Test
    @DisplayName("1. Certificate Generation: Creates valid PDF byte array starting with %PDF header")
    void testCertificatePdfGeneration() {
        byte[] pdfBytes = certificateService.generateCertificatePdf(completedDonation);

        assertThat(pdfBytes).isNotNull().isNotEmpty();
        String header = new String(pdfBytes, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("2. Owner Certificate Access: Owner can download their completed donation certificate")
    void testOwnerCanAccessCertificate() {
        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(donationRepository.findById(completedDonation.getId())).thenReturn(Optional.of(completedDonation));

        byte[] pdf = certificateService.getCertificatePdfForDonor(completedDonation.getId(), ownerUser.getEmail());

        assertThat(pdf).isNotNull().isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("3. Unauthorized Access: Attacker cannot access another donor's certificate")
    void testUnauthorizedCertificateAccessDenied() {
        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(donationRepository.findById(completedDonation.getId())).thenReturn(Optional.of(completedDonation));

        assertThrows(UnauthorizedDonationAccessException.class, () ->
                certificateService.getCertificatePdfForDonor(completedDonation.getId(), otherUser.getEmail())
        );
    }

    @Test
    @DisplayName("4. Pending/Accepted Donation: Certificate access denied")
    void testPendingDonationCertificateDenied() {
        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(donationRepository.findById(pendingDonation.getId())).thenReturn(Optional.of(pendingDonation));

        assertThrows(InvalidDonationStateException.class, () ->
                certificateService.getCertificatePdfForDonor(pendingDonation.getId(), ownerUser.getEmail())
        );
    }

    @Test
    @DisplayName("5. Rejected Donation: Certificate access denied")
    void testRejectedDonationCertificateDenied() {
        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(donationRepository.findById(rejectedDonation.getId())).thenReturn(Optional.of(rejectedDonation));

        assertThrows(InvalidDonationStateException.class, () ->
                certificateService.getCertificatePdfForDonor(rejectedDonation.getId(), ownerUser.getEmail())
        );
    }

    @Test
    @DisplayName("6. Cancelled Donation: Certificate access denied")
    void testCancelledDonationCertificateDenied() {
        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(donationRepository.findById(cancelledDonation.getId())).thenReturn(Optional.of(cancelledDonation));

        assertThrows(InvalidDonationStateException.class, () ->
                certificateService.getCertificatePdfForDonor(cancelledDonation.getId(), ownerUser.getEmail())
        );
    }

    @Test
    @DisplayName("7. Certificate ID Assignment: Auto-assigned on first access for completed donation without ID")
    void testCertificateIdAutoAssignedOnAccess() {
        Donation donationWithoutCertId = Donation.builder()
                .id(99L)
                .donor(ownerDonor)
                .hospital(hospital)
                .donationDate(LocalDate.of(2026, 1, 15))
                .unitsDonated(1)
                .status(DonationStatus.COMPLETED)
                .certificateId(null)
                .build();

        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(donationRepository.findById(99L)).thenReturn(Optional.of(donationWithoutCertId));
        when(donationRepository.save(donationWithoutCertId)).thenReturn(donationWithoutCertId);

        byte[] pdf = certificateService.getCertificatePdfForDonor(99L, ownerUser.getEmail());

        assertThat(pdf).isNotNull().isNotEmpty();
        assertThat(donationWithoutCertId.getCertificateId()).startsWith("CERT-BB-");
    }

    @Test
    @DisplayName("8. Invalid Donation ID: Throws DonationNotFoundException")
    void testInvalidDonationIdReturnsNotFound() {
        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(donationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.bloodbridge.exception.DonationNotFoundException.class, () ->
                certificateService.getCertificatePdfForDonor(999L, ownerUser.getEmail())
        );
    }
}
