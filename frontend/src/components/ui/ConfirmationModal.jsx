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
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  isLoading = false,
  variant = 'danger'
}) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 max-w-sm w-full border border-slate-100 dark:border-slate-800 shadow-2xl flex flex-col gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 rounded-2xl border border-red-100 dark:border-red-900/50 shrink-0">
            <AlertTriangle className="h-6 w-6" />
          </div>
          <h3 className="font-bold text-slate-900 dark:text-white text-base">{title}</h3>
        </div>
        
        <div className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed font-medium">
          {message}
        </div>

        <div className="flex items-center gap-3 justify-end mt-2">
          <Button 
            onClick={onClose} 
            variant="outline" 
            className="py-2 px-4 text-xs font-semibold border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700/80" 
            disabled={isLoading}
          >
            {cancelText}
          </Button>
          <Button 
            onClick={onConfirm} 
            variant={variant} 
            className={`py-2 px-4 text-xs font-bold text-white ${
              variant === 'danger'
                ? 'bg-rose-600 hover:bg-rose-700 dark:bg-rose-600 dark:hover:bg-rose-700'
                : 'bg-primary hover:bg-primary-dark'
            }`} 
            isLoading={isLoading}
          >
            {confirmText}
          </Button>
        </div>
      </div>
    </div>
  );
}

