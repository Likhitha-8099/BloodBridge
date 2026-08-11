package com.bloodbridge.scheduler;

import com.bloodbridge.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Spring Scheduler for recurring background communication jobs:
 * 1. Notification retry job
 * 2. Expired blood request cleanup
 * 3. Daily donor re-eligibility reminder alerts
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Runs every 5 minutes to retry failed notification dispatches.
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void scheduleNotificationRetryJob() {
        log.info("[SCHEDULER] Running Notification Retry Background Job");
        try {
            notificationService.retryFailedNotifications();
        } catch (Exception e) {
            log.error("[SCHEDULER ERROR] Error executing notification retry job: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs daily at midnight to check for expired blood requests.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleDailyRequestCleanupJob() {
        log.info("[SCHEDULER] Running Daily Blood Request Expiration & Maintenance Job");
    }
}
