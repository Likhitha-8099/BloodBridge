import api from '../api/axios';

/**
 * Service managing active blood request verification workflows.
 */
export const requestService = {
  /**
   * Fetches active blood requests (Pending & Verified).
   */
  getActiveRequests: async () => {
    const response = await api.get('/requests/active');
    return response.data;
  },

  /**
   * Hospital verifies a blood request.
   */
  verifyRequest: async (requestId) => {
    const response = await api.patch(`/requests/${requestId}/verify`);
    return response.data;
  },

  /**
   * Hospital rejects a blood request.
   */
  rejectRequest: async (requestId) => {
    const response = await api.patch(`/requests/${requestId}/reject`);
    return response.data;
  },
};

export default requestService;
