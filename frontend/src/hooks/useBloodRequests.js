import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';

/**
 * Custom hook to retrieve active blood requests from patients.
 */
export function useBloodRequests() {
  return useQuery({
    queryKey: ['activeBloodRequests'],
    queryFn: async () => {
      const response = await api.get('/requests/active');
      return response.data;
    },
  });
}
