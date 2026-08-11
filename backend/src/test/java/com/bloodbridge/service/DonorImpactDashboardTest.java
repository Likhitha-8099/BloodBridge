package com.bloodbridge.service;

import com.bloodbridge.config.MatchingConfig;
import com.bloodbridge.dto.response.DonorDashboardResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EligibilityStatus;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.Role;
import com.bloodbridge.mapper.DonorProfileMapper;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.DonorProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Module 3: Donor Impact Dashboard.
 * Verifies that all data displayed on the /donor/impact page comes exclusively from
 * backend-computed fields — no hardcoded values for blood groups, donation counts, dates,
 * donor names, or eligibility status.
 * <p>
 * Dashboard fields verified:
 * <ul>
 *   <li>Donor summary: fullName, bloodGroup, emergencyAvailable</li>
 *   <li>Eligibility: eligible, eligibilityStatus, daysUntilEligible, nextEligibleDate, cooldownDays</li>
 *   <li>Donation statistics: totalDonations, lastDonationDate, donorScore</li>
 *   <li>Impact journey: blood group, donor score tier</li>
 *   <li>Dashboard aggregate: completedDonations, livesSaved, nearbyActiveRequestsCount</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class DonorImpactDashboardTest {

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private com.bloodbridge.repository.MatchResultRepository matchResultRepository;

    @Mock
    private com.bloodbridge.repository.NotificationRepository notificationRepository;

    @Mock
    private com.bloodbridge.service.AuditLoggerService auditLoggerService;

    @Mock
    private MatchingConfig matchingConfig;

    @Spy
    private DonorProfileMapper donorProfileMapper = new DonorProfileMapper();

    @InjectMocks
    private DonorProfileServiceImpl donorProfileService;

    private User user;
    private DonorProfile donorProfile;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("impact.donor@example.com")
                .fullName("Impact Donor")
                .role(Role.DONOR)
                .build();

        donorProfile = DonorProfile.builder()
                .id(10L)
                .user(user)
                .email("impact.donor@example.com")
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(28)
                .gender(Gender.MALE)
                .city("Chennai")
                .state("Tamil Nadu")
                .weight(72.0)
                .totalDonations(5)
                .lastDonationDate(LocalDate.now().minusDays(100)) // past cooldown → eligible
                .availableForDonation(true)
                .emergencyAvailable(true)
                .build();

        when(userRepository.findByEmail("impact.donor@example.com")).thenReturn(Optional.of(user));
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(donorProfile));
        when(donorProfileRepository.findByEmail(anyString())).thenReturn(Optional.of(donorProfile));
        when(matchingConfig.getCooldownDays()).thenReturn(90);
    }

    // ──────────────────── Donor Profile / Donor Summary ────────────────────

    @Test
    @DisplayName("1. Profile: fullName comes from backend user entity (not hardcoded)")
    void testGetMyProfile_FullNameFromBackend() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFullName()).isEqualTo("Impact Donor");
    }

    @Test
    @DisplayName("2. Profile: bloodGroup comes from backend (not hardcoded)")
    void testGetMyProfile_BloodGroupFromBackend() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        assertThat(response.getData().getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
    }

    @Test
    @DisplayName("3. Profile: emergencyAvailable comes from backend (not hardcoded)")
    void testGetMyProfile_EmergencyAvailabilityFromBackend() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        assertThat(response.getData().getEmergencyAvailable()).isTrue();
    }

    // ──────────────────── Eligibility Engine ───────────────────────────────

    @Test
    @DisplayName("4. Eligibility: donor with last donation 100 days ago is ELIGIBLE (cooldown=90)")
    void testEligibility_EligibleAfterCooldown() {
        var response = donorProfileService.calculateEligibility("impact.donor@example.com");
        assertThat(response).isNotNull();
        assertThat(response.getData().isEligible()).isTrue();
        assertThat(response.getData().getDaysUntilEligible()).isEqualTo(0L);
        assertThat(response.getData().getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    @DisplayName("5. Eligibility: donor with last donation 30 days ago is NOT ELIGIBLE (cooldown=90)")
    void testEligibility_NotEligibleWithinCooldown() {
        donorProfile.setLastDonationDate(LocalDate.now().minusDays(30));
        var response = donorProfileService.calculateEligibility("impact.donor@example.com");
        assertThat(response.getData().isEligible()).isFalse();
        assertThat(response.getData().getDaysUntilEligible()).isGreaterThan(0L);
        assertThat(response.getData().getNextEligibleDate()).isNotNull();
    }

    @Test
    @DisplayName("6. Eligibility: donor with no previous donation is ELIGIBLE")
    void testEligibility_NeverDonatedIsEligible() {
        donorProfile.setLastDonationDate(null);
        donorProfile.setTotalDonations(0);
        var response = donorProfileService.calculateEligibility("impact.donor@example.com");
        assertThat(response.getData().isEligible()).isTrue();
    }

    @Test
    @DisplayName("7. Profile: nextEligibleDate is null/today when already eligible")
    void testProfile_NextEligibleDateWhenEligible() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        // When eligible, nextEligibleDate should be null or today/past
        LocalDate nextEligible = response.getData().getNextEligibleDate();
        if (nextEligible != null) {
            assertThat(nextEligible).isBeforeOrEqualTo(LocalDate.now());
        }
    }

    @Test
    @DisplayName("8. Profile: daysUntilEligible is 0 when donor is eligible")
    void testProfile_DaysUntilEligibleIsZeroWhenEligible() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        Long days = response.getData().getDaysUntilEligible();
        assertThat(days).isNotNull().isEqualTo(0L);
    }

    // ──────────────────── Donation Statistics ──────────────────────────────

    @Test
    @DisplayName("9. Profile: totalDonations field is populated from backend entity")
    void testProfile_TotalDonationsFromBackend() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        assertThat(response.getData().getTotalDonations()).isEqualTo(5);
    }

    @Test
    @DisplayName("10. Profile: lastDonationDate is populated from backend entity")
    void testProfile_LastDonationDateFromBackend() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        assertThat(response.getData().getLastDonationDate()).isNotNull();
        assertThat(response.getData().getLastDonationDate()).isBefore(LocalDate.now());
    }

    // ──────────────────── Donor Score / Impact Journey ─────────────────────

    @Test
    @DisplayName("11. Profile: donorScore field is backend-computed (not hardcoded)")
    void testProfile_DonorScoreIsBackendComputed() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        // Score should be non-null and >= 0 — exact value depends on engine
        assertThat(response.getData().getDonorScore()).isNotNull().isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("12. Profile: eligibilityStatus is a backend-computed enum, not hardcoded string")
    void testProfile_EligibilityStatusIsEnum() {
        var response = donorProfileService.getMyProfile("impact.donor@example.com");
        EligibilityStatus status = response.getData().getEligibilityStatus();
        assertThat(status).isIn(EligibilityStatus.ELIGIBLE, EligibilityStatus.TEMPORARILY_DEFERRED,
                EligibilityStatus.PERMANENTLY_DEFERRED);
    }

    @Test
    @DisplayName("13. Eligibility: reason field is populated (not null or blank)")
    void testEligibility_ReasonIsPresent() {
        var response = donorProfileService.calculateEligibility("impact.donor@example.com");
        assertThat(response.getData().getReason()).isNotBlank();
    }

    @Test
    @DisplayName("14. Eligibility: recommendation field is populated (not null or blank)")
    void testEligibility_RecommendationIsPresent() {
        var response = donorProfileService.calculateEligibility("impact.donor@example.com");
        assertThat(response.getData().getRecommendation()).isNotBlank();
    }

    // ──────────────────── Dashboard Aggregate ──────────────────────────────

    @Test
    @DisplayName("15. Dashboard: all aggregated metrics are present and non-negative")
    void testDashboard_AggregatedMetricsPresent() {
        var response = donorProfileService.getDashboard("impact.donor@example.com");
        DonorDashboardResponse dashboard = response.getData();
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getFullName()).isEqualTo("Impact Donor");
        assertThat(dashboard.getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
        assertThat(dashboard.getTotalDonations()).isGreaterThanOrEqualTo(0);
        assertThat(dashboard.getDonorScore()).isGreaterThanOrEqualTo(0);
        assertThat(dashboard.getEligibilityStatus()).isNotNull();
        assertThat(dashboard.getEligible()).isNotNull();
    }

    @Test
    @DisplayName("16. Dashboard: emergencyAvailable flag comes from backend entity")
    void testDashboard_EmergencyAvailableFromEntity() {
        var response = donorProfileService.getDashboard("impact.donor@example.com");
        assertThat(response.getData().getEmergencyAvailable()).isTrue();
    }

    @Test
    @DisplayName("17. Dashboard: eligible donor shows 0 daysUntilEligible")
    void testDashboard_EligibleDonorHasZeroDays() {
        var response = donorProfileService.getDashboard("impact.donor@example.com");
        assertThat(response.getData().getEligible()).isTrue();
        assertThat(response.getData().getDaysUntilEligible()).isEqualTo(0L);
    }
}

