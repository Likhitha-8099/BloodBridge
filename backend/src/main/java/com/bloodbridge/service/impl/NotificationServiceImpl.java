package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.BroadcastAnnouncementRequest;
import com.bloodbridge.dto.request.SendNotificationRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationCountResponse;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.dto.DonorMatchingResult;
import com.bloodbridge.dto.EmergencyMailDto;
import com.bloodbridge.dto.NotificationDTO;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.EmailNotification;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.NotificationPreference;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.EmailDeliveryStatus;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.NotificationPriority;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationTarget;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.NotificationNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.NotificationMapper;
import com.bloodbridge.provider.NotificationProvider;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.DonorMatchingService;
import com.bloodbridge.service.EmailService;
import com.bloodbridge.service.NotificationPreferenceService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.QuietHoursService;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Enterprise service implementation for Automated Notification, Alert & Communication System (Phase 3C).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final EmailNotificationRepository emailNotificationRepository;
    private final DonorMatchingService donorMatchingService;
    private final NotificationMapper notificationMapper;
    private final List<NotificationProvider> notificationProviders;
    private final AuditLoggerService auditLoggerService;
    private final RealtimeService realtimeService;
    private final EmailService emailService;
    private final NotificationPreferenceService preferenceService;
    private final QuietHoursService quietHoursService;
    private final com.bloodbridge.notification.NotificationOrchestrator notificationOrchestrator;

    @Qualifier("emergencyEmailExecutor")
    private final Executor emergencyEmailExecutor;

    private void publishRealtimeNotification(Notification saved) {
        if (saved == null || saved.getRecipientUser() == null) return;
        try {
            NotificationDTO dto = notificationMapper.toDto(saved);
            realtimeService.publishUserNotification(saved.getRecipientUser().getId(), dto);

            long unreadCount = notificationRepository.countUnreadByRecipientUserId(saved.getRecipientUser().getId());
            realtimeService.publishUnreadCount(saved.getRecipientUser().getId(), unreadCount);

            if ("ADMIN".equalsIgnoreCase(saved.getRecipientRole()) || 
                (saved.getRecipientUser().getRole() != null && saved.getRecipientUser().getRole() == Role.ADMIN)) {
                realtimeService.publishAdminNotification(dto);
            }

            RealtimeEventDTO notificationCreatedEvent = RealtimeEventDTO.builder()
                    .eventType(RealtimeEventType.NOTIFICATION_CREATED)
                    .requestId(saved.getBloodRequest() != null ? saved.getBloodRequest().getId() : null)
                    .donorId(saved.getDonor() != null ? saved.getDonor().getId() : null)
                    .hospitalId(saved.getHospital() != null ? saved.getHospital().getId() : null)
                    .title(saved.getTitle())
                    .message(saved.getMessage())
                    .payload(dto)
                    .timestamp(LocalDateTime.now())
                    .build();

            realtimeService.publishEmergencyEvent(notificationCreatedEvent);
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to publish real-time notification WebSocket event: {}", e.getMessage());
        }
    }

    private boolean checkSuppressionAndPreferences(User recipient, NotificationCategory category, NotificationPriority priority, DeliveryChannel channel) {
        if (recipient == null || preferenceService == null || quietHoursService == null) return false;
        try {
            NotificationPreference pref = preferenceService.getOrCreateDefaultPreference(recipient);
            
            // 1. Channel check
            if (!preferenceService.isChannelEnabled(pref, channel)) {
                log.info("[PREFERENCE-SUPPRESS] Channel {} disabled for user ID: {}", channel, recipient.getId());
                return true;
            }

            // 2. Category check
            if (!preferenceService.isCategoryEnabled(pref, category)) {
                log.info("[PREFERENCE-SUPPRESS] Category {} disabled for user ID: {}", category, recipient.getId());
                return true;
            }

            // 3. Quiet Hours check
            if (quietHoursService.isQuietHoursSuppressed(pref, category, priority)) {
                log.info("[QUIET-HOURS-SUPPRESS] Notification suppressed for user ID: {} during quiet hours", recipient.getId());
                return true;
            }
        } catch (Exception e) {
            log.warn("Error checking notification preferences: {}", e.getMessage());
        }

        return false;
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> sendNotification(SendNotificationRequest request) {
        log.info("Sending notification to user ID: {} | Channel: {} | Title: {}", request.getRecipientUserId(), request.getChannel(), request.getTitle());
        User recipient = userRepository.findById(request.getRecipientUserId())
                .orElseThrow(() -> new UserNotFoundException("Recipient user not found for ID: " + request.getRecipientUserId()));

        NotificationCategory cat = request.getType() != null ? inferCategoryFromType(request.getType()) : NotificationCategory.SYSTEM;
        NotificationPriority prioEnum = parsePriorityEnum(request.getPriority());

        boolean suppressed = checkSuppressionAndPreferences(recipient, cat, prioEnum, request.getChannel());

        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .recipientRole(recipient.getRole() != null ? recipient.getRole().name() : "USER")
                .title(request.getTitle())
                .message(request.getMessage())
                .notificationType(request.getType() != null ? request.getType() : NotificationType.SYSTEM_NOTIFICATION)
                .category(cat)
                .deliveryChannel(request.getChannel() != null ? request.getChannel() : DeliveryChannel.IN_APP)
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .priorityEnum(prioEnum)
                .actionUrl(request.getActionUrl())
                .status(suppressed ? NotificationStatus.FAILED : NotificationStatus.PENDING)
                .readStatus(false)
                .deleted(false)
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .build();

        if (suppressed) {
            notification.setLastFailureReason("Suppressed by user preferences or quiet hours");
        } else {
            dispatchToProvider(notification);
        }

        notification.setSentAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        auditLoggerService.logEvent("SEND_NOTIFICATION", recipient.getEmail(),
                "Notification sent to user: " + recipient.getEmail() + " (ID: " + saved.getId() + ")");

        publishRealtimeNotification(saved);

        return ApiResponse.success("Notification sent successfully", notificationMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> markAsRead(String email, Long notificationId) {
        User user = findUserByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for ID: " + notificationId));

        if (!notification.getRecipientUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User not authorized to update this notification");
        }

        notification.setReadStatus(true);
        notification.setReadAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        long unread = notificationRepository.countUnreadByRecipientUserId(user.getId());
        realtimeService.publishUnreadCount(user.getId(), unread);

        return ApiResponse.success("Notification marked as read", notificationMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ApiResponse<String> markAllAsRead(String email) {
        User user = findUserByEmail(email);
        notificationRepository.markAllAsReadForUser(user.getId(), LocalDateTime.now());
        realtimeService.publishUnreadCount(user.getId(), 0);
        return ApiResponse.success("All notifications marked as read");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationResponse>> getMyNotifications(String email) {
        User user = findUserByEmail(email);
        List<Notification> notifications = notificationRepository.findUserNotifications(user.getId());

        List<NotificationResponse> responses = notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("User notifications retrieved successfully", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getNotificationsPaginated(String email, Integer page, Integer size, String category, String priority, Boolean read, Long cursor) {
        User user = findUserByEmail(email);
        int pageNo = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 20;

        NotificationCategory catEnum = null;
        if (category != null && !category.trim().isEmpty()) {
            try {
                catEnum = NotificationCategory.valueOf(category.toUpperCase());
            } catch (Exception ignored) {}
        }

        NotificationPriority prioEnum = null;
        if (priority != null && !priority.trim().isEmpty()) {
            try {
                prioEnum = NotificationPriority.valueOf(priority.toUpperCase());
            } catch (Exception ignored) {}
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifPage = notificationRepository.findUserNotificationsFiltered(user.getId(), catEnum, prioEnum, priority, read, pageable);

        List<NotificationResponse> items = notifPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("notifications", items);
        result.put("items", items);
        result.put("page", notifPage.getNumber());
        result.put("size", notifPage.getSize());
        result.put("totalElements", notifPage.getTotalElements());
        result.put("totalPages", notifPage.getTotalPages());
        result.put("last", notifPage.isLast());
        result.put("unreadCount", notificationRepository.countUnreadByRecipientUserId(user.getId()));

        if (!items.isEmpty()) {
            result.put("nextCursor", items.get(items.size() - 1).getId());
        }

        return ApiResponse.success("Notifications page retrieved successfully", result);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications(String email) {
        User user = findUserByEmail(email);
        List<Notification> unread = notificationRepository.findUnreadNotifications(user.getId());

        List<NotificationResponse> responses = unread.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Unread notifications retrieved successfully", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<NotificationCountResponse> getNotificationCount(String email) {
        User user = findUserByEmail(email);
        long unread = notificationRepository.countUnreadByRecipientUserId(user.getId());
        long total = notificationRepository.findUserNotifications(user.getId()).size();

        NotificationCountResponse countResponse = NotificationCountResponse.builder()
                .unreadCount(unread)
                .totalCount(total)
                .build();

        return ApiResponse.success("Notification count metrics retrieved successfully", countResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getUnreadBadgeCount(String email) {
        User user = findUserByEmail(email);
        long unread = notificationRepository.countUnreadByRecipientUserId(user.getId());
        Map<String, Object> res = new HashMap<>();
        res.put("unreadCount", unread);
        res.put("count", unread);
        return ApiResponse.success("Unread count retrieved successfully", res);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteNotification(String email, Long notificationId) {
        User user = findUserByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for ID: " + notificationId));

        if (!notification.getRecipientUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User not authorized to delete this notification");
        }

        notification.setDeleted(true);
        notificationRepository.save(notification);

        long unread = notificationRepository.countUnreadByRecipientUserId(user.getId());
        realtimeService.publishUnreadCount(user.getId(), unread);

        return ApiResponse.success("Notification deleted successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> broadcastAnnouncement(String adminEmail, BroadcastAnnouncementRequest request) {
        log.info("Admin {} broadcasting announcement to target: {} | Title: {}", adminEmail, request.getTarget(), request.getTitle());
        List<User> targetUsers;

        if (request.getTarget() == NotificationTarget.ALL) {
            targetUsers = userRepository.findAll();
        } else {
            Role role = Role.valueOf(request.getTarget().name());
            targetUsers = userRepository.findByRole(role);
        }

        int sentCount = 0;
        NotificationPriority prio = parsePriorityEnum(request.getPriority());

        for (User user : targetUsers) {
            Notification notification = Notification.builder()
                    .recipientUser(user)
                    .recipientRole(user.getRole() != null ? user.getRole().name() : "USER")
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .notificationType(NotificationType.SYSTEM_ANNOUNCEMENT)
                    .category(NotificationCategory.ADMIN)
                    .deliveryChannel(DeliveryChannel.IN_APP)
                    .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                    .priorityEnum(prio)
                    .status(NotificationStatus.SENT)
                    .readStatus(false)
                    .deleted(false)
                    .sentAt(LocalDateTime.now())
                    .build();

            Notification saved = notificationRepository.save(notification);
            publishRealtimeNotification(saved);
            sentCount++;
        }

        auditLoggerService.logEvent("BROADCAST_ANNOUNCEMENT", adminEmail,
                "Broadcast sent by " + adminEmail + " to " + sentCount + " users under target: " + request.getTarget());

        return ApiResponse.success("Announcement broadcasted successfully to " + sentCount + " users");
    }

    @Override
    @Transactional
    public void retryFailedNotifications() {
        log.info("Retrying failed notifications");
        List<Notification> failed = notificationRepository.findByStatusAndNextRetryTimeBefore(NotificationStatus.FAILED, LocalDateTime.now());
        for (Notification n : failed) {
            n.setStatus(NotificationStatus.RETRYING);
            n.setRetryCount((n.getRetryCount() != null ? n.getRetryCount() : 0) + 1);
            Long reqId = n.getBloodRequest() != null ? n.getBloodRequest().getId() : null;
            log.info("[EMAIL-RETRY] requestId={} attempt={}", reqId, n.getRetryCount());
            dispatchToProvider(n);
            notificationRepository.save(n);
        }
    }

    @Override
    public void triggerNotificationEvent(User recipient, String title, String message, NotificationType type, DeliveryChannel channel, String priority) {
        if (recipient == null) return;
        NotificationCategory cat = inferCategoryFromType(type);
        NotificationPriority prioEnum = parsePriorityEnum(priority);

        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .recipientRole(recipient.getRole() != null ? recipient.getRole().name() : "USER")
                .title(title)
                .message(message)
                .notificationType(type)
                .category(cat)
                .deliveryChannel(channel != null ? channel : DeliveryChannel.IN_APP)
                .priority(priority != null ? priority : "NORMAL")
                .priorityEnum(prioEnum)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .deleted(false)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            Notification saved = notificationRepository.saveAndFlush(notification);
            publishRealtimeNotification(saved);
        } catch (Exception e) {
            log.error("Failed to persist notification event: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void triggerNotificationEvent(User recipient, String title, String message, NotificationType type) {
        triggerNotificationEvent(recipient, title, message, type, DeliveryChannel.IN_APP, "NORMAL");
    }

    @Override
    @Transactional
    public void createDonorAcceptedNotification(DonorProfile donor, Hospital hospital, BloodRequest request) {
        if (hospital == null || donor == null) return;
        User hospitalUser = hospital.getUser();
        if (hospitalUser == null && hospital.getEmail() != null) {
            hospitalUser = userRepository.findByEmail(hospital.getEmail()).orElse(null);
        }
        if (hospitalUser == null) return;

        String title = "Donor Accepted Blood Request!";
        String message = String.format("Donor %s has accepted the blood request #%d for %s blood.",
                donor.getUser() != null ? donor.getUser().getFullName() : "Donor",
                request != null ? request.getId() : 0,
                request != null ? request.getBloodGroupNeeded().name() : "needed");

        Notification notification = Notification.builder()
                .recipientUser(hospitalUser)
                .recipientRole(Role.HOSPITAL.name())
                .donor(donor)
                .hospital(hospital)
                .bloodRequest(request)
                .title(title)
                .message(message)
                .notificationType(NotificationType.DONOR_ACCEPTED)
                .category(NotificationCategory.DONATION_APPROVED)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .priority("HIGH")
                .priorityEnum(NotificationPriority.HIGH)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .deleted(false)
                .actionUrl("/hospital/requests")
                .relatedEntityType("BLOOD_REQUEST")
                .relatedEntityId(request != null ? request.getId() : null)
                .sentAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        publishRealtimeNotification(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationResponse>> getHospitalNotifications(String email) {
        User user = findUserByEmail(email);
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Hospital profile not found for user"));

        List<Notification> notifications = notificationRepository.findByHospitalIdOrderByCreatedAtDesc(hospital.getId());

        List<NotificationResponse> responses = notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Hospital notifications retrieved successfully", responses);
    }

    @Override
    @Transactional
    public void notifyHospital(Hospital hospital, String title, String message, NotificationType type, String actionUrl, BloodRequest bloodRequest, DonorProfile donor) {
        if (hospital == null) return;
        User recipientUser = hospital.getUser();
        if (recipientUser == null && hospital.getEmail() != null) {
            recipientUser = userRepository.findByEmail(hospital.getEmail()).orElse(null);
        }
        if (recipientUser == null) return;

        Notification notification = Notification.builder()
                .recipientUser(recipientUser)
                .recipientRole(Role.HOSPITAL.name())
                .hospital(hospital)
                .donor(donor)
                .bloodRequest(bloodRequest)
                .title(title)
                .message(message)
                .notificationType(type)
                .category(inferCategoryFromType(type))
                .deliveryChannel(DeliveryChannel.IN_APP)
                .priority("HIGH")
                .priorityEnum(NotificationPriority.HIGH)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .deleted(false)
                .actionUrl(actionUrl != null ? actionUrl : "/hospital/requests")
                .relatedEntityType(bloodRequest != null ? "BLOOD_REQUEST" : "HOSPITAL")
                .relatedEntityId(bloodRequest != null ? bloodRequest.getId() : hospital.getId())
                .sentAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        publishRealtimeNotification(saved);
    }

    @Override
    @Transactional
    public void notifyDonor(DonorProfile donor, String title, String message, NotificationType type, String actionUrl, BloodRequest bloodRequest, Hospital hospital) {
        if (donor == null || donor.getUser() == null) return;

        Notification notification = Notification.builder()
                .recipientUser(donor.getUser())
                .recipientRole(Role.DONOR.name())
                .donor(donor)
                .hospital(hospital)
                .bloodRequest(bloodRequest)
                .title(title)
                .message(message)
                .notificationType(type)
                .category(inferCategoryFromType(type))
                .deliveryChannel(DeliveryChannel.IN_APP)
                .priority("HIGH")
                .priorityEnum(NotificationPriority.HIGH)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .deleted(false)
                .actionUrl(actionUrl != null ? actionUrl : "/donor/requests")
                .relatedEntityType(bloodRequest != null ? "BLOOD_REQUEST" : "DONOR")
                .relatedEntityId(bloodRequest != null ? bloodRequest.getId() : donor.getId())
                .sentAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        publishRealtimeNotification(saved);
    }

    @Override
    @Transactional
    public void notifyPatient(User patientUser, String title, String message, NotificationType type, String actionUrl, BloodRequest bloodRequest) {
        if (patientUser == null) return;

        Notification notification = Notification.builder()
                .recipientUser(patientUser)
                .recipientRole(Role.PATIENT.name())
                .bloodRequest(bloodRequest)
                .hospital(bloodRequest != null ? bloodRequest.getHospital() : null)
                .patient(bloodRequest != null ? bloodRequest.getPatient() : null)
                .title(title)
                .message(message)
                .notificationType(type)
                .category(inferCategoryFromType(type))
                .deliveryChannel(DeliveryChannel.IN_APP)
                .priority("HIGH")
                .priorityEnum(NotificationPriority.HIGH)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .deleted(false)
                .actionUrl(actionUrl != null ? actionUrl : "/patient/requests")
                .relatedEntityType(bloodRequest != null ? "BLOOD_REQUEST" : "PATIENT")
                .relatedEntityId(bloodRequest != null ? bloodRequest.getId() : patientUser.getId())
                .sentAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        publishRealtimeNotification(saved);
    }

    @Override
    @Transactional
    public void notifyAdmin(String title, String message, NotificationType type, String actionUrl) {
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        if (admins.isEmpty()) return;

        for (User admin : admins) {
            Notification notification = Notification.builder()
                    .recipientUser(admin)
                    .recipientRole(Role.ADMIN.name())
                    .title(title)
                    .message(message)
                    .notificationType(type)
                    .category(inferCategoryFromType(type))
                    .deliveryChannel(DeliveryChannel.IN_APP)
                    .priority("HIGH")
                    .priorityEnum(NotificationPriority.HIGH)
                    .status(NotificationStatus.SENT)
                    .readStatus(false)
                    .deleted(false)
                    .actionUrl(actionUrl != null ? actionUrl : "/admin/dashboard")
                    .sentAt(LocalDateTime.now())
                    .build();

            Notification saved = notificationRepository.save(notification);
            publishRealtimeNotification(saved);
        }
    }

    @Override
    @Transactional
    public void notifyNearbyCompatibleDonors(BloodRequest request) {
        if (request == null) {
            log.warn("[DONOR-NOTIFY] notifyNearbyCompatibleDonors received null BloodRequest");
            return;
        }

        long engineStartTime = System.currentTimeMillis();
        Hospital hospital = request.getHospital();
        String hospitalName = hospital != null ? hospital.getHospitalName() : "Hospital";
        String hospitalAddress = hospital != null ? hospital.getAddress() : "Hospital Address";
        BloodGroup requestedGroup = request.getBloodGroupNeeded();

        DonorMatchingResult matchResult = donorMatchingService.evaluateEligibleDonors(request);
        List<DonorProfile> matchedDonors = matchResult.getMatchedDonors();

        if (matchedDonors.isEmpty()) {
            log.warn("[DONOR-NOTIFY] No eligible donors matched within 50 KM radius for Emergency Request #{}", request.getId());
            return;
        }

        String bgName = requestedGroup != null ? requestedGroup.name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";
        String title = String.format("Emergency %s blood needed at %s", bgName, hospitalName);
        String message = String.format("Urgent requirement for %d units of %s blood at %s.", request.getUnitsRequired(), bgName, hospitalName);
        String actionUrl = "/donor/requests";

        log.info("[SMART-ENGINE] Dispatching Notifications to {} matched donors", matchedDonors.size());

        for (DonorProfile donor : matchedDonors) {
            String donorEmail = (donor.getUser() != null && donor.getUser().getEmail() != null) ? donor.getUser().getEmail() : donor.getEmail();
            Double distanceKm = matchResult.getDonorDistances().getOrDefault(donor.getId(), 0.0);

            notifyDonor(donor, title, message, NotificationType.EMERGENCY_BLOOD_REQUEST, actionUrl, request, hospital);

            RealtimeEventDTO emergencyEvent = RealtimeEventDTO.of(
                    RealtimeEventType.EMERGENCY_REQUEST_ALERT,
                    "BLOOD_REQUEST",
                    request.getId(),
                    title,
                    message,
                    request
            );
            realtimeService.publishDonorUpdate(donor.getId(), emergencyEvent);

            if (notificationOrchestrator != null) {
                com.bloodbridge.notification.NotificationPayload payload = com.bloodbridge.notification.NotificationPayload.builder()
                        .emergencyRequestId(request.getId())
                        .recipientUser(donor.getUser())
                        .recipientDonor(donor)
                        .hospital(hospital)
                        .bloodRequest(request)
                        .recipientEmail(donorEmail)
                        .title(title)
                        .message(message)
                        .notificationType(NotificationType.EMERGENCY_BLOOD_REQUEST)
                        .priority("HIGH")
                        .actionUrl(actionUrl)
                        .extraData(java.util.Map.of("distanceKm", distanceKm))
                        .build();
                try {
                    notificationOrchestrator.dispatchNotification(payload);
                } catch (Exception e) {
                    log.error("[ORCHESTRATOR-DISPATCH-ERROR] Failed to dispatch via orchestrator: {}", e.getMessage());
                }
            }

            if (emailNotificationRepository.existsByEmergencyRequestIdAndDonorId(request.getId(), donor.getId())) {
                log.info("[DUPLICATE-PREVENTION] Donor ID {} ({}) already notified for Request #{}. Skipping duplicate email.",
                        donor.getId(), donorEmail, request.getId());
                continue;
            }

            EmailNotification emailNotif = EmailNotification.builder()
                    .emergencyRequestId(request.getId())
                    .donorId(donor.getId())
                    .email(donorEmail)
                    .status(EmailDeliveryStatus.PENDING)
                    .deliveryAttempts(0)
                    .build();
            EmailNotification savedNotif = emailNotificationRepository.save(emailNotif);

            EmergencyMailDto mailDto = EmergencyMailDto.builder()
                    .toEmail(donorEmail)
                    .donorName(donor.getUser() != null && donor.getUser().getFullName() != null ? donor.getUser().getFullName() : "Valued Donor")
                    .hospitalName(hospitalName)
                    .bloodGroup(requestedGroup != null ? requestedGroup.name() : "")
                    .unitsRequired(request.getUnitsRequired())
                    .urgencyLevel(request.getUrgencyLevel() != null ? request.getUrgencyLevel().name() : "EMERGENCY")
                    .hospitalAddress(hospitalAddress)
                    .city(donor.getCity())
                    .state(donor.getState())
                    .requiredByDate(request.getRequiredByDate() != null ? request.getRequiredByDate().toString() : "Immediate")
                    .reason(request.getReason() != null ? request.getReason() : "Emergency Requirement")
                    .loginUrl("http://localhost:5173/login")
                    .build();

            emergencyEmailExecutor.execute(() -> {
                int maxAttempts = 3;
                int attempts = 0;
                boolean sentSuccessfully = false;

                while (attempts < maxAttempts && !sentSuccessfully) {
                    attempts++;
                    long startTime = System.currentTimeMillis();
                    try {
                        emailService.sendEmergencyAlert(mailDto);
                        long duration = System.currentTimeMillis() - startTime;

                        savedNotif.setStatus(EmailDeliveryStatus.SENT);
                        savedNotif.setSentAt(LocalDateTime.now());
                        savedNotif.setDeliveryAttempts(attempts);
                        savedNotif.setSmtpResponseTimeMs(duration);
                        savedNotif.setFailureReason(null);
                        emailNotificationRepository.save(savedNotif);

                        sentSuccessfully = true;
                    } catch (Exception e) {
                        long duration = System.currentTimeMillis() - startTime;
                        savedNotif.setDeliveryAttempts(attempts);
                        savedNotif.setSmtpResponseTimeMs(duration);
                        savedNotif.setFailureReason("Attempt " + attempts + " Failed: " + e.getMessage());

                        if (attempts >= maxAttempts) {
                            savedNotif.setStatus(EmailDeliveryStatus.FAILED);
                            emailNotificationRepository.save(savedNotif);
                        }
                    }
                }
            });
        }

        long engineTotalTime = System.currentTimeMillis() - engineStartTime;
        log.info("[SMART-ENGINE-COMPLETED] Emergency Notification Pipeline Executed in {} ms", engineTotalTime);
    }

    private void dispatchToProvider(Notification notification) {
        if (notificationProviders == null) return;
        for (NotificationProvider provider : notificationProviders) {
            if (provider.supports(notification.getDeliveryChannel())) {
                provider.send(notification);
                return;
            }
        }
        notification.setStatus(NotificationStatus.FAILED);
        notification.setLastFailureReason("Unsupported delivery channel: " + notification.getDeliveryChannel());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private NotificationCategory inferCategoryFromType(NotificationType type) {
        if (type == null) return NotificationCategory.SYSTEM;
        switch (type) {
            case EMERGENCY_BLOOD_REQUEST:
                return NotificationCategory.EMERGENCY;
            case DONATION_ACCEPTED:
            case DONATION_CONFIRMED:
            case HOSPITAL_APPROVAL:
                return NotificationCategory.DONATION_APPROVED;
            case DONATION_COMPLETED:
                return NotificationCategory.DONATION_COMPLETED;
            case REQUEST_REJECTED:
            case DONOR_DECLINED:
            case DONOR_REJECTED:
                return NotificationCategory.REQUEST_CANCELLED;
            case SYSTEM_ANNOUNCEMENT:
                return NotificationCategory.ADMIN;
            case DONATION_REMINDER:
                return NotificationCategory.REMINDER;
            default:
                return NotificationCategory.SYSTEM;
        }
    }

    private NotificationPriority parsePriorityEnum(String priorityStr) {
        if (priorityStr == null) return NotificationPriority.NORMAL;
        try {
            return NotificationPriority.valueOf(priorityStr.toUpperCase());
        } catch (Exception e) {
            return NotificationPriority.NORMAL;
        }
    }
}
