import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import { useDonationHistory } from '../../hooks/useDonationHistory';
import { useWebSocket } from '../../hooks/useWebSocket';
import useAuthStore from '../../store/authStore';
import donationService from '../../services/donationService';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import {
  Heart, Droplets, CalendarCheck2, Clock, CheckCircle2,
  Award, Download, Activity, Target,
  HeartHandshake, Zap, Star, User, Wifi, WifiOff, Edit3, TrendingUp, ShieldCheck
} from 'lucide-react';

// ─── Helpers ─────────────────────────────────────────────────────────────────
const fmt = (dateStr) => {
  if (!dateStr) return null;
  return new Date(dateStr).toLocaleDateString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric'
  });
};

const fmtBg = (bg) =>
  bg ? bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : '?';

const statusColor = (status) => {
  switch (status) {
    case 'COMPLETED': return 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/50 dark:text-emerald-400 dark:border-emerald-800';
    case 'ACCEPTED': return 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/50 dark:text-blue-400 dark:border-blue-800';
    case 'REJECTED': case 'CANCELLED': return 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/50 dark:text-red-400 dark:border-red-800';
    default: return 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/50 dark:text-amber-400 dark:border-amber-800';
  }
};

// ─── Stat Card ────────────────────────────────────────────────────────────────
function ImpactStatCard({ icon: Icon, label, value, sub, color = 'red' }) {
  const colorMap = {
    red: 'bg-red-50 text-red-600 dark:bg-red-950/60 dark:text-red-400',
    emerald: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/60 dark:text-emerald-400',
    indigo: 'bg-indigo-50 text-indigo-600 dark:bg-indigo-950/60 dark:text-indigo-400',
    amber: 'bg-amber-50 text-amber-600 dark:bg-amber-950/60 dark:text-amber-400',
    blue: 'bg-blue-50 text-blue-600 dark:bg-blue-950/60 dark:text-blue-400',
  };
  return (
    <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-5 flex items-start gap-4 shadow-sm hover:shadow-md transition-shadow">
      <div className={`h-11 w-11 rounded-xl flex items-center justify-center shrink-0 ${colorMap[color]}`}>
        <Icon className="h-5 w-5" />
      </div>
      <div className="flex flex-col gap-0.5 min-w-0">
        <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">{label}</span>
        <span className="text-xl font-black text-slate-900 dark:text-white leading-tight">{value ?? '—'}</span>
        {sub && <span className="text-xs text-slate-500 dark:text-slate-400 truncate">{sub}</span>}
      </div>
    </div>
  );
}

// ─── Eligibility Panel ────────────────────────────────────────────────────────
function EligibilityPanel({ profile }) {
  const isEligible = profile?.eligible || profile?.eligibilityStatus === 'ELIGIBLE';
  const days = profile?.daysUntilEligible ?? 0;
  const pct = isEligible ? 100 : Math.max(0, Math.min(100, Math.round(((profile?.cooldownDays ?? 90) - days) / (profile?.cooldownDays ?? 90) * 100)));

  return (
    <div className={`rounded-2xl border p-5 flex flex-col gap-4 ${
      isEligible
        ? 'bg-gradient-to-br from-emerald-50 to-white dark:from-emerald-950/30 dark:to-slate-900 border-emerald-200 dark:border-emerald-800/60'
        : 'bg-gradient-to-br from-amber-50 to-white dark:from-amber-950/30 dark:to-slate-900 border-amber-200 dark:border-amber-800/60'
    }`}>
      <div className="flex items-center justify-between gap-2 flex-wrap">
        <div className="flex items-center gap-3">
          <div className={`h-10 w-10 rounded-xl flex items-center justify-center ${
            isEligible ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/60 dark:text-emerald-400'
                       : 'bg-amber-100 text-amber-600 dark:bg-amber-900/60 dark:text-amber-400'
          }`}>
            {isEligible ? <CheckCircle2 className="h-5 w-5" /> : <Clock className="h-5 w-5" />}
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-0.5">Donation Eligibility</p>
            <h3 className={`text-base font-extrabold ${isEligible ? 'text-emerald-700 dark:text-emerald-400' : 'text-amber-700 dark:text-amber-400'}`}>
              {isEligible ? '✅ ELIGIBLE — Ready to Donate' : `⏳ NOT ELIGIBLE — ${days} day${days !== 1 ? 's' : ''} remaining`}
            </h3>
          </div>
        </div>
        <span className={`text-xs font-bold px-3 py-1 rounded-full border ${
          isEligible ? 'bg-emerald-100 text-emerald-700 border-emerald-300 dark:bg-emerald-900/40 dark:text-emerald-400 dark:border-emerald-700'
                     : 'bg-amber-100 text-amber-700 border-amber-300 dark:bg-amber-900/40 dark:text-amber-400 dark:border-amber-700'
        }`}>
          {isEligible ? 'Donate Now' : `${profile?.cooldownDays ?? 90}-Day Cooldown`}
        </span>
      </div>

      {/* Progress bar */}
      <div className="flex flex-col gap-2">
        <div className="flex justify-between text-xs font-semibold text-slate-500 dark:text-slate-400">
          <span>Cooldown Progress</span>
          <span>{pct}%</span>
        </div>
        <div className="w-full bg-slate-100 dark:bg-slate-800 h-2.5 rounded-full overflow-hidden">
          <div
            className={`h-2.5 rounded-full transition-all duration-700 ${isEligible ? 'bg-emerald-500' : 'bg-amber-400'}`}
            style={{ width: `${pct}%` }}
          />
        </div>
        <div className="flex justify-between text-[10px] text-slate-400">
          <span>Last: {fmt(profile?.lastDonationDate) ?? 'No donations yet'}</span>
          {!isEligible && <span>Next eligible: {fmt(profile?.nextEligibleDate) ?? 'Calculating...'}</span>}
          {isEligible && <span className="text-emerald-600 dark:text-emerald-400 font-bold">🟢 You can donate today!</span>}
        </div>
      </div>
    </div>
  );
}

