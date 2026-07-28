package com.bloodbridge.service;

import com.bloodbridge.dto.PatientProfileRequest;
import com.bloodbridge.dto.PatientProfileResponse;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.InvalidAgeException;
import com.bloodbridge.exception.PatientProfileAlreadyExistsException;
import com.bloodbridge.exception.PatientProfileNotFoundException;
import com.bloodbridge.mapper.PatientProfileMapper;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.PatientProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PatientProfileServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class PatientProfileServiceImplTest {

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientProfileMapper patientProfileMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PatientProfileServiceImpl patientProfileService;

    private User patientUser;
    private User donorUser;
    private PatientProfileRequest validRequest;
    private PatientProfile patientProfile;
    private PatientProfileResponse expectedResponse;

    @BeforeEach
    void setUp() {
        patientUser = User.builder()
                .id(2L)
                .fullName("Sarah Patient")
                .email("sarah.patient@example.com")
                .password("encodedPass")
                .phoneNumber("9876543210")
                .role(Role.PATIENT)
                .active(true)
                .build();

        donorUser = User.builder()
                .id(3L)
                .fullName("John Donor")
                .email("john.donor@example.com")
                .password("encodedPass")
                .phoneNumber("9876543210")
                .role(Role.DONOR)
                .active(true)
                .build();

        validRequest = PatientProfileRequest.builder()
                .age(30)
                .gender(Gender.FEMALE)
                .bloodGroup(BloodGroup.A_POSITIVE)
                .address("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .emergencyContactName("Dad")
                .emergencyContactNumber("9111111111")
                .medicalHistory("None")
                .build();

        patientProfile = PatientProfile.builder()
                .id(1L)
                .user(patientUser)
                .age(30)
                .gender(Gender.FEMALE)
                .bloodGroup(BloodGroup.A_POSITIVE)
                .address("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .emergencyContactName("Dad")
                .emergencyContactNumber("9111111111")
                .medicalHistory("None")
                .build();

        expectedResponse = PatientProfileResponse.builder()
                .id(1L)
                .fullName("Sarah Patient")
                .email("sarah.patient@example.com")
                .phoneNumber("9876543210")
                .age(30)
                .gender(Gender.FEMALE)
                .bloodGroup(BloodGroup.A_POSITIVE)
                .address("123 Main St")
                .city("Bangalore")
                .state("Karnataka")
                .emergencyContactName("Dad")
                .emergencyContactNumber("9111111111")
                .medicalHistory("None")
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext(String email, User userContext) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userContext));
    }

    @Test
    void createProfile_Success() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        when(patientProfileRepository.existsByUserId(patientUser.getId())).thenReturn(false);
        when(patientProfileMapper.toEntity(any(), any())).thenReturn(patientProfile);
        when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(patientProfile);
        when(patientProfileMapper.toResponse(any())).thenReturn(expectedResponse);

        PatientProfileResponse response = patientProfileService.createProfile(validRequest);

        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        verify(patientProfileRepository, times(1)).save(any(PatientProfile.class));
    }

    @Test
    void createProfile_ThrowsException_WhenUserNotPatient() {
        mockSecurityContext("john.donor@example.com", donorUser);

        assertThrows(IllegalArgumentException.class, () -> patientProfileService.createProfile(validRequest));
        verify(patientProfileRepository, never()).save(any());
    }

    @Test
    void createProfile_ThrowsException_WhenProfileAlreadyExists() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        when(patientProfileRepository.existsByUserId(patientUser.getId())).thenReturn(true);

        assertThrows(PatientProfileAlreadyExistsException.class, () -> patientProfileService.createProfile(validRequest));
    }

    @Test
    void createProfile_ThrowsInvalidAgeException_WhenAgeTooHigh() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        PatientProfileRequest badRequest = PatientProfileRequest.builder()
                .age(125)
                .build();

        assertThrows(InvalidAgeException.class, () -> patientProfileService.createProfile(badRequest));
    }

    @Test
    void getMyProfile_Success() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        when(patientProfileRepository.findByUserId(patientUser.getId())).thenReturn(Optional.of(patientProfile));
        when(patientProfileMapper.toResponse(any())).thenReturn(expectedResponse);

        PatientProfileResponse response = patientProfileService.getMyProfile();

        assertNotNull(response);
        assertEquals(expectedResponse.getEmail(), response.getEmail());
    }

    @Test
    void getPatientById_Success() {
        when(patientProfileRepository.findById(1L)).thenReturn(Optional.of(patientProfile));
        when(patientProfileMapper.toResponse(any())).thenReturn(expectedResponse);

        PatientProfileResponse response = patientProfileService.getPatientById(1L);

        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
    }

    @Test
    void getPatientById_ThrowsNotFound() {
        when(patientProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PatientProfileNotFoundException.class, () -> patientProfileService.getPatientById(99L));
    }
}
