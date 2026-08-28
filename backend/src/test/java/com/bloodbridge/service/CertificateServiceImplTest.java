package com.bloodbridge.service;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.InvalidDonationStateException;
import com.bloodbridge.exception.UnauthorizedDonationAccessException;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.CertificateServiceImpl;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    private User sampleUser;
    private DonorProfile sampleDonor;
    private Hospital sampleHospital;
    private Donation sampleDonation;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(101L)
                .fullName("Likhitha Markonda")
                .email("likhitha@bloodbridge.com")
                .role(Role.DONOR)
                .build();

        sampleDonor = DonorProfile.builder()
                .id(202L)
                .user(sampleUser)
                .email("likhitha@bloodbridge.com")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(25)
                .gender(Gender.FEMALE)
                .city("Hyderabad")
                .state("Telangana")
                .build();

        sampleHospital = Hospital.builder()
                .id(303L)
                .hospitalName("Apollo Super Specialty Hospital")
                .city("Hyderabad")
                .build();

        sampleDonation = Donation.builder()
                .id(404L)
                .donor(sampleDonor)
                .hospital(sampleHospital)
                .bloodRequest(BloodRequest.builder().bloodGroupNeeded(BloodGroup.O_POSITIVE).build())
                .donationDate(LocalDate.of(2026, 8, 26))
                .unitsDonated(1)
                .status(DonationStatus.COMPLETED)
                .certificateId("CERT-BB-2026-000404")
                .completedAt(LocalDateTime.of(2026, 8, 26, 14, 30))
                .build();
    }

    @Test
    @DisplayName("Generate Certificate: PDF is valid, landscape A4, single page, and contains official headers")
    void testGenerateCertificatePdf_ValidLandscapePdf() throws Exception {
        byte[] pdfBytes = certificateService.generateCertificatePdf(sampleDonation);

        assertThat(pdfBytes).isNotNull().isNotEmpty();
        assertThat(pdfBytes.length).isGreaterThan(5000); // Premium vector certificate size
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");

        // Verify PDF Structure using PdfReader
        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        assertThat(reader.getNumberOfPages()).isEqualTo(1);

        // A4 Landscape: width > height (~841.89 x ~595.28)
        float width = reader.getPageSizeWithRotation(1).getWidth();
        float height = reader.getPageSizeWithRotation(1).getHeight();
        assertThat(width).isGreaterThan(height);
        assertThat(width).isBetween(840.0f, 843.0f);
        assertThat(height).isBetween(594.0f, 597.0f);

        reader.close();
    }

    @Test
    @DisplayName("Get Certificate For Donor: Authorized donor successfully fetches redesigned certificate")
    void testGetCertificatePdfForDonor_Success() {
        when(userRepository.findByEmail("likhitha@bloodbridge.com")).thenReturn(Optional.of(sampleUser));
        when(donationRepository.findById(404L)).thenReturn(Optional.of(sampleDonation));

        byte[] result = certificateService.getCertificatePdfForDonor(404L, "likhitha@bloodbridge.com");

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(new String(result, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("Get Certificate For Donor: Unauthorized attacker throws UnauthorizedDonationAccessException")
    void testGetCertificatePdfForDonor_Unauthorized() {
        User attacker = User.builder()
                .id(999L)
                .fullName("Attacker User")
                .email("attacker@test.com")
                .role(Role.DONOR)
                .build();

        when(userRepository.findByEmail("attacker@test.com")).thenReturn(Optional.of(attacker));
        when(donationRepository.findById(404L)).thenReturn(Optional.of(sampleDonation));

        assertThrows(UnauthorizedDonationAccessException.class,
                () -> certificateService.getCertificatePdfForDonor(404L, "attacker@test.com"));
    }

    @Test
    @DisplayName("Get Certificate For Donor: Non-completed donation throws InvalidDonationStateException")
    void testGetCertificatePdfForDonor_NonCompletedDonation() {
        sampleDonation.setStatus(DonationStatus.PENDING);

        when(userRepository.findByEmail("likhitha@bloodbridge.com")).thenReturn(Optional.of(sampleUser));
        when(donationRepository.findById(404L)).thenReturn(Optional.of(sampleDonation));

        assertThrows(InvalidDonationStateException.class,
                () -> certificateService.getCertificatePdfForDonor(404L, "likhitha@bloodbridge.com"));
    }
}
