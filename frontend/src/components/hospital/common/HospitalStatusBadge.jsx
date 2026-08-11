import React from 'react';
import { ShieldCheck, AlertCircle, CheckCircle2, Clock, XCircle, Zap } from 'lucide-react';

/**
 * Standardized status and urgency badges for the Hospital Module.
 */
export default function HospitalStatusBadge({ status, _type = 'status', className = '' }) {
  const normalized = (status || '').toString().toUpperCase();

  const configs = {
    // Urgency Levels
    CRITICAL: {
      label: 'CRITICAL EMERGENCY',
      bg: 'bg-red-500/10 dark:bg-red-950/60',
      text: 'text-red-700 dark:text-red-300',
      border: 'border-red-200 dark:border-red-800/60',
      icon: Zap,
    },
    URGENT: {
      label: 'URGENT',
      bg: 'bg-rose-500/10 dark:bg-rose-950/60',
      text: 'text-rose-700 dark:text-rose-300',
      border: 'border-rose-200 dark:border-rose-800/60',
      icon: AlertCircle,
    },
    HIGH: {
      label: 'HIGH PRIORITY',
      bg: 'bg-orange-500/10 dark:bg-orange-950/60',
      text: 'text-orange-700 dark:text-orange-300',
      border: 'border-orange-200 dark:border-orange-800/60',
      icon: AlertCircle,
    },
    MEDIUM: {
      label: 'MEDIUM',
      bg: 'bg-amber-500/10 dark:bg-amber-950/60',
      text: 'text-amber-700 dark:text-amber-300',
      border: 'border-amber-200 dark:border-amber-800/60',
      icon: Clock,
    },
    LOW: {
      label: 'ROUTINE / LOW',
      bg: 'bg-blue-500/10 dark:bg-blue-950/60',
      text: 'text-blue-700 dark:text-blue-300',
      border: 'border-blue-200 dark:border-blue-800/60',
      icon: Clock,
    },

    // Request & Response Statuses
    ACTIVE: {
      label: 'ACTIVE REQUEST',
      bg: 'bg-emerald-500/10 dark:bg-emerald-950/60',
      text: 'text-emerald-700 dark:text-emerald-300',
      border: 'border-emerald-200 dark:border-emerald-800/60',
      icon: ShieldCheck,
    },
    MATCHED: {
      label: 'MATCHED',
      bg: 'bg-cyan-500/10 dark:bg-cyan-950/60',
      text: 'text-cyan-700 dark:text-cyan-300',
      border: 'border-cyan-200 dark:border-cyan-800/60',
      icon: ShieldCheck,
    },
    ACCEPTED: {
      label: 'ACCEPTED DONOR',
      bg: 'bg-teal-500/15 dark:bg-teal-950/70',
      text: 'text-teal-700 dark:text-teal-300',
      border: 'border-teal-300 dark:border-teal-700',
      icon: CheckCircle2,
    },
    COMPLETED: {
      label: 'DONATION COMPLETED',
      bg: 'bg-emerald-500/15 dark:bg-emerald-950/70',
      text: 'text-emerald-800 dark:text-emerald-300',
      border: 'border-emerald-300 dark:border-emerald-700',
      icon: CheckCircle2,
    },
    PENDING: {
      label: 'PENDING',
      bg: 'bg-amber-500/10 dark:bg-amber-950/60',
      text: 'text-amber-700 dark:text-amber-300',
      border: 'border-amber-200 dark:border-amber-800/60',
      icon: Clock,
    },
    CANCELLED: {
      label: 'CANCELLED',
      bg: 'bg-slate-500/10 dark:bg-slate-800/60',
      text: 'text-slate-600 dark:text-slate-400',
      border: 'border-slate-200 dark:border-slate-700',
      icon: XCircle,
    },
  };

  const current = configs[normalized] || {
    label: status || 'UNKNOWN',
    bg: 'bg-slate-100 dark:bg-slate-800',
    text: 'text-slate-700 dark:text-slate-300',
    border: 'border-slate-200 dark:border-slate-700',
    icon: Clock,
  };

  const IconComponent = current.icon;

  return (
    <span 
      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-bold tracking-wider uppercase border shadow-xs ${current.bg} ${current.text} ${current.border} ${className}`}
    >
      <IconComponent className="h-3.5 w-3.5 shrink-0" />
      <span>{current.label}</span>
    </span>
  );
}
