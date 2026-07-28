import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import patientService from '../services/patientService';

/**
 * Custom hook to manage patient profiles.
 */
export function usePatientProfile() {
  const queryClient = useQueryClient();

  const profileQuery = useQuery({
    queryKey: ['patientProfile'],
    queryFn: async () => {
      try {
        return await patientService.getProfile();
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
    mutationFn: patientService.createProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patientProfile'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: patientService.updateProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patientProfile'] });
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
