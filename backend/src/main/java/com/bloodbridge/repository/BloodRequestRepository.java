package com.bloodbridge.repository;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link BloodRequest} entity.
 * Provides database operations for managing blood requests.
 */
@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    /**
     * Find blood requests registered by a specific patient profile ID.
     *
     * @param patientId the patient profile ID
     * @return a list of blood requests
     */
    List<BloodRequest> findByPatientId(Long patientId);

    /**
     * Find blood requests assigned to a specific hospital profile ID.
     *
     * @param hospitalId the hospital profile ID
     * @return a list of blood requests
     */
    List<BloodRequest> findByHospitalId(Long hospitalId);

    /**
     * Find blood requests matching a specific status.
     *
     * @param status the request status
     * @return a list of blood requests
     */
    List<BloodRequest> findByStatus(RequestStatus status);

    /**
     * Find blood requests matching a specific blood group needed.
     *
     * @param bloodGroupNeeded the blood group
     * @return a list of blood requests
     */
    List<BloodRequest> findByBloodGroupNeeded(BloodGroup bloodGroupNeeded);

    /**
     * Find blood requests matching both a specific status and a blood group.
     *
     * @param status           the request status
     * @param bloodGroupNeeded the blood group
     * @return a list of blood requests
     */
    List<BloodRequest> findByStatusAndBloodGroupNeeded(RequestStatus status, BloodGroup bloodGroupNeeded);

    /**
     * Find blood requests matching any of the specified statuses.
     *
     * @param statuses the list of request statuses
     * @return a list of blood requests
     */
    List<BloodRequest> findByStatusIn(List<RequestStatus> statuses);

    /**
     * Counts requests by status.
     *
     * @param status the request status
     * @return count of requests
     */
    long countByStatus(RequestStatus status);

    /**
     * Groups and counts requests by blood group needed.
     *
     * @return list of array objects representing [BloodGroup, count]
     */
    @org.springframework.data.jpa.repository.Query("SELECT r.bloodGroupNeeded, COUNT(r) FROM BloodRequest r GROUP BY r.bloodGroupNeeded")
    List<Object[]> getBloodGroupDistribution();

    /**
     * Aggregate monthly request counts starting from a specific date.
     *
     * @param startDate limit results since this date
     * @return list of array objects representing [Year, Month, count]
     */
    @org.springframework.data.jpa.repository.Query("SELECT YEAR(r.createdAt) as yr, MONTH(r.createdAt) as mo, COUNT(r) FROM BloodRequest r WHERE r.createdAt >= :startDate GROUP BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> getMonthlyRequestCounts(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);
}
