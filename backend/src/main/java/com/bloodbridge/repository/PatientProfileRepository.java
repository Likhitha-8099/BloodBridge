package com.bloodbridge.repository;

import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link PatientProfile} entity.
 * Provides database operations for managing patient profiles.
 */
@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    /**
     * Find a patient profile by the associated user's ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the profile if found, or empty otherwise
     */
    Optional<PatientProfile> findByUserId(Long userId);

    /**
     * Check if a patient profile already exists for the specified user ID.
     *
     * @param userId the ID of the user
     * @return true if a profile exists, false otherwise
     */
    boolean existsByUserId(Long userId);

    /**
     * Find patient profiles matching a specific blood group.
     *
     * @param bloodGroup the blood group enum to query
     * @return a list of matching patient profiles
     */
    List<PatientProfile> findByBloodGroup(BloodGroup bloodGroup);

    /**
     * Find patient profiles by city name.
     *
     * @param city the city name
     * @return a list of patient profiles in that city
     */
    List<PatientProfile> findByCity(String city);

    /**
     * Find patient profiles by city name, ignoring case sensitivity.
     *
     * @param city the city name
     * @return a list of patient profiles in that city
     */
    List<PatientProfile> findByCityIgnoreCase(String city);
}
