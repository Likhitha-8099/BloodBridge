package com.bloodbridge.service;

import com.bloodbridge.dto.DonationStatusUpdateRequest;
import com.bloodbridge.dto.DonationResponse;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.DuplicateDonationException;
import com.bloodbridge.exception.HospitalNotFoundException;
import com.bloodbridge.exception.UnauthorizedDonationAccessException;
import com.bloodbridge.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SriSaiLikhithaDonationWorkflowTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private MatchResultRepository matchResultRepository;

    @Autowired
    private MatchedEmergencyDonorRepository matchedDonorRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private CertificateService certificateService;

    private User hospitalUser;
    private Hospital hospital;
    private User otherHospitalUser;
    private Hospital otherHospital;
    private User donorUser;
    private DonorProfile donorProfile;
    private User patientUser;
    private PatientProfile patientProfile;
    private BloodRequest bloodRequest;
    private MatchResult matchResult;
    private MatchedEmergencyDonor matchedDonor;
    private Donation donation;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // 1. Setup SriSai Multi-speciality Hospital
        hospitalUser = userRepository.save(User.builder()
                .fullName("SriSai Multi-speciality Hospital")
                .email("srisai@hospital.com")
                .password("Password123!")
                .phoneNumber("08099998888")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .build());

        hospital = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser)
                .hospitalName("SriSai Multi-speciality Hospital")
                .email(hospitalUser.getEmail())
                .phoneNumber("08099998888")
                .registrationNumber("REG-SRISAI-999")
                .address("123 Health Ave")
                .city("Hyderabad")
                .state("Telangana")
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // 2. Setup Other Hospital for security testing
        otherHospitalUser = userRepository.save(User.builder()
                .fullName("Other City Hospital")
                .email("other@hospital.com")
                .password("Password123!")
                .phoneNumber("08077776666")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .build());

        otherHospital = hospitalRepository.save(Hospital.builder()
                .user(otherHospitalUser)
                .hospitalName("Other City Hospital")
                .email(otherHospitalUser.getEmail())
                .phoneNumber("08077776666")
                .registrationNumber("REG-OTHER-111")
                .address("456 Other Way")
                .city("Hyderabad")
                .state("Telangana")
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // 3. Setup Donor Likhitha Markonda
        donorUser = userRepository.save(User.builder()
                .fullName("Likhitha Markonda")
                .email("likhitha@markonda.com")
                .password("Password123!")
                .phoneNumber("9123456789")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .build());

        donorProfile = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUser)
                .email(donorUser.getEmail())
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(28)
                .gender(Gender.FEMALE)
                .weight(62.0)
                .city("Hyderabad")
                .state("Telangana")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .totalDonations(2)
                .livesSaved(6)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .lastDonationDate(LocalDate.now().minusDays(120))
                .nextEligibleDate(LocalDate.now().minusDays(30))
                .build());

        // 4. Setup Patient Profile
        patientUser = userRepository.save(User.builder()
                .fullName("Emergency Patient")
                .email("patient@test.com")
                .password("Password123!")
                .phoneNumber("9988776655")
                .role(Role.PATIENT)
                .active(true)
                .emailVerified(true)
                .build());

        patientProfile = patientProfileRepository.save(PatientProfile.builder()
                .user(patientUser)
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(35)
                .gender(Gender.FEMALE)
                .emergencyContactName("Attendant")
                .emergencyContactNumber("9988776655")
                .city("Hyderabad")
                .state("Telangana")
                .build());

        // 5. Emergency Blood Request by SriSai Hospital
        bloodRequest = bloodRequestRepository.save(BloodRequest.builder()
                .hospital(hospital)
                .patient(patientProfile)
                .bloodGroupNeeded(BloodGroup.B_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(1))
                .status(RequestStatus.IN_PROGRESS)
                .build());

        // 6. Match Result
        matchResult = matchResultRepository.save(MatchResult.builder()
                .bloodRequest(bloodRequest)
                .donor(donorProfile)
                .matchScore(98.0)
                .compatibilityScore(98.0)
                .distanceKm(2.4)
                .matchedAt(LocalDateTime.now())
                .status(MatchStatus.ACCEPTED)
                .build());

        // 7. Matched Donor (Likhitha Markonda Matched & Accepted)
        matchedDonor = matchedDonorRepository.save(MatchedEmergencyDonor.builder()
                .bloodRequest(bloodRequest)
                .donor(donorProfile)
                .hospital(hospital)
                .status(MatchedEmergencyDonorStatus.ACCEPTED)
                .distanceKm(2.4)
                .matchingGroup("TIER_1")
                .acceptedAt(LocalDateTime.now())
                .build());

        // 8. Donation Record (Status: ACCEPTED)
        donation = donationRepository.save(Donation.builder()
                .donor(donorProfile)
                .patient(patientProfile)
                .bloodRequest(bloodRequest)
                .hospital(hospital)
                .matchResult(matchResult)
                .unitsDonated(2)
                .status(DonationStatus.ACCEPTED)
                .donationDate(LocalDate.now())
                .build());
    }

    private void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Bug Fix Verification: Hospital -> Blood Requests -> Assigned to My Hospital retrieves request & accepted donor Likhitha Markonda")
    void testHospitalBloodRequestsRetrievalAndCompletion() {
        // Step 1: Authenticate as SriSai Multi-speciality Hospital
        authenticateUser(hospitalUser);

        // Step 2: Fetch hospital blood requests for SriSai Hospital
        ApiResponse<List<BloodRequestResponse>> hospitalRequestsRes = hospitalService.getHospitalBloodRequests(hospitalUser.getEmail());
        assertThat(hospitalRequestsRes).isNotNull();
        assertThat(hospitalRequestsRes.isSuccess()).isTrue();

        List<BloodRequestResponse> requestsList = hospitalRequestsRes.getData();
        assertThat(requestsList).isNotEmpty();
        assertThat(requestsList.stream().anyMatch(r -> r.getId().equals(bloodRequest.getId()))).isTrue();

        BloodRequestResponse reqResponse = requestsList.stream()
                .filter(r -> r.getId().equals(bloodRequest.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(reqResponse.getHospitalName()).isEqualTo("SriSai Multi-speciality Hospital");

        // Security Test A: Unrelated Hospital cannot complete donation assigned to SriSai Hospital
        authenticateUser(otherHospitalUser);
        DonationStatusUpdateRequest updateReq = new DonationStatusUpdateRequest();
        updateReq.setRemarks("Physical donation completed at SriSai Hospital");

        assertThatThrownBy(() -> donationService.completeDonation(donation.getId(), updateReq))
                .isInstanceOf(UnauthorizedDonationAccessException.class);

        // Security Test B: Donor cannot complete their own donation directly
        authenticateUser(donorUser);
        assertThatThrownBy(() -> donationService.completeDonation(donation.getId(), updateReq))
                .isInstanceOf(HospitalNotFoundException.class);

        // Step 3: SriSai Multi-speciality Hospital confirms actual physical donation
        authenticateUser(hospitalUser);
        DonationResponse completedDonation = donationService.completeDonation(donation.getId(), updateReq);

        // Step 4: Assertions on Completed Donation
        assertThat(completedDonation).isNotNull();
        assertThat(completedDonation.getStatus()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(completedDonation.getCompletedAt()).isNotNull();
        assertThat(completedDonation.getCertificateId()).isNotNull();
        assertThat(completedDonation.getCertificateId()).startsWith("CERT-BB-2026-");

        // Step 5: Assertions on Donor Profile Updates for Likhitha Markonda
        DonorProfile updatedProfile = donorProfileRepository.findById(donorProfile.getId()).orElseThrow();
        assertThat(updatedProfile.getLastDonationDate()).isEqualTo(completedDonation.getCompletedAt().toLocalDate());
        assertThat(updatedProfile.getNextEligibleDate()).isEqualTo(completedDonation.getCompletedAt().toLocalDate().plusDays(90));
        assertThat(updatedProfile.getTotalDonations()).isEqualTo(3); // 2 + 1
        assertThat(updatedProfile.getLivesSaved()).isEqualTo(9);     // 6 + 3

        // Step 6: Certificate Availability Verification
        authenticateUser(donorUser);
        byte[] pdfBytes = certificateService.getCertificatePdfForDonor(donation.getId(), donorUser.getEmail());
        assertThat(pdfBytes).isNotNull().isNotEmpty();

        // Step 7: In-App Notification Verification for Likhitha Markonda
        List<Notification> notifications = notificationRepository.findUserNotifications(donorUser.getId());
        assertThat(notifications).isNotEmpty();

        // Step 8: Idempotency Protection Verification
        authenticateUser(hospitalUser);
        assertThatThrownBy(() -> donationService.completeDonation(donation.getId(), updateReq))
                .isInstanceOf(DuplicateDonationException.class);
    }

    @Test
    @DisplayName("Verify Emergency Donation History Retrieval & Security for Likhitha Markonda & SriSai Hospital")
    void testSriSaiLikhithaEmergencyDonationHistoryRetrieval() {
        // 1. Hospital completes emergency blood request
        authenticateUser(hospitalUser);
        matchedDonor.setStatus(MatchedEmergencyDonorStatus.ACCEPTED);
        matchedDonorRepository.save(matchedDonor);

        ApiResponse<BloodRequestResponse> completeResp = hospitalService.completeEmergencyRequest(hospitalUser.getEmail(), bloodRequest.getId());
        assertThat(completeResp).isNotNull();
        assertThat(completeResp.getData().getStatus()).isEqualTo(RequestStatus.COMPLETED);

        // 2. Likhitha authenticates and fetches donation history
        authenticateUser(donorUser);
        List<com.bloodbridge.dto.DonationSummaryResponse> likhithaHistory = donationService.getMyDonations(donorUser.getEmail());

        assertThat(likhithaHistory).isNotNull().isNotEmpty();
        com.bloodbridge.dto.DonationSummaryResponse completedRecord = likhithaHistory.stream()
                .filter(d -> d.getStatus() == DonationStatus.COMPLETED)
                .findFirst()
                .orElse(null);

        assertThat(completedRecord).isNotNull();
        assertThat(completedRecord.getHospitalName()).isEqualTo("SriSai Multi-speciality Hospital");
        assertThat(completedRecord.getBloodGroup()).isEqualTo(donorProfile.getBloodGroup());
        assertThat(completedRecord.getUnitsDonated()).isNotNull();
        assertThat(completedRecord.getDonationDate()).isNotNull();
        assertThat(completedRecord.getCertificateAvailable()).isTrue();
        assertThat(completedRecord.getCertificateId()).isNotNull().startsWith("CERT-BB-");

        // 3. Security: Another user receives their own history only (not Likhitha's)
        User anotherUser = userRepository.save(User.builder()
                .fullName("Unrelated Donor")
                .email("unrelated.donor@example.com")
                .password("Password123!")
                .phoneNumber("08055554444")
                .role(Role.DONOR)
                .active(true)
                .build());
        DonorProfile anotherDonor = donorProfileRepository.save(DonorProfile.builder()
                .user(anotherUser)
                .email(anotherUser.getEmail())
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(30)
                .gender(Gender.MALE)
                .weight(70.0)
                .city("Hyderabad")
                .state("Telangana")
                .build());

        authenticateUser(anotherUser);
        assertThat(anotherDonor.getId()).isNotNull();
        assertThat(otherHospital.getId()).isNotNull();
        List<com.bloodbridge.dto.DonationSummaryResponse> anotherHistory = donationService.getMyDonations(anotherUser.getEmail());
        assertThat(anotherHistory).isEmpty();

        // 4. Verify Certificate Email Attachment & Duplicate Protection
        byte[] certPdf = certificateService.getCertificatePdfForDonor(completedRecord.getId(), donorUser.getEmail());
        assertThat(certPdf).isNotNull().isNotEmpty();
        assertThat(new String(certPdf, 0, 4)).isEqualTo("%PDF");
    }
}
