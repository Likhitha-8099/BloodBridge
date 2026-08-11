package com.bloodbridge.auth;

import com.bloodbridge.dto.request.LoginRequest;
import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.AuthResponse;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AdminService;
import com.bloodbridge.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Test Suite for Hospital Registration, Admin Approval, and Login Synchronization.
 * Verifies that hospital login succeeds immediately following administrator approval.
 */
@SpringBootTest
@ActiveProfiles("test")
public class HospitalApprovalLoginTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    private String testHospitalEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();
        testHospitalEmail = "hospital." + timestamp + "@stjude.org";
        testPassword = "Password@123";
    }

    @AfterEach
    void tearDown() {
        try {
            userRepository.findByEmail(testHospitalEmail).ifPresent(u -> {
                hospitalRepository.findByUserId(u.getId()).ifPresent(hospitalRepository::delete);
                userRepository.delete(u);
            });
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Hospital Pending State: Login must throw IllegalStateException when pending approval")
    void testLoginFailsWhenHospitalRegistrationIsPending() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("St. Jude Hospital")
                .email(testHospitalEmail)
                .password(testPassword)
                .phoneNumber("9876543210")
                .role(Role.HOSPITAL)
                .city("Memphis")
                .state("Tennessee")
                .country("USA")
                .address("262 Danny Thomas Place")
                .postalCode("38105")
                .build();

        authService.register(registerReq);

        User registeredUser = userRepository.findByEmail(testHospitalEmail).orElseThrow();
        assertFalse(registeredUser.getActive(), "User must initially be inactive pending admin approval");

        Hospital hospital = hospitalRepository.findByUserId(registeredUser.getId()).orElseThrow();
        assertEquals("PENDING", hospital.getVerificationStatus());

        LoginRequest loginReq = LoginRequest.builder()
                .email(testHospitalEmail)
                .password(testPassword)
                .build();

        IllegalStateException pendingException = assertThrows(IllegalStateException.class, () -> {
            authService.login(loginReq);
        });
        assertTrue(pendingException.getMessage().contains("pending admin approval"));
    }

    @Test
    @DisplayName("Hospital Approval State: Hospital login succeeds immediately following admin approval")
    void testHospitalApprovalAndImmediateLoginWorkflow() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("St. Jude Research Hospital")
                .email(testHospitalEmail)
                .password(testPassword)
                .phoneNumber("9876543210")
                .role(Role.HOSPITAL)
                .city("Memphis")
                .state("Tennessee")
                .country("USA")
                .address("262 Danny Thomas Place")
                .postalCode("38105")
                .build();

        authService.register(registerReq);

        User registeredUser = userRepository.findByEmail(testHospitalEmail).orElseThrow();
        Hospital hospital = hospitalRepository.findByUserId(registeredUser.getId()).orElseThrow();

        // Admin Approves Hospital Registration
        ApiResponse<HospitalResponse> approveResponse = adminService.verifyHospital(
                hospital.getId(),
                "APPROVED",
                "Verified hospital medical license and documentation",
                "admin@bloodbridge.com"
        );
        assertNotNull(approveResponse);
        assertTrue(approveResponse.isSuccess());
        assertEquals("APPROVED", approveResponse.getData().getVerificationStatus());
        assertTrue(approveResponse.getData().getVerified());

        // Verify database after approval: BOTH users and hospitals tables are updated
        User approvedUser = userRepository.findByEmail(testHospitalEmail).orElseThrow();
        assertTrue(approvedUser.getActive(), "User.active must be true after admin approval");

        Hospital approvedHospital = hospitalRepository.findById(hospital.getId()).orElseThrow();
        assertEquals("APPROVED", approvedHospital.getVerificationStatus());
        assertTrue(approvedHospital.getVerified());
        assertEquals("ACTIVE", approvedHospital.getStatus());

        // Hospital Logs In Immediately After Approval -> Must Succeed and return JWT
        LoginRequest loginReq = LoginRequest.builder()
                .email(testHospitalEmail)
                .password(testPassword)
                .build();

        ApiResponse<AuthResponse> loginResponse = authService.login(loginReq);
        assertNotNull(loginResponse);
        assertTrue(loginResponse.isSuccess());
        assertNotNull(loginResponse.getData().getToken(), "JWT token must be generated");
        assertEquals(testHospitalEmail, loginResponse.getData().getUser().getEmail());
        assertEquals(Role.HOSPITAL, loginResponse.getData().getUser().getRole());
    }

    @Test
    @DisplayName("Hospital Rejection State: Login throws rejection message when rejected by admin")
    void testHospitalRejectionWorkflow() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("Unverified Hospital")
                .email(testHospitalEmail)
                .password(testPassword)
                .phoneNumber("1112223333")
                .role(Role.HOSPITAL)
                .city("Metropolis")
                .state("State")
                .address("Unknown")
                .build();

        authService.register(registerReq);

        User user = userRepository.findByEmail(testHospitalEmail).orElseThrow();
        Hospital hospital = hospitalRepository.findByUserId(user.getId()).orElseThrow();

        // Admin Rejects Hospital Registration
        adminService.verifyHospital(hospital.getId(), "REJECTED", "License invalid", "admin@bloodbridge.com");

        LoginRequest loginReq = LoginRequest.builder().email(testHospitalEmail).password(testPassword).build();
        IllegalStateException rejectException = assertThrows(IllegalStateException.class, () -> {
            authService.login(loginReq);
        });
        assertTrue(rejectException.getMessage().contains("rejected by administrator"));
    }
}
