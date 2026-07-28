package com.bloodbridge.repository;

import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link DonorProfile} entity.
 * Provides database operations for managing donor profiles.
 */
@Repository
public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {

    /**
     * Find a donor profile by the associated user's ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the profile if found, or empty otherwise
     */
    Optional<DonorProfile> findByUserId(Long userId);

    /**
     * Find donor profiles matching a specific blood group.
     *
     * @param bloodGroup the blood group enum to query
     * @return a list of matching donor profiles
     */
    List<DonorProfile> findByBloodGroup(BloodGroup bloodGroup);

    /**
     * Find donor profiles by exact city name match.
     *
     * @param city the city name
     * @return a list of donor profiles located in the city
     */
    List<DonorProfile> findByCity(String city);

    /**
     * Find donor profiles by city name, ignoring case sensitivity.
     *
     * @param city the city name
     * @return a list of donor profiles located in the city
     */
    List<DonorProfile> findByCityIgnoreCase(String city);

    /**
     * Find all donor profiles that are currently available for donation.
     *
     * @return a list of available donor profiles
     */
    List<DonorProfile> findByAvailableForDonationTrue();

    /**
     * Finds the top donors ranked by total donations.
     *
     * @param pageable page settings to limit results (e.g. top 10)
     * @return list of top donor projections
     */
    @org.springframework.data.jpa.repository.Query("SELECT d.user.fullName, d.bloodGroup, d.totalDonations FROM DonorProfile d ORDER BY d.totalDonations DESC")
    List<Object[]> findTopDonors(org.springframework.data.domain.Pageable pageable);

    /**
     * Groups and counts donors by their blood group.
     *
     * @return list of array objects representing [BloodGroup, count]
     */
    @org.springframework.data.jpa.repository.Query("SELECT d.bloodGroup, COUNT(d) FROM DonorProfile d GROUP BY d.bloodGroup")
    List<Object[]> getBloodGroupDistribution();
}
