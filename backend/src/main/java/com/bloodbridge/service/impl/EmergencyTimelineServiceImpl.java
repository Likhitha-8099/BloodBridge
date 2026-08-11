package com.bloodbridge.service.impl;

import com.bloodbridge.entity.EmergencyTimelineEvent;
import com.bloodbridge.repository.EmergencyTimelineRepository;
import com.bloodbridge.service.EmergencyTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of EmergencyTimelineService for recording immutable emergency milestone logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyTimelineServiceImpl implements EmergencyTimelineService {

    private final EmergencyTimelineRepository repository;

    @Override
    @Transactional
    public EmergencyTimelineEvent recordEvent(Long emergencyRequestId, String eventType, String title, String description, String actor, String metadataJson) {
        log.info("[TIMELINE-LOG] Request #{} | Event: {} | Title: {} | Actor: {}",
                emergencyRequestId, eventType, title, actor != null ? actor : "SYSTEM");

        EmergencyTimelineEvent event = EmergencyTimelineEvent.builder()
                .emergencyRequestId(emergencyRequestId)
                .eventType(eventType)
                .title(title)
                .description(description)
                .actor(actor != null ? actor : "SYSTEM")
                .metadataJson(metadataJson)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyTimelineEvent> getTimelineForRequest(Long emergencyRequestId) {
        return repository.findByEmergencyRequestIdOrderByCreatedAtAsc(emergencyRequestId);
    }
}
