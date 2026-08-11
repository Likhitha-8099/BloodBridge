import { useQuery } from '@tanstack/react-query';
import adminService from '../services/adminService';

/**
 * Hook to retrieve consolidated dashboard metrics.
 */
export function useDashboardOverview() {
  return useQuery({
    queryKey: ['adminDashboardOverview'],
    queryFn: adminService.getDashboardOverview,
    refetchInterval: 30000,
  });
}

/**
 * Hook to retrieve detailed user demographics.
 */
export function useUserStatistics() {
  return useQuery({
    queryKey: ['adminUserStatistics'],
    queryFn: adminService.getUserStatistics,
  });
}

/**
 * Hook to retrieve blood request verification statistics.
 */
export function useRequestStatistics() {
  return useQuery({
    queryKey: ['adminRequestStatistics'],
    queryFn: adminService.getRequestStatistics,
  });
}

/**
 * Hook to retrieve donation completion stats and trends.
 */
export function useDonationStatistics() {
  return useQuery({
    queryKey: ['adminDonationStatistics'],
    queryFn: adminService.getDonationStatistics,
  });
}

/**
 * Hook to retrieve matching engine success rates.
 */
export function useMatchingStatistics() {
  return useQuery({
    queryKey: ['adminMatchingStatistics'],
    queryFn: adminService.getMatchingStatistics,
  });
}

/**
 * Hook to retrieve blood group analytics.
 */
export function useBloodGroupAnalytics() {
  return useQuery({
    queryKey: ['adminBloodGroupAnalytics'],
    queryFn: adminService.getBloodGroupAnalytics,
  });
}

/**
 * Hook to retrieve top donor leaderboard.
 */
export function useTopDonors() {
  return useQuery({
    queryKey: ['adminTopDonors'],
    queryFn: adminService.getTopDonors,
  });
}

/**
 * Hook to retrieve top hospitals list.
 */
export function useTopHospitals() {
  return useQuery({
    queryKey: ['adminTopHospitals'],
    queryFn: adminService.getTopHospitals,
  });
}

/**
 * Hook to retrieve system notification statistics.
 */
export function useNotificationStatistics() {
  return useQuery({
    queryKey: ['adminNotificationStatistics'],
    queryFn: adminService.getNotificationStatistics,
  });
}

/**
 * Hook to retrieve monthly donation trends.
 */
export function useMonthlyDonations() {
  return useQuery({
    queryKey: ['adminMonthlyDonations'],
    queryFn: adminService.getMonthlyDonations,
  });
}

/**
 * Hook to retrieve monthly request trends.
 */
export function useMonthlyRequests() {
  return useQuery({
    queryKey: ['adminMonthlyRequests'],
    queryFn: adminService.getMonthlyRequests,
  });
}
