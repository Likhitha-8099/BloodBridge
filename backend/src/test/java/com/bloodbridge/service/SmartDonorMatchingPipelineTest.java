package com.bloodbridge.service;

import com.bloodbridge.config.MatchingConfig;
import com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchingAnalytics;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmergencyResponseRepository;
import com.bloodbridge.repository.MatchingAnalyticsRepository;
import com.bloodbridge.service.impl.LocationServiceImpl;
import com.bloodbridge.service.impl.SmartDonorMatchingPipelineServiceImpl;
import com.bloodbridge.util.BloodCompatibilityMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit and integration test suite for Phase 3D.1 Smart Donor Matching Pipeline.
 */
class SmartDonorMatchingPipelineTest {

    private MatchingConfig matchingConfig;
    private BloodRequestRepository bloodRequestRepository;
    private DonorProfileRepository donorProfileRepository;
    private EmergencyResponseRepository emergencyResponseRepository;
    private MatchingAnalyticsRepository matchingAnalyticsRepository;
    private com.bloodbridge.repository.MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private LocationService locationService;
    private com.bloodbridge.service.NotificationService notificationService;
    private com.bloodbridge.service.RealtimeService realtimeService;
    private com.bloodbridge.notification.NotificationOrchestrator notificationOrchestrator;

    private SmartDonorMatchingPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        matchingConfig = new MatchingConfig(); // Defaults: primary=50, secondary=75, tertiary=100, cooldown=90
        bloodRequestRepository = mock(BloodRequestRepository.class);
        donorProfileRepository = mock(DonorProfileRepository.class);
        emergencyResponseRepository = mock(EmergencyResponseRepository.class);
        matchingAnalyticsRepository = mock(MatchingAnalyticsRepository.class);
        matchedEmergencyDonorRepository = mock(com.bloodbridge.repository.MatchedEmergencyDonorRepository.class);
        locationService = new LocationServiceImpl();
        notificationService = mock(com.bloodbridge.service.NotificationService.class);
        realtimeService = mock(com.bloodbridge.service.RealtimeService.class);
        notificationOrchestrator = mock(com.bloodbridge.notification.NotificationOrchestrator.class);

