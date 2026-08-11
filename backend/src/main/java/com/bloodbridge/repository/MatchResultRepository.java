package com.bloodbridge.repository;

import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link MatchResult} entity.
 */
@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    /**
     * Finds matches associated with a specific blood request.
     *
     * @param requestId the blood request ID
     * @return a list of match results
     */
    List<MatchResult> findByBloodRequestId(Long requestId);

    /**
     * Finds matches associated with a specific blood request ordered by rank position.
     *
     * @param requestId the blood request ID
     * @return a list of match results ordered by rank ASC
     */
    List<MatchResult> findByBloodRequestIdOrderByRankAsc(Long requestId);

    /**
     * Finds matches associated with a specific donor profile.
     *
     * @param donorId the donor profile ID
     * @return a list of match results
     */
    List<MatchResult> findByDonorId(Long donorId);

    /**
     * Finds match for a specific request and donor.
     *
     * @param requestId blood request ID
     * @param donorId donor profile ID
     * @return Optional of MatchResult
     */
    Optional<MatchResult> findByBloodRequestIdAndDonorId(Long requestId, Long donorId);

    /**
     * Finds matches with a specific status.
     *
     * @param status the match status
     * @return a list of match results
     */
    List<MatchResult> findByStatus(MatchStatus status);

    /**
     * Checks if a match already exists between a specific request and donor.
     *
     * @param requestId the request ID
     * @param donorId the donor ID
     * @return true if match exists, false otherwise
     */
    boolean existsByBloodRequestIdAndDonorId(Long requestId, Long donorId);

    /**
     * Deletes all match results for a given blood request (for recalculation).
     *
     * @param requestId the blood request ID
     */
    void deleteByBloodRequestId(Long requestId);

    /**
     * Counts match results matching a specific status.
     *
     * @param status the match status
     * @return count of matches
     */
    long countByStatus(MatchStatus status);

    /**
     * Counts total matched donors for a hospital across its blood requests.
     */
    long countByBloodRequestHospitalId(Long hospitalId);

    /**
     * Counts total matched donors for a patient across their blood requests.
     */
    long countByBloodRequestPatientId(Long patientId);

    /**
     * Counts matches for a donor by match status.
     */
    long countByDonorIdAndStatus(Long donorId, MatchStatus status);
}
