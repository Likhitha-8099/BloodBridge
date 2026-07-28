import React from 'react';
import { usePatientProfile } from '../../hooks/usePatientProfile';
import { useMyRequests } from '../../hooks/useMyRequests';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/ui/Button';
import { FileText, PlusCircle, AlertCircle, Eye, Activity, Heart, Clock, User } from 'lucide-react';

/**
 * Patient dashboard showing request metrics, recent activity lists, and guides.
 */
export default function PatientDashboard() {
  const navigate = useNavigate();
  const { 
    profile, 
    isLoading: isProfileLoading, 
    error: profileError, 
    refetch: refetchProfile 
  } = usePatientProfile();
  
  const { 
    requests, 
    isLoading: isRequestsLoading, 
    error: requestsError 
  } = useMyRequests();

  const isLoading = isProfileLoading || isRequestsLoading;
  const error = profileError || requestsError;

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
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed border-gray-200">
          <div className="p-4 bg-red-50 text-primary rounded-full border border-red-100">
            <User className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">Setup your Patient Profile</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              Complete your patient profile setup to start submitting blood requests and tracking compatibility matches.
            </p>
          </div>
          <Link to="/patient/profile/edit">
            <Button variant="primary" className="px-6 py-2.5">Get Started</Button>
          </Link>
        </Card>
      </div>
    );
  }

  // Summarize count analytics
  const total = requests ? requests.length : 0;
  const active = requests ? requests.filter(r => ['PENDING', 'VERIFIED', 'MATCHED'].includes(r.status)).length : 0;
  const completed = requests ? requests.filter(r => r.status === 'COMPLETED').length : 0;
  const pending = requests ? requests.filter(r => r.status === 'PENDING').length : 0;

  const recentItems = requests ? requests.slice(0, 3) : [];

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
          <h1 className="text-2xl font-bold text-gray-900">Patient Dashboard</h1>
          <p className="text-xs text-gray-500 mt-1">
            Welcome back, {profile.fullName}! Request blood and track matches here.
          </p>
        </div>
        <Link to="/patient/create-request">
          <Button variant="primary" className="flex items-center gap-2 text-xs py-2">
            <PlusCircle className="h-4 w-4" /> Create Request
          </Button>
        </Link>
      </div>

      {/* Stats Cards Section */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Requests"
          value={total}
          icon={FileText}
          iconColor="text-blue-500 bg-blue-50"
        />

        <StatCard
          title="Active Requests"
          value={active}
          icon={Activity}
          iconColor="text-indigo-500 bg-indigo-50"
        />

        <StatCard
          title="Completed Requests"
          value={completed}
          icon={Heart}
          iconColor="text-green-500 bg-green-50"
        />

        <StatCard
          title="Pending Requests"
          value={pending}
          icon={Clock}
          iconColor="text-yellow-500 bg-yellow-50"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Recent Requests Section */}
        <Card className="md:col-span-2">
          <div className="flex items-center justify-between border-b border-gray-100 pb-4 mb-4">
            <h3 className="font-bold text-gray-800 text-sm">Recent Requests</h3>
            <Link to="/patient/requests" className="text-xs font-semibold text-primary hover:underline">
              View All
            </Link>
          </div>

          {recentItems.length > 0 ? (
            <div className="flex flex-col divide-y divide-gray-50">
              {recentItems.map((item) => (
                <div key={item.id} className="py-3.5 flex items-center justify-between flex-wrap gap-2">
                  <div className="flex flex-col gap-0.5">
                    <h4 className="text-sm font-bold text-gray-700">
                      Request #{item.id} - Group {item.bloodGroupNeeded?.replace('_POSITIVE', '+').replace('_NEGATIVE', '-')}
                    </h4>
                    <span className="text-[10px] text-gray-400">
                      {item.hospitalName} • Required by {formatDate(item.requiredByDate)}
                    </span>
                  </div>
                  <div className="flex items-center gap-3">
                    <StatusBadge status={item.status} />
                    <button
                      onClick={() => navigate(`/patient/requests/${item.id}`)}
                      className="p-1.5 hover:bg-slate-100 rounded-lg text-slate-500 hover:text-slate-900 transition-all"
                    >
                      <Eye className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-gray-450 text-center py-6">
              You have no active or completed blood requests yet.
            </p>
          )}
        </Card>

        {/* Informative Help Guide Card */}
        <Card>
          <div className="flex items-center justify-between border-b border-gray-100 pb-4 mb-4">
            <h3 className="font-bold text-gray-800 text-sm">System Status Alerts</h3>
          </div>
          <div className="flex flex-col gap-3.5 py-2 text-xs leading-relaxed text-slate-500">
            <div className="flex items-start gap-2">
              <AlertCircle className="h-4 w-4 text-primary shrink-0 mt-0.5" />
              <span>Make sure to select verified hospitals located near you for faster donor matches.</span>
            </div>
            <div className="flex items-start gap-2">
              <AlertCircle className="h-4 w-4 text-primary shrink-0 mt-0.5" />
              <span>Transfusion matching runs are performed automatically upon hospital verification.</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
