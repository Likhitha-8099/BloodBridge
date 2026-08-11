import React, { forwardRef, useState } from 'react';
import { Lock, Eye, EyeOff } from 'lucide-react';

/**
 * Reusable PasswordInput component with interactive Show/Hide Password toggle,
 * soft focus glow, error state, and accessibility support.
 */
const PasswordInput = forwardRef(({
  label = 'Password',
  error,
  icon: Icon = Lock,
  className = '',
  id,
  focusTheme = 'red', // 'red' | 'teal' | 'slate'
  ...props
}, ref) => {
  const [showPassword, setShowPassword] = useState(false);
  const inputId = id || 'password-input';

  const themeGlows = {
    red: 'focus:border-red-500 dark:focus:border-red-400 focus:ring-4 focus:ring-red-500/10 dark:focus:ring-red-500/20 group-focus-within:text-red-500 dark:group-focus-within:text-red-400',
    teal: 'focus:border-teal-500 dark:focus:border-teal-400 focus:ring-4 focus:ring-teal-500/10 dark:focus:ring-teal-500/20 group-focus-within:text-teal-500 dark:group-focus-within:text-teal-400',
    slate: 'focus:border-indigo-500 dark:focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10 dark:focus:ring-indigo-500/20 group-focus-within:text-indigo-500 dark:group-focus-within:text-indigo-400',
  };

  const currentGlow = themeGlows[focusTheme] || themeGlows.red;

  return (
    <div className="w-full flex flex-col gap-1.5 text-left">
      {label && (
        <label 
          htmlFor={inputId}
          className="text-xs font-semibold text-slate-700 dark:text-slate-300 tracking-wide flex items-center justify-between"
        >
          <span>{label}</span>
        </label>
      )}

      <div className="relative flex items-center w-full group">
        {Icon && (
          <div className="absolute left-3.5 text-slate-400 transition-colors pointer-events-none">
            <Icon className="h-4 w-4" />
          </div>
        )}

        <input
          ref={ref}
          id={inputId}
          type={showPassword ? 'text' : 'password'}
          className={`w-full ${Icon ? 'pl-10' : 'px-4'} pr-11 py-3 rounded-2xl text-sm bg-slate-50 dark:bg-slate-800/80 text-slate-900 dark:text-white placeholder-slate-400 dark:placeholder-slate-500 border transition-all duration-200 focus:outline-none ${
            error
              ? 'border-red-400 dark:border-red-500/80 focus:border-red-500 focus:ring-4 focus:ring-red-500/15 bg-red-50/20'
              : `border-slate-200 dark:border-slate-700/80 focus:bg-white dark:focus:bg-slate-800 ${currentGlow}`
          } ${className}`}
          {...props}
        />

        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-3 p-1 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-400/30"
          aria-label={showPassword ? 'Hide password' : 'Show password'}
          tabIndex={-1}
        >
          {showPassword ? (
            <EyeOff className="h-4 w-4" />
          ) : (
            <Eye className="h-4 w-4" />
          )}
        </button>
      </div>

      {error && (
        <span className="text-[11px] text-red-500 dark:text-red-400 font-medium pl-1">
          {error}
        </span>
      )}
    </div>
  );
});

PasswordInput.displayName = 'PasswordInput';

export default PasswordInput;
