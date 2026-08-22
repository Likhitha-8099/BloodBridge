package com.bloodbridge.repository;

import com.bloodbridge.entity.EmergencyResponse;
import com.bloodbridge.enums.EmergencyResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing EmergencyResponse entities with N+1 fetch optimization.
 */
@Repository
public interface EmergencyResponseRepository extends JpaRepository<EmergencyResponse, Long> {

    /**
     * Finds existing response for a request and donor.
     */
    Optional<EmergencyResponse> findByBloodRequestIdAndDonorId(Long bloodRequestId, Long donorId);

    /**
     * Duplicate Check: Verifies if a donor has already responded with a specific status.
     */
    boolean existsByBloodRequestIdAndDonorIdAndStatus(Long bloodRequestId, Long donorId, EmergencyResponseStatus status);

    /**
     * Counts emergency responses by request ID and status.
     */
    long countByBloodRequestIdAndStatus(Long bloodRequestId, EmergencyResponseStatus status);

    /**
     * Optimized JOIN FETCH query returning all accepted responses for a request along with donor and user records.
     */
    @Query("SELECT r FROM EmergencyResponse r JOIN FETCH r.donor d JOIN FETCH d.user u WHERE r.bloodRequest.id = :requestId AND r.status = :status ORDER BY r.acceptedAt ASC")
    List<EmergencyResponse> findWithDonorDetailsByBloodRequestIdAndStatus(@Param("requestId") Long requestId, @Param("status") EmergencyResponseStatus status);

    /**
     * Finds all responses for a request.
     */
    List<EmergencyResponse> findByBloodRequestId(Long bloodRequestId);

    /**
     * Finds all responses submitted by a specific donor.
     */
    @Query("SELECT r FROM EmergencyResponse r JOIN FETCH r.bloodRequest b JOIN FETCH b.hospital h WHERE r.donor.id = :donorId ORDER BY r.createdAt DESC")
    List<EmergencyResponse> findByDonorIdWithDetails(@Param("donorId") Long donorId);

    /**
     * Calculates average response time in seconds for accepted responses of a request.
     */
    @Query("SELECT AVG(r.responseTimeSeconds) FROM EmergencyResponse r WHERE r.bloodRequest.id = :requestId AND r.status = 'ACCEPTED'")
    Double findAverageResponseTimeSecondsByBloodRequestId(@Param("requestId") Long requestId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmergencyResponse er WHERE er.donor.id = :donorId")
    void deleteAllByDonorId(@Param("donorId") Long donorId);
}
