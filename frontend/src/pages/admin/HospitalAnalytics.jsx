import React, { useEffect, useState, useMemo } from 'react';
import { useTopHospitals } from '../../hooks/useAdminStatistics';
import { useWebSocket } from '../../hooks/useWebSocket';
import adminService from '../../services/adminService';
import DataTable from '../../components/ui/DataTable';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import { 
  Hospital, CheckCircle2, XCircle, ShieldCheck, Clock, Eye, 
  Search, Building, AlertTriangle, UserCheck
} from 'lucide-react';

/**
 * Enterprise Hospital Verification & Approval Workflow Module for Admin Portal.
 * Manages Pending, Approved, and Rejected Hospital registrations with inspection modal and audit trail.
 */
export default function HospitalAnalytics() {
  const { isLoading: loadingLeaderboard, refetch: refetchLeaderboard } = useTopHospitals();
  
  const hospitalTopics = useMemo(() => ['/topic/admin/hospitals', '/topic/admin/dashboard'], []);

  useWebSocket(hospitalTopics, () => {
    console.log('⚡ Real-time Hospital event received, updating verification list...');
    fetchHospitals();
    refetchLeaderboard();
  });
  
  const [activeTab, setActiveTab] = useState('PENDING'); // 'PENDING' | 'APPROVED' | 'REJECTED'
  const [hospitalsList, setHospitalsList] = useState([]);
  const [loadingHospitals, setLoadingHospitals] = useState(true);
  
  // Search & Filter State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedCity, setSelectedCity] = useState('ALL');

  // Modal States
  const [selectedHospital, setSelectedHospital] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [actionModal, setActionModal] = useState({ open: false, type: null, hospital: null }); // type: 'APPROVE' | 'REJECT'
  const [actionRemarks, setActionRemarks] = useState('');
  const [isSubmittingAction, setIsSubmittingAction] = useState(false);
  const [notificationMsg, setNotificationMsg] = useState('');

  // Fetch Hospitals Data
  const fetchHospitals = async () => {
    setLoadingHospitals(true);
    try {
      const data = await adminService.getPendingHospitals();
      setHospitalsList(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load hospitals list:', err);
    } finally {
      setLoadingHospitals(false);
    }
  };

  useEffect(() => {
    fetchHospitals();
  }, []);

  // Filtered Hospitals based on Tab, Search, and Filters
  const filteredHospitals = hospitalsList.filter((h) => {
    const vStatus = (h.verificationStatus || h.status || 'PENDING').toUpperCase();
    const tabMatch = activeTab === 'PENDING' 
      ? (vStatus === 'PENDING' || (!h.verified && vStatus !== 'REJECTED'))
      : activeTab === 'APPROVED' 
        ? (vStatus === 'APPROVED' || vStatus === 'VERIFIED' || h.verified === true)
        : (vStatus === 'REJECTED');

    const searchLower = searchQuery.toLowerCase();
    const matchesSearch = !searchQuery || 
      (h.hospitalName && h.hospitalName.toLowerCase().includes(searchLower)) ||
      (h.registrationNumber && h.registrationNumber.toLowerCase().includes(searchLower)) ||
      (h.email && h.email.toLowerCase().includes(searchLower)) ||
      (h.city && h.city.toLowerCase().includes(searchLower));

    const matchesType = selectedType === 'ALL' || h.hospitalType === selectedType;
    const matchesCity = selectedCity === 'ALL' || h.city === selectedCity;

    return tabMatch && matchesSearch && matchesType && matchesCity;
  });

  // Calculate Metrics Counts
  const pendingCount = hospitalsList.filter(h => {
    const s = (h.verificationStatus || h.status || 'PENDING').toUpperCase();
    return s === 'PENDING' || (!h.verified && s !== 'REJECTED');
  }).length;

  const approvedCount = hospitalsList.filter(h => {
    const s = (h.verificationStatus || h.status || '').toUpperCase();
    return s === 'APPROVED' || s === 'VERIFIED' || h.verified === true;
  }).length;

  const rejectedCount = hospitalsList.filter(h => {
    const s = (h.verificationStatus || h.status || '').toUpperCase();
    return s === 'REJECTED';
  }).length;

  const todaysRegistrations = hospitalsList.filter(h => {
    if (!h.createdAt) return false;
    const regDate = new Date(h.createdAt).toDateString();
    return regDate === new Date().toDateString();
  }).length;

  // Handle Approve / Reject Submission
  const handleActionSubmit = async (e) => {
    e.preventDefault();
    if (!actionModal.hospital) return;

    setIsSubmittingAction(true);
    setNotificationMsg('');
    const status = actionModal.type === 'APPROVE' ? 'APPROVED' : 'REJECTED';

    try {
      await adminService.verifyHospital(
        actionModal.hospital.id, 
        status, 
        actionRemarks || (status === 'APPROVED' ? 'Approved by Administrator' : 'Registration rejected due to license mismatch')
      );

      setNotificationMsg(
        status === 'APPROVED'
          ? `Hospital "${actionModal.hospital.hospitalName}" approved successfully! Hospital can now log in.`
          : `Hospital "${actionModal.hospital.hospitalName}" registration rejected.`
      );

      setActionModal({ open: false, type: null, hospital: null });
      setActionRemarks('');
      fetchHospitals();
      refetchLeaderboard();
    } catch (err) {
      setNotificationMsg(err.message || 'Failed to update verification status.');
    } finally {
      setIsSubmittingAction(false);
    }
  };

  if (loadingHospitals && loadingLeaderboard) {
    return <LoadingSpinner fullScreen />;
  }

  const columns = [
    {
      header: 'Hospital Name',
      render: (row) => (
        <div className="flex flex-col">
          <span className="font-bold text-gray-900 dark:text-white text-sm">{row.hospitalName || 'Registered Hospital'}</span>
          <span className="text-[11px] text-gray-400 font-mono">{row.email}</span>
        </div>
      )
    },
    {
      header: 'Reg / License No',
      render: (row) => (
        <div className="flex flex-col">
          <span className="font-mono text-xs font-semibold text-slate-700 dark:text-slate-300">
            {row.registrationNumber || 'REG-PENDING'}
          </span>
          {row.licenseNumber && <span className="text-[10px] text-slate-400">Lic: {row.licenseNumber}</span>}
        </div>
      )
    },
    {
      header: 'Hospital Type',
      render: (row) => (
        <span className="text-xs font-medium text-slate-700 dark:text-slate-300">
          {row.hospitalType || 'GENERAL'}
        </span>
      )
    },
    {
      header: 'City & State',
      render: (row) => (
        <span className="text-xs text-gray-600 dark:text-slate-400">
          {row.city || 'N/A'}, {row.state || 'N/A'}
        </span>
      )
    },
    {
      header: 'Status',
      render: (row) => {
        const status = (row.verificationStatus || row.status || 'PENDING').toUpperCase();
        const variant = status === 'APPROVED' || status === 'VERIFIED' ? 'success' : status === 'REJECTED' ? 'error' : 'warning';
        return <Badge variant={variant}>{status}</Badge>;
      }
    },
    {
      header: 'Actions',
      render: (row) => {
        const status = (row.verificationStatus || row.status || 'PENDING').toUpperCase();
        const isPending = status === 'PENDING' || (!row.verified && status !== 'REJECTED');

        return (
          <div className="flex items-center gap-1.5">
            <Button
              size="xs"
              variant="outline"
              onClick={() => {
                setSelectedHospital(row);
                setShowDetailModal(true);
              }}
              title="View Full Profile Details"
            >
              <Eye className="h-3.5 w-3.5 mr-1" /> View Details
            </Button>

            {isPending && (
              <>
                <Button
                  size="xs"
                  variant="primary"
                  className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold"
                  onClick={() => setActionModal({ open: true, type: 'APPROVE', hospital: row })}
                >
                  <CheckCircle2 className="h-3.5 w-3.5 mr-1" /> Approve
                </Button>
                <Button
                  size="xs"
                  variant="outline"
                  className="text-red-600 border-red-200 hover:bg-red-50 font-bold"
                  onClick={() => setActionModal({ open: true, type: 'REJECT', hospital: row })}
                >
                  <XCircle className="h-3.5 w-3.5 mr-1" /> Reject
                </Button>
              </>
            )}
          </div>
        );
      }
    }
  ];

  return (
    <div className="flex flex-col gap-6 font-sans">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900 dark:text-white tracking-tight">
          Hospital Verification & Approval Center
        </h1>
        <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">
          Review institutional registrations, inspect medical licenses, and approve or reject hospital credentials.
        </p>
      </div>

      {notificationMsg && (
        <div className="bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 p-4 rounded-2xl text-xs border border-emerald-100 dark:border-emerald-900/30 font-medium flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-2">
            <ShieldCheck className="h-4 w-4 shrink-0" />
            <span>{notificationMsg}</span>
          </div>
          <button onClick={() => setNotificationMsg('')} className="text-emerald-700 font-bold hover:underline">Dismiss</button>
        </div>
      )}

      {/* SUMMARY STATS CARDS */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-xs flex flex-col gap-1">
          <span className="text-xs font-bold text-amber-600 dark:text-amber-400 flex items-center gap-1.5">
            <Clock className="h-4 w-4" /> Pending Approval
          </span>
          <span className="text-2xl font-black text-gray-900 dark:text-white">{pendingCount}</span>
          <span className="text-[10px] text-gray-400">Awaiting admin review</span>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-xs flex flex-col gap-1">
          <span className="text-xs font-bold text-emerald-600 dark:text-emerald-400 flex items-center gap-1.5">
            <CheckCircle2 className="h-4 w-4" /> Approved Hospitals
          </span>
          <span className="text-2xl font-black text-gray-900 dark:text-white">{approvedCount}</span>
          <span className="text-[10px] text-gray-400">Active on platform</span>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-xs flex flex-col gap-1">
          <span className="text-xs font-bold text-red-600 dark:text-red-400 flex items-center gap-1.5">
            <XCircle className="h-4 w-4" /> Rejected Requests
          </span>
          <span className="text-2xl font-black text-gray-900 dark:text-white">{rejectedCount}</span>
          <span className="text-[10px] text-gray-400">Denied registration</span>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-xs flex flex-col gap-1">
          <span className="text-xs font-bold text-blue-600 dark:text-blue-400 flex items-center gap-1.5">
            <UserCheck className="h-4 w-4" /> Today's Registrations
          </span>
          <span className="text-2xl font-black text-gray-900 dark:text-white">{todaysRegistrations}</span>
          <span className="text-[10px] text-gray-400">New submissions today</span>
        </div>
      </div>

      {/* WORKFLOW NAVIGATION TABS */}
      <Card className="p-6">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-100 dark:border-slate-800 pb-4 mb-6">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setActiveTab('PENDING')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 ${
                activeTab === 'PENDING'
                  ? 'bg-amber-500 text-white shadow-md'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200'
              }`}
            >
              <Clock className="h-3.5 w-3.5" /> Pending Hospitals ({pendingCount})
            </button>

            <button
              onClick={() => setActiveTab('APPROVED')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 ${
                activeTab === 'APPROVED'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200'
              }`}
            >
              <CheckCircle2 className="h-3.5 w-3.5" /> Approved ({approvedCount})
            </button>

            <button
              onClick={() => setActiveTab('REJECTED')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 ${
                activeTab === 'REJECTED'
                  ? 'bg-red-600 text-white shadow-md'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200'
              }`}
            >
              <XCircle className="h-3.5 w-3.5" /> Rejected ({rejectedCount})
            </button>
          </div>
        </div>

        {/* SEARCH & FILTER BAR */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
          <div className="relative">
            <Search className="h-4 w-4 absolute left-3.5 top-3 text-gray-400" />
            <input
              type="text"
              placeholder="Search by name, reg no, or email..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-200 dark:border-slate-800 text-xs bg-slate-50 dark:bg-slate-800/50 text-gray-900 dark:text-white"
            />
          </div>

          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-slate-800 text-xs bg-slate-50 dark:bg-slate-800/50 text-gray-900 dark:text-white"
          >
            <option value="ALL">All Hospital Types</option>
            <option value="Government Hospital">Government Hospital</option>
            <option value="Private Multi-Speciality">Private Multi-Speciality</option>
            <option value="Trust / Charitable Hospital">Trust / Charitable Hospital</option>
            <option value="Speciality Blood Bank Center">Speciality Blood Bank Center</option>
          </select>

          <button
            onClick={() => { setSearchQuery(''); setSelectedType('ALL'); setSelectedCity('ALL'); }}
            className="text-xs font-semibold text-gray-500 hover:text-primary transition-colors self-center"
          >
            Reset Filters
          </button>
        </div>

        {/* DATA TABLE */}
        {loadingHospitals ? (
          <LoadingSpinner />
        ) : filteredHospitals.length > 0 ? (
          <DataTable columns={columns} data={filteredHospitals} keyField="id" />
        ) : (
          <EmptyState
            message={`No ${activeTab.toLowerCase()} hospital records found.`}
            icon={Hospital}
          />
        )}
      </Card>

      {/* DETAIL INSPECTION MODAL */}
      {showDetailModal && selectedHospital && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <Card className="w-full max-w-2xl p-6 max-h-[90vh] overflow-y-auto space-y-6 bg-white dark:bg-slate-900 rounded-3xl">
            <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-3">
              <div className="flex items-center gap-2">
                <Building className="h-6 w-6 text-teal-600" />
                <h3 className="font-bold text-lg text-gray-900 dark:text-white">Hospital Inspection Dossier</h3>
              </div>
              <button onClick={() => setShowDetailModal(false)} className="text-gray-400 hover:text-gray-600 font-bold text-lg">✕</button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
              <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-2">
                <h4 className="font-bold text-slate-800 dark:text-white">Hospital Information</h4>
                <div><span className="text-gray-400 block">Name</span><strong>{selectedHospital.hospitalName}</strong></div>
                <div><span className="text-gray-400 block">Registration No</span><strong>{selectedHospital.registrationNumber || 'PENDING'}</strong></div>
                <div><span className="text-gray-400 block">License No</span><strong>{selectedHospital.licenseNumber || 'N/A'}</strong></div>
                <div><span className="text-gray-400 block">Type</span><strong>{selectedHospital.hospitalType || 'GENERAL'}</strong></div>
              </div>

              <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-2">
                <h4 className="font-bold text-slate-800 dark:text-white">Contact & Admin Person</h4>
                <div><span className="text-gray-400 block">Contact Person</span><strong>{selectedHospital.contactPerson || 'Hospital Representative'}</strong></div>
                <div><span className="text-gray-400 block">Email</span><strong>{selectedHospital.email}</strong></div>
                <div><span className="text-gray-400 block">Phone</span><strong>{selectedHospital.phoneNumber}</strong></div>
                <div><span className="text-gray-400 block">Website</span><strong>{selectedHospital.website || 'None'}</strong></div>
              </div>
            </div>

            <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 text-xs space-y-2">
              <h4 className="font-bold text-slate-800 dark:text-white">Address & Location</h4>
              <p className="font-medium text-slate-700 dark:text-slate-300">
                {selectedHospital.address}, {selectedHospital.city}, {selectedHospital.state}, {selectedHospital.country} - {selectedHospital.postalCode}
              </p>
            </div>

            {selectedHospital.rejectionReason && (
              <div className="bg-red-50 dark:bg-red-950/40 p-4 rounded-2xl border border-red-100 text-xs space-y-1">
                <h4 className="font-bold text-red-700 dark:text-red-400 flex items-center gap-1.5">
                  <AlertTriangle className="h-4 w-4" /> Rejection Reason
                </h4>
                <p className="text-red-600 dark:text-red-300 font-medium">{selectedHospital.rejectionReason}</p>
              </div>
            )}

            <div className="flex justify-end gap-3 pt-2 border-t border-slate-100 dark:border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setShowDetailModal(false)}>Close Dossier</Button>
            </div>
          </Card>
        </div>
      )}

      {/* APPROVE / REJECT CONFIRMATION MODAL */}
      {actionModal.open && actionModal.hospital && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <Card className="w-full max-w-md p-6 space-y-4 bg-white dark:bg-slate-900 rounded-3xl">
            <div className="flex items-center gap-2 border-b border-slate-100 dark:border-slate-800 pb-3">
              {actionModal.type === 'APPROVE' ? (
                <CheckCircle2 className="h-6 w-6 text-emerald-600" />
              ) : (
                <XCircle className="h-6 w-6 text-red-600" />
              )}
              <h3 className="font-bold text-base text-gray-900 dark:text-white">
                {actionModal.type === 'APPROVE' ? 'Approve Hospital Registration' : 'Reject Hospital Registration'}
              </h3>
            </div>

            <p className="text-xs text-gray-600 dark:text-slate-300 leading-relaxed">
              Are you sure you want to {actionModal.type.toLowerCase()} <strong>{actionModal.hospital.hospitalName}</strong>? 
              {actionModal.type === 'APPROVE' 
                ? ' This will activate their hospital account and grant access to the Hospital Dashboard.' 
                : ' This will block their login and send a rejection notice.'}
            </p>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-gray-700 dark:text-slate-300">
                {actionModal.type === 'APPROVE' ? 'Approval Remarks (Optional)' : 'Rejection Reason (Required)'}
              </label>
              <textarea
                rows={3}
                placeholder={actionModal.type === 'APPROVE' ? 'e.g. License verified against state medical board registry.' : 'e.g. License document expired or invalid registration number.'}
                value={actionRemarks}
                onChange={(e) => setActionRemarks(e.target.value)}
                className="w-full p-3 rounded-xl border border-gray-200 dark:border-slate-800 text-xs bg-slate-50 dark:bg-slate-800/50 text-gray-900 dark:text-white"
              />
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-100 dark:border-slate-800">
              <Button 
                type="button" 
                variant="outline" 
                size="sm" 
                onClick={() => setActionModal({ open: false, type: null, hospital: null })}
              >
                Cancel
              </Button>
              
              <Button
                type="button"
                variant="primary"
                size="sm"
                isLoading={isSubmittingAction}
                className={actionModal.type === 'APPROVE' ? 'bg-emerald-600 hover:bg-emerald-700 text-white font-bold' : 'bg-red-600 hover:bg-red-700 text-white font-bold'}
                onClick={handleActionSubmit}
              >
                Confirm {actionModal.type === 'APPROVE' ? 'Approval' : 'Rejection'}
              </Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}
