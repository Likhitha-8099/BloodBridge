package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.CreateHospitalRequest;
import com.bloodbridge.dto.request.HospitalBloodRequestCreate;
import com.bloodbridge.dto.request.UpdateHospitalRequest;
import com.bloodbridge.dto.request.UpdateInventoryRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodInventoryResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonorMatchViewResponse;
import com.bloodbridge.dto.response.HospitalAnalyticsResponse;
import com.bloodbridge.dto.response.HospitalDashboardResponse;
import com.bloodbridge.dto.response.HospitalDonorResponseDTO;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.entity.BloodInventory;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.exception.HospitalAlreadyExistsException;
import com.bloodbridge.exception.HospitalNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.BloodRequestMapper;
import com.bloodbridge.mapper.DonorProfileMapper;
import com.bloodbridge.mapper.HospitalMapper;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.BloodInventoryRepository;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.MatchedEmergencyDonorRepository;
import org.springframework.security.access.AccessDeniedException;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.HospitalService;
import com.bloodbridge.service.RealtimeService;
import com.bloodbridge.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Hospital Management, Blood Inventory & Emergency Request Center workflows.
 * Integrates instant real-time STOMP WebSocket broadcasting.
 */
import com.bloodbridge.event.HospitalVerificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.bloodbridge.service.CertificateService;
import com.bloodbridge.service.EmailService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.SmartDonorMatchingPipelineService;

