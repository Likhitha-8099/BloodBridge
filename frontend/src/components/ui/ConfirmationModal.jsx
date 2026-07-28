import React from 'react';
import Button from './Button';
import { AlertTriangle } from 'lucide-react';

/**
 * Reusable modal for confirming destructive/high-risk actions.
 */
export default function ConfirmationModal({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title = 'Are you sure?', 
  message = 'This action cannot be undone. Please confirm.', 
  isLoading = false 
}) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
      <div className="bg-white rounded-3xl p-6 max-w-sm w-full border border-gray-100 shadow-xl flex flex-col gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-red-50 text-red-500 rounded-2xl border border-red-100">
            <AlertTriangle className="h-6 w-6" />
          </div>
          <h3 className="font-bold text-gray-800 text-base">{title}</h3>
        </div>
        
        <p className="text-xs text-gray-500 leading-relaxed">
          {message}
        </p>

        <div className="flex items-center gap-3 justify-end mt-2">
          <Button 
            onClick={onClose} 
            variant="outline" 
            className="py-2 px-4 text-xs" 
            disabled={isLoading}
          >
            Go Back
          </Button>
          <Button 
            onClick={onConfirm} 
            variant="danger" 
            className="py-2 px-4 text-xs" 
            isLoading={isLoading}
          >
            Confirm
          </Button>
        </div>
      </div>
    </div>
  );
}
