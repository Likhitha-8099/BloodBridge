package com.bloodbridge.repository;

import com.bloodbridge.entity.MatchingAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing MatchingAnalytics entities.
 */
@Repository
public interface MatchingAnalyticsRepository extends JpaRepository<MatchingAnalytics, Long> {

    /**
     * Finds most recent matching analytics record for a blood request.
     */
    Optional<MatchingAnalytics> findTopByBloodRequestIdOrderByCreatedAtDesc(Long bloodRequestId);

    /**
     * Finds all matching analytics records for a hospital.
     */
    List<MatchingAnalytics> findByHospitalId(Long hospitalId);
}
