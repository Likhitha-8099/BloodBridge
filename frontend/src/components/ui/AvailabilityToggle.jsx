import React from 'react';
import ToggleSwitch from './ToggleSwitch';

/**
 * Reusable slide toggle for enabling or disabling donation availability.
 */
export default function AvailabilityToggle({ 
  isAvailable, 
  onToggle, 
  isLoading = false 
}) {
  return (
    <div className="flex items-center gap-3">
      <ToggleSwitch
        checked={isAvailable}
        onChange={onToggle}
        disabled={isLoading}
        label={isLoading ? 'Updating Availability...' : isAvailable ? 'Available for Donation' : 'Currently Unavailable'}
        sublabel={isAvailable ? 'Visible to emergency hospital calls' : 'Hidden from active emergency lists'}
      />
    </div>
  );
}
