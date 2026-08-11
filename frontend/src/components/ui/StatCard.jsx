import React from 'react';
import Card from './Card';

/**
 * Reusable dashboard stats card.
 */
export default function StatCard({ 
  title, 
  value, 
  icon: Icon, 
  iconColor = 'text-primary bg-red-50' 
}) {
  return (
    <Card className="flex items-center gap-4 hover:shadow-md transition-shadow">
      {Icon && (
        <div className={`p-3.5 rounded-2xl ${iconColor} shrink-0`}>
          <Icon className="h-6 w-6" />
        </div>
      )}
      <div>
        <span className="text-[10px] text-slate-400 dark:text-slate-400 font-bold uppercase tracking-wider">{title}</span>
        <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-0.5">{value ?? 0}</h3>
      </div>
    </Card>
  );
}
