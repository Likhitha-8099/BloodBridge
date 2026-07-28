import { useQuery } from '@tanstack/react-query';
import bloodRequestService from '../services/bloodRequestService';

/**
 * Custom hook to retrieve specific blood request details.
 */
export function useRequestDetails(id) {
  return useQuery({
    queryKey: ['requestDetails', id],
    queryFn: () => bloodRequestService.getRequestById(id),
    enabled: !!id,
  });
}
export default useRequestDetails;
