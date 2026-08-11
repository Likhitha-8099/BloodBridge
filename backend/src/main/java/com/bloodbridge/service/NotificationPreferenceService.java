package com.bloodbridge.service;

import com.bloodbridge.dto.request.NotificationPreferenceRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationPreferenceResponse;
import com.bloodbridge.entity.NotificationPreference;
import com.bloodbridge.entity.User;

public interface NotificationPreferenceService {
    ApiResponse<NotificationPreferenceResponse> getUserPreferences(String email);

    ApiResponse<NotificationPreferenceResponse> updatePreferences(String email, NotificationPreferenceRequest request);

    NotificationPreference getOrCreateDefaultPreference(User user);

    boolean isChannelEnabled(NotificationPreference pref, com.bloodbridge.enums.DeliveryChannel channel);

    boolean isCategoryEnabled(NotificationPreference pref, com.bloodbridge.enums.NotificationCategory category);
}
