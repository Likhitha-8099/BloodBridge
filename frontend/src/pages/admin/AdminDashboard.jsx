import React from 'react';
import { useDashboardOverview, useBloodGroupAnalytics, useTopHospitals } from '../../hooks/useAdminStatistics';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import PieChartCard from '../../components/ui/PieChartCard';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Users, FileText, Heart, Activity, Bell, Shield, Award, User } from 'lucide-react';

/**
 * Main Admin Dashboard providing system overview metrics, blood group counts, and hospitals performance.
 */
export default function AdminDashboard() {
  const { data: overview, isLoading: isOverviewLoading, error: overviewError } = useDashboardOverview();
  const { data: bloodGroups, isLoading: isBgLoading } = useBloodGroupAnalytics();
  const { data: topHospitals, isLoading: isHospitalsLoading } = useTopHospitals();

  const isLoading = isOverviewLoading || isBgLoading || isHospitalsLoading;
  const error = overviewError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  const userStats = overview?.userStatistics || {};
  const requestStats = overview?.requestStatistics || {};
  const donationStats = overview?.donationStatistics || {};
  const matchingStats = overview?.matchingStatistics || {};
  const notificationStats = overview?.notificationStatistics || {};

  // Formulate pie slices for blood group distributions
  const bgData = bloodGroups ? [
    { name: 'A+', value: bloodGroups.aPositive || 0 },
    { name: 'A-', value: bloodGroups.aNegative || 0 },
    { name: 'B+', value: bloodGroups.bPositive || 0 },
    { name: 'B-', value: bloodGroups.bNegative || 0 },
    { name: 'AB+', value: bloodGroups.abPositive || 0 },
    { name: 'AB-', value: bloodGroups.abNegative || 0 },
    { name: 'O+', value: bloodGroups.oPositive || 0 },
    { name: 'O-', value: bloodGroups.oNegative || 0 },
  ].filter(entry => entry.value > 0) : [];

  const hospitalColumns = [
    {
      header: 'Hospital Name',
      field: 'hospitalName',
    },
    {
      header: 'Total Requests',
      field: 'totalRequests',
    },
    {
      header: 'Completed Donations',
      field: 'completedDonations',
    },
    {
      header: 'Success Rate (%)',
      render: (row) => (
        <span className="font-extrabold text-blue-600">
          {row.successRate != null ? `${row.successRate}%` : '0%'}
        </span>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard Overview</h1>
        <p className="text-xs text-gray-500 mt-1">
          Real-time summaries of system operations, blood compatibility metrics, and health diagnostics.
        </p>
      </div>

      {/* Stats Cards Section */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Users"
          value={userStats.totalUsers}
          icon={Users}
          iconColor="text-blue-500 bg-blue-50"
        />

        <StatCard
          title="Total Donors"
          value={userStats.totalDonors}
          icon={Heart}
          iconColor="text-red-500 bg-red-50"
        />

        <StatCard
          title="Total Patients"
          value={userStats.totalPatients}
          icon={User}
          iconColor="text-indigo-500 bg-indigo-50"
        />

        <StatCard
          title="Total Hospitals"
          value={userStats.totalHospitals}
          icon={Award}
          iconColor="text-teal-500 bg-teal-50"
        />

        <StatCard
          title="Total Requests"
          value={requestStats.totalRequests}
          icon={FileText}
          iconColor="text-orange-500 bg-orange-50"
        />

        <StatCard
          title="Total Donations"
          value={donationStats.totalDonations}
          icon={Activity}
          iconColor="text-green-500 bg-green-50"
        />

        <StatCard
          title="Total Matches"
          value={matchingStats.totalMatches}
          icon={Shield}
          iconColor="text-purple-500 bg-purple-50"
        />

        <StatCard
          title="Total Notifications"
          value={notificationStats.totalNotifications}
          icon={Bell}
          iconColor="text-slate-500 bg-slate-50"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Blood Groups Distribution Donut */}
        <div className="lg:col-span-1">
          {bgData.length > 0 ? (
            <PieChartCard title="Blood Group Distribution" data={bgData} />
          ) : (
            <Card className="h-full flex items-center justify-center p-8 text-center text-xs text-gray-400">
              No blood group records found.
            </Card>
          )}
        </div>

        {/* Top Hospitals Leaderboard */}
        <div className="lg:col-span-2">
          <Card className="h-full">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-50 pb-2.5 mb-4">
              Top Coordinating Hospitals
            </h3>
            {topHospitals && topHospitals.length > 0 ? (
              <DataTable
                columns={hospitalColumns}
                data={topHospitals}
                keyField="hospitalName"
              />
            ) : (
              <p className="text-xs text-gray-450 py-8 text-center">No hospital performance metrics recorded.</p>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
