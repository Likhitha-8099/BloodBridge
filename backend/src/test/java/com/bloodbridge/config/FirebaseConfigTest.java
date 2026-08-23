package com.bloodbridge.config;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class FirebaseConfigTest {

    @Test
    @DisplayName("Should prioritize FIREBASE_SERVICE_ACCOUNT_BASE64 even if FIREBASE_SERVICE_ACCOUNT_JSON is malformed")
    void testBase64TakesPriorityOverMalformedJson() throws Exception {
        Path localJsonPath = Path.of("src/main/resources/firebase/firebase-service-account.json");
        if (Files.exists(localJsonPath)) {
            String rawJson = Files.readString(localJsonPath);
            String base64Encoded = Base64.getEncoder().encodeToString(rawJson.getBytes());

            FirebaseConfig config = new FirebaseConfig();
            ReflectionTestUtils.setField(config, "serviceAccountBase64", base64Encoded);
            ReflectionTestUtils.setField(config, "serviceAccountJson", "{ this is malformed json }");
            ReflectionTestUtils.setField(config, "serviceAccountPath", "non/existent/path.json");
            ReflectionTestUtils.setField(config, "projectId", "bloodbridge-12b62");

            FirebaseMessaging messaging = config.firebaseMessaging();
            assertNotNull(messaging);
        }
    }

    @Test
    @DisplayName("Should throw IllegalStateException on invalid Base64 in FIREBASE_SERVICE_ACCOUNT_BASE64")
    void testInvalidBase64ThrowsException() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "serviceAccountBase64", "!!!invalid-base64!!!");
        ReflectionTestUtils.setField(config, "serviceAccountJson", "");
        ReflectionTestUtils.setField(config, "serviceAccountPath", "non/existent/path/firebase.json");
        ReflectionTestUtils.setField(config, "projectId", "test-project");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::firebaseMessaging);
        assertTrue(ex.getMessage().contains("FIREBASE_SERVICE_ACCOUNT_BASE64"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException on non-JSON content inside Base64")
    void testNonJsonBase64ThrowsException() {
        FirebaseConfig config = new FirebaseConfig();
        String encodedNonJson = Base64.getEncoder().encodeToString("not a json object".getBytes());
        ReflectionTestUtils.setField(config, "serviceAccountBase64", encodedNonJson);
        ReflectionTestUtils.setField(config, "serviceAccountJson", "");
        ReflectionTestUtils.setField(config, "serviceAccountPath", "non/existent/path/firebase.json");
        ReflectionTestUtils.setField(config, "projectId", "test-project");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::firebaseMessaging);
        assertTrue(ex.getMessage().contains("FIREBASE_SERVICE_ACCOUNT_BASE64") || ex.getMessage().contains("Firebase service account JSON"));
    }

    @Test
    @DisplayName("Should successfully load credentials from local classpath / file when Base64 is empty")
    void testLocalFileFallback() throws Exception {
        Path localJsonPath = Path.of("src/main/resources/firebase/firebase-service-account.json");
        if (Files.exists(localJsonPath)) {
            FirebaseConfig config = new FirebaseConfig();
            ReflectionTestUtils.setField(config, "serviceAccountBase64", "");
            ReflectionTestUtils.setField(config, "serviceAccountJson", "");
            ReflectionTestUtils.setField(config, "serviceAccountPath", localJsonPath.toString());
            ReflectionTestUtils.setField(config, "projectId", "bloodbridge-12b62");

            FirebaseMessaging messaging = config.firebaseMessaging();
            assertNotNull(messaging);
        }
    }
}
