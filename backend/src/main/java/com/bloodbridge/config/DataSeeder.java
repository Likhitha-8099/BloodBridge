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
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Startup Validation & Schema Migration Guard: Convert MySQL ENUMs to production-ready VARCHAR
        try {
            if (jdbcTemplate != null) {
                jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN notification_type VARCHAR(50) NOT NULL");
                jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN delivery_channel VARCHAR(30) NOT NULL");
                jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN status VARCHAR(20) NOT NULL");
                jdbcTemplate.execute("ALTER TABLE match_results MODIFY COLUMN compatibility_score DOUBLE NULL");
                jdbcTemplate.execute("ALTER TABLE audit_logs MODIFY COLUMN timestamp DATETIME NULL");
                try {
                    jdbcTemplate.execute("ALTER TABLE donations ADD COLUMN completed_at DATETIME NULL");
                } catch (Exception ignored) {}
                try {
                    jdbcTemplate.execute("ALTER TABLE donations MODIFY COLUMN match_result_id BIGINT NULL");
                } catch (Exception ignored) {}
                try {
                    jdbcTemplate.execute("ALTER TABLE donations MODIFY COLUMN patient_profile_id BIGINT NULL");
                } catch (Exception ignored) {}
                log.info("Startup Validation: Successfully aligned notifications, match_results, audit_logs, and donations table schema.");
            }
        } catch (Exception e) {
            log.warn("Startup Validation Warning: Schema alignment skipped: {}", e.getMessage());
        }

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