import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.MatchResultRepository;
import com.bloodbridge.service.DonationService;
import com.bloodbridge.entity.Donation;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final BloodInventoryRepository bloodInventoryRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final MatchResultRepository matchResultRepository;
    private final DonationRepository donationRepository;
    private final HospitalMapper hospitalMapper;
    private final BloodRequestMapper bloodRequestMapper;
    private final UserMapper userMapper;
    private final DonorProfileMapper donorProfileMapper;
    private final StorageService storageService;
    private final AuditLoggerService auditLoggerService;
    private final RealtimeService realtimeService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final CertificateService certificateService;
    private final ApplicationEventPublisher eventPublisher;
    private final SmartDonorMatchingPipelineService smartDonorMatchingPipelineService;
    private final MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private final DonationService donationService;

    @Override
    @Transactional
    public ApiResponse<HospitalResponse> createHospital(String email, CreateHospitalRequest request) {
        log.info("Creating hospital profile for email: {}", email);
        User user = findUserByEmail(email);

        if (hospitalRepository.existsByUserId(user.getId())) {
            log.warn("Hospital profile creation failed: Profile already exists for user ID: {}", user.getId());
            throw new HospitalAlreadyExistsException("Hospital profile already exists for user: " + email);
        }

        Optional<Hospital> existingByReg = hospitalRepository.findByRegistrationNumber(request.getRegistrationNumber());
        if (existingByReg.isPresent()) {
            Hospital existingHosp = existingByReg.get();
            if (existingHosp.getUser() != null && !existingHosp.getUser().getId().equals(user.getId()) && Boolean.TRUE.equals(existingHosp.getUser().getActive())) {
                log.warn("Hospital creation failed: Registration number {} already registered", request.getRegistrationNumber());
                throw new HospitalAlreadyExistsException("Registration number already registered: " + request.getRegistrationNumber());
            }
            existingHosp.setUser(user);
            if (request.getHospitalName() != null) existingHosp.setHospitalName(request.getHospitalName());
            if (request.getLicenseNumber() != null) existingHosp.setLicenseNumber(request.getLicenseNumber());
            if (request.getHospitalType() != null) existingHosp.setHospitalType(request.getHospitalType());
            if (request.getContactPerson() != null) existingHosp.setContactPerson(request.getContactPerson());
            if (request.getEmail() != null) existingHosp.setEmail(request.getEmail());
            if (request.getPhoneNumber() != null) existingHosp.setPhoneNumber(request.getPhoneNumber());
            if (request.getWebsite() != null) existingHosp.setWebsite(request.getWebsite());
            if (request.getAddress() != null) existingHosp.setAddress(request.getAddress());
            if (request.getCity() != null) existingHosp.setCity(request.getCity());
            if (request.getState() != null) existingHosp.setState(request.getState());
            if (request.getCountry() != null) existingHosp.setCountry(request.getCountry());
            if (request.getPostalCode() != null) existingHosp.setPostalCode(request.getPostalCode());
            if (request.getLatitude() != null) existingHosp.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) existingHosp.setLongitude(request.getLongitude());
            if (request.getOperatingHours() != null) existingHosp.setOperatingHours(request.getOperatingHours());
            if (request.getEmergencyAvailable() != null) existingHosp.setEmergencyAvailable(request.getEmergencyAvailable());
            Hospital savedHospital = hospitalRepository.save(existingHosp);
            initializeHospitalInventory(savedHospital);
            auditLoggerService.logEvent("HOSPITAL_REGISTERED", email, "Hospital registered: " + savedHospital.getHospitalName());
            log.info("Hospital profile updated successfully with ID: {}", savedHospital.getId());
            HospitalResponse response = hospitalMapper.toResponse(savedHospital);
            return ApiResponse.success("Hospital profile created successfully. Verification pending.", response);
        }

        Hospital hospital = hospitalMapper.toEntity(request, user);
        Hospital savedHospital = hospitalRepository.save(hospital);

        initializeHospitalInventory(savedHospital);

        auditLoggerService.logEvent("HOSPITAL_REGISTERED", email, "Hospital registered: " + savedHospital.getHospitalName());
        log.info("Hospital profile created successfully with ID: {}", savedHospital.getId());

        HospitalResponse response = hospitalMapper.toResponse(savedHospital);
        return ApiResponse.success("Hospital profile created successfully. Verification pending.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HospitalResponse> getMyHospital(String email) {
        log.info("Fetching hospital profile for email: {}", email);
        Hospital hospital = findHospitalByEmail(email);
        HospitalResponse response = hospitalMapper.toResponse(hospital);
        return ApiResponse.success("Hospital profile retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<HospitalResponse> updateHospital(String email, UpdateHospitalRequest request) {
        log.info("Updating hospital profile for email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        hospitalMapper.updateEntityFromRequest(request, hospital);
        Hospital updatedHospital = hospitalRepository.save(hospital);

        auditLoggerService.logEvent("HOSPITAL_UPDATED", email, "Hospital profile details updated");
        log.info("Successfully updated hospital profile for ID: {}", updatedHospital.getId());

        HospitalResponse response = hospitalMapper.toResponse(updatedHospital);

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.HOSPITAL_UPDATED,
                    "HOSPITAL",
                    updatedHospital.getId(),
                    "Hospital Profile Updated",
                    "Hospital details updated.",
                    response
            );
            realtimeService.publishHospitalUpdate(updatedHospital.getId(), event);
            realtimeService.publishAdminHospitalsUpdate(event);
        } catch (Exception e) {
            log.error("Failed to publish hospital profile update STOMP event: {}", e.getMessage());
        }

        return ApiResponse.success("Hospital profile updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<HospitalResponse> uploadLicense(String email, String documentUrl) {
        log.info("Uploading license document for hospital user email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        String processedUrl = storageService.storeImageUrl(documentUrl, hospital.getId());
        hospital.setLicenseDocumentUrl(processedUrl);
        Hospital updatedHospital = hospitalRepository.save(hospital);

        auditLoggerService.logEvent("LICENSE_UPLOADED", email, "License document uploaded: " + processedUrl);
        log.info("Successfully updated license document for hospital ID: {}", hospital.getId());

        HospitalResponse response = hospitalMapper.toResponse(updatedHospital);
        return ApiResponse.success("License document uploaded successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<HospitalResponse> uploadLogo(String email, String logoUrl) {
        log.info("Uploading logo for hospital user email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        String processedUrl = storageService.storeImageUrl(logoUrl, hospital.getId());
        hospital.setLogoUrl(processedUrl);
        Hospital updatedHospital = hospitalRepository.save(hospital);

        auditLoggerService.logEvent("LOGO_UPLOADED", email, "Logo uploaded: " + processedUrl);
        log.info("Successfully updated logo for hospital ID: {}", hospital.getId());

        HospitalResponse response = hospitalMapper.toResponse(updatedHospital);
        return ApiResponse.success("Hospital logo uploaded successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HospitalDashboardResponse> getDashboard(String email) {
        log.info("Fetching Hospital Dashboard summary for email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        List<BloodInventory> inventories = bloodInventoryRepository.findByHospitalId(hospital.getId());
        List<BloodInventoryResponse> inventorySummary = inventories.stream()
                .map(inv -> BloodInventoryResponse.builder()
                        .id(inv.getId())
                        .bloodGroup(inv.getBloodGroup())
                        .availableUnits(inv.getAvailableUnits())
                        .reservedUnits(inv.getReservedUnits())
                        .criticalThreshold(inv.getCriticalThreshold())
                        .inventoryStatus(inv.getInventoryStatus())
                        .updatedAt(inv.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        int lowAlerts = (int) inventories.stream().filter(inv -> "LOW".equals(inv.getInventoryStatus())).count();
        int criticalAlerts = (int) inventories.stream().filter(inv -> "CRITICAL".equals(inv.getInventoryStatus()) || "OUT_OF_STOCK".equals(inv.getInventoryStatus())).count();

        List<BloodRequest> requests = bloodRequestRepository.findByHospitalId(hospital.getId());
        int todaysRequests = (int) requests.stream()
                .filter(req -> req.getCreatedAt() != null && req.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        long matchedDonorsCount = matchResultRepository.countByBloodRequestHospitalId(hospital.getId());
        long pendingDonationsCount = donationRepository.countByHospitalIdAndStatusIn(
                hospital.getId(),
                List.of(com.bloodbridge.enums.DonationStatus.PENDING, com.bloodbridge.enums.DonationStatus.ACCEPTED, com.bloodbridge.enums.DonationStatus.CONFIRMED)
        );

        HospitalDashboardResponse dashboard = HospitalDashboardResponse.builder()
                .verificationStatus(hospital.getVerificationStatus())
                .inventorySummary(inventorySummary)
                .todaysRequests(todaysRequests)
                .emergencyRequestsCount((int) requests.stream().filter(r -> r.getStatus() == RequestStatus.CREATED || r.getStatus() == RequestStatus.MATCHING || r.getStatus() == RequestStatus.PENDING).count())
                .matchedDonorsCount((int) matchedDonorsCount)
                .pendingDonationsCount((int) pendingDonationsCount)
                .completedDonationsCount((int) requests.stream().filter(r -> r.getStatus() == RequestStatus.FULFILLED || r.getStatus() == RequestStatus.COMPLETED).count())
                .lowInventoryAlerts(lowAlerts)
                .criticalInventoryAlerts(criticalAlerts)
                .build();

        return ApiResponse.success("Hospital dashboard summary retrieved successfully", dashboard);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HospitalAnalyticsResponse> getAnalytics(String email) {
        log.info("Generating Hospital Analytics for email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        List<BloodRequest> requests = bloodRequestRepository.findByHospitalId(hospital.getId());
        long total = requests.size();
        long fulfilled = requests.stream().filter(r -> r.getStatus() == RequestStatus.FULFILLED || r.getStatus() == RequestStatus.COMPLETED).count();
        long cancelled = requests.stream().filter(r -> r.getStatus() == RequestStatus.CANCELLED || r.getStatus() == RequestStatus.REJECTED).count();

        Map<String, Integer> demandMap = new HashMap<>();
        for (BloodRequest req : requests) {
            if (req.getBloodGroupNeeded() != null) {
                String bg = req.getBloodGroupNeeded().name();
                demandMap.put(bg, demandMap.getOrDefault(bg, 0) + req.getUnitsRequired());
            }
        }

        Map<String, Integer> consumptionMap = new HashMap<>();
        List<BloodInventory> inventories = bloodInventoryRepository.findByHospitalId(hospital.getId());
        for (BloodInventory inv : inventories) {
            if (inv.getBloodGroup() != null) {
                consumptionMap.put(inv.getBloodGroup().name(), inv.getAvailableUnits() + inv.getReservedUnits());
            }
        }

        double successRate = total > 0 ? ((double) fulfilled / total) * 100.0 : 100.0;

        double avgResponseTimeHours = requests.stream()
                .filter(r -> (r.getStatus() == RequestStatus.COMPLETED || r.getStatus() == RequestStatus.FULFILLED) && r.getCreatedAt() != null && r.getUpdatedAt() != null)
                .mapToDouble(r -> java.time.Duration.between(r.getCreatedAt(), r.getUpdatedAt()).toMinutes() / 60.0)
                .average()
                .orElse(0.0);
        avgResponseTimeHours = Math.round(avgResponseTimeHours * 10.0) / 10.0;

        HospitalAnalyticsResponse analytics = HospitalAnalyticsResponse.builder()
                .totalRequests(total)
                .fulfilledRequests(fulfilled)
                .cancelledRequests(cancelled)
                .averageResponseTimeHours(avgResponseTimeHours)
                .donationSuccessRatePct(Math.round(successRate * 10.0) / 10.0)
                .bloodGroupDemand(demandMap)
                .inventoryConsumption(consumptionMap)
                .build();

        return ApiResponse.success("Hospital analytics retrieved successfully", analytics);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<BloodInventoryResponse>> getBloodInventory(String email) {
        log.info("Fetching blood bank inventory for hospital email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        List<BloodInventory> inventories = bloodInventoryRepository.findByHospitalId(hospital.getId());
        List<BloodInventoryResponse> response = inventories.stream()
                .map(inv -> BloodInventoryResponse.builder()
                        .id(inv.getId())
                        .bloodGroup(inv.getBloodGroup())
                        .availableUnits(inv.getAvailableUnits())
                        .reservedUnits(inv.getReservedUnits())
                        .criticalThreshold(inv.getCriticalThreshold())
                        .inventoryStatus(inv.getInventoryStatus())
                        .updatedAt(inv.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success("Blood bank inventory stock list retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<BloodInventoryResponse> updateInventory(String email, UpdateInventoryRequest request) {
        log.info("Updating inventory for hospital email: {} and blood group: {}", email, request.getBloodGroup());
        Hospital hospital = findHospitalByEmail(email);

        BloodInventory inventory = bloodInventoryRepository.findByHospitalIdAndBloodGroup(hospital.getId(), request.getBloodGroup())
                .orElseGet(() -> BloodInventory.builder()
                        .hospital(hospital)
                        .bloodGroup(request.getBloodGroup())
                        .availableUnits(0)
                        .reservedUnits(0)
                        .criticalThreshold(5)
                        .build());

        inventory.setAvailableUnits(request.getAvailableUnits());
        if (request.getReservedUnits() != null) {
            inventory.setReservedUnits(request.getReservedUnits());
        }
        if (request.getCriticalThreshold() != null) {
            inventory.setCriticalThreshold(request.getCriticalThreshold());
        }

        inventory.calculateInventoryStatus();
        BloodInventory savedInventory = bloodInventoryRepository.save(inventory);

        auditLoggerService.logEvent("INVENTORY_UPDATED", email, "Updated " + request.getBloodGroup() + " units to " + request.getAvailableUnits());
        log.info("Successfully updated blood inventory ID: {} to status: {}", savedInventory.getId(), savedInventory.getInventoryStatus());

        BloodInventoryResponse response = BloodInventoryResponse.builder()
                .id(savedInventory.getId())
                .bloodGroup(savedInventory.getBloodGroup())
                .availableUnits(savedInventory.getAvailableUnits())
                .reservedUnits(savedInventory.getReservedUnits())
                .criticalThreshold(savedInventory.getCriticalThreshold())
                .inventoryStatus(savedInventory.getInventoryStatus())
                .updatedAt(savedInventory.getUpdatedAt())
                .build();

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.INVENTORY_UPDATED,
                    "BLOOD_INVENTORY",
                    savedInventory.getId(),
                    "Blood Inventory Updated",
                    String.format("Blood Group %s updated to %d units", savedInventory.getBloodGroup(), savedInventory.getAvailableUnits()),
                    response
            );
            realtimeService.publishHospitalUpdate(hospital.getId(), event);
            realtimeService.publishAdminDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to publish blood inventory STOMP event: {}", e.getMessage());
        }

        return ApiResponse.success("Blood bank inventory updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<BloodRequestResponse> createBloodRequest(String email, HospitalBloodRequestCreate request) {
        log.info("Hospital creating emergency blood request for email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        if (!hospital.isApprovedOrVerified()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Your hospital account is pending administrator verification before blood requests can be created."
            );
        }

        PatientProfile patient = null;
        if (request.getPatientId() != null) {
            patient = patientProfileRepository.findById(request.getPatientId()).orElse(null);
        }

        BloodRequest bloodRequest = BloodRequest.builder()
                .hospital(hospital)
                .patient(patient)
                .bloodGroupNeeded(request.getBloodGroupNeeded())
                .unitsRequired(request.getUnitsRequired())
                .urgencyLevel(request.getUrgencyLevel())
                .requestDate(LocalDateTime.now())
                .requiredByDate(request.getRequiredByDate())
                .reason(request.getReason())
                .notes(request.getNotes())
                .status(RequestStatus.CREATED)
                .build();

        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        auditLoggerService.logEvent("BLOOD_REQUEST_CREATED", email, "Created request ID: " + savedRequest.getId() + " for " + savedRequest.getBloodGroupNeeded());
        log.info("Successfully created blood request ID: {}", savedRequest.getId());

        BloodRequestResponse response = bloodRequestMapper.toResponse(savedRequest);

        try {
            log.info("[PIPELINE-TRIGGER] Automatically triggering Smart Donor Matching Pipeline for Hospital Emergency Request #{} (Status: {})", savedRequest.getId(), savedRequest.getStatus());
            smartDonorMatchingPipelineService.executePipeline(savedRequest);
            log.info("[PIPELINE-TRIGGER-SUCCESS] Smart Donor Matching Pipeline completed execution for Emergency Request #{}", savedRequest.getId());
        } catch (Exception e) {
            log.error("[PIPELINE-TRIGGER-ERROR] Failed to execute Smart Donor Matching Pipeline for emergency request #{}: {}", savedRequest.getId(), e.getMessage(), e);
        }

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.BLOOD_REQUEST_CREATED,
                    "BLOOD_REQUEST",
                    savedRequest.getId(),
                    "New Hospital Emergency Request",
                    String.format("Emergency %s blood request created.", savedRequest.getBloodGroupNeeded()),
                    response
            );
            realtimeService.publishHospitalUpdate(hospital.getId(), event);
            realtimeService.publishAdminDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to publish hospital blood request STOMP event: {}", e.getMessage());
        }

        return ApiResponse.success("Emergency blood request created successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<BloodRequestResponse>> getHospitalBloodRequests(String email) {
        log.info("Fetching blood requests for hospital user email: {}", email);
        Hospital hospital = findHospitalByEmail(email);

        List<BloodRequest> requests = bloodRequestRepository.findByHospitalIdOrderByCreatedAtDesc(hospital.getId());
        List<BloodRequestResponse> responseList = requests.stream()
                .map(bloodRequestMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Hospital blood requests retrieved successfully", responseList);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<DonorMatchViewResponse>> getMatchedDonors(String email, Long requestId) {
        log.info("Fetching matched compatible donors for hospital email: {} and request ID: {}", email, requestId);
        Hospital hospital = findHospitalByEmail(email);

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + requestId));

        List<DonorProfile> availableDonors = donorProfileRepository.findByAvailableForDonationTrue();
        List<DonorMatchViewResponse> matchedList = new ArrayList<>();

        for (DonorProfile donor : availableDonors) {
            if (isCompatibleBloodGroup(donor.getBloodGroup(), bloodRequest.getBloodGroupNeeded())) {
                User donorUser = donor.getUser();
                matchedList.add(DonorMatchViewResponse.builder()
                        .donorId(donor.getId())
                        .fullName(donorUser != null ? donorUser.getFullName() : "Anonymous Donor")
                        .bloodGroup(donor.getBloodGroup())
                        .distanceKm(calculateDistanceKm(hospital.getLatitude(), hospital.getLongitude(), donor.getLatitude(), donor.getLongitude()))
                        .eligible(true)
                        .available(donor.getAvailableForDonation())
                        .donorScore(donor.getDonorScore())
                        .lastDonationDate(donor.getLastDonationDate())
                        .build());
            }
        }

        return ApiResponse.success("Compatible matched donors retrieved successfully", matchedList);
    }

    @Override
    @Transactional
    public ApiResponse<HospitalResponse> verifyHospital(Long hospitalId, String adminEmail, String status, String remarks) {
        log.info("[STEP 1] Loading hospital with ID: {} for verification review by admin: {}", hospitalId, adminEmail);
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found with ID: " + hospitalId));

        boolean approved = "APPROVED".equalsIgnoreCase(status) || "VERIFIED".equalsIgnoreCase(status);

        log.info("[STEP 2] Updating hospital status to: {} (approved={})", status, approved);
        if (approved) {
            hospital.markAsApproved(adminEmail, remarks);
        } else {
            hospital.markAsRejected(adminEmail, remarks);
        }

        if (hospital.getUser() != null || hospital.getEmail() != null) {
            String targetEmail = hospital.getUser() != null ? hospital.getUser().getEmail() : hospital.getEmail();
            log.info("[STEP 3] Updating associated user active status to: {} for email: {}", approved, targetEmail);
            userRepository.findByEmail(targetEmail).ifPresent(u -> {
                u.setActive(approved);
                userRepository.save(u);
                hospital.setUser(u);
            });
        }

        log.info("[STEP 4] Saving updated hospital entity to database");
        Hospital updatedHospital = hospitalRepository.save(hospital);

        log.info("[STEP 5] Mapping updated hospital entity to response DTO");
        HospitalResponse response = hospitalMapper.toResponse(updatedHospital);

        log.info("[STEP 6] Publishing HospitalVerificationEvent for post-commit side effects");
        eventPublisher.publishEvent(new HospitalVerificationEvent(this, updatedHospital, status, remarks, adminEmail, approved, response));

        log.info("[STEP 7] Transaction setup complete for hospital ID: {}. Returning success response.", updatedHospital.getId());
        return ApiResponse.success("Hospital verification status updated successfully", response);
    }

    private void initializeHospitalInventory(Hospital hospital) {
        for (BloodGroup bg : BloodGroup.values()) {
            BloodInventory inventory = BloodInventory.builder()
                    .hospital(hospital)
                    .bloodGroup(bg)
                    .availableUnits(0)
                    .reservedUnits(0)
                    .criticalThreshold(5)
                    .inventoryStatus("OUT_OF_STOCK")
                    .build();
            bloodInventoryRepository.save(inventory);
        }
    }

    private boolean isCompatibleBloodGroup(BloodGroup donorBg, BloodGroup neededBg) {
        if (donorBg == null || neededBg == null) return false;
        if (donorBg == neededBg) return true;
        if (donorBg == BloodGroup.O_NEGATIVE) return true;
        return false;
    }

    private double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 5.0;
        }
        double latDiff = Math.toRadians(lat2 - lat1);
        double lonDiff = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((6371 * c) * 10.0) / 10.0;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private Hospital findHospitalByEmail(String email) {
        User user = findUserByEmail(email);
        return hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public com.bloodbridge.dto.response.UserPageResponse getAllUsers(String search, String bloodGroup, String city, String state, int page, int size) {
        log.info("Fetching users for hospital portal: search={}, bloodGroup={}, city={}, state={}, page={}, size={}",
                search, bloodGroup, city, state, page, size);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page >= 0 ? page : 0,
                size > 0 ? size : 10,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );

        org.springframework.data.domain.Page<User> userPage = userRepository.findUsersWithFilters(
                (search != null && !search.isBlank()) ? search.trim() : null,
                (city != null && !city.isBlank()) ? city.trim() : null,
                (state != null && !state.isBlank()) ? state.trim() : null,
                pageable
        );

        java.util.List<com.bloodbridge.dto.response.UserProfileResponse> content = userPage.getContent().stream()
                .map(user -> {
                    com.bloodbridge.dto.response.UserProfileResponse res = userMapper.toProfileResponse(user);
                    donorProfileRepository.findByUserId(user.getId()).ifPresent(dp -> {
                        if (dp.getBloodGroup() != null) {
                            res.setProfileImage(dp.getBloodGroup().name());
                        }
                    });
                    return res;
                })
                .collect(java.util.stream.Collectors.toList());

        return com.bloodbridge.dto.response.UserPageResponse.builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public com.bloodbridge.dto.response.DonorPageResponse getAllDonors(String search, String bloodGroup, String city, String state, Boolean available, int page, int size) {
        log.info("Fetching donors for hospital portal: search={}, bloodGroup={}, city={}, state={}, available={}, page={}, size={}",
                search, bloodGroup, city, state, available, page, size);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page >= 0 ? page : 0,
                size > 0 ? size : 10,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );

        BloodGroup parsedBg = null;
        if (bloodGroup != null && !bloodGroup.isBlank()) {
            try {
                String formatted = bloodGroup.trim().toUpperCase().replace("+", "_POSITIVE").replace("-", "_NEGATIVE");
                parsedBg = BloodGroup.valueOf(formatted);
            } catch (Exception e) {
                log.debug("Could not parse bloodGroup enum from: {}", bloodGroup);
            }
        }

        org.springframework.data.domain.Page<DonorProfile> donorPage = donorProfileRepository.searchDonors(
                (search != null && !search.isBlank()) ? search.trim() : null,
                parsedBg,
                (city != null && !city.isBlank()) ? city.trim() : null,
                (state != null && !state.isBlank()) ? state.trim() : null,
                available,
                pageable
        );

        java.util.List<com.bloodbridge.dto.response.DonorProfileResponse> content = donorPage.getContent().stream()
                .map(donorProfileMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());

        return com.bloodbridge.dto.response.DonorPageResponse.builder()
                .content(content)
                .pageNumber(donorPage.getNumber())
                .pageSize(donorPage.getSize())
                .totalElements(donorPage.getTotalElements())
                .totalPages(donorPage.getTotalPages())
                .last(donorPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO> getEmergencyRequestResponses(String email, Long requestId) {
        log.info("Fetching donor responses for emergency blood request #{} requested by user email: {}", requestId, email);

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new com.bloodbridge.exception.BloodRequestNotFoundException("Blood request not found for ID: " + requestId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        // Ownership Verification: Ensure hospital owns the request or user is ADMIN
        if (user.getRole() != Role.ADMIN) {
            Hospital hospital = findHospitalByEmail(email);
            if (bloodRequest.getHospital() == null || !bloodRequest.getHospital().getId().equals(hospital.getId())) {
                log.warn("Access Denied: Hospital user {} attempted to view responses for request #{} owned by hospital #{}",
                        email, requestId, bloodRequest.getHospital() != null ? bloodRequest.getHospital().getId() : "NULL");
                throw new AccessDeniedException("Access denied: You do not own blood request #" + requestId);
            }
        }

        List<MatchedEmergencyDonor> matchedList = matchedEmergencyDonorRepository.findByBloodRequestIdWithDonorDetails(requestId);

        List<com.bloodbridge.dto.response.HospitalDonorResponseDTO> responseList = matchedList.stream()
                .<com.bloodbridge.dto.response.HospitalDonorResponseDTO>map(med -> {
                    DonorProfile donor = med.getDonor();
                    User donorUser = donor != null ? donor.getUser() : null;
                    String donorName = donorUser != null && donorUser.getFullName() != null ? donorUser.getFullName() : (donor != null ? donor.getEmail() : "Valued Donor");
                    String donorEmail = donorUser != null ? donorUser.getEmail() : (donor != null ? donor.getEmail() : "");
                    String donorPhone = donorUser != null ? donorUser.getPhoneNumber() : (donor != null && donor.getAlternatePhoneNumber() != null ? donor.getAlternatePhoneNumber() : "");
                    String bgFormatted = (donor != null && donor.getBloodGroup() != null) ?
                            donor.getBloodGroup().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";

                    boolean isConfirmed = Boolean.TRUE.equals(med.getConfirmed()) || med.getStatus() == com.bloodbridge.enums.MatchedEmergencyDonorStatus.CONFIRMED;

                    return com.bloodbridge.dto.response.HospitalDonorResponseDTO.builder()
                            .matchedDonorId(med.getId())
                            .donorId(donor != null ? donor.getId() : null)
                            .donorName(donorName)
                            .bloodGroup(bgFormatted)
                            .email(donorEmail)
                            .phone(donorPhone)
                            .donorStatus(donor != null && donor.getStatus() != null ? donor.getStatus() : "ACTIVE")
                            .distanceKm(med.getDistanceKm())
                            .tierGroup(med.getMatchingGroup())
                            .matchingGroup(med.getMatchingGroup())
                            .responseStatus(med.getStatus() != null ? med.getStatus().name() : "PENDING")
                            .confirmed(isConfirmed)
                            .confirmedAt(med.getConfirmedAt())
                            .acceptedAt(med.getAcceptedAt())
                            .rejectedAt(med.getRejectedAt())
                            .fulfillmentStatus(med.getFulfillmentStatus())
                            .createdAt(med.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());

        int totalMatched = responseList.size();
        int acceptedCount = (int) responseList.stream().filter(r -> "ACCEPTED".equalsIgnoreCase(r.getResponseStatus()) || "CONFIRMED".equalsIgnoreCase(r.getResponseStatus())).count();
        int confirmedCount = (int) responseList.stream().filter(r -> Boolean.TRUE.equals(r.getConfirmed()) || "CONFIRMED".equalsIgnoreCase(r.getResponseStatus())).count();
        int pendingCount = (int) responseList.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getResponseStatus()) || "VIEWED".equalsIgnoreCase(r.getResponseStatus())).count();
        int rejectedCount = (int) responseList.stream().filter(r -> "REJECTED".equalsIgnoreCase(r.getResponseStatus())).count();

        log.info("[HOSPITAL-RESPONSE-DEBUG] requestId={}, totalMatched={}, accepted={}, pending={}, confirmed={}", requestId, totalMatched, acceptedCount, pendingCount, confirmedCount);

        com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO container = com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO.builder()
                .requestId(requestId)
                .totalMatchedDonors(totalMatched)
                .acceptedDonors(acceptedCount)
                .pendingDonors(pendingCount)
                .rejectedDonors(rejectedCount)
                .confirmedDonors(confirmedCount)
                .responses(responseList)
                .build();

        return ApiResponse.success("Matched donor responses retrieved successfully", container);
    }

    @Override
    @Transactional
    public ApiResponse<com.bloodbridge.dto.response.HospitalDonorResponseDTO> confirmDonor(String email, Long requestId, Long matchedDonorId) {
        log.info("Hospital user email {} confirming matched donor #{} for request #{}", email, matchedDonorId, requestId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + requestId));

        if (user.getRole() != Role.ADMIN) {
            Hospital hospital = findHospitalByEmail(email);
            if (bloodRequest.getHospital() == null || !bloodRequest.getHospital().getId().equals(hospital.getId())) {
                throw new AccessDeniedException("Access denied: You do not own blood request #" + requestId);
            }
        }

        if (bloodRequest.isTerminalState()) {
            throw new com.bloodbridge.exception.InvalidRequestStateException("Cannot confirm donor for request in terminal state: " + bloodRequest.getStatus());
        }

        MatchedEmergencyDonor med = matchedEmergencyDonorRepository.findById(matchedDonorId)
                .orElseGet(() -> matchedEmergencyDonorRepository.findByBloodRequestIdAndDonorId(requestId, matchedDonorId)
                        .orElseThrow(() -> new IllegalArgumentException("Matched donor record not found for ID: " + matchedDonorId)));

        if (!med.getBloodRequest().getId().equals(requestId)) {
            throw new IllegalArgumentException("Matched donor ID #" + matchedDonorId + " does not belong to request #" + requestId);
        }

        if (med.getStatus() != com.bloodbridge.enums.MatchedEmergencyDonorStatus.ACCEPTED &&
            med.getStatus() != com.bloodbridge.enums.MatchedEmergencyDonorStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only ACCEPTED donors can be confirmed for an emergency request. Current status: " + med.getStatus());
        }

        med.setConfirmed(true);
        med.setConfirmedAt(LocalDateTime.now());
        med.setConfirmedByUserId(user.getId());
        med.setStatus(com.bloodbridge.enums.MatchedEmergencyDonorStatus.CONFIRMED);
        med.setFulfillmentStatus("FULFILLMENT_IN_PROGRESS");
        MatchedEmergencyDonor savedMed = matchedEmergencyDonorRepository.save(med);

        bloodRequest.setStatus(RequestStatus.FULFILLMENT_IN_PROGRESS);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        DonorProfile donor = savedMed.getDonor();
        Hospital hosp = savedRequest.getHospital();

        if (donor != null && notificationService != null) {
            notificationService.notifyDonor(
                    donor,
                    "You Have Been Selected for an Emergency Blood Request",
                    String.format("Hospital %s selected you for Emergency Request #%d (%s, %d unit(s)). Please report to hospital.",
                            hosp != null ? hosp.getHospitalName() : "Hospital",
                            savedRequest.getId(),
                            savedRequest.getBloodGroupNeeded(),
                            savedRequest.getUnitsRequired()),
                    com.bloodbridge.enums.NotificationType.DONOR_CONFIRMED,
                    "/donor/requests",
                    savedRequest,
                    hosp
            );
        }

        if (donor != null && donor.getUser() != null && donor.getUser().getEmail() != null && emailService != null) {
            try {
                String bgFormatted = savedRequest.getBloodGroupNeeded() != null ?
                        savedRequest.getBloodGroupNeeded().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";
                String emailSubject = "You Have Been Selected for an Emergency Blood Request";
                String emailBody = String.format(
                        "Hello %s,\n\n" +
                        "Great news! You have been selected by %s for an emergency blood donation request.\n\n" +
                        "Request ID: #%d\n" +
                        "Blood Group: %s\n" +
                        "Units Required: %d\n" +
                        "Hospital Name: %s\n" +
                        "Hospital Address: %s\n" +
                        "Hospital Phone: %s\n\n" +
                        "Please open your BloodBridge dashboard to navigate to the hospital.\n\n" +
                        "Thank you for saving lives,\nBloodBridge Team",
                        donor.getUser().getFullName() != null ? donor.getUser().getFullName() : "Donor",
                        hosp != null ? hosp.getHospitalName() : "Hospital",
                        savedRequest.getId(),
                        bgFormatted,
                        savedRequest.getUnitsRequired() != null ? savedRequest.getUnitsRequired() : 1,
                        hosp != null ? hosp.getHospitalName() : "Hospital",
                        hosp != null && hosp.getAddress() != null ? hosp.getAddress() : "Address N/A",
                        hosp != null && hosp.getPhoneNumber() != null ? hosp.getPhoneNumber() : "Phone N/A"
                );
                emailService.sendEmail(donor.getUser().getEmail(), emailSubject, emailBody);
            } catch (Exception e) {
                log.error("[EMAIL-DONOR-CONFIRM-ERROR] Failed to send email to donor: {}", e.getMessage());
            }
        }

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.DONOR_CONFIRMED,
                    "BLOOD_REQUEST",
                    savedRequest.getId(),
                    "Donor Confirmed for Request",
                    String.format("Donor %s confirmed for Blood Request #%d", donor != null && donor.getUser() != null ? donor.getUser().getFullName() : "Donor", savedRequest.getId()),
                    savedMed.getId()
            );
            realtimeService.publishHospitalUpdate(hosp != null ? hosp.getId() : 0L, event);
            realtimeService.publishAdminDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to broadcast real-time DONOR_CONFIRMED event: {}", e.getMessage());
        }

        auditLoggerService.logEvent("DONOR_CONFIRMED", email, "Confirmed matched donor #" + savedMed.getId() + " for request #" + savedRequest.getId());
        auditLoggerService.logEvent("FULFILLMENT_STARTED", email, "Emergency request #" + savedRequest.getId() + " moved to FULFILLMENT_IN_PROGRESS");

        User donorUser = donor != null ? donor.getUser() : null;
        String donorName = donorUser != null && donorUser.getFullName() != null ? donorUser.getFullName() : (donor != null ? donor.getEmail() : "Valued Donor");
        String donorEmail = donorUser != null ? donorUser.getEmail() : (donor != null ? donor.getEmail() : "");
        String donorPhone = donorUser != null ? donorUser.getPhoneNumber() : (donor != null && donor.getAlternatePhoneNumber() != null ? donor.getAlternatePhoneNumber() : "");
        String bgFormatted = (donor != null && donor.getBloodGroup() != null) ?
                donor.getBloodGroup().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";

        HospitalDonorResponseDTO dto = HospitalDonorResponseDTO.builder()
                .matchedDonorId(savedMed.getId())
                .donorId(donor != null ? donor.getId() : null)
                .donorName(donorName)
                .bloodGroup(bgFormatted)
                .email(donorEmail)
                .phone(donorPhone)
                .donorStatus(donor != null && donor.getStatus() != null ? donor.getStatus() : "ACTIVE")
                .distanceKm(savedMed.getDistanceKm())
                .tierGroup(savedMed.getMatchingGroup())
                .matchingGroup(savedMed.getMatchingGroup())
                .responseStatus(savedMed.getStatus().name())
                .confirmed(true)
                .confirmedAt(savedMed.getConfirmedAt())
                .acceptedAt(savedMed.getAcceptedAt())
                .fulfillmentStatus(savedMed.getFulfillmentStatus())
                .createdAt(savedMed.getCreatedAt())
                .build();

        return ApiResponse.success("Donor confirmed successfully for emergency request", dto);
    }

    @Override
    @Transactional
    public ApiResponse<BloodRequestResponse> startFulfillment(String email, Long requestId) {
        log.info("Hospital user email {} starting fulfillment for request #{}", email, requestId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + requestId));

        if (user.getRole() != Role.ADMIN) {
            Hospital hospital = findHospitalByEmail(email);
            if (bloodRequest.getHospital() == null || !bloodRequest.getHospital().getId().equals(hospital.getId())) {
                throw new AccessDeniedException("Access denied: You do not own blood request #" + requestId);
            }
        }

        if (bloodRequest.isTerminalState()) {
            throw new com.bloodbridge.exception.InvalidRequestStateException("Cannot start fulfillment for request in terminal state: " + bloodRequest.getStatus());
        }

        bloodRequest.setStatus(RequestStatus.FULFILLMENT_IN_PROGRESS);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.FULFILLMENT_STARTED,
                    "BLOOD_REQUEST",
                    savedRequest.getId(),
                    "Fulfillment Started",
                    "Blood request #" + savedRequest.getId() + " is now in progress.",
                    bloodRequestMapper.toResponse(savedRequest)
            );
            realtimeService.publishHospitalUpdate(savedRequest.getHospital().getId(), event);
            realtimeService.publishAdminDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to publish FULFILLMENT_STARTED STOMP event: {}", e.getMessage());
        }

        auditLoggerService.logEvent("FULFILLMENT_STARTED", email, "Request #" + savedRequest.getId() + " fulfillment started");

        return ApiResponse.success("Blood request fulfillment started", bloodRequestMapper.toResponse(savedRequest));
    }

    @Override
    @Transactional
    public ApiResponse<BloodRequestResponse> completeEmergencyRequest(String email, Long requestId) {
        log.info("Hospital user email {} completing emergency request #{}", email, requestId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + requestId));

        if (user.getRole() != Role.ADMIN) {
            Hospital hospital = findHospitalByEmail(email);
            if (bloodRequest.getHospital() == null || !bloodRequest.getHospital().getId().equals(hospital.getId())) {
                throw new AccessDeniedException("Access denied: You do not own blood request #" + requestId);
            }
        }

        if (bloodRequest.getStatus() == RequestStatus.COMPLETED) {
            return ApiResponse.success("Blood request is already completed", bloodRequestMapper.toResponse(bloodRequest));
        }

        bloodRequest.setStatus(RequestStatus.COMPLETED);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        List<MatchedEmergencyDonor> matchedDonors = matchedEmergencyDonorRepository.findByBloodRequestId(requestId);
        for (MatchedEmergencyDonor med : matchedDonors) {
            if (med.getStatus() == com.bloodbridge.enums.MatchedEmergencyDonorStatus.CONFIRMED ||
                med.getStatus() == com.bloodbridge.enums.MatchedEmergencyDonorStatus.ACCEPTED ||
                med.getStatus() == com.bloodbridge.enums.MatchedEmergencyDonorStatus.FULFILLMENT_IN_PROGRESS) {
                LocalDateTime completionTime = LocalDateTime.now();
                med.setStatus(com.bloodbridge.enums.MatchedEmergencyDonorStatus.COMPLETED);
                med.setCompletedAt(completionTime);
                med.setFulfillmentStatus("COMPLETED");
                matchedEmergencyDonorRepository.save(med);

                if (med.getDonor() != null) {
                    List<Donation> donorDonations = donationRepository.findByDonorIdAndBloodRequestIdAndStatusIn(
                            med.getDonor().getId(), requestId,
                            List.of(com.bloodbridge.enums.DonationStatus.ACCEPTED, com.bloodbridge.enums.DonationStatus.CONFIRMED, com.bloodbridge.enums.DonationStatus.PENDING)
                    );
                    Donation targetDonation = null;
                    if (donorDonations.isEmpty()) {
                        Donation newDonation = Donation.builder()
                                .donor(med.getDonor())
                                .patient(savedRequest.getPatient())
                                .hospital(savedRequest.getHospital())
                                .bloodRequest(savedRequest)
                                .donationDate(completionTime.toLocalDate())
                                .completedAt(completionTime)
                                .unitsDonated(savedRequest.getUnitsRequired() != null ? savedRequest.getUnitsRequired() : 1)
                                .status(com.bloodbridge.enums.DonationStatus.COMPLETED)
                                .remarks("Emergency blood donation completed for Request #" + requestId)
                                .build();
                        newDonation = donationRepository.save(newDonation);
                        newDonation.setCertificateId("CERT-BB-" + completionTime.getYear() + "-" + String.format("%06d", newDonation.getId()));
                        targetDonation = donationRepository.save(newDonation);
                    } else {
                        for (Donation d : donorDonations) {
                            d.setStatus(com.bloodbridge.enums.DonationStatus.COMPLETED);
                            d.setCompletedAt(completionTime);
                            d.setDonationDate(completionTime.toLocalDate());
                            if (d.getCertificateId() == null || d.getCertificateId().isBlank()) {
                                d.setCertificateId("CERT-BB-" + completionTime.getYear() + "-" + String.format("%06d", d.getId()));
                            }
                            targetDonation = donationRepository.save(d);
                        }
                    }
                    if (donationService != null) {
                        donationService.updateDonorStatistics(med.getDonor(), completionTime.toLocalDate());
                    }

                    // Dispatch Donation Certificate PDF via Email
                    if (targetDonation != null && emailService != null && certificateService != null && med.getDonor() != null && med.getDonor().getUser() != null) {
                        try {
                            String donorEmail = med.getDonor().getUser().getEmail();
                            if (donorEmail != null && !donorEmail.isBlank()) {
                                byte[] pdfBytes = certificateService.generateCertificatePdf(targetDonation);
                                String donorName = med.getDonor().getUser().getFullName();
                                String hospName = savedRequest.getHospital() != null ? savedRequest.getHospital().getHospitalName() : "SriSai Multi-speciality Hospital";
                                String bgStr = med.getDonor().getBloodGroup() != null ? med.getDonor().getBloodGroup().name() : "N/A";
                                String donDateStr = targetDonation.getDonationDate() != null ? targetDonation.getDonationDate().toString() : completionTime.toLocalDate().toString();
                                emailService.sendDonationCertificateEmail(
                                        donorEmail, donorName, hospName, bgStr, targetDonation.getUnitsDonated(), donDateStr, targetDonation.getCertificateId(), pdfBytes
                                );
                            }
                        } catch (Exception e) {
                            log.error("[EMAIL-CERTIFICATE-ERROR] Non-blocking error generating/sending certificate email for Emergency Request #{}: {}", requestId, e.getMessage());
                        }
                    }
                }

                if (med.getDonor() != null && notificationService != null) {
                    notificationService.notifyDonor(
                            med.getDonor(),
                            "Emergency Blood Request Completed",
                            String.format("Emergency Request #%d has been completed successfully. Thank you for your support!", requestId),
                            com.bloodbridge.enums.NotificationType.REQUEST_COMPLETED,
                            "/donor/requests",
                            savedRequest,
                            savedRequest.getHospital()
                    );
                }

                if (med.getDonor() != null && med.getDonor().getUser() != null && med.getDonor().getUser().getEmail() != null && emailService != null) {
                    try {
                        String emailSubject = "Emergency Blood Request Completed";
                        String emailBody = String.format(
                                "Hello %s,\n\n" +
                                "The emergency blood request #%d at %s has been completed.\n\n" +
                                "Thank you for being an active donor with BloodBridge!\n\n" +
                                "Best regards,\nBloodBridge Team",
                                med.getDonor().getUser().getFullName() != null ? med.getDonor().getUser().getFullName() : "Donor",
                                requestId,
                                savedRequest.getHospital() != null ? savedRequest.getHospital().getHospitalName() : "Hospital"
                        );
                        emailService.sendEmail(med.getDonor().getUser().getEmail(), emailSubject, emailBody);
                    } catch (Exception e) {
                        log.error("[EMAIL-DONOR-COMPLETE-ERROR] Failed to send email to donor: {}", e.getMessage());
                    }
                }
            }
        }

        try {
            RealtimeEventDTO event = RealtimeEventDTO.of(
                    RealtimeEventType.REQUEST_COMPLETED,
                    "BLOOD_REQUEST",
                    savedRequest.getId(),
                    "Emergency Request Completed",
                    "Blood request #" + savedRequest.getId() + " has been completed successfully.",
                    bloodRequestMapper.toResponse(savedRequest)
            );
            realtimeService.publishHospitalUpdate(savedRequest.getHospital().getId(), event);
            realtimeService.publishAdminDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to publish REQUEST_COMPLETED STOMP event: {}", e.getMessage());
        }

        auditLoggerService.logEvent("REQUEST_FULFILLED", email, "Emergency request #" + savedRequest.getId() + " fulfilled");
        auditLoggerService.logEvent("REQUEST_COMPLETED", email, "Emergency request #" + savedRequest.getId() + " marked COMPLETED");

        return ApiResponse.success("Emergency blood request completed successfully", bloodRequestMapper.toResponse(savedRequest));
    }
}
