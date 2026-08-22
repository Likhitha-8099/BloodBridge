package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.CreateBloodRequestRequest;
import com.bloodbridge.dto.request.CreatePatientProfileRequest;
import com.bloodbridge.dto.request.UpdateBloodRequestRequest;
import com.bloodbridge.dto.request.UpdatePatientProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.PatientDashboardResponse;
import com.bloodbridge.dto.response.PatientProfileResponse;
import com.bloodbridge.dto.response.RequestTimelineResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.exception.HospitalNotFoundException;
import com.bloodbridge.exception.InvalidAgeException;
import com.bloodbridge.exception.InvalidRequestStateException;
import com.bloodbridge.exception.PatientProfileAlreadyExistsException;
import com.bloodbridge.exception.PatientProfileNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.BloodRequestMapper;
import com.bloodbridge.mapper.PatientProfileMapper;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.PatientProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Patient Management & Emergency Blood Request Portal workflows.
 */
import com.bloodbridge.repository.MatchResultRepository;
import com.bloodbridge.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientProfileServiceImpl implements PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final MatchResultRepository matchResultRepository;
    private final NotificationRepository notificationRepository;
    private final PatientProfileMapper patientProfileMapper;
    private final BloodRequestMapper bloodRequestMapper;
    private final AuditLoggerService auditLoggerService;

    @Override
    @Transactional
    public ApiResponse<PatientProfileResponse> createProfile(String email, CreatePatientProfileRequest request) {
        log.info("Creating patient profile for email: {}", email);
        User user = findUserByEmail(email);

        if (request.getAge() != null && (request.getAge() < 0 || request.getAge() > 120)) {
            log.warn("Patient profile creation failed: Invalid age {}", request.getAge());
            throw new InvalidAgeException("Age must be between 0 and 120");
        }

        if (patientProfileRepository.existsByUserId(user.getId())) {
            log.warn("Patient profile creation failed: Profile already exists for user ID: {}", user.getId());
            throw new PatientProfileAlreadyExistsException("Patient profile already exists for user: " + email);
        }

        Hospital hospital = null;
        if (request.getHospitalId() != null) {
            hospital = hospitalRepository.findById(request.getHospitalId()).orElse(null);
        }

        Optional<PatientProfile> existingByEmail = patientProfileRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            PatientProfile existingProfile = existingByEmail.get();
            if (existingProfile.getUser() != null && !existingProfile.getUser().getId().equals(user.getId()) && Boolean.TRUE.equals(existingProfile.getUser().getActive())) {
                log.warn("Patient profile creation failed: Profile already exists for user: {}", email);
                throw new PatientProfileAlreadyExistsException("Patient profile already exists for user: " + email);
            }
            existingProfile.setUser(user);
            existingProfile.setStatus("ACTIVE");
            if (request.getAge() != null) existingProfile.setAge(request.getAge());
            if (request.getGender() != null) existingProfile.setGender(request.getGender());
            if (request.getBloodGroup() != null) existingProfile.setBloodGroup(request.getBloodGroup());
            if (request.getRhFactor() != null) existingProfile.setRhFactor(request.getRhFactor());
            if (request.getWeight() != null) existingProfile.setWeight(request.getWeight());
            if (request.getMedicalCondition() != null) existingProfile.setMedicalCondition(request.getMedicalCondition());
            if (request.getDiagnosis() != null) existingProfile.setDiagnosis(request.getDiagnosis());
            if (request.getDoctorName() != null) existingProfile.setDoctorName(request.getDoctorName());
            if (hospital != null) existingProfile.setHospital(hospital);
            if (request.getEmergencyContactName() != null) existingProfile.setEmergencyContactName(request.getEmergencyContactName());
            if (request.getEmergencyContactNumber() != null) existingProfile.setEmergencyContactNumber(request.getEmergencyContactNumber());
            if (request.getRelationship() != null) existingProfile.setRelationship(request.getRelationship());
            if (request.getAddress() != null) existingProfile.setAddress(request.getAddress());
            if (request.getCity() != null) existingProfile.setCity(request.getCity());
            if (request.getState() != null) existingProfile.setState(request.getState());
            if (request.getCountry() != null) existingProfile.setCountry(request.getCountry());
            if (request.getPostalCode() != null) existingProfile.setPostalCode(request.getPostalCode());
            if (request.getLatitude() != null) existingProfile.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) existingProfile.setLongitude(request.getLongitude());
            if (request.getPreferredHospital() != null) existingProfile.setPreferredHospital(request.getPreferredHospital());
            if (request.getMedicalHistory() != null) existingProfile.setMedicalHistory(request.getMedicalHistory());
            PatientProfile savedProfile = patientProfileRepository.save(existingProfile);
            auditLoggerService.logEvent("PATIENT_REGISTERED", email, "Patient profile updated with code: " + savedProfile.getPatientCode());
            return ApiResponse.success("Patient profile created successfully", patientProfileMapper.toResponse(savedProfile));
        }

        PatientProfile profile = patientProfileMapper.toEntity(request, user, hospital);
        PatientProfile savedProfile = patientProfileRepository.save(profile);

        auditLoggerService.logEvent("PATIENT_REGISTERED", email, "Patient profile created with code: " + savedProfile.getPatientCode());
        log.info("Patient profile created successfully with ID: {}", savedProfile.getId());

        PatientProfileResponse response = patientProfileMapper.toResponse(savedProfile);
        return ApiResponse.success("Patient profile created successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PatientProfileResponse> getMyProfile(String email) {
        log.info("Fetching patient profile for email: {}", email);
        PatientProfile profile = findPatientByEmail(email);
        PatientProfileResponse response = patientProfileMapper.toResponse(profile);
        return ApiResponse.success("Patient profile retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<PatientProfileResponse> updateProfile(String email, UpdatePatientProfileRequest request) {
        log.info("Updating patient profile for email: {}", email);
        PatientProfile profile = findPatientByEmail(email);

        Hospital hospital = null;
        if (request.getHospitalId() != null) {
            hospital = hospitalRepository.findById(request.getHospitalId()).orElse(null);
        }

        patientProfileMapper.updateEntityFromRequest(request, profile, hospital);
        PatientProfile updatedProfile = patientProfileRepository.save(profile);

        auditLoggerService.logEvent("PATIENT_PROFILE_UPDATED", email, "Patient profile details updated");
        log.info("Successfully updated patient profile for ID: {}", updatedProfile.getId());

        PatientProfileResponse response = patientProfileMapper.toResponse(updatedProfile);
        return ApiResponse.success("Patient profile updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteProfile(String email) {
        log.info("Soft deleting (deactivating) patient profile for email: {}", email);
        PatientProfile profile = findPatientByEmail(email);

        profile.setStatus("DEACTIVATED");
        patientProfileRepository.save(profile);

        if (profile.getUser() != null) {
            User user = profile.getUser();
            user.setActive(false);
            userRepository.save(user);
        }

        auditLoggerService.logEvent("PATIENT_PROFILE_DEACTIVATED", email, "Patient profile soft deleted");
        log.info("Successfully deactivated patient profile for ID: {}", profile.getId());

        return ApiResponse.success("Patient profile deactivated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PatientDashboardResponse> getDashboard(String email) {
        log.info("Fetching Patient Dashboard summary for email: {}", email);
        PatientProfile profile = findPatientByEmail(email);

        List<BloodRequest> requests = bloodRequestRepository.findByPatientId(profile.getId());
        List<BloodRequestResponse> recentRequests = requests.stream()
                .map(bloodRequestMapper::toResponse)
                .collect(Collectors.toList());

        int activeCount = (int) requests.stream().filter(r -> r.getStatus() == RequestStatus.CREATED || r.getStatus() == RequestStatus.MATCHING || r.getStatus() == RequestStatus.DONOR_ACCEPTED || r.getStatus() == RequestStatus.IN_PROGRESS).count();
        int completedCount = (int) requests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED || r.getStatus() == RequestStatus.FULFILLED).count();
        int pendingCount = (int) requests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count();

        int completionPct = calculateProfileCompletionPercentage(profile);
        long matchedDonorsCount = matchResultRepository.countByBloodRequestPatientId(profile.getId());
        long unreadNotificationsCount = profile.getUser() != null ? notificationRepository.countUnreadByRecipientUserId(profile.getUser().getId()) : 0;

        PatientDashboardResponse dashboard = PatientDashboardResponse.builder()
                .profileCompletionPercentage(completionPct)
                .currentBloodRequestsCount(activeCount)
                .completedRequestsCount(completedCount)
                .pendingRequestsCount(pendingCount)
                .matchedDonorsCount((int) matchedDonorsCount)
                .hospitalName(profile.getHospital() != null ? profile.getHospital().getHospitalName() : profile.getPreferredHospital())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactNumber(profile.getEmergencyContactNumber())
                .recentNotificationsCount((int) unreadNotificationsCount)
                .medicalSummary(profile.getMedicalCondition() != null ? profile.getMedicalCondition() : "No active critical conditions recorded")
                .recentRequests(recentRequests)
                .build();

        return ApiResponse.success("Patient dashboard summary retrieved successfully", dashboard);
    }

    @Override
    @Transactional
    public ApiResponse<BloodRequestResponse> createBloodRequest(String email, CreateBloodRequestRequest request) {
        log.info("Patient creating emergency blood request for email: {}", email);
        PatientProfile patient = findPatientByEmail(email);

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found for ID: " + request.getHospitalId()));

        BloodRequest bloodRequest = BloodRequest.builder()
                .patient(patient)
                .hospital(hospital)
                .bloodGroupNeeded(request.getBloodGroupNeeded())
                .unitsRequired(request.getUnitsRequired())
                .urgencyLevel(request.getUrgencyLevel())
                .requestDate(LocalDateTime.now())
                .requiredByDate(request.getRequiredByDate())
                .reason(request.getPatientCondition())
                .notes(request.getNotes())
                .status(RequestStatus.CREATED)
                .build();

        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        auditLoggerService.logEvent("BLOOD_REQUEST_CREATED", email, "Created blood request ID: " + savedRequest.getId());
        log.info("Successfully created blood request ID: {}", savedRequest.getId());

        BloodRequestResponse response = bloodRequestMapper.toResponse(savedRequest);
        return ApiResponse.success("Emergency blood request created successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<BloodRequestResponse>> getMyBloodRequests(String email) {
        log.info("Fetching blood requests for patient email: {}", email);
        PatientProfile patient = findPatientByEmail(email);

        List<BloodRequest> requests = bloodRequestRepository.findByPatientId(patient.getId());
        List<BloodRequestResponse> response = requests.stream()
                .map(bloodRequestMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Patient blood requests retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BloodRequestResponse> getBloodRequestById(String email, Long requestId) {
        log.info("Fetching blood request ID: {} for patient email: {}", requestId, email);
        PatientProfile patient = findPatientByEmail(email);

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + requestId));

        if (request.getPatient() != null && !request.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Unauthorized to access blood request belonging to another patient");
        }

        BloodRequestResponse response = bloodRequestMapper.toResponse(request);
        return ApiResponse.success("Blood request details retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<BloodRequestResponse> updateBloodRequest(String email, Long requestId, UpdateBloodRequestRequest request) {
        log.info("Updating blood request ID: {} for patient email: {}", requestId, email);
        PatientProfile patient = findPatientByEmail(email);

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + requestId));

        if (bloodRequest.getPatient() != null && !bloodRequest.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Unauthorized to edit blood request belonging to another patient");
        }

        if (bloodRequest.getStatus() == RequestStatus.COMPLETED || bloodRequest.getStatus() == RequestStatus.FULFILLED || bloodRequest.getStatus() == RequestStatus.CANCELLED) {
            throw new InvalidRequestStateException("Cannot update blood request in status: " + bloodRequest.getStatus());
        }

        if (request.getUnitsRequired() != null) {
            bloodRequest.setUnitsRequired(request.getUnitsRequired());
        }
        if (request.getRequiredByDate() != null) {
            bloodRequest.setRequiredByDate(request.getRequiredByDate());
        }
        if (request.getUrgencyLevel() != null) {
            bloodRequest.setUrgencyLevel(request.getUrgencyLevel());
        }
        if (request.getPatientCondition() != null) {
            bloodRequest.setReason(request.getPatientCondition());
        }
        if (request.getNotes() != null) {
            bloodRequest.setNotes(request.getNotes());
        }

        BloodRequest updatedRequest = bloodRequestRepository.save(bloodRequest);

        auditLoggerService.logEvent("BLOOD_REQUEST_UPDATED", email, "Updated blood request ID: " + requestId);
        log.info("Successfully updated blood request ID: {}", requestId);

        BloodRequestResponse response = bloodRequestMapper.toResponse(updatedRequest);
        return ApiResponse.success("Blood request updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<BloodRequestResponse> cancelBloodRequest(String email, Long requestId) {
        log.info("Cancelling blood request ID: {} for patient email: {}", requestId, email);
        PatientProfile patient = findPatientByEmail(email);

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + requestId));

        if (bloodRequest.getPatient() != null && !bloodRequest.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Unauthorized to cancel blood request belonging to another patient");
        }

        if (bloodRequest.getStatus() == RequestStatus.COMPLETED || bloodRequest.getStatus() == RequestStatus.FULFILLED) {
            throw new InvalidRequestStateException("Completed blood requests cannot be cancelled");
        }

        bloodRequest.setStatus(RequestStatus.CANCELLED);
        BloodRequest cancelledRequest = bloodRequestRepository.save(bloodRequest);

        auditLoggerService.logEvent("BLOOD_REQUEST_CANCELLED", email, "Cancelled blood request ID: " + requestId);
        log.info("Successfully cancelled blood request ID: {}", requestId);

        BloodRequestResponse response = bloodRequestMapper.toResponse(cancelledRequest);
        return ApiResponse.success("Blood request cancelled successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RequestTimelineResponse> getBloodRequestTimeline(String email, Long requestId) {
        log.info("Building status tracking timeline for request ID: {} and patient email: {}", requestId, email);
        PatientProfile patient = findPatientByEmail(email);

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + requestId));

        if (request.getPatient() != null && !request.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Unauthorized to access timeline for request belonging to another patient");
        }

        List<RequestTimelineResponse.TimelineStep> steps = new ArrayList<>();
        LocalDateTime baseTime = request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now();

        steps.add(RequestTimelineResponse.TimelineStep.builder()
                .stepName("Request Created")
                .timestamp(baseTime)
                .status("COMPLETED")
                .details("Emergency blood request ticket created by patient.")
                .build());

        steps.add(RequestTimelineResponse.TimelineStep.builder()
                .stepName("Matching Started")
                .timestamp(baseTime.plusMinutes(2))
                .status(request.getStatus() != RequestStatus.CREATED ? "COMPLETED" : "IN_PROGRESS")
                .details("Automated matching engine querying compatible nearby donors.")
                .build());

        steps.add(RequestTimelineResponse.TimelineStep.builder()
                .stepName("Hospital Verified")
                .timestamp(baseTime.plusMinutes(5))
                .status((request.getStatus() == RequestStatus.VERIFIED || request.getStatus() == RequestStatus.DONOR_ACCEPTED || request.getStatus() == RequestStatus.COMPLETED || request.getStatus() == RequestStatus.FULFILLED) ? "COMPLETED" : "PENDING")
                .details("Hospital medical team verified blood requirements.")
                .build());

        steps.add(RequestTimelineResponse.TimelineStep.builder()
                .stepName("Donor Accepted")
                .timestamp(baseTime.plusMinutes(15))
                .status((request.getStatus() == RequestStatus.DONOR_ACCEPTED || request.getStatus() == RequestStatus.IN_PROGRESS || request.getStatus() == RequestStatus.COMPLETED || request.getStatus() == RequestStatus.FULFILLED) ? "COMPLETED" : "PENDING")
                .details("Eligible donor accepted emergency dispatch request.")
                .build());

        steps.add(RequestTimelineResponse.TimelineStep.builder()
                .stepName("Donation Completed")
                .timestamp(baseTime.plusMinutes(60))
                .status((request.getStatus() == RequestStatus.COMPLETED || request.getStatus() == RequestStatus.FULFILLED) ? "COMPLETED" : "PENDING")
                .details("Blood units donated and verified at hospital blood bank.")
                .build());

        RequestTimelineResponse timelineResponse = RequestTimelineResponse.builder()
                .requestId(request.getId())
                .currentStatus(request.getStatus())
                .timeline(steps)
                .build();

        return ApiResponse.success("Blood request status timeline retrieved successfully", timelineResponse);
    }

    private int calculateProfileCompletionPercentage(PatientProfile profile) {
        int total = 10;
        int count = 0;

        if (profile.getAge() != null) count++;
        if (profile.getGender() != null) count++;
        if (profile.getBloodGroup() != null) count++;
        if (profile.getMedicalCondition() != null) count++;
        if (profile.getEmergencyContactName() != null) count++;
        if (profile.getEmergencyContactNumber() != null) count++;
        if (profile.getCity() != null) count++;
        if (profile.getState() != null) count++;
        if (profile.getDoctorName() != null) count++;
        if (profile.getHospital() != null || profile.getPreferredHospital() != null) count++;

        return (count * 100) / total;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private PatientProfile findPatientByEmail(String email) {
        User user = findUserByEmail(email);
        return patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found for user: " + email));
    }
}
