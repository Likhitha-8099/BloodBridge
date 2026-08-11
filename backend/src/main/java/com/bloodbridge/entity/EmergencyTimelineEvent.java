package com.bloodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity tracking the end-to-end lifecycle timeline events of an emergency request.
 */
@Entity
@Table(name = "emergency_timeline_events", indexes = {
        @Index(name = "idx_timeline_req_id", columnList = "emergency_request_id"),
        @Index(name = "idx_timeline_event_type", columnList = "event_type"),
        @Index(name = "idx_timeline_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Emergency request ID is required")
    @Column(name = "emergency_request_id", nullable = false)
    private Long emergencyRequestId;

    @NotNull(message = "Event type is required")
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "actor", length = 100)
    private String actor;

    @Column(name = "metadata_json", length = 2000)
    private String metadataJson;

    @CreatedDate
    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;
}