// ─── Impact Journey Banner ────────────────────────────────────────────────────
function ImpactJourney({ profile, totalCompleted, totalUnits }) {
  const bgStr = fmtBg(profile?.bloodGroup);

  const facts = [
    { icon: Droplets, value: totalCompleted, label: `Donation${totalCompleted !== 1 ? 's' : ''} Completed` },
    { icon: Heart, value: `${totalUnits} unit${totalUnits !== 1 ? 's' : ''}`, label: 'Blood Donated' },
    { icon: Star, value: profile?.donorScore ?? 0, label: 'Donor Score' },
    { icon: Zap, value: bgStr, label: 'Blood Group' },
  ];

  return (
    <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-red-600 via-rose-600 to-red-800 dark:from-red-800 dark:via-rose-800 dark:to-red-950 p-8 text-white shadow-2xl">
      {/* Decorative rings */}
      <div className="absolute -top-12 -right-12 h-48 w-48 rounded-full bg-white/5" />
      <div className="absolute -bottom-8 -left-8 h-36 w-36 rounded-full bg-white/5" />
      <div className="absolute top-6 right-24 h-20 w-20 rounded-full bg-white/5" />

      <div className="relative z-10">
        <div className="flex items-start justify-between flex-wrap gap-4 mb-6">
          <div>
            <p className="text-red-200 text-xs font-bold uppercase tracking-widest mb-1">Your Donation Journey</p>
            <h2 className="text-2xl sm:text-3xl font-black leading-tight">
              {profile?.fullName ? `Thank you, ${profile.fullName.split(' ')[0]}!` : 'Your Impact Matters'}
            </h2>
            <p className="text-red-100 text-sm mt-1 max-w-sm">
              {totalCompleted === 0
                ? 'Make your first donation to start your life-saving journey.'
                : `You've completed ${totalCompleted} blood donation${totalCompleted !== 1 ? 's' : ''}, contributing ${totalUnits} unit${totalUnits !== 1 ? 's' : ''} of blood to those in need.`}
            </p>
          </div>
          <div className="h-16 w-16 rounded-3xl bg-white/15 backdrop-blur-sm flex items-center justify-center font-black text-2xl border border-white/20">
            {bgStr}
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {facts.map(({ icon: Icon, value, label }, i) => (
            <div key={i} className="bg-white/10 backdrop-blur-sm rounded-2xl p-3.5 border border-white/15">
              <Icon className="h-4 w-4 text-red-200 mb-2" />
              <p className="text-xl font-black leading-tight">{value}</p>
              <p className="text-red-200 text-[10px] font-semibold mt-0.5">{label}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ─── Donation Table ───────────────────────────────────────────────────────────
function DonationTable({ donations, donorBloodGroup }) {
  if (!donations?.length) {
    return (
      <div className="flex flex-col items-center justify-center text-center gap-4 py-14 px-6">
        <div className="h-14 w-14 rounded-2xl bg-red-50 dark:bg-red-950/40 text-red-400 flex items-center justify-center">
          <Heart className="h-7 w-7" />
        </div>
        <div>
          <p className="font-bold text-gray-800 dark:text-white text-sm">No Completed Donations Yet</p>
          <p className="text-xs text-gray-500 dark:text-slate-400 mt-1 max-w-xs">
            Once you complete a blood donation, it will appear here with your certificate.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto -mx-1">
      <table className="w-full text-left text-xs border-collapse min-w-[600px]">
        <thead>
          <tr className="border-b border-slate-100 dark:border-slate-800 text-slate-400 font-bold uppercase tracking-wider bg-slate-50/80 dark:bg-slate-800/40">
            <th className="py-3 px-4 rounded-tl-xl">Date</th>
            <th className="py-3 px-4">Hospital</th>
            <th className="py-3 px-4">Blood Group</th>
            <th className="py-3 px-4">Units</th>
            <th className="py-3 px-4">Status</th>
            <th className="py-3 px-4 text-center rounded-tr-xl">Certificate</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-50 dark:divide-slate-800/60">
          {donations.map((item) => {
            const isCompleted = item.status === 'COMPLETED' || item.certificateAvailable;
            const bg = item.bloodGroup
              ? fmtBg(item.bloodGroup)
              : fmtBg(donorBloodGroup);
            return (
              <tr key={item.id} className="hover:bg-slate-50/60 dark:hover:bg-slate-800/30 transition group">
                <td className="py-4 px-4 font-semibold text-slate-800 dark:text-slate-200 whitespace-nowrap">
                  {fmt(item.donationDate) ?? '—'}
                </td>
                <td className="py-4 px-4 text-slate-600 dark:text-slate-300 font-medium max-w-[160px] truncate">
                  {item.hospitalName ?? '—'}
                </td>
                <td className="py-4 px-4">
                  <span className="font-extrabold text-red-600 dark:text-red-400 text-sm">{bg}</span>
                </td>
                <td className="py-4 px-4 font-bold text-slate-800 dark:text-slate-200">
                  {item.unitsDonated ?? 1} unit{(item.unitsDonated ?? 1) !== 1 ? 's' : ''}
                </td>
                <td className="py-4 px-4">
                  <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-[10px] font-bold border ${statusColor(item.status)}`}>
                    {item.status ?? 'PENDING'}
                  </span>
                </td>
                <td className="py-4 px-4 text-center">
                  {isCompleted ? (
                    <button
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-red-600 hover:bg-red-700 active:scale-95 text-white text-[11px] font-bold transition-all shadow-sm hover:shadow-red-200 dark:hover:shadow-red-900/40"
                      onClick={() => donationService.downloadCertificate(item.id)}
                    >
                      <Download className="h-3.5 w-3.5" /> Certificate
                    </button>
                  ) : (
                    <span className="text-slate-300 dark:text-slate-600 text-[11px] italic">—</span>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

// ─── Donor Header Card ────────────────────────────────────────────────────────
function DonorHeaderCard({ profile }) {
  const isEligible = profile?.eligible || profile?.eligibilityStatus === 'ELIGIBLE';
  const bgStr = fmtBg(profile?.bloodGroup);

  return (
    <Card className="p-6 sm:p-8">
      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5 sm:gap-6">
        {/* Avatar / Blood Group */}
        <div className="h-20 w-20 rounded-3xl bg-gradient-to-br from-red-500 to-rose-600 flex items-center justify-center font-black text-white text-3xl shadow-lg shrink-0">
          {bgStr}
        </div>

        {/* Donor Info */}
        <div className="flex-1 flex flex-col gap-2 min-w-0">
          <div className="flex items-center gap-3 flex-wrap">
            <h2 className="text-xl font-black text-slate-900 dark:text-white">
              {profile?.fullName ?? 'Donor'}
            </h2>
            <span className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
              isEligible
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800'
                : 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-400 dark:border-amber-800'
            }`}>
              {isEligible ? '🟢 ELIGIBLE' : '🟡 NOT ELIGIBLE'}
            </span>
            {profile?.emergencyAvailable && (
              <span className="text-xs font-bold px-2.5 py-1 rounded-full border bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/40 dark:text-blue-400 dark:border-blue-800">
                ⚡ Emergency Available
              </span>
            )}
          </div>
          <div className="flex items-center gap-4 text-xs text-slate-500 dark:text-slate-400 flex-wrap">
            {profile?.city && profile?.state && (
              <span>📍 {profile.city}, {profile.state}</span>
            )}
            {profile?.age && <span>🎂 {profile.age} years</span>}
            {profile?.gender && <span>👤 {profile.gender.charAt(0) + profile.gender.slice(1).toLowerCase()}</span>}
          </div>
          <div className="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
            <ShieldCheck className="h-3.5 w-3.5 text-emerald-500" />
            <span>Verified BloodBridge Donor</span>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="flex flex-row sm:flex-col gap-2 shrink-0">
          <Link to="/donor/profile/edit">
            <Button variant="outline" size="sm" className="flex items-center gap-1.5 text-xs">
              <Edit3 className="h-3.5 w-3.5" /> Edit Profile
            </Button>
          </Link>
          <Link to="/donor/history">
            <Button variant="outline" size="sm" className="flex items-center gap-1.5 text-xs">
              <Activity className="h-3.5 w-3.5" /> Full History
            </Button>
          </Link>
        </div>
      </div>
    </Card>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function DonorImpactDashboard() {
  const { user } = useAuthStore();
  const {
    profile,
    isLoading: profileLoading,
    error: profileError,
    refetch: refetchProfile
  } = useDonorProfile();

  const donorId = profile?.id;
  const {
    data: donations,
    isLoading: historyLoading,
    error: historyError,
    refetch: refetchHistory,
  } = useDonationHistory(donorId);

  // Real-time WS subscription to refresh on donation events
  const topics = useMemo(() => {
    const list = [];
    if (user?.id) list.push(`/topic/notifications/${user.id}`);
    if (donorId) list.push(`/topic/donor/${donorId}`);
    return list;
  }, [user?.id, donorId]);

  const { isConnected, isFallback } = useWebSocket(topics, () => {
    refetchProfile();
    refetchHistory();
  });

  const isLoading = profileLoading || historyLoading;
  const error = profileError || historyError;

  // Derive computed stats from backend data only
  const completedDonations = useMemo(
    () => (donations ?? []).filter((d) => d.status === 'COMPLETED' || d.certificateAvailable),
    [donations]
  );
  const totalCompleted = completedDonations.length;
  const totalUnits = completedDonations.reduce((sum, d) => sum + (d.unitsDonated ?? 1), 0);
  const lastDonation = profile?.lastDonationDate;
  const nextEligible = profile?.nextEligibleDate;
  const daysRemaining = profile?.daysUntilEligible ?? 0;
  const isEligible = profile?.eligible || profile?.eligibilityStatus === 'ELIGIBLE';

  // ── Loading State
  if (isLoading) return <LoadingSpinner fullScreen />;

  // ── Error State
  if (error) {
    return (
      <ErrorState
        message={error?.message ?? 'Failed to load donor impact data.'}
        onRetry={() => { refetchProfile(); refetchHistory(); }}
      />
    );
  }

  // ── No Profile
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-12">
        <Card className="flex flex-col items-center justify-center text-center p-10 gap-5 border border-dashed border-gray-200 rounded-3xl shadow-sm">
          <div className="p-4 bg-red-50 text-primary rounded-2xl border border-red-100">
            <User className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">Donor Profile Required</h2>
            <p className="text-xs text-gray-500 dark:text-slate-400 leading-relaxed">
              Create your donor profile to unlock your personal impact dashboard, donation history, certificates, and eligibility tracker.
            </p>
          </div>
          <Link to="/donor/profile/edit">
            <Button variant="primary" className="px-6 py-2.5 font-bold">Create Donor Profile</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 font-sans pb-10">

      {/* ── Page Header */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-white flex items-center gap-3">
            <span className="inline-flex items-center gap-1.5">
              <TrendingUp className="h-6 w-6 text-primary" />
              Donor Impact Dashboard
            </span>
            {isConnected ? (
              <span className="flex items-center gap-1 text-xs text-emerald-600 dark:text-emerald-400 font-semibold bg-emerald-50 dark:bg-emerald-950/50 px-2.5 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
                <Wifi className="h-3 w-3" /> Live
              </span>
            ) : (
              <span className="flex items-center gap-1 text-xs text-amber-600 dark:text-amber-400 font-semibold bg-amber-50 dark:bg-amber-950/50 px-2.5 py-1 rounded-full border border-amber-200 dark:border-amber-800">
                <WifiOff className="h-3 w-3" /> {isFallback ? 'REST' : 'Reconnecting'}
              </span>
            )}
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Your complete blood donation journey, eligibility status, and impact metrics.
          </p>
        </div>
      </div>

      {/* ── 1. Donor Summary Header */}
      <DonorHeaderCard profile={profile} />

      {/* ── 2. Impact Journey Banner (data from backend only) */}
      <ImpactJourney profile={profile} totalCompleted={totalCompleted} totalUnits={totalUnits} />

      {/* ── 3. Donation Statistics Grid */}
      <div>
        <h2 className="text-sm font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3">
          Donation Statistics
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <ImpactStatCard
            icon={Heart}
            label="Completed Donations"
            value={profile.totalDonations ?? 0}
            sub="Verified by BloodBridge"
            color="red"
          />
          <ImpactStatCard
            icon={Droplets}
            label="Units Donated"
            value={`${totalUnits} unit${totalUnits !== 1 ? 's' : ''}`}
            sub="Total blood contributed"
            color="blue"
          />
          <ImpactStatCard
            icon={CalendarCheck2}
            label="Last Donation"
            value={fmt(lastDonation) ?? 'No donations yet'}
            sub={lastDonation ? 'Most recent date' : 'Complete your first!'}
            color="emerald"
          />
          <ImpactStatCard
            icon={Clock}
            label={isEligible ? 'Next Eligible Date' : 'Days Remaining'}
            value={isEligible ? 'Today ✅' : `${daysRemaining} day${daysRemaining !== 1 ? 's' : ''}`}
            sub={isEligible ? 'Ready to donate now' : (fmt(nextEligible) ?? 'Pending calculation')}
            color={isEligible ? 'emerald' : 'amber'}
          />
        </div>
      </div>

      {/* ── 4. Eligibility Progress Section */}
      <div>
        <h2 className="text-sm font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3">
          Eligibility Status
        </h2>
        <EligibilityPanel profile={profile} />
      </div>

      {/* ── 5. Donation Timeline Table */}
      <Card className="p-6">
        <div className="flex items-center justify-between mb-5 flex-wrap gap-3">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <Activity className="h-4 w-4 text-primary" />
              Donation Timeline
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">All donation records with certificate availability</p>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-500 dark:text-slate-400 font-semibold bg-slate-100 dark:bg-slate-800 px-3 py-1.5 rounded-full border border-slate-200 dark:border-slate-700">
              {donations?.length ?? 0} record{(donations?.length ?? 0) !== 1 ? 's' : ''}
            </span>
            <Link to="/donor/history">
              <Button variant="outline" size="sm" className="text-xs flex items-center gap-1.5">
                View All
              </Button>
            </Link>
          </div>
        </div>
        <DonationTable donations={donations} donorBloodGroup={profile?.bloodGroup} />
      </Card>

      {/* ── 6. Emergency Availability Info */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Card className="p-5 flex items-start gap-4">
          <div className="h-10 w-10 rounded-xl bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
            <Zap className="h-5 w-5" />
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">Emergency Availability</p>
            <p className="text-base font-black text-slate-900 dark:text-white">
              {profile?.emergencyAvailable ? '⚡ Available' : '❌ Not Available'}
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              {profile?.emergencyAvailable
                ? 'You are opted in for emergency blood request alerts.'
                : 'Enable emergency availability in your profile settings.'}
            </p>
          </div>
        </Card>

        <Card className="p-5 flex items-start gap-4">
          <div className="h-10 w-10 rounded-xl bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
            <Award className="h-5 w-5" />
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">Donor Score</p>
            <p className="text-base font-black text-slate-900 dark:text-white">
              {profile?.donorScore ?? 0} pts
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              {profile?.donorScore >= 200 ? '🏆 Life Saver Hero' : profile?.donorScore >= 160 ? '✨ Platinum Donor' : profile?.donorScore >= 130 ? '⭐ Gold Donor' : profile?.donorScore >= 100 ? '🥈 Silver Donor' : '🥉 Bronze Champion'}
            </p>
          </div>
        </Card>
      </div>

      {/* ── 7. Quick Navigation Links */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {[
          { to: '/donor/dashboard', label: 'Dashboard', icon: HeartHandshake, color: 'text-red-600 bg-red-50 dark:bg-red-950/40' },
          { to: '/donor/requests', label: 'Blood Requests', icon: Target, color: 'text-blue-600 bg-blue-50 dark:bg-blue-950/40' },
          { to: '/donor/history', label: 'Full History', icon: Activity, color: 'text-emerald-600 bg-emerald-50 dark:bg-emerald-950/40' },
          { to: '/donor/profile', label: 'My Profile', icon: User, color: 'text-amber-600 bg-amber-50 dark:bg-amber-950/40' },
        ].map(({ to, label, icon: Icon, color }) => (
          <Link key={to} to={to}>
            <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl p-4 flex flex-col items-center gap-2 hover:shadow-md transition-all text-center cursor-pointer group">
              <div className={`h-10 w-10 rounded-xl flex items-center justify-center ${color}`}>
                <Icon className="h-5 w-5" />
              </div>
              <span className="text-xs font-bold text-slate-700 dark:text-slate-300 group-hover:text-slate-900 dark:group-hover:text-white">
                {label}
              </span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
