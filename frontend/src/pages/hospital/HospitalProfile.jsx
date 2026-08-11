import React from 'react';
import { Link } from 'react-router-dom';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import HospitalEmptyState from '../../components/hospital/common/HospitalEmptyState';
import { 
  Edit3, 
  Hospital, 
  ShieldCheck, 
  ShieldAlert, 
  Mail, 
  Phone, 
  MapPin, 
  Building
} from 'lucide-react';

/**
 * Hospital Profile View for Hospital Module.
 * Modern healthcare portal design preserving 100% of existing profile hooks and data models.
 */
export default function HospitalProfile() {
  const { profile, isLoading, error, refetch } = useHospitalProfile();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message || 'Failed to load hospital profile.'} onRetry={refetch} />;
  }

  if (!profile) {
    return (
      <div className="max-w-2xl mx-auto py-12 font-sans">
        <HospitalEmptyState
          title="Configure Hospital Profile"
          description="To begin reviewing patient blood requests and confirming donor matched donations, configure your hospital profile first."
          icon={Hospital}
          action={
            <Link to="/hospital/profile/edit">
              <button className="px-5 py-2.5 rounded-2xl bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs shadow-md transition-all">
                Create Profile Now
              </button>
            </Link>
          }
        />
      </div>
    );
  }

  const isVerified = profile.verified || profile.verificationStatus === 'APPROVED' || profile.verificationStatus === 'VERIFIED';

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans max-w-4xl mx-auto">
      <HospitalPageHeader
        title="Institutional Profile"
        subtitle="Review registration details, license verification status, and contact information."
        icon={Hospital}
        badge="Healthcare Institution"
        breadcrumbs={[{ label: 'Hospital Profile' }]}
        action={
          <Link to="/hospital/profile/edit">
            <button className="flex items-center gap-2 px-5 py-2.5 rounded-2xl bg-gradient-to-r from-teal-600 to-emerald-600 text-white font-bold text-xs shadow-lg shadow-teal-500/20 hover:shadow-teal-500/35 transition-all transform hover:-translate-y-0.5">
              <Edit3 className="h-4 w-4" />
              <span>Edit Profile</span>
            </button>
          </Link>
        }
      />

      <HospitalCard bodyClassName="p-6 sm:p-8 flex flex-col gap-8">
        {/* Main Entity Banner */}
        <div className="flex items-center justify-between gap-5 pb-6 border-b border-slate-100 dark:border-slate-800 flex-wrap">
          <div className="flex items-center gap-4">
            <div className="h-16 w-16 rounded-3xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center border border-teal-100 dark:border-teal-900/40 shadow-inner shrink-0">
              <Building className="h-8 w-8" />
            </div>

            <div className="flex flex-col gap-1">
              <h2 className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white">
                {profile.hospitalName || 'Hospital Name'}
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400 font-mono">
                Reg ID: #{profile.id || profile.registrationNumber || 'N/A'}
              </p>
            </div>
          </div>

          <div className="shrink-0">
            {isVerified ? (
              <span className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full text-xs font-bold bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                <ShieldCheck className="h-4 w-4 text-emerald-600 shrink-0" />
                <span>Status: APPROVED</span>
              </span>
            ) : (
              <span className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full text-xs font-bold bg-amber-50 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800">
                <ShieldAlert className="h-4 w-4 text-amber-600 shrink-0" />
                <span>Status: {profile.verificationStatus || 'PENDING'}</span>
              </span>
            )}
          </div>
        </div>

        {/* Section 1: Hospital Information */}
        <div className="flex flex-col gap-3">
          <h3 className="text-xs font-extrabold uppercase tracking-wider text-slate-400 dark:text-slate-500 flex items-center gap-1.5">
            <Building className="h-3.5 w-3.5 text-teal-500" /> Hospital Information
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Hospital Name</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">{profile.hospitalName || 'N/A'}</span>
            </div>
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">License Number</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white font-mono">{profile.licenseNumber || profile.registrationNumber || 'N/A'}</span>
            </div>
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Registration Number</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white font-mono">{profile.registrationNumber || `REG-${profile.id || '101'}`}</span>
            </div>
          </div>
        </div>

        {/* Section 2: Contact Information */}
        <div className="flex flex-col gap-3">
          <h3 className="text-xs font-extrabold uppercase tracking-wider text-slate-400 dark:text-slate-500 flex items-center gap-1.5">
            <Mail className="h-3.5 w-3.5 text-teal-500" /> Contact Information
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider flex items-center gap-1">
                <Mail className="h-3 w-3" /> Email Address
              </span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">{profile.email || 'N/A'}</span>
            </div>
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider flex items-center gap-1">
                <Phone className="h-3 w-3" /> Phone Number
              </span>
              <span className="text-sm font-bold text-slate-900 dark:text-white font-mono">{profile.phoneNumber || profile.phone || 'N/A'}</span>
            </div>
          </div>
        </div>

        {/* Section 3: Facility Location */}
        <div className="flex flex-col gap-3">
          <h3 className="text-xs font-extrabold uppercase tracking-wider text-slate-400 dark:text-slate-500 flex items-center gap-1.5">
            <MapPin className="h-3.5 w-3.5 text-teal-500" /> Location Details
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800 sm:col-span-3">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Street Address</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">{profile.address || 'N/A'}</span>
            </div>
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">City</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">{profile.city || 'N/A'}</span>
            </div>
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">State</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">{profile.state || 'N/A'}</span>
            </div>
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">GPS Coordinates</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white font-mono">
                {profile.latitude && profile.longitude ? `${profile.latitude}, ${profile.longitude}` : '12.9716, 77.5946 (Default)'}
              </span>
            </div>
          </div>
        </div>
      </HospitalCard>
    </div>
  );
}
