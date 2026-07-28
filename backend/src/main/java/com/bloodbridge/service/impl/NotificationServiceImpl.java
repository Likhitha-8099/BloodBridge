package com.bloodbridge.service.impl;

import com.bloodbridge.dto.NotificationCreateRequest;
import com.bloodbridge.dto.NotificationResponse;
import com.bloodbridge.dto.NotificationStatisticsResponse;
import com.bloodbridge.dto.NotificationSummaryResponse;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.NotificationNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.NotificationMapper;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.EmailService;
import com.bloodbridge.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing notifications.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        User recipient = userRepository.findById(request.getRecipientUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found for ID: " + request.getRecipientUserId()));

        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .title(request.getTitle())
                .message(request.getMessage())
                .notificationType(request.getNotificationType())
                .deliveryChannel(DeliveryChannel.IN_APP)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NotificationResponse sendNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for ID: " + id));

        if (notification.getDeliveryChannel() == DeliveryChannel.EMAIL && notification.getStatus() == NotificationStatus.PENDING) {
            sendEmailNotification(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for ID: " + id));

        User user = getAuthenticatedUser();

        // Enforce recipient ownership
        if (!notification.getRecipientUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to mark this notification as read");
        }

        notification.setReadStatus(true);
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSummaryResponse> getMyNotifications() {
        User user = getAuthenticatedUser();
        List<Notification> notifications = notificationRepository.findByRecipientUserId(user.getId());
        return mapToSummaryResponses(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSummaryResponse> getUnreadNotifications() {
        User user = getAuthenticatedUser();
        List<Notification> notifications = notificationRepository.findByRecipientUserIdAndReadStatus(user.getId(), false);
        return mapToSummaryResponses(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for ID: " + id));

        User user = getAuthenticatedUser();

        // Restrict to recipient or ADMIN
        if (!notification.getRecipientUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You are not authorized to view this notification");
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void sendEmailNotification(Notification notification) {
        if (notification == null || notification.getRecipientUser() == null) {
            return;
        }

        log.info("Sending email notification to {}", notification.getRecipientUser().getEmail());
        try {
            emailService.sendEmail(
                    notification.getRecipientUser().getEmail(),
                    notification.getTitle(),
                    notification.getMessage()
            );
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
        }
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void triggerNotificationEvent(User recipient, String title, String message, NotificationType type) {
        if (recipient == null) {
            return;
        }

        // 1. Create In-App Notification
        Notification inAppNotification = Notification.builder()
                .recipientUser(recipient)
                .title(title)
                .message(message)
                .notificationType(type)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .build();
        notificationRepository.save(inAppNotification);

        // 2. Create Email Notification and Dispatch
        Notification emailNotification = Notification.builder()
                .recipientUser(recipient)
                .title(title)
                .message(message)
                .notificationType(type)
                .deliveryChannel(DeliveryChannel.EMAIL)
                .status(NotificationStatus.PENDING)
                .readStatus(false)
                .build();
        Notification savedEmail = notificationRepository.save(emailNotification);

        // Trigger email delivery
        sendEmailNotification(savedEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatisticsResponse getNotificationStatistics() {
        long total = notificationRepository.count();
        long read = notificationRepository.countByReadStatus(true);
        long unread = notificationRepository.countByReadStatus(false);
        long failed = notificationRepository.countByStatus(NotificationStatus.FAILED);
        long sent = notificationRepository.countByStatus(NotificationStatus.SENT);

        return NotificationStatisticsResponse.builder()
                .totalNotifications(total)
                .readNotifications(read)
                .unreadNotifications(unread)
                .failedNotifications(failed)
                .sentNotifications(sent)
                .build();
    }

    private List<NotificationSummaryResponse> mapToSummaryResponses(List<Notification> notifications) {
        return notifications.stream()
                .map(notificationMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for authenticated email: " + email));
    }
}
