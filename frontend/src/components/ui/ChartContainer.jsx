import React from 'react';
import Card from './Card';

/**
 * Common card container providing titles, borders, and margins for analytics charts.
 */
export default function ChartContainer({ title, children }) {
  return (
    <Card className="flex flex-col gap-4">
      {title && (
        <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider border-b border-slate-50 pb-2">
          {title}
        </h4>
      )}
      <div className="w-full h-64">
        {children}
      </div>
    </Card>
  );
}
export { ChartContainer };
