import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import hospitalService from '../services/hospitalService';

/**
 * Custom hook to retrieve hospital-hosted donations and perform status transitions.
 */
export function useDonations(hospitalId) {
  const queryClient = useQueryClient();

  const donationsQuery = useQuery({
    queryKey: ['hospitalDonations', hospitalId],
    queryFn: () => hospitalService.getHospitalDonations(hospitalId),
    enabled: !!hospitalId,
  });

  const confirmMutation = useMutation({
    mutationFn: hospitalService.confirmDonation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hospitalDonations', hospitalId] });
    },
  });

  const completeMutation = useMutation({
    mutationFn: ({ donationId, payload }) => hospitalService.completeDonation(donationId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hospitalDonations', hospitalId] });
    },
  });

  return {
    donations: donationsQuery.data,
    isLoading: donationsQuery.isLoading,
    error: donationsQuery.error,
    refetch: donationsQuery.refetch,
    confirmDonation: confirmMutation.mutateAsync,
    isConfirming: confirmMutation.isPending,
    completeDonation: completeMutation.mutateAsync,
    isCompleting: completeMutation.isPending,
  };
}
export default useDonations;
