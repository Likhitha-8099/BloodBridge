import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import hospitalService from '../services/hospitalService';

/**
 * Custom hook to retrieve and edit hospital profile.
 */
export function useHospitalProfile() {
  const queryClient = useQueryClient();

  const profileQuery = useQuery({
    queryKey: ['hospitalProfile'],
    queryFn: async () => {
      try {
        return await hospitalService.getHospitalProfile();
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
    mutationFn: hospitalService.createHospitalProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hospitalProfile'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: hospitalService.updateHospitalProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hospitalProfile'] });
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
  };
}
export default useHospitalProfile;
