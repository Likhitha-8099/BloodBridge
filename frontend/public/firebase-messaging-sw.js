/**
 * firebase-messaging-sw.js — Firebase Cloud Messaging Service Worker
 *
 * Phase 3B: Background push notification handler.
 *
 * This Service Worker MUST be placed in the /public directory so Vite
 * serves it at the root path: http://localhost:5173/firebase-messaging-sw.js
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
 * They do NOT have access to import.meta.env or Vite's module system.
 * Firebase config values MUST be hardcoded or injected via importScripts.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * MANUAL STEP REQUIRED:
 * After you add your Firebase credentials to frontend/.env, you MUST also
 * paste your firebaseConfig values into the self.firebaseConfig object below.
 * This is the only file where values appear directly — Service Workers cannot
 * use import.meta.env.
 * ─────────────────────────────────────────────────────────────────────────────
 */

// ── Step 1: Import Firebase scripts via CDN ──────────────────────────────
// Firebase 10.x compat scripts are required for Service Workers.
// The compat layer supports importScripts() which Service Workers use.
importScripts('https://www.gstatic.com/firebasejs/10.12.5/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.12.5/firebase-messaging-compat.js');

// ── Step 2: Firebase configuration ───────────────────────────────────────
// MANUAL: Paste your Firebase config values here after getting them from
// Firebase Console → Project Settings → General → Your apps → Web app config
//
// These values match what you put in frontend/.env but are needed separately
// because Service Workers cannot access import.meta.env or process.env.
//
// ⚠ NOTE: These values are NOT secret — they are public client-side keys
// that Firebase uses to identify your app. The actual security is enforced
// by Firebase Security Rules and your backend.

const firebaseConfig = {
  apiKey: 'AIzaSyB-qyrOqjA1Zky_rXnHjtj5UVzPHgRlKHY',
  authDomain: 'bloodbridge-12b62.firebaseapp.com',
  projectId: 'bloodbridge-12b62',
  storageBucket: 'bloodbridge-12b62.firebasestorage.app',
  messagingSenderId: '908018823527',
  appId: '1:908018823527:web:0a4be5067951be60253fc2',
};
// ── Step 3: Initialize Firebase in the Service Worker context ─────────────
if (firebaseConfig.apiKey && firebaseConfig.apiKey.trim() !== '') {
  firebase.initializeApp(firebaseConfig);

  // ── Step 4: Get Messaging instance ──────────────────────────────────────
  const messaging = firebase.messaging();

  // ── Step 5: Handle background messages ──────────────────────────────────
  // This handler fires when a push notification arrives and:
  //   - The browser tab is closed
  //   - BloodBridge is in the background
  //   - The screen is locked
  //
  // Phase 3B Skeleton: logs payload only.
  // Custom notification display logic will be added in Phase 3B Step 4.
  messaging.onBackgroundMessage((payload) => {
    console.log('[SW-Firebase] 📬 Background message received:', payload);

    // Extract notification data from payload
    const notificationTitle = payload?.notification?.title || 'BloodBridge Alert';
    const notificationBody = payload?.notification?.body || 'You have a new notification.';
    const notificationIcon = payload?.notification?.icon || '/favicon.svg';
    const notificationBadge = '/favicon.svg';
    const notificationData = payload?.data || {};

    // Display the notification
    // In Step 4, this will be extended with action buttons, deep links, etc.
    const notificationOptions = {
      body: notificationBody,
      icon: notificationIcon,
      badge: notificationBadge,
      data: notificationData,
      // vibrate and actions will be configured in Step 4
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
  });

  // ── Step 6: Handle notification click behavior ──────────────────────────
  self.addEventListener('notificationclick', (event) => {
    console.log('[SW-Firebase] 👆 Notification clicked:', event.notification);
    event.notification.close();

    const data = event.notification.data || {};
    const requestId = data.requestId;
    const clickAction = data.clickAction || '/donor/dashboard';
    const targetUrl = requestId ? `${clickAction}?emergencyId=${requestId}` : clickAction;

    event.waitUntil(
      clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
        for (const client of clientList) {
          if (client.url && 'focus' in client) {
            client.navigate(targetUrl);
            return client.focus();
          }
        }
        if (clients.openWindow) {
          return clients.openWindow(targetUrl);
        }
      })
    );
  });

  console.log('[SW-Firebase] ✔ Firebase Messaging Service Worker initialized.');
} else {
  // Config not yet filled in — log warning but do not throw
  console.warn(
    '[SW-Firebase] ⚠ Firebase config is empty in firebase-messaging-sw.js.\n' +
    'After pasting your Firebase credentials into frontend/.env,\n' +
    'also paste them into the firebaseConfig object in public/firebase-messaging-sw.js.\n' +
    'Background notifications will not work until this is done.'
  );
}
