package com.bloodbridge.controller;

import com.bloodbridge.dto.response.ApiResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Firebase Debug Endpoint — Phase 3B.
 *
 * <p>Provides a health-check endpoint to verify Firebase configuration
 * at runtime WITHOUT exposing any credentials or secrets.</p>
 *
 * <p>Accessible at: {@code GET /api/debug/firebase}</p>
 *
 * <p>This endpoint is permitted in SecurityConfig under {@code /api/debug/**}
 * so it can be called without authentication during local development.</p>
 *
 * <p><strong>IMPORTANT:</strong> This endpoint is for development/staging only.
 * Disable or restrict it in production via {@code firebase.debug.enabled=false}.</p>
 */
@RestController
@RequestMapping({"/api/v1/debug", "/api/debug"})
@Slf4j
public class FirebaseDebugController {

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.service-account-json:${FIREBASE_SERVICE_ACCOUNT_JSON:}}")
    private String serviceAccountJson;

    @Value("${firebase.service-account-path:${FIREBASE_SERVICE_ACCOUNT_PATH:src/main/resources/firebase/firebase-service-account.json}}")
    private String serviceAccountPath;

    @Value("${firebase.project-id:${FIREBASE_PROJECT_ID:}}")
    private String projectId;

    private final ApplicationContext applicationContext;

    public FirebaseDebugController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Returns Firebase initialization health status.
     * Never exposes credential values — only boolean/status fields.
     *
     * @return structured JSON health check for Firebase setup
     */
    @GetMapping("/firebase")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFirebaseStatus() {
        log.info("[Firebase-Debug] Health check requested.");

        Map<String, Object> status = new LinkedHashMap<>();

        // ── 1. Is Firebase enabled in config? ─────────────────────────────────
        status.put("firebase_enabled_in_config", firebaseEnabled);

        // ── 2. Is Project ID set? (show value — it's not secret) ──────────────
        boolean projectIdSet = projectId != null && !projectId.isBlank();
        status.put("project_id_configured", projectIdSet);
        status.put("project_id_value", projectIdSet ? projectId : "<AUTO-RESOLVE>");

        // ── 3. Credentials resolution (ENV VAR or LOCAL FILE or CLASSPATH) ───
        String resolvedJson = getEffectiveServiceAccountJson();
        boolean jsonStringConfigured = resolvedJson != null && !resolvedJson.trim().isEmpty();
        Path jsonPath = Paths.get(serviceAccountPath != null ? serviceAccountPath.trim() : "").toAbsolutePath();
        boolean jsonFileExists = Files.exists(jsonPath);
        boolean classpathResourceExists = getClass().getClassLoader().getResource("firebase/firebase-service-account.json") != null;

        status.put("service_account_json_env_configured", jsonStringConfigured);
        status.put("service_account_path_configured", serviceAccountPath != null && !serviceAccountPath.isBlank());
        status.put("service_account_file_found", jsonFileExists);
        status.put("service_account_resolved_path", jsonPath.toString());
        status.put("service_account_classpath_found", classpathResourceExists);
        status.put("credentials_source", jsonStringConfigured ? "ENVIRONMENT_VARIABLE" : (jsonFileExists ? "LOCAL_FILE" : (classpathResourceExists ? "CLASSPATH" : "NONE")));

        // ── 4. Is FirebaseApp initialized in the JVM? ─────────────────────────
        boolean firebaseAppReady = !FirebaseApp.getApps().isEmpty();
        status.put("firebase_app_initialized", firebaseAppReady);
        if (firebaseAppReady) {
            status.put("firebase_app_name", FirebaseApp.getInstance().getName());
        }

        // ── 5. Is FirebaseMessaging bean available in Spring context? ──────────
        boolean messagingBeanPresent = false;
        try {
            applicationContext.getBean(FirebaseMessaging.class);
            messagingBeanPresent = true;
        } catch (Exception e) {
            // Bean not registered — firebase.enabled=false or credentials missing
        }
        status.put("firebase_messaging_bean_available", messagingBeanPresent);

        // ── 6. Overall readiness ───────────────────────────────────────────────
        boolean credentialsFound = jsonStringConfigured || jsonFileExists || classpathResourceExists;
        boolean allReady = firebaseEnabled && credentialsFound && firebaseAppReady && messagingBeanPresent;
        status.put("overall_status", allReady ? "READY ✔" : "NOT READY — Check items above");

        // ── 7. Guidance if not ready ───────────────────────────────────────────
        if (!allReady) {
            Map<String, String> nextSteps = new LinkedHashMap<>();
            if (!firebaseEnabled) {
                nextSteps.put("step_1", "Set FIREBASE_ENABLED=true in your environment variables / .env");
            }
            if (!credentialsFound) {
                nextSteps.put("step_2", "Set FIREBASE_SERVICE_ACCOUNT_JSON in environment variables OR place firebase-service-account.json at: " + jsonPath);
            }
            if (!firebaseAppReady) {
                nextSteps.put("step_3", "FirebaseApp not initialized — fix credentials configuration first");
            }
            if (!messagingBeanPresent) {
                nextSteps.put("step_4", "FirebaseMessaging bean not available — ensure FIREBASE_ENABLED=true and valid credentials");
            }
            status.put("next_steps", nextSteps);
        }

        log.info("[Firebase-Debug] Status check complete. Overall: {}", allReady ? "READY" : "NOT READY");

        return ResponseEntity.ok(ApiResponse.success("Firebase health check complete", status));
    }

    private String getEffectiveServiceAccountJson() {
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
