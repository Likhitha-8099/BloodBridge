import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useHospitalRequests } from '../../hooks/useHospitalRequests';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import HospitalStatusBadge from '../../components/hospital/common/HospitalStatusBadge';
import HospitalEmptyState from '../../components/hospital/common/HospitalEmptyState';
import { 
  FileText, 
  Eye, 
  CheckCircle2, 
  Ban, 
  AlertCircle, 
  Plus, 
  Search, 
  Droplet,
  Users
} from 'lucide-react';

/**
 * Hospital Blood Requests Management Screen.
 * Modern healthcare portal design preserving 100% of existing verification/rejection APIs and hooks.
 */
export default function BloodRequests() {
  const navigate = useNavigate();
  const { profile } = useHospitalProfile();
  const { requests, isLoading, error, refetch, verifyRequest, rejectRequest } = useHospitalRequests();

  const [selectedReqId, setSelectedReqId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [actionError, setActionError] = useState('');
  const [activeTab, setActiveTab] = useState('assigned'); // 'assigned' | 'all'
  const [searchQuery, setSearchQuery] = useState('');

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message || 'Failed to load blood requests.'} onRetry={refetch} />;
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

  const formatBloodGroup = (bg) => {
    if (!bg) return '?';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  // Filter requests
  const filteredRequests = (requests || []).filter((req) => {
    const isAssigned = activeTab === 'assigned' ? (profile && req.hospitalName === profile.hospitalName) : true;
    if (!isAssigned) return false;

    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      (req.patientName && req.patientName.toLowerCase().includes(q)) ||
      (req.bloodGroupNeeded && req.bloodGroupNeeded.toLowerCase().includes(q)) ||
      (req.id && req.id.toString().includes(q))
    );
  });

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans">
      <HospitalPageHeader
        title="Emergency Blood Requests"
        subtitle="Review, verify, and monitor urgent blood requests assigned to your institution."
        icon={FileText}
        badge="Clinical Management"
        breadcrumbs={[{ label: 'Blood Requests' }]}
        action={
          <Link to="/hospital/create-request">
            <button className="flex items-center gap-2 px-5 py-3 rounded-2xl bg-gradient-to-r from-teal-600 to-emerald-600 text-white font-bold text-xs shadow-lg shadow-teal-500/20 hover:shadow-teal-500/35 transition-all transform hover:-translate-y-0.5">
              <Plus className="h-4 w-4" />
              <span>Create Blood Request</span>
            </button>
          </Link>
        }
      />

      {/* Filter Tabs & Search Bar */}
      <HospitalCard bodyClassName="p-4 sm:p-6 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        {/* Navigation Tabs */}
        <div className="flex items-center gap-2 bg-slate-100 dark:bg-slate-800 p-1.5 rounded-2xl border border-slate-200/60 dark:border-slate-700/60">
          <button
            onClick={() => setActiveTab('assigned')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'assigned'
                ? 'bg-white dark:bg-slate-900 text-teal-600 dark:text-teal-400 shadow-sm'
                : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            Assigned to My Hospital
          </button>
          <button
            onClick={() => setActiveTab('all')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'all'
                ? 'bg-white dark:bg-slate-900 text-teal-600 dark:text-teal-400 shadow-sm'
                : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            All System Requests
          </button>
        </div>

        {/* Search Input */}
        <div className="relative flex items-center w-full sm:w-72">
          <Search className="absolute left-3.5 h-4 w-4 text-slate-400 pointer-events-none" />
          <input
            type="text"
            placeholder="Search patient, blood group..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-xl text-xs bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all"
          />
        </div>
      </HospitalCard>

      {/* Requests Table / Cards */}
      {filteredRequests.length > 0 ? (
        <HospitalCard bodyClassName="p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider border-b border-slate-100 dark:border-slate-800">
                <tr>
                  <th className="px-5 py-4">Request ID</th>
                  <th className="px-5 py-4">Patient Name</th>
                  <th className="px-5 py-4">Blood Group</th>
                  <th className="px-5 py-4">Units Needed</th>
                  <th className="px-5 py-4">Urgency</th>
                  <th className="px-5 py-4">Status</th>
                  <th className="px-5 py-4">Required Date</th>
                  <th className="px-5 py-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200">
                {filteredRequests.map((row) => {
                  const isMyHospital = profile && row.hospitalName === profile.hospitalName;
                  const isPending = row.status === 'PENDING';

                  return (
                    <tr key={row.id} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors">
                      <td className="px-5 py-4 font-mono font-bold text-slate-900 dark:text-white">
                        #{row.id}
                      </td>
                      <td className="px-5 py-4 font-semibold text-slate-900 dark:text-white">
                        {row.patientName || 'Emergency Patient'}
                      </td>
                      <td className="px-5 py-4">
                        <span className="inline-flex items-center gap-1 font-black px-3 py-1 text-xs rounded-full bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 border border-red-100 dark:border-red-900/40">
                          <Droplet className="h-3 w-3 fill-current" />
                          {formatBloodGroup(row.bloodGroupNeeded)}
                        </span>
                      </td>
                      <td className="px-5 py-4 font-bold text-slate-900 dark:text-white">
                        {row.unitsRequired} {row.unitsRequired === 1 ? 'Unit' : 'Units'}
                      </td>
                      <td className="px-5 py-4">
                        <HospitalStatusBadge status={row.urgencyLevel} type="urgency" />
                      </td>
                      <td className="px-5 py-4">
                        <HospitalStatusBadge status={row.status} />
                      </td>
                      <td className="px-5 py-4 text-slate-500 dark:text-slate-400 font-medium">
                        {formatDate(row.requiredByDate)}
                      </td>
                      <td className="px-5 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => navigate(`/hospital/requests/${row.id}`)}
                            className="p-2 text-slate-500 hover:text-teal-600 dark:hover:text-teal-400 hover:bg-teal-50 dark:hover:bg-teal-950/50 rounded-xl transition-colors"
                            title="View Details & Donor Matches"
                          >
                            <Eye className="h-4 w-4" />
                          </button>

                          <button
                            onClick={() => navigate(`/hospital/matches`)}
                            className="p-2 text-indigo-500 hover:text-indigo-700 dark:hover:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-950/50 rounded-xl transition-colors"
                            title="View Matches"
                          >
                            <Users className="h-4 w-4" />
                          </button>

                          {isMyHospital && isPending && (
                            <>
                              <button
                                onClick={() => handleVerify(row.id)}
                                className="p-2 text-emerald-600 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/50 rounded-xl transition-colors"
                                title="Verify Request"
                              >
                                <CheckCircle2 className="h-4 w-4" />
                              </button>

                              <button
                                onClick={() => handleOpenRejectModal(row.id)}
                                className="p-2 text-red-500 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/50 rounded-xl transition-colors"
                                title="Reject Request"
                              >
                                <Ban className="h-4 w-4" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </HospitalCard>
      ) : (
        <HospitalEmptyState
          title={activeTab === 'assigned' ? 'No Requests Assigned' : 'No Blood Requests Found'}
          description={
            activeTab === 'assigned'
              ? 'There are currently no active blood requests assigned to your hospital.'
              : 'No active blood requests match your current search criteria.'
          }
          icon={FileText}
          action={
            <Link to="/hospital/create-request">
              <button className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white font-bold text-xs rounded-xl transition-colors">
                Create New Request
              </button>
            </Link>
          }
        />
      )}

      {/* Rejection Modal */}
      <ConfirmationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmReject}
        title="Reject Blood Request"
        message={
          <div className="flex flex-col gap-3">
            <span>
              Are you sure you want to reject request #{selectedReqId}? This action sets the request status to REJECTED and notifies the patient.
            </span>
            {actionError && (
              <span className="text-xs text-red-500 font-bold flex items-center gap-1 bg-red-50 dark:bg-red-950/60 p-2.5 rounded-xl border border-red-200">
                <AlertCircle className="h-4 w-4 inline shrink-0" /> {actionError}
              </span>
            )}
          </div>
        }
      />
    </div>
  );
}
