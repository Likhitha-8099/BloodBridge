package com.bloodbridge.service;

import com.bloodbridge.config.MatchingConfig;
import com.bloodbridge.dto.DonationStatusUpdateRequest;
import com.bloodbridge.dto.DonationResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.MatchStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.exception.DuplicateDonationException;
import com.bloodbridge.exception.InvalidDonationStateException;
import com.bloodbridge.exception.UnauthorizedDonationAccessException;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.MatchResultRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.DonationServiceImpl;
import org.junit.jupiter.api.AfterEach;
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
public class DonationCompletionLifecycleTest {

    @Autowired
    private DonationServiceImpl donationService;

    @Autowired
    private CertificateService certificateService;

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
    private DonationRepository donationRepository;

    @Autowired
    private MatchingConfig matchingConfig;

    private User hospitalUser1;
    private Hospital hospital1;
    private User hospitalUser2;
    private Hospital hospital2;

    private User donorUser;
    private DonorProfile donorProfile;

    private User patientUser;
    private PatientProfile patientProfile;

    private BloodRequest bloodRequest;
    private MatchResult matchResult;
    private Donation donation;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // 1. Hospital 1
        hospitalUser1 = userRepository.save(User.builder()
                .fullName("Apollo Hospitals Central")
                .email("apollo.central@test.com")
                .password("Password123!")
                .phoneNumber("08011112222")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .build());

