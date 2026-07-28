import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Edit3, Hospital, ShieldCheck, ShieldAlert } from 'lucide-react';

/**
 * Screen displaying hospital configuration properties and administration review states.
 */
export default function HospitalProfile() {
  const navigate = useNavigate();
  const { profile, isLoading, error, refetch } = useHospitalProfile();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  // Profile not set up check
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed border-gray-200">
          <div className="p-4 bg-red-50 text-primary rounded-full border border-red-100">
            <Hospital className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">Setup your Hospital Profile</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              To begin reviewing patient blood requests and confirming donor matched donations, configure your hospital profile first.
            </p>
          </div>
          <Link to="/hospital/profile/edit">
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
          <h1 className="text-2xl font-bold text-gray-900">Hospital Profile</h1>
          <p className="text-xs text-gray-500 mt-1 flex items-center gap-1.5">
            Review your license verification, details, and addresses.
          </p>
        </div>
        <Link to="/hospital/profile/edit">
          <Button variant="outline" className="flex items-center gap-2 text-xs py-2">
            <Edit3 className="h-4 w-4" /> Edit Profile
          </Button>
        </Link>
      </div>

      <Card className="flex flex-col gap-6">
        <div className="flex items-center gap-4 pb-6 border-b border-gray-100 flex-wrap">
          <div className="h-14 w-14 rounded-2xl bg-red-50 text-primary flex items-center justify-center font-extrabold text-xl border border-red-100 shrink-0">
            <Hospital className="h-6 w-6" />
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h2 className="text-base font-bold text-gray-800">{profile.hospitalName}</h2>
              {profile.verified ? (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-green-50 text-green-700 border border-green-200">
                  <ShieldCheck className="h-3 w-3" /> Verified
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-yellow-50 text-yellow-700 border border-yellow-200">
                  <ShieldAlert className="h-3 w-3" /> Pending Verification
                </span>
              )}
            </div>
            <p className="text-xs text-gray-400 mt-0.5">Reg No: {profile.registrationNumber}</p>
          </div>
        </div>

        {/* Details Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Email Contact</span>
            <span className="text-sm font-semibold text-gray-800">{profile.email}</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Phone Number</span>
            <span className="text-sm font-semibold text-gray-800">{profile.phoneNumber}</span>
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Street Address</span>
            <span className="text-sm font-semibold text-gray-800">{profile.address}</span>
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
      </Card>
    </div>
  );
}
