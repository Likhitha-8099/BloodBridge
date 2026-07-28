package com.bloodbridge.service;

import com.bloodbridge.dto.DonorProfileRequest;
import com.bloodbridge.dto.DonorProfileResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.*;
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
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private DonorProfileServiceImpl donorProfileService;

    private User user;
    private DonorProfileRequest validRequest;
    private DonorProfile donorProfile;
    private DonorProfileResponse expectedResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("John Donor")
                .email("john.donor@example.com")
                .password("encodedPass")
                .phoneNumber("9876543210")
                .role(Role.DONOR)
                .active(true)
                .build();

        validRequest = DonorProfileRequest.builder()
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(25)
                .gender(Gender.MALE)
                .city("Hyderabad")
                .state("Telangana")
                .lastDonationDate(LocalDate.now().minusDays(100))
                .weight(70.0)
                .availableForDonation(true)
                .totalDonations(2)
                .build();

        donorProfile = DonorProfile.builder()
                .id(1L)
                .user(user)
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(25)
                .gender(Gender.MALE)
                .city("Hyderabad")
                .state("Telangana")
                .lastDonationDate(LocalDate.now().minusDays(100))
                .weight(70.0)
                .availableForDonation(true)
                .totalDonations(2)
                .build();

        expectedResponse = DonorProfileResponse.builder()
                .id(1L)
                .fullName("John Donor")
                .email("john.donor@example.com")
                .phoneNumber("9876543210")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(25)
                .gender(Gender.MALE)
                .city("Hyderabad")
                .state("Telangana")
                .lastDonationDate(LocalDate.now().minusDays(100))
                .weight(70.0)
                .availableForDonation(true)
                .totalDonations(2)
                .eligible(true)
                .nextEligibleDate(LocalDate.now().minusDays(100).plusDays(90))
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
        when(donorProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(donorProfileMapper.toEntity(any(), any())).thenReturn(donorProfile);
        when(donorProfileRepository.save(any(DonorProfile.class))).thenReturn(donorProfile);
        when(donorProfileMapper.toResponse(any(), anyBoolean(), any())).thenReturn(expectedResponse);

        DonorProfileResponse response = donorProfileService.createProfile(validRequest);

        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        verify(donorProfileRepository, times(1)).save(any(DonorProfile.class));
    }

    @Test
    void createProfile_ThrowsInvalidAgeException() {
        mockSecurityContext();
        DonorProfileRequest badRequest = DonorProfileRequest.builder()
                .age(17)
                .weight(60.0)
                .build();

        assertThrows(InvalidAgeException.class, () -> donorProfileService.createProfile(badRequest));
    }

    @Test
    void createProfile_ThrowsInvalidWeightException() {
        mockSecurityContext();
        DonorProfileRequest badRequest = DonorProfileRequest.builder()
                .age(25)
                .weight(48.0)
                .build();

        assertThrows(InvalidWeightException.class, () -> donorProfileService.createProfile(badRequest));
    }

    @Test
    void createProfile_ThrowsEligibilityViolationException_WhenFutureDonationDate() {
        mockSecurityContext();
        DonorProfileRequest badRequest = DonorProfileRequest.builder()
                .age(25)
                .weight(60.0)
                .lastDonationDate(LocalDate.now().plusDays(2))
                .build();

        assertThrows(EligibilityViolationException.class, () -> donorProfileService.createProfile(badRequest));
    }

    @Test
    void getMyProfile_Success() {
        mockSecurityContext();
        when(donorProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(donorProfile));
        when(donorProfileMapper.toResponse(any(), anyBoolean(), any())).thenReturn(expectedResponse);

        DonorProfileResponse response = donorProfileService.getMyProfile();

        assertNotNull(response);
        assertEquals(expectedResponse.getEmail(), response.getEmail());
    }

    @Test
    void isEligibleForDonation_Male_Eligible() {
        DonorProfile maleProfile = DonorProfile.builder()
                .age(25)
                .weight(60.0)
                .gender(Gender.MALE)
                .lastDonationDate(LocalDate.now().minusDays(95))
                .build();

        assertTrue(donorProfileService.isEligibleForDonation(maleProfile));
        assertEquals(maleProfile.getLastDonationDate().plusDays(90), donorProfileService.calculateNextEligibleDate(maleProfile));
    }

    @Test
    void isEligibleForDonation_Male_NotEligible() {
        DonorProfile maleProfile = DonorProfile.builder()
                .age(25)
                .weight(60.0)
                .gender(Gender.MALE)
                .lastDonationDate(LocalDate.now().minusDays(80))
                .build();

        assertFalse(donorProfileService.isEligibleForDonation(maleProfile));
    }

    @Test
    void isEligibleForDonation_Female_Eligible() {
        DonorProfile femaleProfile = DonorProfile.builder()
                .age(25)
                .weight(60.0)
                .gender(Gender.FEMALE)
                .lastDonationDate(LocalDate.now().minusDays(125))
                .build();

        assertTrue(donorProfileService.isEligibleForDonation(femaleProfile));
        assertEquals(femaleProfile.getLastDonationDate().plusDays(120), donorProfileService.calculateNextEligibleDate(femaleProfile));
    }

    @Test
    void isEligibleForDonation_Female_NotEligible() {
        DonorProfile femaleProfile = DonorProfile.builder()
                .age(25)
                .weight(60.0)
                .gender(Gender.FEMALE)
                .lastDonationDate(LocalDate.now().minusDays(100))
                .build();

        assertFalse(donorProfileService.isEligibleForDonation(femaleProfile));
    }
}
