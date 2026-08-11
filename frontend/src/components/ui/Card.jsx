import React from 'react';

/**
 * Reusable Card container component.
 */
export default function Card({ children, className = '' }) {
  return (
    <div className={`bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm p-6 ${className}`}>
      {children}
    </div>
  );
}
