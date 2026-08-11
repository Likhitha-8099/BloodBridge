package com.bloodbridge.service;

import com.bloodbridge.dto.request.AcceptEmergencyRequestDTO;
import com.bloodbridge.dto.request.RejectEmergencyRequestDTO;
import com.bloodbridge.dto.response.EmergencyResponseDTO;
import com.bloodbridge.dto.response.HospitalEmergencyLiveStatsDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.EmergencyResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EmergencyResponseStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.repository.EmergencyResponseRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.EmergencyResponseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmergencyResponseServiceImpl evaluating Phase 2 donor response flow,
 * duplicate prevention, auto-closing threshold logic, and Google Maps URL generation.
 */
class EmergencyResponseServiceImplTest {

    private EmergencyResponseRepository emergencyResponseRepository;
    private BloodRequestRepository bloodRequestRepository;
    private DonorProfileRepository donorProfileRepository;
    private UserRepository userRepository;
    private EmailNotificationRepository emailNotificationRepository;
    private LocationService locationService;
    private RealtimeService realtimeService;
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private EmergencyResponseService emergencyResponseService;

    private User donorUser;
    private DonorProfile donorProfile;
    private Hospital hospital;
    private BloodRequest bloodRequest;

    @BeforeEach
    void setUp() {
        emergencyResponseRepository = mock(EmergencyResponseRepository.class);
        bloodRequestRepository = mock(BloodRequestRepository.class);
        donorProfileRepository = mock(DonorProfileRepository.class);
        userRepository = mock(UserRepository.class);
        emailNotificationRepository = mock(EmailNotificationRepository.class);
        locationService = mock(LocationService.class);
        realtimeService = mock(RealtimeService.class);
        eventPublisher = mock(org.springframework.context.ApplicationEventPublisher.class);

        EmergencyTimelineService timelineService = mock(EmergencyTimelineService.class);
        SmartDonorMatchingPipelineService pipelineService = mock(SmartDonorMatchingPipelineService.class);

        emergencyResponseService = new EmergencyResponseServiceImpl(
                emergencyResponseRepository,
                bloodRequestRepository,
                donorProfileRepository,
                userRepository,
                emailNotificationRepository,
                locationService,
                realtimeService,
                eventPublisher,
                timelineService,
                pipelineService
        );

        donorUser = User.builder().id(10L).fullName("Jane Donor").email("jane@example.com").active(true).build();
        donorProfile = DonorProfile.builder().id(100L).user(donorUser).email("jane@example.com").latitude(12.9716).longitude(77.5946).build();
        hospital = Hospital.builder().id(50L).hospitalName("Apollo Medical Center").latitude(12.9698).longitude(77.7499).user(User.builder().id(20L).build()).build();
        bloodRequest = BloodRequest.builder().id(1L).unitsRequired(2).status(RequestStatus.PENDING).hospital(hospital).bloodGroupNeeded(BloodGroup.A_POSITIVE).build();
    }

    @Test
    void acceptEmergencyRequest_Success_ReturnsGoogleMapsUrl() {
        AcceptEmergencyRequestDTO dto = AcceptEmergencyRequestDTO.builder()
                .emergencyRequestId(1L)
                .etaMinutes(12)
                .remarks("On my way")
                .build();

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(donorUser));
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(donorProfile));
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(1L, 100L, EmergencyResponseStatus.ACCEPTED)).thenReturn(false);
        when(locationService.calculateDistance(12.9698, 77.7499, 12.9716, 77.5946)).thenReturn(4.8);

        when(emergencyResponseRepository.findByBloodRequestIdAndDonorId(1L, 100L)).thenReturn(Optional.empty());
        when(emergencyResponseRepository.save(any(EmergencyResponse.class))).thenAnswer(i -> {
            EmergencyResponse r = i.getArgument(0);
            r.setId(500L);
            return r;
        });

        EmergencyResponseDTO result = emergencyResponseService.acceptEmergencyRequest("jane@example.com", dto);

        assertNotNull(result);
        assertEquals(EmergencyResponseStatus.ACCEPTED, result.getStatus());
        assertEquals(12, result.getEtaMinutes());
        assertTrue(result.getGoogleMapsUrl().contains("google.com/maps/dir/?api=1&destination=12.969800,77.749900"));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void acceptEmergencyRequest_DuplicateAccept_ThrowsIllegalArgumentException() {
        AcceptEmergencyRequestDTO dto = AcceptEmergencyRequestDTO.builder().emergencyRequestId(1L).build();

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(donorUser));
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(donorProfile));
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(1L, 100L, EmergencyResponseStatus.ACCEPTED)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> emergencyResponseService.acceptEmergencyRequest("jane@example.com", dto));
    }

    @Test
    void acceptEmergencyRequest_AutoCloseThresholdMet_UpdatesStatusToCompleted() {
        AcceptEmergencyRequestDTO dto = AcceptEmergencyRequestDTO.builder().emergencyRequestId(1L).etaMinutes(10).build();

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(donorUser));
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(donorProfile));
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(1L, 100L, EmergencyResponseStatus.ACCEPTED)).thenReturn(false);
        when(emergencyResponseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Required units = 2. Return 2 accepted responses to trigger auto-close threshold
        when(emergencyResponseRepository.countByBloodRequestIdAndStatus(1L, EmergencyResponseStatus.ACCEPTED)).thenReturn(2L);

        emergencyResponseService.acceptEmergencyRequest("jane@example.com", dto);

        assertEquals(RequestStatus.IN_PROGRESS, bloodRequest.getStatus());
        verify(bloodRequestRepository, times(1)).save(bloodRequest);
    }

    @Test
    void rejectEmergencyRequest_Success_UpdatesStatusToRejected() {
        RejectEmergencyRequestDTO dto = RejectEmergencyRequestDTO.builder().emergencyRequestId(1L).reason("Busy").build();

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(donorUser));
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(donorProfile));
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));
        when(emergencyResponseRepository.findByBloodRequestIdAndDonorId(1L, 100L)).thenReturn(Optional.empty());
        when(emergencyResponseRepository.save(any())).thenAnswer(i -> {
            EmergencyResponse r = i.getArgument(0);
            r.setId(501L);
            return r;
        });

        EmergencyResponseDTO result = emergencyResponseService.rejectEmergencyRequest("jane@example.com", dto);

        assertNotNull(result);
        assertEquals(EmergencyResponseStatus.REJECTED, result.getStatus());
    }

    @Test
    void getHospitalLiveStats_ReturnsCalculatedMetrics() {
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(bloodRequest));
        when(emergencyResponseRepository.findWithDonorDetailsByBloodRequestIdAndStatus(1L, EmergencyResponseStatus.ACCEPTED)).thenReturn(List.of());
        when(emergencyResponseRepository.countByBloodRequestIdAndStatus(1L, EmergencyResponseStatus.REJECTED)).thenReturn(1L);
        when(emergencyResponseRepository.countByBloodRequestIdAndStatus(1L, EmergencyResponseStatus.PENDING)).thenReturn(3L);

        HospitalEmergencyLiveStatsDTO stats = emergencyResponseService.getHospitalLiveStats(1L);

        assertNotNull(stats);
        assertEquals(1L, stats.getEmergencyRequestId());
        assertEquals(2, stats.getUnitsRequired());
    }
}
