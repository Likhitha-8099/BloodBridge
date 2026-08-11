import api from '../api/axios';

/**
 * Service for managing donor profiles and availability statuses.
 */
export const donorService = {
  /**
   * Retrieves the profile of the currently logged-in donor.
   */
  getProfile: async () => {
    const response = await api.get('/donors/me');
    return response.data?.data ?? response.data;
  },

  /**
   * Creates a new donor profile.
   */
  createProfile: async (profileData) => {
    const response = await api.post('/donors', profileData);
    return response.data?.data ?? response.data;
  },

  /**
   * Updates the existing donor profile.
   */
  updateProfile: async (profileData) => {
    const response = await api.put('/donors/me', profileData);
    return response.data?.data ?? response.data;
  },

  /**
   * Updates availability status.
   */
  updateAvailability: async (availableForDonation) => {
    const response = await api.patch('/donors/availability', { availableForDonation });
    return response.data?.data ?? response.data;
  },
};

export default donorService;
