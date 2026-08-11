package com.bloodbridge.service.impl;

import com.bloodbridge.entity.NotificationPreference;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.NotificationPriority;
import com.bloodbridge.service.QuietHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class QuietHoursServiceImpl implements QuietHoursService {

    @Override
    public boolean isQuietHoursSuppressed(NotificationPreference pref, NotificationCategory category, NotificationPriority priority) {
        if (pref == null) {
            return false;
        }

        // CRITICAL priority notifications strictly bypass quiet hours
        if (priority == NotificationPriority.CRITICAL) {
            log.info("[QUIET-HOURS] Notification with priority CRITICAL bypasses quiet hours.");
            return false;
        }

        // EMERGENCY notifications strictly bypass quiet hours
        if (category == NotificationCategory.EMERGENCY) {
            log.info("[QUIET-HOURS] Notification category EMERGENCY bypasses quiet hours.");
            return false;
        }

        // If quiet hours are disabled, do not suppress
        if (!Boolean.TRUE.equals(pref.getQuietHoursEnabled())) {
            return false;
        }

        if (!isInQuietHours(pref)) {
            return false;
        }

        // Suppress REWARD, REMINDER, SYSTEM, ADMIN during quiet hours
        if (category == NotificationCategory.REWARD ||
            category == NotificationCategory.REMINDER ||
            category == NotificationCategory.SYSTEM ||
            category == NotificationCategory.ADMIN) {
            log.info("[QUIET-HOURS] Suppressing notification category: {} during quiet hours window (Start: {}, End: {}, Timezone: {})",
                    category, pref.getQuietHoursStart(), pref.getQuietHoursEnd(), pref.getTimezone());
            return true;
        }

        return false;
    }

    @Override
    public boolean isInQuietHours(NotificationPreference pref) {
        if (pref == null || !Boolean.TRUE.equals(pref.getQuietHoursEnabled())) {
            return false;
        }

        LocalTime start = pref.getQuietHoursStart();
        LocalTime end = pref.getQuietHoursEnd();
        if (start == null || end == null) {
            return false;
        }

        ZoneId zoneId;
        try {
            zoneId = (pref.getTimezone() != null && !pref.getTimezone().trim().isEmpty())
                    ? ZoneId.of(pref.getTimezone())
                    : ZoneId.of("UTC");
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }

        LocalTime now = ZonedDateTime.now(zoneId).toLocalTime();

        if (start.isBefore(end)) {
            // e.g. 13:00 to 17:00
            return !now.isBefore(start) && !now.isAfter(end);
        } else {
            // Overnight quiet hours, e.g., 22:00 to 07:00
            return !now.isBefore(start) || !now.isAfter(end);
        }
    }
}
