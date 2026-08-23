package com.bloodbridge.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class FirebaseConfigTest {

    @Test
    @DisplayName("Should throw IllegalStateException on malformed JSON in FIREBASE_SERVICE_ACCOUNT_JSON")
    void testMalformedJsonThrowsException() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "serviceAccountJson", "{ not valid json }");
        ReflectionTestUtils.setField(config, "serviceAccountPath", "non/existent/path/firebase.json");
        ReflectionTestUtils.setField(config, "projectId", "test-project");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::firebaseMessaging);
        assertTrue(ex.getMessage().contains("FIREBASE_SERVICE_ACCOUNT_JSON"));
    }
}
