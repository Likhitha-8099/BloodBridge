package com.bloodbridge.repository;

import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.enums.MatchedEmergencyDonorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchedEmergencyDonorRepository extends JpaRepository<MatchedEmergencyDonor, Long> {

    Optional<MatchedEmergencyDonor> findByBloodRequestIdAndDonorId(Long bloodRequestId, Long donorId);

    List<MatchedEmergencyDonor> findByDonorIdAndStatusInOrderByCreatedAtDesc(Long donorId, List<MatchedEmergencyDonorStatus> statuses);

    List<MatchedEmergencyDonor> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    List<MatchedEmergencyDonor> findByBloodRequestId(Long bloodRequestId);

    @Query("SELECT med FROM MatchedEmergencyDonor med " +
           "JOIN FETCH med.bloodRequest br " +
           "LEFT JOIN FETCH med.hospital h " +
           "WHERE med.donor.id = :donorId " +
           "AND med.status IN :statuses " +
           "ORDER BY med.createdAt DESC")
    List<MatchedEmergencyDonor> findAssignedRequestsForDonor(
            @Param("donorId") Long donorId,
            @Param("statuses") List<MatchedEmergencyDonorStatus> statuses);

    @Query("SELECT med FROM MatchedEmergencyDonor med " +
           "JOIN FETCH med.donor d " +
           "LEFT JOIN FETCH d.user u " +
           "WHERE med.bloodRequest.id = :bloodRequestId " +
           "ORDER BY med.createdAt DESC")
    List<MatchedEmergencyDonor> findByBloodRequestIdWithDonorDetails(@Param("bloodRequestId") Long bloodRequestId);

    long countByBloodRequestIdAndStatus(Long bloodRequestId, MatchedEmergencyDonorStatus status);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MatchedEmergencyDonor med WHERE med.donor.id = :donorId")
    void deleteAllByDonorId(@Param("donorId") Long donorId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MatchedEmergencyDonor med WHERE med.hospital.id = :hospitalId")
    void deleteAllByHospitalId(@Param("hospitalId") Long hospitalId);
}
