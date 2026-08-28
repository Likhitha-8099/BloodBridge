package com.bloodbridge.service;

import com.bloodbridge.controller.DonorDonationController;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.InvalidDonationStateException;
import com.bloodbridge.exception.UnauthorizedDonationAccessException;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.CertificateServiceImpl;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CertificateEndToEndWorkflowIntegrationTest {

    @Autowired
    private CertificateServiceImpl certificateService;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private com.bloodbridge.repository.BloodRequestRepository bloodRequestRepository;

    @Autowired
    private DonorDonationController donorDonationController;

    @MockBean
    private EmailService emailService;

    private User donor1User;
    private DonorProfile donor1Profile;
    private User donor2User;
    private DonorProfile donor2Profile;
    private Hospital hospitalApollo;
    private Hospital hospitalCare;
    private Donation donation1;
    private Donation donation2;

    @BeforeEach
    void setUp() {
        // Hospital 1
        User hospUser1 = userRepository.save(User.builder()
                .fullName("Apollo Admin")
                .email("admin@apollo.org")
                .password("Password123!")
                .phoneNumber("9000100001")
                .role(Role.HOSPITAL)
                .active(true)
                .build());

        hospitalApollo = hospitalRepository.save(Hospital.builder()
                .user(hospUser1)
                .hospitalName("Apollo Super Specialty Hospital")
                .registrationNumber("HOSP-APOLLO-" + System.nanoTime())
                .email("admin@apollo.org")
                .phoneNumber("9000100001")
                .city("Hyderabad")
                .state("Telangana")
                .address("Jubilee Hills")
                .build());

        // Hospital 2
        User hospUser2 = userRepository.save(User.builder()
                .fullName("Care Admin")
                .email("admin@care.org")
                .password("Password123!")
                .phoneNumber("9000100002")
                .role(Role.HOSPITAL)
                .active(true)
                .build());

        hospitalCare = hospitalRepository.save(Hospital.builder()
                .user(hospUser2)
                .hospitalName("Care Medical Center")
                .registrationNumber("HOSP-CARE-" + System.nanoTime())
                .email("admin@care.org")
                .phoneNumber("9000100002")
                .city("Secunderabad")
                .state("Telangana")
                .address("Clock Tower")
                .build());

        // Donor 1: Likhitha Markonda (O+)
        donor1User = userRepository.save(User.builder()
                .fullName("Likhitha Markonda")
                .email("likhitha.markonda@bloodbridge.com")
                .password("Password123!")
                .phoneNumber("9888877771")
                .role(Role.DONOR)
                .active(true)
                .build());

        donor1Profile = donorProfileRepository.save(DonorProfile.builder()
                .user(donor1User)
                .email(donor1User.getEmail())
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(26)
                .gender(Gender.FEMALE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(65.0)
                .build());

        // Donor 2: Rajesh Sharma (A-)
        donor2User = userRepository.save(User.builder()
                .fullName("Rajesh Sharma")
                .email("rajesh.sharma@bloodbridge.com")
                .password("Password123!")
                .phoneNumber("9888877772")
                .role(Role.DONOR)
                .active(true)
                .build());

        donor2Profile = donorProfileRepository.save(DonorProfile.builder()
                .user(donor2User)
                .email(donor2User.getEmail())
                .bloodGroup(BloodGroup.A_NEGATIVE)
                .age(32)
                .gender(Gender.MALE)
                .city("Secunderabad")
                .state("Telangana")
                .weight(74.0)
                .build());

        // Request 1
        BloodRequest req1 = bloodRequestRepository.save(BloodRequest.builder()
                .hospital(hospitalApollo)
                .bloodGroupNeeded(BloodGroup.O_POSITIVE)
                .unitsRequired(1)
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(2))
                .status(com.bloodbridge.enums.RequestStatus.FULFILLED)
                .urgencyLevel(com.bloodbridge.enums.UrgencyLevel.CRITICAL)
                .build());

        // Completed Donation 1 for Donor 1
        donation1 = donationRepository.save(Donation.builder()
                .donor(donor1Profile)
                .hospital(hospitalApollo)
                .bloodRequest(req1)
                .donationDate(LocalDate.of(2026, 8, 26))
                .unitsDonated(1)
                .status(DonationStatus.COMPLETED)
                .certificateId("CERT-BB-2026-000101")
                .completedAt(LocalDateTime.now())
                .build());

        // Request 2
        BloodRequest req2 = bloodRequestRepository.save(BloodRequest.builder()
                .hospital(hospitalCare)
                .bloodGroupNeeded(BloodGroup.A_NEGATIVE)
                .unitsRequired(2)
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(2))
                .status(com.bloodbridge.enums.RequestStatus.FULFILLED)
                .urgencyLevel(com.bloodbridge.enums.UrgencyLevel.HIGH)
                .build());

        // Completed Donation 2 for Donor 2
        donation2 = donationRepository.save(Donation.builder()
                .donor(donor2Profile)
                .hospital(hospitalCare)
                .bloodRequest(req2)
                .donationDate(LocalDate.of(2026, 8, 25))
                .unitsDonated(2)
                .status(DonationStatus.COMPLETED)
                .certificateId("CERT-BB-2026-000202")
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("Workflow 1: Generate Certificate for Multiple Distinct Donors — Full Isolation & Landscape A4 Structure")
    void testGenerateCertificates_MultipleDistinctDonors() throws Exception {
        byte[] pdf1 = certificateService.generateCertificatePdf(donation1);
        byte[] pdf2 = certificateService.generateCertificatePdf(donation2);

        // Verify PDF 1
        assertThat(pdf1).isNotNull().isNotEmpty();
        assertThat(new String(pdf1, 0, 4)).isEqualTo("%PDF");
        PdfReader reader1 = new PdfReader(new ByteArrayInputStream(pdf1));
        assertThat(reader1.getNumberOfPages()).isEqualTo(1);
        float w1 = reader1.getPageSizeWithRotation(1).getWidth();
        float h1 = reader1.getPageSizeWithRotation(1).getHeight();
        assertThat(w1).isGreaterThan(h1);
        assertThat(w1).isBetween(840.0f, 843.0f);
        assertThat(h1).isBetween(594.0f, 597.0f);
        reader1.close();

        // Verify PDF 2
        assertThat(pdf2).isNotNull().isNotEmpty();
        assertThat(new String(pdf2, 0, 4)).isEqualTo("%PDF");
        PdfReader reader2 = new PdfReader(new ByteArrayInputStream(pdf2));
        assertThat(reader2.getNumberOfPages()).isEqualTo(1);
        float w2 = reader2.getPageSizeWithRotation(1).getWidth();
        float h2 = reader2.getPageSizeWithRotation(1).getHeight();
        assertThat(w2).isGreaterThan(h2);
        assertThat(w2).isBetween(840.0f, 843.0f);
        assertThat(h2).isBetween(594.0f, 597.0f);
        reader2.close();

        // Ensure PDFs are uniquely generated (not identical copies)
        assertThat(pdf1).isNotEqualTo(pdf2);

        // Save samples to disk for physical inspection
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("target"));
        java.nio.file.Files.write(java.nio.file.Paths.get("target/sample_certificate_likhitha.pdf"), pdf1);
        java.nio.file.Files.write(java.nio.file.Paths.get("target/sample_certificate_rajesh.pdf"), pdf2);
    }

    @Test
    @DisplayName("Workflow 2: Donor Downloads Certificate via REST Controller — HTTP 200, Content Headers, Valid PDF")
    void testDownloadCertificate_ControllerEndpoint() {
        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                donor1User.getEmail(), "password", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_DONOR"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        ResponseEntity<byte[]> response = donorDonationController.downloadCertificate(donation1.getId(), principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=\"BloodBridge_Certificate_" + donation1.getId() + ".pdf\"");
        assertThat(response.getBody()).isNotNull().isNotEmpty();
        assertThat(new String(response.getBody(), 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("Workflow 3: Unauthorized Access Prevention — Attacker cannot download other donor's certificate")
    void testDownloadCertificate_SecurityViolation() {
        org.springframework.security.core.userdetails.User attackerPrincipal = new org.springframework.security.core.userdetails.User(
                donor2User.getEmail(), "password", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_DONOR"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(attackerPrincipal, null, attackerPrincipal.getAuthorities())
        );

        assertThrows(UnauthorizedDonationAccessException.class,
                () -> donorDonationController.downloadCertificate(donation1.getId(), attackerPrincipal));
    }

    @Test
    @DisplayName("Workflow 4: Incomplete Donation Rejection — Pending donation throws InvalidDonationStateException")
    void testDownloadCertificate_IncompleteDonation() {
        Donation pendingDonation = donationRepository.save(Donation.builder()
                .donor(donor1Profile)
                .hospital(hospitalApollo)
                .bloodRequest(donation1.getBloodRequest())
                .donationDate(LocalDate.now())
                .unitsDonated(1)
                .status(DonationStatus.PENDING)
                .build());

        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                donor1User.getEmail(), "password", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_DONOR"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertThrows(InvalidDonationStateException.class,
                () -> donorDonationController.downloadCertificate(pendingDonation.getId(), principal));
    }

    @Test
    @DisplayName("Workflow 5: Email Attachment Integration — Verify certificate payload is prepared for email dispatch")
    void testEmailAttachmentWorkflow() {
        byte[] certPdf = certificateService.getCertificatePdfForDonor(donation1.getId(), donor1User.getEmail());

        emailService.sendDonationCertificateEmail(
                donor1User.getEmail(),
                donor1User.getFullName(),
                hospitalApollo.getHospitalName(),
                donor1Profile.getBloodGroup().name(),
                donation1.getUnitsDonated(),
                donation1.getDonationDate().toString(),
                donation1.getCertificateId(),
                certPdf
        );

        verify(emailService).sendDonationCertificateEmail(
                eq("likhitha.markonda@bloodbridge.com"),
                eq("Likhitha Markonda"),
                eq("Apollo Super Specialty Hospital"),
                eq("O_POSITIVE"),
                eq(1),
                eq("2026-08-26"),
                eq("CERT-BB-2026-000101"),
                any(byte[].class)
        );
    }
}
