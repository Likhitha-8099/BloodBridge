import React from 'react';
import Card from './Card';
import { Inbox } from 'lucide-react';

/**
 * Reusable component for empty query results.
 */
export default function EmptyState({ 
  message = 'No data available at this moment.', 
  icon: Icon = Inbox 
}) {
  return (
    <Card className="flex flex-col items-center justify-center text-center p-12 py-16 gap-4 border border-dashed border-gray-200">
      <div className="p-4 bg-slate-50 text-slate-400 rounded-full border border-slate-100">
        <Icon className="h-8 w-8" />
      </div>
      <div className="flex flex-col gap-1 max-w-sm">
        <h4 className="font-bold text-gray-800">No records found</h4>
        <p className="text-xs text-gray-400 leading-relaxed">{message}</p>
      </div>
    </Card>
  );
}
