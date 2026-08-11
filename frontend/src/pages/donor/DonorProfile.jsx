import React from 'react';
import { Link } from 'react-router-dom';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import AvailabilityToggle from '../../components/ui/AvailabilityToggle';
import AchievementCard from '../../components/donor/AchievementCard';
import { Edit3, User, ShieldCheck, Info, MapPin, Phone, Mail } from 'lucide-react';

/**
 * Modern Donor Profile View with Apollo 24/7 aesthetics & health metrics.
 */
export default function DonorProfile() {
  const { 
    profile, 
    isLoading, 
    error, 
    refetch, 
    toggleAvailability, 
    isTogglingAvailability 
  } = useDonorProfile();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const handleToggleAvailability = async (newVal) => {
    try {
      await toggleAvailability(newVal);
    } catch (err) {
      alert(err.message || 'Failed to update availability status.');
    }
  };

  // If profile does not exist yet
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-12">
        <Card className="flex flex-col items-center justify-center text-center p-10 gap-5 border border-dashed border-gray-200 rounded-3xl shadow-sm">
          <div className="p-4 bg-red-50 text-primary rounded-2xl border border-red-100">
            <User className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">Setup your Donor Profile</h2>
            <p className="text-xs text-gray-500 dark:text-slate-400 leading-relaxed">
              You haven't completed your donor profile setup. Add your blood group and medical details to appear on compatibility search lists.
            </p>
          </div>
          <Link to="/donor/profile/edit">
            <Button variant="primary" className="px-6 py-2.5 font-bold">Create Profile Now</Button>
          </Link>
        </Card>
      </div>
    );
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  };

  const formatBloodGroup = (bg) => {
    if (!bg) return '?';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto font-sans">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Donor Health Profile</h1>
          <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">
            Review your medical parameters, availability status, and achievement badges.
          </p>
        </div>
        <Link to="/donor/profile/edit">
          <Button variant="outline" size="sm" className="flex items-center gap-2">
            <Edit3 className="h-4 w-4" /> Edit Profile
          </Button>
        </Link>
      </div>

      {/* Main Profile Info Card */}
      <Card className="flex flex-col gap-6 p-6 sm:p-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 pb-6 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-4">
            <div className="h-16 w-16 rounded-3xl bg-red-50 text-red-600 dark:bg-red-950/60 dark:text-red-400 flex items-center justify-center font-black text-2xl shadow-inner border border-red-100 dark:border-red-900/30 shrink-0">
              {formatBloodGroup(profile.bloodGroup)}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-bold text-gray-900 dark:text-white">{profile.fullName}</h2>
                <Badge variant={profile.eligible ? 'success' : 'warning'}>
                  {profile.eligible ? 'Verified Eligible' : 'Deferred'}
                </Badge>
              </div>
              <div className="flex items-center gap-3 text-xs text-gray-500 dark:text-slate-400 mt-1 flex-wrap">
                <span className="flex items-center gap-1"><Mail className="h-3.5 w-3.5 text-primary" /> {profile.email}</span>
                <span className="flex items-center gap-1"><Phone className="h-3.5 w-3.5 text-primary" /> {profile.phoneNumber || 'N/A'}</span>
                <span className="flex items-center gap-1"><MapPin className="h-3.5 w-3.5 text-primary" /> {profile.city}, {profile.state}</span>
              </div>
            </div>
          </div>

          <div className="shrink-0 bg-slate-50 dark:bg-slate-800/60 p-3 rounded-2xl border border-slate-100 dark:border-slate-800">
            <AvailabilityToggle
              isAvailable={profile.availableForDonation}
              onToggle={handleToggleAvailability}
              isLoading={isTogglingAvailability}
            />
          </div>
        </div>

        {/* Details Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-6">
          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Age</span>
            <span className="text-sm font-bold text-gray-900 dark:text-white">{profile.age} years</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Gender</span>
            <span className="text-sm font-bold text-gray-900 dark:text-white capitalize">
              {profile.gender ? profile.gender.toLowerCase() : 'Male'}
            </span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Weight</span>
            <span className="text-sm font-bold text-gray-900 dark:text-white">{profile.weight} kg</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Total Donations</span>
            <span className="text-sm font-bold text-primary">{profile.totalDonations || 0} times</span>
          </div>
        </div>

        {/* Health & Donation Eligibility Section */}
        <div className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80 flex flex-col gap-4">
          <div className="flex items-center justify-between flex-wrap gap-2 pb-3 border-b border-slate-200 dark:border-slate-700">
            <div className="flex items-center gap-2">
              <ShieldCheck className={`h-5 w-5 ${profile.eligible || profile.eligibilityStatus === 'ELIGIBLE' ? 'text-emerald-500' : 'text-amber-500'}`} />
              <h3 className="font-extrabold text-sm text-gray-900 dark:text-white">
                Donation Status: {profile.eligible || profile.eligibilityStatus === 'ELIGIBLE' ? 'ELIGIBLE' : 'NOT ELIGIBLE'}
              </h3>
            </div>
            <Badge variant={profile.eligible || profile.eligibilityStatus === 'ELIGIBLE' ? 'success' : 'warning'}>
              {profile.eligible || profile.eligibilityStatus === 'ELIGIBLE' ? '🟢 Ready to Donate' : '🟡 Cooldown Period Active'}
            </Badge>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Blood Group</span>
              <span className="font-extrabold text-red-600 dark:text-red-400 text-sm">{formatBloodGroup(profile.bloodGroup)}</span>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Last Donation</span>
              <span className="font-semibold text-gray-900 dark:text-white">
                {profile.lastDonationDate ? formatDate(profile.lastDonationDate) : 'No donations yet'}
              </span>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Next Eligible Date</span>
              <span className="font-semibold text-gray-900 dark:text-white">
                {profile.eligible || profile.eligibilityStatus === 'ELIGIBLE' 
                  ? 'Eligible Today' 
                  : (profile.nextEligibleDate ? formatDate(profile.nextEligibleDate) : 'Pending')}
              </span>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Days Remaining</span>
              <span className="font-extrabold text-indigo-600 dark:text-indigo-400 text-sm">
                {profile.daysUntilEligible != null ? profile.daysUntilEligible : 0} days
              </span>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Total Donations</span>
              <span className="font-semibold text-gray-900 dark:text-white">{profile.totalDonations || 0}</span>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Available for Donation</span>
              <span className="font-semibold text-gray-900 dark:text-white">{profile.availableForDonation ? 'Yes' : 'No'}</span>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Emergency Available</span>
              <span className="font-semibold text-gray-900 dark:text-white">{profile.emergencyAvailable ? 'Yes' : 'No'}</span>
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-[10px] text-gray-400 font-bold uppercase">Cooldown Period</span>
              <span className="font-semibold text-gray-900 dark:text-white">{profile.cooldownDays || 90} days</span>
            </div>
          </div>
        </div>

        {/* Medical Conditions Box */}
        <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 flex items-start gap-3">
          <Info className="h-4 w-4 text-primary shrink-0 mt-0.5" />
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-gray-900 dark:text-white">Declared Medical Conditions</span>
            <p className="text-xs text-gray-500 dark:text-slate-400 leading-relaxed mt-0.5">
              {profile.medicalConditions || 'No existing medical conditions, chronic illnesses, or severe allergies declared.'}
            </p>
          </div>
        </div>
      </Card>

      {/* Achievement Card Component */}
      <AchievementCard donorScore={profile.donorScore} totalDonations={profile.totalDonations} livesSaved={profile.livesSaved} />
    </div>
  );
}
