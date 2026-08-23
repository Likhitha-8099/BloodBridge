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
import java.util.Base64;

/**
 * Firebase Admin SDK Configuration — Phase 3B.
 *
 * <p>Initializes FirebaseApp and exposes the {@link FirebaseMessaging} bean.
 * Supports loading credentials securely with the following priority:
 * <ol>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_BASE64} Base64 environment variable (Recommended for Render/Production).</li>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_JSON} raw JSON environment variable.</li>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_PATH} file path (Local development / mounted secret file).</li>
 *   <li>Classpath fallback at {@code firebase/firebase-service-account.json} (Local development).</li>
 * </ol>
 * Sensitive credentials, Base64 strings, and private keys are NEVER logged.</p>
 */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-base64:${FIREBASE_SERVICE_ACCOUNT_BASE64:}}")
    private String serviceAccountBase64;

    @Value("${firebase.service-account-json:${FIREBASE_SERVICE_ACCOUNT_JSON:}}")
    private String serviceAccountJson;

    @Value("${firebase.service-account-path:${FIREBASE_SERVICE_ACCOUNT_PATH:src/main/resources/firebase/firebase-service-account.json}}")
    private String serviceAccountPath;

    @Value("${firebase.project-id:${FIREBASE_PROJECT_ID:}}")
    private String projectId;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        log.info("[Firebase] ═══════════════════════════════════════════════");
        log.info("[Firebase] Initializing Firebase Admin SDK — Phase 3B");

        GoogleCredentials credentials = resolveCredentials();

        // ── Resolve Project ID ──
        String resolvedProjectId = (projectId != null && !projectId.isBlank()) ? projectId.trim() : null;
        if (resolvedProjectId == null && credentials instanceof ServiceAccountCredentials sac) {
            resolvedProjectId = sac.getProjectId();
        }

        FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                .setCredentials(credentials);

        if (resolvedProjectId != null && !resolvedProjectId.isBlank()) {
            optionsBuilder.setProjectId(resolvedProjectId);
            log.info("[Firebase] Firebase project ID = {}", resolvedProjectId);
        } else {
            log.info("[Firebase] Firebase project ID = <AUTO-RESOLVED>");
        }

        FirebaseOptions options = optionsBuilder.build();

        // ── Idempotent initialization guard ──
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

    /**
     * Resolves GoogleCredentials safely from Base64 env var, JSON env var, file system, or classpath.
     */
    private GoogleCredentials resolveCredentials() throws IOException {
        // ── 1. Priority 1: FIREBASE_SERVICE_ACCOUNT_BASE64 (Render / Production) ──
        String rawBase64 = getServiceAccountBase64String();
        if (rawBase64 != null && !rawBase64.trim().isEmpty()) {
            String cleanedBase64 = rawBase64.trim();
            if ((cleanedBase64.startsWith("'") && cleanedBase64.endsWith("'")) ||
                (cleanedBase64.startsWith("\"") && cleanedBase64.endsWith("\""))) {
                cleanedBase64 = cleanedBase64.substring(1, cleanedBase64.length() - 1).trim();
            }
            cleanedBase64 = cleanedBase64.replaceAll("\\s+", "");

            try {
                byte[] decodedBytes = Base64.getDecoder().decode(cleanedBase64);
                String decodedJson = new String(decodedBytes, StandardCharsets.UTF_8);
                if (decodedJson.startsWith("\uFEFF")) {
                    decodedJson = decodedJson.substring(1);
                }
                decodedJson = decodedJson.trim();

                try (InputStream stream = new ByteArrayInputStream(decodedJson.getBytes(StandardCharsets.UTF_8))) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                    log.info("[Firebase] Firebase credentials source = ENV_BASE64");
                    return credentials;
                }
            } catch (IllegalArgumentException ex) {
                log.error("[Firebase] Failed to Base64 decode FIREBASE_SERVICE_ACCOUNT_BASE64: {}", ex.getMessage());
                throw new IllegalStateException("[Firebase] Invalid Base64 string provided in FIREBASE_SERVICE_ACCOUNT_BASE64.", ex);
            } catch (IOException ex) {
                log.error("[Firebase] Failed to parse decoded Firebase JSON from Base64: {}", ex.getMessage());
                throw new IllegalStateException("[Firebase] Decoded Base64 content is not valid Firebase service account JSON.", ex);
            }
        }

        // ── 2. Priority 2: FIREBASE_SERVICE_ACCOUNT_JSON (Raw JSON fallback) ──
        String rawJson = getServiceAccountJsonString();
        if (rawJson != null && !rawJson.trim().isEmpty()) {
            String trimmedJson = rawJson.trim();
            if ((trimmedJson.startsWith("'") && trimmedJson.endsWith("'")) ||
                (trimmedJson.startsWith("\"") && trimmedJson.endsWith("\"") && trimmedJson.contains("{"))) {
                trimmedJson = trimmedJson.substring(1, trimmedJson.length() - 1).trim();
            }
            if (trimmedJson.startsWith("\uFEFF")) {
                trimmedJson = trimmedJson.substring(1).trim();
            }

            try (InputStream stream = new ByteArrayInputStream(trimmedJson.getBytes(StandardCharsets.UTF_8))) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                log.info("[Firebase] Firebase credentials source = ENV_JSON");
                return credentials;
            } catch (Exception ex) {
                log.error("[Firebase] Failed to parse Firebase service account JSON from environment variable: {}", ex.getMessage());
                throw new IllegalStateException("[Firebase] Invalid FIREBASE_SERVICE_ACCOUNT_JSON provided in environment variables.", ex);
            }
        }

        // ── 3. Priority 3: Local File Path (Mounted secret file / Local development) ──
        if (serviceAccountPath != null && !serviceAccountPath.trim().isEmpty()) {
            Path jsonPath = Paths.get(serviceAccountPath.trim()).toAbsolutePath();
            if (Files.exists(jsonPath)) {
                try (InputStream stream = new FileInputStream(jsonPath.toFile())) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                    log.info("[Firebase] Firebase credentials source = LOCAL_FILE");
                    return credentials;
                } catch (IOException ex) {
                    log.error("[Firebase] Failed to read Service Account JSON file at {}: {}", jsonPath, ex.getMessage());
                    throw ex;
                }
            }
        }

        // ── 4. Priority 4: Classpath Resource (Local IDE / JAR resource fallback) ──
        try (InputStream cpStream = getClass().getClassLoader().getResourceAsStream("firebase/firebase-service-account.json")) {
            if (cpStream != null) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(cpStream);
                log.info("[Firebase] Firebase credentials source = LOCAL_FILE");
                return credentials;
            }
        } catch (Exception ignored) {
            // fallback failed
        }

        // ── 5. Error if no credentials found ──
        log.error("[Firebase] Firebase service account credentials: NOT CONFIGURED");
        throw new IllegalStateException(
            "[Firebase] Service Account credentials not found. " +
            "Please provide FIREBASE_SERVICE_ACCOUNT_BASE64 as an environment variable (for Render/production) " +
            "or place firebase-service-account.json at " + serviceAccountPath + " (for local development).");
    }

    private String getServiceAccountBase64String() {
        if (serviceAccountBase64 != null && !serviceAccountBase64.trim().isEmpty()) {
            return serviceAccountBase64;
        }
        String env = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");
        if (env != null && !env.trim().isEmpty()) {
            return env;
        }
        String prop = System.getProperty("FIREBASE_SERVICE_ACCOUNT_BASE64");
        if (prop != null && !prop.trim().isEmpty()) {
            return prop;
        }
        return null;
    }

    private String getServiceAccountJsonString() {
        if (serviceAccountJson != null && !serviceAccountJson.trim().isEmpty()) {
            return serviceAccountJson;
        }
        String env = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (env != null && !env.trim().isEmpty()) {
            return env;
        }
        String prop = System.getProperty("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (prop != null && !prop.trim().isEmpty()) {
            return prop;
        }
        return null;
    }
}
