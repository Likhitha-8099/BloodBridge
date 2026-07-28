package com.bloodbridge.service;

import com.bloodbridge.dto.*;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.exception.*;
import com.bloodbridge.mapper.BloodRequestMapper;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.BloodRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BloodRequestServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BloodRequestServiceImplTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BloodRequestMapper bloodRequestMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BloodRequestServiceImpl bloodRequestService;

    private User patientUser;
    private User hospitalUser;
    private PatientProfile patientProfile;
    private Hospital hospital;
    private BloodRequestCreateRequest validCreateRequest;
    private BloodRequest bloodRequest;
    private BloodRequestResponse expectedResponse;

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

        hospitalUser = User.builder()
                .id(4L)
                .fullName("City Hospital User")
                .email("city.hospital@example.com")
                .password("encodedPass")
                .phoneNumber("9876543210")
                .role(Role.HOSPITAL)
                .active(true)
                .build();

        patientProfile = PatientProfile.builder()
                .id(1L)
                .user(patientUser)
                .age(30)
                .build();

        hospital = Hospital.builder()
                .id(1L)
                .user(hospitalUser)
                .hospitalName("City Hospital")
                .verified(true)
                .build();

        validCreateRequest = BloodRequestCreateRequest.builder()
                .hospitalId(1L)
                .bloodGroupNeeded(BloodGroup.A_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.HIGH)
                .reason("Surgery")
                .requiredByDate(LocalDate.now().plusDays(5))
                .build();

        bloodRequest = BloodRequest.builder()
                .id(1L)
                .patient(patientProfile)
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.A_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.HIGH)
                .reason("Surgery")
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(5))
                .status(RequestStatus.PENDING)
                .build();

        expectedResponse = BloodRequestResponse.builder()
                .id(1L)
                .patientId(1L)
                .patientName("Sarah Patient")
                .hospitalId(1L)
                .hospitalName("City Hospital")
                .bloodGroupNeeded(BloodGroup.A_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.HIGH)
                .status(RequestStatus.PENDING)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext(String email, User userContext) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userContext));
    }

    @Test
    void createRequest_Success() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        when(patientProfileRepository.findByUserId(patientUser.getId())).thenReturn(Optional.of(patientProfile));
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(bloodRequestMapper.toEntity(any(), any(), any())).thenReturn(bloodRequest);
        when(bloodRequestRepository.save(any(BloodRequest.class))).thenReturn(bloodRequest);
        when(bloodRequestMapper.toResponse(any())).thenReturn(expectedResponse);

        BloodRequestResponse response = bloodRequestService.createRequest(validCreateRequest);

        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        assertEquals(RequestStatus.PENDING, response.getStatus());
        verify(bloodRequestRepository, times(1)).save(any(BloodRequest.class));
    }

    @Test
    void createRequest_ThrowsException_WhenUnitsRequiredIsZero() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        when(patientProfileRepository.findByUserId(patientUser.getId())).thenReturn(Optional.of(patientProfile));
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

        BloodRequestCreateRequest badRequest = BloodRequestCreateRequest.builder()
                .hospitalId(1L)
                .unitsRequired(0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> bloodRequestService.createRequest(badRequest));
    }

    @Test
    void updateRequest_ThrowsException_WhenRequestCompleted() {
        mockSecurityContext("sarah.patient@example.com", patientUser);
        bloodRequest.setStatus(RequestStatus.COMPLETED);
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));

        BloodRequestUpdateRequest updateRequest = BloodRequestUpdateRequest.builder()
                .unitsRequired(3)
                .build();

        assertThrows(InvalidRequestStateException.class, () -> bloodRequestService.updateRequest(1L, updateRequest));
    }

    @Test
    void verifyRequest_Success() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));
        when(hospitalRepository.findByUserId(hospitalUser.getId())).thenReturn(Optional.of(hospital));

        RequestStatusResponse response = bloodRequestService.verifyRequest(1L);

        assertNotNull(response);
        assertEquals(RequestStatus.VERIFIED, response.getStatus());
        assertEquals(RequestStatus.VERIFIED, bloodRequest.getStatus());
    }

    @Test
    void verifyRequest_ThrowsException_WhenHospitalNotAssigned() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        Hospital anotherHospital = Hospital.builder().id(99L).build();
        bloodRequest.setHospital(anotherHospital);

        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));
        when(hospitalRepository.findByUserId(hospitalUser.getId())).thenReturn(Optional.of(hospital));

        assertThrows(IllegalArgumentException.class, () -> bloodRequestService.verifyRequest(1L));
    }
}
