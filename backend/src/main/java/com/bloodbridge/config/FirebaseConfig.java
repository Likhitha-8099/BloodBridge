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
 * Firebase Admin SDK Configuration — Production & Local Development.
 *
 * <p>Initializes FirebaseApp and exposes the {@link FirebaseMessaging} bean.
 * Credentials Priority:
 * <ol>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_BASE64} (Highest Priority - Production / Render)</li>
 *   <li>{@code FIREBASE_SERVICE_ACCOUNT_JSON} (Optional Legacy Fallback)</li>
 *   <li>Local file at {@code FIREBASE_SERVICE_ACCOUNT_PATH}</li>
 *   <li>Local classpath file at {@code firebase/firebase-service-account.json}</li>
 * </ol>
 * Secrets, private keys, and Base64 tokens are NEVER logged.</p>
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
        log.info("[Firebase] Initializing Firebase Admin SDK");

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
            log.info("[Firebase] Firebase project ID: {}", resolvedProjectId);
        } else {
            log.info("[Firebase] Firebase project ID: <AUTO-RESOLVED>");
        }

        FirebaseOptions options = optionsBuilder.build();

        // ── Idempotent initialization guard ──
        FirebaseApp app;
        if (FirebaseApp.getApps().isEmpty()) {
            app = FirebaseApp.initializeApp(options);
            log.info("[Firebase] ✔ FirebaseApp initialized successfully: {}", app.getName());
        } else {
            app = FirebaseApp.getInstance();
            log.info("[Firebase] ✔ FirebaseApp already initialized, reusing existing instance.");
        }

        log.info("[Firebase] ✔ Firebase Admin SDK Ready — FirebaseMessaging bean active.");
        log.info("[Firebase] ═══════════════════════════════════════════════");

        return FirebaseMessaging.getInstance(app);
    }

    /**
     * Resolves GoogleCredentials with strict priority order:
     * 1. FIREBASE_SERVICE_ACCOUNT_BASE64
     * 2. FIREBASE_SERVICE_ACCOUNT_JSON
     * 3. Local filesystem path
     * 4. Local classpath resource
     */
    private GoogleCredentials resolveCredentials() throws IOException {
        // ── 1. Priority 1: FIREBASE_SERVICE_ACCOUNT_BASE64 (Render / Production) ──
        String base64Env = getEffectiveBase64();
        if (base64Env != null && !base64Env.isBlank()) {
            String cleanedBase64 = base64Env.trim();
            // Remove wrapping quotes if present
            if ((cleanedBase64.startsWith("'") && cleanedBase64.endsWith("'")) ||
                (cleanedBase64.startsWith("\"") && cleanedBase64.endsWith("\""))) {
                cleanedBase64 = cleanedBase64.substring(1, cleanedBase64.length() - 1).trim();
            }
            // Strip any internal whitespace/newlines
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
                    log.info("[Firebase] Credentials source: ENV_BASE64");
                    return credentials;
                }
            } catch (IllegalArgumentException ex) {
                log.error("[Firebase] Failed to Base64 decode FIREBASE_SERVICE_ACCOUNT_BASE64: {}", ex.getMessage());
                throw new IllegalStateException("[Firebase] Invalid Base64 format in FIREBASE_SERVICE_ACCOUNT_BASE64.", ex);
            } catch (IOException ex) {
                log.error("[Firebase] Failed to parse Firebase credentials from decoded Base64 JSON: {}", ex.getMessage());
                throw new IllegalStateException("[Firebase] Decoded Base64 content is not valid Firebase service account JSON.", ex);
            }
        }

        // ── 2. Priority 2: FIREBASE_SERVICE_ACCOUNT_JSON (Raw JSON Fallback) ──
        String jsonEnv = getEffectiveJson();
        if (jsonEnv != null && !jsonEnv.isBlank()) {
            String trimmedJson = jsonEnv.trim();
            if ((trimmedJson.startsWith("'") && trimmedJson.endsWith("'")) ||
                (trimmedJson.startsWith("\"") && trimmedJson.endsWith("\"") && trimmedJson.contains("{"))) {
                trimmedJson = trimmedJson.substring(1, trimmedJson.length() - 1).trim();
            }
            if (trimmedJson.startsWith("\uFEFF")) {
                trimmedJson = trimmedJson.substring(1).trim();
            }

            try (InputStream stream = new ByteArrayInputStream(trimmedJson.getBytes(StandardCharsets.UTF_8))) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                log.info("[Firebase] Credentials source: ENV_JSON");
                return credentials;
            } catch (Exception ex) {
                log.warn("[Firebase] Could not parse raw FIREBASE_SERVICE_ACCOUNT_JSON: {}. Trying local fallbacks...", ex.getMessage());
            }
        }

        // ── 3. Priority 3: Local File Path (Mounted secret file / Local development) ──
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            Path jsonPath = Paths.get(serviceAccountPath.trim()).toAbsolutePath();
            if (Files.exists(jsonPath)) {
                try (InputStream stream = new FileInputStream(jsonPath.toFile())) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                    log.info("[Firebase] Credentials source: LOCAL_FILE ({})", jsonPath.getFileName());
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
                log.info("[Firebase] Credentials source: LOCAL_FILE (classpath:firebase/firebase-service-account.json)");
                return credentials;
            }
        } catch (Exception ignored) {
            // fallback failed
        }

        // ── 5. Error if no credentials could be resolved ──
        log.error("[Firebase] Credentials source: NONE - No valid credentials found");
        throw new IllegalStateException(
            "[Firebase] Service Account credentials not found. " +
            "Please provide FIREBASE_SERVICE_ACCOUNT_BASE64 as an environment variable (for Render/production) " +
            "or place firebase-service-account.json at " + serviceAccountPath + " (for local development).");
    }

    private String getEffectiveBase64() {
        String env = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");
        if (env != null && !env.isBlank()) {
            return env;
        }
        String prop = System.getProperty("FIREBASE_SERVICE_ACCOUNT_BASE64");
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            return serviceAccountBase64;
        }
        return null;
    }

    private String getEffectiveJson() {
        String env = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (env != null && !env.isBlank()) {
            return env;
        }
        String prop = System.getProperty("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return serviceAccountJson;
        }
        return null;
    }
}
