import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { usePatientProfile } from '../../hooks/usePatientProfile';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Edit3, User, Info, PhoneCall } from 'lucide-react';

/**
 * Profile view page displaying patient specifics and emergency contact details.
 */
export default function PatientProfile() {
  const navigate = useNavigate();
  const { profile, isLoading, error, refetch } = usePatientProfile();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  // If profile is not created yet, prompt setup CTA
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed border-gray-200">
          <div className="p-4 bg-red-50 text-primary rounded-full border border-red-100">
            <User className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">Setup your Patient Profile</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              You haven't completed your patient profile setup. Set up your profile to request blood and track matches.
            </p>
          </div>
          <Link to="/patient/profile/edit">
            <Button variant="primary" className="px-6 py-2.5">Create Profile Now</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 max-w-3xl mx-auto">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Patient Profile</h1>
          <p className="text-xs text-gray-500 mt-1">
            Review and manage your basic details, contact logs, and history.
          </p>
        </div>
        <Link to="/patient/profile/edit">
          <Button variant="outline" className="flex items-center gap-2 text-xs py-2">
            <Edit3 className="h-4 w-4" /> Edit Profile
          </Button>
        </Link>
      </div>

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
        </div>

        {/* Details Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Age</span>
            <span className="text-sm font-semibold text-gray-800">{profile.age} years</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Gender</span>
            <span className="text-sm font-semibold text-gray-800 capitalize">
              {profile.gender ? profile.gender.toLowerCase() : 'N/A'}
            </span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Address</span>
            <span className="text-sm font-semibold text-gray-800">{profile.address || 'N/A'}</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">City</span>
            <span className="text-sm font-semibold text-gray-800">{profile.city}</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">State</span>
            <span className="text-sm font-semibold text-gray-800">{profile.state}</span>
          </div>
        </div>

        {/* Emergency Contact details panel */}
        <div className="bg-red-50/40 p-4 rounded-xl border border-red-100/50 flex items-start gap-3">
          <PhoneCall className="h-4 w-4 text-primary shrink-0 mt-0.5" />
          <div className="flex flex-col gap-1">
            <span className="text-xs font-semibold text-gray-700">Emergency Contact Person</span>
            <p className="text-xs text-gray-600 font-bold mt-0.5">
              {profile.emergencyContactName} • {profile.emergencyContactNumber}
            </p>
          </div>
        </div>

        {/* Medical History detail block */}
        <div className="bg-slate-50 p-4 rounded-xl border border-gray-100 flex items-start gap-3">
          <Info className="h-4 w-4 text-gray-400 shrink-0 mt-0.5" />
          <div className="flex flex-col gap-1">
            <span className="text-xs font-semibold text-gray-700">Medical History Summary</span>
            <p className="text-xs text-gray-500 leading-relaxed mt-0.5">
              {profile.medicalHistory || 'No medical history reported.'}
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}
