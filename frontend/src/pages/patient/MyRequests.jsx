import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useMyRequests } from '../../hooks/useMyRequests';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import StatusBadge from '../../components/ui/StatusBadge';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import Button from '../../components/ui/Button';
import { FileText, PlusCircle, Eye, Ban, AlertCircle } from 'lucide-react';

/**
 * Page displaying lists of requests registered by the patient, supporting cancel options.
 */
export default function MyRequests() {
  const navigate = useNavigate();
  const { requests, isLoading, error, refetch, cancelRequest, isCancelling } = useMyRequests();
  const [selectedRequestId, setSelectedRequestId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [cancelError, setCancelError] = useState('');

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const handleOpenCancelModal = (id) => {
    setSelectedRequestId(id);
    setIsModalOpen(true);
    setCancelError('');
  };

  const handleConfirmCancel = async () => {
    setCancelError('');
    try {
      await cancelRequest(selectedRequestId);
      setIsModalOpen(false);
      setSelectedRequestId(null);
    } catch (err) {
      setCancelError(err.message || 'Failed to cancel the blood request.');
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  const columns = [
    {
      header: 'Request ID',
      field: 'id',
    },
    {
      header: 'Blood Group',
      render: (row) => (
        <span className="inline-flex items-center justify-center font-extrabold px-2.5 py-1 text-xs rounded-full bg-red-50 text-primary border border-red-100">
          {row.bloodGroupNeeded ? row.bloodGroupNeeded.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : '?'}
        </span>
      ),
    },
    {
      header: 'Units',
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
        const style = urgencyClasses[row.urgencyLevel] || 'bg-gray-100 text-gray-655 border-gray-200';
        return (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border ${style}`}>
            {row.urgencyLevel}
          </span>
        );
      },
    },
    {
      header: 'Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      header: 'Required By',
      render: (row) => <span>{formatDate(row.requiredByDate)}</span>,
    },
    {
      header: 'Actions',
      render: (row) => {
        const canCancel = ['PENDING', 'VERIFIED', 'MATCHED'].includes(row.status);
        return (
          <div className="flex items-center gap-2">
            <button
              onClick={() => navigate(`/patient/requests/${row.id}`)}
              className="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-150 rounded-xl transition-all"
              title="View Details"
            >
              <Eye className="h-4 w-4" />
            </button>
            {canCancel && (
              <button
                onClick={() => handleOpenCancelModal(row.id)}
                className="p-2 text-red-500 hover:text-red-750 hover:bg-red-50 rounded-xl transition-all"
                title="Cancel Request"
              >
                <Ban className="h-4 w-4" />
              </button>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Requests</h1>
          <p className="text-xs text-gray-500 mt-1">
            Track status history, verification states, and matching progress of your submissions.
          </p>
        </div>
        <Link to="/patient/create-request">
          <Button variant="primary" className="flex items-center gap-2 text-xs">
            <PlusCircle className="h-4 w-4" /> Create Request
          </Button>
        </Link>
      </div>

      {requests && requests.length > 0 ? (
        <DataTable
          columns={columns}
          data={requests}
          keyField="id"
          emptyMessage="No blood requests found."
        />
      ) : (
        <EmptyState
          message="You haven't submitted any blood match requests yet."
          icon={FileText}
        />
      )}

      {/* Cancellation modal details */}
      <ConfirmationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmCancel}
        isLoading={isCancelling}
        title="Cancel Blood Request"
        message={
          <div className="flex flex-col gap-3">
            <span>
              Are you sure you want to cancel this blood request? This will retract the request and stop all active donor compatibility searches.
            </span>
            {cancelError && (
              <span className="text-xs text-red-500 font-bold flex items-center gap-1 bg-red-50 p-2 rounded-lg border border-red-100">
                <AlertCircle className="h-3.5 w-3.5 inline shrink-0" /> {cancelError}
              </span>
            )}
          </div>
        }
      />
    </div>
  );
}
