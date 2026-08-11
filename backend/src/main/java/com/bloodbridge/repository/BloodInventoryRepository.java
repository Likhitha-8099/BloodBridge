package com.bloodbridge.repository;

import com.bloodbridge.entity.BloodInventory;
import com.bloodbridge.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link BloodInventory} entity.
 */
@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {

    /**
     * Finds blood inventory entries for a specific hospital ID.
     *
     * @param hospitalId hospital ID
     * @return list of blood inventory records
     */
    List<BloodInventory> findByHospitalId(Long hospitalId);

    /**
     * Finds a specific blood group inventory record for a hospital.
     *
     * @param hospitalId hospital ID
     * @param bloodGroup blood group
     * @return Optional of BloodInventory
     */
    Optional<BloodInventory> findByHospitalIdAndBloodGroup(Long hospitalId, BloodGroup bloodGroup);
}
