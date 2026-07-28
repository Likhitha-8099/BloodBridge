package com.bloodbridge.config;

import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

/**
 * Seeds default data into the database when the application starts.
 * Used here to ensure a default Admin account always exists.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @SuppressWarnings("null")
    public void run(String... args) {
        String adminEmail = "admin@bloodbridge.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Default Admin account not found. Creating one...");
            
            User admin = User.builder()
                    .fullName("System Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@12345"))
                    .phoneNumber("0000000000")
                    .role(Role.ADMIN)
                    .roles(new HashSet<>(List.of(Role.ADMIN)))
                    .active(true)
                    .build();
                    
            userRepository.save(admin);
            log.info("Default Admin account created successfully.");
        } else {
            log.info("Default Admin account already exists.");
        }
    }
}
