import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';

/**
 * Custom hook to retrieve matched emergency blood requests assigned to the authenticated donor.
 * Includes periodic polling fallback (every 10s) alongside WebSocket STOMP real-time listeners.
 */
export function useBloodRequests() {
  return useQuery({
    queryKey: ['emergencyBloodRequests'],
    queryFn: async () => {
      try {
        const response = await api.get('/donor/emergency-requests');
        const data = response.data?.data !== undefined ? response.data.data : response.data;
        return Array.isArray(data) ? data : (data?.data || []);
      } catch (err) {
        console.warn('Fallback to active requests on error:', err);
        const response = await api.get('/requests/active');
        const data = response.data?.data !== undefined ? response.data.data : response.data;
        return Array.isArray(data) ? data : (data?.data || []);
      }
    },
    refetchInterval: 10000, // Polling fallback every 10 seconds
    staleTime: 4000,
  });
}
