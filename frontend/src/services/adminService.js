import api from '../api/axios';

/**
 * Service managing all administrative statistics, health diagnostics, and analytics.
 */
export const adminService = {
  /**
   * Retrieves unified dashboard metrics overview.
   */
  getDashboardOverview: async () => {
    const response = await api.get('/admin/dashboard');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves detailed user registration metrics & demographics.
   */
  getUserStatistics: async () => {
    const response = await api.get('/admin/statistics/users');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves blood request metrics.
   */
  getRequestStatistics: async () => {
    const response = await api.get('/admin/statistics/requests');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves donation analytics and completions.
   */
  getDonationStatistics: async () => {
    const response = await api.get('/admin/statistics/donations');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves matching engine analytics and success rates.
   */
  getMatchingStatistics: async () => {
    const response = await api.get('/admin/statistics/matching');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves unified notifications logs.
   */
  getNotificationStatistics: async () => {
    const response = await api.get('/admin/statistics/notifications');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves blood group distribution stats.
   */
  getBloodGroupAnalytics: async () => {
    const response = await api.get('/admin/analytics/blood-groups');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves top 10 donors leaderboard.
   */
  getTopDonors: async () => {
    const response = await api.get('/admin/analytics/top-donors');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves top 10 hospitals table.
   */
  getTopHospitals: async () => {
    const response = await api.get('/admin/analytics/top-hospitals');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves monthly donation trends.
   */
  getMonthlyDonations: async () => {
    const response = await api.get('/admin/analytics/monthly-donations');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves monthly request trends.
   */
  getMonthlyRequests: async () => {
    const response = await api.get('/admin/analytics/monthly-requests');
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves pending hospital registrations for review.
   */
  getPendingHospitals: async () => {
    const response = await api.get('/admin/hospitals/pending');
    return response.data?.data ?? response.data;
  },

  /**
   * Verifies, approves, or rejects a hospital registration.
   */
  verifyHospital: async (id, status, remarks = 'Admin Review') => {
    const response = await api.patch(`/admin/hospitals/${id}/verify`, null, {
      params: { status, remarks }
    });
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves all registered donors with optional filtering.
   */
  getAllDonors: async (params = {}) => {
    const response = await api.get('/admin/donors', { params });
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves complete profile details for a specific donor ID.
   */
  getDonorById: async (id) => {
    if (!id || id === 'undefined' || id === 'null') {
      throw new Error('Donor ID is missing.');
    }
    const response = await api.get(`/admin/donors/${id}`);
    return response.data?.data ?? response.data;
  },

  /**
   * Permanently deletes a donor profile, user account, and associated data.
   */
  deleteDonor: async (donorId) => {
    if (donorId === undefined || donorId === null || donorId === 'undefined' || donorId === 'null') {
      throw new Error('Unable to delete donor: donor ID is missing.');
    }
    const response = await api.delete(`/admin/donors/${donorId}`);
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves all registered hospitals with optional filtering.
   */
  getAllHospitals: async (params = {}) => {
    const response = await api.get('/admin/hospitals', { params });
    return response.data?.data ?? response.data;
  },

  /**
   * Retrieves complete profile details for a specific hospital ID.
   */
  getHospitalById: async (id) => {
    if (!id || id === 'undefined' || id === 'null') {
      throw new Error('Hospital ID is missing.');
    }
    const response = await api.get(`/admin/hospitals/${id}`);
    return response.data?.data ?? response.data;
  },

  /**
   * Permanently deletes a hospital profile, user account, and associated data.
   */
  deleteHospital: async (hospitalId) => {
    if (hospitalId === undefined || hospitalId === null || hospitalId === 'undefined' || hospitalId === 'null') {
      throw new Error('Unable to delete hospital: hospital ID is missing.');
    }
    const response = await api.delete(`/admin/hospitals/${hospitalId}`);
    return response.data?.data ?? response.data;
  },
};

export default adminService;
