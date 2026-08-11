import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import requestService from '../services/requestService';

/**
 * Custom hook to retrieve active blood requests and handle verification and rejection.
 */
export function useHospitalRequests() {
  const queryClient = useQueryClient();

  const requestsQuery = useQuery({
    queryKey: ['hospitalRequests'],
    queryFn: requestService.getHospitalRequests,
  });

  const verifyMutation = useMutation({
    mutationFn: requestService.verifyRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hospitalRequests'] });
      queryClient.invalidateQueries({ queryKey: ['requestDetails'] });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: requestService.rejectRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hospitalRequests'] });
      queryClient.invalidateQueries({ queryKey: ['requestDetails'] });
    },
  });

  return {
    requests: requestsQuery.data,
    isLoading: requestsQuery.isLoading,
    error: requestsQuery.error,
    refetch: requestsQuery.refetch,
    verifyRequest: verifyMutation.mutateAsync,
    isVerifying: verifyMutation.isPending,
    rejectRequest: rejectMutation.mutateAsync,
    isRejecting: rejectMutation.isPending,
  };
}
export default useHospitalRequests;
