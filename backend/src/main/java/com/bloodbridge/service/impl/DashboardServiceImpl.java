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
    private final EmailNotificationRepository emailNotificationRepository;
    private final PushDeliveryLogRepository pushDeliveryLogRepository;

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
        long donorsCount = donorProfileRepository.count();
        long patients = patientProfileRepository.count();
        long hospitals = hospitalRepository.count();
        long active = userRepository.countByActive(true);
        long inactive = userRepository.countByActive(false);

        List<com.bloodbridge.entity.DonorProfile> donors = donorProfileRepository.findAll();
        List<com.bloodbridge.entity.User> users = userRepository.findAll();

        long availableDonors = donors.stream().filter(dp -> Boolean.TRUE.equals(dp.getAvailableForDonation())).count();
        long emergencyAvailableDonors = donors.stream().filter(dp -> Boolean.TRUE.equals(dp.getEmergencyAvailable())).count();
        long eligibleDonors = donors.stream().filter(dp -> dp.getNextEligibleDate() == null || !dp.getNextEligibleDate().isAfter(LocalDate.now())).count();
        long cooldownDonors = donors.stream().filter(dp -> dp.getNextEligibleDate() != null && dp.getNextEligibleDate().isAfter(LocalDate.now())).count();

        // Blood group distribution
        Map<String, Long> bloodGroupMap = new LinkedHashMap<>();
        for (com.bloodbridge.enums.BloodGroup bg : com.bloodbridge.enums.BloodGroup.values()) {
            bloodGroupMap.put(bg.name(), 0L);
        }
        for (com.bloodbridge.entity.DonorProfile dp : donors) {
            if (dp.getBloodGroup() != null) {
                String key = dp.getBloodGroup().name();
                bloodGroupMap.put(key, bloodGroupMap.getOrDefault(key, 0L) + 1);
            }
        }

        // Gender distribution
        Map<String, Long> genderMap = new LinkedHashMap<>();
        for (com.bloodbridge.entity.DonorProfile dp : donors) {
            String g = dp.getGender() != null ? dp.getGender().name() : "UNSPECIFIED";
            genderMap.put(g, genderMap.getOrDefault(g, 0L) + 1);
        }

        // Age distribution
        Map<String, Long> ageMap = new LinkedHashMap<>();
        ageMap.put("18-25", 0L);
        ageMap.put("26-35", 0L);
        ageMap.put("36-45", 0L);
        ageMap.put("46-55", 0L);
        ageMap.put("56-65", 0L);
        for (com.bloodbridge.entity.DonorProfile dp : donors) {
            if (dp.getAge() != null) {
                int age = dp.getAge();
                if (age <= 25) ageMap.put("18-25", ageMap.get("18-25") + 1);
                else if (age <= 35) ageMap.put("26-35", ageMap.get("26-35") + 1);
                else if (age <= 45) ageMap.put("36-45", ageMap.get("36-45") + 1);
                else if (age <= 55) ageMap.put("46-55", ageMap.get("46-55") + 1);
                else ageMap.put("56-65", ageMap.get("56-65") + 1);
            }
        }

        // Role distribution
        Map<String, Long> roleMap = new LinkedHashMap<>();
        for (com.bloodbridge.entity.User u : users) {
            if (u.getRole() != null) {
                String r = u.getRole().name();
                roleMap.put(r, roleMap.getOrDefault(r, 0L) + 1);
            }
        }

        // Location distributions (Top 10 Cities and States)
        Map<String, Long> cityMap = donors.stream()
                .filter(dp -> dp.getCity() != null && !dp.getCity().isBlank())
                .collect(Collectors.groupingBy(dp -> dp.getCity().trim(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Map<String, Long> stateMap = donors.stream()
                .filter(dp -> dp.getState() != null && !dp.getState().isBlank())
                .collect(Collectors.groupingBy(dp -> dp.getState().trim(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Availability map
        Map<String, Long> availMap = new LinkedHashMap<>();
        availMap.put("Available", availableDonors);
        availMap.put("Unavailable", Math.max(0, donorsCount - availableDonors));
        availMap.put("Emergency Ready", emergencyAvailableDonors);

        // Monthly trends (12 months)
        List<Map<String, Object>> trendList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            String monthName = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + m.getYear();
            long count = users.stream()
                    .filter(u -> u.getCreatedAt() != null &&
                            u.getCreatedAt().getYear() == m.getYear() &&
                            u.getCreatedAt().getMonthValue() == m.getMonthValue())
                    .count();
            Map<String, Object> t = new HashMap<>();
            t.put("month", monthName);
            t.put("Registrations", count);
            trendList.add(t);
        }

        // Automated Real Insights
        List<String> insights = new ArrayList<>();
        if (!donors.isEmpty()) {
            Map.Entry<String, Long> topBg = bloodGroupMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).orElse(null);
            if (topBg != null && topBg.getValue() > 0) {
                String bgFormatted = topBg.getKey().replace("_POSITIVE", "+").replace("_NEGATIVE", "-");
                insights.add("Most common donor blood group: " + bgFormatted + " (" + topBg.getValue() + " donors)");
            }
            long availablePct = donorsCount > 0 ? Math.round(((double) availableDonors / donorsCount) * 100) : 0;
            insights.add(availablePct + "% of registered donors are currently active & available for donation");
            insights.add(cooldownDonors + " donors are currently in 90-day eligibility cooldown period");
            if (!cityMap.isEmpty()) {
                String topCity = cityMap.keySet().iterator().next();
                insights.add("Most represented donor city: " + topCity + " (" + cityMap.get(topCity) + " donors)");
            }
        }

        return UserStatisticsResponse.builder()
                .totalUsers(total)
                .totalDonors(donorsCount)
                .totalPatients(patients)
                .totalHospitals(hospitals)
                .activeUsers(active)
                .inactiveUsers(inactive)
                .availableDonors(availableDonors)
                .emergencyAvailableDonors(emergencyAvailableDonors)
                .eligibleDonors(eligibleDonors)
                .cooldownDonors(cooldownDonors)
                .bloodGroupDistribution(bloodGroupMap)
                .genderDistribution(genderMap)
                .ageGroupDistribution(ageMap)
                .roleDistribution(roleMap)
                .locationCityDistribution(cityMap)
                .locationStateDistribution(stateMap)
                .availabilityDistribution(availMap)
                .monthlyRegistrationTrends(trendList)
                .automatedInsights(insights)
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
        try {
            List<Object[]> donorDistribution = donorProfileRepository.getBloodGroupDistribution();
            if (donorDistribution != null) {
                for (Object[] row : donorDistribution) {
                    if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                        String bgName = (row[0] instanceof BloodGroup) ? ((BloodGroup) row[0]).name() : row[0].toString();
                        Long count = ((Number) row[1]).longValue();
                        donorMap.put(bgName, count);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching donor blood group distribution: {}", e.getMessage());
        }

        // Fill request counts
        try {
            List<Object[]> requestDistribution = bloodRequestRepository.getBloodGroupDistribution();
            if (requestDistribution != null) {
                for (Object[] row : requestDistribution) {
                    if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                        String bgName = (row[0] instanceof BloodGroup) ? ((BloodGroup) row[0]).name() : row[0].toString();
                        Long count = ((Number) row[1]).longValue();
                        requestMap.put(bgName, count);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching request blood group distribution: {}", e.getMessage());
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
        if (queryResults == null || queryResults.isEmpty()) {
            return Collections.emptyList();
        }
        return queryResults.stream()
                .map(row -> {
                    Long id = (row != null && row.length > 0 && row[0] != null) ? ((Number) row[0]).longValue() : null;
                    Long userId = (row != null && row.length > 1 && row[1] != null) ? ((Number) row[1]).longValue() : null;
                    String name = (row != null && row.length > 2 && row[2] != null) ? row[2].toString() : "Registered Donor";
                    String email = (row != null && row.length > 3 && row[3] != null) ? row[3].toString() : null;
                    String city = (row != null && row.length > 4 && row[4] != null) ? row[4].toString() : null;
                    String state = (row != null && row.length > 5 && row[5] != null) ? row[5].toString() : null;
                    BloodGroup bg = null;
                    if (row != null && row.length > 6 && row[6] != null) {
                        bg = (row[6] instanceof BloodGroup) ? (BloodGroup) row[6] : BloodGroup.valueOf(row[6].toString());
                    }
                    Integer donations = (row != null && row.length > 7 && row[7] != null) ? ((Number) row[7]).intValue() : 0;

                    return TopDonorResponse.builder()
                            .id(id)
                            .donorId(id)
                            .userId(userId)
                            .donorName(name)
                            .email(email)
                            .city(city)
                            .state(state)
                            .role("DONOR")
                            .bloodGroup(bg)
                            .totalDonations(donations)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopHospitalResponse> getTopHospitals() {
        log.info("Executing getTopHospitals query for analytics dashboard");
        try {
            List<Object[]> queryResults = hospitalRepository.findTopHospitals(PageRequest.of(0, 10));
            if (queryResults == null || queryResults.isEmpty()) {
                log.info("No top hospital metrics found in database, returning empty list");
                return Collections.emptyList();
            }

            return queryResults.stream()
                    .map(row -> {
                        String name = (row != null && row.length > 0 && row[0] != null) ? row[0].toString() : "Registered Hospital";
                        long reqCount = (row != null && row.length > 1 && row[1] != null) ? ((Number) row[1]).longValue() : 0L;
                        long donCount = (row != null && row.length > 2 && row[2] != null) ? ((Number) row[2]).longValue() : 0L;
                        return TopHospitalResponse.builder()
                                .hospitalName(name)
                                .totalRequests(reqCount)
                                .totalDonations(donCount)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error executing getTopHospitals analytics query: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
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

    @Override
    @Transactional(readOnly = true)
    public com.bloodbridge.dto.response.PushAnalyticsResponse getPushNotificationAnalytics() {
        log.info("Executing getPushNotificationAnalytics query for admin analytics dashboard");
        try {
            long emailsSent = emailNotificationRepository.countByStatus(com.bloodbridge.enums.EmailDeliveryStatus.SENT);
            long emailsFailed = emailNotificationRepository.countByStatus(com.bloodbridge.enums.EmailDeliveryStatus.FAILED);
            long wsDelivered = notificationRepository.countByDeliveryChannel(com.bloodbridge.enums.DeliveryChannel.IN_APP);

            long pushSent = pushDeliveryLogRepository.countByStatus("SENT");
            long pushFailed = pushDeliveryLogRepository.countByStatus("FAILED");
            long totalPush = pushSent + pushFailed;
            double pushSuccessPct = totalPush > 0 ? ((double) pushSent / totalPush) * 100.0 : 100.0;

            double avgLatency = pushDeliveryLogRepository.findAverageLatencyMs();
            long retryCount = pushDeliveryLogRepository.findTotalRetryCount();
            long invalidTokensRemoved = pushDeliveryLogRepository.countDistinctInvalidTokens();

            Map<String, Long> topFailures = new LinkedHashMap<>();
            List<Object[]> failureList = pushDeliveryLogRepository.findTopFailureReasons();
            if (failureList != null) {
                for (Object[] row : failureList) {
                    if (row != null && row.length >= 2 && row[0] != null) {
                        topFailures.put(row[0].toString(), ((Number) row[1]).longValue());
                    }
                }
            }

            return com.bloodbridge.dto.response.PushAnalyticsResponse.builder()
                    .emailsSent(emailsSent)
                    .emailsFailed(emailsFailed)
                    .webSocketDelivered(wsDelivered)
                    .pushSent(pushSent)
                    .pushFailed(pushFailed)
                    .pushSuccessPercentage(Math.round(pushSuccessPct * 10.0) / 10.0)
                    .averagePushLatencyMs(Math.round(avgLatency * 10.0) / 10.0)
                    .retryCount(retryCount)
                    .invalidTokensRemoved(invalidTokensRemoved)
                    .topFailureReasons(topFailures)
                    .build();
        } catch (Exception e) {
            log.error("Error executing getPushNotificationAnalytics: {}", e.getMessage(), e);
            return com.bloodbridge.dto.response.PushAnalyticsResponse.builder()
                    .emailsSent(0L)
                    .emailsFailed(0L)
                    .webSocketDelivered(0L)
                    .pushSent(0L)
                    .pushFailed(0L)
                    .pushSuccessPercentage(100.0)
                    .averagePushLatencyMs(0.0)
                    .retryCount(0L)
                    .invalidTokensRemoved(0L)
                    .topFailureReasons(Collections.emptyMap())
                    .build();
        }
    }
}
