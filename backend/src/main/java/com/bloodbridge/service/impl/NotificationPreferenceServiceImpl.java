package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.NotificationPreferenceRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationPreferenceResponse;
import com.bloodbridge.entity.NotificationPreference;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.repository.NotificationPreferenceRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<NotificationPreferenceResponse> getUserPreferences(String email) {
        User user = findUserByEmail(email);
        NotificationPreference pref = getOrCreateDefaultPreference(user);
        return ApiResponse.success("Notification preferences retrieved successfully", mapToResponse(pref));
    }

    @Override
    @Transactional
    public ApiResponse<NotificationPreferenceResponse> updatePreferences(String email, NotificationPreferenceRequest request) {
        User user = findUserByEmail(email);
        NotificationPreference pref = getOrCreateDefaultPreference(user);

        if (request.getEmailEnabled() != null) pref.setEmailEnabled(request.getEmailEnabled());
        if (request.getPushEnabled() != null) pref.setPushEnabled(request.getPushEnabled());
        if (request.getWebSocketEnabled() != null) pref.setWebSocketEnabled(request.getWebSocketEnabled());
        
        // Emergency alerts are ALWAYS ON per requirement
        pref.setEmergencyAlertsEnabled(true);

        if (request.getRewardNotificationsEnabled() != null) pref.setRewardNotificationsEnabled(request.getRewardNotificationsEnabled());
        if (request.getReminderNotificationsEnabled() != null) pref.setReminderNotificationsEnabled(request.getReminderNotificationsEnabled());
        if (request.getAdminMessagesEnabled() != null) pref.setAdminMessagesEnabled(request.getAdminMessagesEnabled());
        if (request.getQuietHoursEnabled() != null) pref.setQuietHoursEnabled(request.getQuietHoursEnabled());

        if (request.getQuietHoursStart() != null && !request.getQuietHoursStart().trim().isEmpty()) {
            pref.setQuietHoursStart(parseLocalTime(request.getQuietHoursStart()));
        }
        if (request.getQuietHoursEnd() != null && !request.getQuietHoursEnd().trim().isEmpty()) {
            pref.setQuietHoursEnd(parseLocalTime(request.getQuietHoursEnd()));
        }
        if (request.getTimezone() != null && !request.getTimezone().trim().isEmpty()) {
            pref.setTimezone(request.getTimezone());
        }

        NotificationPreference saved = preferenceRepository.save(pref);
        log.info("Updated notification preferences for user ID: {}", user.getId());
        return ApiResponse.success("Notification preferences updated successfully", mapToResponse(saved));
    }

    @Override
    @Transactional
    public NotificationPreference getOrCreateDefaultPreference(User user) {
        return preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    NotificationPreference newPref = NotificationPreference.builder()
                            .user(user)
                            .emailEnabled(true)
                            .pushEnabled(true)
                            .webSocketEnabled(true)
                            .emergencyAlertsEnabled(true)
                            .rewardNotificationsEnabled(true)
                            .reminderNotificationsEnabled(true)
                            .adminMessagesEnabled(true)
                            .quietHoursEnabled(false)
                            .timezone("UTC")
                            .build();
                    return preferenceRepository.save(newPref);
                });
    }

    @Override
    public boolean isChannelEnabled(NotificationPreference pref, DeliveryChannel channel) {
        if (pref == null) return true;
        if (channel == null) return true;

        switch (channel) {
            case EMAIL:
                return Boolean.TRUE.equals(pref.getEmailEnabled());
            case PUSH:
                return Boolean.TRUE.equals(pref.getPushEnabled());
            case IN_APP:
            case WHATSAPP:
            case SMS:
                return Boolean.TRUE.equals(pref.getWebSocketEnabled()) || Boolean.TRUE.equals(pref.getPushEnabled());
            default:
                return true;
        }
    }

    @Override
    public boolean isCategoryEnabled(NotificationPreference pref, NotificationCategory category) {
        if (pref == null) return true;
        if (category == null) return true;

        switch (category) {
            case EMERGENCY:
                return true; // Emergency alerts are ALWAYS ON
            case REWARD:
                return Boolean.TRUE.equals(pref.getRewardNotificationsEnabled());
            case REMINDER:
                return Boolean.TRUE.equals(pref.getReminderNotificationsEnabled());
            case ADMIN:
                return Boolean.TRUE.equals(pref.getAdminMessagesEnabled());
            default:
                return true;
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private LocalTime parseLocalTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            try {
                return LocalTime.parse(timeStr);
            } catch (Exception ex) {
                log.warn("Could not parse quiet hours time: {}", timeStr);
                return null;
            }
        }
    }

    private NotificationPreferenceResponse mapToResponse(NotificationPreference pref) {
        return NotificationPreferenceResponse.builder()
                .id(pref.getId())
                .userId(pref.getUser() != null ? pref.getUser().getId() : null)
                .emailEnabled(pref.getEmailEnabled())
                .pushEnabled(pref.getPushEnabled())
                .webSocketEnabled(pref.getWebSocketEnabled())
                .emergencyAlertsEnabled(true)
                .rewardNotificationsEnabled(pref.getRewardNotificationsEnabled())
                .reminderNotificationsEnabled(pref.getReminderNotificationsEnabled())
                .adminMessagesEnabled(pref.getAdminMessagesEnabled())
                .quietHoursEnabled(pref.getQuietHoursEnabled())
                .quietHoursStart(pref.getQuietHoursStart())
                .quietHoursEnd(pref.getQuietHoursEnd())
                .timezone(pref.getTimezone())
                .createdAt(pref.getCreatedAt())
                .updatedAt(pref.getUpdatedAt())
                .build();
    }
}
