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
    return response.data;
  },

  /**
   * Retrieves detailed user registration metrics.
   */
  getUserStatistics: async () => {
    const response = await api.get('/admin/statistics/users');
    return response.data;
  },

  /**
   * Retrieves blood request metrics.
   */
  getRequestStatistics: async () => {
    const response = await api.get('/admin/statistics/requests');
    return response.data;
  },

  /**
   * Retrieves donation analytics and completions.
   */
  getDonationStatistics: async () => {
    const response = await api.get('/admin/statistics/donations');
    return response.data;
  },

  /**
   * Retrieves matching engine analytics and success rates.
   */
  getMatchingStatistics: async () => {
    const response = await api.get('/admin/statistics/matching');
    return response.data;
  },

  /**
   * Retrieves unified notifications logs.
   */
  getNotificationStatistics: async () => {
    const response = await api.get('/admin/statistics/notifications');
    return response.data;
  },

  /**
   * Retrieves blood group distribution stats.
   */
  getBloodGroupAnalytics: async () => {
    const response = await api.get('/admin/analytics/blood-groups');
    return response.data;
  },

  /**
   * Retrieves top 10 donors leaderboard.
   */
  getTopDonors: async () => {
    const response = await api.get('/admin/analytics/top-donors');
    return response.data;
  },

  /**
   * Retrieves top 10 hospitals table.
   */
  getTopHospitals: async () => {
    const response = await api.get('/admin/analytics/top-hospitals');
    return response.data;
  },

  /**
   * Retrieves monthly donation trends.
   */
  getMonthlyDonations: async () => {
    const response = await api.get('/admin/analytics/monthly-donations');
    return response.data;
  },

  /**
   * Retrieves monthly request trends.
   */
  getMonthlyRequests: async () => {
    const response = await api.get('/admin/analytics/monthly-requests');
    return response.data;
  },

  /**
   * Retrieves database and queue status.
   */
  getSystemHealth: async () => {
    const response = await api.get('/admin/system-health');
    return response.data;
  },
};

export default adminService;