        pipelineService = new SmartDonorMatchingPipelineServiceImpl(
                matchingConfig,
                bloodRequestRepository,
                donorProfileRepository,
                emergencyResponseRepository,
                matchingAnalyticsRepository,
                matchedEmergencyDonorRepository,
                locationService,
                notificationService,
                realtimeService,
                notificationOrchestrator
        );
    }

    @Test
    @DisplayName("Stage 2: Medical Compatibility Matrix for all 8 blood types")
    void testMedicalCompatibilityMatrix_All8BloodGroups() {
        // O- is universal donor
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.O_NEGATIVE, BloodGroup.A_POSITIVE));
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.O_NEGATIVE, BloodGroup.O_POSITIVE));
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.O_NEGATIVE, BloodGroup.AB_NEGATIVE));
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.O_NEGATIVE, BloodGroup.B_NEGATIVE));

        // AB+ is universal recipient (can receive from all 8)
        for (BloodGroup bg : BloodGroup.values()) {
            assertTrue(BloodCompatibilityMatrix.isCompatible(bg, BloodGroup.AB_POSITIVE), "AB+ must accept " + bg);
        }

        // A+ can receive from A+, A-, O+, O-
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.A_POSITIVE, BloodGroup.A_POSITIVE));
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.A_NEGATIVE, BloodGroup.A_POSITIVE));
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.O_POSITIVE, BloodGroup.A_POSITIVE));
        assertFalse(BloodCompatibilityMatrix.isCompatible(BloodGroup.B_POSITIVE, BloodGroup.A_POSITIVE));
        assertFalse(BloodCompatibilityMatrix.isCompatible(BloodGroup.AB_POSITIVE, BloodGroup.A_POSITIVE));

        // O- recipient can only receive from O-
        assertTrue(BloodCompatibilityMatrix.isCompatible(BloodGroup.O_NEGATIVE, BloodGroup.O_NEGATIVE));
        assertFalse(BloodCompatibilityMatrix.isCompatible(BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE));
        assertFalse(BloodCompatibilityMatrix.isCompatible(BloodGroup.A_NEGATIVE, BloodGroup.O_NEGATIVE));
    }

    @Test
    @DisplayName("Stage 1: Validation Fails for Unapproved Hospital or Invalid Request")
    void testStage1Validation_UnapprovedHospital_ThrowsException() {
        Hospital unapprovedHospital = Hospital.builder().id(1L).verified(false).verificationStatus("PENDING").build();
        BloodRequest invalidReq = BloodRequest.builder().id(100L).bloodGroupNeeded(BloodGroup.A_POSITIVE).unitsRequired(2).hospital(unapprovedHospital).build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> pipelineService.executePipeline(invalidReq));
        assertTrue(ex.getMessage().contains("pending approval or unverified"));
    }

    @Test
    @DisplayName("Stage 3 & 4: Eligibility Filtering and Distance Grouping A, B, C, D")
    void testPipeline_EligibilityAndDistanceGrouping_GroupsCorrectly() {
        Hospital hospital = Hospital.builder()
                .id(10L)
                .verified(true)
                .verificationStatus("APPROVED")
                .latitude(12.9716) // Bangalore
                .longitude(77.5946)
                .build();

        BloodRequest request = BloodRequest.builder()
                .id(200L)
                .bloodGroupNeeded(BloodGroup.B_POSITIVE)
                .unitsRequired(3)
                .hospital(hospital)
                .build();

        // Donor 1: Group A (16 KM)
        User u1 = User.builder().id(1L).email("d1@test.com").active(true).emailVerified(true).build();
        DonorProfile d1 = DonorProfile.builder().id(101L).user(u1).bloodGroup(BloodGroup.B_POSITIVE).availableForDonation(true).emergencyAvailable(true).status("ACTIVE").latitude(12.9698).longitude(77.7499).lastDonationDate(LocalDate.now().minusDays(120)).build();

        // Donor 2: Group B (~60 KM)
        User u2 = User.builder().id(2L).email("d2@test.com").active(true).emailVerified(true).build();
        DonorProfile d2 = DonorProfile.builder().id(102L).user(u2).bloodGroup(BloodGroup.O_POSITIVE).availableForDonation(true).emergencyAvailable(true).status("ACTIVE").latitude(12.5218).longitude(76.8951).lastDonationDate(LocalDate.now().minusDays(150)).build();

        // Donor 3: Group C (~85 KM)
        User u3 = User.builder().id(3L).email("d3@test.com").active(true).emailVerified(true).build();
        DonorProfile d3 = DonorProfile.builder().id(103L).user(u3).bloodGroup(BloodGroup.B_NEGATIVE).availableForDonation(true).emergencyAvailable(true).status("ACTIVE").latitude(12.2958).longitude(76.6394).lastDonationDate(LocalDate.now().minusDays(100)).build();

        // Donor 4: Incompatible Blood Group (A_POSITIVE for B_POSITIVE request) -> Skipped in Stage 2
        User u4 = User.builder().id(4L).email("d4@test.com").active(true).emailVerified(true).build();
        DonorProfile d4 = DonorProfile.builder().id(104L).user(u4).bloodGroup(BloodGroup.A_POSITIVE).availableForDonation(true).emergencyAvailable(true).status("ACTIVE").latitude(12.9716).longitude(77.5946).build();

        // Donor 5: Cooldown violation (donated 20 days ago) -> Filtered in Stage 3
        User u5 = User.builder().id(5L).email("d5@test.com").active(true).emailVerified(true).build();
        DonorProfile d5 = DonorProfile.builder().id(105L).user(u5).bloodGroup(BloodGroup.B_POSITIVE).availableForDonation(true).emergencyAvailable(true).status("ACTIVE").latitude(12.9716).longitude(77.5946).lastDonationDate(LocalDate.now().minusDays(20)).build();

        when(donorProfileRepository.findAll()).thenReturn(List.of(d1, d2, d3, d4, d5));

        SmartDonorMatchingPipelineDTO result = pipelineService.executePipeline(request);

        assertNotNull(result);
        assertEquals(4, result.getTotalCompatibleDonors(), "d1, d2, d3, d5 are medically compatible blood groups");
        assertEquals(3, result.getTotalEligibleDonors(), "d5 filtered by cooldown, d4 filtered by compatibility");

        assertEquals(1, result.getGroupA().getDonorCount(), "d1 is Group A (0-50 KM)");
        assertEquals("Immediate Matches", result.getGroupA().getSectionTitle());

        assertEquals(0, result.getGroupB().getDonorCount(), "No Group B donors (50-75 KM)");
        assertEquals("Nearby Compatible", result.getGroupB().getSectionTitle());

        assertEquals(1, result.getGroupC().getDonorCount(), "d2 is Group C (75-100 KM)");
        assertEquals("Extended Compatible", result.getGroupC().getSectionTitle());

        assertEquals(1, result.getGroupD().getDonorCount(), "d3 is Group D (> 100 KM)");

        // Verify notification template assignments
        assertTrue(result.getGroupA().getNotificationSubjectTemplate().contains("URGENT"));
        assertTrue(result.getGroupB().getNotificationSubjectTemplate().contains("Blood Donation Request"));

        // Verify Stage 8 analytics persisted
        verify(matchingAnalyticsRepository, times(1)).save(any(MatchingAnalytics.class));
    }
}
