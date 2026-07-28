import api from '../api/axios';

/**
 * Service for retrieving donation histories.
 */
export const donationService = {
  /**
   * Fetches donation histories for a specific donor ID.
   */
  getDonationHistory: async (donorId) => {
    const response = await api.get(`/donations/donor/${donorId}`);
    return response.data;
  },
};

export default donationService;
