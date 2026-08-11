package com.bloodbridge.service;

import com.bloodbridge.entity.EmergencyTimelineEvent;

import java.util.List;

/**
 * Service interface for recording and retrieving emergency lifecycle timeline events.
 */
public interface EmergencyTimelineService {

    EmergencyTimelineEvent recordEvent(Long emergencyRequestId, String eventType, String title, String description, String actor, String metadataJson);

    List<EmergencyTimelineEvent> getTimelineForRequest(Long emergencyRequestId);
}
