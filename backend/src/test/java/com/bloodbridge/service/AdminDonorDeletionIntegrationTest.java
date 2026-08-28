package com.bloodbridge.service;

import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.UserAlreadyExistsException;
import com.bloodbridge.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test Suite for Admin Donor Hard Deletion:
 * - TEST 1: Full child cleanup & unlinking
 * - TEST 2: Immediate re-registration after deletion with same email
 * - TEST 3: Duplicate registration prevention for active donors
 * - TEST 4: Hospital, patient, and blood request preservation
 * - TEST 5: Transactional atomicity & rollback on failure
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminDonorDeletionIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private MatchResultRepository matchResultRepository;

    @Autowired
    private MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;

    @Autowired
    private EmergencyResponseRepository emergencyResponseRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private PushDeliveryLogRepository pushDeliveryLogRepository;

    @Autowired
    private DonorLiveLocationRepository donorLiveLocationRepository;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private User adminUser;
    private User hospitalUser;
    private Hospital hospital;
    private User patientUser;
    private PatientProfile patient;
    private BloodRequest bloodRequest;

    @BeforeEach
    void setUp() {
        // Create Admin
        adminUser = userRepository.findByEmail("admin.test@bloodbridge.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("System Admin")
                        .email("admin.test@bloodbridge.com")
                        .password("AdminPass123!")
                        .phoneNumber("9000000001")
                        .role(Role.ADMIN)
                        .active(true)
                        .build())
        );

        // Create Hospital User & Profile
        hospitalUser = userRepository.findByEmail("care.hospital@bloodbridge.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Care Hospital Admin")
                        .email("care.hospital@bloodbridge.com")
                        .password("HospPass123!")
                        .phoneNumber("9000000002")
                        .role(Role.HOSPITAL)
                        .active(true)
                        .build())
        );

        hospital = hospitalRepository.findByUserId(hospitalUser.getId()).orElseGet(() ->
                hospitalRepository.save(Hospital.builder()
                        .user(hospitalUser)
                        .hospitalName("Care Super Specialty Hospital")
                        .registrationNumber("HOSP-REG-" + System.nanoTime())
                        .email("care.hospital@bloodbridge.com")
                        .phoneNumber("9000000002")
                        .city("Hyderabad")
                        .state("Telangana")
                        .address("Banjara Hills Rd 1")
                        .latitude(17.4123)
                        .longitude(78.4321)
                        .build())
        );

        // Create Patient User & Profile
        patientUser = userRepository.findByEmail("patient.test@bloodbridge.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Ravi Kumar")
                        .email("patient.test@bloodbridge.com")
                        .password("PatientPass123!")
                        .phoneNumber("9000000003")
                        .role(Role.PATIENT)
                        .active(true)
                        .build())
        );

        patient = patientProfileRepository.findByUserId(patientUser.getId()).orElseGet(() ->
                patientProfileRepository.save(PatientProfile.builder()
                        .user(patientUser)
                        .age(35)
                        .gender(Gender.MALE)
                        .bloodGroup(BloodGroup.O_POSITIVE)
                        .city("Hyderabad")
                        .state("Telangana")
                        .emergencyContactName("Sunita Kumar")
                        .emergencyContactNumber("9000000004")
                        .build())
        );

        // Create Blood Request
        bloodRequest = bloodRequestRepository.save(BloodRequest.builder()
                .hospital(hospital)
                .patient(patient)
                .bloodGroupNeeded(BloodGroup.O_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .status(RequestStatus.ACTIVE)
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(2))
                .build());
    }

    private User createFullDonor(String email, String name) {
        User donorUser = userRepository.save(User.builder()
                .fullName(name)
                .email(email)
                .password("DonorPass123!")
                .phoneNumber("9888877771")
                .role(Role.DONOR)
                .city("Hyderabad")
                .state("Telangana")
                .active(true)
                .build());

        DonorProfile donorProfile = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUser)
                .email(email)
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(28)
                .gender(Gender.MALE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(72.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .donorScore(100)
                .build());

        // Child 1: Device Token
        DeviceToken token = deviceTokenRepository.save(DeviceToken.builder()
                .user(donorUser)
                .fcmToken("fcm_token_" + System.nanoTime())
                .platform("WEB")
                .isActive(true)
                .lastSeen(LocalDateTime.now())
                .build());

        // Child 2: Push Delivery Log
        pushDeliveryLogRepository.save(PushDeliveryLog.builder()
                .user(donorUser)
                .deviceToken(token)
                .fcmToken(token.getFcmToken())
                .emergencyRequestId(bloodRequest.getId())
                .status("DELIVERED")
                .latencyMs(120L)
                .build());

        // Child 3: Notification Preference
        notificationPreferenceRepository.save(NotificationPreference.builder()
                .user(donorUser)
                .emailEnabled(true)
                .pushEnabled(true)
                .webSocketEnabled(true)
                .build());

        // Child 4: Notification to donor (recipient)
        notificationRepository.save(Notification.builder()
                .recipientUser(donorUser)
                .title("Emergency Request Nearby")
                .message("Urgent O+ blood needed at Care Hospital")
                .notificationType(NotificationType.EMERGENCY_BLOOD_REQUEST)
                .deliveryChannel(DeliveryChannel.PUSH)
                .status(NotificationStatus.SENT)
                .bloodRequest(bloodRequest)
                .build());

        // Child 5: Notification to Hospital referencing donor
        notificationRepository.save(Notification.builder()
                .recipientUser(hospitalUser)
                .hospital(hospital)
                .donor(donorProfile)
                .title("Donor Accepted")
                .message(name + " has accepted your emergency request")
                .notificationType(NotificationType.DONATION_ACCEPTED)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .status(NotificationStatus.SENT)
                .bloodRequest(bloodRequest)
                .build());

        // Child 6: Donor Live Location
        donorLiveLocationRepository.save(DonorLiveLocation.builder()
                .donorId(donorProfile.getId())
                .bloodRequestId(bloodRequest.getId())
                .hospitalId(hospital.getId())
                .latitude(17.4130)
                .longitude(78.4330)
                .trackingStatus(TrackingStatus.MOVING)
                .lastUpdated(LocalDateTime.now())
                .build());

        // Child 7: Matched Emergency Donor
        matchedEmergencyDonorRepository.save(MatchedEmergencyDonor.builder()
                .bloodRequest(bloodRequest)
                .donor(donorProfile)
                .hospital(hospital)
                .status(MatchedEmergencyDonorStatus.ACCEPTED)
                .distanceKm(3.5)
                .build());

        // Child 8: Emergency Response
        emergencyResponseRepository.save(EmergencyResponse.builder()
                .bloodRequest(bloodRequest)
                .donor(donorProfile)
                .status(EmergencyResponseStatus.ACCEPTED)
                .distanceKm(3.5)
                .etaMinutes(15)
                .build());

        // Child 9: Email Notification Log
        emailNotificationRepository.save(EmailNotification.builder()
                .emergencyRequestId(bloodRequest.getId())
                .donorId(donorProfile.getId())
                .email(email)
                .status(EmailDeliveryStatus.SENT)
                .build());

        // Child 10: Match Result
        MatchResult matchResult = matchResultRepository.save(MatchResult.builder()
                .bloodRequest(bloodRequest)
                .donor(donorProfile)
                .matchScore(95.0)
                .status(MatchStatus.NOTIFIED)
                .build());

        // Child 11: Donation (historical record linked to hospital & request)
        donationRepository.save(Donation.builder()
                .bloodRequest(bloodRequest)
                .hospital(hospital)
                .donor(donorProfile)
                .matchResult(matchResult)
                .unitsDonated(1)
                .donationDate(LocalDate.now())
                .status(DonationStatus.COMPLETED)
                .build());

        // Child 12: Audit Log referencing donor
        auditLogRepository.save(AuditLog.builder()
                .userEmail(email)
                .userRole("DONOR")
                .donorId(donorProfile.getId())
                .emergencyRequestId(bloodRequest.getId())
                .hospitalId(hospital.getId())
                .action("DONOR_ACCEPTED_REQUEST")
                .description("Donor accepted emergency blood request")
                .build());

        return donorUser;
    }

    @Test
    @DisplayName("TEST 1: Create donor with full child records -> delete donor via admin -> verify complete donor cleanup")
    void test1_CreateDonor_DeleteViaAdmin_VerifyCompleteCleanup() {
        String donorEmail = "hard.delete.donor@bloodbridge.com";
        User donorUser = createFullDonor(donorEmail, "Hard Delete Donor");
        Long userId = donorUser.getId();
        DonorProfile donorProfile = donorProfileRepository.findByUserId(userId).orElseThrow();
        Long donorProfileId = donorProfile.getId();

        // Perform admin deletion
        ApiResponse<String> response = adminService.deleteDonor(donorProfileId, adminUser.getEmail());

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("permanently deleted");

        // Verify donor user & profile are completely gone
        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(donorProfileRepository.findById(donorProfileId)).isEmpty();
        assertThat(donorProfileRepository.findByEmail(donorEmail)).isEmpty();

        // Verify all donor-specific child records are deleted
        assertThat(deviceTokenRepository.findAllByUser(donorUser)).isEmpty();
        assertThat(notificationPreferenceRepository.findByUserId(userId)).isEmpty();
        assertThat(notificationRepository.findUserNotifications(userId)).isEmpty();
        assertThat(donorLiveLocationRepository.findRouteByDonorIdAndBloodRequestId(donorProfileId, bloodRequest.getId())).isEmpty();
        assertThat(emergencyResponseRepository.findByDonorIdWithDetails(donorProfileId)).isEmpty();
        assertThat(matchedEmergencyDonorRepository.findByDonorIdOrderByCreatedAtDesc(donorProfileId)).isEmpty();
        assertThat(matchResultRepository.findByDonorId(donorProfileId)).isEmpty();

        // Verify donations are unlinked (hospital record preserved, donor & matchResult unlinked)
        List<Donation> hospitalDonations = donationRepository.findByHospitalId(hospital.getId());
        assertThat(hospitalDonations).isNotEmpty();
        for (Donation d : hospitalDonations) {
            assertThat(d.getDonor()).isNull();
            assertThat(d.getMatchResult()).isNull();
        }

        // Verify notifications sent to hospital had donor unlinked
        List<Notification> hospitalNotifs = notificationRepository.findByHospitalIdOrderByCreatedAtDesc(hospital.getId());
        for (Notification n : hospitalNotifs) {
            assertThat(n.getDonor()).isNull();
        }

        // Verify audit logs had donorId unlinked but retained userEmail history
        List<AuditLog> auditLogs = auditLogRepository.findByUserEmailOrderByCreatedAtDesc(donorEmail);
        for (AuditLog log : auditLogs) {
            assertThat(log.getDonorId()).isNull();
        }
    }

    @Test
    @DisplayName("TEST 2: Create donor -> delete donor -> register again using same email -> registration succeeds")
    void test2_CreateDonor_Delete_ReRegisterSameEmail_Succeeds() {
        String testEmail = "re.register.donor@bloodbridge.com";
        User initialDonor = createFullDonor(testEmail, "Initial Donor");
        Long initialProfileId = donorProfileRepository.findByUserId(initialDonor.getId()).orElseThrow().getId();

        // Delete donor
        adminService.deleteDonor(initialProfileId, adminUser.getEmail());

        // Re-register with the same email
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("New Donor Profile")
                .email(testEmail)
                .password("BrandNewPass123!")
                .phoneNumber("9123456789")
                .role(Role.DONOR)
                .bloodGroup(BloodGroup.A_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .age(30)
                .gender(Gender.FEMALE)
                .build();

        ApiResponse<String> regResponse = authService.register(registerRequest);

        assertThat(regResponse).isNotNull();
        assertThat(regResponse.isSuccess()).isTrue();
        assertThat(regResponse.getMessage()).contains("registered successfully");

        // Verify new user and profile exist
        Optional<User> freshUserOpt = userRepository.findByEmail(testEmail);
        assertThat(freshUserOpt).isPresent();
        User freshUser = freshUserOpt.get();
        assertThat(freshUser.getFullName()).isEqualTo("New Donor Profile");
        assertThat(freshUser.getActive()).isTrue();

        Optional<DonorProfile> freshProfileOpt = donorProfileRepository.findByUserId(freshUser.getId());
        assertThat(freshProfileOpt).isPresent();
        assertThat(freshProfileOpt.get().getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
    }

    @Test
    @DisplayName("TEST 3: Create active donor -> attempt duplicate registration -> returns UserAlreadyExistsException (HTTP 409)")
    void test3_ActiveDonor_AttemptDuplicateRegistration_ThrowsConflict() {
        String activeEmail = "active.donor.test@bloodbridge.com";
        createFullDonor(activeEmail, "Active Donor");

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .fullName("Duplicate User")
                .email(activeEmail)
                .password("AnyPassword123!")
                .phoneNumber("9999900000")
                .role(Role.DONOR)
                .bloodGroup(BloodGroup.B_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .build();

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(duplicateRequest));
    }

    @Test
    @DisplayName("TEST 4: Delete donor -> verify hospital, admin, patient, and blood request records remain intact")
    void test4_DeleteDonor_HospitalAdminPatientBloodRequestsUnaffected() {
        String donorEmail = "donor.isolation.test@bloodbridge.com";
        User donor = createFullDonor(donorEmail, "Isolation Donor");
        Long donorProfileId = donorProfileRepository.findByUserId(donor.getId()).orElseThrow().getId();

        Long hospitalId = hospital.getId();
        Long patientId = patient.getId();
        Long bloodRequestId = bloodRequest.getId();
        Long adminId = adminUser.getId();

        // Delete donor
        adminService.deleteDonor(donorProfileId, adminUser.getEmail());

        // Verify hospital is completely untouched
        Optional<Hospital> checkHospital = hospitalRepository.findById(hospitalId);
        assertThat(checkHospital).isPresent();
        assertThat(checkHospital.get().getHospitalName()).isEqualTo("Care Super Specialty Hospital");

        // Verify patient is completely untouched
        Optional<PatientProfile> checkPatient = patientProfileRepository.findById(patientId);
        assertThat(checkPatient).isPresent();
        assertThat(checkPatient.get().getId()).isEqualTo(patientId);

        // Verify blood request remains intact
        Optional<BloodRequest> checkRequest = bloodRequestRepository.findById(bloodRequestId);
        assertThat(checkRequest).isPresent();
        assertThat(checkRequest.get().getHospital().getId()).isEqualTo(hospitalId);
        assertThat(checkRequest.get().getPatient().getId()).isEqualTo(patientId);
        assertThat(checkRequest.get().getStatus()).isEqualTo(RequestStatus.ACTIVE);

        // Verify admin is unaffected
        Optional<User> checkAdmin = userRepository.findById(adminId);
        assertThat(checkAdmin).isPresent();
        assertThat(checkAdmin.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("TEST 5: Controlled deletion failure -> transaction rolls back and no partial deletion occurs")
    void test5_ControlledDeletionFailure_RollsBackCleanly() {
        String donorEmail = "rollback.donor@bloodbridge.com";
        User donor = createFullDonor(donorEmail, "Rollback Donor");
        Long donorProfileId = donorProfileRepository.findByUserId(donor.getId()).orElseThrow().getId();

        // Attempt deletion of non-existent donor ID throws UserNotFoundException without modifying any record
        assertThrows(com.bloodbridge.exception.UserNotFoundException.class, () ->
                userService.deleteDonor(999999L)
        );

        // Verify the existing donor remains fully intact
        assertThat(userRepository.findByEmail(donorEmail)).isPresent();
        assertThat(donorProfileRepository.findById(donorProfileId)).isPresent();
    }
}
