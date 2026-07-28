import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useRequestDetails } from '../../hooks/useRequestDetails';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useHospitalRequests } from '../../hooks/useHospitalRequests';
import Card from '../../components/ui/Card';
import StatusBadge from '../../components/ui/StatusBadge';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { ArrowLeft, CheckCircle2, Ban, AlertCircle, ShieldCheck, Heart, User } from 'lucide-react';

/**
 * Detailed screen showing transfusion request requirements, patient context, and hospital verification triggers.
 */
export default function RequestDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: request, isLoading, error, refetch } = useRequestDetails(id);
  const { profile } = useHospitalProfile();
  const { verifyRequest, rejectRequest, isVerifying, isRejecting } = useHospitalRequests();
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [actionError, setActionError] = useState('');

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  if (!request) {
    return <ErrorState message="Request details not found." />;
  }

  const handleVerify = async () => {
    setActionError('');
    try {
      await verifyRequest(request.id);
      refetch();
    } catch (err) {
      setActionError(err.message || 'Failed to verify the request.');
    }
  };

  const handleConfirmReject = async () => {
    setActionError('');
    try {
      await rejectRequest(request.id);
      setIsModalOpen(false);
      refetch();
    } catch (err) {
      setActionError(err.message || 'Failed to reject the request.');
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

  const isMyHospital = profile && request.hospitalName === profile.hospitalName;
  const isPending = request.status === 'PENDING';
  const isVerified = ['VERIFIED', 'MATCHED', 'COMPLETED'].includes(request.status);

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto">
      <div className="flex items-center gap-3 justify-between flex-wrap">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/hospital/requests')}
            className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded-xl shadow-sm transition-all"
          >
            <ArrowLeft className="h-4 w-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-gray-900">Request #{request.id}</h1>
            <p className="text-xs text-gray-500 mt-0.5">Assigned to: {request.hospitalName}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <StatusBadge status={request.status} />
        </div>
      </div>

      {actionError && (
        <div className="flex items-start gap-2 bg-red-50 text-red-600 p-3.5 rounded-xl text-xs border border-red-100 font-medium">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{actionError}</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left Side: Detail specifications */}
        <div className="md:col-span-2 flex flex-col gap-6">
          <Card className="flex flex-col gap-4">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-50 pb-2 flex items-center gap-2">
              <User className="h-4 w-4 text-primary" /> Patient & Transfusion Info
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Patient Name</span>
                <span className="font-bold text-gray-800">{request.patientName}</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Blood Group Needed</span>
                <span className="font-extrabold text-gray-800 text-base">
                  {request.bloodGroupNeeded 
                    ? request.bloodGroupNeeded.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') 
                    : 'N/A'}
                </span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Units Required</span>
                <span className="font-bold text-gray-800">{request.unitsRequired} Bags</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Urgency Level</span>
                <span className="font-bold text-gray-800">{request.urgencyLevel}</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Required By Date</span>
                <span className="font-bold text-gray-800">{formatDate(request.requiredByDate)}</span>
              </div>
            </div>

            <div className="flex flex-col gap-1.5 mt-2 bg-slate-50 p-4 rounded-xl border border-gray-100">
              <span className="text-xs font-semibold text-gray-700">Patient Clinical Reason</span>
              <p className="text-xs text-gray-500 leading-relaxed mt-0.5">
                {request.reason || 'No clinical reason provided.'}
              </p>
            </div>
          </Card>
        </div>

        {/* Right Side Actions panel */}
        <div className="flex flex-col gap-6">
          {isMyHospital && (
            <Card className="flex flex-col gap-4">
              <h3 className="font-bold text-gray-800 text-sm pb-1">Verification Action Panel</h3>

              {isPending ? (
                <div className="flex flex-col gap-2">
                  <Button
                    variant="primary"
                    onClick={handleVerify}
                    isLoading={isVerifying}
                    className="w-full flex items-center justify-center gap-2 py-2.5 text-xs"
                  >
                    <CheckCircle2 className="h-4 w-4" /> Verify Request
                  </Button>
                  <Button
                    variant="danger"
                    onClick={() => setIsModalOpen(true)}
                    isLoading={isRejecting}
                    className="w-full flex items-center justify-center gap-2 py-2.5 text-xs"
                  >
                    <Ban className="h-4 w-4" /> Reject Request
                  </Button>
                </div>
              ) : isVerified ? (
                <div className="flex flex-col gap-3">
                  <div className="flex items-start gap-2 bg-green-50 text-green-700 p-3 rounded-xl text-xs border border-green-100 font-medium">
                    <ShieldCheck className="h-4 w-4 shrink-0 mt-0.5" />
                    <span>This request is verified. You can now execute donor matching compatibility routines.</span>
                  </div>
                  <Link to={`/hospital/matches?requestId=${request.id}`} className="w-full">
                    <Button variant="primary" className="w-full flex items-center justify-center gap-2 py-2.5 text-xs">
                      <Heart className="h-4 w-4" /> Run Matching Engine
                    </Button>
                  </Link>
                </div>
              ) : (
                <p className="text-xs text-gray-400">No actions can be performed on this request.</p>
              )}
            </Card>
          )}
        </div>
      </div>

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmReject}
        isLoading={isRejecting}
        title="Reject Blood Request"
        message="Are you sure you want to reject this request? Rejection sets the request status to REJECTED and notifies the patient."
      />
    </div>
  );
}
