import api from '../api/axios';

/**
 * Service for creating and managing patient blood requests.
 */
export const bloodRequestService = {
  /**
   * Creates a new blood request.
   */
  createRequest: async (requestData) => {
    const response = await api.post('/requests', requestData);
    return response.data;
  },

  /**
   * Retrieves all blood requests registered by the logged-in patient.
   */
  getMyRequests: async () => {
    const response = await api.get('/requests/my');
    return response.data;
  },

  /**
   * Retrieves a detailed blood request by its ID.
   */
  getRequestById: async (id) => {
    const response = await api.get(`/requests/${id}`);
    return response.data;
  },

  /**
   * Cancels a blood request.
   */
  cancelRequest: async (id) => {
    const response = await api.patch(`/requests/${id}/cancel`);
    return response.data;
  },
};

export default bloodRequestService;
