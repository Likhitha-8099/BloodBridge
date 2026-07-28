import React from 'react';
import { useBloodRequests } from '../../hooks/useBloodRequests';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import { FileText } from 'lucide-react';

/**
 * Page displaying active patient blood requests matching system availability.
 */
export default function BloodRequests() {
  const { data, isLoading, error, refetch } = useBloodRequests();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const columns = [
    {
      header: 'Patient',
      render: (row) => (
        <div>
          <span className="font-semibold text-gray-800">{row.patientName || 'N/A'}</span>
          {row.patientCity && (
            <span className="block text-[10px] text-gray-400 mt-0.5">
              {row.patientCity}, {row.patientState}
            </span>
          )}
        </div>
      ),
    },
    {
      header: 'Blood Group',
      render: (row) => (
        <span className="inline-flex items-center justify-center font-extrabold px-2.5 py-1 text-xs rounded-full bg-red-50 text-primary border border-red-105">
          {row.bloodGroupNeeded ? row.bloodGroupNeeded.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : '?'}
        </span>
      ),
    },
    {
      header: 'Units Required',
      field: 'unitsRequired',
    },
    {
      header: 'Hospital',
      field: 'hospitalName',
    },
    {
      header: 'Urgency',
      render: (row) => {
        const urgencyClasses = {
          LOW: 'bg-slate-100 text-slate-700 border-slate-200',
          MEDIUM: 'bg-blue-50 text-blue-700 border-blue-100',
          HIGH: 'bg-orange-50 text-orange-700 border-orange-100',
          CRITICAL: 'bg-red-50 text-red-700 border-red-100 animate-pulse',
        };
        const style = urgencyClasses[row.urgencyLevel] || 'bg-gray-100 text-gray-600 border-gray-200';
        return (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border ${style}`}>
            {row.urgencyLevel}
          </span>
        );
      },
    },
    {
      header: 'Status',
      render: (row) => (
        <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
          {row.status ? row.status.toLowerCase() : 'Pending'}
        </span>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Active Blood Requests</h1>
        <p className="text-xs text-gray-500 mt-1">
          Review active request listings and requirements across network hospitals.
        </p>
      </div>

      {data && data.length > 0 ? (
        <DataTable
          columns={columns}
          data={data}
          keyField="id"
          emptyMessage="No active requests found."
        />
      ) : (
        <EmptyState
          message="No active blood requests are currently registered in the system."
          icon={FileText}
        />
      )}
    </div>
  );
}
