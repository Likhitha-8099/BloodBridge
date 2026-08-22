import React from 'react';

/**
 * Reusable table component with responsive scrolling, dark mode theme support, and customizable column renders.
 */
export default function DataTable({ 
  columns, 
  data, 
  keyField = 'id', 
  emptyMessage = 'No records found' 
}) {
  if (!data || data.length === 0) {
    return (
      <div className="text-center py-10 text-sm text-slate-500 dark:text-slate-400 bg-white dark:bg-slate-900 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm font-medium">
        {emptyMessage}
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm bg-white dark:bg-slate-900 transition-colors duration-150">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="bg-slate-50 dark:bg-slate-800/70 border-b border-slate-200/80 dark:border-slate-800">
            {columns.map((col) => (
              <th key={col.header} className="px-6 py-4 text-xs font-bold text-slate-600 dark:text-slate-300 uppercase tracking-wider">
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80">
          {data.map((row, idx) => {
            const keyVal = (keyField && row[keyField] !== undefined && row[keyField] !== null)
              ? row[keyField]
              : (row.donorId ?? row.id ?? row.userId ?? `row-${idx}`);
            return (
              <tr key={keyVal} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/50 transition-colors duration-150">
                {columns.map((col) => (
                  <td key={col.header} className="px-6 py-4 text-sm text-slate-700 dark:text-slate-300 whitespace-nowrap">
                    {col.render ? col.render(row) : (row[col.field] ?? '—')}
                  </td>
                ))}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

