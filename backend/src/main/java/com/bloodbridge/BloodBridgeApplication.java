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
 * Automatically loads .env environment variables during local development.
 * In cloud/container environments (e.g. Render), native environment variables are used directly.
 */
@Slf4j
@SpringBootApplication
public class BloodBridgeApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(BloodBridgeApplication.class, args);
    }

    private static void loadDotEnvIfPresent() {
        // Skip .env file scanning if running in a cloud/container environment (Render, Docker, etc.)
        if (System.getenv("RENDER") != null || System.getenv("PORT") != null || System.getenv("SPRING_DATASOURCE_URL") != null) {
            log.info("[BloodBridge] Cloud/container environment detected. Using native environment variables.");
            return;
        }

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
                        if (!key.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                            loadedCount++;
                        }
                    }
                }
                log.info("[BloodBridge] Loaded {} environment variables from local {}", loadedCount, envPath.toAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("[BloodBridge] Note on .env loading: {}", e.getMessage());
        }
    }
}
