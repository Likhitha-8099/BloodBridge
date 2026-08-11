import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useRequestDetails } from '../../hooks/useRequestDetails';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useHospitalRequests } from '../../hooks/useHospitalRequests';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import HospitalStatusBadge from '../../components/hospital/common/HospitalStatusBadge';
import EmergencyRequestResponses from '../../components/hospital/HospitalDashboard/EmergencyRequestResponses';
import { 
  ArrowLeft, 
  CheckCircle2, 
  Ban, 
  AlertCircle, 
  ShieldCheck, 
  Heart, 
  User, 
  Droplet,
  Calendar,
  Building,
  Activity
} from 'lucide-react';

/**
 * Detailed Request View for Hospital Portal.
 * Modern healthcare portal design preserving 100% of existing verification/rejection APIs and hooks.
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
    return <ErrorState message={error.message || 'Failed to load request details.'} onRetry={refetch} />;
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

  const formatBloodGroup = (bg) => {
    if (!bg) return 'N/A';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  const isMyHospital = profile && request.hospitalName === profile.hospitalName;
  const isPending = request.status === 'PENDING';
  const isVerified = ['VERIFIED', 'MATCHED', 'COMPLETED'].includes(request.status);

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans">
      <HospitalPageHeader
        title={`Transfusion Request #${request.id}`}
        subtitle={`Clinical Specifications & Verification Panel for ${request.patientName || 'Emergency Patient'}`}
        icon={Activity}
        badge={request.status}
        breadcrumbs={[
          { label: 'Blood Requests', to: '/hospital/requests' },
          { label: `Request #${request.id}` }
        ]}
        action={
          <button
            onClick={() => navigate('/hospital/requests')}
            className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Back to Requests</span>
          </button>
        }
      />

      {actionError && (
        <div className="flex items-start gap-3 bg-red-50 dark:bg-red-950/50 text-red-600 dark:text-red-400 p-4 rounded-2xl text-xs border border-red-100 dark:border-red-900/40 font-medium">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{actionError}</span>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Specifications */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          <HospitalCard
            title="Transfusion Requirements & Patient Info"
            subtitle="Clinical details provided during request creation"
            icon={User}
          >
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 py-2">
              <div className="flex flex-col gap-1 p-3.5 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800">
                <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Patient Name</span>
                <span className="font-bold text-sm text-slate-900 dark:text-white">{request.patientName || 'N/A'}</span>
              </div>

              <div className="flex flex-col gap-1 p-3.5 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800">
                <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Blood Group Needed</span>
                <span className="inline-flex items-center gap-1.5 font-black text-base text-red-600 dark:text-red-400">
                  <Droplet className="h-4 w-4 fill-current" />
                  {formatBloodGroup(request.bloodGroupNeeded)}
                </span>
              </div>

              <div className="flex flex-col gap-1 p-3.5 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800">
                <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Units Required</span>
                <span className="font-bold text-sm text-slate-900 dark:text-white">{request.unitsRequired} Units</span>
              </div>

              <div className="flex flex-col gap-1 p-3.5 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800">
                <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Urgency Level</span>
                <div>
                  <HospitalStatusBadge status={request.urgencyLevel} type="urgency" />
                </div>
              </div>

              <div className="flex flex-col gap-1 p-3.5 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800 sm:col-span-2">
                <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Assigned Hospital</span>
                <span className="font-bold text-sm text-slate-900 dark:text-white flex items-center gap-2">
                  <Building className="h-4 w-4 text-teal-600 dark:text-teal-400" />
                  {request.hospitalName}
                </span>
              </div>

              <div className="flex flex-col gap-1 p-3.5 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800 sm:col-span-2">
                <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Required By Date</span>
                <span className="font-bold text-sm text-slate-900 dark:text-white flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-teal-600 dark:text-teal-400" />
                  {formatDate(request.requiredByDate)}
                </span>
              </div>
            </div>

            <div className="mt-4 p-4 rounded-2xl bg-teal-50/50 dark:bg-teal-950/30 border border-teal-100 dark:border-teal-900/40">
              <span className="text-xs font-bold text-teal-900 dark:text-teal-200">Patient Clinical Reason</span>
              <p className="text-xs text-teal-800/80 dark:text-teal-300/80 leading-relaxed mt-1">
                {request.reason || 'No specific clinical notes provided.'}
              </p>
            </div>
          </HospitalCard>

          {/* Matched Donor Feed */}
          <EmergencyRequestResponses requestId={request.id} />
        </div>

        {/* Right Column: Actions Panel */}
        <div className="flex flex-col gap-6">
          <HospitalCard
            title="Verification Action Panel"
            subtitle="Clinical verification triggers"
            icon={ShieldCheck}
          >
            {isMyHospital && isPending ? (
              <div className="flex flex-col gap-3">
                <button
                  onClick={handleVerify}
                  disabled={isVerifying}
                  className="w-full py-3 px-4 rounded-2xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-md shadow-emerald-600/20 transition-all flex items-center justify-center gap-2"
                >
                  <CheckCircle2 className="h-4 w-4" />
                  <span>{isVerifying ? 'Verifying...' : 'Verify Request'}</span>
                </button>

                <button
                  onClick={() => setIsModalOpen(true)}
                  disabled={isRejecting}
                  className="w-full py-3 px-4 rounded-2xl bg-red-600 hover:bg-red-500 text-white font-bold text-xs shadow-md shadow-red-600/20 transition-all flex items-center justify-center gap-2"
                >
                  <Ban className="h-4 w-4" />
                  <span>{isRejecting ? 'Rejecting...' : 'Reject Request'}</span>
                </button>
              </div>
            ) : isVerified ? (
              <div className="flex flex-col gap-4">
                <div className="flex items-start gap-2.5 bg-emerald-50 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-300 p-4 rounded-2xl text-xs border border-emerald-100 dark:border-emerald-900/40 font-medium">
                  <ShieldCheck className="h-4 w-4 shrink-0 mt-0.5 text-emerald-600" />
                  <span>This request is verified. You can now execute donor matching compatibility routines.</span>
                </div>

                <Link to={`/hospital/matches?requestId=${request.id}`} className="w-full">
                  <button className="w-full py-3 px-4 rounded-2xl bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs shadow-lg shadow-teal-500/20 transition-all flex items-center justify-center gap-2">
                    <Heart className="h-4 w-4" />
                    <span>Run Matching Engine</span>
                  </button>
                </Link>
              </div>
            ) : (
              <p className="text-xs text-slate-400 dark:text-slate-500 italic text-center py-4">
                No verification actions available for this request.
              </p>
            )}
          </HospitalCard>
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
