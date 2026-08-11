import React from 'react';

/**
 * Modern Healthcare Card component for Hospital Module.
 */
export default function HospitalCard({
  children,
  title,
  subtitle,
  icon: Icon,
  action,
  className = '',
  headerClassName = '',
  bodyClassName = '',
  hoverEffect = false,
  ...props
}) {
  return (
    <div 
      className={`bg-white dark:bg-slate-900 rounded-3xl border border-slate-100 dark:border-slate-800/80 shadow-xl shadow-slate-200/40 dark:shadow-none transition-all duration-200 ${
        hoverEffect ? 'hover:border-teal-200 dark:hover:border-teal-900/50 hover:shadow-2xl hover:-translate-y-0.5' : ''
      } ${className}`}
      {...props}
    >
      {(title || subtitle || Icon || action) && (
        <div className={`p-6 pb-4 border-b border-slate-100 dark:border-slate-800/80 flex items-center justify-between gap-4 ${headerClassName}`}>
          <div className="flex items-center gap-3">
            {Icon && (
              <div className="h-10 w-10 rounded-xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center shrink-0 border border-teal-100 dark:border-teal-900/30">
                <Icon className="h-5 w-5" />
              </div>
            )}
            <div>
              {title && (
                <h3 className="font-bold text-base text-slate-900 dark:text-white">
                  {title}
                </h3>
              )}
              {subtitle && (
                <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                  {subtitle}
                </p>
              )}
            </div>
          </div>

          {action && <div className="shrink-0">{action}</div>}
        </div>
      )}

      <div className={`p-6 ${bodyClassName}`}>
        {children}
      </div>
    </div>
  );
}
