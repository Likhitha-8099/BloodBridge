import React from 'react';

/**
 * Reusable table component with responsive scrolling and customizable column renders.
 */
export default function DataTable({ 
  columns, 
  data, 
  keyField = 'id', 
  emptyMessage = 'No records found' 
}) {
  if (!data || data.length === 0) {
    return (
      <div className="text-center py-10 text-sm text-gray-400 bg-white rounded-2xl border border-gray-100 shadow-sm font-medium">
        {emptyMessage}
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto rounded-2xl border border-gray-100 shadow-sm bg-white">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="bg-slate-50/80 border-b border-gray-100">
            {columns.map((col) => (
              <th key={col.header} className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {data.map((row) => (
            <tr key={row[keyField]} className="hover:bg-slate-50/40 transition-all duration-150">
              {columns.map((col) => (
                <td key={col.header} className="px-6 py-4 text-sm text-gray-700 whitespace-nowrap">
                  {col.render ? col.render(row) : row[col.field]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
