import React from 'react';
import { useMatchingStatistics } from '../../hooks/useAdminStatistics';
import StatCard from '../../components/ui/StatCard';
import PieChartCard from '../../components/ui/PieChartCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Activity, ShieldAlert, CheckCircle, ShieldCheck } from 'lucide-react';

/**
 * Screen showing compatibility matching engine statistics, dispatch rates, and accept distributions.
 */
export default function MatchingAnalytics() {
  const { data: stats, isLoading, error } = useMatchingStatistics();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  const successRate = stats?.matchingSuccessRate != null ? stats.matchingSuccessRate : 0;

  const matchStatusData = [
    { name: 'Accepted', value: stats?.acceptedMatches || 0 },
    { name: 'Rejected', value: stats?.rejectedMatches || 0 },
    { name: 'Active', value: stats?.activeMatches || 0 },
  ].filter(s => s.value > 0);

  return (
    <div className="flex flex-col gap-6 font-sans">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Compatibility Matching Engine Analytics</h1>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Audit the matching algorithm performance, dispatch rates, and accept ratios.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-6">
        <StatCard
          title="Total Matches Run"
          value={stats?.totalMatches}
          icon={Activity}
          iconColor="text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/60"
        />
        <StatCard
          title="Accepted Matches"
          value={stats?.acceptedMatches}
          icon={CheckCircle}
          iconColor="text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60"
        />
        <StatCard
          title="Rejected Matches"
          value={stats?.rejectedMatches}
          icon={ShieldAlert}
          iconColor="text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/60"
        />
        <StatCard
          title="Active Matches"
          value={stats?.activeMatches}
          icon={ShieldCheck}
          iconColor="text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950/60"
        />
        <StatCard
          title="Matching Success Rate"
          value={`${successRate}%`}
          icon={Activity}
          iconColor="text-teal-600 dark:text-teal-400 bg-teal-50 dark:bg-teal-950/60"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="md:col-span-1">
          {matchStatusData.length > 0 ? (
            <PieChartCard title="Matching Status Results" data={matchStatusData} />
          ) : (
            <div className="h-64 flex items-center justify-center border border-slate-100 dark:border-slate-800 rounded-3xl text-xs text-slate-400 dark:text-slate-500 bg-white dark:bg-slate-900">
              No matching run status data recorded.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
