package com.bloodbridge.service.impl;

import com.bloodbridge.dto.DonationResponse;
import com.bloodbridge.dto.DonationStatisticsResponse;
import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.dto.DonationStatusUpdateRequest;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.*;
import com.bloodbridge.event.DonationAcceptedEvent;
import com.bloodbridge.event.DonationCompletedEvent;
import com.bloodbridge.event.DonationConfirmedEvent;
import com.bloodbridge.mapper.DonationMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.CertificateService;
import com.bloodbridge.service.DonationService;
import com.bloodbridge.service.EmailService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.RealtimeService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for managing donation transactions.
 * Integrates instant real-time STOMP WebSocket broadcasting.
 */
@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationServiceImpl.class);

    private final DonationRepository donationRepository;
    private final MatchResultRepository matchResultRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;
    private final DonationMapper donationMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final CertificateService certificateService;
    private final RealtimeService realtimeService;
    private final MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private final AuditLoggerService auditLoggerService;
    private final com.bloodbridge.config.MatchingConfig matchingConfig;

    @Override
    @Transactional
    public DonationResponse acceptDonation(Long matchId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match result not found for ID: " + matchId));

        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile must be registered to accept request"));

        if (!matchResult.getDonor().getId().equals(donor.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to accept this match request");
        }

        List<Donation> existing = donationRepository.findByDonorIdAndBloodRequestIdAndStatusIn(
                donor.getId(), matchResult.getBloodRequest().getId(),
                List.of(DonationStatus.PENDING, DonationStatus.ACCEPTED, DonationStatus.CONFIRMED, DonationStatus.COMPLETED));
        if (!existing.isEmpty()) {
            throw new DuplicateDonationException("A donation record has already been accepted for this match result");
        }

        matchResult.setStatus(MatchStatus.ACCEPTED);
        matchResultRepository.save(matchResult);

        Donation donation = Donation.builder()
                .donor(donor)
                .bloodRequest(matchResult.getBloodRequest())
                .hospital(matchResult.getBloodRequest().getHospital())
                .unitsDonated(matchResult.getBloodRequest().getUnitsRequired())
                .status(DonationStatus.ACCEPTED)
                .build();

        Donation savedDonation = donationRepository.save(donation);

        eventPublisher.publishEvent(new DonationAcceptedEvent(this, savedDonation));

        if (savedDonation.getHospital() != null) {
            notificationService.createDonorAcceptedNotification(donor, savedDonation.getHospital(), savedDonation.getBloodRequest());
        }

        // Publish WebSocket STOMP Events
        try {
            DonationResponse response = donationMapper.toResponse(savedDonation);
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.DONOR_ACCEPTED,
                    "DONATION",
                    savedDonation.getId(),
                    "Donor Accepted Request",
                    String.format("Donor %s accepted blood request #%d", donor.getUser().getFullName(), matchResult.getBloodRequest().getId()),
                    response
            );

            realtimeService.publishAdminDashboardUpdate(event);
            if (savedDonation.getHospital() != null) {
                realtimeService.publishHospitalUpdate(savedDonation.getHospital().getId(), event);
            }
            if (savedDonation.getBloodRequest() != null && savedDonation.getBloodRequest().getPatient() != null) {
                realtimeService.publishPatientUpdate(savedDonation.getBloodRequest().getPatient().getId(), event);
            }
        } catch (Exception e) {
            log.error("Failed to publish donor acceptance STOMP event: {}", e.getMessage());
        }

        return donationMapper.toResponse(savedDonation);
    }

    @Override
    @Transactional
    public com.bloodbridge.dto.ApiResponse rejectDonation(Long matchId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match result not found for ID: " + matchId));

        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found"));

        if (!matchResult.getDonor().getId().equals(donor.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to reject this match request");
        }

        matchResult.setStatus(MatchStatus.REJECTED);
        matchResultRepository.save(matchResult);

        if (matchResult.getBloodRequest() != null && matchResult.getBloodRequest().getHospital() != null) {
            notificationService.notifyHospital(
                    matchResult.getBloodRequest().getHospital(),
                    "Donor Declined Request",
                    String.format("Donor %s declined the donation request for Blood Request #%d", donor.getUser().getFullName(), matchResult.getBloodRequest().getId()),
                    NotificationType.DONOR_DECLINED,
                    "/hospital/requests",
                    matchResult.getBloodRequest(),
                    donor
            );
        }

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.DONOR_DECLINED,
                    "MATCH_RESULT",
                    matchId,
                    "Donor Declined Request",
                    String.format("Donor %s declined request #%d", donor.getUser().getFullName(), matchResult.getBloodRequest().getId()),
                    matchResult
            );

            realtimeService.publishAdminDashboardUpdate(event);
            if (matchResult.getBloodRequest() != null && matchResult.getBloodRequest().getHospital() != null) {
                realtimeService.publishHospitalUpdate(matchResult.getBloodRequest().getHospital().getId(), event);
            }
        } catch (Exception e) {
            log.error("Failed to publish donor decline STOMP event: {}", e.getMessage());
        }

        return com.bloodbridge.dto.ApiResponse.builder()
                .message("Match request declined successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + id));

        User user = getAuthenticatedUser();
        if (user.getRole() == Role.DONOR) {
            DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found"));
            if (!donation.getDonor().getId().equals(donor.getId())) {
                throw new UnauthorizedDonationAccessException("You are not authorized to view this donation record");
            }
        } else if (user.getRole() == Role.HOSPITAL) {
            Hospital hospital = hospitalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found"));
            if (!donation.getHospital().getId().equals(hospital.getId())) {
                throw new UnauthorizedDonationAccessException("You are not authorized to view this donation record");
            }
        }

        return donationMapper.toResponse(donation);
    }

    @Override
    @Transactional
    public DonationResponse confirmDonation(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + id));

        User user = getAuthenticatedUser();
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        if (!donation.getHospital().getId().equals(hospital.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to confirm donations assigned to another hospital");
        }

        if (donation.getStatus() != DonationStatus.ACCEPTED) {
            throw new InvalidDonationStateException("Donation must be in ACCEPTED status. Current status: " + donation.getStatus());
        }

        donation.setStatus(DonationStatus.CONFIRMED);
        Donation savedDonation = donationRepository.save(donation);

        eventPublisher.publishEvent(new DonationConfirmedEvent(this, savedDonation));

        try {
            DonationResponse response = donationMapper.toResponse(savedDonation);
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.DONATION_CONFIRMED,
                    "DONATION",
                    savedDonation.getId(),
                    "Donation Confirmed",
                    "Donation confirmed at " + hospital.getHospitalName(),
                    response
            );
            realtimeService.publishHospitalUpdate(hospital.getId(), event);
            if (savedDonation.getDonor() != null) {
                realtimeService.publishDonorUpdate(savedDonation.getDonor().getId(), event);
            }
        } catch (Exception e) {
            log.error("Failed to publish donation confirmation STOMP event: {}", e.getMessage());
        }

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

        if (!donation.getHospital().getId().equals(hospital.getId())) {
            throw new UnauthorizedDonationAccessException("You are not authorized to complete donations assigned to another hospital");
        }

        // Prevent duplicate completion (Requirement 9: Return HTTP 409 Conflict if already completed)
        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new com.bloodbridge.exception.DuplicateDonationException("Donation record #" + id + " has already been completed.");
        }

        if (donation.getStatus() != DonationStatus.CONFIRMED && donation.getStatus() != DonationStatus.ACCEPTED) {
            throw new InvalidDonationStateException("Donation must be in CONFIRMED or ACCEPTED status to be completed. Current status: " + donation.getStatus());
        }

        DonationStatus previousStatus = donation.getStatus();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        donation.setStatus(DonationStatus.COMPLETED);
        donation.setCompletedAt(now);
        donation.setDonationDate(now.toLocalDate());
        if (request != null && request.getUnitsDonated() != null && request.getUnitsDonated() > 0) {
            donation.setUnitsDonated(request.getUnitsDonated());
        }
        if (request != null && request.getRemarks() != null) {
            donation.setRemarks(request.getRemarks());
        }

        if (donation.getCertificateId() == null || donation.getCertificateId().isBlank()) {
            donation.setCertificateId("CERT-BB-" + LocalDate.now().getYear() + "-" + String.format("%06d", donation.getId()));
        }

        Donation savedDonation = donationRepository.save(donation);

        // Update Donor Profile Statistics
        updateDonorStatistics(donation.getDonor(), donation.getDonationDate());

        // Update Blood Request Status if units completed
        BloodRequest bloodRequest = donation.getBloodRequest();
        if (bloodRequest != null) {
            List<Donation> requestDonations = donationRepository.findByBloodRequestId(bloodRequest.getId());
            int totalCompletedUnits = requestDonations.stream()
                    .filter(d -> d.getStatus() == DonationStatus.COMPLETED)
                    .mapToInt(Donation::getUnitsDonated)
                    .sum();

            if (totalCompletedUnits >= bloodRequest.getUnitsRequired()) {
                bloodRequest.setStatus(RequestStatus.COMPLETED);
                bloodRequestRepository.save(bloodRequest);
            }
        }

        // Audit Trail
        String donorName = savedDonation.getDonor() != null && savedDonation.getDonor().getUser() != null 
                ? savedDonation.getDonor().getUser().getFullName() : "Donor #" + (savedDonation.getDonor() != null ? savedDonation.getDonor().getId() : "N/A");
        auditLoggerService.logEvent("DONATION_COMPLETED", user.getEmail(),
                String.format("Donation ID #%d status updated from %s to COMPLETED for %s at hospital %s",
                        savedDonation.getId(), previousStatus, donorName, hospital.getHospitalName()));

        eventPublisher.publishEvent(new DonationCompletedEvent(this, savedDonation));

        // Dispatch Donation Certificate PDF via Email
        if (savedDonation.getDonor() != null && savedDonation.getDonor().getUser() != null && emailService != null && certificateService != null) {
            try {
                String donorEmail = savedDonation.getDonor().getUser().getEmail();
                if (donorEmail != null && !donorEmail.isBlank()) {
                    byte[] pdfBytes = certificateService.generateCertificatePdf(savedDonation);
                    String donorFullName = savedDonation.getDonor().getUser().getFullName();
                    String hospName = savedDonation.getHospital() != null ? savedDonation.getHospital().getHospitalName() : "Partner Hospital";
                    String bgStr = savedDonation.getDonor().getBloodGroup() != null ? savedDonation.getDonor().getBloodGroup().name() : "N/A";
                    String donDateStr = savedDonation.getDonationDate() != null ? savedDonation.getDonationDate().toString() : LocalDate.now().toString();
                    emailService.sendDonationCertificateEmail(
                            donorEmail, donorFullName, hospName, bgStr, savedDonation.getUnitsDonated(), donDateStr, savedDonation.getCertificateId(), pdfBytes
                    );
                }
            } catch (Exception e) {
                log.error("[EMAIL-CERTIFICATE-ERROR] Non-blocking error sending certificate email for Donation #{}: {}", savedDonation.getId(), e.getMessage());
            }
        }

        // Realtime STOMP Broadcasting (Requirement 7)
        try {
            DonationResponse response = donationMapper.toResponse(savedDonation);
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.DONATION_COMPLETED,
                    "DONATION",
                    savedDonation.getId(),
                    "Donation Completed!",
                    "Your blood donation has been successfully verified by " + hospital.getHospitalName() + ". Thank you for saving a life.",
                    response
            );

            realtimeService.publishAdminDashboardUpdate(event);
            realtimeService.publishHospitalUpdate(hospital.getId(), event);
            if (savedDonation.getDonor() != null) {
                realtimeService.publishDonorUpdate(savedDonation.getDonor().getId(), event);
            }
            if (bloodRequest != null && bloodRequest.getPatient() != null) {
                realtimeService.publishPatientUpdate(bloodRequest.getPatient().getId(), event);
            }
        } catch (Exception e) {
            log.error("Failed to publish donation completion STOMP event: {}", e.getMessage());
        }

        // Automated In-App Notifications (Requirement 6)
        try {
            String hospitalName = hospital.getHospitalName();
            Long requestId = bloodRequest != null ? bloodRequest.getId() : savedDonation.getId();

            if (savedDonation.getDonor() != null) {
                notificationService.notifyDonor(
                        savedDonation.getDonor(),
                        "Blood Donation Verified!",
                        String.format("Your blood donation has been successfully verified by %s. Thank you for saving a life.", hospitalName),
                        NotificationType.DONATION_COMPLETED,
                        "/donor/history",
                        bloodRequest,
                        hospital
                );
            }

            notificationService.notifyAdmin(
                    "Donation Completed",
                    String.format("Donation completed by %s at %s.", donorName, hospitalName),
                    NotificationType.DONATION_COMPLETED,
                    "/admin/dashboard"
            );

            notificationService.notifyHospital(
                    hospital,
                    "Donation Verification Recorded",
                    String.format("Donation for Request #%d by %s completed successfully.", requestId, donorName),
                    NotificationType.DONATION_COMPLETED,
                    "/hospital/donations",
                    bloodRequest,
                    savedDonation.getDonor()
            );
        } catch (Exception e) {
            log.error("Error creating donation completion notifications: {}", e.getMessage());
        }

        return donationMapper.toResponse(savedDonation);
    }

    @Override
    @Transactional
    public DonationResponse cancelDonation(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + id));

        User user = getAuthenticatedUser();
        if (user.getRole() == Role.DONOR) {
            DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found"));
            if (!donation.getDonor().getId().equals(donor.getId())) {
                throw new UnauthorizedDonationAccessException("You are not authorized to cancel this donation");
            }
        } else if (user.getRole() == Role.HOSPITAL) {
            Hospital hospital = hospitalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found"));
            if (!donation.getHospital().getId().equals(hospital.getId())) {
                throw new UnauthorizedDonationAccessException("You are not authorized to cancel this donation");
            }
        }

        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new InvalidDonationStateException("Completed donations cannot be cancelled");
        }

        donation.setStatus(DonationStatus.CANCELLED);
        Donation savedDonation = donationRepository.save(donation);

        try {
            DonationResponse response = donationMapper.toResponse(savedDonation);
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.DONATION_CANCELLED,
                    "DONATION",
                    savedDonation.getId(),
                    "Donation Cancelled",
                    "Donation #" + savedDonation.getId() + " was cancelled.",
                    response
            );

            if (savedDonation.getHospital() != null) {
                realtimeService.publishHospitalUpdate(savedDonation.getHospital().getId(), event);
            }
            if (savedDonation.getDonor() != null) {
                realtimeService.publishDonorUpdate(savedDonation.getDonor().getId(), event);
            }
        } catch (Exception e) {
            log.error("Failed to publish donation cancellation STOMP event: {}", e.getMessage());
        }

        return donationMapper.toResponse(savedDonation);
    }

    @Override
    @Transactional
    public List<DonationSummaryResponse> getDonationsByDonor(Long donorId) {
        donorProfileRepository.findById(donorId).ifPresent(this::syncCompletedEmergencyDonations);
        List<Donation> donations = donationRepository.findByDonorId(donorId);
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationSummaryResponse> getDonationsByPatient(Long patientId) {
        List<Donation> donations = donationRepository.findByPatientId(patientId);
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationSummaryResponse> getDonationsByHospital(Long hospitalId) {
        List<Donation> donations = donationRepository.findByHospitalId(hospitalId);
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationSummaryResponse> getDonationHistory() {
        List<Donation> donations = donationRepository.findAll();
        return mapToSummaryResponses(donations);
    }

    @Transactional
    public List<DonationSummaryResponse> getMyDonations() {
        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + user.getEmail()));

        syncCompletedEmergencyDonations(donor);

        List<Donation> donations = donationRepository.findByDonorId(donor.getId());
        return mapToSummaryResponses(donations);
    }

    @Override
    @Transactional(readOnly = true)
    public DonationStatisticsResponse getDonationStatistics() {
        long totalDonations = donationRepository.countByStatus(DonationStatus.COMPLETED);
        long pendingDonations = donationRepository.countByStatus(DonationStatus.PENDING) +
                donationRepository.countByStatus(DonationStatus.ACCEPTED) +
                donationRepository.countByStatus(DonationStatus.CONFIRMED);
        long cancelledDonations = donationRepository.countByStatus(DonationStatus.CANCELLED);

        List<Donation> completedDonations = donationRepository.findByStatus(DonationStatus.COMPLETED);

        Map<String, Long> bloodGroupBreakdown = completedDonations.stream()
                .filter(d -> d.getDonor() != null && d.getDonor().getBloodGroup() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getDonor().getBloodGroup().name(),
                        Collectors.counting()
                ));

        return DonationStatisticsResponse.builder()
                .totalDonations(totalDonations)
                .pendingDonations(pendingDonations)
                .completedDonations(totalDonations)
                .cancelledDonations(cancelledDonations)
                .donationsByBloodGroup(bloodGroupBreakdown)
                .donationCompletionRate(totalDonations > 0 ? (double) totalDonations / (totalDonations + pendingDonations + cancelledDonations) * 100 : 0.0)
                .build();
    }

    @Override
    @Transactional
    public void updateDonorStatistics(DonorProfile donor, LocalDate donationDate) {
        if (donor == null) return;

        int total = donor.getTotalDonations() != null ? donor.getTotalDonations() + 1 : 1;
        int lives = donor.getLivesSaved() != null ? donor.getLivesSaved() + 3 : 3;

        donor.setTotalDonations(total);
        donor.setLivesSaved(lives);
        donor.setLastDonationDate(donationDate);

        if (donationDate != null) {
            int cooldownDays = matchingConfig != null ? matchingConfig.getCooldownDays() : 90;
            donor.setNextEligibleDate(donationDate.plusDays(cooldownDays));
            donor.setIsAvailableForDonation(false);
        }

        DonorProfile saved = donorProfileRepository.save(donor);

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.ELIGIBILITY_CHANGED,
                    "DONOR",
                    saved.getId(),
                    "Eligibility Status Changed",
                    "Donor eligibility status updated.",
                    saved
            );
            realtimeService.publishDonorUpdate(saved.getId(), event);
        } catch (Exception e) {
            log.error("Failed to publish donor eligibility STOMP event: {}", e.getMessage());
        }
    }

    private void syncCompletedEmergencyDonations(DonorProfile donor) {
        User user = donor != null ? donor.getUser() : null;
        syncCompletedEmergencyDonations(donor, user);
    }

    private void syncCompletedEmergencyDonations(DonorProfile donor, User user) {
        if (matchedEmergencyDonorRepository == null) return;
        List<MatchedEmergencyDonor> matchedList = donor != null 
                ? matchedEmergencyDonorRepository.findByDonorIdOrderByCreatedAtDesc(donor.getId())
                : java.util.Collections.emptyList();
        if (matchedList.isEmpty() && user != null && donorProfileRepository != null) {
            DonorProfile emailDonor = donorProfileRepository.findByEmail(user.getEmail()).orElse(null);
            if (emailDonor != null) {
                matchedList = matchedEmergencyDonorRepository.findByDonorIdOrderByCreatedAtDesc(emailDonor.getId());
                if (donor == null) donor = emailDonor;
            }
        }
        if (donor == null) return;

        for (MatchedEmergencyDonor med : matchedList) {
            BloodRequest req = med.getBloodRequest();
            if (req != null) {
                boolean isCompleted = med.getStatus() == MatchedEmergencyDonorStatus.COMPLETED
                        || "COMPLETED".equalsIgnoreCase(med.getFulfillmentStatus())
                        || req.getStatus() == com.bloodbridge.enums.RequestStatus.COMPLETED;
                if (isCompleted) {
                    List<Donation> existing = donationRepository.findByDonorIdAndBloodRequestIdAndStatusIn(
                            donor.getId(), req.getId(), List.of(DonationStatus.COMPLETED)
                    );
                    if (existing.isEmpty()) {
                        java.time.LocalDateTime completionTime = med.getCompletedAt() != null ? med.getCompletedAt() : java.time.LocalDateTime.now();
                        Donation newDonation = Donation.builder()
                                .donor(donor)
                                .patient(req.getPatient())
                                .hospital(req.getHospital())
                                .bloodRequest(req)
                                .donationDate(completionTime.toLocalDate())
                                .completedAt(completionTime)
                                .unitsDonated(req.getUnitsRequired() != null ? req.getUnitsRequired() : 1)
                                .status(DonationStatus.COMPLETED)
                                .remarks("Emergency blood donation completed for Request #" + req.getId())
                                .build();
                        newDonation = donationRepository.save(newDonation);
                        newDonation.setCertificateId("CERT-BB-" + completionTime.getYear() + "-" + String.format("%06d", newDonation.getId()));
                        donationRepository.save(newDonation);
                        log.info("[EMERGENCY-DONATION-SYNC] Auto-synced completed emergency donation #{} for donor #{}", newDonation.getId(), donor.getId());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public List<DonationSummaryResponse> getMyDonations(String userEmail) {
        log.info("Fetching donation history for authenticated donor email: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + userEmail));

        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> donorProfileRepository.findByEmail(userEmail).orElse(null));

        if (donor != null && donor.getUser() == null) {
            donor.setUser(user);
            donorProfileRepository.save(donor);
        }

        syncCompletedEmergencyDonations(donor, user);

        Long donorId = donor != null ? donor.getId() : null;
        List<Donation> donations = donationRepository.findDonationsForDonorUser(donorId, user.getId(), userEmail);

        return mapToSummaryResponses(donations);
    }

    private List<DonationSummaryResponse> mapToSummaryResponses(List<Donation> donations) {
        return donations.stream()
                .map(donationMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found for email: " + email));
    }
}
