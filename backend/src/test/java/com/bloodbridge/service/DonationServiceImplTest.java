package com.bloodbridge.service;

import com.bloodbridge.dto.*;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.*;
import com.bloodbridge.mapper.DonationMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.impl.DonationServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DonationServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class DonationServiceImplTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationMapper donationMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DonationServiceImpl donationService;

    private User donorUser;
    private User hospitalUser;
    private DonorProfile donorProfile;
    private Hospital hospital;
    private BloodRequest bloodRequest;
    private MatchResult matchResult;
    private Donation donation;
    private DonationResponse expectedResponse;

    @BeforeEach
    void setUp() {
        donorUser = User.builder()
                .id(10L)
                .fullName("Sarah Donor")
                .email("sarah.donor@example.com")
                .role(Role.DONOR)
                .build();

        hospitalUser = User.builder()
                .id(4L)
                .fullName("City Hospital User")
                .email("city.hospital@example.com")
                .role(Role.HOSPITAL)
                .build();

        donorProfile = DonorProfile.builder()
                .id(1L)
                .user(donorUser)
                .bloodGroup(BloodGroup.A_POSITIVE)
                .availableForDonation(true)
                .totalDonations(3)
                .build();

        hospital = Hospital.builder()
                .id(1L)
                .user(hospitalUser)
                .hospitalName("City Hospital")
                .verified(true)
                .build();

        bloodRequest = BloodRequest.builder()
                .id(1L)
                .unitsRequired(2)
                .status(RequestStatus.VERIFIED)
                .build();

        matchResult = MatchResult.builder()
                .id(1L)
                .donor(donorProfile)
                .bloodRequest(bloodRequest)
                .status(MatchStatus.MATCHED)
                .build();

        donation = Donation.builder()
                .id(1L)
                .donor(donorProfile)
                .bloodRequest(bloodRequest)
                .hospital(hospital)
                .matchResult(matchResult)
                .status(DonationStatus.ACCEPTED)
                .build();

        expectedResponse = DonationResponse.builder()
                .id(1L)
                .status(DonationStatus.ACCEPTED)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext(String email, User userContext) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userContext));
    }

    @Test
    void acceptDonation_Success() {
        mockSecurityContext("sarah.donor@example.com", donorUser);
        when(matchResultRepository.findById(1L)).thenReturn(Optional.of(matchResult));
        when(donorProfileRepository.findByUserId(donorUser.getId())).thenReturn(Optional.of(donorProfile));
        when(donationRepository.existsByMatchResultIdAndStatusIn(eq(1L), any())).thenReturn(false);
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);
        when(donationMapper.toResponse(any())).thenReturn(expectedResponse);

        DonationResponse response = donationService.acceptDonation(1L);

        assertNotNull(response);
        assertEquals(DonationStatus.ACCEPTED, response.getStatus());
        assertEquals(MatchStatus.ACCEPTED, matchResult.getStatus());
        verify(donationRepository, times(1)).save(any(Donation.class));
    }

    @Test
    void acceptDonation_ThrowsDuplicateDonationException_WhenAlreadyAccepted() {
        mockSecurityContext("sarah.donor@example.com", donorUser);
        when(matchResultRepository.findById(1L)).thenReturn(Optional.of(matchResult));
        when(donorProfileRepository.findByUserId(donorUser.getId())).thenReturn(Optional.of(donorProfile));
        when(donationRepository.existsByMatchResultIdAndStatusIn(eq(1L), any())).thenReturn(true);

        assertThrows(DuplicateDonationException.class, () -> donationService.acceptDonation(1L));
    }

    @Test
    void confirmDonation_Success() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));
        when(hospitalRepository.findByUserId(hospitalUser.getId())).thenReturn(Optional.of(hospital));
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);
        when(donationMapper.toResponse(any())).thenReturn(DonationResponse.builder().id(1L).status(DonationStatus.CONFIRMED).build());

        DonationResponse response = donationService.confirmDonation(1L);

        assertNotNull(response);
        assertEquals(DonationStatus.CONFIRMED, donation.getStatus());
    }

    @Test
    void completeDonation_Success() {
        mockSecurityContext("city.hospital@example.com", hospitalUser);
        donation.setStatus(DonationStatus.CONFIRMED);

        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));
        when(hospitalRepository.findByUserId(hospitalUser.getId())).thenReturn(Optional.of(hospital));
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);
        when(donationRepository.findByBloodRequestId(1L)).thenReturn(List.of(donation));
        when(donationMapper.toResponse(any())).thenReturn(DonationResponse.builder().id(1L).status(DonationStatus.COMPLETED).build());

        DonationStatusUpdateRequest updateRequest = DonationStatusUpdateRequest.builder()
                .unitsDonated(2)
                .remarks("Successful")
                .build();

        DonationResponse response = donationService.completeDonation(1L, updateRequest);

        assertNotNull(response);
        assertEquals(DonationStatus.COMPLETED, donation.getStatus());
        assertFalse(donorProfile.getAvailableForDonation());
        assertEquals(4, donorProfile.getTotalDonations());
        assertEquals(RequestStatus.COMPLETED, bloodRequest.getStatus());
        verify(donorProfileRepository, times(1)).save(donorProfile);
    }
}
