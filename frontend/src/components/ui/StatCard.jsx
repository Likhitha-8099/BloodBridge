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
    <Card className="flex items-center gap-4">
      {Icon && (
        <div className={`p-4 rounded-2xl ${iconColor} shrink-0`}>
          <Icon className="h-6 w-6" />
        </div>
      )}
      <div>
        <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">{title}</span>
        <h3 className="text-2xl font-black text-gray-900 mt-0.5">{value}</h3>
      </div>
    </Card>
  );
}
