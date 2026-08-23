package com.bloodbridge.service.impl;

import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.dto.response.AdminAnalyticsResponse;
import com.bloodbridge.dto.response.AdminDashboardResponse;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.AuditLogResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonationHistoryResponse;
import com.bloodbridge.dto.response.GlobalSearchResponse;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.dto.response.SystemHealthResponse;
import com.bloodbridge.dto.response.UserPageResponse;
import com.bloodbridge.dto.response.UserProfileResponse;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.AuditLog;
import com.bloodbridge.entity.BloodInventory;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.enums.MatchStatus;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.exception.HospitalNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.BloodRequestMapper;
import com.bloodbridge.mapper.HospitalMapper;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.AuditLogRepository;
import com.bloodbridge.repository.BloodInventoryRepository;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.MatchResultRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AdminService;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import com.bloodbridge.event.HospitalVerificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for Enterprise Admin Dashboard & System Operations Center workflows.
 * Integrates instant real-time STOMP WebSocket broadcasting and 100% dynamic database analytics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonationRepository donationRepository;
    private final MatchResultRepository matchResultRepository;
    private final BloodInventoryRepository bloodInventoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final com.bloodbridge.repository.NotificationRepository notificationRepository;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.bloodbridge.repository.MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private final HospitalMapper hospitalMapper;
    private final UserMapper userMapper;
    private final BloodRequestMapper bloodRequestMapper;
    private final com.bloodbridge.mapper.DonorProfileMapper donorProfileMapper;
    private final NotificationService notificationService;
    private final AuditLoggerService auditLoggerService;
    private final RealtimeService realtimeService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.bloodbridge.service.UserService userService;

    private static final long START_TIME = System.currentTimeMillis();

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AdminDashboardResponse> getDashboard() {
        log.info("Calculating Enterprise Admin Dashboard Executive KPIs strictly from database");
        long totalUsers = userRepository.count();
        long totalDonors = donorProfileRepository.count() > 0 ? donorProfileRepository.count() : userRepository.countByRole(com.bloodbridge.enums.Role.DONOR);
        long totalPatients = patientProfileRepository.count() > 0 ? patientProfileRepository.count() : userRepository.countByRole(com.bloodbridge.enums.Role.PATIENT);
        long totalHospitals = hospitalRepository.count() > 0 ? hospitalRepository.count() : userRepository.countByRole(com.bloodbridge.enums.Role.HOSPITAL);

        List<Hospital> hospitals = hospitalRepository.findAll();
        long verifiedHospitals = hospitals.stream().filter(h -> Boolean.TRUE.equals(h.getVerified())).count();
        long pendingApprovals = hospitals.stream().filter(h -> "PENDING".equalsIgnoreCase(h.getVerificationStatus())).count();

        List<BloodRequest> requests = bloodRequestRepository.findAll();
        long todaysRequests = requests.stream().filter(r -> r.getCreatedAt() != null && r.getCreatedAt().toLocalDate().equals(LocalDate.now())).count();
        long activeRequests = requests.stream().filter(r -> r.getStatus() == RequestStatus.CREATED || r.getStatus() == RequestStatus.MATCHING || r.getStatus() == RequestStatus.DONOR_ACCEPTED || r.getStatus() == RequestStatus.IN_PROGRESS).count();
        long completedRequests = requests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED || r.getStatus() == RequestStatus.FULFILLED).count();
        long emergencyRequests = requests.stream().filter(r -> r.getUrgencyLevel() != null && "CRITICAL".equalsIgnoreCase(r.getUrgencyLevel().name())).count();

        List<Donation> donations = donationRepository.findAll();
        long todaysDonations = donations.stream().filter(d -> d.getCreatedAt() != null && d.getCreatedAt().toLocalDate().equals(LocalDate.now())).count();
        long totalDonations = donations.size();
        long livesSaved = totalDonations * 3;

        // Dynamic Calculations
        long totalMatches = matchResultRepository.count();
        long acceptedMatches = matchResultRepository.countByStatus(MatchStatus.ACCEPTED);
        double matchingSuccessRatePct = totalMatches == 0 ? 0.0 : Math.round(((double) acceptedMatches / totalMatches) * 100.0 * 10.0) / 10.0;

        double averageResponseTimeHours = requests.stream()
                .filter(r -> (r.getStatus() == RequestStatus.COMPLETED || r.getStatus() == RequestStatus.FULFILLED) && r.getCreatedAt() != null && r.getUpdatedAt() != null)
                .mapToDouble(r -> java.time.Duration.between(r.getCreatedAt(), r.getUpdatedAt()).toMinutes() / 60.0)
                .average()
                .orElse(0.0);
        averageResponseTimeHours = Math.round(averageResponseTimeHours * 10.0) / 10.0;

        List<User> allUsersList = userRepository.findAll();
        long dailyRegistrations = allUsersList.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        LocalDateTime now = LocalDateTime.now();
        long thisWeekUsers = allUsersList.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(now.minusDays(7))).count();
        long priorWeekUsers = allUsersList.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(now.minusDays(14)) && u.getCreatedAt().isBefore(now.minusDays(7))).count();
        double weeklyGrowthPct = priorWeekUsers == 0 ? (thisWeekUsers > 0 ? 100.0 : 0.0) : Math.round(((double) (thisWeekUsers - priorWeekUsers) / priorWeekUsers) * 100.0 * 10.0) / 10.0;

        long thisMonthUsers = allUsersList.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(now.minusDays(30))).count();
        long priorMonthUsers = allUsersList.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(now.minusDays(60)) && u.getCreatedAt().isBefore(now.minusDays(30))).count();
        double monthlyGrowthPct = priorMonthUsers == 0 ? (thisMonthUsers > 0 ? 100.0 : 0.0) : Math.round(((double) (thisMonthUsers - priorMonthUsers) / priorMonthUsers) * 100.0 * 10.0) / 10.0;

        long emailsSentToday = notificationRepository.countByDeliveryChannelAndCreatedAtAfter(DeliveryChannel.EMAIL, LocalDate.now().atStartOfDay());
        long emailsFailed = notificationRepository.countByDeliveryChannelAndStatus(DeliveryChannel.EMAIL, com.bloodbridge.enums.NotificationStatus.FAILED);
        long totalEmergencyEmails = notificationRepository.countByDeliveryChannelAndNotificationType(DeliveryChannel.EMAIL, NotificationType.EMERGENCY_BLOOD_REQUEST);

        AdminDashboardResponse dashboard = AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalDonors(totalDonors)
                .totalPatients(totalPatients)
                .totalHospitals(totalHospitals)
                .verifiedHospitals(verifiedHospitals)
                .pendingHospitalApprovals(pendingApprovals)
                .todaysRequests(todaysRequests)
                .activeRequests(activeRequests)
                .completedRequests(completedRequests)
                .emergencyRequests(emergencyRequests)
                .todaysDonations(todaysDonations)
                .totalDonations(totalDonations)
                .livesSaved(livesSaved)
                .matchingSuccessRatePct(matchingSuccessRatePct)
                .averageResponseTimeHours(averageResponseTimeHours)
                .dailyRegistrations((int) dailyRegistrations)
                .weeklyGrowthPct(weeklyGrowthPct)
                .monthlyGrowthPct(monthlyGrowthPct)
                .emailsSentToday(emailsSentToday)
                .emailsFailed(emailsFailed)
                .totalEmergencyEmails(totalEmergencyEmails)
                .build();

        return ApiResponse.success("Admin executive dashboard summary retrieved successfully", dashboard);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AdminAnalyticsResponse> getAnalytics() {
        log.info("Generating Admin Platform Analytics and Chart Data strictly from database");
        List<BloodRequest> requests = bloodRequestRepository.findAll();
        List<Donation> donations = donationRepository.findAll();
        List<Hospital> hospitals = hospitalRepository.findAll();

        Map<String, Integer> demandMap = new LinkedHashMap<>();
        for (BloodRequest req : requests) {
            if (req.getBloodGroupNeeded() != null) {
                String bg = req.getBloodGroupNeeded().name();
                demandMap.put(bg, demandMap.getOrDefault(bg, 0) + req.getUnitsRequired());
            }
        }

        Map<String, Integer> donationTrends = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String key = ym.toString();
            long count = donations.stream()
                    .filter(d -> d.getCreatedAt() != null && YearMonth.from(d.getCreatedAt()).equals(ym))
                    .count();
            donationTrends.put(key, (int) count);
        }

        Map<String, Integer> hospitalPerformance = new LinkedHashMap<>();
        for (Hospital h : hospitals) {
            long completedDonationsForHospital = donations.stream()
                    .filter(d -> d.getHospital() != null && Objects.equals(d.getHospital().getId(), h.getId()) && d.getStatus() == DonationStatus.COMPLETED)
                    .count();
            hospitalPerformance.put(h.getHospitalName(), (int) completedDonationsForHospital);
        }

        Map<String, Double> responseTimes = new LinkedHashMap<>();
        for (UrgencyLevel urgency : UrgencyLevel.values()) {
            double avgHours = requests.stream()
                    .filter(r -> r.getUrgencyLevel() == urgency && r.getCreatedAt() != null && r.getUpdatedAt() != null)
                    .mapToDouble(r -> java.time.Duration.between(r.getCreatedAt(), r.getUpdatedAt()).toMinutes() / 60.0)
                    .average()
                    .orElse(0.0);
            responseTimes.put(urgency.name(), Math.round(avgHours * 10.0) / 10.0);
        }

        Map<String, Integer> consumptionMap = new LinkedHashMap<>();
        List<BloodInventory> inventories = bloodInventoryRepository.findAll();
        for (BloodInventory inv : inventories) {
            if (inv.getBloodGroup() != null) {
                String bg = inv.getBloodGroup().name();
                consumptionMap.put(bg, consumptionMap.getOrDefault(bg, 0) + inv.getAvailableUnits() + inv.getReservedUnits());
            }
        }

        AdminAnalyticsResponse analytics = AdminAnalyticsResponse.builder()
                .bloodGroupDemand(demandMap)
                .donationTrends(donationTrends)
                .hospitalPerformance(hospitalPerformance)
                .emergencyResponseTimes(responseTimes)
                .inventoryConsumption(consumptionMap)
                .build();

        return ApiResponse.success("Platform analytics retrieved successfully", analytics);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<HospitalResponse>> getPendingHospitals() {
        log.info("Fetching pending hospital registrations for review");
        List<Hospital> hospitals = hospitalRepository.findAll();
        List<HospitalResponse> pendingList = hospitals.stream()
                .filter(h -> "PENDING".equalsIgnoreCase(h.getVerificationStatus()) || !Boolean.TRUE.equals(h.getVerified()))
                .map(hospitalMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Pending hospital registrations retrieved successfully", pendingList);
    }

    @Override
    @Transactional
    public ApiResponse<HospitalResponse> verifyHospital(Long id, String status, String remarks, String adminEmail) {
        log.info("[STEP 1] Loading hospital with ID: {} for verification review by admin: {}", id, adminEmail);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found for ID: " + id));

        boolean approved = "APPROVED".equalsIgnoreCase(status) || "VERIFIED".equalsIgnoreCase(status);

        log.info("[STEP 2] Updating hospital status to: {} (approved={})", status, approved);
        if (approved) {
            hospital.markAsApproved(adminEmail, remarks);
        } else {
            hospital.markAsRejected(adminEmail, remarks);
        }

        if (hospital.getUser() != null || hospital.getEmail() != null) {
            String targetEmail = hospital.getUser() != null ? hospital.getUser().getEmail() : hospital.getEmail();
            log.info("[STEP 3] Updating associated user active status to: {} for email: {}", approved, targetEmail);
            userRepository.findByEmail(targetEmail).ifPresent(u -> {
                u.setActive(approved);
                userRepository.save(u);
                hospital.setUser(u);
            });
        }

        log.info("[STEP 4] Saving updated hospital entity to database");
        Hospital updatedHospital = hospitalRepository.save(hospital);

        log.info("[STEP 5] Mapping updated hospital entity to response DTO");
        HospitalResponse response = hospitalMapper.toResponse(updatedHospital);

        log.info("[STEP 6] Publishing HospitalVerificationEvent for post-commit side effects");
        eventPublisher.publishEvent(new HospitalVerificationEvent(this, updatedHospital, status, remarks, adminEmail, approved, response));

        log.info("[STEP 7] Transaction setup complete for hospital ID: {}. Returning success response.", updatedHospital.getId());
        return ApiResponse.success("Hospital verification status updated successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserPageResponse> getAllUsers(int page, int size, String query) {
        log.info("Fetching all users with page: {}, size: {}, query: {}", page, size, query);
        Page<User> userPage;
        if (query != null && !query.trim().isEmpty()) {
            String searchTerm = query.trim();
            userPage = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, PageRequest.of(page, size));
        } else {
            userPage = userRepository.findAll(PageRequest.of(page, size));
        }

        List<UserProfileResponse> profiles = (userPage != null && userPage.getContent() != null)
                ? userPage.getContent().stream()
                        .map(userMapper::toResponse)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        UserPageResponse pageResponse = UserPageResponse.builder()
                .content(profiles)
                .pageNumber(userPage != null ? userPage.getNumber() : page)
                .pageSize(userPage != null ? userPage.getSize() : size)
                .totalElements(userPage != null ? userPage.getTotalElements() : 0L)
                .totalPages(userPage != null ? userPage.getTotalPages() : 0)
                .last(userPage == null || userPage.isLast())
                .build();

        return ApiResponse.success("Users list retrieved successfully", pageResponse);
    }

    @Override
    @Transactional
    public ApiResponse<UserProfileResponse> updateUserStatus(Long userId, String status, String adminEmail) {
        log.info("Admin {} updating status for user ID: {} to status: {}", adminEmail, userId, status);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for ID: " + userId));

        boolean active = "ACTIVE".equalsIgnoreCase(status);
        user.setActive(active);
        User updatedUser = userRepository.save(user);

        auditLoggerService.logEvent("USER_STATUS_UPDATED", adminEmail, "User ID " + userId + " set to " + status);
        log.info("Successfully updated status for user ID: {} to {}", userId, status);

        UserProfileResponse response = userMapper.toResponse(updatedUser);

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.USER_UPDATED,
                    "USER",
                    updatedUser.getId(),
                    "User Status Updated",
                    String.format("User %s status updated to %s", updatedUser.getEmail(), status),
                    response
            );
            realtimeService.publishAdminUsersUpdate(event);
            realtimeService.publishAdminDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to publish user update WebSocket event: {}", e.getMessage());
        }

        return ApiResponse.success("User account status updated successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<BloodRequestResponse>> getAllBloodRequests() {
        log.info("Admin fetching all blood requests");
        List<BloodRequest> requests = bloodRequestRepository.findAll();
        List<BloodRequestResponse> response = requests.stream()
                .map(bloodRequestMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("All blood requests retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<BloodRequestResponse> forceCloseBloodRequest(Long requestId, String adminEmail) {
        log.info("Admin {} force closing blood request ID: {}", adminEmail, requestId);
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + requestId));

        request.setStatus(RequestStatus.CANCELLED);
        BloodRequest updatedRequest = bloodRequestRepository.save(request);

        auditLoggerService.logEvent("BLOOD_REQUEST_FORCE_CLOSED", adminEmail, "Force closed request ID: " + requestId);
        log.info("Successfully force closed blood request ID: {}", requestId);

        BloodRequestResponse response = bloodRequestMapper.toResponse(updatedRequest);

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.BLOOD_REQUEST_CANCELLED,
                    "BLOOD_REQUEST",
                    updatedRequest.getId(),
                    "Blood Request Cancelled",
                    "Blood request #" + requestId + " was force closed by admin",
                    response
            );
            realtimeService.publishAdminDashboardUpdate(event);
            if (updatedRequest.getHospital() != null) {
                realtimeService.publishHospitalUpdate(updatedRequest.getHospital().getId(), event);
            }
        } catch (Exception e) {
            log.error("Failed to publish force close WebSocket event: {}", e.getMessage());
        }

        return ApiResponse.success("Blood request force closed successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<DonationHistoryResponse>> getAllDonations() {
        log.info("Admin fetching all donation records");
        List<Donation> donations = donationRepository.findAll();
        List<DonationHistoryResponse> history = new ArrayList<>();

        for (Donation donation : donations) {
            history.add(DonationHistoryResponse.builder()
                    .id(donation.getId())
                    .donationDate(donation.getDonationDate())
                    .hospitalName(donation.getHospital() != null ? donation.getHospital().getHospitalName() : "Community Blood Bank")
                    .unitsDonated(donation.getUnitsDonated())
                    .bloodGroup(donation.getDonor() != null ? donation.getDonor().getBloodGroup() : null)
                    .status(donation.getStatus())
                    .certificateUrl("https://certificates.bloodbridge.com/cert_" + donation.getId() + ".pdf")
                    .doctorNotes(donation.getRemarks() != null ? donation.getRemarks() : "Verified donation")
                    .donationType("WHOLE_BLOOD")
                    .createdAt(donation.getCreatedAt())
                    .build());
        }

        return ApiResponse.success("All donation records retrieved successfully", history);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AuditLogResponse>> getAuditLogs(int page, int size) {
        log.info("Fetching audit logs for page: {}, size: {}", page, size);
        Page<AuditLog> auditPage = auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));

        List<AuditLogResponse> logs = auditPage.getContent().stream()
                .map(log -> AuditLogResponse.builder()
                        .id(log.getId())
                        .userEmail(log.getUserEmail())
                        .action(log.getAction())
                        .module(log.getModule())
                        .description(log.getDescription())
                        .ipAddress(log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1")
                        .browser(log.getBrowser() != null ? log.getBrowser() : "System Agent")
                        .status(log.getStatus())
                        .severity(log.getSeverity())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success("Audit logs retrieved successfully", logs);
    }

    @Override
    public ApiResponse<SystemHealthResponse> getSystemHealth() {
        log.info("Computing System Health & Infrastructure Operations Metrics");
        Runtime runtime = Runtime.getRuntime();
        long totalMemMb = runtime.totalMemory() / (1024 * 1024);
        long freeMemMb = runtime.freeMemory() / (1024 * 1024);
        long maxMemMb = runtime.maxMemory() / (1024 * 1024);
        long uptimeSec = (System.currentTimeMillis() - START_TIME) / 1000;

        String dbConn = "UP";
        String apiStatus = "UP";
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

            long pendingNotifications = notificationRepository.countByStatus(com.bloodbridge.enums.NotificationStatus.PENDING);
            if (pendingNotifications > 0) {
                queueStatus = "PENDING_ALERTS";
            }
        } catch (Exception e) {
            log.error("Database health diagnostics check failed: {}", e.getMessage(), e);
            dbConn = "DOWN";
            apiStatus = "DEGRADED";
            queueStatus = "INACTIVE";
        }

        SystemHealthResponse health = SystemHealthResponse.builder()
                .status(apiStatus)
                .dbStatus(dbConn)
                .databaseConnectivity(dbConn)
                .apiHealth(apiStatus)
                .notificationQueueStatus(queueStatus)
                .totalRecords(totalRecords)
                .uptimeSeconds(uptimeSec)
                .totalMemoryMb(totalMemMb)
                .freeMemoryMb(freeMemMb)
                .maxMemoryMb(maxMemMb)
                .activeUsers(activeUsers)
                .serverTime(LocalDateTime.now())
                .version("0.0.1-SNAPSHOT")
                .environment("PRODUCTION")
                .build();

        return ApiResponse.success("System health metrics retrieved successfully", health);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<GlobalSearchResponse> globalSearch(String query) {
        log.info("Performing global admin search for query keyword: {}", query);
        String q = query != null ? query.toLowerCase() : "";

        List<GlobalSearchResponse.SearchResultItem> items = new ArrayList<>();

        List<User> users = userRepository.findAll();
        long matchUsers = 0;
        for (User u : users) {
            if (u.getFullName().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q)) {
                matchUsers++;
                items.add(GlobalSearchResponse.SearchResultItem.builder()
                        .entityType("USER")
                        .entityId(u.getId())
                        .title(u.getFullName())
                        .subtitle("Role: " + u.getRole() + " - " + u.getEmail())
                        .status(Boolean.TRUE.equals(u.getActive()) ? "ACTIVE" : "INACTIVE")
                        .build());
            }
        }

        List<Hospital> hospitals = hospitalRepository.findAll();
        long matchHospitals = 0;
        for (Hospital h : hospitals) {
            if ((h.getHospitalName() != null && h.getHospitalName().toLowerCase().contains(q)) ||
                (h.getCity() != null && h.getCity().toLowerCase().contains(q))) {
                matchHospitals++;
                items.add(GlobalSearchResponse.SearchResultItem.builder()
                        .entityType("HOSPITAL")
                        .entityId(h.getId())
                        .title(h.getHospitalName())
                        .subtitle(h.getCity() + ", " + h.getState() + " - Reg: " + h.getRegistrationNumber())
                        .status(h.getVerificationStatus())
                        .build());
            }
        }

        List<BloodRequest> reqs = bloodRequestRepository.findAll();
        long matchRequests = 0;
        for (BloodRequest req : reqs) {
            String reason = req.getReason() != null ? req.getReason().toLowerCase() : "";
            String bg = req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name().toLowerCase() : "";
            if (reason.contains(q) || bg.contains(q) || String.valueOf(req.getId()).equals(q)) {
                matchRequests++;
                items.add(GlobalSearchResponse.SearchResultItem.builder()
                        .entityType("BLOOD_REQUEST")
                        .entityId(req.getId())
                        .title("Request #" + req.getId() + " (" + req.getBloodGroupNeeded() + ")")
                        .subtitle("Units: " + req.getUnitsRequired() + " - Hospital: " + (req.getHospital() != null ? req.getHospital().getHospitalName() : "N/A"))
                        .status(req.getStatus() != null ? req.getStatus().name() : "PENDING")
                        .build());
            }
        }

        List<Donation> donations = donationRepository.findAll();
        long matchDonations = 0;
        for (Donation don : donations) {
            String remarks = don.getRemarks() != null ? don.getRemarks().toLowerCase() : "";
            String hosp = don.getHospital() != null && don.getHospital().getHospitalName() != null ? don.getHospital().getHospitalName().toLowerCase() : "";
            if (remarks.contains(q) || hosp.contains(q) || String.valueOf(don.getId()).equals(q)) {
                matchDonations++;
                items.add(GlobalSearchResponse.SearchResultItem.builder()
                        .entityType("DONATION")
                        .entityId(don.getId())
                        .title("Donation #" + don.getId())
                        .subtitle("Hospital: " + (don.getHospital() != null ? don.getHospital().getHospitalName() : "N/A"))
                        .status(don.getStatus() != null ? don.getStatus().name() : "COMPLETED")
                        .build());
            }
        }

        GlobalSearchResponse searchResponse = GlobalSearchResponse.builder()
                .usersCount(matchUsers)
                .hospitalsCount(matchHospitals)
                .requestsCount(matchRequests)
                .donationsCount(matchDonations)
                .results(items)
                .build();

        return ApiResponse.success("Global search executed successfully", searchResponse);
    }

    @Override
    @Transactional
    public ApiResponse<String> broadcastTargetNotification(String adminEmail, String title, String message, String role, String targetCity, String priority) {
        log.info("Admin {} broadcasting target notification to Role: {} | City: {}", adminEmail, role, targetCity);
        List<User> users = userRepository.findAll();

        int sentCount = 0;
        for (User user : users) {
            if (role != null && !role.isBlank() && !user.getRole().name().equalsIgnoreCase(role)) {
                continue;
            }
            notificationService.triggerNotificationEvent(user, title, message, NotificationType.SYSTEM_ANNOUNCEMENT, DeliveryChannel.IN_APP, priority);
            sentCount++;
        }

        auditLoggerService.logEvent("BROADCAST_SENT", adminEmail, "Target broadcast sent to " + sentCount + " users.");
        return ApiResponse.success("Target broadcast notification sent to " + sentCount + " users successfully");
    }

    @Override
    public ApiResponse<String> deleteDonor(Long donorId, String adminEmail) {
        log.info("Admin {} permanently deleting donor ID: {}", adminEmail, donorId);
        return userService.deleteDonor(donorId);
    }

    @Override
    @Transactional
    public ApiResponse<List<DonorProfileResponse>> getAllDonors(String search, String bloodGroup, String city) {
        log.info("Admin fetching all registered donors (search: {}, bloodGroup: {}, city: {})", search, bloodGroup, city);

        List<DonorProfile> existingDonors = donorProfileRepository.findAll();
        Set<Long> registeredUserIds = existingDonors.stream()
                .filter(d -> d.getUser() != null)
                .map(d -> d.getUser().getId())
                .collect(Collectors.toSet());

        List<User> donorUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.bloodbridge.enums.Role.DONOR || (u.getRoles() != null && u.getRoles().contains(com.bloodbridge.enums.Role.DONOR)))
                .collect(Collectors.toList());

        for (User u : donorUsers) {
            if (!registeredUserIds.contains(u.getId())) {
                log.info("Auto-syncing missing DonorProfile for user ID: {} ({})", u.getId(), u.getEmail());
                DonorProfile newDp = DonorProfile.builder()
                        .user(u)
                        .email(u.getEmail())
                        .bloodGroup(com.bloodbridge.enums.BloodGroup.O_POSITIVE)
                        .rhFactor("POSITIVE")
                        .age(u.getDateOfBirth() != null ? java.time.Period.between(u.getDateOfBirth(), java.time.LocalDate.now()).getYears() : 25)
                        .gender(u.getGender() != null && u.getGender().equalsIgnoreCase("FEMALE") ? com.bloodbridge.enums.Gender.FEMALE : com.bloodbridge.enums.Gender.MALE)
                        .dateOfBirth(u.getDateOfBirth())
                        .city(u.getCity() != null ? u.getCity() : "City")
                        .state(u.getState() != null ? u.getState() : "State")
                        .country(u.getCountry() != null ? u.getCountry() : "India")
                        .postalCode(u.getPostalCode())
                        .address(u.getAddress())
                        .latitude(u.getLatitude())
                        .longitude(u.getLongitude())
                        .availableForDonation(true)
                        .emergencyAvailable(true)
                        .preferredDonationRadius(25.0)
                        .status("ACTIVE")
                        .verificationStatus("VERIFIED")
                        .totalDonations(0)
                        .livesSaved(0)
                        .donorScore(100)
                        .build();
                donorProfileRepository.save(newDp);
                existingDonors.add(newDp);
                registeredUserIds.add(u.getId());
            }
        }

        List<DonorProfileResponse> list = existingDonors.stream()
                .filter(d -> {
                    if (search != null && !search.isBlank()) {
                        String q = search.trim().toLowerCase();
                        String name = (d.getUser() != null && d.getUser().getFullName() != null) ? d.getUser().getFullName().toLowerCase() : "";
                        String email = (d.getEmail() != null) ? d.getEmail().toLowerCase() : ((d.getUser() != null && d.getUser().getEmail() != null) ? d.getUser().getEmail().toLowerCase() : "");
                        String phone = (d.getUser() != null && d.getUser().getPhoneNumber() != null) ? d.getUser().getPhoneNumber() : "";
                        String c = (d.getCity() != null) ? d.getCity().toLowerCase() : "";
                        if (!name.contains(q) && !email.contains(q) && !phone.contains(q) && !c.contains(q)) return false;
                    }
                    if (bloodGroup != null && !bloodGroup.isBlank() && !"ALL".equalsIgnoreCase(bloodGroup)) {
                        String bg = d.getBloodGroup() != null ? d.getBloodGroup().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";
                        if (!bg.equalsIgnoreCase(bloodGroup) && (d.getBloodGroup() == null || !d.getBloodGroup().name().equalsIgnoreCase(bloodGroup))) return false;
                    }
                    if (city != null && !city.isBlank() && !"ALL".equalsIgnoreCase(city)) {
                        if (d.getCity() == null || !d.getCity().equalsIgnoreCase(city.trim())) return false;
                    }
                    return true;
                })
                .map(donorProfileMapper::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ApiResponse.success("All registered donors retrieved successfully", list);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<DonorProfileResponse> getDonorById(Long id) {
        log.info("Admin fetching donor profile details for ID: {}", id);
        DonorProfile donor = donorProfileRepository.findById(id)
                .or(() -> donorProfileRepository.findByUserId(id))
                .orElseThrow(() -> new UserNotFoundException("Donor profile not found for ID: " + id));
        return ApiResponse.success("Donor profile details retrieved successfully", donorProfileMapper.toResponse(donor));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<HospitalResponse>> getAllHospitals(String search, String city, String status) {
        log.info("Admin fetching all registered hospitals (search: {}, city: {}, status: {})", search, city, status);
        List<Hospital> hospitals = hospitalRepository.findAll();
        List<HospitalResponse> list = hospitals.stream()
                .filter(h -> {
                    if (search != null && !search.isBlank()) {
                        String q = search.trim().toLowerCase();
                        String name = h.getHospitalName() != null ? h.getHospitalName().toLowerCase() : "";
                        String email = h.getEmail() != null ? h.getEmail().toLowerCase() : "";
                        String phone = h.getPhoneNumber() != null ? h.getPhoneNumber() : "";
                        String c = h.getCity() != null ? h.getCity().toLowerCase() : "";
                        if (!name.contains(q) && !email.contains(q) && !phone.contains(q) && !c.contains(q)) return false;
                    }
                    if (city != null && !city.isBlank() && !"ALL".equalsIgnoreCase(city)) {
                        if (h.getCity() == null || !h.getCity().equalsIgnoreCase(city.trim())) return false;
                    }
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        if (h.getVerificationStatus() == null || !h.getVerificationStatus().equalsIgnoreCase(status.trim())) return false;
                    }
                    return true;
                })
                .map(hospitalMapper::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ApiResponse.success("All registered hospitals retrieved successfully", list);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HospitalResponse> getHospitalById(Long id) {
        log.info("Admin fetching hospital details for ID: {}", id);
        Hospital hospital = hospitalRepository.findById(id)
                .or(() -> hospitalRepository.findByUserId(id))
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found for ID: " + id));
        return ApiResponse.success("Hospital profile details retrieved successfully", hospitalMapper.toResponse(hospital));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteHospital(Long hospitalId, String adminEmail) {
        log.info("Admin {} permanently deleting hospital ID: {}", adminEmail, hospitalId);
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .or(() -> hospitalRepository.findByUserId(hospitalId))
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found for ID: " + hospitalId));

        Long hid = hospital.getId();
        User user = hospital.getUser();

        // 1. Unlink notifications & donations associated with this hospital
        try {
            notificationRepository.unlinkHospitalProfile(hid);
            donationRepository.unlinkHospitalProfile(hid);
            if (matchedEmergencyDonorRepository != null) {
                matchedEmergencyDonorRepository.deleteAllByHospitalId(hid);
            }
        } catch (Exception ex) {
            log.warn("Unlinking hospital records encountered warning: {}", ex.getMessage());
        }

        // 2. Delete inventory records for this hospital
        try {
            List<BloodInventory> inventories = bloodInventoryRepository.findByHospitalId(hid);
            if (inventories != null && !inventories.isEmpty()) {
                bloodInventoryRepository.deleteAll(inventories);
            }
        } catch (Exception ex) {
            log.warn("Deleting hospital inventory encountered warning: {}", ex.getMessage());
        }

        // 3. Delete hospital entity
        hospitalRepository.delete(hospital);

        // 4. Delete user if exists
        if (user != null) {
            userService.deleteUser(user.getId());
        }

        auditLoggerService.logEvent("HOSPITAL_PERMANENTLY_DELETED", adminEmail, "Hospital ID " + hid + " deleted by admin " + adminEmail);
        return ApiResponse.success("Hospital permanently deleted successfully");
    }
}
