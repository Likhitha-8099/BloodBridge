import React from 'react';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import { useDonationHistory } from '../../hooks/useDonationHistory';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { Link } from 'react-router-dom';
import { ClipboardList } from 'lucide-react';

/**
 * Page displaying the authenticated donor's historical donation cycles.
 */
export default function DonationHistory() {
  const { 
    profile, 
    isLoading: isProfileLoading, 
    error: profileError 
  } = useDonorProfile();

  const donorId = profile?.id;
  const { 
    data, 
    isLoading: isHistoryLoading, 
    error: historyError, 
    refetch 
  } = useDonationHistory(donorId);

  const isLoading = isProfileLoading || isHistoryLoading;
  const error = profileError || historyError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed border-gray-200">
          <div className="p-4 bg-red-50 text-primary rounded-full border border-red-100">
            <ClipboardList className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">No Profile Setup</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              Create a donor profile to track donation histories and manage compatibility schedules.
            </p>
          </div>
          <Link to="/donor/profile/edit">
            <Button variant="primary" className="px-6 py-2.5">Create Profile Now</Button>
          </Link>
        </Card>
      </div>
    );
  }

  const columns = [
    {
      header: 'Donation Date',
      render: (row) => (
        <span className="font-medium text-gray-850">
          {row.donationDate 
            ? new Date(row.donationDate).toLocaleDateString(undefined, { 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
              }) 
            : 'Scheduled'}
        </span>
      ),
    },
    {
      header: 'Hospital',
      field: 'hospitalName',
    },
    {
      header: 'Units Donated',
      render: (row) => (
        <span className="font-semibold text-gray-700">
          {row.unitsDonated !== null && row.unitsDonated !== undefined ? `${row.unitsDonated} Units` : 'TBD'}
        </span>
      ),
    },
    {
      header: 'Status',
      render: (row) => {
        const statusClasses = {
          PENDING: 'bg-yellow-50 text-yellow-700 border-yellow-100',
          ACCEPTED: 'bg-blue-50 text-blue-700 border-blue-100',
          CONFIRMED: 'bg-indigo-50 text-indigo-700 border-indigo-100',
          COMPLETED: 'bg-green-50 text-green-700 border-green-100',
          CANCELLED: 'bg-gray-100 text-gray-600 border-gray-200',
          REJECTED: 'bg-red-50 text-red-700 border-red-100',
        };
        const style = statusClasses[row.status] || 'bg-gray-100 text-gray-600 border-gray-200';
        return (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border ${style}`}>
            {row.status}
          </span>
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Donation History</h1>
        <p className="text-xs text-gray-500 mt-1">
          Track and monitor your historical life-saving donations.
        </p>
      </div>

      {data && data.length > 0 ? (
        <DataTable
          columns={columns}
          data={data}
          keyField="id"
          emptyMessage="No donation records found."
        />
      ) : (
        <EmptyState
          message="You haven't completed any donation cycles yet."
          icon={ClipboardList}
        />
      )}
    </div>
  );
}
