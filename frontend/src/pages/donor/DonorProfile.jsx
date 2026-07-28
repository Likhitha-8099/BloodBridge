import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import AvailabilityToggle from '../../components/ui/AvailabilityToggle';
import { Edit3, User, ShieldCheck, ShieldAlert, Info } from 'lucide-react';

/**
 * Profile view page displaying donor specifics and eligibility indicators.
 */
export default function DonorProfile() {
  const navigate = useNavigate();
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

  // If profile does not exist yet, prompt creation CTA
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed border-gray-200">
          <div className="p-4 bg-red-50 text-primary rounded-full border border-red-100">
            <User className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">Setup your Donor Profile</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              You haven't completed your donor profile setup. Add your blood group and medical details to appear on compatibility search lists.
            </p>
          </div>
          <Link to="/donor/profile/edit">
            <Button variant="primary" className="px-6 py-2.5">Create Profile Now</Button>
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

  return (
    <div className="flex flex-col gap-6 max-w-3xl mx-auto">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Donor Profile</h1>
          <p className="text-xs text-gray-500 mt-1">
            Review and manage your medical details and donation statuses.
          </p>
        </div>
        <Link to="/donor/profile/edit">
          <Button variant="outline" className="flex items-center gap-2 text-xs py-2">
            <Edit3 className="h-4 w-4" /> Edit Profile
          </Button>
        </Link>
      </div>

      {/* Main Profile Info Card */}
      <Card className="flex flex-col gap-6">
        <div className="flex items-center gap-4 pb-6 border-b border-gray-100 flex-wrap">
          <div className="h-14 w-14 rounded-2xl bg-red-50 text-primary flex items-center justify-center font-extrabold text-xl shadow-inner border border-red-100">
            {profile.bloodGroup ? profile.bloodGroup.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : '?'}
          </div>
          <div>
            <h2 className="text-base font-bold text-gray-800">{profile.fullName}</h2>
            <p className="text-xs text-gray-400 mt-0.5">
              {profile.email} • {profile.phoneNumber || 'No phone contact info'}
            </p>
          </div>
          
          <div className="sm:ml-auto">
            <AvailabilityToggle
              isAvailable={profile.availableForDonation}
              onToggle={handleToggleAvailability}
              isLoading={isTogglingAvailability}
            />
          </div>
        </div>

        {/* Details Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Age</span>
            <span className="text-sm font-semibold text-gray-850">{profile.age} years</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Gender</span>
            <span className="text-sm font-semibold text-gray-855 capitalize">
              {profile.gender ? profile.gender.toLowerCase() : 'N/A'}
            </span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Weight</span>
            <span className="text-sm font-semibold text-gray-860">{profile.weight} kg</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">City</span>
            <span className="text-sm font-semibold text-gray-865">{profile.city}</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">State</span>
            <span className="text-sm font-semibold text-gray-870">{profile.state}</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Total Donations</span>
            <span className="text-sm font-semibold text-gray-875">{profile.totalDonations || 0} times</span>
          </div>

          <div className="flex flex-col gap-1 sm:col-span-2">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Last Donation Date</span>
            <span className="text-sm font-semibold text-gray-880">{formatDate(profile.lastDonationDate)}</span>
          </div>
        </div>

        {/* Medical Conditions Box */}
        <div className="bg-slate-50 p-4 rounded-xl border border-gray-100 flex items-start gap-3">
          <Info className="h-4 w-4 text-gray-400 shrink-0 mt-0.5" />
          <div className="flex flex-col gap-1">
            <span className="text-xs font-semibold text-gray-700">Medical Conditions & Allergies</span>
            <p className="text-xs text-gray-500 leading-relaxed mt-0.5">
              {profile.medicalConditions || 'No existing medical conditions or allergies declared.'}
            </p>
          </div>
        </div>
      </Card>

      {/* Eligibility Indicator Section */}
      <Card className={`border flex flex-col sm:flex-row items-center gap-4 ${
        profile.eligible 
          ? 'border-green-150 bg-green-50/10 text-green-800' 
          : 'border-red-150 bg-red-50/10 text-red-800'
      }`}>
        <div className={`p-3 rounded-full ${profile.eligible ? 'bg-green-100/50 text-green-600' : 'bg-red-100/50 text-red-600'}`}>
          {profile.eligible ? <ShieldCheck className="h-6 w-6" /> : <ShieldAlert className="h-6 w-6" />}
        </div>
        <div className="flex flex-col gap-1 max-w-md text-center sm:text-left">
          <h4 className="text-sm font-bold text-gray-900">
            {profile.eligible ? 'You are eligible to donate!' : 'You are currently not eligible to donate.'}
          </h4>
          <p className="text-xs text-gray-500 leading-relaxed">
            {profile.eligible 
              ? 'Excellent! You can accept blood match requests and donate immediately.' 
              : `To protect your health, you must wait at least 56 days between donations. You will be eligible on ${formatDate(profile.nextEligibleDate)}.`}
          </p>
        </div>
      </Card>
    </div>
  );
}
