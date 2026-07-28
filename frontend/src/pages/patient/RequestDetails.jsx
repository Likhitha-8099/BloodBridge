import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useRequestDetails } from '../../hooks/useRequestDetails';
import { useMyRequests } from '../../hooks/useMyRequests';
import Card from '../../components/ui/Card';
import StatusBadge from '../../components/ui/StatusBadge';
import RequestTimeline from '../../components/ui/RequestTimeline';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { ArrowLeft, Ban, AlertCircle, Layers, Hospital } from 'lucide-react';

/**
 * Screen showing complete details, timelines, and action overrides of a blood request.
 */
export default function RequestDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: request, isLoading, error, refetch } = useRequestDetails(id);
  const { cancelRequest, isCancelling } = useMyRequests();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [cancelError, setCancelError] = useState('');

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  if (!request) {
    return <ErrorState message="Request details not found." />;
  }

  const handleConfirmCancel = async () => {
    setCancelError('');
    try {
      await cancelRequest(request.id);
      setIsModalOpen(false);
      refetch();
    } catch (err) {
      setCancelError(err.message || 'Failed to cancel the request.');
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  };

  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return 'N/A';
    return new Date(dateTimeStr).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const canCancel = ['PENDING', 'VERIFIED', 'MATCHED'].includes(request.status);

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto">
      <div className="flex items-center gap-3 justify-between flex-wrap">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/patient/requests')}
            className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded-xl shadow-sm transition-all"
          >
            <ArrowLeft className="h-4 w-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-gray-900">Request #{request.id}</h1>
            <p className="text-xs text-gray-500 mt-0.5">
              Submitted on {formatDateTime(request.createdAt)}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <StatusBadge status={request.status} />
          {canCancel && (
            <Button
              variant="danger"
              onClick={() => setIsModalOpen(true)}
              className="flex items-center gap-2 text-xs py-2"
            >
              <Ban className="h-4 w-4" /> Cancel Request
            </Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left Side: Detail specifications */}
        <div className="md:col-span-2 flex flex-col gap-6">
          <Card className="flex flex-col gap-4">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-50 pb-2 flex items-center gap-2">
              <Layers className="h-4 w-4 text-primary" /> Request Specifications
            </h3>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">
                  Blood Group Needed
                </span>
                <span className="font-extrabold text-gray-800 mt-0.5">
                  {request.bloodGroupNeeded 
                    ? request.bloodGroupNeeded.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') 
                    : 'N/A'}
                </span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">
                  Units Required
                </span>
                <span className="font-bold text-gray-805 mt-0.5">{request.unitsRequired} Bags</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">
                  Urgency Level
                </span>
                <span className="font-bold text-gray-810 mt-0.5">{request.urgencyLevel}</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">
                  Required By Date
                </span>
                <span className="font-bold text-gray-815 mt-0.5">{formatDate(request.requiredByDate)}</span>
              </div>
            </div>

            <div className="flex flex-col gap-1.5 mt-2 bg-slate-50 p-4 rounded-xl border border-gray-100">
              <span className="text-xs font-semibold text-gray-705">Clinical Reason</span>
              <p className="text-xs text-gray-500 leading-relaxed mt-0.5">
                {request.reason || 'No clinical reason provided.'}
              </p>
            </div>

            {request.notes && (
              <div className="flex flex-col gap-1 bg-yellow-50/50 p-4 rounded-xl border border-yellow-100/50 text-xs">
                <span className="font-bold text-yellow-800">Hospital Reviewer Notes</span>
                <p className="text-yellow-700 mt-0.5 leading-relaxed">{request.notes}</p>
              </div>
            )}
          </Card>

          <Card className="flex flex-col gap-4">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-50 pb-2 flex items-center gap-2">
              <Hospital className="h-4 w-4 text-primary" /> Target Hospital Details
            </h3>

            <div className="flex flex-col gap-1">
              <h4 className="text-sm font-bold text-gray-700">{request.hospitalName}</h4>
              <p className="text-xs text-gray-450 mt-0.5">
                Authorized medical facility coordinating verification and match runs.
              </p>
            </div>
          </Card>
        </div>

        {/* Right Side: Step Timeline indicator */}
        <div className="flex flex-col gap-6">
          <Card className="h-fit">
            <RequestTimeline status={request.status} />
          </Card>
        </div>
      </div>

      {/* Cancellation confirmation modal */}
      <ConfirmationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmCancel}
        isLoading={isCancelling}
        title="Cancel Blood Request"
        message={
          <div className="flex flex-col gap-3">
            <span>
              Are you sure you want to retract blood request #{request.id}? Retracting this request terminates donor matching immediately.
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
