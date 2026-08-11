package com.bloodbridge.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Implementation of NotificationOrchestrator utilizing the Strategy Pattern across active channels in parallel.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@Service
@Slf4j
public class NotificationOrchestratorImpl implements NotificationOrchestrator {

    private final List<NotificationChannel> channels;
    private final Executor emergencyEmailExecutor;

    public NotificationOrchestratorImpl(List<NotificationChannel> channels) {
        this(channels, new org.springframework.core.task.SyncTaskExecutor());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public NotificationOrchestratorImpl(
            List<NotificationChannel> channels,
            @Qualifier("emergencyEmailExecutor") Executor emergencyEmailExecutor
    ) {
        this.channels = channels;
        this.emergencyEmailExecutor = emergencyEmailExecutor != null ? emergencyEmailExecutor : new org.springframework.core.task.SyncTaskExecutor();
    }

    @Override
    public void dispatchNotification(NotificationPayload payload) {
        log.info("[ORCHESTRATOR] Parallel dispatch initiated for Emergency Request #{} across {} strategy channel(s)",
                payload.getEmergencyRequestId(), channels.size());

        Map<String, Boolean> deliveryResults = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = channels.stream()
                .filter(NotificationChannel::isEnabled)
                .map(channel -> CompletableFuture.runAsync(() -> {
                    String channelName = channel.getChannel().name();
                    try {
                        log.info("[ORCHESTRATOR] [PARALLEL-START] Routing to Channel: {}", channelName);
                        boolean success = channel.send(payload);
                        deliveryResults.put(channelName, success);
                        log.info("[ORCHESTRATOR] [PARALLEL-COMPLETE] Channel {} result: {}", channelName, success);
                    } catch (Exception e) {
                        log.error("[ORCHESTRATOR-CHANNEL-ERROR] Channel {} failed independently: {}", channelName, e.getMessage(), e);
                        deliveryResults.put(channelName, false);
                    }
                }, emergencyEmailExecutor))
                .toList();

        // Wait for all channels to complete (non-blocking for HTTP callers if invoked asynchronously)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("[ORCHESTRATOR-COMPLETE] Emergency Request #{} multi-channel delivery summary: {}",
                payload.getEmergencyRequestId(), deliveryResults);
    }

    @Override
    public List<NotificationChannel> getRegisteredChannels() {
        return List.copyOf(channels);
    }
}
