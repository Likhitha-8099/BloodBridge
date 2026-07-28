import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import matchingService from '../services/matchingService';

/**
 * Custom hook to retrieve ranked donor candidates and active match run records.
 */
export function useMatches(requestId) {
  const queryClient = useQueryClient();

  const rankedDonorsQuery = useQuery({
    queryKey: ['rankedDonors', requestId],
    queryFn: () => matchingService.getRankedDonors(requestId),
    enabled: !!requestId,
  });

  const resultsQuery = useQuery({
    queryKey: ['matchResults', requestId],
    queryFn: () => matchingService.getGeneratedMatches(requestId),
    enabled: !!requestId,
  });

  const generateMutation = useMutation({
    mutationFn: () => matchingService.generateMatches(requestId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rankedDonors', requestId] });
      queryClient.invalidateQueries({ queryKey: ['matchResults', requestId] });
      queryClient.invalidateQueries({ queryKey: ['requestDetails', requestId] });
    },
  });

  return {
    rankedDonors: rankedDonorsQuery.data,
    isRankedLoading: rankedDonorsQuery.isLoading,
    rankedError: rankedDonorsQuery.error,
    results: resultsQuery.data,
    isResultsLoading: resultsQuery.isLoading,
    resultsError: resultsQuery.error,
    generateMatches: generateMutation.mutateAsync,
    isGenerating: generateMutation.isPending,
  };
}
export default useMatches;
