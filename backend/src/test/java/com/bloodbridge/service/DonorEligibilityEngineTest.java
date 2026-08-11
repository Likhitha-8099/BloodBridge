package com.bloodbridge.service;

import com.bloodbridge.config.MatchingConfig;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.dto.response.EligibilityResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EligibilityStatus;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.mapper.DonorProfileMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.impl.DonorProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit & Eligibility Calculation Tests for {@link DonorProfileServiceImpl} and Smart Eligibility Engine.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class DonorEligibilityEngineTest {

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuditLoggerService auditLoggerService;

    @Mock
    private MatchingConfig matchingConfig;

    @Spy
    private DonorProfileMapper donorProfileMapper = new DonorProfileMapper();

    @InjectMocks
    private DonorProfileServiceImpl donorProfileService;

    private User sampleUser;
    private DonorProfile freshDonor;
    private DonorProfile recentDonor;
    private DonorProfile expiredCooldownDonor;
    private DonorProfile medicalDeferredDonor;

    @BeforeEach
    void setUp() {
        when(matchingConfig.getCooldownDays()).thenReturn(90);

        sampleUser = User.builder()
                .id(1L)
                .email("donor@example.com")
                .fullName("Sample Donor")
                .phoneNumber("+1234567890")
                .build();

        // 1. Fresh donor with NO donation history
        freshDonor = DonorProfile.builder()
                .id(10L)
                .user(sampleUser)
                .email("fresh.donor@example.com")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(28)
                .weight(65.0)
                .gender(Gender.MALE)
                .lastDonationDate(null)
                .totalDonations(0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .build();

        // 2. Donor with recent donation (20 days ago)
        recentDonor = DonorProfile.builder()
                .id(11L)
                .user(sampleUser)
                .email("recent.donor@example.com")
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(30)
                .weight(70.0)
                .gender(Gender.MALE)
                .lastDonationDate(LocalDate.now().minusDays(20))
                .totalDonations(2)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .build();

        // 3. Donor whose 90-day cooldown has expired (100 days ago)
        expiredCooldownDonor = DonorProfile.builder()
                .id(12L)
                .user(sampleUser)
                .email("expired.donor@example.com")
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(32)
                .weight(75.0)
                .gender(Gender.MALE)
                .lastDonationDate(LocalDate.now().minusDays(100))
                .totalDonations(4)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .build();

        // 4. Donor with chronic medical condition
        medicalDeferredDonor = DonorProfile.builder()
                .id(13L)
                .user(sampleUser)
                .email("medical.donor@example.com")
                .bloodGroup(BloodGroup.AB_POSITIVE)
                .age(35)
                .weight(80.0)
                .gender(Gender.MALE)
                .medicalConditions("Hepatitis B recorded in history")
                .lastDonationDate(null)
                .totalDonations(0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("1. Fresh Donor (No Donation History): Eligible=true, DaysRemaining=0, LastDonation=null")
    void testFreshDonorEligibility() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(donorProfileRepository.findByUserId(anyLong())).thenReturn(Optional.of(freshDonor));

        var response = donorProfileService.calculateEligibility("fresh.donor@example.com");

        assertThat(response.getData()).isNotNull();
        EligibilityResponse dto = response.getData();
        assertThat(dto.isEligible()).isTrue();
        assertThat(dto.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(dto.getDaysUntilEligible()).isEqualTo(0);
    }

    @Test
    @DisplayName("2. Recent Donor (Donated 20 days ago with 90-day cooldown): Eligible=false, DaysRemaining=70")
    void testRecentDonorEligibility() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(donorProfileRepository.findByUserId(anyLong())).thenReturn(Optional.of(recentDonor));

        var response = donorProfileService.calculateEligibility("recent.donor@example.com");

        assertThat(response.getData()).isNotNull();
        EligibilityResponse dto = response.getData();
        assertThat(dto.isEligible()).isFalse();
        assertThat(dto.getStatus()).isEqualTo(EligibilityStatus.TEMPORARILY_DEFERRED);
        assertThat(dto.getDaysUntilEligible()).isEqualTo(70);
        assertThat(dto.getNextEligibleDate()).isEqualTo(LocalDate.now().minusDays(20).plusDays(90));
    }

    @Test
    @DisplayName("3. Expired Cooldown Donor (Donated 100 days ago): Eligible=true, DaysRemaining=0")
    void testExpiredCooldownDonorEligibility() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(donorProfileRepository.findByUserId(anyLong())).thenReturn(Optional.of(expiredCooldownDonor));

        var response = donorProfileService.calculateEligibility("expired.donor@example.com");

        assertThat(response.getData()).isNotNull();
        EligibilityResponse dto = response.getData();
        assertThat(dto.isEligible()).isTrue();
        assertThat(dto.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(dto.getDaysUntilEligible()).isEqualTo(0);
    }

    @Test
    @DisplayName("4. Medical Condition Deferral (Hepatitis B): Permanently Deferred")
    void testMedicalConditionDeferral() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(donorProfileRepository.findByUserId(anyLong())).thenReturn(Optional.of(medicalDeferredDonor));

        var response = donorProfileService.calculateEligibility("medical.donor@example.com");

        assertThat(response.getData()).isNotNull();
        EligibilityResponse dto = response.getData();
        assertThat(dto.isEligible()).isFalse();
        assertThat(dto.getStatus()).isEqualTo(EligibilityStatus.PERMANENTLY_DEFERRED);
    }

    @Test
    @DisplayName("5. Profile Endpoint (/me): Exposes eligible, daysUntilEligible, and cooldownDays parameters")
    void testGetMyProfileIncludesEligibilityDetails() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(donorProfileRepository.findByUserId(anyLong())).thenReturn(Optional.of(recentDonor));

        var response = donorProfileService.getMyProfile("recent.donor@example.com");

        assertThat(response.getData()).isNotNull();
        DonorProfileResponse profileDto = response.getData();
        assertThat(profileDto.getEligible()).isFalse();
        assertThat(profileDto.getDaysUntilEligible()).isEqualTo(70);
        assertThat(profileDto.getCooldownDays()).isEqualTo(90);
        assertThat(profileDto.getBloodGroup()).isEqualTo(BloodGroup.A_POSITIVE);
    }
}
