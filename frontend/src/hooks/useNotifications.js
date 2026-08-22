import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import notificationService from '../services/notificationService';
import { useAuthStore } from '../store/authStore';

/**
 * Custom hook to retrieve all notifications for the authenticated user with 20s polling interval.
 */
export function useNotifications() {
  const token = useAuthStore((state) => state.token);
  return useQuery({
    queryKey: ['notifications'],
    queryFn: notificationService.getNotifications,
    enabled: !!token,
    refetchInterval: token ? 20000 : false,
  });
}

/**
 * Custom hook to retrieve unread notifications with 20s polling interval.
 */
export function useUnreadNotifications() {
  const token = useAuthStore((state) => state.token);
  return useQuery({
    queryKey: ['unreadNotifications'],
    queryFn: notificationService.getUnreadNotifications,
    enabled: !!token,
    refetchInterval: token ? 20000 : false,
  });
}

/**
 * Custom hook to retrieve unread notification count with 20s polling interval.
 */
export function useUnreadCount() {
  const token = useAuthStore((state) => state.token);
  return useQuery({
    queryKey: ['unreadCount'],
    queryFn: notificationService.getUnreadCount,
    enabled: !!token,
    refetchInterval: token ? 20000 : false,
  });
}

/**
 * Custom hook to retrieve a detailed notification by its ID.
 */
export function useNotificationDetails(id) {
  return useQuery({
    queryKey: ['notificationDetails', id],
    queryFn: () => notificationService.getNotificationById(id),
    enabled: !!id,
  });
}

/**
 * Custom hook mutation to mark a notification as read and sync cache states.
 */
export function useMarkNotificationAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationService.markAsRead,
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
      queryClient.invalidateQueries({ queryKey: ['notificationDetails', variables] });
    },
  });
}

/**
 * Custom hook mutation to mark ALL notifications as read.
 */
export function useMarkAllNotificationsAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationService.markAllAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
    },
  });
}

/**
 * Custom hook mutation to delete a notification by ID.
 */
export function useDeleteNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationService.deleteNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
    },
  });
}
