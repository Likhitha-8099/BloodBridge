package com.bloodbridge.repository;

import com.bloodbridge.entity.Donation;
import com.bloodbridge.enums.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for {@link Donation} entity.
 */
@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    /**
     * Finds donations associated with a specific donor profile.
     *
     * @param donorId the donor profile ID
     * @return a list of donations
     */
    List<Donation> findByDonorId(Long donorId);

    /**
     * Finds donations associated with a specific patient profile.
     *
     * @param patientId the patient profile ID
     * @return a list of donations
     */
    List<Donation> findByPatientId(Long patientId);

    /**
     * Finds donations associated with a specific hospital.
     *
     * @param hospitalId the hospital ID
     * @return a list of donations
     */
    List<Donation> findByHospitalId(Long hospitalId);

    /**
     * Finds donations associated with a specific blood request.
     *
     * @param requestId the blood request ID
     * @return a list of donations
     */
    List<Donation> findByBloodRequestId(Long requestId);

    /**
     * Finds donations matching a specific status.
     *
     * @param status the donation status
     * @return a list of donations
     */
    List<Donation> findByStatus(DonationStatus status);

    /**
     * Counts donations matching a specific status.
     *
     * @param status the donation status
     * @return the count of donations
     */
    long countByStatus(DonationStatus status);

    /**
     * Checks if a donation already exists for a specific match result in any of the specified statuses.
     * Helps check if a donor attempts to accept the request twice.
     *
     * @param matchResultId the match result ID
     * @param statuses      the collection of statuses
     * @return true if a donation exists in those states, false otherwise
     */
    boolean existsByMatchResultIdAndStatusIn(Long matchResultId, Collection<DonationStatus> statuses);

    /**
     * Aggregate monthly donation counts starting from a specific date.
     *
     * @param startDate limit results since this date
     * @return list of array objects representing [Year, Month, count]
     */
    @org.springframework.data.jpa.repository.Query("SELECT YEAR(d.createdAt) as yr, MONTH(d.createdAt) as mo, COUNT(d) FROM Donation d WHERE d.createdAt >= :startDate GROUP BY YEAR(d.createdAt), MONTH(d.createdAt)")
    List<Object[]> getMonthlyDonationCounts(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);
}
