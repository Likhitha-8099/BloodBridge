package com.bloodbridge.service.impl;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.AuthResponse;
import com.bloodbridge.dto.LoginRequest;
import com.bloodbridge.dto.RegisterRequest;
import com.bloodbridge.entity.User;
import com.bloodbridge.exception.UserAlreadyExistsException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.security.JwtService;
import com.bloodbridge.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Service implementation for authentication workflows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ApiResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Email is already registered: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        return ApiResponse.builder()
                .message("User registered successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login authentication for email: {}", request.getEmail());
        // Authenticate the user credentials using Spring Security Manager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Retrieve user to generate token and return response details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email {}", request.getEmail());
                    return new UserNotFoundException("User not found with email: " + request.getEmail());
                });

        // Build UserDetails representation for JWT generation
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getActive(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        String jwtToken = jwtService.generateToken(userDetails);
        log.info("Login successful for email: {} with role: {}", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole())
                .user(AuthResponse.UserInfo.builder()
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .roles(user.getRoles())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse switchRole(String email, com.bloodbridge.enums.Role newRole) {
        log.info("Attempting to switch active role to {} for email: {}", newRole, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Role switch failed: User not found with email {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        if (!user.getRoles().contains(newRole)) {
            log.warn("Role switch failed: Role {} is not assigned to user {}", newRole, email);
            throw new IllegalArgumentException("Role " + newRole + " is not assigned to user " + email);
        }

        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        log.info("Successfully updated active role to {} in database for email: {}", newRole, email);

        // Build UserDetails representation for JWT generation
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getActive(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + savedUser.getRole().name()))
        );

        String jwtToken = jwtService.generateToken(userDetails);
        log.info("Successfully generated new JWT token for email: {} with active role: {}", email, newRole);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .user(AuthResponse.UserInfo.builder()
                        .email(savedUser.getEmail())
                        .fullName(savedUser.getFullName())
                        .role(savedUser.getRole())
                        .roles(savedUser.getRoles())
                        .build())
                .build();
    }
}
