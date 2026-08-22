package com.bloodbridge.service;

import com.bloodbridge.dto.request.LoginRequest;
import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.AuthResponse;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.UserAlreadyExistsException;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.security.JwtService;
import com.bloodbridge.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditLoggerService auditLoggerService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RealtimeService realtimeService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .role(Role.DONOR)
                .build();

        loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("Password@123")
                .build();

        java.util.Set<Role> roles = new java.util.HashSet<>();
        roles.add(Role.DONOR);

        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .phoneNumber("9876543210")
                .role(Role.DONOR)
                .roles(roles)
                .active(true)
                .build();

        authResponse = AuthResponse.builder()
                .token("mockToken")
                .email("john@example.com")
                .role(Role.DONOR)
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        ApiResponse<String> response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_Success_WhenAccountWasDeactivated() {
        User deactivatedUser = User.builder()
                .id(2L)
                .fullName("Inactive User")
                .email(registerRequest.getEmail())
                .active(false)
                .build();

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(deactivatedUser));
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(deactivatedUser);

        ApiResponse<String> response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertTrue(deactivatedUser.getActive());
        verify(userRepository, times(1)).save(deactivatedUser);
    }

    @Test
    void register_ThrowsException_WhenActiveEmailExists() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken((User) any())).thenReturn("mockToken");
        when(userMapper.toAuthResponse(any(), any())).thenReturn(authResponse);

        ApiResponse<AuthResponse> response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("mockToken", response.getData().getToken());
        assertEquals("john@example.com", response.getData().getEmail());
        assertEquals(Role.DONOR, response.getData().getRole());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
