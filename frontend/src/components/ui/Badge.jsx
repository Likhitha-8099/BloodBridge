import React from 'react';

/**
 * Reusable Badge component for status tags, urgency levels, blood groups, and donor tiers.
 */
export default function Badge({ 
  children, 
  variant = 'default', 
  size = 'md', 
  className = '',
  icon: Icon
}) {
  const baseStyles = 'inline-flex items-center font-semibold rounded-full tracking-wide transition-all';

  const sizes = {
    sm: 'px-2 py-0.5 text-[10px] gap-1',
    md: 'px-2.5 py-1 text-xs gap-1.5',
    lg: 'px-3 py-1.5 text-sm gap-2'
  };

  const variants = {
    default: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
    primary: 'bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-400 border border-red-100 dark:border-red-900/30',
    critical: 'bg-rose-500 text-white shadow-sm shadow-rose-200 animate-pulse',
    urgent: 'bg-amber-500 text-white shadow-sm shadow-amber-200',
    normal: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-900/30',
    success: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/50 dark:text-emerald-300',
    warning: 'bg-amber-100 text-amber-800 dark:bg-amber-900/50 dark:text-amber-300',
    info: 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400 border border-blue-100 dark:border-blue-900/30',
    purple: 'bg-purple-50 text-purple-700 dark:bg-purple-950/40 dark:text-purple-400 border border-purple-100 dark:border-purple-900/30',
    bronze: 'bg-amber-900/10 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300 border border-amber-800/20',
    silver: 'bg-slate-200 text-slate-800 dark:bg-slate-700 dark:text-slate-200 border border-slate-300',
    gold: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950/40 dark:text-yellow-400 border border-yellow-300',
    platinum: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-950/40 dark:text-indigo-300 border border-indigo-300',
    hero: 'bg-gradient-to-r from-red-500 to-amber-500 text-white font-extrabold shadow-md'
  };

  return (
    <span className={`${baseStyles} ${sizes[size] || sizes.md} ${variants[variant] || variants.default} ${className}`}>
      {Icon && <Icon className={size === 'sm' ? 'h-3 w-3' : size === 'lg' ? 'h-4 w-4' : 'h-3.5 w-3.5'} />}
      {children}
    </span>
  );
}
