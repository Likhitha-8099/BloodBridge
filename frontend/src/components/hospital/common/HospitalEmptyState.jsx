import React from 'react';
import { Inbox } from 'lucide-react';

/**
 * Standardized Empty State Component for Hospital Module.
 */
export default function HospitalEmptyState({
  title = 'No Records Found',
  description = 'There are no active entries to display at this time.',
  icon: Icon = Inbox,
  action,
  className = '',
}) {
  return (
    <div className={`flex flex-col items-center justify-center text-center p-8 sm:p-12 bg-slate-50/50 dark:bg-slate-900/40 rounded-3xl border border-dashed border-slate-200 dark:border-slate-800 ${className}`}>
      <div className="h-16 w-16 rounded-3xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center mb-4 border border-teal-100 dark:border-teal-900/40 shadow-inner">
        <Icon className="h-8 w-8" />
      </div>

      <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-1">
        {title}
      </h3>

      <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 max-w-sm leading-relaxed mb-6">
        {description}
      </p>

      {action && <div>{action}</div>}
    </div>
  );
}
