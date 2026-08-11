package com.bloodbridge.service;

import com.bloodbridge.dto.request.CreatePatientProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.PatientProfileResponse;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;

import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.InvalidAgeException;
import com.bloodbridge.exception.PatientProfileAlreadyExistsException;
import com.bloodbridge.mapper.PatientProfileMapper;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.PatientProfileServiceImpl;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PatientProfileServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PatientProfileServiceImplTest {

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private PatientProfileMapper patientProfileMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private AuditLoggerService auditLoggerService;

    @InjectMocks
    private PatientProfileServiceImpl patientProfileService;

    private User patientUser;
    private CreatePatientProfileRequest validRequest;
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

        validRequest = CreatePatientProfileRequest.builder()
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
        when(patientProfileMapper.toEntity(any(), any(), any())).thenReturn(patientProfile);
        when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(patientProfile);
        when(patientProfileMapper.toResponse(any())).thenReturn(expectedResponse);

        ApiResponse<PatientProfileResponse> response = patientProfileService.createProfile("sarah.patient@example.com", validRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(expectedResponse.getId(), response.getData().getId());
        verify(patientProfileRepository, times(1)).save(any(PatientProfile.class));
    }

    @Test
    void createProfile_ThrowsException_WhenProfileAlreadyExists() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        when(patientProfileRepository.existsByUserId(patientUser.getId())).thenReturn(true);

        assertThrows(PatientProfileAlreadyExistsException.class, () -> patientProfileService.createProfile("sarah.patient@example.com", validRequest));
    }

    @Test
    void createProfile_ThrowsInvalidAgeException_WhenAgeTooHigh() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        CreatePatientProfileRequest badRequest = CreatePatientProfileRequest.builder()
                .age(125)
                .build();

        assertThrows(InvalidAgeException.class, () -> patientProfileService.createProfile("sarah.patient@example.com", badRequest));
    }

    @Test
    void getMyProfile_Success() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        when(patientProfileRepository.findByUserId(patientUser.getId())).thenReturn(Optional.of(patientProfile));
        when(patientProfileMapper.toResponse(any())).thenReturn(expectedResponse);

        ApiResponse<PatientProfileResponse> response = patientProfileService.getMyProfile("sarah.patient@example.com");

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(expectedResponse.getEmail(), response.getData().getEmail());
    }
}
