package com.bloodbridge.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Firebase Admin SDK Configuration — Phase 3B.
 *
 * <p>Initializes FirebaseApp and exposes a FirebaseMessaging bean.
 * Supports loading credentials securely from:
 * <ol>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_JSON} environment variable (Production / Container deployments).</li>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_PATH} file path (Local development / mounted secrets).</li>
 * </ol>
 * No secrets are ever logged or hardcoded in this class.</p>
 *
 * <p>Activation: requires {@code firebase.enabled=true} in application.properties
 * (or {@code FIREBASE_ENABLED=true} in the environment).
 * This prevents startup failure when credentials are not yet configured.</p>
 */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class FirebaseConfig {

    /**
     * Raw Firebase Service Account JSON string from environment variable.
     * Preferred for cloud/container deployments (e.g. Render, Railway, Kubernetes)
     * without needing to commit credentials to Git.
     */
    @Value("${firebase.service-account-json:${FIREBASE_SERVICE_ACCOUNT_JSON:}}")
    private String serviceAccountJson;

    /**
     * Path to the Firebase Service Account JSON file.
     * Resolved from {@code FIREBASE_SERVICE_ACCOUNT_PATH} env variable,
     * with a safe classpath/filesystem fallback for local development.
     */
    @Value("${firebase.service-account-path:${FIREBASE_SERVICE_ACCOUNT_PATH:src/main/resources/firebase/firebase-service-account.json}}")
    private String serviceAccountPath;

    /**
     * Firebase / GCP Project ID.
     * Resolved from {@code FIREBASE_PROJECT_ID} env variable (optional if embedded in service account JSON).
     */
    @Value("${firebase.project-id:${FIREBASE_PROJECT_ID:}}")
    private String projectId;

    /**
     * Initializes and returns the {@link FirebaseMessaging} singleton bean.
     *
     * @return {@link FirebaseMessaging} bean ready for injection into services.
     * @throws IOException           if credentials cannot be parsed or read.
     * @throws IllegalStateException if neither valid JSON string nor valid file path is provided.
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        log.info("[Firebase] ═══════════════════════════════════════════════");
        log.info("[Firebase] Initializing Firebase Admin SDK — Phase 3B");

        GoogleCredentials credentials;

        // ── 1. Production: Check FIREBASE_SERVICE_ACCOUNT_JSON environment variable ──
        if (serviceAccountJson != null && !serviceAccountJson.trim().isEmpty()) {
            log.info("[Firebase] Attempting to load credentials from FIREBASE_SERVICE_ACCOUNT_JSON environment variable...");
            try (InputStream stream = new ByteArrayInputStream(serviceAccountJson.trim().getBytes(StandardCharsets.UTF_8))) {
                credentials = GoogleCredentials.fromStream(stream);
                log.info("[Firebase] ✔ Firebase credentials loaded from environment variable.");
            } catch (IOException ex) {
                log.error("[Firebase] Failed to parse Firebase service account JSON from environment variable: {}", ex.getMessage());
                throw new IllegalStateException("[Firebase] Invalid FIREBASE_SERVICE_ACCOUNT_JSON provided in environment variables.", ex);
            }
        }
        // ── 2. Local development: Fallback to file path ──
        else if (serviceAccountPath != null && !serviceAccountPath.trim().isEmpty()) {
            Path jsonPath = Paths.get(serviceAccountPath.trim()).toAbsolutePath();
            log.info("[Firebase] Checking local service account file at: {}", jsonPath);

            if (!Files.exists(jsonPath)) {
                log.error("[Firebase] Service Account JSON not found at: {}", jsonPath);
                log.error("[Firebase] Provide FIREBASE_SERVICE_ACCOUNT_JSON in environment variables or place firebase-service-account.json at {}", jsonPath);
                throw new IllegalStateException(
                    "[Firebase] Service Account credentials not found. Either provide FIREBASE_SERVICE_ACCOUNT_JSON " +
                    "as an environment variable or place the JSON file at: " + jsonPath);
            }

            try (InputStream stream = new FileInputStream(jsonPath.toFile())) {
                credentials = GoogleCredentials.fromStream(stream);
                log.info("[Firebase] ✔ Firebase credentials loaded from local file.");
            } catch (IOException ex) {
                log.error("[Firebase] Failed to read Service Account JSON file: {}", ex.getMessage());
                throw ex;
            }
        } else {
            log.error("[Firebase] Neither FIREBASE_SERVICE_ACCOUNT_JSON nor FIREBASE_SERVICE_ACCOUNT_PATH configured.");
            throw new IllegalStateException(
                "[Firebase] Missing Firebase configuration: Provide FIREBASE_SERVICE_ACCOUNT_JSON or FIREBASE_SERVICE_ACCOUNT_PATH.");
        }

        // ── 3. Resolve Project ID (if not explicitly set, extract from ServiceAccountCredentials) ──
        String resolvedProjectId = (projectId != null && !projectId.isBlank()) ? projectId.trim() : null;
        if (resolvedProjectId == null && credentials instanceof ServiceAccountCredentials sac) {
            resolvedProjectId = sac.getProjectId();
        }

        // ── 4. Build FirebaseOptions ──
        FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                .setCredentials(credentials);

        if (resolvedProjectId != null && !resolvedProjectId.isBlank()) {
            optionsBuilder.setProjectId(resolvedProjectId);
            log.info("[Firebase] Project ID           : {}", resolvedProjectId);
        } else {
            log.info("[Firebase] Project ID           : <AUTO-RESOLVED>");
        }

        FirebaseOptions options = optionsBuilder.build();

        // ── 5. Initialize FirebaseApp (idempotent guard) ──
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
