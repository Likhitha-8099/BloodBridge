import api from '../api/axios';

/**
 * Service managing hospital profile configuration and donation confirmations.
 */
export const hospitalService = {
  /**
   * Retrieves the logged-in hospital profile details.
   */
  getHospitalProfile: async () => {
    try {
      const response = await api.get('/hospital/profile');
      return response.data?.data || response.data;
    } catch {
      const response = await api.get('/hospitals/me');
      return response.data?.data || response.data;
    }
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

  /**
   * Fetches paginated registered users for hospital portal view.
   */
  getUsers: async (params) => {
    const response = await api.get('/hospitals/users', { params });
    return response.data;
  },

  /**
   * Fetches paginated registered donors for hospital portal view.
   */
  getDonors: async (params) => {
    const response = await api.get('/hospitals/donors', { params });
    return response.data;
  },

  /**
   * Fetches complete hospital dashboard data.
   */
  getDashboardData: async () => {
    const response = await api.get('/hospital/dashboard');
    return response.data;
  },

  /**
   * Fetches recent blood requests for the hospital.
   */
  getRecentRequests: async (limit = 5) => {
    const response = await api.get('/hospital/dashboard/recent-requests', { params: { limit } });
    return response.data;
  },

  /**
   * Fetches emergency blood requests for the hospital.
   */
  getEmergencyRequests: async (limit = 5) => {
    const response = await api.get('/hospital/dashboard/emergency-requests', { params: { limit } });
    return response.data;
  },

  /**
   * Fetches recent completed donations.
   */
  getRecentDonations: async (limit = 5) => {
    const response = await api.get('/hospital/dashboard/recent-donations', { params: { limit } });
    return response.data;
  },

  /**
   * Fetches nearby available donors.
   */
  getNearbyDonors: async (limit = 5) => {
    const response = await api.get('/hospital/dashboard/nearby-donors', { params: { limit } });
    return response.data;
  },

  /**
   * Fetches unread/recent hospital notifications.
   */
  getNotifications: async (limit = 10) => {
    try {
      const response = await api.get('/hospital/notifications');
      return response.data?.data || response.data;
    } catch {
      const response = await api.get('/hospital/dashboard/notifications', { params: { limit } });
      return response.data;
    }
  },

  /**
   * Marks a hospital notification as read.
   */
  markNotificationAsRead: async (notificationId) => {
    const response = await api.put(`/hospital/notifications/${notificationId}/read`);
    return response.data;
  },

  /**
   * Fetches matched donor responses (ACCEPTED, PENDING, REJECTED) for an emergency request.
   */
  getEmergencyRequestResponses: async (requestId) => {
    const response = await api.get(`/hospital/emergency-requests/${requestId}/responses`);
    return response.data?.data || response.data;
  },

  /**
   * Confirms an accepted matched donor for an emergency blood request.
   */
  confirmEmergencyDonor: async (requestId, matchedDonorId) => {
    const response = await api.post(`/hospital/emergency-requests/${requestId}/confirm-donor/${matchedDonorId}`);
    return response.data?.data || response.data;
  },

  /**
   * Transitions request status to FULFILLMENT_IN_PROGRESS.
   */
  startFulfillment: async (requestId) => {
    const response = await api.post(`/hospital/emergency-requests/${requestId}/start-fulfillment`);
    return response.data?.data || response.data;
  },

  /**
   * Completes an emergency blood request.
   */
  completeEmergencyRequest: async (requestId) => {
    const response = await api.post(`/hospital/emergency-requests/${requestId}/complete`);
    return response.data?.data || response.data;
  },

  /**
   * Fetches dashboard analytics trends.
   */
  getAnalytics: async () => {
    const response = await api.get('/hospital/dashboard/analytics');
    return response.data;
  },
};

export default hospitalService;
