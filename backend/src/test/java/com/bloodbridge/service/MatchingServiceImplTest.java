package com.bloodbridge.service;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.MatchResponse;
import com.bloodbridge.engine.MatchingEngine;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.mapper.MatchingMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.impl.MatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private MatchingEngine matchingEngine;

    @Mock
    private MatchingMapper matchingMapper;

    @Mock
    private AuditLoggerService auditLoggerService;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private BloodRequest verifiedRequest;
    private DonorProfile eligibleDonor;

    @BeforeEach
    void setUp() {
        verifiedRequest = BloodRequest.builder()
                .id(1L)
                .bloodGroupNeeded(BloodGroup.A_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.HIGH)
                .status(RequestStatus.CREATED)
                .requiredByDate(LocalDate.now().plusDays(2))
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
    }

    @Test
    void triggerMatching_Success() {
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(verifiedRequest));
        when(donorProfileRepository.findByAvailableForDonationTrue()).thenReturn(List.of(eligibleDonor));
        
        MatchResult match = MatchResult.builder()
                .id(1L)
                .bloodRequest(verifiedRequest)
                .donor(eligibleDonor)
                .matchScore(95.0)
                .rank(1)
                .status(MatchStatus.MATCHED)
                .build();

        when(matchingEngine.executeMatching(any(), any())).thenReturn(List.of(match));
        when(matchResultRepository.saveAll(any())).thenReturn(List.of(match));
        when(matchingMapper.toResponse(any())).thenReturn(MatchResponse.builder().id(1L).matchScore(95.0).build());

        ApiResponse<List<MatchResponse>> response = matchingService.triggerMatching(1L);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(RequestStatus.MATCHED, verifiedRequest.getStatus());
        verify(matchResultRepository, times(1)).saveAll(any());
    }

    @Test
    void triggerMatching_ThrowsException_WhenRequestNotFound() {
        when(bloodRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BloodRequestNotFoundException.class, () -> matchingService.triggerMatching(99L));
        verify(matchResultRepository, never()).saveAll(any());
    }
}
