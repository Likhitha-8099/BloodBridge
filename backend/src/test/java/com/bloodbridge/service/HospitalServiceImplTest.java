package com.bloodbridge.service;

import com.bloodbridge.dto.HospitalRequest;
import com.bloodbridge.dto.HospitalResponse;
import com.bloodbridge.dto.HospitalVerificationResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.*;
import com.bloodbridge.mapper.HospitalMapper;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.HospitalServiceImpl;
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
 * Unit tests for {@link HospitalServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class HospitalServiceImplTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HospitalMapper hospitalMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HospitalServiceImpl hospitalService;

    private User hospitalUser;
    private User donorUser;
    private HospitalRequest validRequest;
    private Hospital hospital;
    private HospitalResponse expectedResponse;

    @BeforeEach
    void setUp() {
        hospitalUser = User.builder()
                .id(4L)
                .fullName("City Hospital")
                .email("city.hospital@example.com")
                .password("encodedPass")
                .phoneNumber("9876543210")
                .role(Role.HOSPITAL)
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

        validRequest = HospitalRequest.builder()
                .hospitalName("City Hospital")
                .registrationNumber("HOSP-1234")
                .email("info@cityhospital.com")
                .phoneNumber("080-1234567")
                .address("100 Corporate Rd")
                .city("Bangalore")
                .state("Karnataka")
                .build();

        hospital = Hospital.builder()
                .id(1L)
                .user(hospitalUser)
                .hospitalName("City Hospital")
                .registrationNumber("HOSP-1234")
                .email("info@cityhospital.com")
                .phoneNumber("080-1234567")
                .address("100 Corporate Rd")
                .city("Bangalore")
                .state("Karnataka")
                .verified(false)
                .build();

        expectedResponse = HospitalResponse.builder()
                .id(1L)
                .fullName("City Hospital")
                .userEmail("city.hospital@example.com")
                .hospitalName("City Hospital")
                .registrationNumber("HOSP-1234")
                .email("info@cityhospital.com")
                .phoneNumber("080-1234567")
                .address("100 Corporate Rd")
                .city("Bangalore")
                .state("Karnataka")
                .verified(false)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext(String email, User userContext) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userContext));
    }

    @Test
    void createHospital_Success() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(hospitalRepository.existsByUserId(hospitalUser.getId())).thenReturn(false);
        when(hospitalRepository.existsByRegistrationNumber(validRequest.getRegistrationNumber())).thenReturn(false);
        when(hospitalMapper.toEntity(any(), any())).thenReturn(hospital);
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);
        when(hospitalMapper.toResponse(any())).thenReturn(expectedResponse);

        HospitalResponse response = hospitalService.createHospital(validRequest);

        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        assertFalse(response.getVerified());
        verify(hospitalRepository, times(1)).save(any(Hospital.class));
    }

    @Test
    void createHospital_ThrowsException_WhenUserNotHospital() {
        mockSecurityContext("john.donor@example.com", donorUser);

        assertThrows(IllegalArgumentException.class, () -> hospitalService.createHospital(validRequest));
        verify(hospitalRepository, never()).save(any());
    }

    @Test
    void createHospital_ThrowsException_WhenProfileAlreadyExists() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(hospitalRepository.existsByUserId(hospitalUser.getId())).thenReturn(true);

        assertThrows(HospitalAlreadyExistsException.class, () -> hospitalService.createHospital(validRequest));
    }

    @Test
    void createHospital_ThrowsException_WhenRegistrationNumberExists() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(hospitalRepository.existsByUserId(hospitalUser.getId())).thenReturn(false);
        when(hospitalRepository.existsByRegistrationNumber(validRequest.getRegistrationNumber())).thenReturn(true);

        assertThrows(DuplicateRegistrationNumberException.class, () -> hospitalService.createHospital(validRequest));
    }

    @Test
    void getMyHospital_Success() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(hospitalRepository.findByUserId(hospitalUser.getId())).thenReturn(Optional.of(hospital));
        when(hospitalMapper.toResponse(any())).thenReturn(expectedResponse);

        HospitalResponse response = hospitalService.getMyHospital();

        assertNotNull(response);
        assertEquals(expectedResponse.getRegistrationNumber(), response.getRegistrationNumber());
    }

    @Test
    void verifyHospital_Success() {
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        HospitalVerificationResponse response = hospitalService.verifyHospital(1L);

        assertNotNull(response);
        assertTrue(response.getVerified());
        assertEquals("Hospital verified successfully", response.getMessage());
        assertTrue(hospital.getVerified());
    }
}
