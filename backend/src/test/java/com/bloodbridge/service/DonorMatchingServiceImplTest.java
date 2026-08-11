package com.bloodbridge.service;

import com.bloodbridge.dto.DonorMatchingResult;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.service.impl.DonorMatchingServiceImpl;
import com.bloodbridge.service.impl.LocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DonorMatchingServiceImpl evaluating the 8 eligibility rules.
 */
class DonorMatchingServiceImplTest {

    private DonorProfileRepository donorProfileRepository;
    private EmailNotificationRepository emailNotificationRepository;
    private LocationService locationService;
    private DonorMatchingService donorMatchingService;

    @BeforeEach
    void setUp() {
        donorProfileRepository = mock(DonorProfileRepository.class);
        emailNotificationRepository = mock(EmailNotificationRepository.class);
        locationService = new LocationServiceImpl();
        donorMatchingService = new DonorMatchingServiceImpl(donorProfileRepository, emailNotificationRepository, locationService);
    }

    @Test
    void evaluateEligibleDonors_All8RulesMet_MatchesDonor() {
        User user = User.builder()
                .id(1L)
                .fullName("John Donor")
                .email("john.donor@example.com")
                .active(true)
                .emailVerified(true)
                .build();

        DonorProfile donor = DonorProfile.builder()
                .id(10L)
                .user(user)
                .email("john.donor@example.com")
                .bloodGroup(BloodGroup.O_NEGATIVE)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .latitude(12.9716)
                .longitude(77.5946)
                .lastDonationDate(LocalDate.now().minusDays(100))
                .build();

        Hospital hospital = Hospital.builder()
                .id(5L)
                .hospitalName("City Care Hospital")
                .latitude(12.9698)
                .longitude(77.7499) // ~16.8 KM distance
                .build();

        BloodRequest request = BloodRequest.builder()
                .id(100L)
                .bloodGroupNeeded(BloodGroup.A_POSITIVE)
                .hospital(hospital)
                .build();

        when(donorProfileRepository.findAll()).thenReturn(List.of(donor));
        when(emailNotificationRepository.existsByEmergencyRequestIdAndDonorId(100L, 10L)).thenReturn(false);

        DonorMatchingResult result = donorMatchingService.evaluateEligibleDonors(request);

        assertEquals(1, result.getMatchedDonors().size());
        assertEquals(10L, result.getMatchedDonors().get(0).getId());
        assertTrue(result.getDonorDistances().containsKey(10L));
    }

    @Test
    void evaluateEligibleDonors_IncompatibleBloodGroup_SkipsDonor() {
        User user = User.builder().id(2L).email("b.pos@example.com").active(true).emailVerified(true).build();
        DonorProfile donor = DonorProfile.builder()
                .id(11L)
                .user(user)
                .bloodGroup(BloodGroup.B_POSITIVE)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .latitude(12.9716)
                .longitude(77.5946)
                .build();

        Hospital hospital = Hospital.builder().id(5L).latitude(12.9716).longitude(77.5946).build();
        BloodRequest request = BloodRequest.builder().id(101L).bloodGroupNeeded(BloodGroup.A_NEGATIVE).hospital(hospital).build();

        when(donorProfileRepository.findAll()).thenReturn(List.of(donor));

        DonorMatchingResult result = donorMatchingService.evaluateEligibleDonors(request);

        assertEquals(0, result.getMatchedDonors().size());
    }

    @Test
    void evaluateEligibleDonors_Exceeds50KmRadius_SkipsDonor() {
        User user = User.builder().id(3L).email("far.donor@example.com").active(true).emailVerified(true).build();
        DonorProfile donor = DonorProfile.builder()
                .id(12L)
                .user(user)
                .bloodGroup(BloodGroup.O_NEGATIVE)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .latitude(12.9716) // Bangalore
                .longitude(77.5946)
                .build();

        Hospital hospital = Hospital.builder()
                .id(5L)
                .latitude(12.2958) // Mysore (~140 KM)
                .longitude(76.6394)
                .build();

        BloodRequest request = BloodRequest.builder().id(102L).bloodGroupNeeded(BloodGroup.A_POSITIVE).hospital(hospital).build();

        when(donorProfileRepository.findAll()).thenReturn(List.of(donor));

        DonorMatchingResult result = donorMatchingService.evaluateEligibleDonors(request);

        assertEquals(0, result.getMatchedDonors().size());
    }

    @Test
    void evaluateEligibleDonors_AlreadyNotified_SkipsDuplicate() {
        User user = User.builder().id(4L).email("notified@example.com").active(true).emailVerified(true).build();
        DonorProfile donor = DonorProfile.builder()
                .id(13L)
                .user(user)
                .bloodGroup(BloodGroup.O_NEGATIVE)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .latitude(12.9716)
                .longitude(77.5946)
                .build();

        Hospital hospital = Hospital.builder().id(5L).latitude(12.9716).longitude(77.5946).build();
        BloodRequest request = BloodRequest.builder().id(103L).bloodGroupNeeded(BloodGroup.A_POSITIVE).hospital(hospital).build();

        when(donorProfileRepository.findAll()).thenReturn(List.of(donor));
        when(emailNotificationRepository.existsByEmergencyRequestIdAndDonorId(103L, 13L)).thenReturn(true);

        DonorMatchingResult result = donorMatchingService.evaluateEligibleDonors(request);

        assertEquals(0, result.getMatchedDonors().size());
    }
}
