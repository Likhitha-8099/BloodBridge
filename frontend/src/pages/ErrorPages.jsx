import React from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/ui/Button';
import { ShieldAlert, HelpCircle, AlertTriangle } from 'lucide-react';

/**
 * Page displayed when a route cannot be matched.
 */
export function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh] text-center p-6 gap-4">
      <div className="p-4 bg-red-50 dark:bg-red-950/40 text-primary rounded-full animate-bounce">
        <HelpCircle className="h-12 w-12" />
      </div>
      <h1 className="text-3xl font-extrabold text-gray-900 dark:text-slate-100">404 - Page Not Found</h1>
      <p className="text-sm text-gray-500 dark:text-slate-400 max-w-sm">
        The destination you are trying to visit does not exist or has been relocated.
      </p>
      <Button onClick={() => navigate('/')} className="mt-2">
        Return Home
      </Button>
    </div>
  );
}

/**
 * Page displayed when authorization rules fail.
 */
export function ForbiddenPage() {
  const navigate = useNavigate();
  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh] text-center p-6 gap-4">
      <div className="p-4 bg-orange-50 dark:bg-orange-950/40 text-orange-600 rounded-full animate-pulse">
        <ShieldAlert className="h-12 w-12" />
      </div>
      <h1 className="text-3xl font-extrabold text-gray-900 dark:text-slate-100">403 - Access Denied</h1>
      <p className="text-sm text-gray-500 dark:text-slate-400 max-w-sm">
        You do not possess the authorization clearance levels required to load this registry.
      </p>
      <Button onClick={() => navigate('/')} className="mt-2">
        Return Home
      </Button>
    </div>
  );
}

/**
 * Page displayed when internal backend exceptions occur.
 */
export function ServerErrorPage() {
  const navigate = useNavigate();
  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh] text-center p-6 gap-4">
      <div className="p-4 bg-yellow-50 dark:bg-yellow-950/40 text-yellow-600 rounded-full">
        <AlertTriangle className="h-12 w-12" />
      </div>
      <h1 className="text-3xl font-extrabold text-gray-900 dark:text-slate-100">500 - Internal Server Error</h1>
      <p className="text-sm text-gray-500 dark:text-slate-400 max-w-sm">
        The application server encountered an unexpected error processing this transaction.
      </p>
      <Button onClick={() => navigate('/')} className="mt-2">
        Return Home
      </Button>
    </div>
  );
}
