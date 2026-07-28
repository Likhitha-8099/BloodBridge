package com.bloodbridge.service;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.AuthResponse;
import com.bloodbridge.dto.LoginRequest;
import com.bloodbridge.dto.RegisterRequest;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.UserAlreadyExistsException;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.security.JwtService;
import com.bloodbridge.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

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
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        ApiResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ThrowsException_WhenEmailExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.DONOR, response.getRole());
        assertNotNull(response.getUser());
        assertEquals("john@example.com", response.getUser().getEmail());
        assertEquals("John Doe", response.getUser().getFullName());
        assertEquals(Role.DONOR, response.getUser().getRole());
        assertTrue(response.getUser().getRoles().contains(Role.DONOR));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void switchRole_Success() {
        java.util.Set<Role> roles = new java.util.HashSet<>();
        roles.add(Role.DONOR);
        roles.add(Role.PATIENT);
        user.setRoles(roles);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("newMockToken");

        AuthResponse response = authService.switchRole("john@example.com", Role.PATIENT);

        assertNotNull(response);
        assertEquals("newMockToken", response.getToken());
        assertEquals(Role.PATIENT, response.getRole());
        assertEquals(Role.PATIENT, response.getUser().getRole());
        assertTrue(response.getUser().getRoles().contains(Role.DONOR));
        assertTrue(response.getUser().getRoles().contains(Role.PATIENT));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void switchRole_ThrowsException_WhenRoleNotAssigned() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.switchRole("john@example.com", Role.PATIENT));
        verify(userRepository, never()).save(any(User.class));
    }
}
