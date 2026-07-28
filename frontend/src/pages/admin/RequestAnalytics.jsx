import React from 'react';
import { useRequestStatistics, useMonthlyRequests } from '../../hooks/useAdminStatistics';
import StatCard from '../../components/ui/StatCard';
import PieChartCard from '../../components/ui/PieChartCard';
import LineChartCard from '../../components/ui/LineChartCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { FileText, ShieldAlert, CheckCircle, Heart } from 'lucide-react';

/**
 * Screen showing request status distribution charts and monthly submission trends.
 */
export default function RequestAnalytics() {
  const { data: stats, isLoading: isStatsLoading, error: statsError } = useRequestStatistics();
  const { data: trends, isLoading: isTrendsLoading } = useMonthlyRequests();

  const isLoading = isStatsLoading || isTrendsLoading;
  const error = statsError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  const statusData = [
    { name: 'Pending', value: stats?.pendingRequests || 0 },
    { name: 'Verified', value: stats?.verifiedRequests || 0 },
    { name: 'Matched', value: stats?.matchedRequests || 0 },
    { name: 'Completed', value: stats?.completedRequests || 0 },
    { name: 'Rejected', value: stats?.rejectedRequests || 0 },
    { name: 'Cancelled', value: stats?.cancelledRequests || 0 },
  ].filter(s => s.value > 0);

  const trendData = trends ? trends.map(t => ({
    month: t.month,
    Requests: t.count,
  })) : [];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Blood Request Analytics</h1>
        <p className="text-xs text-gray-500 mt-1">
          Monitor the verification pipeline, cancellation ratios, and operations volume trends.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Requests"
          value={stats?.totalRequests}
          icon={FileText}
          iconColor="text-blue-500 bg-blue-50"
        />
        <StatCard
          title="Pending Review"
          value={stats?.pendingRequests}
          icon={ShieldAlert}
          iconColor="text-yellow-500 bg-yellow-50"
        />
        <StatCard
          title="Verified Requests"
          value={stats?.verifiedRequests}
          icon={CheckCircle}
          iconColor="text-indigo-500 bg-indigo-50"
        />
        <StatCard
          title="Completed Transfusions"
          value={stats?.completedRequests}
          icon={Heart}
          iconColor="text-green-500 bg-green-50"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          {statusData.length > 0 ? (
            <PieChartCard title="Request Status Distribution" data={statusData} />
          ) : (
            <div className="h-64 flex items-center justify-center border rounded-3xl text-xs text-gray-400 bg-white">
              No request status data recorded.
            </div>
          )}
        </div>

        <div className="lg:col-span-2">
          {trendData.length > 0 ? (
            <LineChartCard 
              title="12-Month Request Trend" 
              data={trendData} 
              xKey="month" 
              yKey="Requests" 
              name="Blood Requests Submitted" 
              color="#EC4899" 
            />
          ) : (
            <div className="h-64 flex items-center justify-center border rounded-3xl text-xs text-gray-400 bg-white">
              No trend data available.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
