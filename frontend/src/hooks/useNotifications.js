import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import notificationService from '../services/notificationService';

/**
 * Custom hook to retrieve all notifications for the authenticated user.
 */
export function useNotifications() {
  return useQuery({
    queryKey: ['notifications'],
    queryFn: notificationService.getNotifications,
    refetchInterval: 30000,
  });
}

/**
 * Custom hook to retrieve unread notifications with 30s polling interval.
 */
export function useUnreadNotifications() {
  return useQuery({
    queryKey: ['unreadNotifications'],
    queryFn: notificationService.getUnreadNotifications,
    refetchInterval: 30000,
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
      queryClient.invalidateQueries({ queryKey: ['notificationDetails', variables] });
    },
  });
}
