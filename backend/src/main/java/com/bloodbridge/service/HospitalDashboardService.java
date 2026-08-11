package com.bloodbridge.service;

import com.bloodbridge.dto.response.*;

import java.util.List;

/**
 * Service interface for Hospital Dashboard Phase 1 operations.
 */
public interface HospitalDashboardService {

    /**
     * Retrieves full dashboard summary data for the authenticated hospital.
     */
    HospitalDashboardDTO getDashboardData(String email);

    /**
     * Retrieves statistics summary metrics.
     */
    DashboardStatisticsDTO getStatistics(String email);

    /**
     * Retrieves recent blood requests for the hospital.
     */
    List<RecentRequestDTO> getRecentRequests(String email, int limit);

    /**
     * Retrieves recent emergency requests for the hospital.
     */
    List<RecentRequestDTO> getEmergencyRequests(String email, int limit);

    /**
     * Retrieves recent completed donations associated with the hospital.
     */
    List<RecentDonationDTO> getRecentDonations(String email, int limit);

    /**
     * Retrieves nearby available donors for the hospital.
     */
    List<NearbyDonorDTO> getNearbyDonors(String email, int limit);

    /**
     * Retrieves recent notifications for the authenticated user.
     */
    List<NotificationDTO> getNotifications(String email, int limit);

    /**
     * Retrieves analytics trends for requests and blood group distribution.
     */
    AnalyticsDTO getAnalytics(String email);
}
