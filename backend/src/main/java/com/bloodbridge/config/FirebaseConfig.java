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
 * <p>Initializes FirebaseApp and exposes the {@link FirebaseMessaging} bean.
 * Supports loading credentials securely with the following priority:
 * <ol>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_JSON} environment variable / system property (Production / Render Docker).</li>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_PATH} file path (Mounted secrets / local development).</li>
 *   <li>Classpath fallback at {@code firebase/firebase-service-account.json} (Local development).</li>
 * </ol>
 * Sensitive credentials and private keys are NEVER logged.</p>
 */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class FirebaseConfig {

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

        // ── Resolve Project ID (if not explicitly set, extract from ServiceAccountCredentials) ──
        String resolvedProjectId = (projectId != null && !projectId.isBlank()) ? projectId.trim() : null;
        if (resolvedProjectId == null && credentials instanceof ServiceAccountCredentials sac) {
            resolvedProjectId = sac.getProjectId();
        }

        FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                .setCredentials(credentials);

        if (resolvedProjectId != null && !resolvedProjectId.isBlank()) {
            optionsBuilder.setProjectId(resolvedProjectId);
            log.info("[Firebase] Project ID: {}", resolvedProjectId);
        } else {
            log.info("[Firebase] Project ID: <AUTO-RESOLVED>");
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
     * Resolves GoogleCredentials safely from environment variable, file system, or classpath.
     */
    private GoogleCredentials resolveCredentials() throws IOException {
        // 1. Check FIREBASE_SERVICE_ACCOUNT_JSON (Environment Variable / System Property)
        String rawJson = getServiceAccountJsonString();
        if (rawJson != null && !rawJson.trim().isEmpty()) {
            String trimmedJson = rawJson.trim();

            // Strip outer wrapping quotes if present (e.g. '{"type":...}' or "{\"type\":...}")
            if ((trimmedJson.startsWith("'") && trimmedJson.endsWith("'")) ||
                (trimmedJson.startsWith("\"") && trimmedJson.endsWith("\"") && trimmedJson.contains("{"))) {
                trimmedJson = trimmedJson.substring(1, trimmedJson.length() - 1).trim();
            }

            try {
                return parseGoogleCredentialsFromJson(trimmedJson);
            } catch (Exception ex) {
                // Attempt fallback if escaped newlines were double-escaped in environment variable
                if (trimmedJson.contains("\\\\n")) {
                    try {
                        String unescapedJson = trimmedJson.replace("\\\\n", "\\n");
                        return parseGoogleCredentialsFromJson(unescapedJson);
                    } catch (Exception ignored) {
                        // proceed to throw original error
                    }
                }
                log.error("[Firebase] Failed to parse Firebase service account JSON from environment variable: {}", ex.getMessage());
                throw new IllegalStateException("[Firebase] Invalid FIREBASE_SERVICE_ACCOUNT_JSON provided in environment variables.", ex);
            }
        }

        // 2. Check File Path (Local development / mounted secret file)
        if (serviceAccountPath != null && !serviceAccountPath.trim().isEmpty()) {
            Path jsonPath = Paths.get(serviceAccountPath.trim()).toAbsolutePath();
            if (Files.exists(jsonPath)) {
                try (InputStream stream = new FileInputStream(jsonPath.toFile())) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                    log.info("[Firebase] Firebase service account credentials: CONFIGURED (source: local file)");
                    return credentials;
                } catch (IOException ex) {
                    log.error("[Firebase] Failed to read Service Account JSON file at {}: {}", jsonPath, ex.getMessage());
                    throw ex;
                }
            }
        }

        // 3. Check Classpath (Local IDE / JAR resource fallback)
        try (InputStream cpStream = getClass().getClassLoader().getResourceAsStream("firebase/firebase-service-account.json")) {
            if (cpStream != null) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(cpStream);
                log.info("[Firebase] Firebase service account credentials: CONFIGURED (source: classpath)");
                return credentials;
            }
        } catch (Exception ignored) {
            // fallback failed
        }

        // 4. If neither exists, produce a clear, actionable error
        log.error("[Firebase] Firebase service account credentials: NOT CONFIGURED");
        throw new IllegalStateException(
            "[Firebase] Service Account credentials not found. " +
            "Please provide FIREBASE_SERVICE_ACCOUNT_JSON as an environment variable (for Render/production) " +
            "or place firebase-service-account.json at " + serviceAccountPath + " (for local development).");
    }

    private GoogleCredentials parseGoogleCredentialsFromJson(String json) throws IOException {
        try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            log.info("[Firebase] Firebase service account credentials: CONFIGURED");
            return credentials;
        }
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
