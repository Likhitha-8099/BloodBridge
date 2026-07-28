package com.bloodbridge.repository;

import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Finds matches associated with a specific donor profile.
     *
     * @param donorId the donor profile ID
     * @return a list of match results
     */
    List<MatchResult> findByDonorId(Long donorId);

    /**
     * Finds matches with a specific status.
     *
     * @param status the match status
     * @return a list of match results
     */
    List<MatchResult> findByStatus(MatchStatus status);

    /**
     * Checks if a match already exists between a specific request and donor.
     * Prevents generating duplicate match logs.
     *
     * @param requestId the request ID
     * @param donorId   the donor ID
     * @return true if match exists, false otherwise
     */
    boolean existsByBloodRequestIdAndDonorId(Long requestId, Long donorId);

    /**
     * Counts match results matching a specific status.
     *
     * @param status the match status
     * @return count of matches
     */
    long countByStatus(MatchStatus status);
}
