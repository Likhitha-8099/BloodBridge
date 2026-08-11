/**
 * firebase.js — Firebase App Initialization
 *
 * Phase 3B: Firebase Cloud Messaging skeleton.
 *
 * Reads ALL configuration from Vite environment variables (import.meta.env).
 * NO credentials are hardcoded in this file.
 *
 * Required environment variables (set in frontend/.env):
 *   VITE_FIREBASE_API_KEY
 *   VITE_FIREBASE_AUTH_DOMAIN
 *   VITE_FIREBASE_PROJECT_ID
 *   VITE_FIREBASE_STORAGE_BUCKET
 *   VITE_FIREBASE_MESSAGING_SENDER_ID
 *   VITE_FIREBASE_APP_ID
 *   VITE_FIREBASE_VAPID_KEY  (used in firebase-messaging.js)
 */

import { initializeApp, getApps, getApp } from 'firebase/app';

// ── Firebase configuration object ──────────────────────────────────────────
// All values read exclusively from import.meta.env (Vite injects .env at build time).
const firebaseConfig = {
  apiKey:            import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain:        import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId:         import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket:     import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId:             import.meta.env.VITE_FIREBASE_APP_ID,
};

// ── Validate configuration at startup (logs only, never throws) ───────────
const requiredKeys = [
  'VITE_FIREBASE_API_KEY',
  'VITE_FIREBASE_AUTH_DOMAIN',
  'VITE_FIREBASE_PROJECT_ID',
  'VITE_FIREBASE_STORAGE_BUCKET',
  'VITE_FIREBASE_MESSAGING_SENDER_ID',
  'VITE_FIREBASE_APP_ID',
];

const missingKeys = requiredKeys.filter(
  (key) => !import.meta.env[key] || import.meta.env[key].trim() === ''
);

if (missingKeys.length > 0) {
  console.warn(
    '[Firebase] ⚠ The following environment variables are not set:\n',
    missingKeys.map((k) => `  • ${k}`).join('\n'),
    '\n  → Open frontend/.env and paste your Firebase configuration values.',
    '\n  → FCM push notifications will NOT work until all values are set.'
  );
} else {
  console.info('[Firebase] ✔ All required environment variables are present.');
  console.info('[Firebase] Project ID:', import.meta.env.VITE_FIREBASE_PROJECT_ID);
}

// ── Initialize Firebase App (idempotent — safe for HMR / React StrictMode) ──
// getApps() returns existing apps; avoids "Firebase App named '[DEFAULT]' already exists" error.
const firebaseApp = getApps().length === 0
  ? initializeApp(firebaseConfig)
  : getApp();

if (getApps().length > 0) {
  console.info('[Firebase] ✔ FirebaseApp initialized successfully.');
}

export default firebaseApp;
export { firebaseConfig };
