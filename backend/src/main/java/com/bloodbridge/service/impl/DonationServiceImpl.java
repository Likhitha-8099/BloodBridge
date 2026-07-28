package com.bloodbridge.service.impl;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.DonationResponse;
import com.bloodbridge.dto.DonationStatisticsResponse;
import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.dto.DonationStatusUpdateRequest;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.*;
import com.bloodbridge.event.DonationAcceptedEvent;
import com.bloodbridge.event.DonationCompletedEvent;
import com.bloodbridge.event.DonationConfirmedEvent;
import com.bloodbridge.mapper.DonationMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for managing donation transactions.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final MatchResultRepository matchResultRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;
    private final DonationMapper donationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DonationResponse acceptDonation(Long matchId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match result not found for ID: " + matchId));

        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile must be registered to accept request"));

        // Verify own match check
        if (!matchResult.getDonor().getId().equals(donor.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to accept this match request");
        }

        // Verify status
        if (matchResult.getStatus() != MatchStatus.MATCHED) {
            throw new InvalidDonationStateException("Match request is not in MATCHED status. Current status: " + matchResult.getStatus());
        }

        // Check duplicate acceptances
        boolean alreadyExists = donationRepository.existsByMatchResultIdAndStatusIn(matchId,
                List.of(DonationStatus.PENDING, DonationStatus.ACCEPTED, DonationStatus.CONFIRMED, DonationStatus.COMPLETED));
        if (alreadyExists) {
            throw new DuplicateDonationException("A donation record has already been accepted for this match result");
        }

        // Update Match status
        matchResult.setStatus(MatchStatus.ACCEPTED);
        matchResultRepository.save(matchResult);

        // Create Donation
        Donation donation = Donation.builder()
                .donor(matchResult.getDonor())
                .patient(matchResult.getBloodRequest().getPatient())
                .bloodRequest(matchResult.getBloodRequest())
                .hospital(matchResult.getBloodRequest().getHospital())
                .matchResult(matchResult)
                .status(DonationStatus.ACCEPTED)
                .build();

        Donation savedDonation = donationRepository.save(donation);
        eventPublisher.publishEvent(new DonationAcceptedEvent(this, savedDonation));
        return donationMapper.toResponse(savedDonation);
    }

    @Override
    @Transactional
    public ApiResponse rejectDonation(Long matchId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match result not found for ID: " + matchId));

        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile must be registered to reject request"));

        // Verify own match check
        if (!matchResult.getDonor().getId().equals(donor.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to reject this match request");
        }

        if (matchResult.getStatus() != MatchStatus.MATCHED) {
            throw new InvalidDonationStateException("Match request is not in MATCHED status. Current status: " + matchResult.getStatus());
        }

        matchResult.setStatus(MatchStatus.REJECTED);
        matchResultRepository.save(matchResult);

        return ApiResponse.builder()
                .message("Match request rejected successfully")
                .build();
    }

    @Override
    @Transactional
    public DonationResponse confirmDonation(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + id));

        User user = getAuthenticatedUser();
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        // Verify assigned hospital check
        if (!donation.getHospital().getId().equals(hospital.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to confirm donations assigned to another hospital");
        }

        if (donation.getStatus() != DonationStatus.ACCEPTED) {
            throw new InvalidDonationStateException("Donation must be in ACCEPTED status. Current status: " + donation.getStatus());
        }

        donation.setStatus(DonationStatus.CONFIRMED);
        Donation savedDonation = donationRepository.save(donation);

        eventPublisher.publishEvent(new DonationConfirmedEvent(this, savedDonation));

        return donationMapper.toResponse(savedDonation);
    }

    @Override
    @Transactional
    public DonationResponse completeDonation(Long id, DonationStatusUpdateRequest request) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + id));

        User user = getAuthenticatedUser();
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        // Verify assigned hospital check
        if (!donation.getHospital().getId().equals(hospital.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to complete donations assigned to another hospital");
        }

        if (donation.getStatus() != DonationStatus.CONFIRMED) {
            throw new InvalidDonationStateException("Donation must be in CONFIRMED status to be completed. Current status: " + donation.getStatus());
        }

        donation.setStatus(DonationStatus.COMPLETED);
        donation.setDonationDate(LocalDate.now());
        donation.setUnitsDonated(request.getUnitsDonated());
        donation.setRemarks(request.getRemarks());

        Donation savedDonation = donationRepository.save(donation);

        // Update donor stats
        updateDonorStatistics(donation.getDonor(), donation.getDonationDate());

        // Update blood request status
        BloodRequest bloodRequest = donation.getBloodRequest();
        List<Donation> requestDonations = donationRepository.findByBloodRequestId(bloodRequest.getId());
        int totalCompletedUnits = requestDonations.stream()
                .filter(d -> d.getStatus() == DonationStatus.COMPLETED)
                .mapToInt(Donation::getUnitsDonated)
                .sum();

        if (totalCompletedUnits >= bloodRequest.getUnitsRequired()) {
            bloodRequest.setStatus(RequestStatus.COMPLETED);
            bloodRequestRepository.save(bloodRequest);
        }

        eventPublisher.publishEvent(new DonationCompletedEvent(this, savedDonation));

        return donationMapper.toResponse(savedDonation);
    }

    @Override
    @Transactional
    public DonationResponse cancelDonation(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + id));

        User user = getAuthenticatedUser();

        // Donor or Hospital linked can cancel
        boolean isOwnerDonor = false;
        if (user.getRole() == Role.DONOR) {
            DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found"));
            isOwnerDonor = donation.getDonor().getId().equals(donor.getId());
        }

        boolean isAssignedHospital = false;
        if (user.getRole() == Role.HOSPITAL) {
            Hospital hospital = hospitalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found"));
            isAssignedHospital = donation.getHospital().getId().equals(hospital.getId());
        }

        if (!isOwnerDonor && !isAssignedHospital && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedDonationAccessException("You are not authorized to cancel this donation");
        }

        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new InvalidDonationStateException("Completed donations cannot be cancelled");
        }

        donation.setStatus(DonationStatus.CANCELLED);
        Donation savedDonation = donationRepository.save(donation);
        return donationMapper.toResponse(savedDonation);
    }

    @Override
    @Transactional(readOnly = true)
    public DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + id));

        User user = getAuthenticatedUser();

        // Security check
        if (user.getRole() == Role.DONOR) {
            DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found"));
            if (!donation.getDonor().getId().equals(donor.getId())) {
                throw new AccessDeniedException("Unauthorized to view this donation");
            }
        } else if (user.getRole() == Role.PATIENT) {
            PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found"));
            if (!donation.getPatient().getId().equals(patient.getId())) {
                throw new AccessDeniedException("Unauthorized to view this donation");
            }
        } else if (user.getRole() == Role.HOSPITAL) {
            Hospital hospital = hospitalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found"));
            if (!donation.getHospital().getId().equals(hospital.getId())) {
                throw new AccessDeniedException("Unauthorized to view this donation");
            }
        }

        return donationMapper.toResponse(donation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationSummaryResponse> getDonationsByDonor(Long donorId) {
        User user = getAuthenticatedUser();
        if (user.getRole() == Role.DONOR) {
            DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found"));
            if (!donor.getId().equals(donorId)) {
                throw new AccessDeniedException("Unauthorized to view matches for this donor ID");
            }
        }

        List<Donation> donations = donationRepository.findByDonorId(donorId);
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationSummaryResponse> getDonationsByPatient(Long patientId) {
        User user = getAuthenticatedUser();
        if (user.getRole() == Role.PATIENT) {
            PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found"));
            if (!patient.getId().equals(patientId)) {
                throw new AccessDeniedException("Unauthorized to view matches for this patient ID");
            }
        }

        List<Donation> donations = donationRepository.findByPatientId(patientId);
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationSummaryResponse> getDonationsByHospital(Long hospitalId) {
        User user = getAuthenticatedUser();
        if (user.getRole() == Role.HOSPITAL) {
            Hospital hospital = hospitalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found"));
            if (!hospital.getId().equals(hospitalId)) {
                throw new AccessDeniedException("Unauthorized to view matches for this hospital ID");
            }
        }

        List<Donation> donations = donationRepository.findByHospitalId(hospitalId);
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationSummaryResponse> getDonationHistory() {
        List<Donation> donations = donationRepository.findAll();
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public DonationStatisticsResponse getDonationStatistics() {
        long totalDonations = donationRepository.count();
        long completed = donationRepository.countByStatus(DonationStatus.COMPLETED);
        long pending = donationRepository.countByStatus(DonationStatus.ACCEPTED) + donationRepository.countByStatus(DonationStatus.CONFIRMED);
        long cancelled = donationRepository.countByStatus(DonationStatus.CANCELLED);

        List<Donation> completedDonationsList = donationRepository.findByStatus(DonationStatus.COMPLETED);

        Map<String, Long> donationsByBloodGroup = completedDonationsList.stream()
                .collect(Collectors.groupingBy(d -> d.getDonor().getBloodGroup().name(), Collectors.counting()));

        Map<String, Long> topDonors = completedDonationsList.stream()
                .collect(Collectors.groupingBy(d -> d.getDonor().getUser().getFullName(), Collectors.counting()));

        Map<String, Long> mostActiveHospitals = completedDonationsList.stream()
                .collect(Collectors.groupingBy(d -> d.getHospital().getHospitalName(), Collectors.counting()));

        Map<String, Long> monthlyDonationTrends = completedDonationsList.stream()
                .filter(d -> d.getDonationDate() != null)
                .collect(Collectors.groupingBy(d -> {
                    LocalDate date = d.getDonationDate();
                    return String.format("%04d-%02d", date.getYear(), date.getMonthValue());
                }, Collectors.counting()));

        return DonationStatisticsResponse.builder()
                .totalDonations(totalDonations)
                .completedDonations(completed)
                .pendingDonations(pending)
                .cancelledDonations(cancelled)
                .donationsByBloodGroup(donationsByBloodGroup)
                .topDonors(topDonors)
                .mostActiveHospitals(mostActiveHospitals)
                .monthlyDonationTrends(monthlyDonationTrends)
                .build();
    }

    @Override
    @Transactional
    public void updateDonorStatistics(DonorProfile donor, LocalDate donationDate) {
        donor.setAvailableForDonation(false);
        donor.setTotalDonations(donor.getTotalDonations() != null ? donor.getTotalDonations() + 1 : 1);
        donor.setLastDonationDate(donationDate);
        donorProfileRepository.save(donor);
    }

    private List<DonationSummaryResponse> mapToSummaryResponses(List<Donation> donations) {
        return donations.stream()
                .map(donationMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for authenticated email: " + email));
    }
}
