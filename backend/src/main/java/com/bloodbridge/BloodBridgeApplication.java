package com.bloodbridge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main entry point for the Blood Bridge application.
 * Automatically loads .env environment variables before Spring application startup.
 */
@Slf4j
@SpringBootApplication
public class BloodBridgeApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(BloodBridgeApplication.class, args);
    }

    private static void loadDotEnv() {
        try {
            Path envPath = Paths.get(".env");
            if (!Files.exists(envPath)) {
                envPath = Paths.get("backend", ".env");
            }
            if (!Files.exists(envPath)) {
                envPath = Paths.get("..", ".env");
            }

            if (Files.exists(envPath)) {
                List<String> lines = Files.readAllLines(envPath);
                int loadedCount = 0;
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    int eqIndex = trimmed.indexOf('=');
                    if (eqIndex > 0) {
                        String key = trimmed.substring(0, eqIndex).trim();
                        String value = trimmed.substring(eqIndex + 1).trim();
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        if (!key.isEmpty()) {
                            System.setProperty(key, value);
                            loadedCount++;
                        }
                    }
                }
                log.info("================================================================================");
                log.info("✅ Successfully loaded {} environment variables from {}", loadedCount, envPath.toAbsolutePath());
                log.info("================================================================================");
            } else {
                log.warn("⚠️ No .env file found at {}", envPath.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("❌ Exception while loading .env file: {}", e.getMessage(), e);
        }
    }
}