        hospital1 = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser1)
                .hospitalName("Apollo Hospitals Central")
                .email(hospitalUser1.getEmail())
                .phoneNumber("08011112222")
                .registrationNumber("REG-APOLLO-001")
                .address("100 Health Way")
                .city("Bangalore")
                .state("Karnataka")
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // 2. Hospital 2 (Unrelated Hospital)
        hospitalUser2 = userRepository.save(User.builder()
                .fullName("Fortis Care Hospital")
                .email("fortis.care@test.com")
                .password("Password123!")
                .phoneNumber("08033334444")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .build());

        hospital2 = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser2)
                .hospitalName("Fortis Care Hospital")
                .email(hospitalUser2.getEmail())
                .phoneNumber("08033334444")
                .registrationNumber("REG-FORTIS-002")
                .address("200 Care Blvd")
                .city("Bangalore")
                .state("Karnataka")
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());
        assertThat(hospital2.getId()).isNotNull();

        // 3. Donor
        donorUser = userRepository.save(User.builder()
                .fullName("Anand Kumar")
                .email("anand.donor@test.com")
                .password("Password123!")
                .phoneNumber("9876543210")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .build());

        donorProfile = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUser)
                .email(donorUser.getEmail())
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(30)
                .gender(Gender.MALE)
                .weight(72.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .totalDonations(0)
                .livesSaved(0)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .build());

        // 4. Patient
        patientUser = userRepository.save(User.builder()
                .fullName("Ramesh Patient")
                .email("ramesh.patient@test.com")
                .password("Password123!")
                .phoneNumber("9123456789")
                .role(Role.PATIENT)
                .active(true)
                .emailVerified(true)
                .build());

        patientProfile = patientProfileRepository.save(PatientProfile.builder()
                .user(patientUser)
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(35)
                .gender(Gender.MALE)
                .emergencyContactName("Sunita Patient")
                .emergencyContactNumber("9988776655")
                .city("Bangalore")
                .state("Karnataka")
                .build());

        // 5. Blood Request
        bloodRequest = bloodRequestRepository.save(BloodRequest.builder()
                .hospital(hospital1)
                .patient(patientProfile)
                .bloodGroupNeeded(BloodGroup.B_POSITIVE)
                .unitsRequired(1)
                .urgencyLevel(UrgencyLevel.HIGH)
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(1))
                .status(RequestStatus.ACTIVE)
                .build());

        // 6. Match Result
        matchResult = matchResultRepository.save(MatchResult.builder()
                .bloodRequest(bloodRequest)
                .donor(donorProfile)
                .matchScore(95.0)
                .compatibilityScore(95.0)
                .distanceKm(5.2)
                .matchedAt(LocalDateTime.now())
                .status(MatchStatus.ACCEPTED)
                .build());

        // 7. Donation Record (Status: ACCEPTED)
        donation = donationRepository.save(Donation.builder()
                .donor(donorProfile)
                .patient(patientProfile)
                .bloodRequest(bloodRequest)
                .hospital(hospital1)
                .matchResult(matchResult)
                .unitsDonated(1)
                .status(DonationStatus.ACCEPTED)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                )
        );
    }

    @Test
    @DisplayName("Test 1 & 2: Hospital completes donation — status becomes COMPLETED and completedAt timestamp is recorded")
    void test1And2_CompleteDonation_StatusAndTimestamp() {
        authenticateUser(hospitalUser1);

        DonationStatusUpdateRequest request = DonationStatusUpdateRequest.builder()
                .unitsDonated(1)
                .remarks("Successful whole blood donation.")
                .build();

        DonationResponse response = donationService.completeDonation(donation.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(DonationStatus.COMPLETED);

        Donation updated = donationRepository.findById(donation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
        assertThat(updated.getCompletedAt().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(updated.getCertificateId()).startsWith("CERT-BB-");
    }

    @Test
    @DisplayName("Test 3 & 4: Completion updates donor lastDonationDate and nextEligibleDate using dynamic cooldown")
    void test3And4_CompleteDonation_UpdatesDonorEligibilityAndCooldown() {
        authenticateUser(hospitalUser1);

        donationService.completeDonation(donation.getId(), null);

        DonorProfile updatedDonor = donorProfileRepository.findById(donorProfile.getId()).orElseThrow();
        int expectedCooldown = matchingConfig.getCooldownDays();

        assertThat(updatedDonor.getLastDonationDate()).isEqualTo(LocalDate.now());
        assertThat(updatedDonor.getNextEligibleDate()).isEqualTo(LocalDate.now().plusDays(expectedCooldown));
        assertThat(updatedDonor.getAvailableForDonation()).isFalse();
    }

    @Test
    @DisplayName("Test 5: Certificate is available ONLY for COMPLETED donations")
    void test5_CertificateAvailableOnlyAfterCompletion() {
        // Before completion: Certificate request throws InvalidDonationStateException
        authenticateUser(donorUser);

        assertThatThrownBy(() -> certificateService.getCertificatePdfForDonor(donation.getId(), donorUser.getEmail()))
                .isInstanceOf(InvalidDonationStateException.class);

        // Hospital completes donation
        authenticateUser(hospitalUser1);
        donationService.completeDonation(donation.getId(), null);

        // After completion: Certificate request succeeds and generates PDF byte array
        authenticateUser(donorUser);
        byte[] pdf = certificateService.getCertificatePdfForDonor(donation.getId(), donorUser.getEmail());
        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Test 6: Duplicate completion throws DuplicateDonationException (HTTP 409 Conflict)")
    void test6_DuplicateCompletion_ThrowsException() {
        authenticateUser(hospitalUser1);

        // First completion succeeds
        donationService.completeDonation(donation.getId(), null);

        // Second completion attempt fails
        assertThatThrownBy(() -> donationService.completeDonation(donation.getId(), null))
                .isInstanceOf(DuplicateDonationException.class);
    }

    @Test
    @DisplayName("Test 7: Unrelated hospital cannot complete donation assigned to Hospital 1")
    void test7_UnrelatedHospital_AccessDenied() {
        authenticateUser(hospitalUser2);

        assertThatThrownBy(() -> donationService.completeDonation(donation.getId(), null))
                .isInstanceOf(UnauthorizedDonationAccessException.class);
    }

    @Test
    @DisplayName("Test 8: Donor cannot complete their own donation directly (throws HospitalNotFoundException)")
    void test8_DonorCannotCompleteDonation() {
        authenticateUser(donorUser);

        assertThatThrownBy(() -> donationService.completeDonation(donation.getId(), null))
                .isInstanceOf(com.bloodbridge.exception.HospitalNotFoundException.class);
    }

    @Test
    @DisplayName("Test 9: Donor impact statistics update total donations (+1) and lives saved (+3)")
    void test9_ImpactStatistics_UpdatedExactlyOnce() {
        authenticateUser(hospitalUser1);

        assertThat(donorProfile.getTotalDonations()).isEqualTo(0);
        assertThat(donorProfile.getLivesSaved()).isEqualTo(0);

        donationService.completeDonation(donation.getId(), null);

        DonorProfile updatedDonor = donorProfileRepository.findById(donorProfile.getId()).orElseThrow();
        assertThat(updatedDonor.getTotalDonations()).isEqualTo(1);
        assertThat(updatedDonor.getLivesSaved()).isEqualTo(3);
    }
}
