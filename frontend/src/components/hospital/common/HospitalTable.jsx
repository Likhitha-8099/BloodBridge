import React from 'react';

/**
 * Reusable, responsive table component for Hospital Module with dark mode support.
 */
export default function HospitalTable({
  headers = [],
  children,
  _emptyMessage = 'No data available',
  className = '',
}) {
  return (
    <div className={`w-full overflow-hidden rounded-2xl border border-slate-100 dark:border-slate-800/80 shadow-sm ${className}`}>
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider border-b border-slate-100 dark:border-slate-800">
            <tr>
              {headers.map((header, idx) => (
                <th key={idx} className="px-5 py-3.5 whitespace-nowrap">
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200">
            {children}
          </tbody>
        </table>
      </div>
    </div>
  );
}
