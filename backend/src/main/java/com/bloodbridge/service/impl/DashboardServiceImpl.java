package com.bloodbridge.service.impl;

import com.bloodbridge.dto.*;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.enums.MatchStatus;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.exception.DashboardDataException;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Dashboard statistics and analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonationRepository donationRepository;
    private final MatchResultRepository matchResultRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardOverview() {
        try {
            return DashboardResponse.builder()
                    .userStatistics(getUserStatistics())
                    .requestStatistics(getRequestStatistics())
                    .donationStatistics(getDonationStatistics())
                    .matchingStatistics(getMatchingStatistics())
                    .notificationStatistics(getNotificationStatistics())
                    .build();
        } catch (Exception e) {
            log.error("Failed to compile dashboard overview: {}", e.getMessage());
            throw new DashboardDataException("Unable to load dashboard overview statistics: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        long total = userRepository.count();
        long donors = donorProfileRepository.count();
        long patients = patientProfileRepository.count();
        long hospitals = hospitalRepository.count();
        long active = userRepository.countByActive(true);
        long inactive = userRepository.countByActive(false);

        return UserStatisticsResponse.builder()
                .totalUsers(total)
                .totalDonors(donors)
                .totalPatients(patients)
                .totalHospitals(hospitals)
                .activeUsers(active)
                .inactiveUsers(inactive)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RequestStatisticsResponse getRequestStatistics() {
        long total = bloodRequestRepository.count();
        long pending = bloodRequestRepository.countByStatus(RequestStatus.PENDING);
        long verified = bloodRequestRepository.countByStatus(RequestStatus.VERIFIED);
        long matched = bloodRequestRepository.countByStatus(RequestStatus.MATCHED);
        long completed = bloodRequestRepository.countByStatus(RequestStatus.COMPLETED);
        long cancelled = bloodRequestRepository.countByStatus(RequestStatus.CANCELLED);
        long rejected = bloodRequestRepository.countByStatus(RequestStatus.REJECTED);

        return RequestStatisticsResponse.builder()
                .totalRequests(total)
                .pendingRequests(pending)
                .verifiedRequests(verified)
                .matchedRequests(matched)
                .completedRequests(completed)
                .cancelledRequests(cancelled)
                .rejectedRequests(rejected)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DonationStatisticsResponse getDonationStatistics() {
        long total = donationRepository.count();
        long completed = donationRepository.countByStatus(DonationStatus.COMPLETED);
        long pending = donationRepository.countByStatus(DonationStatus.PENDING) +
                donationRepository.countByStatus(DonationStatus.ACCEPTED) +
                donationRepository.countByStatus(DonationStatus.CONFIRMED);
        long cancelled = donationRepository.countByStatus(DonationStatus.CANCELLED) +
                donationRepository.countByStatus(DonationStatus.REJECTED);

        // Completion Rate
        double completionRate = total == 0 ? 0.0 : ((double) completed / total) * 100.0;

        return DonationStatisticsResponse.builder()
                .totalDonations(total)
                .completedDonations(completed)
                .pendingDonations(pending)
                .cancelledDonations(cancelled)
                .donationCompletionRate(completionRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MatchingStatisticsResponse getMatchingStatistics() {
        long total = matchResultRepository.count();
        long accepted = matchResultRepository.countByStatus(MatchStatus.ACCEPTED);
        long rejected = matchResultRepository.countByStatus(MatchStatus.REJECTED);
        long active = matchResultRepository.countByStatus(MatchStatus.MATCHED);

        double rate = total == 0 ? 0.0 : ((double) accepted / total) * 100.0;

        return MatchingStatisticsResponse.builder()
                .totalMatches(total)
                .acceptedMatches(accepted)
                .rejectedMatches(rejected)
                .activeMatches(active)
                .matchingSuccessRate(rate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatisticsResponse getNotificationStatistics() {
        long total = notificationRepository.count();
        long sent = notificationRepository.countByStatus(NotificationStatus.SENT);
        long failed = notificationRepository.countByStatus(NotificationStatus.FAILED);
        long unread = notificationRepository.countByReadStatus(false);

        return NotificationStatisticsResponse.builder()
                .totalNotifications(total)
                .sentNotifications(sent)
                .failedNotifications(failed)
                .unreadNotifications(unread)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BloodGroupAnalyticsResponse getBloodGroupAnalytics() {
        // Initialize maps with 0 for all blood groups
        Map<String, Long> donorMap = new LinkedHashMap<>();
        Map<String, Long> requestMap = new LinkedHashMap<>();
        for (BloodGroup bg : BloodGroup.values()) {
            donorMap.put(bg.name(), 0L);
            requestMap.put(bg.name(), 0L);
        }

        // Fill donor counts
        List<Object[]> donorDistribution = donorProfileRepository.getBloodGroupDistribution();
        for (Object[] row : donorDistribution) {
            BloodGroup bg = (BloodGroup) row[0];
            Long count = ((Number) row[1]).longValue();
            donorMap.put(bg.name(), count);
        }

        // Fill request counts
        List<Object[]> requestDistribution = bloodRequestRepository.getBloodGroupDistribution();
        for (Object[] row : requestDistribution) {
            BloodGroup bg = (BloodGroup) row[0];
            Long count = ((Number) row[1]).longValue();
            requestMap.put(bg.name(), count);
        }

        return BloodGroupAnalyticsResponse.builder()
                .donorDistribution(donorMap)
                .requestDistribution(requestMap)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopDonorResponse> getTopDonors() {
        List<Object[]> queryResults = donorProfileRepository.findTopDonors(PageRequest.of(0, 10));
        return queryResults.stream()
                .map(row -> TopDonorResponse.builder()
                        .donorName((String) row[0])
                        .bloodGroup((BloodGroup) row[1])
                        .totalDonations(((Number) row[2]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopHospitalResponse> getTopHospitals() {
        List<Object[]> queryResults = hospitalRepository.findTopHospitals(PageRequest.of(0, 10));
        return queryResults.stream()
                .map(row -> TopHospitalResponse.builder()
                        .hospitalName((String) row[0])
                        .totalRequests(((Number) row[1]).longValue())
                        .totalDonations(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyDonationTrends() {
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> databaseCounts = donationRepository.getMonthlyDonationCounts(twelveMonthsAgo);

        // Prepopulate last 12 months
        List<MonthlyTrendResponse> trends = new ArrayList<>();
        LocalDate runner = LocalDate.now().minusMonths(11);
        for (int i = 0; i < 12; i++) {
            String monthName = runner.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            trends.add(MonthlyTrendResponse.builder()
                    .month(monthName)
                    .totalDonations(0L)
                    .build());
            runner = runner.plusMonths(1);
        }

        // Merge database results
        for (Object[] row : databaseCounts) {
            int monthNum = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            String monthName = java.time.Month.of(monthNum).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            for (MonthlyTrendResponse trend : trends) {
                if (trend.getMonth().equalsIgnoreCase(monthName)) {
                    trend.setTotalDonations(count);
                    break;
                }
            }
        }

        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyRequestTrends() {
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> databaseCounts = bloodRequestRepository.getMonthlyRequestCounts(twelveMonthsAgo);

        // Prepopulate last 12 months
        List<MonthlyTrendResponse> trends = new ArrayList<>();
        LocalDate runner = LocalDate.now().minusMonths(11);
        for (int i = 0; i < 12; i++) {
            String monthName = runner.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            trends.add(MonthlyTrendResponse.builder()
                    .month(monthName)
                    .totalRequests(0L)
                    .build());
            runner = runner.plusMonths(1);
        }

        // Merge database results
        for (Object[] row : databaseCounts) {
            int monthNum = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            String monthName = java.time.Month.of(monthNum).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            for (MonthlyTrendResponse trend : trends) {
                if (trend.getMonth().equalsIgnoreCase(monthName)) {
                    trend.setTotalRequests(count);
                    break;
                }
            }
        }

        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth() {
        String dbConn = "UP";
        long totalRecords = 0;
        long activeUsers = 0;
        String queueStatus = "ACTIVE";

        try {
            long userCount = userRepository.count();
            long requestCount = bloodRequestRepository.count();
            long donationCount = donationRepository.count();
            long matchCount = matchResultRepository.count();
            long notificationCount = notificationRepository.count();

            totalRecords = userCount + requestCount + donationCount + matchCount + notificationCount;
            activeUsers = userRepository.countByActive(true);

            long pendingNotifications = notificationRepository.countByStatus(NotificationStatus.PENDING);
            if (pendingNotifications > 0) {
                queueStatus = "PENDING_ALERTS";
            }
        } catch (Exception e) {
            log.error("Database health diagnostics check failed: {}", e.getMessage());
            dbConn = "DOWN";
        }

        return SystemHealthResponse.builder()
                .databaseConnectivity(dbConn)
                .totalRecords(totalRecords)
                .activeUsers(activeUsers)
                .notificationQueueStatus(queueStatus)
                .apiHealth(dbConn)
                .build();
    }
}
