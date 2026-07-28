import React from 'react';
import Card from './Card';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import Button from './Button';

/**
 * Reusable component for displaying API request errors and offering retries.
 */
export default function ErrorState({ 
  message = 'Unable to fetch the requested records.', 
  onRetry 
}) {
  return (
    <Card className="flex flex-col items-center justify-center text-center p-12 py-16 gap-4 border border-red-100 bg-red-50/10">
      <div className="p-4 bg-red-50 text-red-500 rounded-full border border-red-100">
        <AlertTriangle className="h-7 w-7" />
      </div>
      <div className="flex flex-col gap-1.5 max-w-sm">
        <h4 className="font-bold text-gray-800">Data Fetch Failure</h4>
        <p className="text-xs text-red-500 leading-relaxed font-medium">{message}</p>
      </div>
      {onRetry && (
        <Button onClick={onRetry} variant="outline" className="flex items-center gap-2 mt-2 text-xs py-2">
          <RefreshCw className="h-3.5 w-3.5" /> Try Again
        </Button>
      )}
    </Card>
  );
}
