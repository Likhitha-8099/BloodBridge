import api from '../api/axios';

/**
 * Service managing user notifications and read state mappings.
 */
export const notificationService = {
  /**
   * Retrieves all notifications sent to the currently logged-in user.
   */
  getNotifications: async () => {
    const response = await api.get('/notifications');
    return response.data;
  },

  /**
   * Retrieves unread notifications.
   */
  getUnreadNotifications: async () => {
    const response = await api.get('/notifications/unread');
    return response.data;
  },

  /**
   * Retrieves detailed notification details by ID.
   */
  getNotificationById: async (id) => {
    const response = await api.get(`/notifications/${id}`);
    return response.data;
  },

  /**
   * Marks a specific notification as read.
   */
  markAsRead: async (id) => {
    const response = await api.patch(`/notifications/${id}/read`);
    return response.data;
  },
};

export default notificationService;
