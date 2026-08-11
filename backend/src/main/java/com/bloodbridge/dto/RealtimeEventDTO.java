package com.bloodbridge.dto;

import com.bloodbridge.enums.RealtimeEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Standardized DTO payload wrapper for real-time WebSocket events.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealtimeEventDTO {

    private RealtimeEventType eventType;
    private Long requestId;
    private Long matchedDonorId;
    private Long donorId;
    private String donorName;
    private Long hospitalId;
    private String hospitalName;
    private String bloodGroup;
    private String status;

    private String entityType;
    private Long entityId;
    private String title;
    private String message;
    private Object payload;
    private String actionUrl;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static RealtimeEventDTO of(RealtimeEventType eventType, String title, String message, Object payload) {
        return RealtimeEventDTO.builder()
                .eventType(eventType)
                .title(title)
                .message(message)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RealtimeEventDTO of(RealtimeEventType eventType, String entityType, Long entityId, String title, String message, Object payload) {
        return RealtimeEventDTO.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .title(title)
                .message(message)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RealtimeEventDTO createEmergencyEvent(
            RealtimeEventType eventType,
            Long requestId,
            Long matchedDonorId,
            Long donorId,
            String donorName,
            Long hospitalId,
            String hospitalName,
            String bloodGroup,
            String status) {
        return RealtimeEventDTO.builder()
                .eventType(eventType)
                .requestId(requestId)
                .matchedDonorId(matchedDonorId)
                .donorId(donorId)
                .donorName(donorName)
                .hospitalId(hospitalId)
                .hospitalName(hospitalName)
                .bloodGroup(bloodGroup)
                .status(status)
                .entityType("BLOOD_REQUEST")
                .entityId(requestId)
                .title(eventType.name())
                .message(String.format("Emergency Request #%d event: %s", requestId, eventType.name()))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
