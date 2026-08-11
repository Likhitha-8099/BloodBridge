package com.bloodbridge.service;

import com.bloodbridge.dto.request.LocationUpdateDTO;
import com.bloodbridge.dto.response.DonorLiveLocationDTO;
import com.bloodbridge.dto.response.EtaResultDTO;
import com.bloodbridge.dto.response.TrackingAnalyticsDTO;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.InvalidRequestStateException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.impl.LocationTrackingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocationTrackingServiceImpl covering GPS pipeline, smart filtering,
 * ETA computation, tracking status determination, and analytics.
 */
@ExtendWith(MockitoExtension.class)
class LocationTrackingServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private DonorProfileRepository donorProfileRepository;
    @Mock private BloodRequestRepository bloodRequestRepository;
    @Mock private EmergencyResponseRepository emergencyResponseRepository;
    @Mock private DonorLiveLocationRepository donorLiveLocationRepository;
    @Mock private LocationService locationService;
    @Mock private EtaEngineService etaEngineService;
    @Mock private RealtimeService realtimeService;
    @Mock private com.bloodbridge.service.EmergencyResponseService emergencyResponseService;

    @InjectMocks
    private LocationTrackingServiceImpl service;

    private User user;
    private DonorProfile donor;
    private Hospital hospital;
    private BloodRequest request;
    private LocationUpdateDTO dto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).fullName("Test Donor").email("donor@test.com")
                .phoneNumber("9999999999").role(Role.DONOR).build();

        donor = DonorProfile.builder().id(10L).user(user).email("donor@test.com")
                .bloodGroup(BloodGroup.O_POSITIVE).build();

        hospital = Hospital.builder().id(5L).hospitalName("City Hospital")
                .latitude(12.9698).longitude(77.7499).build();

        request = BloodRequest.builder().id(100L).hospital(hospital)
                .bloodGroupNeeded(BloodGroup.O_POSITIVE)
                .status(RequestStatus.PENDING).build();

        dto = LocationUpdateDTO.builder()
                .bloodRequestId(100L)
                .latitude(12.9716)
                .longitude(77.5946)
                .speedKmh(30.0)
                .accuracyMeters(10.0)
                .build();
    }

    // ── Happy path: donor accepted, moving ────────────────────────────────────

    @Test
    void processLocationUpdate_acceptedDonor_movingState_savesAndBroadcasts() {
        // Given
        when(userRepository.findByEmail("donor@test.com")).thenReturn(Optional.of(user));
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(donor));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.ACCEPTED)).thenReturn(true);
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.STARTED_TRAVEL)).thenReturn(false);
        when(donorLiveLocationRepository.findLatestByDonorIdAndBloodRequestId(10L, 100L))
                .thenReturn(Optional.empty());
        when(locationService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(5.0); // 5 km from hospital
        when(etaEngineService.calculateEta(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(EtaResultDTO.builder().etaMinutes(10)
                        .estimatedArrivalTime(LocalDateTime.now().plusMinutes(10))
                        .travelDistanceKm(5.0).build());
        when(donorLiveLocationRepository.save(any(DonorLiveLocation.class)))
                .thenAnswer(inv -> {
                    DonorLiveLocation loc = inv.getArgument(0);
                    loc.setId(999L);
                    return loc;
                });

        // When
        DonorLiveLocationDTO result = service.processLocationUpdate("donor@test.com", dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrackingStatus()).isEqualTo(TrackingStatus.MOVING);
        assertThat(result.getDonorName()).isEqualTo("Test Donor");
        assertThat(result.getEtaMinutes()).isEqualTo(10);
        assertThat(result.getDistanceRemainingKm()).isEqualTo(5.0);
        verify(donorLiveLocationRepository).save(any(DonorLiveLocation.class));
    }

    // ── Auto-arrival: distance < 100m ─────────────────────────────────────────

    @Test
    void processLocationUpdate_withinAutoArrivalThreshold_triggersReachHospital() {
        // Given
        when(userRepository.findByEmail("donor@test.com")).thenReturn(Optional.of(user));
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(donor));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.ACCEPTED)).thenReturn(true);
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.STARTED_TRAVEL)).thenReturn(false);
        when(donorLiveLocationRepository.findLatestByDonorIdAndBloodRequestId(10L, 100L))
                .thenReturn(Optional.empty());
        // Distance = 0.05 km = 50 m — below 100m threshold
        when(locationService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(0.05);
        when(etaEngineService.calculateEta(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(EtaResultDTO.builder().etaMinutes(0)
                        .estimatedArrivalTime(LocalDateTime.now())
                        .travelDistanceKm(0.05).build());
        when(donorLiveLocationRepository.save(any(DonorLiveLocation.class)))
                .thenAnswer(inv -> {
                    DonorLiveLocation loc = inv.getArgument(0);
                    loc.setId(998L);
                    return loc;
                });

        // When
        DonorLiveLocationDTO result = service.processLocationUpdate("donor@test.com", dto);

        // Then
        assertThat(result.getTrackingStatus()).isEqualTo(TrackingStatus.REACHED);
        verify(emergencyResponseService).reachHospital("donor@test.com", 100L);
    }

    // ── Donor stopped (speed ~ 0) ─────────────────────────────────────────────

    @Test
    void processLocationUpdate_lowSpeed_stoppedStatus() {
        dto.setSpeedKmh(0.1); // Below MOVING threshold of 0.5 km/h
        when(userRepository.findByEmail("donor@test.com")).thenReturn(Optional.of(user));
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(donor));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.ACCEPTED)).thenReturn(true);
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.STARTED_TRAVEL)).thenReturn(false);
        when(donorLiveLocationRepository.findLatestByDonorIdAndBloodRequestId(10L, 100L))
                .thenReturn(Optional.empty());
        when(locationService.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(3.0);
        when(etaEngineService.calculateEta(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(EtaResultDTO.builder().etaMinutes(8)
                        .estimatedArrivalTime(LocalDateTime.now().plusMinutes(8))
                        .travelDistanceKm(3.0).build());
        when(donorLiveLocationRepository.save(any(DonorLiveLocation.class)))
                .thenAnswer(inv -> {
                    DonorLiveLocation loc = inv.getArgument(0);
                    loc.setId(997L);
                    return loc;
                });

        DonorLiveLocationDTO result = service.processLocationUpdate("donor@test.com", dto);
        assertThat(result.getTrackingStatus()).isEqualTo(TrackingStatus.STOPPED);
    }

    // ── Validation: donor not found ───────────────────────────────────────────

    @Test
    void processLocationUpdate_unknownDonorEmail_throwsUserNotFoundException() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.processLocationUpdate("ghost@test.com", dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ── Validation: donor has not accepted the request ────────────────────────

    @Test
    void processLocationUpdate_donorNotAccepted_throwsInvalidRequestStateException() {
        when(userRepository.findByEmail("donor@test.com")).thenReturn(Optional.of(user));
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(donor));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.ACCEPTED)).thenReturn(false);
        when(emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                100L, 10L, EmergencyResponseStatus.STARTED_TRAVEL)).thenReturn(false);

        assertThatThrownBy(() -> service.processLocationUpdate("donor@test.com", dto))
                .isInstanceOf(InvalidRequestStateException.class)
                .hasMessageContaining("has not accepted");
    }

    // ── getLiveLocationsForRequest ─────────────────────────────────────────────

    @Test
    void getLiveLocationsForRequest_returnsLatestPerDonor() {
        DonorLiveLocation loc = DonorLiveLocation.builder()
                .id(1L).donorId(10L).bloodRequestId(100L).hospitalId(5L)
                .latitude(12.97).longitude(77.59).trackingStatus(TrackingStatus.MOVING)
                .distanceRemainingKm(4.0).etaMinutes(7)
                .lastUpdated(LocalDateTime.now()).build();

        when(donorLiveLocationRepository.findLatestForAllDonorsByBloodRequestId(100L))
                .thenReturn(Collections.singletonList(loc));
        when(donorProfileRepository.findById(10L)).thenReturn(Optional.of(donor));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(request));

        List<DonorLiveLocationDTO> result = service.getLiveLocationsForRequest(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDonorId()).isEqualTo(10L);
        assertThat(result.get(0).getTrackingStatus()).isEqualTo(TrackingStatus.MOVING);
    }

    // ── getTrackingAnalytics ──────────────────────────────────────────────────

    @Test
    void getTrackingAnalytics_returnsPopulatedDTO() {
        DonorLiveLocation activeRow = DonorLiveLocation.builder()
                .donorId(10L).trackingStatus(TrackingStatus.MOVING).build();
        DonorLiveLocation reachedRow = DonorLiveLocation.builder()
                .donorId(20L).trackingStatus(TrackingStatus.REACHED).build();

        when(donorLiveLocationRepository.findByTrackingStatusIn(
                Arrays.asList(TrackingStatus.STARTED, TrackingStatus.MOVING, TrackingStatus.STOPPED)))
                .thenReturn(Collections.singletonList(activeRow));
        when(donorLiveLocationRepository.findAverageEtaMinutesSince(any())).thenReturn(12.0);
        when(donorLiveLocationRepository.findAverageSpeedKmhSince(any())).thenReturn(35.0);
        when(donorLiveLocationRepository.count()).thenReturn(50L);
        when(donorLiveLocationRepository.findByTrackingStatusIn(
                Arrays.asList(TrackingStatus.REACHED, TrackingStatus.COMPLETED)))
                .thenReturn(Collections.singletonList(reachedRow));

        TrackingAnalyticsDTO analytics = service.getTrackingAnalytics();

        assertThat(analytics.getActiveTrackingSessions()).isEqualTo(1L);
        assertThat(analytics.getAverageEtaMinutes()).isEqualTo(12.0);
        assertThat(analytics.getAverageSpeedKmh()).isEqualTo(35.0);
        assertThat(analytics.getTotalTrackingRecords()).isEqualTo(50L);
    }

    // ── getRouteHistory ────────────────────────────────────────────────────────

    @Test
    void getRouteHistory_returnsSortedRoutePoints() {
        DonorLiveLocation pt1 = DonorLiveLocation.builder()
                .id(1L).donorId(10L).bloodRequestId(100L).hospitalId(5L)
                .latitude(12.90).longitude(77.50).trackingStatus(TrackingStatus.STARTED)
                .lastUpdated(LocalDateTime.now().minusMinutes(15)).build();
        DonorLiveLocation pt2 = DonorLiveLocation.builder()
                .id(2L).donorId(10L).bloodRequestId(100L).hospitalId(5L)
                .latitude(12.95).longitude(77.60).trackingStatus(TrackingStatus.MOVING)
                .lastUpdated(LocalDateTime.now().minusMinutes(5)).build();

        when(donorLiveLocationRepository.findRouteByDonorIdAndBloodRequestId(10L, 100L))
                .thenReturn(Arrays.asList(pt1, pt2));
        when(donorProfileRepository.findById(10L)).thenReturn(Optional.of(donor));
        when(bloodRequestRepository.findById(100L)).thenReturn(Optional.of(request));

        List<DonorLiveLocationDTO> route = service.getRouteHistory(100L, 10L);

        assertThat(route).hasSize(2);
        assertThat(route.get(0).getTrackingStatus()).isEqualTo(TrackingStatus.STARTED);
        assertThat(route.get(1).getTrackingStatus()).isEqualTo(TrackingStatus.MOVING);
    }
}
