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
     * Find a donor profile by email.
     *
     * @param email donor email address
     * @return optional containing matching profile
     */
    Optional<DonorProfile> findByEmail(String email);

    /**
     * Checks if a donor profile exists for a given email.
     *
     * @param email donor email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a donor profile exists with email excluding a specific profile ID.
     *
     * @param email donor email address
     * @param id profile ID to exclude
     * @return true if exists, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Checks if a donor profile exists for a given user ID.
     *
     * @param userId user ID
     * @return true if exists, false otherwise
     */
    boolean existsByUserId(Long userId);

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
    @org.springframework.data.jpa.repository.Query("SELECT d.id, d.user.id, d.user.fullName, d.user.email, d.city, d.state, d.bloodGroup, d.totalDonations FROM DonorProfile d ORDER BY d.totalDonations DESC")
    List<Object[]> findTopDonors(org.springframework.data.domain.Pageable pageable);

    /**
     * Groups and counts donors by their blood group.
     *
     * @return list of array objects representing [BloodGroup, count]
     */
    @org.springframework.data.jpa.repository.Query("SELECT d.bloodGroup, COUNT(d) FROM DonorProfile d GROUP BY d.bloodGroup")
    List<Object[]> getBloodGroupDistribution();

    /**
     * Finds donor profiles matching dynamic search, bloodGroup, city, state, and availability filters with pagination.
     */
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonorProfile d WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:bloodGroup IS NULL OR d.bloodGroup = :bloodGroup) AND " +
           "(:city IS NULL OR :city = '' OR LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR :state = '' OR LOWER(d.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:available IS NULL OR d.availableForDonation = :available)")
    org.springframework.data.domain.Page<DonorProfile> searchDonors(
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("bloodGroup") BloodGroup bloodGroup,
            @org.springframework.data.repository.query.Param("city") String city,
            @org.springframework.data.repository.query.Param("state") String state,
            @org.springframework.data.repository.query.Param("available") Boolean available,
            org.springframework.data.domain.Pageable pageable);
    /**
     * Finds donor profiles matching PART 1 requirements:
     * donor.availableForDonation = true, donor.bloodGroup == request.bloodGroupNeeded,
     * and (donor.city == hospital.city OR donor.state == hospital.state)
     */
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonorProfile d WHERE " +
           "d.availableForDonation = true AND " +
           "d.bloodGroup = :bloodGroup AND " +
           "((:city IS NOT NULL AND d.city IS NOT NULL AND LOWER(d.city) = LOWER(:city)) OR " +
           " (:state IS NOT NULL AND d.state IS NOT NULL AND LOWER(d.state) = LOWER(:state)))")
    List<DonorProfile> findMatchingDonors(
            @org.springframework.data.repository.query.Param("bloodGroup") BloodGroup bloodGroup,
            @org.springframework.data.repository.query.Param("city") String city,
            @org.springframework.data.repository.query.Param("state") String state);
}
