package com.bloodbridge.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Firebase Admin SDK Configuration — Phase 3B.
 *
 * <p>Initializes FirebaseApp and exposes a FirebaseMessaging bean.
 * Credentials are loaded exclusively from environment variables.
 * No credentials are ever hardcoded in this class.</p>
 *
 * <p>Activation: requires {@code firebase.enabled=true} in application.properties
 * (or {@code FIREBASE_ENABLED=true} in .env).
 * This prevents startup failure when credentials are not yet configured.</p>
 *
 * <p>Environment variables consumed:
 * <ul>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_PATH} — path to the Service Account JSON file</li>
 *   <li>{@code FIREBASE_PROJECT_ID}            — GCP / Firebase Project ID</li>
 *   <li>{@code FIREBASE_ENABLED}               — master on/off switch (true/false)</li>
 * </ul>
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class FirebaseConfig {

    /**
     * Path to the Firebase Service Account JSON file.
     * Resolved from {@code FIREBASE_SERVICE_ACCOUNT_PATH} env variable,
     * with a safe classpath fallback for local development.
     */
    @Value("${firebase.service-account-path:src/main/resources/firebase/firebase-service-account.json}")
    private String serviceAccountPath;

    /**
     * Firebase / GCP Project ID.
     * Resolved from {@code FIREBASE_PROJECT_ID} env variable.
     */
    @Value("${firebase.project-id:}")
    private String projectId;

    /**
     * Initializes and returns the {@link FirebaseMessaging} singleton bean.
     *
     * <p>Startup sequence:
     * <ol>
     *   <li>Validate that the service-account JSON file exists on disk.</li>
     *   <li>Load {@link GoogleCredentials} from the file stream.</li>
     *   <li>Build {@link FirebaseOptions} with the credentials and project ID.</li>
     *   <li>Initialize {@link FirebaseApp} only once (idempotent guard).</li>
     *   <li>Return the {@link FirebaseMessaging} instance tied to that app.</li>
     * </ol>
     * </p>
     *
     * @return {@link FirebaseMessaging} bean ready for injection into services.
     * @throws IOException              if the service-account JSON file cannot be read.
     * @throws IllegalStateException    if the service-account path is blank or the project ID is missing.
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        log.info("[Firebase] ═══════════════════════════════════════════════");
        log.info("[Firebase] Initializing Firebase Admin SDK — Phase 3B");
        log.info("[Firebase] Service Account Path : {}", serviceAccountPath);
        log.info("[Firebase] Project ID           : {}", projectId.isEmpty() ? "<NOT SET>" : projectId);

        // ── 1. Validate configuration ─────────────────────────────────────────
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.error("[Firebase] FIREBASE_SERVICE_ACCOUNT_PATH is not set. " +
                      "Add it to your backend/.env file.");
            throw new IllegalStateException(
                "[Firebase] Configuration Missing: FIREBASE_SERVICE_ACCOUNT_PATH must be set " +
                "in the environment before enabling Firebase.");
        }

        if (projectId == null || projectId.isBlank()) {
            log.error("[Firebase] FIREBASE_PROJECT_ID is not set. " +
                      "Add it to your backend/.env file.");
            throw new IllegalStateException(
                "[Firebase] Configuration Missing: FIREBASE_PROJECT_ID must be set " +
                "in the environment before enabling Firebase.");
        }

        // ── 2. Verify JSON file exists on disk ────────────────────────────────
        Path jsonPath = Paths.get(serviceAccountPath).toAbsolutePath();
        log.info("[Firebase] Resolved absolute path: {}", jsonPath);

        if (!Files.exists(jsonPath)) {
            log.error("[Firebase] Service Account JSON not found at: {}", jsonPath);
            log.error("[Firebase] Place your downloaded firebase-service-account.json at that path.");
            throw new IllegalStateException(
                "[Firebase] Service Account JSON file not found at: " + jsonPath +
                ". Download it from Firebase Console → Project Settings → Service Accounts.");
        }
        log.info("[Firebase] ✔ Service Account JSON file found.");

        // ── 3. Load GoogleCredentials from the JSON file ──────────────────────
        GoogleCredentials credentials;
        try (InputStream serviceAccountStream = new FileInputStream(jsonPath.toFile())) {
            credentials = GoogleCredentials.fromStream(serviceAccountStream);
            log.info("[Firebase] ✔ Google Credentials loaded successfully.");
        } catch (IOException ex) {
            log.error("[Firebase] Failed to read Service Account JSON: {}", ex.getMessage());
            throw ex;
        }

        // ── 4. Build FirebaseOptions ──────────────────────────────────────────
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();

        // ── 5. Initialize FirebaseApp (idempotent guard) ──────────────────────
        FirebaseApp app;
        if (FirebaseApp.getApps().isEmpty()) {
            app = FirebaseApp.initializeApp(options);
            log.info("[Firebase] ✔ FirebaseApp initialized: {}", app.getName());
        } else {
            app = FirebaseApp.getInstance();
            log.info("[Firebase] ✔ FirebaseApp already initialized, reusing existing instance.");
        }

        log.info("[Firebase] ✔ Firebase Admin SDK Ready — FirebaseMessaging bean available.");
        log.info("[Firebase] ═══════════════════════════════════════════════");

        return FirebaseMessaging.getInstance(app);
    }
}
