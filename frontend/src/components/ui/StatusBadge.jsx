import React from 'react';

/**
 * Reusable color-coded badge for blood request and donation statuses.
 */
export default function StatusBadge({ status }) {
  const statusClasses = {
    PENDING: 'bg-yellow-50 text-yellow-750 border-yellow-200',
    VERIFIED: 'bg-indigo-50 text-indigo-750 border-indigo-200',
    MATCHED: 'bg-blue-50 text-blue-750 border-blue-200',
    COMPLETED: 'bg-green-50 text-green-750 border-green-200',
    CANCELLED: 'bg-slate-100 text-slate-700 border-slate-200',
    REJECTED: 'bg-red-50 text-red-750 border-red-200',
  };

  const style = statusClasses[status] || 'bg-gray-100 text-gray-650 border-gray-200';

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold border uppercase tracking-wider ${style}`}>
      {status}
    </span>
  );
}
