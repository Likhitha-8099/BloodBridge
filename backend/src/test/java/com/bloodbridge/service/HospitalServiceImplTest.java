package com.bloodbridge.service;

import com.bloodbridge.dto.request.CreateHospitalRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.*;
import com.bloodbridge.mapper.HospitalMapper;
import com.bloodbridge.repository.BloodInventoryRepository;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.HospitalServiceImpl;
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
 * Unit tests for {@link HospitalServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HospitalServiceImplTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BloodInventoryRepository bloodInventoryRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private HospitalMapper hospitalMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private AuditLoggerService auditLoggerService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private HospitalServiceImpl hospitalService;

    private User hospitalUser;
    private CreateHospitalRequest validRequest;
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

        validRequest = CreateHospitalRequest.builder()
                .hospitalName("City Hospital")
                .registrationNumber("HOSP-1234")
                .contactEmail("info@cityhospital.com")
                .contactPhone("080-1234567")
                .address("100 Corporate Rd")
                .city("Bangalore")
                .state("Karnataka")
                .licenseNumber("LIC-8891")
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

        ApiResponse<HospitalResponse> response = hospitalService.createHospital("city.hospital@example.com", validRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(expectedResponse.getId(), response.getData().getId());
        verify(hospitalRepository, times(1)).save(any(Hospital.class));
    }

    @Test
    void createHospital_ThrowsException_WhenProfileAlreadyExists() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(hospitalRepository.existsByUserId(hospitalUser.getId())).thenReturn(true);

        assertThrows(HospitalAlreadyExistsException.class, () -> hospitalService.createHospital("city.hospital@example.com", validRequest));
    }

    @Test
    void getMyHospital_Success() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(hospitalRepository.findByUserId(hospitalUser.getId())).thenReturn(Optional.of(hospital));
        when(hospitalMapper.toResponse(any())).thenReturn(expectedResponse);

        ApiResponse<HospitalResponse> response = hospitalService.getMyHospital("city.hospital@example.com");

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(expectedResponse.getRegistrationNumber(), response.getData().getRegistrationNumber());
    }
}
