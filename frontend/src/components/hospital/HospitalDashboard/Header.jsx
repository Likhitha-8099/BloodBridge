import React from 'react';
import { Calendar, ShieldCheck, Building2 } from 'lucide-react';
import { useAuthStore } from '../../../store/authStore';

/**
 * Hospital Identity & Welcome Banner (inside Hospital Dashboard page).
 * Displays institution verification, welcome back message, date, and emergency context.
 * Unified with top-level layout header without duplicating navbar controls or logos.
 */
export default function Header({ hospitalName, verificationStatus }) {
  const { user } = useAuthStore();

  const formattedDate = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  const isVerified = verificationStatus === 'APPROVED' || verificationStatus === 'VERIFIED';
  const nameToDisplay = hospitalName || user?.hospitalName || user?.fullName || 'Registered Hospital';
  const initials = nameToDisplay
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'H';

  return (
    <div className="bg-gradient-to-r from-teal-700 via-teal-800 to-emerald-800 dark:from-slate-950 dark:via-teal-950 dark:to-slate-900 text-white rounded-3xl p-6 shadow-xl border border-teal-600/40 dark:border-teal-900/40 mb-2 w-full">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
        
        {/* Left Side: Institution Avatar & Name Details */}
        <div className="flex items-center gap-4">
          <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center text-white font-black text-lg shadow-lg border border-teal-400/30 shrink-0">
            {initials || <Building2 className="h-6 w-6" />}
          </div>

          <div>
            <div className="flex items-center gap-3 flex-wrap">
              <h1 className="text-xl sm:text-2xl font-black tracking-tight text-white">
                {nameToDisplay}
              </h1>
              <span className={`inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full text-xs font-bold uppercase tracking-wider ${
                isVerified
                  ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                  : 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
              }`}>
                <ShieldCheck className="h-3.5 w-3.5" />
                {isVerified ? 'Verified Institution' : (verificationStatus || 'Pending Verification')}
              </span>
            </div>

            <p className="text-xs text-slate-300 mt-1 flex items-center gap-2 flex-wrap">
              <span>Welcome back, <strong className="text-white">{user?.fullName || user?.name || nameToDisplay}</strong></span>
              <span className="text-teal-500">•</span>
              <span className="text-teal-300 font-medium">Emergency Response Center</span>
            </p>
          </div>
        </div>

        {/* Right Side: Current Date Badge */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-4 py-2 bg-slate-900/80 border border-slate-800 rounded-2xl text-xs text-slate-300 shadow-inner">
            <Calendar className="h-4 w-4 text-teal-400" />
            <span className="font-semibold">{formattedDate}</span>
          </div>
        </div>

      </div>
    </div>
  );
}
