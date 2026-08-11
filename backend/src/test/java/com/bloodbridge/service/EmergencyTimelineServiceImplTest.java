package com.bloodbridge.service;

import com.bloodbridge.entity.EmergencyTimelineEvent;
import com.bloodbridge.repository.EmergencyTimelineRepository;
import com.bloodbridge.service.impl.EmergencyTimelineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmergencyTimelineServiceImplTest {

    private EmergencyTimelineRepository repository;
    private EmergencyTimelineService timelineService;

    @BeforeEach
    void setUp() {
        repository = mock(EmergencyTimelineRepository.class);
        timelineService = new EmergencyTimelineServiceImpl(repository);
    }

    @Test
    void recordEvent_SavesAndReturnsTimelineEvent() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        EmergencyTimelineEvent event = timelineService.recordEvent(
                1L, "EMERGENCY_CREATED", "New Emergency Request",
                "Hospital created blood request", "hospital@example.com", "{}"
        );

        assertNotNull(event);
        assertEquals(1L, event.getEmergencyRequestId());
        assertEquals("EMERGENCY_CREATED", event.getEventType());
        verify(repository, times(1)).save(any());
    }

    @Test
    void getTimelineForRequest_ReturnsOrderedEvents() {
        EmergencyTimelineEvent e1 = EmergencyTimelineEvent.builder().emergencyRequestId(1L).eventType("EMERGENCY_CREATED").build();
        when(repository.findByEmergencyRequestIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(e1));

        List<EmergencyTimelineEvent> events = timelineService.getTimelineForRequest(1L);

        assertEquals(1, events.size());
        assertEquals("EMERGENCY_CREATED", events.get(0).getEventType());
    }
}
