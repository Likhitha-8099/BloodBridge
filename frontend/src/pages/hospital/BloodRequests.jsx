import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useHospitalRequests } from '../../hooks/useHospitalRequests';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import StatusBadge from '../../components/ui/StatusBadge';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import { FileText, Eye, CheckCircle2, Ban, AlertCircle } from 'lucide-react';

/**
 * Screen displaying lists of active patient requests, supporting quick verify and reject buttons.
 */
export default function BloodRequests() {
  const navigate = useNavigate();
  const { profile } = useHospitalProfile();
  const { requests, isLoading, error, refetch, verifyRequest, rejectRequest } = useHospitalRequests();
  
  const [selectedReqId, setSelectedReqId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [actionError, setActionError] = useState('');
  const [activeTab, setActiveTab] = useState('assigned'); // 'assigned' or 'all'

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const handleVerify = async (id) => {
    setActionError('');
    try {
      await verifyRequest(id);
    } catch (err) {
      alert(err.message || 'Failed to verify request.');
    }
  };

  const handleOpenRejectModal = (id) => {
    setSelectedReqId(id);
    setIsModalOpen(true);
    setActionError('');
  };

  const handleConfirmReject = async () => {
    setActionError('');
    try {
      await rejectRequest(selectedReqId);
      setIsModalOpen(false);
      setSelectedReqId(null);
    } catch (err) {
      setActionError(err.message || 'Failed to reject the request.');
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

  // Filter requests lists
  const filteredRequests = (requests || []).filter(req => {
    if (activeTab === 'assigned' && profile) {
      return req.hospitalName === profile.hospitalName;
    }
    return true;
  });

  const columns = [
    {
      header: 'Request ID',
      field: 'id',
    },
    {
      header: 'Patient Name',
      field: 'patientName',
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
        const isMyHospital = profile && row.hospitalName === profile.hospitalName;
        const isPending = row.status === 'PENDING';
        
        return (
          <div className="flex items-center gap-2">
            <button
              onClick={() => navigate(`/hospital/requests/${row.id}`)}
              className="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-150 rounded-xl transition-all"
              title="View Details"
            >
              <Eye className="h-4 w-4" />
            </button>
            {isMyHospital && isPending && (
              <>
                <button
                  onClick={() => handleVerify(row.id)}
                  className="p-2 text-green-600 hover:text-green-800 hover:bg-green-50 rounded-xl transition-all"
                  title="Verify Request"
                >
                  <CheckCircle2 className="h-4 w-4" />
                </button>
                <button
                  onClick={() => handleOpenRejectModal(row.id)}
                  className="p-2 text-red-500 hover:text-red-750 hover:bg-red-55 rounded-xl transition-all"
                  title="Reject Request"
                >
                  <Ban className="h-4 w-4" />
                </button>
              </>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Patient Requests</h1>
        <p className="text-xs text-gray-500 mt-1">
          Review, verify, and check donor compatibility matches for transfusion requests.
        </p>
      </div>

      {/* Assigned tabs */}
      <div className="flex items-center border-b border-gray-100 gap-6">
        <button
          onClick={() => setActiveTab('assigned')}
          className={`pb-3 text-sm font-semibold transition-all border-b-2 ${
            activeTab === 'assigned'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-400 hover:text-gray-600'
          }`}
        >
          Assigned to My Hospital
        </button>
        <button
          onClick={() => setActiveTab('all')}
          className={`pb-3 text-sm font-semibold transition-all border-b-2 ${
            activeTab === 'all'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-400 hover:text-gray-600'
          }`}
        >
          All Active Requests
        </button>
      </div>

      {filteredRequests.length > 0 ? (
        <DataTable
          columns={columns}
          data={filteredRequests}
          keyField="id"
          emptyMessage="No blood requests found."
        />
      ) : (
        <EmptyState
          message={
            activeTab === 'assigned'
              ? 'No requests currently assigned to your hospital.'
              : 'No active blood requests found in the system.'
          }
          icon={FileText}
        />
      )}

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmReject}
        title="Reject Blood Request"
        message={
          <div className="flex flex-col gap-3">
            <span>
              Are you sure you want to reject this request? Rejection sets the request status to REJECTED and notifies the patient.
            </span>
            {actionError && (
              <span className="text-xs text-red-500 font-bold flex items-center gap-1 bg-red-50 p-2 rounded-lg border border-red-100">
                <AlertCircle className="h-3.5 w-3.5 inline shrink-0" /> {actionError}
              </span>
            )}
          </div>
        }
      />
    </div>
  );
}
