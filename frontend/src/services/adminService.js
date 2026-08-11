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
};

export default adminService;
