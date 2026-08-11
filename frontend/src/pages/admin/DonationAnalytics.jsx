import React from 'react';
import { useDonationStatistics, useMonthlyDonations } from '../../hooks/useAdminStatistics';
import StatCard from '../../components/ui/StatCard';
import LineChartCard from '../../components/ui/LineChartCard';
import BarChartCard from '../../components/ui/BarChartCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Activity, CheckSquare, Clock, XSquare } from 'lucide-react';

/**
 * Screen showing donation completion statistics, trends, and group fill distributions.
 */
export default function DonationAnalytics() {
  const { data: stats, isLoading: isStatsLoading, error: statsError } = useDonationStatistics();
  const { data: trends, isLoading: isTrendsLoading } = useMonthlyDonations();

  const isLoading = isStatsLoading || isTrendsLoading;
  const error = statsError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  // Format monthly trends
  const trendData = trends ? trends.map(t => ({
    month: t.month,
    Donations: t.count,
  })) : [];

  // Format blood group metrics from Map
  const bgData = stats?.donationsByBloodGroup ? Object.entries(stats.donationsByBloodGroup).map(([group, count]) => ({
    group: group.replace('_POSITIVE', '+').replace('_NEGATIVE', '-'),
    Donations: count,
  })) : [];

  return (
    <div className="flex flex-col gap-6 font-sans">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Donation Tracking & Analytics</h1>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Check donation completion rates, active schedule slots, and monthly fulfillment trends.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Donations"
          value={stats?.totalDonations}
          icon={Activity}
          iconColor="text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/60"
        />
        <StatCard
          title="Completed Donations"
          value={stats?.completedDonations}
          icon={CheckSquare}
          iconColor="text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60"
        />
        <StatCard
          title="Pending Appointments"
          value={stats?.pendingDonations}
          icon={Clock}
          iconColor="text-yellow-600 dark:text-yellow-400 bg-yellow-50 dark:bg-yellow-950/60"
        />
        <StatCard
          title="Cancelled Runs"
          value={stats?.cancelledDonations}
          icon={XSquare}
          iconColor="text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/60"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {trendData.length > 0 ? (
          <LineChartCard 
            title="12-Month Donation Trend" 
            data={trendData} 
            xKey="month" 
            yKey="Donations" 
            name="Donations Logged" 
            color="#10B981" 
          />
        ) : (
          <div className="h-64 flex items-center justify-center border border-slate-100 dark:border-slate-800 rounded-3xl text-xs text-slate-400 dark:text-slate-500 bg-white dark:bg-slate-900">
            No trend data available.
          </div>
        )}

        {bgData.length > 0 ? (
          <BarChartCard 
            title="Fulfillments By Blood Group" 
            data={bgData} 
            xKey="group" 
            yKey="Donations" 
            name="Bags Filled" 
            color="#E11D48" 
          />
        ) : (
          <div className="h-64 flex items-center justify-center border border-slate-100 dark:border-slate-800 rounded-3xl text-xs text-slate-400 dark:text-slate-500 bg-white dark:bg-slate-900">
            No blood group metrics registered.
          </div>
        )}
      </div>
    </div>
  );
}
