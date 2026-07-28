import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import bloodRequestService from '../services/bloodRequestService';

/**
 * Custom hook to retrieve logged-in patient's blood requests and perform cancellations.
 */
export function useMyRequests() {
  const queryClient = useQueryClient();

  const requestsQuery = useQuery({
    queryKey: ['myRequests'],
    queryFn: bloodRequestService.getMyRequests,
  });

  const cancelMutation = useMutation({
    mutationFn: bloodRequestService.cancelRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myRequests'] });
      queryClient.invalidateQueries({ queryKey: ['requestDetails'] });
    },
  });

  return {
    requests: requestsQuery.data,
    isLoading: requestsQuery.isLoading,
    error: requestsQuery.error,
    refetch: requestsQuery.refetch,
    cancelRequest: cancelMutation.mutateAsync,
    isCancelling: cancelMutation.isPending,
  };
}
export default useMyRequests;
