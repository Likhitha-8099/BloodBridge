package com.bloodbridge.service;

import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.UserAlreadyExistsException;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.security.JwtService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Enterprise test suite validating Account Lifecycle, Deactivation, Complete Deletion,
 * Orphan Profile Management, and Re-Registration.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserDeletionAndReRegistrationLifecycleTest {

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
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private AuditLoggerService auditLoggerService;

    @Mock
    private RealtimeService realtimeService;

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
    private AuditLogRepository auditLogRepository;

    @Mock
    private EmailNotificationRepository emailNotificationRepository;

    @Mock
    private DonationRepository donationRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest donorRequest;

    @BeforeEach
    void setUp() {
        donorRequest = RegisterRequest.builder()
                .fullName("Likhitha Anumakonda")
                .email("likith.anumakonda@gmail.com")
                .password("SecurePass123!")
                .phoneNumber("9876543210")
                .role(Role.DONOR)
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .build();

        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded_pass");
    }

    @Test
    @DisplayName("1. Active user with same email must be rejected with Email Already Exists")
    void testActiveUserRegistration_IsRejected() {
        User activeUser = User.builder()
                .id(101L)
                .email("likith.anumakonda@gmail.com")
                .fullName("Active User")
                .active(true)
                .role(Role.DONOR)
                .build();

        when(userRepository.findByEmail("likith.anumakonda@gmail.com")).thenReturn(Optional.of(activeUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(donorRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("2. If previous account was completely deleted, allow registration with same email")
    void testCompletelyDeletedUser_AllowsReRegistration() {
        when(userRepository.findByEmail("likith.anumakonda@gmail.com")).thenReturn(Optional.empty());

        User newUser = User.builder()
                .id(202L)
                .email("likith.anumakonda@gmail.com")
                .fullName("Likhitha Anumakonda")
                .active(true)
                .role(Role.DONOR)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(donorProfileRepository.findByUserId(202L)).thenReturn(Optional.empty());
        when(donorProfileRepository.findByEmail("likith.anumakonda@gmail.com")).thenReturn(Optional.empty());

        ApiResponse<String> response = authService.register(donorRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(userRepository, times(1)).save(any(User.class));
        verify(donorProfileRepository, times(1)).save(any(DonorProfile.class));
    }

    @Test
    @DisplayName("3. If account was soft-deleted/deactivated, reactivate existing account cleanly on re-registration")
    void testSoftDeletedUser_ReactivatesCleanly() {
        User deactivatedUser = User.builder()
                .id(303L)
                .email("likith.anumakonda@gmail.com")
                .fullName("Old Deactivated Account")
                .active(false)
                .role(Role.DONOR)
                .build();

        when(userRepository.findByEmail("likith.anumakonda@gmail.com")).thenReturn(Optional.of(deactivatedUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DonorProfile existingProfile = DonorProfile.builder()
                .id(404L)
                .email("likith.anumakonda@gmail.com")
                .user(deactivatedUser)
                .status("DEACTIVATED")
                .build();

        when(donorProfileRepository.findByUserId(303L)).thenReturn(Optional.of(existingProfile));

        ApiResponse<String> response = authService.register(donorRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(deactivatedUser.getActive());
        assertEquals("Likhitha Anumakonda", deactivatedUser.getFullName());
        assertEquals("ACTIVE", existingProfile.getStatus());
        verify(donorProfileRepository, times(1)).save(existingProfile);
    }

    @Test
    @DisplayName("4. Orphaned donor profile from deleted user is safely re-linked on re-registration")
    void testOrphanedDonorProfile_IsRelinkedSafely() {
        when(userRepository.findByEmail("likith.anumakonda@gmail.com")).thenReturn(Optional.empty());

        User freshUser = User.builder()
                .id(505L)
                .email("likith.anumakonda@gmail.com")
                .fullName("Likhitha Anumakonda")
                .active(true)
                .role(Role.DONOR)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(freshUser);
        when(donorProfileRepository.findByUserId(505L)).thenReturn(Optional.empty());

        // Orphaned profile without an active user
        DonorProfile orphanProfile = DonorProfile.builder()
                .id(606L)
                .email("likith.anumakonda@gmail.com")
                .user(null)
                .build();

        when(donorProfileRepository.findByEmail("likith.anumakonda@gmail.com")).thenReturn(Optional.of(orphanProfile));

        ApiResponse<String> response = authService.register(donorRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(freshUser, orphanProfile.getUser());
        verify(donorProfileRepository, times(1)).save(orphanProfile);
    }

    @Test
    @DisplayName("5. deleteUser completely purges user and all associated profile records")
    void testDeleteUser_PurgesUserAndCascadesProfiles() {
        User userToDelete = User.builder()
                .id(707L)
                .email("likith.anumakonda@gmail.com")
                .fullName("User To Delete")
                .active(true)
                .build();

        when(userRepository.findById(707L)).thenReturn(Optional.of(userToDelete));

        ApiResponse<String> response = userService.deleteUser(707L);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("User deleted successfully", response.getMessage());

        verify(deviceTokenRepository, times(1)).deleteAllByUser(userToDelete);
        verify(donorProfileRepository, times(1)).findByUserId(707L);
        verify(hospitalRepository, times(1)).findByUserId(707L);
        verify(patientProfileRepository, times(1)).findByUserId(707L);
        verify(userRepository, times(1)).delete(userToDelete);
    }
}
