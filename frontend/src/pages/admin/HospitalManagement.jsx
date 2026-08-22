import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Building2, Search, Trash2, Eye, MapPin, Phone, Mail, 
  ShieldCheck, CheckCircle2, XCircle, Clock, RefreshCw
} from 'lucide-react';
import Card from '../../components/ui/Card';
import StatCard from '../../components/ui/StatCard';
import Button from '../../components/ui/Button';
import DataTable from '../../components/ui/DataTable';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import useToastStore from '../../store/toastStore';
import adminService from '../../services/adminService';
import { useWebSocket } from '../../hooks/useWebSocket';

/**
 * Dedicated Admin Hospital Management Page.
 * Displays all registered hospitals from the real database, with live real-time sync,
 * status filtering, view details navigation, license verification, and permanent deletion.
 */
export default function HospitalManagement() {
  const navigate = useNavigate();
  const { addToast } = useToastStore();

  const [hospitals, setHospitals] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Search and Filter State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('ALL');

  // Deletion Modal State
  const [selectedHospitalToDelete, setSelectedHospitalToDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // WebSocket Live Subscription
  const wsTopics = useMemo(() => ['/topic/admin/hospitals', '/topic/admin/dashboard'], []);
  useWebSocket(wsTopics, () => {
    console.log('⚡ Real-time hospital update event received, refreshing hospital list...');
    fetchHospitals(false);
  });

  const fetchHospitals = async (showLoading = true) => {
    if (showLoading) setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getAllHospitals();
      setHospitals(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load registered hospitals:', err);
      setError(err?.response?.data?.message || err?.message || 'Unable to load hospitals. Please try again.');
    } finally {
      if (showLoading) setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchHospitals(true);
  }, []);

  // Filtered Hospitals
  const filteredHospitals = useMemo(() => {
    return hospitals.filter((h) => {
      const searchLower = searchQuery.toLowerCase();
      const name = (h.hospitalName || '').toLowerCase();
      const email = (h.email || '').toLowerCase();
      const phone = (h.phoneNumber || '').toLowerCase();
      const city = (h.city || '').toLowerCase();
      const state = (h.state || '').toLowerCase();

      const matchesSearch = !searchQuery || 
        name.includes(searchLower) || 
        email.includes(searchLower) || 
        phone.includes(searchLower) || 
        city.includes(searchLower) || 
        state.includes(searchLower);

      const status = (h.verificationStatus || (h.verified ? 'APPROVED' : 'PENDING')).toUpperCase();
      const matchesStatus = selectedStatus === 'ALL' || status === selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }, [hospitals, searchQuery, selectedStatus]);

  // Summary Metrics
  const totalCount = hospitals.length;
  const approvedCount = hospitals.filter(h => h.verificationStatus === 'APPROVED' || Boolean(h.verified)).length;
  const pendingCount = hospitals.filter(h => h.verificationStatus === 'PENDING' || (!h.verified && h.verificationStatus !== 'REJECTED')).length;

  const handleConfirmDelete = async () => {
    if (!selectedHospitalToDelete) return;
    const hospitalId = selectedHospitalToDelete.id ?? selectedHospitalToDelete.userId;
    const hospitalName = selectedHospitalToDelete.hospitalName || 'Hospital';

    console.log('[DELETE HOSPITAL] Selected hospital:', selectedHospitalToDelete);
    console.log('[DELETE HOSPITAL] Resolved hospital ID:', hospitalId);

    if (hospitalId === undefined || hospitalId === null || hospitalId === 'undefined' || hospitalId === 'null') {
      console.error('[DELETE HOSPITAL ERROR] Unable to delete hospital: hospital ID is missing.', selectedHospitalToDelete);
      addToast('Unable to delete hospital: hospital ID is missing.', 'error');
      return;
    }

    try {
      setIsDeleting(true);
      await adminService.deleteHospital(hospitalId);
      addToast(`Hospital "${hospitalName}" was permanently deleted successfully.`, 'success');
      setSelectedHospitalToDelete(null);
      fetchHospitals(false);
    } catch (err) {
      console.error('Failed to delete hospital:', err);
      const errMsg = err?.response?.data?.message || err?.message || 'Unable to delete hospital.';
      addToast(errMsg, 'error');
    } finally {
      setIsDeleting(false);
    }
  };

  const hospitalColumns = [
    {
      header: 'Hospital ID',
      render: (row) => (
        <span className="font-mono text-xs font-bold text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-1 rounded-md">
          #{row.id ?? '—'}
        </span>
      )
    },
    {
      header: 'Hospital Name',
      render: (row) => (
        <div className="flex flex-col">
          <span className="font-bold text-slate-900 dark:text-white text-sm tracking-tight flex items-center gap-1.5">
            <Building2 className="h-3.5 w-3.5 text-red-500 flex-shrink-0" />
            {row.hospitalName || 'Registered Hospital'}
          </span>
          {row.hospitalType && (
            <span className="text-[10px] text-slate-400 dark:text-slate-500 uppercase font-semibold">
              {row.hospitalType}
            </span>
          )}
        </div>
      )
    },
    {
      header: 'Email',
      render: (row) => (
        <span className="text-xs text-slate-600 dark:text-slate-300 font-mono flex items-center gap-1">
          <Mail className="h-3 w-3 text-slate-400" />
          {row.email || '—'}
        </span>
      )
    },
    {
      header: 'Phone',
      render: (row) => (
        <span className="text-xs text-slate-600 dark:text-slate-300 font-mono flex items-center gap-1">
          <Phone className="h-3 w-3 text-slate-400" />
          {row.phoneNumber || '—'}
        </span>
      )
    },
    {
      header: 'Location',
      render: (row) => (
        <span className="text-xs text-slate-600 dark:text-slate-400 flex items-center gap-1">
          <MapPin className="h-3 w-3 text-slate-400" />
          {row.city || 'N/A'}{row.state ? `, ${row.state}` : ''}
        </span>
      )
    },
    {
      header: 'Status',
      render: (row) => {
        const status = (row.verificationStatus || (row.verified ? 'APPROVED' : 'PENDING')).toUpperCase();
        let badgeStyle = 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700';
        let icon = <Clock className="h-3 w-3" />;

        if (status === 'APPROVED') {
          badgeStyle = 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800';
          icon = <CheckCircle2 className="h-3 w-3 text-emerald-600 dark:text-emerald-400" />;
        } else if (status === 'PENDING') {
          badgeStyle = 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300 border-amber-200 dark:border-amber-800';
          icon = <Clock className="h-3 w-3 text-amber-600 dark:text-amber-400" />;
        } else if (status === 'REJECTED') {
          badgeStyle = 'bg-rose-50 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300 border-rose-200 dark:border-rose-800';
          icon = <XCircle className="h-3 w-3 text-rose-600 dark:text-rose-400" />;
        }

        return (
          <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold border ${badgeStyle}`}>
            {icon}
            {status}
          </span>
        );
      }
    },
    {
      header: 'Registered On',
      render: (row) => {
        const dateStr = row.createdAt ? new Date(row.createdAt).toLocaleDateString(undefined, {
          year: 'numeric', month: 'short', day: 'numeric'
        }) : '—';
        return <span className="text-xs text-slate-500 dark:text-slate-400">{dateStr}</span>;
      }
    },
    {
      header: 'Actions',
      render: (row) => {
        const hospitalId = row.id ?? row.userId;
        return (
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate(`/admin/hospitals/${hospitalId}`)}
              className="flex items-center gap-1.5 text-xs py-1 px-2.5 h-8 dark:border-slate-700 dark:hover:bg-slate-800"
              title="View Hospital Details"
            >
              <Eye className="h-3.5 w-3.5 text-blue-600 dark:text-blue-400" />
              <span>View</span>
            </Button>

            <Button
              variant="danger"
              size="sm"
              onClick={() => setSelectedHospitalToDelete(row)}
              className="flex items-center gap-1 text-xs py-1 px-2.5 h-8 bg-red-600 hover:bg-red-700 text-white"
              title="Permanently Delete Hospital"
            >
              <Trash2 className="h-3.5 w-3.5" />
              <span>Delete</span>
            </Button>
          </div>
        );
      }
    }
  ];

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={() => fetchHospitals(true)} />;
  }

  return (
    <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-6 animate-fadeIn transition-colors duration-150">
      {/* PAGE HEADER */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-slate-900 dark:text-white tracking-tight flex items-center gap-3">
            <Building2 className="h-8 w-8 text-blue-600 dark:text-blue-500" />
            Hospital Management
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1 font-medium">
            Manage all registered hospitals in the BloodBridge AI system.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={() => fetchHospitals(true)}
            className="flex items-center gap-2 text-xs dark:border-slate-700 dark:bg-slate-800"
          >
            <RefreshCw className="h-3.5 w-3.5 text-slate-500" />
            Refresh
          </Button>
        </div>
      </div>

      {/* SUMMARY STAT CARDS */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <StatCard
          title="Total Registered Hospitals"
          value={totalCount}
          subtitle="Registered hospital profiles"
          icon={Building2}
          variant="default"
        />
        <StatCard
          title="Approved Hospitals"
          value={approvedCount}
          subtitle="Verified & active credentials"
          icon={ShieldCheck}
          variant="success"
        />
        <StatCard
          title="Pending Verifications"
          value={pendingCount}
          subtitle="Awaiting license review"
          icon={Clock}
          variant="warning"
        />
      </div>

      {/* HOSPITAL TABLE CARD */}
      <Card className="border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl overflow-hidden">
        {/* TABLE FILTER CONTROLS */}
        <div className="p-5 border-b border-slate-100 dark:border-slate-800/80 bg-slate-50/50 dark:bg-slate-800/30">
          <div className="grid grid-cols-1 sm:grid-cols-12 gap-3">
            <div className="sm:col-span-8 relative">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 dark:text-slate-500" />
              <input
                type="text"
                placeholder="Search hospitals by name, email, phone, city..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-colors"
              />
            </div>

            <div className="sm:col-span-4">
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-colors"
              >
                <option value="ALL">All Verification Statuses</option>
                <option value="APPROVED">Approved Only</option>
                <option value="PENDING">Pending Review Only</option>
                <option value="REJECTED">Rejected Only</option>
              </select>
            </div>
          </div>
        </div>

        {/* DATA TABLE */}
        <div className="p-0">
          <DataTable
            columns={hospitalColumns}
            data={filteredHospitals}
            keyField="id"
            emptyMessage="No registered hospitals found."
          />
        </div>
      </Card>

      {/* CONFIRMATION MODAL */}
      <ConfirmationModal
        isOpen={Boolean(selectedHospitalToDelete)}
        onClose={() => {
          if (!isDeleting) setSelectedHospitalToDelete(null);
        }}
        onConfirm={handleConfirmDelete}
        title="Delete Hospital Profile?"
        message={
          <div className="flex flex-col gap-2">
            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed font-medium">
              Are you sure you want to permanently delete{' '}
              <strong className="font-bold text-slate-900 dark:text-white">
                {selectedHospitalToDelete?.hospitalName || 'this hospital'}
              </strong>
              {selectedHospitalToDelete?.email && (
                <span className="text-slate-500 dark:text-slate-400"> ({selectedHospitalToDelete.email})</span>
              )}
              ?
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
              This will permanently remove the hospital profile, associated account, and inventory records from the system. This action cannot be undone.
            </p>
          </div>
        }
        confirmText="Delete Hospital"
        cancelText="Cancel"
        isLoading={isDeleting}
      />
    </div>
  );
}
