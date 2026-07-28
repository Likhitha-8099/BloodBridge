import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import donorService from '../services/donorService';

/**
 * Custom hook to manage the donor profile state, creating, updating, and toggling availability.
 */
export function useDonorProfile() {
  const queryClient = useQueryClient();

  const profileQuery = useQuery({
    queryKey: ['donorProfile'],
    queryFn: async () => {
      try {
        return await donorService.getProfile();
      } catch (error) {
        if (error.message.includes('404') || error.message.toLowerCase().includes('not found')) {
          return null;
        }
        throw error;
      }
    },
    retry: false,
  });

  const createMutation = useMutation({
    mutationFn: donorService.createProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donorProfile'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: donorService.updateProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donorProfile'] });
    },
  });

  const toggleAvailabilityMutation = useMutation({
    mutationFn: donorService.updateAvailability,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donorProfile'] });
    },
  });

  return {
    profile: profileQuery.data,
    isLoading: profileQuery.isLoading,
    error: profileQuery.error,
    refetch: profileQuery.refetch,
    createProfile: createMutation.mutateAsync,
    isCreating: createMutation.isPending,
    updateProfile: updateMutation.mutateAsync,
    isUpdating: updateMutation.isPending,
    toggleAvailability: toggleAvailabilityMutation.mutateAsync,
    isTogglingAvailability: toggleAvailabilityMutation.isPending,
  };
}
