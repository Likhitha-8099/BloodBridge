import React from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';

/**
 * Unified Page Header for Hospital Module pages.
 * Displays title, icon, subtitle, breadcrumbs, status badge, and optional action buttons.
 */
export default function HospitalPageHeader({
  title,
  subtitle,
  icon: Icon,
  badge,
  breadcrumbs = [],
  action,
  className = '',
}) {
  return (
    <div className={`flex flex-col gap-4 mb-8 ${className}`}>
      {/* Breadcrumbs */}
      {breadcrumbs.length > 0 && (
        <nav className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400 font-medium">
          <Link to="/hospital/dashboard" className="hover:text-teal-600 dark:hover:text-teal-400 transition-colors">
            Hospital Portal
          </Link>
          {breadcrumbs.map((crumb, idx) => (
            <React.Fragment key={idx}>
              <ChevronRight className="h-3.5 w-3.5 text-slate-400 shrink-0" />
              {crumb.to ? (
                <Link to={crumb.to} className="hover:text-teal-600 dark:hover:text-teal-400 transition-colors">
                  {crumb.label}
                </Link>
              ) : (
                <span className="text-slate-900 dark:text-slate-200 font-semibold">{crumb.label}</span>
              )}
            </React.Fragment>
          ))}
        </nav>
      )}

      {/* Title & Actions Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-start sm:items-center gap-3.5">
          {Icon && (
            <div className="p-3 bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 rounded-2xl border border-teal-100 dark:border-teal-900/40 shrink-0 shadow-inner">
              <Icon className="h-6 w-6" />
            </div>
          )}
          <div>
            <div className="flex items-center gap-2.5 flex-wrap">
              <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-slate-900 dark:text-white">
                {title}
              </h1>
              {badge && (
                <span className="bg-teal-50 dark:bg-teal-950/60 text-teal-700 dark:text-teal-300 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider border border-teal-100 dark:border-teal-900/30">
                  {badge}
                </span>
              )}
            </div>
            {subtitle && (
              <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 font-medium mt-0.5">
                {subtitle}
              </p>
            )}
          </div>
        </div>

        {action && <div className="shrink-0">{action}</div>}
      </div>
    </div>
  );
}
