package com.bloodbridge.service;

import com.bloodbridge.dto.request.CreateDonorProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EligibilityStatus;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.EligibilityViolationException;
import com.bloodbridge.exception.InvalidAgeException;
import com.bloodbridge.exception.InvalidWeightException;
import com.bloodbridge.mapper.DonorProfileMapper;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.DonorProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DonorProfileServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DonorProfileServiceImplTest {

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonorProfileMapper donorProfileMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private AuditLoggerService auditLoggerService;

    @InjectMocks
    private DonorProfileServiceImpl donorProfileService;

    private User user;
    private CreateDonorProfileRequest validRequest;
    private DonorProfile donorProfile;
    private DonorProfileResponse expectedResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(2L)
                .fullName("John Donor")
                .email("john.donor@example.com")
                .password("encodedPass")
                .phoneNumber("9876543210")
                .role(Role.DONOR)
                .active(true)
                .build();

        validRequest = CreateDonorProfileRequest.builder()
                .email("john.donor@example.com")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(70.0)
                .age(25)
                .gender(Gender.MALE)
                .availableForDonation(true)
                .lastDonationDate(LocalDate.now().minusDays(100))
                .build();

        donorProfile = DonorProfile.builder()
                .id(1L)
                .user(user)
                .email("john.donor@example.com")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(70.0)
                .age(25)
                .gender(Gender.MALE)
                .availableForDonation(true)
                .lastDonationDate(LocalDate.now().minusDays(100))
                .totalDonations(2)
                .donorScore(100)
                .verificationStatus("VERIFIED")
                .status("ACTIVE")
                .build();

        expectedResponse = DonorProfileResponse.builder()
                .id(1L)
                .fullName("John Donor")
                .email("john.donor@example.com")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(70.0)
                .availableForDonation(true)
                .eligibilityStatus(EligibilityStatus.ELIGIBLE)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john.donor@example.com");
        when(userRepository.findByEmail("john.donor@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void createProfile_Success() {
        mockSecurityContext();
        when(donorProfileRepository.existsByUserId(user.getId())).thenReturn(false);
        when(donorProfileMapper.toEntity(any(), any())).thenReturn(donorProfile);
        when(donorProfileRepository.save(any(DonorProfile.class))).thenReturn(donorProfile);
        when(donorProfileMapper.toResponse(any(), any(), any())).thenReturn(expectedResponse);

        ApiResponse<DonorProfileResponse> response = donorProfileService.createProfile("john.donor@example.com", validRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(expectedResponse.getId(), response.getData().getId());
        verify(donorProfileRepository, times(1)).save(any(DonorProfile.class));
    }

    @Test
    void createProfile_ThrowsInvalidAgeException() {
        mockSecurityContext();
        when(donorProfileRepository.existsByUserId(user.getId())).thenReturn(false);
        when(donorProfileMapper.toEntity(any(), any())).thenReturn(donorProfile);
        when(donorProfileRepository.save(any(DonorProfile.class))).thenReturn(donorProfile);
        CreateDonorProfileRequest badRequest = CreateDonorProfileRequest.builder()
                .age(17)
                .weight(60.0)
                .build();

        assertThrows(InvalidAgeException.class, () -> donorProfileService.createProfile("john.donor@example.com", badRequest));
    }

    @Test
    void createProfile_ThrowsInvalidWeightException() {
        mockSecurityContext();
        when(donorProfileRepository.existsByUserId(user.getId())).thenReturn(false);
        when(donorProfileMapper.toEntity(any(), any())).thenReturn(donorProfile);
        when(donorProfileRepository.save(any(DonorProfile.class))).thenReturn(donorProfile);
        CreateDonorProfileRequest badRequest = CreateDonorProfileRequest.builder()
                .age(25)
                .weight(48.0)
                .build();

        assertThrows(InvalidWeightException.class, () -> donorProfileService.createProfile("john.donor@example.com", badRequest));
    }

    @Test
    void createProfile_ThrowsEligibilityViolationException_WhenFutureDonationDate() {
        mockSecurityContext();
        when(donorProfileRepository.existsByUserId(user.getId())).thenReturn(false);
        when(donorProfileMapper.toEntity(any(), any())).thenReturn(donorProfile);
        when(donorProfileRepository.save(any(DonorProfile.class))).thenReturn(donorProfile);
        CreateDonorProfileRequest badRequest = CreateDonorProfileRequest.builder()
                .age(25)
                .weight(60.0)
                .lastDonationDate(LocalDate.now().plusDays(2))
                .build();

        assertThrows(EligibilityViolationException.class, () -> donorProfileService.createProfile("john.donor@example.com", badRequest));
    }

    @Test
    void getMyProfile_Success() {
        mockSecurityContext();
        when(donorProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(donorProfile));
        when(donorProfileMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(expectedResponse);

        ApiResponse<DonorProfileResponse> response = donorProfileService.getMyProfile("john.donor@example.com");

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(expectedResponse.getEmail(), response.getData().getEmail());
    }
}
