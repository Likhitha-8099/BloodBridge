import React from 'react';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useHospitalRequests } from '../../hooks/useHospitalRequests';
import { useDonations } from '../../hooks/useDonations';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/ui/Button';
import { FileText, ShieldAlert, Heart, CheckSquare, ArrowRight, User } from 'lucide-react';

/**
 * Hospital Dashboard listing verification summaries, matched counts, and recent logs.
 */
export default function HospitalDashboard() {
  const navigate = useNavigate();
  const { profile, isLoading: isProfileLoading, error: profileError, refetch: refetchProfile } = useHospitalProfile();

  const hospitalId = profile?.id;

  const {
    requests,
    isLoading: isRequestsLoading,
    error: requestsError,
  } = useHospitalRequests();

  const {
    donations,
    isLoading: isDonationsLoading,
    error: donationsError,
  } = useDonations(hospitalId);

  const isLoading = isProfileLoading || isRequestsLoading || isDonationsLoading;
  const error = profileError || requestsError || donationsError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetchProfile} />;
  }

  // Profile setup check
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed rounded-3xl">
          <div className="p-4 bg-red-50 text-primary rounded-full border border-red-100">
            <User className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">Setup your Hospital Profile</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              Configure your hospital profile to begin reviewing patient requests and logging matched transfusions.
            </p>
          </div>
          <Link to="/hospital/profile/edit">
            <Button variant="primary" className="px-6 py-2.5">Create Profile Now</Button>
          </Link>
        </Card>
      </div>
    );
  }

  // Filter requests assigned to this hospital
  const myRequests = (requests || []).filter(req => req.hospitalName === profile.hospitalName);

  // Compute metrics
  const totalRequests = myRequests.length;
  const pendingRequests = myRequests.filter(r => r.status === 'PENDING').length;
  const activeMatches = myRequests.filter(r => ['VERIFIED', 'MATCHED'].includes(r.status)).length;
  const completedDonations = donations ? donations.filter(d => d.status === 'COMPLETED').length : 0;

  const recentRequests = myRequests.slice(0, 3);
  const recentDonations = donations ? donations.slice(0, 3) : [];

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Hospital Dashboard</h1>
          <p className="text-xs text-gray-500 mt-1">
            Welcome, {profile.hospitalName}! Verify requests, check compatibility and track appointments.
          </p>
        </div>
      </div>

      {/* Stats Cards Section */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Requests"
          value={totalRequests}
          icon={FileText}
          iconColor="text-blue-500 bg-blue-50"
        />

        <StatCard
          title="Pending Verification"
          value={pendingRequests}
          icon={ShieldAlert}
          iconColor="text-yellow-500 bg-yellow-50"
        />

        <StatCard
          title="Active Matches"
          value={activeMatches}
          icon={Heart}
          iconColor="text-indigo-500 bg-indigo-50"
        />

        <StatCard
          title="Completed Donations"
          value={completedDonations}
          icon={CheckSquare}
          iconColor="text-green-500 bg-green-50"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Recent Requests Section */}
        <Card>
          <div className="flex items-center justify-between border-b border-gray-100 pb-4 mb-4">
            <h3 className="font-bold text-gray-800 text-sm">Recent Blood Requests</h3>
            <Link to="/hospital/requests" className="text-xs font-semibold text-primary hover:underline">
              View All
            </Link>
          </div>

          {recentRequests.length > 0 ? (
            <div className="flex flex-col divide-y divide-gray-50">
              {recentRequests.map((item) => (
                <div key={item.id} className="py-3 flex items-center justify-between gap-2 flex-wrap text-xs">
                  <div className="flex flex-col gap-0.5">
                    <h4 className="font-bold text-gray-700">Request #{item.id} - Patient: {item.patientName}</h4>
                    <span className="text-gray-450">
                      Needed Group: {item.bloodGroupNeeded?.replace('_POSITIVE', '+').replace('_NEGATIVE', '-')} • {item.unitsRequired} Bags
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <StatusBadge status={item.status} />
                    <button
                      onClick={() => navigate(`/hospital/requests/${item.id}`)}
                      className="p-1.5 hover:bg-slate-100 rounded-lg text-slate-400 hover:text-slate-900 transition-all"
                    >
                      <ArrowRight className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-gray-400 text-center py-6">No requests assigned to your hospital.</p>
          )}
        </Card>

        {/* Recent Donations Section */}
        <Card>
          <div className="flex items-center justify-between border-b border-gray-100 pb-4 mb-4">
            <h3 className="font-bold text-gray-800 text-sm">Recent Donations</h3>
            <Link to="/hospital/donations" className="text-xs font-semibold text-primary hover:underline">
              View All
            </Link>
          </div>

          {recentDonations.length > 0 ? (
            <div className="flex flex-col divide-y divide-gray-50">
              {recentDonations.map((item) => (
                <div key={item.id} className="py-3 flex items-center justify-between gap-2 flex-wrap text-xs">
                  <div className="flex flex-col gap-0.5">
                    <h4 className="font-bold text-gray-700">Donor: {item.donorName} → {item.patientName}</h4>
                    <span className="text-gray-400">Scheduled for {formatDate(item.donationDate)}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-55 border uppercase text-slate-700">
                      {item.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-gray-400 text-center py-6">No matched donations logged yet.</p>
          )}
        </Card>
      </div>
    </div>
  );
}
