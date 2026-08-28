package com.bloodbridge.service;

import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.UserAlreadyExistsException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.security.JwtService;
import com.bloodbridge.service.impl.AdminServiceImpl;
import com.bloodbridge.service.impl.AuthServiceImpl;
import com.bloodbridge.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Enterprise test suite verifying Admin Permanent Donor Deletion:
 * - Admin service orchestration & role execution
 * - Dependent records cleanup & FK constraint safety
 * - Unlinking completed hospital donations to preserve history & statistics
 * - Re-registration support after permanent deletion
 * - Active user duplicate-email protection
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDonorDeletionEnterpriseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;

    @Mock
    private EmergencyResponseRepository emergencyResponseRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PushDeliveryLogRepository pushDeliveryLogRepository;

    @Mock
    private DonorLiveLocationRepository donorLiveLocationRepository;

    @Mock
    private EmailNotificationRepository emailNotificationRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private RealtimeService realtimeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private AuditLoggerService auditLoggerService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private AdminServiceImpl adminService;

    private User sampleUser;
    private DonorProfile sampleDonor;

    @BeforeEach
    void setUp() {
        com.bloodbridge.mapper.DonorProfileMapper donorProfileMapper = new com.bloodbridge.mapper.DonorProfileMapper();
        adminService = new AdminServiceImpl(
                userRepository,
                donorProfileRepository,
                patientProfileRepository,
                hospitalRepository,
                null,
                donationRepository,
                matchResultRepository,
                null,
                null,
                notificationRepository,
                null,
                userMapper,
                null,
                donorProfileMapper,
                null,
                auditLoggerService,
                realtimeService,
                eventPublisher,
                userService
        );

        sampleUser = User.builder()
                .id(101L)
                .email("donor.deletion.test@bloodbridge.com")
                .password("encoded_pass")
                .fullName("Jane Donor")
                .role(Role.DONOR)
                .active(true)
                .build();

        sampleDonor = DonorProfile.builder()
                .id(202L)
                .user(sampleUser)
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .totalDonations(5)
                .build();
    }

    @Test
    @DisplayName("Admin fetches all registered donors from database")
    void testAdminGetAllDonorsSuccessfully() {
        when(donorProfileRepository.findAll()).thenReturn(java.util.List.of(sampleDonor));
        when(userRepository.findAll()).thenReturn(java.util.List.of(sampleUser));

        ApiResponse<java.util.List<com.bloodbridge.dto.response.DonorProfileResponse>> response =
                adminService.getAllDonors(null, null, null);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("Jane Donor", response.getData().get(0).getFullName());
        assertEquals("donor.deletion.test@bloodbridge.com", response.getData().get(0).getEmail());
        assertEquals(BloodGroup.O_POSITIVE, response.getData().get(0).getBloodGroup());
    }

    @Test
    @DisplayName("Admin fetches donor details by ID")
    void testAdminGetDonorByIdSuccessfully() {
        when(donorProfileRepository.findById(202L)).thenReturn(Optional.of(sampleDonor));

        ApiResponse<com.bloodbridge.dto.response.DonorProfileResponse> response =
                adminService.getDonorById(202L);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(202L, response.getData().getId());
        assertEquals("Jane Donor", response.getData().getFullName());
    }

    @Test
    @DisplayName("Admin fetches donor details by non-existent ID throws exception")
    void testAdminGetDonorByIdNotFoundThrowsException() {
        when(donorProfileRepository.findById(999L)).thenReturn(Optional.empty());
        when(donorProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminService.getDonorById(999L));
    }

    @Test
    @DisplayName("Admin deletes donor: cleans up dependent tables, unlinks hospital donations, and removes user/profile")
    void testAdminPermanentlyDeletesDonorSuccessfully() {
        when(donorProfileRepository.findById(202L)).thenReturn(Optional.of(sampleDonor));
        when(userRepository.findById(101L)).thenReturn(Optional.of(sampleUser));

        ApiResponse<String> response = adminService.deleteDonor(202L, "admin@bloodbridge.com");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Donor permanently deleted successfully", response.getMessage());

        // Verify unlinking donations rather than deleting hospital history
        verify(donationRepository, times(1)).unlinkDonorProfile(202L);
        verify(auditLogRepository, times(1)).unlinkDonor(202L);

        // Verify child cleanups
        verify(notificationRepository, times(1)).unlinkDonorProfile(202L);
        verify(donorLiveLocationRepository, times(1)).deleteAllByDonorId(202L);
        verify(matchedEmergencyDonorRepository, times(1)).deleteAllByDonorId(202L);
        verify(emergencyResponseRepository, times(1)).deleteAllByDonorId(202L);
        verify(matchResultRepository, times(1)).deleteAllByDonorId(202L);
        verify(emailNotificationRepository, times(1)).deleteAllByDonorId(202L);
        verify(pushDeliveryLogRepository, times(1)).deleteAllByUserId(101L);
        verify(notificationRepository, times(1)).deleteAllByRecipientUserId(101L);
        verify(deviceTokenRepository, times(1)).deleteAllByUser(sampleUser);

        // Verify entity deletion
        verify(donorProfileRepository, times(1)).delete(sampleDonor);
        verify(userRepository, times(1)).delete(sampleUser);

        // Verify audit logging & realtime broadcast
        verify(auditLoggerService, times(1)).logEvent(eq("DONOR_DELETED"), eq("donor.deletion.test@bloodbridge.com"), anyString());
        verify(realtimeService, atLeastOnce()).publishAdminUsersUpdate(any(RealtimeEventDTO.class));
    }

    @Test
    @DisplayName("Admin deletes donor using User ID identifier")
    void testAdminDeletesDonorByUserId() {
        when(donorProfileRepository.findById(101L)).thenReturn(Optional.empty());
        when(donorProfileRepository.findByUserId(101L)).thenReturn(Optional.of(sampleDonor));
        when(userRepository.findById(101L)).thenReturn(Optional.of(sampleUser));

        ApiResponse<String> response = userService.deleteDonor(101L);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(donationRepository, times(1)).unlinkDonorProfile(202L);
        verify(auditLogRepository, times(1)).unlinkDonor(202L);
        verify(donorProfileRepository, times(1)).delete(sampleDonor);
        verify(userRepository, times(1)).delete(sampleUser);
    }

    @Test
    @DisplayName("Admin deletes donor: non-existent donor throws UserNotFoundException")
    void testDeleteNonExistentDonorThrowsException() {
        when(donorProfileRepository.findById(999L)).thenReturn(Optional.empty());
        when(donorProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteDonor(999L));
    }

    @Test
    @DisplayName("Email can be re-registered immediately after permanent donor deletion")
    void testReRegistrationAfterDonorDeletion() {
        String testEmail = "donor.deletion.test@bloodbridge.com";

        // Step 1: When user is active, duplicate registration is rejected
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(sampleUser));

        RegisterRequest registerReq = RegisterRequest.builder()
                .email(testEmail)
                .password("Password@123")
                .fullName("Jane Donor")
                .role(Role.DONOR)
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .build();

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerReq));

        // Step 2: Simulate permanent deletion (userRepository no longer has email)
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(donorProfileRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        User newUser = User.builder()
                .id(102L)
                .email(testEmail)
                .fullName("Jane Donor")
                .role(Role.DONOR)
                .active(true)
                .build();

        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        ApiResponse<String> regResponse = authService.register(registerReq);

        assertNotNull(regResponse);
        assertTrue(regResponse.isSuccess());
        assertEquals("User registered successfully", regResponse.getMessage());
    }
}
