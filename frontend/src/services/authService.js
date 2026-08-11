import api from '../api/axios';

/**
 * Authentication service communicating with Spring Boot Auth endpoints.
 */
export const authService = {
  /**
   * Log in user with credentials.
   *
   * @param {Object} credentials - { email, password }
   * @returns {Promise<Object>} API response including JWT token and user info
   */
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    // Backend wraps all responses in ApiResponse<T>: { success, message, data: AuthResponse }
    return response.data?.data ?? response.data;
  },

  /**
   * Register a new user in the platform.
   *
   * @param {Object} userData - { fullName, email, password, phoneNumber, role }
   * @returns {Promise<Object>} API response
   */
  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  /**
   * Switch the current active role of the user.
   *
   * @param {string} newRole - The role to switch to (e.g. DONOR, PATIENT, HOSPITAL)
   * @returns {Promise<Object>} The authentication response containing new token and user details
   */
  switchRole: async (newRole) => {
    const response = await api.post(`/auth/switch-role?role=${newRole}`);
    return response.data;
  },
};
export default authService;
