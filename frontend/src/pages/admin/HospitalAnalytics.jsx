import React from 'react';
import { useTopHospitals } from '../../hooks/useAdminStatistics';
import DataTable from '../../components/ui/DataTable';
import Card from '../../components/ui/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import { Award, Hospital } from 'lucide-react';

/**
 * Screen showing hospital performance metrics, requests, donations, and success ratios.
 */
export default function HospitalAnalytics() {
  const { data: topHospitals, isLoading, error, refetch } = useTopHospitals();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const columns = [
    {
      header: 'Hospital Name',
      field: 'hospitalName',
    },
    {
      header: 'Requests',
      field: 'totalRequests',
    },
    {
      header: 'Donations',
      field: 'totalDonations',
    },
    {
      header: 'Success Rate',
      render: (row) => {
        const successRate = row.totalRequests > 0 
          ? Math.round((row.totalDonations / row.totalRequests) * 100) 
          : 0;
        return (
          <span className="font-extrabold text-blue-600">
            {successRate}%
          </span>
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Hospital Operations Analytics</h1>
        <p className="text-xs text-gray-500 mt-1">
          Detailed leaderboard comparing requests, completed donations, and fulfillment success ratios.
        </p>
      </div>

      <Card>
        <h3 className="font-bold text-gray-800 text-sm border-b border-gray-50 pb-2.5 mb-4 flex items-center gap-2">
          <Award className="h-5 w-5 text-primary" /> Hospital Performance Leaderboard
        </h3>

        {topHospitals && topHospitals.length > 0 ? (
          <DataTable
            columns={columns}
            data={topHospitals}
            keyField="hospitalName"
          />
        ) : (
          <EmptyState
            message="No hospital performance metrics recorded in the database."
            icon={Hospital}
          />
        )}
      </Card>
    </div>
  );
}
