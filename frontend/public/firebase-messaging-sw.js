/**
 * firebase-messaging-sw.js — Firebase Cloud Messaging Service Worker
 *
 * Phase 3B: Background push notification handler.
 *
 * This Service Worker MUST be placed in the /public directory so Vite
 * serves it at the root path: /firebase-messaging-sw.js
 *
 * WHY this file is required:
 * Firebase Cloud Messaging uses Service Workers to receive and display
 * push notifications when the browser tab/window is:
 *   - Closed
 *   - In the background
 *   - Screen is locked
 *   - User is not actively using BloodBridge
 *
 * IMPORTANT: Service Workers run in a separate thread from the main page.
 * Configuration parameters are dynamically passed to the Service Worker via URL
 * query parameters when registered by the main application in firebase-messaging.js.
 * This avoids hardcoding secrets or API keys in tracked source files.
 */

// ── Step 1: Import Firebase scripts via CDN ──────────────────────────────
// Firebase 10.x compat scripts are required for Service Workers.
// The compat layer supports importScripts() which Service Workers use.
importScripts('https://www.gstatic.com/firebasejs/10.12.5/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.12.5/firebase-messaging-compat.js');

// ── Step 2: Firebase configuration ───────────────────────────────────────
// Extract configuration parameters dynamically from self.location.search.
// The main application passes VITE_FIREBASE_* environment variables during SW registration.
const params = new URLSearchParams(self.location.search);

const firebaseConfig = {
  apiKey: params.get('apiKey') || '',
  authDomain: params.get('authDomain') || '',
  projectId: params.get('projectId') || '',
  storageBucket: params.get('storageBucket') || '',
  messagingSenderId: params.get('messagingSenderId') || '',
  appId: params.get('appId') || '',
};

// ── Step 3: Initialize Firebase in the Service Worker context ─────────────
try {
  if (firebaseConfig.apiKey && firebaseConfig.apiKey.trim() !== '') {
    // Idempotent initialization: avoid duplicate app initialization errors
    if (!firebase.apps.length) {
      firebase.initializeApp(firebaseConfig);
    }

    // ── Step 4: Get Messaging instance ──────────────────────────────────────
    const messaging = firebase.messaging();

    // ── Step 5: Handle background messages ──────────────────────────────────
    // This handler fires when a push notification arrives and:
    //   - The browser tab is closed
    //   - BloodBridge is in the background
    //   - The screen is locked
    messaging.onBackgroundMessage((payload) => {
      console.log('[SW-Firebase] 📬 Background message received:', payload);

      // Extract notification data from payload
      const notificationTitle = payload?.notification?.title || payload?.data?.title || 'BloodBridge Alert';
      const notificationBody = payload?.notification?.body || payload?.data?.body || 'You have a new notification.';
      const notificationIcon = payload?.notification?.icon || payload?.data?.icon || '/favicon.svg';
      const notificationBadge = '/favicon.svg';
      const notificationData = payload?.data || {};

      // Display the notification
      const notificationOptions = {
        body: notificationBody,
        icon: notificationIcon,
        badge: notificationBadge,
        data: notificationData,
      };

      self.registration.showNotification(notificationTitle, notificationOptions);
    });

    console.log('[SW-Firebase] ✔ Firebase Messaging Service Worker initialized.');
  } else {
    // Config not yet set — log warning but do not throw
    console.warn(
      '[SW-Firebase] ⚠ Firebase config is empty or missing from URL query parameters.\n' +
      'Ensure VITE_FIREBASE_* environment variables are configured in frontend/.env.\n' +
      'Background notifications will not work until values are set.'
    );
  }
} catch (initErr) {
  console.error('[SW-Firebase] ✘ Firebase initialization error in Service Worker:', initErr);
}

// ── Step 6: Handle notification click behavior ──────────────────────────
self.addEventListener('notificationclick', (event) => {
  console.log('[SW-Firebase] 👆 Notification clicked:', event.notification);
  event.notification.close();

  const data = event.notification.data || {};
  const requestId = data.requestId;
  const clickAction = data.clickAction || '/donor/dashboard';
  const targetUrl = requestId ? `${clickAction}?emergencyId=${requestId}` : clickAction;

  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if (client.url && 'focus' in client) {
          client.navigate(targetUrl);
          return client.focus();
        }
      }
      if (self.clients.openWindow) {
        return self.clients.openWindow(targetUrl);
      }
    })
  );
});


