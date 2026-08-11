import React, { forwardRef } from 'react';

/**
 * Reusable LoginInput component for auth pages.
 * Supports icon prefix, label, soft focus glow, error states, and responsive styling.
 */
const LoginInput = forwardRef(({
  label,
  type = 'text',
  error,
  icon: Icon,
  className = '',
  id,
  ...props
}, ref) => {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

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
          <div className="absolute left-3.5 text-slate-400 group-focus-within:text-red-500 dark:group-focus-within:text-red-400 transition-colors pointer-events-none">
            <Icon className="h-4 w-4" />
          </div>
        )}

        <input
          ref={ref}
          id={inputId}
          type={type}
          className={`w-full ${Icon ? 'pl-10' : 'px-4'} pr-4 py-3 rounded-2xl text-sm bg-slate-50 dark:bg-slate-800/80 text-slate-900 dark:text-white placeholder-slate-400 dark:placeholder-slate-500 border transition-all duration-200 focus:outline-none ${
            error
              ? 'border-red-400 dark:border-red-500/80 focus:border-red-500 focus:ring-4 focus:ring-red-500/15 bg-red-50/20'
              : 'border-slate-200 dark:border-slate-700/80 focus:border-red-500 dark:focus:border-red-400 focus:ring-4 focus:ring-red-500/10 dark:focus:ring-red-500/20 focus:bg-white dark:focus:bg-slate-800'
          } ${className}`}
          {...props}
        />
      </div>

      {error && (
        <span className="text-[11px] text-red-500 dark:text-red-400 font-medium pl-1">
          {error}
        </span>
      )}
    </div>
  );
});

LoginInput.displayName = 'LoginInput';

export default LoginInput;
