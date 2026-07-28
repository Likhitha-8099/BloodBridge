import React from 'react';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import { useDonationHistory } from '../../hooks/useDonationHistory';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Link } from 'react-router-dom';
import Button from '../../components/ui/Button';
import { Heart, Activity, Calendar, ShieldCheck, ShieldAlert, ArrowRight, User } from 'lucide-react';

/**
 * Donor dashboard displaying eligibility, donation metrics, and historical summaries.
 */
export default function DonorDashboard() {
  const { 
    profile, 
    isLoading: isProfileLoading, 
    error: profileError, 
    refetch: refetchProfile 
  } = useDonorProfile();
  
  const donorId = profile?.id;
  const { 
    data: donations, 
    isLoading: isHistoryLoading, 
    error: historyError 
  } = useDonationHistory(donorId);

  const isLoading = isProfileLoading || isHistoryLoading;
  const error = profileError || historyError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetchProfile} />;
  }

  // Profile not initialized yet
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
              Complete your donor profile configuration to see active match lists and view donation tracking panels.
            </p>
          </div>
          <Link to="/donor/profile/edit">
            <Button variant="primary" className="px-6 py-2.5">Get Started</Button>
          </Link>
        </Card>
      </div>
    );
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  const recentItems = donations ? donations.slice(0, 3) : [];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Donor Dashboard</h1>
        <p className="text-xs text-gray-500 mt-1">
          Welcome back, {profile.fullName}! Monitor your eligibility and compatibility matches.
        </p>
      </div>

      {/* Stats Cards Section */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Donations"
          value={profile.totalDonations || 0}
          icon={Heart}
          iconColor="text-red-500 bg-red-50"
        />
        
        <StatCard
          title="Availability"
          value={profile.availableForDonation ? 'Available' : 'Unavailable'}
          icon={Activity}
          iconColor={profile.availableForDonation ? 'text-green-500 bg-green-50' : 'text-slate-400 bg-slate-50'}
        />

        <StatCard
          title="Blood Group"
          value={profile.bloodGroup ? profile.bloodGroup.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : 'N/A'}
          icon={Heart}
          iconColor="text-primary bg-red-50"
        />

        <StatCard
          title="Last Donation"
          value={profile.lastDonationDate ? formatDate(profile.lastDonationDate) : 'Never'}
          icon={Calendar}
          iconColor="text-blue-500 bg-blue-50"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Eligibility Check Banner */}
        <Card className={`md:col-span-2 border flex flex-col sm:flex-row items-center gap-4 ${
          profile.eligible 
            ? 'border-green-150 bg-green-50/10 text-green-800' 
            : 'border-red-150 bg-red-50/10 text-red-800'
        }`}>
          <div className={`p-3 rounded-full shrink-0 ${profile.eligible ? 'bg-green-100/50 text-green-600' : 'bg-red-100/50 text-red-600'}`}>
            {profile.eligible ? <ShieldCheck className="h-6 w-6" /> : <ShieldAlert className="h-6 w-6" />}
          </div>
          <div className="flex flex-col gap-1 text-center sm:text-left">
            <h4 className="text-sm font-bold text-gray-900">
              {profile.eligible ? 'Eligibility Status: Cleared' : 'Eligibility Status: Waiting Period'}
            </h4>
            <p className="text-xs text-gray-500 leading-relaxed mt-0.5">
              {profile.eligible 
                ? 'Excellent! You are active and eligible to donate. Check active requests to find a match.' 
                : `To ensure donor wellness, wait at least 56 days between donation logs. Your next eligible date is ${formatDate(profile.nextEligibleDate)}.`}
            </p>
          </div>
        </Card>

        {/* Quick Links Card */}
        <Card className="flex flex-col gap-3 justify-center">
          <h4 className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Quick Actions</h4>
          <div className="flex flex-col gap-2">
            <Link to="/donor/requests" className="text-xs font-semibold text-primary hover:underline flex items-center gap-1">
              Browse Active Requests <ArrowRight className="h-3 w-3" />
            </Link>
            <Link to="/donor/profile" className="text-xs font-semibold text-slate-650 hover:underline flex items-center gap-1">
              View Profile Settings <ArrowRight className="h-3 w-3" />
            </Link>
          </div>
        </Card>
      </div>

      {/* Recent activity summary list */}
      <Card>
        <div className="flex items-center justify-between border-b border-gray-100 pb-4 mb-4">
          <h3 className="font-bold text-gray-800">Recent Donation Activity</h3>
          <Link to="/donor/history" className="text-xs font-semibold text-primary hover:underline">
            View All History
          </Link>
        </div>

        {recentItems.length > 0 ? (
          <div className="flex flex-col divide-y divide-gray-50">
            {recentItems.map((item) => (
              <div key={item.id} className="py-3.5 flex items-center justify-between flex-wrap gap-2">
                <div>
                  <h4 className="text-sm font-bold text-gray-700">{item.hospitalName}</h4>
                  <span className="text-[10px] text-gray-400">{formatDate(item.donationDate)}</span>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-xs font-semibold text-gray-500">
                    {item.unitsDonated ? `${item.unitsDonated} Units` : 'Scheduled'}
                  </span>
                  <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                    item.status === 'COMPLETED' 
                      ? 'bg-green-50 text-green-700 border-green-100' 
                      : 'bg-yellow-50 text-yellow-700 border-yellow-100'
                  }`}>
                    {item.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-xs text-gray-450 py-4 text-center">No recent donation transactions logged yet.</p>
        )}
      </Card>
    </div>
  );
}
