import React from 'react';
import { Loader2, LogIn, CheckCircle2 } from 'lucide-react';

/**
 * Reusable LoginButton supporting Idle, Hover, Submitting ("Signing in..."),
 * Success animations, and role-based themes (red, teal, indigo).
 */
export default function LoginButton({
  children,
  isLoading = false,
  isSuccess = false,
  theme = 'red', // 'red' | 'teal' | 'indigo'
  className = '',
  icon: Icon = LogIn,
  ...props
}) {
  const themeStyles = {
    red: 'bg-gradient-to-r from-red-600 via-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white shadow-lg shadow-red-500/25 dark:shadow-red-950/40 hover:shadow-red-500/40 focus:ring-red-500/30',
    teal: 'bg-gradient-to-r from-teal-600 via-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white shadow-lg shadow-teal-500/25 dark:shadow-teal-950/40 hover:shadow-teal-500/40 focus:ring-teal-500/30',
    indigo: 'bg-gradient-to-r from-indigo-600 via-slate-800 to-indigo-700 hover:from-indigo-500 hover:to-indigo-600 text-white shadow-lg shadow-indigo-500/25 dark:shadow-indigo-950/40 hover:shadow-indigo-500/40 focus:ring-indigo-500/30',
  };

  const currentTheme = themeStyles[theme] || themeStyles.red;

  return (
    <button
      type="submit"
      disabled={isLoading || isSuccess}
      className={`w-full py-3.5 px-6 rounded-2xl font-bold text-sm tracking-wide transition-all duration-200 transform active:scale-[0.98] hover:-translate-y-0.5 flex items-center justify-center gap-2 focus:outline-none focus:ring-4 disabled:opacity-85 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none ${currentTheme} ${className}`}
      {...props}
    >
      {isSuccess ? (
        <>
          <CheckCircle2 className="h-5 w-5 animate-bounce text-emerald-300" />
          <span>Authenticated! Redirecting...</span>
        </>
      ) : isLoading ? (
        <>
          <Loader2 className="h-5 w-5 animate-spin text-white/90" />
          <span>Signing in...</span>
        </>
      ) : (
        <>
          {Icon && <Icon className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />}
          <span>{children || 'Sign In'}</span>
        </>
      )}
    </button>
  );
}
