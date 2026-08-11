package com.bloodbridge.repository;

import com.bloodbridge.entity.EmergencyTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for EmergencyTimelineEvent entity.
 */
@Repository
public interface EmergencyTimelineRepository extends JpaRepository<EmergencyTimelineEvent, Long> {

    List<EmergencyTimelineEvent> findByEmergencyRequestIdOrderByCreatedAtAsc(Long emergencyRequestId);

    long countByEmergencyRequestId(Long emergencyRequestId);
}
