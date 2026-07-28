package com.bloodbridge.repository;

import com.bloodbridge.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Hospital} entity.
 * Provides database operations for managing hospitals.
 */
@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    /**
     * Find a hospital profile by the associated user's ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the hospital if found, or empty otherwise
     */
    Optional<Hospital> findByUserId(Long userId);

    /**
     * Check if a hospital profile already exists for the specified user ID.
     *
     * @param userId the ID of the user
     * @return true if a profile exists, false otherwise
     */
    boolean existsByUserId(Long userId);

    /**
     * Check if a hospital is already registered with the specified registration number.
     *
     * @param registrationNumber the registration number to check
     * @return true if the registration number exists, false otherwise
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Find hospitals located in a specific city.
     *
     * @param city the city name
     * @return a list of hospitals
     */
    List<Hospital> findByCity(String city);

    /**
     * Find hospitals located in a specific city, ignoring case sensitivity.
     *
     * @param city the city name
     * @return a list of hospitals
     */
    List<Hospital> findByCityIgnoreCase(String city);

    /**
     * Find all hospitals that have been verified by administrators.
     *
     * @return a list of verified hospitals
     */
    List<Hospital> findByVerifiedTrue();

    /**
     * Finds top hospitals ordered by completed donations.
     * Includes subqueries counting requests and completed donations.
     *
     * @param pageable page settings to limit results (e.g. top 10)
     * @return list of top hospital projections
     */
    @org.springframework.data.jpa.repository.Query("SELECT h.hospitalName, " +
           "(SELECT COUNT(r) FROM BloodRequest r WHERE r.hospital.id = h.id), " +
           "(SELECT COUNT(d) FROM Donation d WHERE d.hospital.id = h.id AND d.status = 'COMPLETED') as donationCount " +
           "FROM Hospital h " +
           "ORDER BY donationCount DESC")
    List<Object[]> findTopHospitals(org.springframework.data.domain.Pageable pageable);
}
