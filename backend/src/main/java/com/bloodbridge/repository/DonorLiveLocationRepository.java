package com.bloodbridge.repository;

import com.bloodbridge.entity.DonorLiveLocation;
import com.bloodbridge.enums.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for querying donor live GPS telemetry records.
 */
@Repository
public interface DonorLiveLocationRepository extends JpaRepository<DonorLiveLocation, Long> {

    /**
     * Fetches the most-recent GPS fix for a specific donor responding to a specific request.
     */
    @Query("SELECT d FROM DonorLiveLocation d " +
           "WHERE d.donorId = :donorId AND d.bloodRequestId = :requestId " +
           "ORDER BY d.lastUpdated DESC")
    Optional<DonorLiveLocation> findLatestByDonorIdAndBloodRequestId(
            @Param("donorId") Long donorId,
            @Param("requestId") Long requestId);

    /**
     * Returns the full tracking route (all historical points) for a given request + donor, ordered chronologically.
     */
    @Query("SELECT d FROM DonorLiveLocation d " +
           "WHERE d.donorId = :donorId AND d.bloodRequestId = :requestId " +
           "ORDER BY d.lastUpdated ASC")
    List<DonorLiveLocation> findRouteByDonorIdAndBloodRequestId(
            @Param("donorId") Long donorId,
            @Param("requestId") Long requestId);

    /**
     * Returns the latest GPS fix from every active donor for a specific hospital's request.
     * Used to render the multi-donor hospital tracking map.
     */
    @Query("SELECT d FROM DonorLiveLocation d " +
           "WHERE d.bloodRequestId = :requestId " +
           "AND d.id IN (" +
           "  SELECT MAX(d2.id) FROM DonorLiveLocation d2 " +
           "  WHERE d2.bloodRequestId = :requestId " +
           "  GROUP BY d2.donorId" +
           ")")
    List<DonorLiveLocation> findLatestForAllDonorsByBloodRequestId(@Param("requestId") Long requestId);

    /**
     * Returns all tracking locations currently in non-terminal statuses — used for admin telemetry.
     */
    List<DonorLiveLocation> findByTrackingStatusIn(List<TrackingStatus> statuses);

    /**
     * Counts active tracking sessions (non-terminal) for a specific hospital.
     */
    @Query("SELECT COUNT(DISTINCT d.donorId) FROM DonorLiveLocation d " +
           "WHERE d.hospitalId = :hospitalId " +
           "AND d.trackingStatus IN ('STARTED', 'MOVING', 'STOPPED')")
    long countActiveSessionsByHospitalId(@Param("hospitalId") Long hospitalId);

    /**
     * Finds all location rows for a specific blood request (all donors, all timestamps).
     */
    List<DonorLiveLocation> findByBloodRequestId(Long bloodRequestId);

    /**
     * Computes average ETA across all donors in MOVING state updated after a given time.
     */
    @Query("SELECT AVG(d.etaMinutes) FROM DonorLiveLocation d " +
           "WHERE d.trackingStatus = 'MOVING' AND d.lastUpdated > :since")
    Double findAverageEtaMinutesSince(@Param("since") LocalDateTime since);

    /**
     * Computes average speed across all donors in MOVING state updated after a given time.
     */
    @Query("SELECT AVG(d.speedKmh) FROM DonorLiveLocation d " +
           "WHERE d.trackingStatus = 'MOVING' AND d.speedKmh IS NOT NULL AND d.lastUpdated > :since")
    Double findAverageSpeedKmhSince(@Param("since") LocalDateTime since);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DonorLiveLocation d WHERE d.donorId = :donorId")
    void deleteAllByDonorId(@Param("donorId") Long donorId);
}
