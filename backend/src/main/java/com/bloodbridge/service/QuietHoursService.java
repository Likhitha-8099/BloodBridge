package com.bloodbridge.service;

import com.bloodbridge.entity.NotificationPreference;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.NotificationPriority;

public interface QuietHoursService {
    /**
     * Evaluates whether a notification should be suppressed based on user's quiet hours preferences.
     *
     * @param pref User's notification preferences
     * @param category Notification category
     * @param priority Notification priority
     * @return true if notification delivery should be suppressed, false otherwise
     */
    boolean isQuietHoursSuppressed(NotificationPreference pref, NotificationCategory category, NotificationPriority priority);

    /**
     * Checks if current time is within user's quiet hours window.
     */
    boolean isInQuietHours(NotificationPreference pref);
}
