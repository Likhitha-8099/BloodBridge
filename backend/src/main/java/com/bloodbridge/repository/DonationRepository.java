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
     * Counts pending/active donations for a hospital matching given statuses.
     */
    long countByHospitalIdAndStatusIn(Long hospitalId, Collection<DonationStatus> statuses);

    /**
     * Counts pending/active donations for a donor matching given statuses.
     */
    long countByDonorIdAndStatusIn(Long donorId, Collection<DonationStatus> statuses);

    /**
     * Finds donations by donor ID, blood request ID, and status list.
     */
    List<Donation> findByDonorIdAndBloodRequestIdAndStatusIn(Long donorId, Long requestId, Collection<DonationStatus> statuses);

    /**
     * Checks if a donation already exists for a specific match result in any of the specified statuses.
     * Helps check if a donor attempts to accept the request twice.
     *
     * @param matchResultId the match result ID
     * @param statuses      the collection of statuses
     * @return true if a donation exists in those states, false otherwise
     */
    boolean existsByMatchResultIdAndStatusIn(Long matchResultId, Collection<DonationStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT d FROM Donation d " +
           "LEFT JOIN d.donor dp " +
           "LEFT JOIN dp.user u " +
           "WHERE (dp.id = :donorId OR (u.id = :userId) OR (dp.email = :userEmail) OR (u.email = :userEmail)) " +
           "ORDER BY d.createdAt DESC")
    List<Donation> findDonationsForDonorUser(
            @org.springframework.data.repository.query.Param("donorId") Long donorId,
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("userEmail") String userEmail);

    /**
     * Aggregate monthly donation counts starting from a specific date.
     *
     * @param startDate limit results since this date
     * @return list of array objects representing [Year, Month, count]
     */
    @org.springframework.data.jpa.repository.Query("SELECT YEAR(d.createdAt) as yr, MONTH(d.createdAt) as mo, COUNT(d) FROM Donation d WHERE d.createdAt >= :startDate GROUP BY YEAR(d.createdAt), MONTH(d.createdAt)")
    List<Object[]> getMonthlyDonationCounts(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Donation d SET d.donor = null, d.matchResult = null WHERE (d.donor.id = :donorId) OR (d.matchResult.id IN (SELECT mr.id FROM MatchResult mr WHERE mr.donor.id = :donorId))")
    void unlinkDonorProfile(@org.springframework.data.repository.query.Param("donorId") Long donorId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Donation d SET d.hospital = null WHERE d.hospital.id = :hospitalId")
    void unlinkHospitalProfile(@org.springframework.data.repository.query.Param("hospitalId") Long hospitalId);
}
