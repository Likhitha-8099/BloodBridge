/**
 * firebase-messaging.js — FCM Messaging Helpers
 *
 * Phase 3B: Firebase Cloud Messaging helper functions.
 *
 * Exports:
 *   - messaging              : FirebaseMessaging instance (or null if unsupported)
 *   - isFcmSupported()       : boolean — is FCM supported in this browser?
 *   - requestNotificationPermission() : requests browser notification permission
 *   - getDeviceToken()       : retrieves the FCM device registration token
 *   - onForegroundMessage()  : registers a foreground message listener
 *
 * NO UI code in this file.
 * NO backend API calls in this file.
 * NO notification display logic in this file.
 * Those belong in the hook/component layer (Phase 3B Step 4).
 *
 * Required environment variables:
 *   VITE_FIREBASE_VAPID_KEY  — Web Push Certificate from Firebase Console
 */

import { getMessaging, getToken, onMessage, isSupported } from 'firebase/messaging';
import firebaseApp from './firebase';

// ── Messaging instance (lazily resolved — null if unsupported) ────────────
let _messaging = null;

/**
 * Returns true if Firebase Cloud Messaging is supported in this browser.
 * FCM requires:
 *  - Service Workers API
 *  - Push API
 *  - Notification API
 * Safari on iOS < 16.4, and some privacy-focused browsers do not support it.
 *
 * @returns {Promise<boolean>}
 */
export const isFcmSupported = async () => {
  try {
    const supported = await isSupported();
    if (!supported) {
      console.warn('[FCM] ⚠ Firebase Messaging is not supported in this browser.');
    }
    return supported;
  } catch (err) {
    console.warn('[FCM] ⚠ Could not determine FCM support:', err.message);
    return false;
  }
};

/**
 * Returns the FirebaseMessaging instance, initializing it on first call.
 * Returns null if FCM is not supported in the current browser.
 *
 * @returns {Promise<import('firebase/messaging').Messaging | null>}
 */
export const getMessagingInstance = async () => {
  if (_messaging) return _messaging;

  const supported = await isFcmSupported();
  if (!supported) return null;

  try {
    _messaging = getMessaging(firebaseApp);
    console.info('[FCM] ✔ FirebaseMessaging instance obtained.');
    return _messaging;
  } catch (err) {
    console.error('[FCM] ✘ Failed to get FirebaseMessaging instance:', err.message);
    return null;
  }
};

/**
 * Requests browser Notification permission from the user.
 *
 * Returns one of:
 *   'granted'  — user allowed notifications
 *   'denied'   — user blocked notifications (cannot be re-requested programmatically)
 *   'default'  — user dismissed the prompt (can retry)
 *   'unsupported' — browser does not support the Notification API
 *
 * @returns {Promise<'granted' | 'denied' | 'default' | 'unsupported'>}
 */
export const requestNotificationPermission = async () => {
  if (!('Notification' in window)) {
    console.warn('[FCM] ⚠ This browser does not support the Notification API.');
    return 'unsupported';
  }

  // If already granted or denied, return immediately without prompting again
  if (Notification.permission === 'granted') {
    console.info('[FCM] ✔ Notification permission already granted.');
    return 'granted';
  }

  if (Notification.permission === 'denied') {
    console.warn('[FCM] ⚠ Notification permission is denied. ' +
      'User must manually enable it in browser settings.');
    return 'denied';
  }

  try {
    console.info('[FCM] Requesting notification permission from user...');
    const permission = await Notification.requestPermission();
    console.info(`[FCM] Permission result: ${permission}`);
    return permission;
  } catch (err) {
    console.error('[FCM] ✘ Error requesting notification permission:', err.message);
    return 'default';
  }
};

/**
 * Retrieves the FCM device registration token for this browser/device.
 *
 * Prerequisites:
 *   1. Notification permission must be 'granted'
 *   2. VITE_FIREBASE_VAPID_KEY must be set in frontend/.env
 *   3. The Service Worker (public/firebase-messaging-sw.js) must be registered
 *
 * @returns {Promise<string | null>} FCM token string, or null on failure
 */
export const getDeviceToken = async () => {
  const vapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY;

  if (!vapidKey || vapidKey.trim() === '') {
    console.error('[FCM] ✘ VITE_FIREBASE_VAPID_KEY is not set in frontend/.env. ' +
      'Cannot generate device token.');
    return null;
  }

  const messaging = await getMessagingInstance();
  if (!messaging) {
    console.warn('[FCM] ⚠ Messaging not available — skipping token generation.');
    return null;
  }

  const permission = Notification.permission;
  if (permission !== 'granted') {
    console.warn(`[FCM] ⚠ Cannot get token — notification permission is '${permission}'. ` +
      'Call requestNotificationPermission() first.');
    return null;
  }

  try {
    console.info('[FCM] Requesting device registration token...');
    const token = await getToken(messaging, { vapidKey });

    if (token) {
      console.info('[FCM] ✔ Device token obtained successfully.');
      // Log only first 20 chars for debugging (do not log full token)
      console.debug('[FCM] Token preview:', token.substring(0, 20) + '...');
      return token;
    } else {
      console.warn('[FCM] ⚠ No token received. Possible reasons:' +
        '\n  • Service Worker not registered' +
        '\n  • VAPID key mismatch' +
        '\n  • Firebase project misconfigured');
      return null;
    }
  } catch (err) {
    console.error('[FCM] ✘ Failed to get device token:', err.message);
    return null;
  }
};

/**
 * Registers a foreground message listener.
 *
 * Called when the app is in the FOREGROUND (tab is open and active).
 * Background messages are handled by the Service Worker (firebase-messaging-sw.js).
 *
 * @param {function(payload: object): void} callback
 *   Called with the raw FCM message payload when a message arrives
 * @returns {Promise<function | null>}
 *   Unsubscribe function (call it to stop listening), or null if not supported
 *
 * @example
 *   const unsubscribe = await onForegroundMessage((payload) => {
 *     console.log('Message received:', payload);
 *   });
 *   // Later:
 *   unsubscribe?.();
 */
export const onForegroundMessage = async (callback) => {
  const messaging = await getMessagingInstance();
  if (!messaging) {
    console.warn('[FCM] ⚠ Foreground listener not registered — messaging not supported.');
    return null;
  }

  console.info('[FCM] ✔ Foreground message listener registered.');
  const unsubscribe = onMessage(messaging, (payload) => {
    console.info('[FCM] 📬 Foreground message received:', payload?.notification?.title);
    callback(payload);
  });

  return unsubscribe;
};

export default {
  isFcmSupported,
  getMessagingInstance,
  requestNotificationPermission,
  getDeviceToken,
  onForegroundMessage,
};
