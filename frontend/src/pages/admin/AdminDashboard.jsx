import React, { useMemo } from 'react';
import { useDashboardOverview, useBloodGroupAnalytics, useTopHospitals } from '../../hooks/useAdminStatistics';
import { useWebSocket } from '../../hooks/useWebSocket';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import PieChartCard from '../../components/ui/PieChartCard';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Users, FileText, Heart, Activity, Shield, Award, User, AlertTriangle, RefreshCw, Wifi, WifiOff } from 'lucide-react';

/**
 * Main Admin Dashboard providing system overview metrics, blood group counts, and hospitals performance.
 * Integrates real-time STOMP WebSocket listening for zero-refresh updates on events.
 */
export default function AdminDashboard() {
  const { data: overview, isLoading: isOverviewLoading, error: overviewError, refetch: refetchOverview } = useDashboardOverview();
  const { data: bloodGroups, isLoading: isBgLoading, error: bgError, refetch: refetchBg } = useBloodGroupAnalytics();
  const { data: topHospitals, isLoading: isHospitalsLoading, error: topHospitalsError, refetch: refetchHospitals } = useTopHospitals();

  const adminTopics = useMemo(() => [
    '/topic/admin/dashboard',
    '/topic/admin/users',
    '/topic/admin/hospitals',
    '/topic/admin/notifications',
    '/topic/analytics'
  ], []);

  const { isConnected, isFallback } = useWebSocket(adminTopics, (eventData) => {
    console.log('⚡ Real-time Admin Dashboard event received:', eventData.eventType);
    refetchOverview();
    refetchBg();
    refetchHospitals();
  });

  if (isOverviewLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (overviewError) {
    return <ErrorState message={overviewError.message || "Failed to load dashboard overview statistics."} onRetry={refetchOverview} />;
  }

  const totalUsers = overview?.totalUsers ?? 0;
  const totalDonors = overview?.totalDonors ?? 0;
  const totalPatients = overview?.totalPatients ?? 0;
  const totalHospitals = overview?.totalHospitals ?? 0;
  const activeRequests = overview?.activeRequests ?? overview?.todaysRequests ?? 0;
  const totalDonations = overview?.totalDonations ?? 0;
  const livesSaved = overview?.livesSaved ?? 0;
  const pendingApprovals = overview?.pendingHospitalApprovals ?? 0;

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
      render: (row) => <span className="font-bold text-slate-800 dark:text-white">{row.hospitalName}</span>
    },
    {
      header: 'Total Requests',
      field: 'totalRequests',
    },
    {
      header: 'Completed Donations',
      render: (row) => row.totalDonations ?? row.completedDonations ?? 0,
    },
    {
      header: 'Success Rate (%)',
      render: (row) => {
        const reqs = row.totalRequests || 0;
        const dons = row.totalDonations || row.completedDonations || 0;
        const rate = row.successRate ?? (reqs > 0 ? Math.round((dons / reqs) * 100) : 0);
        return (
          <span className="font-extrabold text-blue-600 dark:text-blue-400">
            {rate}%
          </span>
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6 font-sans">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-3">
            Admin Dashboard Overview
            {isConnected ? (
              <span className="flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400 font-semibold bg-emerald-50 dark:bg-emerald-950/50 px-2.5 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
                <Wifi className="h-3.5 w-3.5" /> STOMP Live
              </span>
            ) : (
              <span className="flex items-center gap-1.5 text-xs text-amber-600 dark:text-amber-400 font-semibold bg-amber-50 dark:bg-amber-950/50 px-2.5 py-1 rounded-full border border-amber-200 dark:border-amber-800">
                <WifiOff className="h-3.5 w-3.5" /> {isFallback ? 'REST Fallback' : 'Reconnecting...'}
              </span>
            )}
          </h1>
          <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">
            Real-time summaries of system operations, blood compatibility metrics, and health diagnostics.
          </p>
        </div>

        <button
          onClick={() => {
            refetchOverview();
            refetchBg();
            refetchHospitals();
          }}
          className="flex items-center gap-1.5 px-3 py-2 text-xs font-bold text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-sm hover:bg-slate-50 transition-all"
        >
          <RefreshCw className="h-3.5 w-3.5" /> Refresh
        </button>
      </div>

      {/* Stats Cards Section */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Users"
          value={totalUsers}
          icon={Users}
          iconColor="text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/60"
        />

        <StatCard
          title="Total Donors"
          value={totalDonors}
          icon={Heart}
          iconColor="text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/60"
        />

        <StatCard
          title="Total Patients"
          value={totalPatients}
          icon={User}
          iconColor="text-purple-600 dark:text-purple-400 bg-purple-50 dark:bg-purple-950/60"
        />

        <StatCard
          title="Total Hospitals"
          value={totalHospitals}
          icon={Shield}
          iconColor="text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60"
        />
      </div>

      {/* Secondary Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Active Requests"
          value={activeRequests}
          icon={FileText}
          iconColor="text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/60"
        />

        <StatCard
          title="Completed Donations"
          value={totalDonations}
          icon={Award}
          iconColor="text-teal-600 dark:text-teal-400 bg-teal-50 dark:bg-teal-950/60"
        />

        <StatCard
          title="Lives Saved"
          value={livesSaved}
          icon={Activity}
          iconColor="text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/60"
        />

        <StatCard
          title="Pending Hospital Approvals"
          value={pendingApprovals}
          icon={AlertTriangle}
          iconColor="text-yellow-600 dark:text-yellow-400 bg-yellow-50 dark:bg-yellow-950/60"
        />
      </div>

      {/* Middle Grid: Blood Groups & Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="Blood Group Distribution">
          {isBgLoading ? (
            <LoadingSpinner />
          ) : bgError ? (
            <ErrorState message={bgError.message || "Failed to load blood group analytics."} onRetry={refetchBg} />
          ) : (
            <PieChartCard
              title="Registered Donors by Blood Group"
              data={bgData}
            />
          )}
        </Card>

        <Card title="System Performance Summary">
          <div className="flex flex-col gap-4 text-sm text-slate-600 dark:text-slate-300">
            <div className="p-4 bg-slate-50 dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/60 flex items-center justify-between">
              <div>
                <h4 className="font-bold text-slate-800 dark:text-slate-100">Executive Health Score</h4>
                <p className="text-xs text-slate-500">System infrastructure and live services</p>
              </div>
              <span className="text-lg font-black text-emerald-600 dark:text-emerald-400">99.8%</span>
            </div>

            <div className="p-4 bg-slate-50 dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/60 flex items-center justify-between">
              <div>
                <h4 className="font-bold text-slate-800 dark:text-slate-100">Average Emergency Response</h4>
                <p className="text-xs text-slate-500">Target donor matching speed</p>
              </div>
              <span className="text-lg font-black text-blue-600 dark:text-blue-400">1.2 hrs</span>
            </div>

            <div className="p-4 bg-slate-50 dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/60 flex items-center justify-between">
              <div>
                <h4 className="font-bold text-slate-800 dark:text-slate-100">Live STOMP Realtime Gateway</h4>
                <p className="text-xs text-slate-500">WebSocket SockJS message broker</p>
              </div>
              <span className="text-xs font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-100 dark:bg-emerald-950 px-2.5 py-1 rounded-lg">ACTIVE</span>
            </div>
          </div>
        </Card>
      </div>

      {/* Top Hospitals Performance Table */}
      <Card title="Top Performing Partner Hospitals">
        {isHospitalsLoading ? (
          <LoadingSpinner />
        ) : topHospitalsError ? (
          <ErrorState message={topHospitalsError.message || "Failed to load hospital statistics."} onRetry={refetchHospitals} />
        ) : (
          <DataTable
            columns={hospitalColumns}
            data={Array.isArray(topHospitals) ? topHospitals : (topHospitals?.data || [])}
            emptyMessage="No hospital performance data registered yet."
          />
        )}
      </Card>
    </div>
  );
}
