import React from 'react';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EligibilityWidget from '../../components/donor/EligibilityWidget';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import {
  Calendar,
  Clock,
  ShieldCheck,
  CheckCircle2,
  AlertTriangle,
  Droplets,
  Heart,
  UserCheck,
} from 'lucide-react';

/**
 * Dedicated Donor Eligibility Page.
 * Displays authoritative donation eligibility status, 90-day cooldown countdown,
 * next eligible date, health safeguards, and medical donor rules.
 */
export default function DonorEligibility() {
  const { profile, isLoading, error, refetch } = useDonorProfile();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message || 'Unable to load donor eligibility'} onRetry={refetch} />;
  }

  const bloodGroup = profile?.bloodGroup?.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') || 'N/A';
  const lastDonationStr = profile?.lastDonationDate;
  const nextEligibleStr = profile?.nextEligibleDate;
  const cooldownDays = profile?.cooldownDays || 90;

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

  const daysRemaining = calculateDaysRemaining(nextEligibleStr);
  const isEligible = daysRemaining === 0;

  const formatDateLong = (dateStr) => {
    if (!dateStr) return 'No previous donations';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return 'No previous donations';
    return d.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
  };

  return (
    <div className="flex flex-col gap-8 max-w-5xl mx-auto pb-12 font-sans">
      
      {/* Top Banner */}
      <div className="bg-slate-900 text-white rounded-3xl p-6 sm:p-8 border border-slate-800 shadow-xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="flex items-center gap-4">
          <div className="h-14 w-14 rounded-2xl bg-red-600/20 text-red-400 border border-red-500/30 flex items-center justify-center font-black text-xl shrink-0">
            {bloodGroup}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-extrabold uppercase tracking-widest text-red-400 bg-red-950 px-2.5 py-0.5 rounded border border-red-900">
                Medical Eligibility Center
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-white mt-1">
              Donation Eligibility Status
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Automated 90-day cooldown safeguards for donor health and patient safety.
            </p>
          </div>
        </div>

        <Badge variant={isEligible ? 'success' : 'warning'} className="px-4 py-2 text-xs font-black shadow-md">
          {isEligible ? '✓ READY TO DONATE' : `⏳ ${daysRemaining} DAYS COOLDOWN`}
        </Badge>
      </div>

      {/* Main Grid: Prominent Widget + Detailed Guidelines */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        
        {/* Left Column: Authoritative Eligibility Widget (7 cols) */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          <EligibilityWidget profile={profile} />

          {/* Timeline Breakdown Card */}
          <Card title="Eligibility Timeline Breakdown" subtitle="Authoritative donor schedule parameters">
            <div className="flex flex-col gap-4 text-xs">
              <div className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-800/60 rounded-2xl border border-slate-200/80 dark:border-slate-700/60">
                <div className="flex items-center gap-3">
                  <Calendar className="h-5 w-5 text-red-500" />
                  <div>
                    <span className="text-[10px] font-bold text-slate-400 uppercase">Last Donation Date</span>
                    <p className="font-extrabold text-slate-800 dark:text-slate-200 text-sm">{formatDateLong(lastDonationStr)}</p>
                  </div>
                </div>
                <Badge variant={lastDonationStr ? 'default' : 'secondary'}>
                  {lastDonationStr ? 'Recorded' : 'First-time'}
                </Badge>
              </div>

              <div className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-800/60 rounded-2xl border border-slate-200/80 dark:border-slate-700/60">
                <div className="flex items-center gap-3">
                  <Clock className="h-5 w-5 text-amber-500" />
                  <div>
                    <span className="text-[10px] font-bold text-slate-400 uppercase">Mandatory Cooldown Period</span>
                    <p className="font-extrabold text-slate-800 dark:text-slate-200 text-sm">{cooldownDays} Full Days</p>
                  </div>
                </div>
                <Badge variant="outline">90-Day Standard</Badge>
              </div>

              <div className="flex items-center justify-between p-4 bg-emerald-50/70 dark:bg-emerald-950/40 rounded-2xl border border-emerald-200/80 dark:border-emerald-800/60">
                <div className="flex items-center gap-3">
                  <CheckCircle2 className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
                  <div>
                    <span className="text-[10px] font-bold text-emerald-700 dark:text-emerald-300 uppercase">Next Eligible Date</span>
                    <p className="font-extrabold text-emerald-800 dark:text-emerald-200 text-sm">
                      {isEligible ? 'Eligible Right Now' : formatDateLong(nextEligibleStr)}
                    </p>
                  </div>
                </div>
                <Badge variant={isEligible ? 'success' : 'warning'}>
                  {isEligible ? 'Available' : `${daysRemaining} days remaining`}
                </Badge>
              </div>
            </div>
          </Card>
        </div>

        {/* Right Column: Health Rules & Safeguards (5 cols) */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          <Card title="Medical Health Safeguards" subtitle="BloodBridge donor eligibility requirements">
            <div className="flex flex-col gap-4 text-xs">
              <div className="flex items-start gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                <ShieldCheck className="h-4 w-4 text-emerald-500 shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-slate-900 dark:text-white">90-Day Cooldown Rule</h4>
                  <p className="text-[11px] text-slate-500 mt-0.5">Allows red blood cell recovery between donations.</p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                <Droplets className="h-4 w-4 text-red-500 shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-slate-900 dark:text-white">Minimum Hemoglobin</h4>
                  <p className="text-[11px] text-slate-500 mt-0.5">Ensure hemoglobin levels are above 12.5 g/dL before donating.</p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                <Heart className="h-4 w-4 text-rose-500 shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-slate-900 dark:text-white">Well-being & Rest</h4>
                  <p className="text-[11px] text-slate-500 mt-0.5">Get 7+ hours of sleep and drink 500ml water prior to donation.</p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                <UserCheck className="h-4 w-4 text-blue-500 shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-slate-900 dark:text-white">Hospital Sign-Off</h4>
                  <p className="text-[11px] text-slate-500 mt-0.5">Medical staff verifies eligibility prior to transfusion.</p>
                </div>
              </div>
            </div>
          </Card>

          <Card title="Important Note" className="bg-amber-50/50 dark:bg-amber-950/20 border-amber-200 dark:border-amber-900">
            <div className="flex items-start gap-3 text-xs text-amber-800 dark:text-amber-300">
              <AlertTriangle className="h-5 w-5 shrink-0 text-amber-600 dark:text-amber-400 mt-0.5" />
              <p className="leading-relaxed">
                If you have recently suffered from a viral infection, undergone major surgery, or taken antibiotics, please notify the attending hospital medical officer.
              </p>
            </div>
          </Card>
        </div>

      </div>

    </div>
  );
}
