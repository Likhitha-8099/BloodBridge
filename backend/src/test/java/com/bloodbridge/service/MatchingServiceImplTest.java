package com.bloodbridge.service;

import com.bloodbridge.dto.*;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.*;
import com.bloodbridge.mapper.MatchMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.impl.MatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MatchingServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Spy
    private CompatibilityService compatibilityService;

    @Mock
    private MatchMapper matchMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private User patientUser;
    private User hospitalUser;
    private PatientProfile patientProfile;
    private Hospital hospital;
    private BloodRequest verifiedRequest;
    private BloodRequest pendingRequest;
    private DonorProfile eligibleDonor;
    private DonorProfile ineligibleDonor;

    @BeforeEach
    void setUp() {
        patientUser = User.builder()
                .id(2L)
                .fullName("Sarah Patient")
                .email("sarah.patient@example.com")
                .role(Role.PATIENT)
                .build();

        hospitalUser = User.builder()
                .id(4L)
                .fullName("City Hospital User")
                .email("city.hospital@example.com")
                .role(Role.HOSPITAL)
                .build();

        patientProfile = PatientProfile.builder()
                .id(1L)
                .user(patientUser)
                .build();

        hospital = Hospital.builder()
                .id(1L)
                .user(hospitalUser)
                .hospitalName("City Hospital")
                .city("Bangalore")
                .verified(true)
                .build();

        verifiedRequest = BloodRequest.builder()
                .id(1L)
                .patient(patientProfile)
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.A_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.HIGH)
                .status(RequestStatus.VERIFIED)
                .requiredByDate(LocalDate.now().plusDays(2))
                .build();

        pendingRequest = BloodRequest.builder()
                .id(2L)
                .patient(patientProfile)
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.A_POSITIVE)
                .status(RequestStatus.PENDING)
                .build();

        eligibleDonor = DonorProfile.builder()
                .id(1L)
                .user(User.builder().id(10L).fullName("Eligible Donor").email("donor1@example.com").role(Role.DONOR).build())
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(25)
                .weight(70.0)
                .gender(Gender.MALE)
                .availableForDonation(true)
                .city("Bangalore")
                .totalDonations(2)
                .updatedAt(LocalDateTime.now())
                .build();

        ineligibleDonor = DonorProfile.builder()
                .id(2L)
                .availableForDonation(false) // ineligible due to availability
                .build();
    }

    @Test
    void getCompatibleBloodGroups_Success() {
        CompatibilityResponse response = matchingService.getCompatibleBloodGroups(BloodGroup.O_NEGATIVE);
        assertNotNull(response);
        assertEquals(BloodGroup.O_NEGATIVE, response.getRequestedBloodGroup());
        assertEquals(1, response.getCompatibleDonors().size());
        assertTrue(response.getCompatibleDonors().contains(BloodGroup.O_NEGATIVE));
    }

    @Test
    void calculateCompatibilityScore_Success() {
        // Same City (+50), Available (+30), Low Donation Count (< 5) (+10), Recently updated (+10) -> 100 points
        Integer score = matchingService.calculateCompatibilityScore(eligibleDonor, verifiedRequest);
        assertEquals(100, score);
    }

    @Test
    void generateMatches_Success() {
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(verifiedRequest));
        when(donorProfileRepository.findAll()).thenReturn(List.of(eligibleDonor, ineligibleDonor));
        when(matchResultRepository.existsByBloodRequestIdAndDonorId(1L, eligibleDonor.getId())).thenReturn(false);
        when(matchResultRepository.save(any(MatchResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(matchMapper.toResponse(any())).thenReturn(MatchResponse.builder().id(1L).status(MatchStatus.MATCHED).build());

        List<MatchResponse> responses = matchingService.generateMatches(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(RequestStatus.MATCHED, verifiedRequest.getStatus());
        verify(matchResultRepository, times(1)).save(any(MatchResult.class));
    }

    @Test
    void generateMatches_ThrowsRequestNotVerifiedException_WhenRequestPending() {
        when(bloodRequestRepository.findById(2L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(RequestNotVerifiedException.class, () -> matchingService.generateMatches(2L));
        verify(matchResultRepository, never()).save(any());
    }
}
