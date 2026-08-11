package com.bloodbridge.notification;

import com.bloodbridge.entity.NotificationPreference;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.NotificationPriority;
import com.bloodbridge.service.impl.QuietHoursServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class QuietHoursTest {

    private QuietHoursServiceImpl quietHoursService;
    private NotificationPreference activeQuietPref;

    @BeforeEach
    void setUp() {
        quietHoursService = new QuietHoursServiceImpl();

        // Quiet hours 00:00 to 23:59 (covering all day for predictable testing)
        activeQuietPref = NotificationPreference.builder()
                .user(User.builder().id(1L).build())
                .quietHoursEnabled(true)
                .quietHoursStart(LocalTime.of(0, 0))
                .quietHoursEnd(LocalTime.of(23, 59))
                .timezone("UTC")
                .build();
    }

    @Test
    @DisplayName("Should suppress REWARD, REMINDER, SYSTEM, ADMIN during quiet hours")
    void testSuppressesNonEmergencyDuringQuietHours() {
        assertTrue(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.REWARD, NotificationPriority.NORMAL));
        assertTrue(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.REMINDER, NotificationPriority.NORMAL));
        assertTrue(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.SYSTEM, NotificationPriority.NORMAL));
        assertTrue(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.ADMIN, NotificationPriority.NORMAL));
    }

    @Test
    @DisplayName("EMERGENCY category must strictly BYPASS quiet hours")
    void testEmergencyCategoryBypassesQuietHours() {
        assertFalse(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.EMERGENCY, NotificationPriority.NORMAL));
        assertFalse(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.EMERGENCY, NotificationPriority.HIGH));
    }

    @Test
    @DisplayName("CRITICAL priority must strictly BYPASS quiet hours")
    void testCriticalPriorityBypassesQuietHours() {
        assertFalse(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.REWARD, NotificationPriority.CRITICAL));
        assertFalse(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.ADMIN, NotificationPriority.CRITICAL));
    }

    @Test
    @DisplayName("Should not suppress when quiet hours are disabled")
    void testDisabledQuietHours() {
        activeQuietPref.setQuietHoursEnabled(false);
        assertFalse(quietHoursService.isQuietHoursSuppressed(activeQuietPref, NotificationCategory.REWARD, NotificationPriority.NORMAL));
    }
}
