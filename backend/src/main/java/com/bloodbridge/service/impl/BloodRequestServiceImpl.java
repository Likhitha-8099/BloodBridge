package com.bloodbridge.service.impl;

import com.bloodbridge.dto.BloodRequestCreateRequest;
import com.bloodbridge.dto.BloodRequestResponse;
import com.bloodbridge.dto.BloodRequestSummaryResponse;
import com.bloodbridge.dto.BloodRequestUpdateRequest;
import com.bloodbridge.dto.RequestStatusResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.exception.HospitalNotFoundException;
import com.bloodbridge.exception.InvalidRequestStateException;
import com.bloodbridge.exception.PatientProfileNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.event.BloodRequestCreatedEvent;
import com.bloodbridge.event.RequestRejectedEvent;
import com.bloodbridge.event.RequestVerifiedEvent;
import com.bloodbridge.mapper.BloodRequestMapper;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.BloodRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing blood requests.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class BloodRequestServiceImpl implements BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final BloodRequestMapper bloodRequestMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BloodRequestResponse createRequest(BloodRequestCreateRequest request) {
        User user = getAuthenticatedUser();

        // Enforce that only PATIENT role can create
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("Only users with PATIENT role can create a blood request");
        }

        // Find patient profile
        PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile must be registered before creating a blood request"));

        // Find hospital
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found for ID: " + request.getHospitalId()));

        // Validate units required
        if (request.getUnitsRequired() <= 0) {
            throw new IllegalArgumentException("Units required must be greater than 0");
        }

        BloodRequest bloodRequest = bloodRequestMapper.toEntity(request, patient, hospital);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        eventPublisher.publishEvent(new BloodRequestCreatedEvent(this, savedRequest));

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

        // Verify that request belongs to currently logged-in patient
        if (!bloodRequest.getPatient().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this blood request");
        }

        // Prevent modification if request is COMPLETED, CANCELLED or REJECTED
        if (bloodRequest.getStatus() == RequestStatus.COMPLETED ||
                bloodRequest.getStatus() == RequestStatus.CANCELLED ||
                bloodRequest.getStatus() == RequestStatus.REJECTED) {
            throw new InvalidRequestStateException("Request cannot be edited because it is in status: " + bloodRequest.getStatus());
        }

        // Validate units
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

        return bloodRequestMapper.toResponse(updatedRequest);
    }

    @Override
    @Transactional
    public BloodRequestResponse cancelRequest(Long id) {
        User user = getAuthenticatedUser();
        BloodRequest bloodRequest = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        // Verify ownership
        if (!bloodRequest.getPatient().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to cancel this blood request");
        }

        if (bloodRequest.getStatus() == RequestStatus.COMPLETED) {
            throw new InvalidRequestStateException("Completed requests cannot be cancelled");
        }

        bloodRequest.setStatus(RequestStatus.CANCELLED);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        return bloodRequestMapper.toResponse(savedRequest);
    }

    @Override
    @Transactional
    public RequestStatusResponse verifyRequest(Long id) {
        User user = getAuthenticatedUser();
        BloodRequest bloodRequest = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + id));

        // Find currently logged-in user's hospital profile
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        // Check if hospital is the one linked to request
        if (!bloodRequest.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to verify requests assigned to another hospital");
        }

        // Verify status is PENDING
        if (bloodRequest.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only PENDING requests can be verified. Current status: " + bloodRequest.getStatus());
        }

        bloodRequest.setStatus(RequestStatus.VERIFIED);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        eventPublisher.publishEvent(new RequestVerifiedEvent(this, savedRequest));

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

        // Find currently logged-in user's hospital profile
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        // Check if hospital is the one linked to request
        if (!bloodRequest.getHospital().getId().equals(hospital.getId())) {
            throw new IllegalArgumentException("You are not authorized to reject requests assigned to another hospital");
        }

        // Verify status is PENDING
        if (bloodRequest.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only PENDING requests can be rejected. Current status: " + bloodRequest.getStatus());
        }

        bloodRequest.setStatus(RequestStatus.REJECTED);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        eventPublisher.publishEvent(new RequestRejectedEvent(this, savedRequest));

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
        List<BloodRequest> requests = bloodRequestRepository.findByStatusIn(List.of(RequestStatus.PENDING, RequestStatus.VERIFIED));
        return mapToSummaryResponses(requests);
    }

    private List<BloodRequestSummaryResponse> mapToSummaryResponses(List<BloodRequest> requests) {
        return requests.stream()
                .map(bloodRequestMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for authenticated email: " + email));
    }
}
