import api from '../api/axios';

/**
 * Service managing hospital profile configuration and donation confirmations.
 */
export const hospitalService = {
  /**
   * Retrieves the logged-in hospital profile details.
   */
  getHospitalProfile: async () => {
    const response = await api.get('/hospitals/me');
    return response.data;
  },

  /**
   * Creates a new hospital profile.
   */
  createHospitalProfile: async (profileData) => {
    const response = await api.post('/hospitals', profileData);
    return response.data;
  },

  /**
   * Updates an existing hospital profile.
   */
  updateHospitalProfile: async (profileData) => {
    const response = await api.put('/hospitals/me', profileData);
    return response.data;
  },

  /**
   * Fetches donations scheduled at this hospital.
   */
  getHospitalDonations: async (hospitalId) => {
    const response = await api.get(`/donations/hospital/${hospitalId}`);
    return response.data;
  },

  /**
   * Confirms a donation scheduled at this hospital.
   */
  confirmDonation: async (donationId) => {
    const response = await api.patch(`/donations/${donationId}/confirm`);
    return response.data;
  },

  /**
   * Completes a donation record with units details.
   */
  completeDonation: async (donationId, payload) => {
    const response = await api.patch(`/donations/${donationId}/complete`, payload);
    return response.data;
  },
};

export default hospitalService;
