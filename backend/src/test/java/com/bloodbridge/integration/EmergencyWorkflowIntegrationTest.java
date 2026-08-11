package com.bloodbridge.integration;

import com.bloodbridge.dto.request.AcceptEmergencyRequestDTO;
import com.bloodbridge.dto.response.EmergencyResponseDTO;
import com.bloodbridge.dto.response.HospitalEmergencyLiveStatsDTO;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EmergencyResponseStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.notification.NotificationOrchestrator;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.EmergencyResponseService;
import com.bloodbridge.service.EmergencyTimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmergencyWorkflowIntegrationTest {

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private EmergencyResponseService emergencyResponseService;

    @Autowired
    private EmergencyTimelineService timelineService;

    @Autowired
    private NotificationOrchestrator notificationOrchestrator;

    private User hospitalUser;
    private Hospital hospital;
    private User donorUser;
    private DonorProfile donorProfile;
    private BloodRequest bloodRequest;

    @BeforeEach
    void setUp() {
        String ts = String.valueOf(System.currentTimeMillis());
        hospitalUser = userRepository.save(User.builder().fullName("City General Hospital").email("hosp_" + ts + "@example.com").phoneNumber("+919876543210").role(com.bloodbridge.enums.Role.HOSPITAL).password("pass").active(true).build());
        hospital = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser)
                .hospitalName("City General Hospital")
                .registrationNumber("REG-" + ts)
                .phoneNumber("+919876543210")
                .email(hospitalUser.getEmail())
                .address("123 MG Road")
                .latitude(12.9698)
                .longitude(77.7499)
                .city("Bangalore")
                .state("Karnataka")
                .build());

        donorUser = userRepository.save(User.builder().fullName("John Hero").email("donor_" + ts + "@example.com").phoneNumber("+919876543211").role(com.bloodbridge.enums.Role.DONOR).password("pass").active(true).build());
        donorProfile = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUser)
                .email(donorUser.getEmail())
                .bloodGroup(BloodGroup.O_POSITIVE)
                .latitude(12.9716)
                .longitude(77.5946)
                .age(25)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .weight(70.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .build());

        bloodRequest = bloodRequestRepository.save(BloodRequest.builder()
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.O_POSITIVE)
                .unitsRequired(1)
                .urgencyLevel(com.bloodbridge.enums.UrgencyLevel.CRITICAL)
                .requestDate(LocalDateTime.now())
                .requiredByDate(java.time.LocalDate.now().plusDays(1))
                .status(RequestStatus.PENDING)
                .reason("Urgent Operation")
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    void executeFullEmergencyResponsePipeline() {
        // 1. Record creation milestone
        timelineService.recordEvent(bloodRequest.getId(), "EMERGENCY_CREATED", "Request Created", "Emergency created", hospitalUser.getEmail(), "{}");

        // 2. Dispatch Notification via Orchestrator Strategy Pattern
        NotificationPayload payload = NotificationPayload.builder()
                .emergencyRequestId(bloodRequest.getId())
                .recipientDonor(donorProfile)
                .recipientUser(donorUser)
                .bloodRequest(bloodRequest)
                .title("🚨 Emergency Blood Needed")
                .message("O+ Blood Needed Immediately")
                .build();
        notificationOrchestrator.dispatchNotification(payload);

        // 3. Donor Accepts Emergency Request
        AcceptEmergencyRequestDTO acceptDTO = AcceptEmergencyRequestDTO.builder()
                .emergencyRequestId(bloodRequest.getId())
                .etaMinutes(15)
                .remarks("En route now")
                .build();

        EmergencyResponseDTO acceptResult = emergencyResponseService.acceptEmergencyRequest(donorUser.getEmail(), acceptDTO);
        assertNotNull(acceptResult);
        assertEquals(EmergencyResponseStatus.ACCEPTED, acceptResult.getStatus());
        assertTrue(acceptResult.getGoogleMapsUrl().contains("google.com/maps/dir/"));

        // 4. Donor Journey State Transitions
        EmergencyResponseDTO travelResult = emergencyResponseService.startTravel(donorUser.getEmail(), bloodRequest.getId());
        assertEquals(EmergencyResponseStatus.STARTED_TRAVEL, travelResult.getStatus());

        EmergencyResponseDTO reachResult = emergencyResponseService.reachHospital(donorUser.getEmail(), bloodRequest.getId());
        assertEquals(EmergencyResponseStatus.REACHED_HOSPITAL, reachResult.getStatus());

        EmergencyResponseDTO completeResult = emergencyResponseService.completeDonation(donorUser.getEmail(), bloodRequest.getId());
        assertEquals(EmergencyResponseStatus.DONATION_COMPLETED, completeResult.getStatus());

        // 5. Verify Live Telemetry Stats
        HospitalEmergencyLiveStatsDTO liveStats = emergencyResponseService.getHospitalLiveStats(bloodRequest.getId());
        assertNotNull(liveStats);
        assertEquals(1, liveStats.getUnitsCollected());
        assertEquals(0, liveStats.getRemainingUnitsNeeded());

        // 6. Verify Timeline Log Records
        List<EmergencyTimelineEvent> timeline = timelineService.getTimelineForRequest(bloodRequest.getId());
        assertTrue(timeline.size() >= 4);
    }
}
