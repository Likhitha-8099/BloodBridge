import api from '../api/axios';

/**
 * Service for interacting with backend Device Token endpoints (/api/v1/device/*).
 * Phase 3B.1 — Device Registration module.
 */
export const deviceTokenService = {
  /**
   * Register or update an FCM device token with the backend.
   *
   * @param {Object} data - { token, platform, browser, deviceName, deviceId }
   * @returns {Promise<Object>} ApiResponse containing DeviceTokenResponse
   */
  registerDeviceToken: async (data) => {
    const response = await api.post('/device/register', data);
    return response.data?.data ?? response.data;
  },

  /**
   * Refresh an updated FCM token with the backend.
   *
   * @param {string} oldToken - Previous token (optional)
   * @param {string} newToken - New token
   * @returns {Promise<Object>} ApiResponse containing DeviceTokenResponse
   */
  refreshDeviceToken: async (oldToken, newToken) => {
    const params = new URLSearchParams();
    if (oldToken) params.append('oldToken', oldToken);
    params.append('newToken', newToken);

    const response = await api.post(`/device/refresh?${params.toString()}`);
    return response.data?.data ?? response.data;
  },

  /**
   * Remove/deactivate an FCM device token on the backend (e.g. on logout).
   *
   * @param {string} token - FCM token to remove
   * @returns {Promise<Object>} ApiResponse
   */
  removeDeviceToken: async (token) => {
    const response = await api.delete(`/device/remove?token=${encodeURIComponent(token)}`);
    return response.data;
  },

  /**
   * Fetch all active registered devices for the current user.
   *
   * @returns {Promise<Array>} List of registered device responses
   */
  getMyDevices: async () => {
    const response = await api.get('/device/my-devices');
    return response.data?.data ?? response.data;
  },
};

export default deviceTokenService;
