import { useCallback } from 'react';
import { requestNotificationPermission, getDeviceToken, isFcmSupported } from '../firebase/firebase-messaging';
import { deviceTokenService } from '../services/deviceTokenService';
import { useAuthStore } from '../store/authStore';

/**
 * Custom React hook for FCM Device Registration lifecycle management.
 * Phase 3B.1 — Device Registration module.
 */
export function useDeviceRegistration() {
  const { setFcmToken, fcmToken } = useAuthStore();

  /**
   * Helper to parse browser name from userAgent.
   */
  const getBrowserName = () => {
    const userAgent = navigator.userAgent;
    if (userAgent.includes('Firefox')) return 'Firefox';
    if (userAgent.includes('Edg')) return 'Edge';
    if (userAgent.includes('Chrome')) return 'Chrome';
    if (userAgent.includes('Safari')) return 'Safari';
    return 'Browser';
  };

  /**
   * Registers current device with backend after obtaining notification permission & FCM token.
   * Safe execution: errors are logged and will NEVER block user login/navigation.
   */
  const registerDevice = useCallback(async () => {
    try {
      const supported = await isFcmSupported();
      if (!supported) {
        console.warn('[FCM-Hook] FCM is not supported in this browser. Skipping registration.');
        return null;
      }

      // 1. Check or request notification permission
      const permission = await requestNotificationPermission();
      if (permission !== 'granted') {
        console.warn(`[FCM-Hook] Notification permission is '${permission}'. Device token registration skipped.`);
        return null;
      }

      // 2. Generate FCM Token using VAPID Key
      const token = await getDeviceToken();
      if (!token) {
        console.warn('[FCM-Hook] Failed to generate FCM token.');
        return null;
      }

      // Save token in Zustand authStore
      setFcmToken(token);

      // 3. Register device with Spring Boot backend
      const payload = {
        token,
        platform: 'WEB',
        browser: getBrowserName(),
        deviceName: `${getBrowserName()} / ${navigator.platform || 'Web'}`,
        deviceId: `${window.location.hostname}-${getBrowserName()}`,
      };

      console.info('[FCM-Hook] Registering FCM token with backend...');
      const response = await deviceTokenService.registerDeviceToken(payload);
      console.info('[FCM-Hook] ✔ FCM device token registered successfully with backend:', response);
      return token;
    } catch (err) {
      console.error('[FCM-Hook] ✘ Non-blocking error during device registration:', err?.message || err);
      return null;
    }
  }, [setFcmToken]);

  /**
   * Unregisters device token on logout.
   */
  const unregisterDevice = useCallback(async () => {
    try {
      const tokenToRemove = fcmToken || useAuthStore.getState().fcmToken;
      if (tokenToRemove) {
        console.info('[FCM-Hook] Removing FCM token from backend...');
        await deviceTokenService.removeDeviceToken(tokenToRemove);
        console.info('[FCM-Hook] ✔ FCM token removed from backend.');
      }
    } catch (err) {
      console.warn('[FCM-Hook] ⚠ Error removing device token from backend on logout:', err?.message || err);
    } finally {
      setFcmToken(null);
    }
  }, [fcmToken, setFcmToken]);

  /**
   * Refreshes token with backend if Firebase rotates it.
   */
  const handleTokenRefresh = useCallback(async (newToken) => {
    try {
      const oldToken = fcmToken || useAuthStore.getState().fcmToken;
      console.info('[FCM-Hook] Refreshing FCM token on backend...');
      await deviceTokenService.refreshDeviceToken(oldToken, newToken);
      setFcmToken(newToken);
      console.info('[FCM-Hook] ✔ Token refresh registered with backend.');
    } catch (err) {
      console.error('[FCM-Hook] ✘ Error refreshing token on backend:', err?.message || err);
    }
  }, [fcmToken, setFcmToken]);

  return {
    registerDevice,
    unregisterDevice,
    handleTokenRefresh,
  };
}

export default useDeviceRegistration;
