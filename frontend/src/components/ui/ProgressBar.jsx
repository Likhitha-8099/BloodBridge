import React from 'react';

/**
 * Reusable ProgressBar component for step wizards, profile completion, and score meters.
 */
export default function ProgressBar({
  value = 0,
  max = 100,
  label,
  showValue = true,
  color = 'bg-primary',
  height = 'h-2.5',
  className = ''
}) {
  const percentage = Math.min(100, Math.max(0, Math.round((value / max) * 100)));

  return (
    <div className={`w-full flex flex-col gap-1.5 ${className}`}>
      {(label || showValue) && (
        <div className="flex items-center justify-between text-xs font-medium text-gray-700 dark:text-gray-300">
          {label && <span>{label}</span>}
          {showValue && <span className="font-bold text-gray-900 dark:text-white">{percentage}%</span>}
        </div>
      )}
      <div className={`w-full bg-gray-100 dark:bg-slate-800 rounded-full overflow-hidden ${height}`}>
        <div
          className={`${color} ${height} rounded-full transition-all duration-500 ease-out`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
}
