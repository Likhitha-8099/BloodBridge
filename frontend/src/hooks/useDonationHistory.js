import { useQuery } from '@tanstack/react-query';
import donationService from '../services/donationService';

/**
 * Custom hook to retrieve donation logs for a specific donor ID.
 */
export function useDonationHistory(donorId) {
  return useQuery({
    queryKey: ['donationHistory', donorId || 'me'],
    queryFn: () => donationService.getDonationHistory(donorId),
  });
}
