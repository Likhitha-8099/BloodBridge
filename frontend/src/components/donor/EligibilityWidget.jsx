import React from 'react';
import { CheckCircle2, Clock, Heart, Calendar, Sparkles, AlertTriangle, ShieldCheck } from 'lucide-react';
import Badge from '../ui/Badge';
import Card from '../ui/Card';

/**
 * Single Source of Truth Donor Eligibility & Real-Time Countdown Widget.
 * Calculates days remaining from nextEligibleDate (backend authoritative date).
 */
export default function EligibilityWidget({ eligibilityData, profile }) {
  const nextDateStr = eligibilityData?.nextEligibleDate || profile?.nextEligibleDate;
  const lastDonationStr = eligibilityData?.lastDonationDate || profile?.lastDonationDate;
  const cooldownDays = profile?.cooldownDays || eligibilityData?.cooldownDays || 90;

  // Real-time calculation: difference in full calendar days between today and nextEligibleDate
  const calculateDaysRemaining = (nextStr) => {
    if (!nextStr) return 0;
    const target = new Date(nextStr);
    if (isNaN(target.getTime())) return 0;

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    target.setHours(0, 0, 0, 0);

    const diffTime = target.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return Math.max(0, diffDays);
  };

  const daysRemaining = calculateDaysRemaining(nextDateStr);

  // Check if donor has completed any previous donation
  const hasDonated = Boolean(lastDonationStr);

  // Eligibility evaluation: donor is eligible if daysRemaining === 0 and no permanent deferral
  const isPermanentlyDeferred = profile?.eligibilityStatus === 'PERMANENTLY_DEFERRED';
  const isEligible = !isPermanentlyDeferred && daysRemaining === 0;

  // Format date as "08 November 2026"
  const formatDateLong = (dateStr) => {
    if (!dateStr) return null;
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return null;
    return d.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  };

  const formattedLastDonation = formatDateLong(lastDonationStr);
  const formattedNextEligible = formatDateLong(nextDateStr);

  // Cooldown completion percentage
  const pctCompleted = isEligible
    ? 100
    : Math.max(0, Math.min(100, Math.round(((cooldownDays - daysRemaining) / cooldownDays) * 100)));

  return (
    <Card className={`p-6 relative overflow-hidden transition-all border ${
      isEligible
        ? 'bg-gradient-to-br from-white via-emerald-50/20 to-white dark:from-slate-900 dark:via-emerald-950/20 dark:to-slate-900 border-emerald-200/80 dark:border-emerald-800/60'
        : 'bg-gradient-to-br from-white via-amber-50/20 to-white dark:from-slate-900 dark:via-amber-950/20 dark:to-slate-900 border-amber-200/80 dark:border-amber-800/60'
    }`}>
      {/* Top Header */}
      <div className="flex items-center justify-between gap-4 mb-5 pb-4 border-b border-slate-100 dark:border-slate-800">
        <div className="flex items-center gap-3">
          <div className={`h-11 w-11 rounded-2xl flex items-center justify-center shrink-0 shadow-sm ${
            isEligible
              ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-950 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800'
              : 'bg-amber-100 text-amber-600 dark:bg-amber-950 dark:text-amber-400 border border-amber-200 dark:border-amber-800'
          }`}>
            {isEligible ? <CheckCircle2 className="h-6 w-6" /> : <Clock className="h-6 w-6 animate-pulse" />}
          </div>
          <div>
            <span className="text-[10px] font-extrabold uppercase tracking-widest text-slate-400">
              Donation Status
            </span>
            <h3 className="text-base font-black text-slate-900 dark:text-white flex items-center gap-2">
              {isEligible ? 'ELIGIBLE TO DONATE' : 'NOT YET ELIGIBLE'}
            </h3>
          </div>
        </div>

        <Badge variant={isEligible ? 'success' : 'warning'} className="px-3 py-1 text-xs font-extrabold">
          {isEligible ? 'Ready to Donate' : 'Cooldown Active'}
        </Badge>
      </div>

      {/* Hero Countdown / Eligibility Section */}
      <div className="mb-6">
        {!isEligible ? (
          /* Cooldown Active View */
          <div className="bg-amber-50/70 dark:bg-amber-950/40 rounded-2xl p-5 border border-amber-200/60 dark:border-amber-800/50 flex flex-col gap-4 text-center">
            <div className="flex flex-col items-center gap-1">
              <span className="text-xs font-bold text-amber-700 dark:text-amber-300 uppercase tracking-wider">
                You can donate again in
              </span>
              <div className="flex items-baseline justify-center gap-2 mt-1">
                <span className="text-4xl sm:text-5xl font-black text-amber-600 dark:text-amber-400 tracking-tight leading-none">
                  {daysRemaining}
                </span>
                <span className="text-lg font-extrabold text-amber-800 dark:text-amber-200 uppercase tracking-wide">
                  {daysRemaining === 1 ? 'DAY REMAINING' : 'DAYS REMAINING'}
                </span>
              </div>
            </div>

            {/* Cooldown Progress Bar */}
            <div className="flex flex-col gap-1.5 mt-1">
              <div className="flex justify-between text-[11px] font-bold text-amber-800 dark:text-amber-300">
                <span>{cooldownDays}-Day Cooldown Progress</span>
                <span>{pctCompleted}%</span>
              </div>
              <div className="w-full bg-amber-200/60 dark:bg-amber-900/60 h-2.5 rounded-full overflow-hidden">
                <div
                  className="h-2.5 bg-amber-500 rounded-full transition-all duration-700 shadow-sm"
                  style={{ width: `${pctCompleted}%` }}
                />
              </div>
            </div>
          </div>
        ) : hasDonated ? (
          /* Eligible after previous donation View */
          <div className="bg-emerald-50/80 dark:bg-emerald-950/40 rounded-2xl p-5 border border-emerald-200/70 dark:border-emerald-800/60 flex flex-col items-center text-center gap-2">
            <div className="p-3 bg-emerald-100 dark:bg-emerald-900/60 text-emerald-600 dark:text-emerald-400 rounded-2xl">
              <Heart className="h-7 w-7 fill-emerald-500 text-emerald-600 animate-bounce" />
            </div>
            <h4 className="text-lg font-black text-emerald-800 dark:text-emerald-300">
              ❤️ YOU ARE ELIGIBLE TO DONATE
            </h4>
            <p className="text-xs text-emerald-700 dark:text-emerald-400 font-medium max-w-sm leading-relaxed">
              You can donate blood again now. Your 90-day cooldown period is complete!
            </p>
          </div>
        ) : (
          /* Eligible First-Time Donor View */
          <div className="bg-red-50/70 dark:bg-red-950/40 rounded-2xl p-5 border border-red-200/60 dark:border-red-800/50 flex flex-col items-center text-center gap-2">
            <div className="p-3 bg-red-100 dark:bg-red-900/60 text-red-600 dark:text-red-400 rounded-2xl">
              <Sparkles className="h-7 w-7 text-red-600" />
            </div>
            <h4 className="text-lg font-black text-red-800 dark:text-red-300">
              ❤️ READY TO MAKE A DIFFERENCE?
            </h4>
            <p className="text-xs text-red-700 dark:text-red-400 font-medium max-w-sm leading-relaxed">
              You have no active donation cooldown. You are currently eligible to donate blood.
            </p>
          </div>
        )}
      </div>

      {/* Date & Cooldown Info Details */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs bg-slate-50 dark:bg-slate-800/60 p-4 rounded-2xl border border-slate-100 dark:border-slate-700/60">
        <div className="flex flex-col gap-0.5">
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1">
            <Calendar className="h-3 w-3 text-slate-400" /> Next Eligible Date
          </span>
          <span className="font-extrabold text-slate-800 dark:text-slate-200 text-sm">
            {isEligible ? 'Available Now' : (formattedNextEligible || 'Calculating...')}
          </span>
        </div>

        <div className="flex flex-col gap-0.5">
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1">
            <ShieldCheck className="h-3 w-3 text-slate-400" /> Last Donation
          </span>
          <span className="font-extrabold text-slate-800 dark:text-slate-200 text-sm">
            {formattedLastDonation || 'No previous donations'}
          </span>
        </div>
      </div>

      {/* Health Advice Footer Strip */}
      <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center gap-2 text-[11px] text-slate-500 dark:text-slate-400">
        <AlertTriangle className="h-3.5 w-3.5 text-amber-500 shrink-0" />
        <span>Stay hydrated and maintain balanced iron levels prior to your blood donation.</span>
      </div>
    </Card>
  );
}
