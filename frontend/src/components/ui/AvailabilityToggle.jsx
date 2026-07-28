import React from 'react';

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
      <button
        type="button"
        disabled={isLoading}
        onClick={() => onToggle(!isAvailable)}
        className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 disabled:opacity-50 ${
          isAvailable ? 'bg-primary' : 'bg-gray-200'
        }`}
      >
        <span
          className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
            isAvailable ? 'translate-x-5' : 'translate-x-0'
          }`}
        />
      </button>
      <span className="text-sm font-semibold text-gray-700">
        {isLoading ? 'Updating...' : isAvailable ? 'Available for Donation' : 'Unavailable'}
      </span>
    </div>
  );
}
