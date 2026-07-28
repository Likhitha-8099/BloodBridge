import React from 'react';
import { Heart, CheckSquare, AlertTriangle, Activity, UserCheck, Calendar, Award, Info } from 'lucide-react';

/**
 * Color-coded badge rendering type-specific tags and icons for alerts.
 */
export default function NotificationBadge({ type }) {
  const configs = {
    BLOOD_REQUEST_CREATED: {
      label: 'Request Created',
      style: 'bg-red-50 text-red-750 border-red-200',
      icon: Heart,
    },
    REQUEST_VERIFIED: {
      label: 'Request Verified',
      style: 'bg-green-50 text-green-750 border-green-200',
      icon: CheckSquare,
    },
    REQUEST_REJECTED: {
      label: 'Request Rejected',
      style: 'bg-orange-50 text-orange-755 border-orange-200',
      icon: AlertTriangle,
    },
    DONOR_MATCHED: {
      label: 'Donor Matched',
      style: 'bg-indigo-50 text-indigo-755 border-indigo-200',
      icon: Activity,
    },
    DONATION_ACCEPTED: {
      label: 'Donation Accepted',
      style: 'bg-teal-50 text-teal-750 border-teal-200',
      icon: UserCheck,
    },
    DONATION_CONFIRMED: {
      label: 'Donation Confirmed',
      style: 'bg-blue-50 text-blue-755 border-blue-200',
      icon: Calendar,
    },
    DONATION_COMPLETED: {
      label: 'Donation Completed',
      style: 'bg-orange-50 text-orange-750 border-orange-200',
      icon: Award,
    },
    SYSTEM_NOTIFICATION: {
      label: 'System Alert',
      style: 'bg-slate-100 text-slate-700 border-slate-200',
      icon: Info,
    },
  };

  const config = configs[type] || {
    label: type,
    style: 'bg-gray-50 text-gray-655 border-gray-200',
    icon: Info,
  };

  const Icon = config.icon;

  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[9px] font-bold border uppercase tracking-wider ${config.style}`}>
      <Icon className="h-3 w-3 shrink-0" />
      {config.label}
    </span>
  );
}
export { NotificationBadge };
