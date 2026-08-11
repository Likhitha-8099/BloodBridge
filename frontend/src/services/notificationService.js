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
    const resData = response.data?.data !== undefined ? response.data.data : response.data;
    if (Array.isArray(resData)) return resData;
    if (resData?.notifications && Array.isArray(resData.notifications)) return resData.notifications;
    if (resData?.items && Array.isArray(resData.items)) return resData.items;
    return [];
  },

  /**
   * Retrieves unread notifications.
   */
  getUnreadNotifications: async () => {
    const response = await api.get('/notifications/unread');
    return response.data?.data || response.data || [];
  },

  /**
   * Retrieves unread & total notification count metrics.
   */
  getUnreadCount: async () => {
    const response = await api.get('/notifications/unread-count');
    return response.data?.data || response.data || { unreadCount: 0, totalCount: 0 };
  },

  /**
   * Retrieves detailed notification details by ID.
   */
  getNotificationById: async (id) => {
    const response = await api.get(`/notifications/${id}`);
    return response.data?.data || response.data;
  },

  /**
   * Marks a specific notification as read.
   */
  markAsRead: async (id) => {
    const response = await api.patch(`/notifications/${id}/read`);
    return response.data?.data || response.data;
  },

  /**
   * Marks all notifications for authenticated user as read.
   */
  markAllAsRead: async () => {
    const response = await api.patch('/notifications/read-all');
    return response.data?.data || response.data;
  },

  /**
   * Deletes a notification by ID.
   */
  deleteNotification: async (id) => {
    const response = await api.delete(`/notifications/${id}`);
    return response.data?.data || response.data;
  },
};

export default notificationService;
