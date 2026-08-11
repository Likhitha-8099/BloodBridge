package com.bloodbridge.service.impl;

import com.bloodbridge.dto.BloodRequestCreateRequest;
import com.bloodbridge.dto.BloodRequestSummaryResponse;
import com.bloodbridge.dto.BloodRequestUpdateRequest;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.dto.RequestStatusResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.event.BloodRequestCreatedEvent;
import com.bloodbridge.event.RequestRejectedEvent;
import com.bloodbridge.event.RequestVerifiedEvent;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.exception.HospitalNotFoundException;
import com.bloodbridge.exception.InvalidRequestStateException;
import com.bloodbridge.exception.PatientProfileNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.BloodRequestMapper;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.dto.response.DonorEmergencyRequestDTO;
import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.enums.MatchedEmergencyDonorStatus;
import com.bloodbridge.repository.MatchedEmergencyDonorRepository;
import com.bloodbridge.service.BloodRequestService;
import com.bloodbridge.service.EmailService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.RealtimeService;
import com.bloodbridge.service.SmartDonorMatchingPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing blood donation requests.
 * Integrates real-time STOMP WebSocket broadcasting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BloodRequestServiceImpl implements BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final NotificationService notificationService;
    private final BloodRequestMapper bloodRequestMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RealtimeService realtimeService;
    private final SmartDonorMatchingPipelineService smartDonorMatchingPipelineService;
    private final MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private final EmailService emailService;

    private void broadcastRequestEvent(RealtimeEventType eventType, BloodRequest request, String title, String message) {
        if (request == null) return;
        try {
            BloodRequestResponse response = bloodRequestMapper.toResponse(request);
            Hospital hosp = request.getHospital();
            
            RealtimeEventDTO event = RealtimeEventDTO.builder()
                    .eventType(eventType)
                    .requestId(request.getId())
                    .hospitalId(hosp != null ? hosp.getId() : null)
                    .hospitalName(hosp != null ? hosp.getHospitalName() : null)
                    .bloodGroup(request.getBloodGroupNeeded() != null ? request.getBloodGroupNeeded().name() : null)
                    .status(request.getStatus() != null ? request.getStatus().name() : null)
                    .entityType("BLOOD_REQUEST")
                    .entityId(request.getId())
                    .title(title)
                    .message(message)
                    .payload(response)
                    .timestamp(LocalDateTime.now())
                    .build();

            realtimeService.publishEmergencyEvent(event);
            realtimeService.publishAdminDashboardUpdate(event);
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to broadcast real-time STOMP blood request event: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public BloodRequestResponse createRequest(BloodRequestCreateRequest request) {
        User user = getAuthenticatedUser();

        PatientProfile patient = null;
        if (user.getRole() == Role.PATIENT) {
            patient = patientProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile must be registered before creating a blood request"));
        }

        Hospital hospital;
        if (user.getRole() == Role.HOSPITAL) {
            hospital = hospitalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));
        } else {
            hospital = hospitalRepository.findById(request.getHospitalId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital not found for ID: " + request.getHospitalId()));
        }

        if (request.getUnitsRequired() <= 0) {
            throw new IllegalArgumentException("Units required must be greater than 0");
        }

        BloodRequest bloodRequest = bloodRequestMapper.toEntity(request, patient, hospital);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        eventPublisher.publishEvent(new BloodRequestCreatedEvent(this, savedRequest));

        try {
            log.info("[PIPELINE-TRIGGER] Automatically triggering Smart Donor Matching Pipeline for Blood Request #{} (Status: {})", savedRequest.getId(), savedRequest.getStatus());
            smartDonorMatchingPipelineService.executePipeline(savedRequest);
            log.info("[PIPELINE-TRIGGER-SUCCESS] Smart Donor Matching Pipeline completed execution for Blood Request #{}", savedRequest.getId());
        } catch (Exception e) {
            log.error("[PIPELINE-TRIGGER-ERROR] Failed to execute Smart Donor Matching Pipeline for request #{}: {}", savedRequest.getId(), e.getMessage(), e);
        }

        try {
            notificationService.notifyHospital(
                    hospital,
                    "New Blood Request Submitted",
                    String.format("Patient %s submitted Blood Request #%d for %s.", user.getFullName() != null ? user.getFullName() : "Patient", savedRequest.getId(), savedRequest.getBloodGroupNeeded().name()),
                    NotificationType.BLOOD_REQUEST_CREATED,
                    "/hospital/requests",
                    savedRequest,
                    null
            );
        } catch (Exception e) {
            log.error("Failed to notify hospital for request #{}: {}", savedRequest.getId(), e.getMessage());
        }

        broadcastRequestEvent(RealtimeEventType.BLOOD_REQUEST_CREATED, savedRequest, "New Blood Request Created", "Blood request #" + savedRequest.getId() + " was created.");

        return bloodRequestMapper.toResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public BloodRequestResponse getRequestById(Long id) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        return bloodRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodRequestSummaryResponse> getMyRequests() {
        User user = getAuthenticatedUser();
        PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found for user: " + user.getEmail()));

        List<BloodRequest> requests = bloodRequestRepository.findByPatientId(patient.getId());
        return mapToSummaryResponses(requests);
    }

    @Override
    @Transactional
    public BloodRequestResponse updateRequest(Long id, BloodRequestUpdateRequest request) {
        User user = getAuthenticatedUser();
        BloodRequest bloodRequest = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        if (!bloodRequest.getPatient().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this blood request");
        }

        if (bloodRequest.getStatus() == RequestStatus.COMPLETED ||
                bloodRequest.getStatus() == RequestStatus.CANCELLED ||
                bloodRequest.getStatus() == RequestStatus.REJECTED) {
            throw new InvalidRequestStateException("Request cannot be edited because it is in status: " + bloodRequest.getStatus());
        }

        if (request.getUnitsRequired() <= 0) {
            throw new IllegalArgumentException("Units required must be greater than 0");
        }

        bloodRequest.setBloodGroupNeeded(request.getBloodGroupNeeded());
        bloodRequest.setUnitsRequired(request.getUnitsRequired());
        bloodRequest.setUrgencyLevel(request.getUrgencyLevel());
        bloodRequest.setReason(request.getReason());
        bloodRequest.setRequiredByDate(request.getRequiredByDate());
        bloodRequest.setNotes(request.getNotes());

        BloodRequest updatedRequest = bloodRequestRepository.save(bloodRequest);
        broadcastRequestEvent(RealtimeEventType.BLOOD_REQUEST_UPDATED, updatedRequest, "Blood Request Updated", "Blood request #" + updatedRequest.getId() + " details were updated.");

        return bloodRequestMapper.toResponse(updatedRequest);
    }

    @Override
    @Transactional
    public BloodRequestResponse cancelRequest(Long id) {
        User user = getAuthenticatedUser();
        BloodRequest bloodRequest = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        if (!bloodRequest.getPatient().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to cancel this blood request");
        }

        if (bloodRequest.getStatus() == RequestStatus.COMPLETED) {
            throw new InvalidRequestStateException("Completed requests cannot be cancelled");
        }

        bloodRequest.setStatus(RequestStatus.CANCELLED);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        broadcastRequestEvent(RealtimeEventType.BLOOD_REQUEST_CANCELLED, savedRequest, "Blood Request Cancelled", "Blood request #" + savedRequest.getId() + " was cancelled.");

        return bloodRequestMapper.toResponse(savedRequest);
    }

    @Override
    @Transactional
    public RequestStatusResponse verifyRequest(Long id) {
        User user = getAuthenticatedUser();
        BloodRequest bloodRequest = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        if (!bloodRequest.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to verify requests assigned to another hospital");
        }

        if (bloodRequest.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only PENDING requests can be verified. Current status: " + bloodRequest.getStatus());
        }

        bloodRequest.setStatus(RequestStatus.VERIFIED);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        eventPublisher.publishEvent(new RequestVerifiedEvent(this, savedRequest));

        if (savedRequest.getPatient() != null && savedRequest.getPatient().getUser() != null) {
            notificationService.notifyPatient(
                    savedRequest.getPatient().getUser(),
                    "Blood Request Verified",
                    String.format("Your Blood Request #%d has been verified by %s.", savedRequest.getId(), hospital.getHospitalName()),
                    NotificationType.REQUEST_VERIFIED,
                    "/patient/requests",
                    savedRequest
            );
        }

        broadcastRequestEvent(RealtimeEventType.BLOOD_REQUEST_VERIFIED, savedRequest, "Blood Request Verified", "Blood request #" + savedRequest.getId() + " was verified.");

        return RequestStatusResponse.builder()
                .message("Request verified successfully")
                .status(RequestStatus.VERIFIED)
                .build();
    }

    @Override
    @Transactional
    public RequestStatusResponse rejectRequest(Long id) {
        User user = getAuthenticatedUser();
        BloodRequest bloodRequest = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        if (!bloodRequest.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to reject requests assigned to another hospital");
        }

        if (bloodRequest.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only PENDING requests can be rejected. Current status: " + bloodRequest.getStatus());
        }

        bloodRequest.setStatus(RequestStatus.REJECTED);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        eventPublisher.publishEvent(new RequestRejectedEvent(this, savedRequest));

        if (savedRequest.getPatient() != null && savedRequest.getPatient().getUser() != null) {
            notificationService.notifyPatient(
                    savedRequest.getPatient().getUser(),
                    "Blood Request Rejected",
                    String.format("Your Blood Request #%d was rejected by %s.", savedRequest.getId(), hospital.getHospitalName()),
                    NotificationType.REQUEST_REJECTED,
                    "/patient/requests",
                    savedRequest
            );
        }

        broadcastRequestEvent(RealtimeEventType.BLOOD_REQUEST_REJECTED, savedRequest, "Blood Request Rejected", "Blood request #" + savedRequest.getId() + " was rejected.");

        return RequestStatusResponse.builder()
                .message("Request rejected successfully")
                .status(RequestStatus.REJECTED)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodRequestSummaryResponse> getAllRequests() {
        List<BloodRequest> requests = bloodRequestRepository.findAll();
        return mapToSummaryResponses(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodRequestSummaryResponse> getRequestsByStatus(RequestStatus status) {
        List<BloodRequest> requests = bloodRequestRepository.findByStatus(status);
        return mapToSummaryResponses(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodRequestSummaryResponse> getRequestsByBloodGroup(BloodGroup bloodGroup) {
        List<BloodRequest> requests = bloodRequestRepository.findByBloodGroupNeeded(bloodGroup);
        return mapToSummaryResponses(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodRequestSummaryResponse> getActiveRequests() {
        List<BloodRequest> requests = bloodRequestRepository.findByStatusIn(
                List.of(RequestStatus.PENDING, RequestStatus.VERIFIED, RequestStatus.ACTIVE, RequestStatus.MATCHING, RequestStatus.IN_PROGRESS, RequestStatus.FULFILLED, RequestStatus.COMPLETED)
        );
        return mapToSummaryResponses(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodRequestSummaryResponse> getEmergencyRequestsForDonor() {
        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Donor profile not found for user: " + user.getEmail()));

        List<BloodRequest> requests = bloodRequestRepository.findAll();
        List<BloodRequest> emergencyList = new ArrayList<>();

        for (BloodRequest req : requests) {
            if (req.getStatus() == RequestStatus.CREATED || req.getStatus() == RequestStatus.PENDING || req.getStatus() == RequestStatus.VERIFIED || req.getStatus() == RequestStatus.MATCHING) {
                boolean bloodGroupMatch = (donor.getBloodGroup() == null || donor.getBloodGroup() == req.getBloodGroupNeeded() || donor.getBloodGroup() == BloodGroup.O_NEGATIVE);

                String reqCity = req.getHospital() != null && req.getHospital().getCity() != null ? req.getHospital().getCity()
                        : (req.getPatient() != null && req.getPatient().getUser() != null ? req.getPatient().getUser().getCity() : null);
                String reqState = req.getHospital() != null && req.getHospital().getState() != null ? req.getHospital().getState()
                        : (req.getPatient() != null && req.getPatient().getUser() != null ? req.getPatient().getUser().getState() : null);

                boolean cityMatch = (donor.getCity() == null || donor.getCity().isBlank() || (reqCity != null && reqCity.trim().equalsIgnoreCase(donor.getCity().trim())));
                boolean stateMatch = (donor.getState() == null || donor.getState().isBlank() || (reqState != null && reqState.trim().equalsIgnoreCase(donor.getState().trim())));

                if (bloodGroupMatch && cityMatch && stateMatch) {
                    emergencyList.add(req);
                }
            }
        }
        return mapToSummaryResponses(emergencyList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorEmergencyRequestDTO> getMatchedEmergencyRequestsForDonor() {
        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Donor profile not found for user: " + user.getEmail()));

        List<MatchedEmergencyDonor> matchedDonors = matchedEmergencyDonorRepository
                .findAssignedRequestsForDonor(donor.getId(), List.of(
                        MatchedEmergencyDonorStatus.PENDING,
                        MatchedEmergencyDonorStatus.VIEWED,
                        MatchedEmergencyDonorStatus.ACCEPTED,
                        MatchedEmergencyDonorStatus.CONFIRMED,
                        MatchedEmergencyDonorStatus.FULFILLMENT_IN_PROGRESS,
                        MatchedEmergencyDonorStatus.COMPLETED
                ));

        return matchedDonors.stream()
                .map(this::mapToDonorEmergencyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DonorEmergencyRequestDTO acceptMatchedEmergencyRequest(Long bloodRequestId) {
        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Donor profile not found for user: " + user.getEmail()));

        MatchedEmergencyDonor med = matchedEmergencyDonorRepository.findByBloodRequestIdAndDonorId(bloodRequestId, donor.getId())
                .orElseGet(() -> {
                    BloodRequest req = bloodRequestRepository.findById(bloodRequestId)
                            .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + bloodRequestId));
                    return MatchedEmergencyDonor.builder()
                            .bloodRequest(req)
                            .donor(donor)
                            .hospital(req.getHospital())
                            .distanceKm(0.0)
                            .matchingGroup("GROUP_A")
                            .notificationSent(true)
                            .status(MatchedEmergencyDonorStatus.PENDING)
                            .build();
                });

        BloodRequest req = med.getBloodRequest();
        if (req != null && !req.canAcceptDonor()) {
            throw new InvalidRequestStateException("Emergency blood request is no longer accepting responses.");
        }

        // Idempotency Check: If already accepted or confirmed, return existing match DTO
        if (med.getStatus() == MatchedEmergencyDonorStatus.ACCEPTED || med.getStatus() == MatchedEmergencyDonorStatus.CONFIRMED) {
            log.info("[DONOR-ACCEPT-IDEMPOTENT] Donor Profile #{} already accepted/confirmed Blood Request #{}", donor.getId(), bloodRequestId);
            return mapToDonorEmergencyDTO(med);
        }

        med.setStatus(MatchedEmergencyDonorStatus.ACCEPTED);
        if (med.getAcceptedAt() == null) {
            med.setAcceptedAt(LocalDateTime.now());
        }
        MatchedEmergencyDonor savedMed = matchedEmergencyDonorRepository.save(med);

        if (req != null) {
            if (req.getStatus() == RequestStatus.CREATED || req.getStatus() == RequestStatus.PENDING || req.getStatus() == RequestStatus.VERIFIED || req.getStatus() == RequestStatus.MATCHING || req.getStatus() == RequestStatus.MATCHED || req.getStatus() == RequestStatus.DONOR_NOTIFIED) {
                req.setStatus(RequestStatus.DONOR_ACCEPTED);
                bloodRequestRepository.save(req);
            }

            Hospital hosp = req.getHospital();
            String donorName = user.getFullName() != null ? user.getFullName() : "Valued Donor";
            String bgFormatted = req.getBloodGroupNeeded() != null ? 
                    req.getBloodGroupNeeded().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "Emergency";

            if (hosp != null && notificationService != null) {
                notificationService.createDonorAcceptedNotification(donor, hosp, req);
            }

            RealtimeEventDTO acceptEvent = RealtimeEventDTO.builder()
                    .eventType(RealtimeEventType.DONOR_ACCEPTED_REQUEST)
                    .requestId(req.getId())
                    .matchedDonorId(savedMed.getId())
                    .donorId(donor.getId())
                    .donorName(donorName)
                    .hospitalId(hosp != null ? hosp.getId() : null)
                    .hospitalName(hosp != null ? hosp.getHospitalName() : null)
                    .bloodGroup(req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : null)
                    .status("ACCEPTED")
                    .timestamp(LocalDateTime.now())
                    .build();

            realtimeService.publishEmergencyEvent(acceptEvent);
            broadcastRequestEvent(RealtimeEventType.DONOR_ACCEPTED, req, "Donor Accepted Blood Request",
                    donorName + " has accepted your " + bgFormatted + " blood donation request.");

            // Dispatch Async Email to Hospital Administrator (Safe / Non-blocking)
            if (hosp != null && emailService != null) {
                try {
                    String hospitalEmail = (hosp.getUser() != null && hosp.getUser().getEmail() != null && !hosp.getUser().getEmail().isBlank())
                            ? hosp.getUser().getEmail()
                            : hosp.getEmail();

                    if (hospitalEmail != null && !hospitalEmail.isBlank()) {
                        emailService.sendDonorAcceptanceEmailToHospital(
                                hospitalEmail,
                                hosp.getHospitalName(),
                                donorName,
                                bgFormatted,
                                req.getId(),
                                req.getUnitsRequired(),
                                savedMed.getDistanceKm(),
                                savedMed.getAcceptedAt() != null ? savedMed.getAcceptedAt().toString() : LocalDateTime.now().toString()
                        );
                    }
                } catch (Exception e) {
                    log.error("[EMAIL-FAILURE] Email type: ACCEPTANCE, Recipient: {}, Reason: {}", hosp.getEmail(), e.getMessage());
                }
            }
        }

        return mapToDonorEmergencyDTO(savedMed);
    }

    @Override
    @Transactional
    public DonorEmergencyRequestDTO rejectMatchedEmergencyRequest(Long bloodRequestId) {
        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Donor profile not found for user: " + user.getEmail()));

        MatchedEmergencyDonor med = matchedEmergencyDonorRepository.findByBloodRequestIdAndDonorId(bloodRequestId, donor.getId())
                .orElseGet(() -> {
                    BloodRequest req = bloodRequestRepository.findById(bloodRequestId)
                            .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + bloodRequestId));
                    return MatchedEmergencyDonor.builder()
                            .bloodRequest(req)
                            .donor(donor)
                            .hospital(req.getHospital())
                            .distanceKm(0.0)
                            .matchingGroup("GROUP_A")
                            .notificationSent(true)
                            .status(MatchedEmergencyDonorStatus.PENDING)
                            .build();
                });

        med.setStatus(MatchedEmergencyDonorStatus.REJECTED);
        med.setRejectedAt(LocalDateTime.now());
        MatchedEmergencyDonor savedMed = matchedEmergencyDonorRepository.save(med);

        BloodRequest req = savedMed.getBloodRequest();
        if (req != null && req.getHospital() != null) {
            notificationService.notifyHospital(
                    req.getHospital(),
                    "Donor Declined Request",
                    String.format("Donor %s declined donation for request #%d", user.getFullName(), bloodRequestId),
                    NotificationType.DONOR_DECLINED,
                    "/hospital/requests",
                    req,
                    donor
            );

            RealtimeEventDTO rejectEvent = RealtimeEventDTO.builder()
                    .eventType(RealtimeEventType.DONOR_REJECTED_REQUEST)
                    .requestId(req.getId())
                    .matchedDonorId(savedMed.getId())
                    .donorId(donor.getId())
                    .donorName(user.getFullName())
                    .hospitalId(req.getHospital().getId())
                    .hospitalName(req.getHospital().getHospitalName())
                    .bloodGroup(req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : null)
                    .status("REJECTED")
                    .timestamp(LocalDateTime.now())
                    .build();

            realtimeService.publishEmergencyEvent(rejectEvent);
            broadcastRequestEvent(RealtimeEventType.DONOR_DECLINED, req, "Donor Declined Request", "Donor " + user.getFullName() + " declined blood request #" + bloodRequestId);
        }

        return mapToDonorEmergencyDTO(savedMed);
    }

    private DonorEmergencyRequestDTO mapToDonorEmergencyDTO(MatchedEmergencyDonor med) {
        BloodRequest req = med.getBloodRequest();
        com.bloodbridge.entity.Hospital hosp = med.getHospital() != null ? med.getHospital() : (req != null ? req.getHospital() : null);

        double lat = hosp != null && hosp.getLatitude() != null ? hosp.getLatitude() : 0.0;
        double lon = hosp != null && hosp.getLongitude() != null ? hosp.getLongitude() : 0.0;

        String mapUrl = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", lat, lon);
        boolean isConfirmed = Boolean.TRUE.equals(med.getConfirmed()) || med.getStatus() == MatchedEmergencyDonorStatus.CONFIRMED;

        return DonorEmergencyRequestDTO.builder()
                .requestId(req != null ? req.getId() : null)
                .matchedDonorId(med.getId())
                .hospitalName(hosp != null ? hosp.getHospitalName() : "Medical Center")
                .hospitalAddress(hosp != null ? (hosp.getAddress() != null ? hosp.getAddress() : ((hosp.getCity() != null ? hosp.getCity() : "") + ", " + (hosp.getState() != null ? hosp.getState() : ""))) : "Address N/A")
                .hospitalPhone(hosp != null && hosp.getPhoneNumber() != null ? hosp.getPhoneNumber() : "")
                .bloodGroup(req != null && req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "N/A")
                .unitsRequired(req != null ? req.getUnitsRequired() : 1)
                .priority(req != null && req.getUrgencyLevel() != null ? req.getUrgencyLevel().name() : "HIGH")
                .distanceKm(med.getDistanceKm() != null ? med.getDistanceKm() : 0.0)
                .matchingGroup(med.getMatchingGroup() != null ? med.getMatchingGroup() : "GROUP_A")
                .hospitalLatitude(lat)
                .hospitalLongitude(lon)
                .createdAt(med.getCreatedAt() != null ? med.getCreatedAt() : (req != null ? req.getCreatedAt() : LocalDateTime.now()))
                .expiryTime(req != null && req.getRequiredByDate() != null ? req.getRequiredByDate().atTime(23, 59, 59) : LocalDateTime.now().plusHours(24))
                .confirmed(isConfirmed)
                .confirmedAt(med.getConfirmedAt())
                .requestStatus(req != null && req.getStatus() != null ? req.getStatus().name() : "ACTIVE")
                .fulfillmentInstructions("Please report to hospital emergency reception desk with valid government ID.")
                .status(med.getStatus() != null ? med.getStatus().name() : "PENDING")
                .googleMapsUrl(mapUrl)
                .build();
    }

    @Override
    @Transactional
    public BloodRequestSummaryResponse acceptBloodRequest(Long id) {
        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Donor profile not found for user: " + user.getEmail()));

        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        request.setStatus(RequestStatus.DONOR_ACCEPTED);
        BloodRequest saved = bloodRequestRepository.save(request);

        if (saved.getHospital() != null) {
            notificationService.createDonorAcceptedNotification(donor, saved.getHospital(), saved);
        }

        broadcastRequestEvent(RealtimeEventType.DONOR_ACCEPTED, saved, "Donor Accepted Request", "Donor " + user.getFullName() + " accepted blood request #" + saved.getId());

        return bloodRequestMapper.toSummaryResponse(saved);
    }

    @Override
    @Transactional
    public BloodRequestSummaryResponse rejectBloodRequest(Long id) {
        User user = getAuthenticatedUser();
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Donor profile not found for user: " + user.getEmail()));

        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        if (request.getHospital() != null) {
            notificationService.notifyHospital(
                    request.getHospital(),
                    "Donor Declined Request",
                    String.format("Donor %s declined donation for request #%d", user.getFullName(), id),
                    NotificationType.DONOR_DECLINED,
                    "/hospital/requests",
                    request,
                    donor
            );
        }

        broadcastRequestEvent(RealtimeEventType.DONOR_DECLINED, request, "Donor Declined Request", "Donor " + user.getFullName() + " declined blood request #" + id);

        return bloodRequestMapper.toSummaryResponse(request);
    }

    private List<BloodRequestSummaryResponse> mapToSummaryResponses(List<BloodRequest> requests) {
        return requests.stream()
                .map(bloodRequestMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found for email: " + email));
    }
}
