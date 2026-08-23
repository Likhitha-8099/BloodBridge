package com.bloodbridge.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class FirebaseConfigTest {

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
    @DisplayName("Should throw IllegalStateException on malformed JSON in FIREBASE_SERVICE_ACCOUNT_JSON fallback")
    void testMalformedJsonThrowsException() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "serviceAccountBase64", "");
        ReflectionTestUtils.setField(config, "serviceAccountJson", "{ not valid json }");
        ReflectionTestUtils.setField(config, "serviceAccountPath", "non/existent/path/firebase.json");
        ReflectionTestUtils.setField(config, "projectId", "test-project");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::firebaseMessaging);
        assertTrue(ex.getMessage().contains("FIREBASE_SERVICE_ACCOUNT_JSON"));
    }
}
