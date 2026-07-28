package com.bloodbridge.service;

import com.bloodbridge.dto.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DashboardServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getUserStatistics_Success() {
        when(userRepository.count()).thenReturn(100L);
        when(donorProfileRepository.count()).thenReturn(40L);
        when(patientProfileRepository.count()).thenReturn(50L);
        when(hospitalRepository.count()).thenReturn(10L);
        when(userRepository.countByActive(true)).thenReturn(95L);
        when(userRepository.countByActive(false)).thenReturn(5L);

        UserStatisticsResponse stats = dashboardService.getUserStatistics();

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(40L, stats.getTotalDonors());
        assertEquals(50L, stats.getTotalPatients());
        assertEquals(10L, stats.getTotalHospitals());
        assertEquals(95L, stats.getActiveUsers());
        assertEquals(5L, stats.getInactiveUsers());
    }

    @Test
    void getRequestStatistics_Success() {
        when(bloodRequestRepository.count()).thenReturn(50L);
        when(bloodRequestRepository.countByStatus(RequestStatus.PENDING)).thenReturn(10L);
        when(bloodRequestRepository.countByStatus(RequestStatus.VERIFIED)).thenReturn(15L);
        when(bloodRequestRepository.countByStatus(RequestStatus.MATCHED)).thenReturn(10L);
        when(bloodRequestRepository.countByStatus(RequestStatus.COMPLETED)).thenReturn(10L);
        when(bloodRequestRepository.countByStatus(RequestStatus.CANCELLED)).thenReturn(3L);
        when(bloodRequestRepository.countByStatus(RequestStatus.REJECTED)).thenReturn(2L);

        RequestStatisticsResponse stats = dashboardService.getRequestStatistics();

        assertNotNull(stats);
        assertEquals(50L, stats.getTotalRequests());
        assertEquals(10L, stats.getPendingRequests());
        assertEquals(15L, stats.getVerifiedRequests());
        assertEquals(10L, stats.getMatchedRequests());
        assertEquals(10L, stats.getCompletedRequests());
        assertEquals(3L, stats.getCancelledRequests());
        assertEquals(2L, stats.getRejectedRequests());
    }

    @Test
    void getDonationStatistics_Success() {
        when(donationRepository.count()).thenReturn(20L);
        when(donationRepository.countByStatus(DonationStatus.COMPLETED)).thenReturn(15L);
        when(donationRepository.countByStatus(DonationStatus.PENDING)).thenReturn(1L);
        when(donationRepository.countByStatus(DonationStatus.ACCEPTED)).thenReturn(2L);
        when(donationRepository.countByStatus(DonationStatus.CONFIRMED)).thenReturn(1L);
        when(donationRepository.countByStatus(DonationStatus.CANCELLED)).thenReturn(1L);
        when(donationRepository.countByStatus(DonationStatus.REJECTED)).thenReturn(0L);

        DonationStatisticsResponse stats = dashboardService.getDonationStatistics();

        assertNotNull(stats);
        assertEquals(20L, stats.getTotalDonations());
        assertEquals(15L, stats.getCompletedDonations());
        assertEquals(4L, stats.getPendingDonations()); // 1 Pending + 2 Accepted + 1 Confirmed
        assertEquals(1L, stats.getCancelledDonations()); // 1 Cancelled + 0 Rejected
        assertEquals(75.0, stats.getDonationCompletionRate()); // 15 / 20 * 100
    }

    @Test
    void getMatchingStatistics_Success() {
        when(matchResultRepository.count()).thenReturn(30L);
        when(matchResultRepository.countByStatus(MatchStatus.ACCEPTED)).thenReturn(15L);
        when(matchResultRepository.countByStatus(MatchStatus.REJECTED)).thenReturn(5L);
        when(matchResultRepository.countByStatus(MatchStatus.MATCHED)).thenReturn(10L);

        MatchingStatisticsResponse stats = dashboardService.getMatchingStatistics();

        assertNotNull(stats);
        assertEquals(30L, stats.getTotalMatches());
        assertEquals(15L, stats.getAcceptedMatches());
        assertEquals(5L, stats.getRejectedMatches());
        assertEquals(10L, stats.getActiveMatches());
        assertEquals(50.0, stats.getMatchingSuccessRate()); // 15 / 30 * 100
    }

    @Test
    void getTopDonors_Success() {
        Object[] row1 = new Object[]{"Sarah Donor", BloodGroup.A_POSITIVE, 12};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row1);
        when(donorProfileRepository.findTopDonors(any(Pageable.class))).thenReturn(rows);

        List<TopDonorResponse> topDonors = dashboardService.getTopDonors();

        assertNotNull(topDonors);
        assertEquals(1, topDonors.size());
        assertEquals("Sarah Donor", topDonors.get(0).getDonorName());
        assertEquals(BloodGroup.A_POSITIVE, topDonors.get(0).getBloodGroup());
        assertEquals(12, topDonors.get(0).getTotalDonations());
    }

    @Test
    void getTopHospitals_Success() {
        Object[] row1 = new Object[]{"City General Hospital", 45L, 30L};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row1);
        when(hospitalRepository.findTopHospitals(any(Pageable.class))).thenReturn(rows);

        List<TopHospitalResponse> topHospitals = dashboardService.getTopHospitals();

        assertNotNull(topHospitals);
        assertEquals(1, topHospitals.size());
        assertEquals("City General Hospital", topHospitals.get(0).getHospitalName());
        assertEquals(45L, topHospitals.get(0).getTotalRequests());
        assertEquals(30L, topHospitals.get(0).getTotalDonations());
    }

    @Test
    void getSystemHealth_Success() {
        when(userRepository.count()).thenReturn(100L);
        when(bloodRequestRepository.count()).thenReturn(50L);
        when(donationRepository.count()).thenReturn(20L);
        when(matchResultRepository.count()).thenReturn(30L);
        when(notificationRepository.count()).thenReturn(200L);
        when(userRepository.countByActive(true)).thenReturn(95L);
        when(notificationRepository.countByStatus(NotificationStatus.PENDING)).thenReturn(0L);

        SystemHealthResponse health = dashboardService.getSystemHealth();

        assertNotNull(health);
        assertEquals("UP", health.getDatabaseConnectivity());
        assertEquals("UP", health.getApiHealth());
        assertEquals(400L, health.getTotalRecords()); // 100 + 50 + 20 + 30 + 200
        assertEquals(95L, health.getActiveUsers());
        assertEquals("ACTIVE", health.getNotificationQueueStatus());
    }
}
