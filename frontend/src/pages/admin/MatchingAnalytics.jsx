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
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Compatibility Matching Engine Analytics</h1>
        <p className="text-xs text-gray-500 mt-1">
          Audit the matching algorithm performance, dispatch rates, and accept ratios.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-6">
        <StatCard
          title="Total Matches Run"
          value={stats?.totalMatches}
          icon={Activity}
          iconColor="text-blue-500 bg-blue-50"
        />
        <StatCard
          title="Accepted Matches"
          value={stats?.acceptedMatches}
          icon={CheckCircle}
          iconColor="text-green-500 bg-green-50"
        />
        <StatCard
          title="Rejected Matches"
          value={stats?.rejectedMatches}
          icon={ShieldAlert}
          iconColor="text-red-500 bg-red-50"
        />
        <StatCard
          title="Active Matches"
          value={stats?.activeMatches}
          icon={ShieldCheck}
          iconColor="text-indigo-500 bg-indigo-50"
        />
        <StatCard
          title="Matching Success Rate"
          value={`${successRate}%`}
          icon={Activity}
          iconColor="text-teal-500 bg-teal-50"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="md:col-span-1">
          {matchStatusData.length > 0 ? (
            <PieChartCard title="Matching Status Results" data={matchStatusData} />
          ) : (
            <div className="h-64 flex items-center justify-center border rounded-3xl text-xs text-gray-405 bg-white">
              No matching run status data recorded.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
