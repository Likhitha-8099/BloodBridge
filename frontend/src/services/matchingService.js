import api from '../api/axios';

/**
 * Service managing blood matching engine endpoints.
 */
export const matchingService = {
  /**
   * Retrieves a ranked list of eligible, compatible donors for a verified request.
   */
  getRankedDonors: async (requestId) => {
    const response = await api.get(`/matching/request/${requestId}`);
    return response.data;
  },

  /**
   * Generates and persists matching records for a blood request.
   */
  generateMatches: async (requestId) => {
    const response = await api.post(`/matching/request/${requestId}/generate`);
    return response.data;
  },

  /**
   * Retrieves generated match records for a request.
   */
  getGeneratedMatches: async (requestId) => {
    const response = await api.get(`/matching/request/${requestId}/results`);
    return response.data;
  },
};

export default matchingService;
