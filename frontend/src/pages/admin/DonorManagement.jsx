import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Users, Search, Trash2, Eye, MapPin, Phone, Mail, 
  Heart, ShieldAlert, RefreshCw 
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
 * Dedicated Admin Donor Management Page.
 * Displays all registered donors from the real database, with live real-time sync,
 * robust search, blood group filtering, view details navigation, and permanent deletion.
 */
export default function DonorManagement() {
  const navigate = useNavigate();
  const { addToast } = useToastStore();

  const [donors, setDonors] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Search and Filter State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedBloodGroup, setSelectedBloodGroup] = useState('ALL');
  const [selectedAvailability, setSelectedAvailability] = useState('ALL');

  // Deletion Modal State
  const [selectedDonorToDelete, setSelectedDonorToDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // WebSocket Live Subscription
  const wsTopics = useMemo(() => ['/topic/admin/users', '/topic/admin/donors', '/topic/admin/dashboard'], []);
  useWebSocket(wsTopics, () => {
    console.log('⚡ Real-time donor update event received, refreshing donor list...');
    fetchDonors(false);
  });

  const fetchDonors = async (showLoading = true) => {
    if (showLoading) setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getAllDonors();
      setDonors(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load registered donors:', err);
      setError(err?.response?.data?.message || err?.message || 'Unable to load donors. Please try again.');
    } finally {
      if (showLoading) setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDonors(true);
  }, []);

  // Filtered Donors
  const filteredDonors = useMemo(() => {
    return donors.filter((d) => {
      const searchLower = searchQuery.toLowerCase();
      const name = (d.fullName || d.donorName || d.name || '').toLowerCase();
      const email = (d.email || '').toLowerCase();
      const phone = (d.phoneNumber || d.phone || '').toLowerCase();
      const city = (d.city || '').toLowerCase();
      const state = (d.state || '').toLowerCase();

      const matchesSearch = !searchQuery || 
        name.includes(searchLower) || 
        email.includes(searchLower) || 
        phone.includes(searchLower) || 
        city.includes(searchLower) || 
        state.includes(searchLower);

      const rawBg = d.bloodGroup || '';
      const formattedBg = rawBg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
      const matchesBg = selectedBloodGroup === 'ALL' || formattedBg === selectedBloodGroup || rawBg === selectedBloodGroup;

      const matchesAvailability = selectedAvailability === 'ALL' ||
        (selectedAvailability === 'AVAILABLE' && Boolean(d.availableForDonation)) ||
        (selectedAvailability === 'UNAVAILABLE' && !d.availableForDonation) ||
        (selectedAvailability === 'EMERGENCY' && Boolean(d.emergencyAvailable));

      return matchesSearch && matchesBg && matchesAvailability;
    });
  }, [donors, searchQuery, selectedBloodGroup, selectedAvailability]);

  // Summary Metrics
  const totalCount = donors.length;
  const availableCount = donors.filter(d => Boolean(d.availableForDonation)).length;
  const emergencyCount = donors.filter(d => Boolean(d.emergencyAvailable)).length;

  const handleConfirmDelete = async () => {
    if (!selectedDonorToDelete) return;
    const donorId = selectedDonorToDelete.id ?? selectedDonorToDelete.donorId ?? selectedDonorToDelete.userId;
    const donorName = selectedDonorToDelete.fullName || selectedDonorToDelete.donorName || selectedDonorToDelete.name || selectedDonorToDelete.email || 'Donor';

    console.log('[DELETE DONOR] Selected donor:', selectedDonorToDelete);
    console.log('[DELETE DONOR] Resolved donor ID:', donorId);

    if (donorId === undefined || donorId === null || donorId === 'undefined' || donorId === 'null') {
      console.error('[DELETE DONOR ERROR] Unable to delete donor: donor ID is missing.', selectedDonorToDelete);
      addToast('Unable to delete donor: donor ID is missing.', 'error');
      return;
    }

    try {
      setIsDeleting(true);
      await adminService.deleteDonor(donorId);
      addToast(`Donor "${donorName}" was permanently deleted successfully.`, 'success');
      setSelectedDonorToDelete(null);
      fetchDonors(false);
    } catch (err) {
      console.error('Failed to delete donor:', err);
      const errMsg = err?.response?.data?.message || err?.message || 'Unable to delete donor.';
      addToast(errMsg, 'error');
    } finally {
      setIsDeleting(false);
    }
  };

  const donorColumns = [
    {
      header: 'Donor ID',
      render: (row) => (
        <span className="font-mono text-xs font-bold text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-1 rounded-md">
          #{row.id ?? row.donorId ?? '—'}
        </span>
      )
    },
    {
      header: 'Name',
      render: (row) => {
        const displayName = row.fullName || row.donorName || row.name || 'Registered Donor';
        return (
          <div className="flex flex-col">
            <span className="font-bold text-slate-900 dark:text-white text-sm tracking-tight">
              {displayName}
            </span>
            {row.email && (
              <span className="text-[11px] text-slate-500 dark:text-slate-400 font-mono mt-0.5 flex items-center gap-1">
                <Mail className="h-3 w-3 text-slate-400" />
                {row.email}
              </span>
            )}
          </div>
        );
      }
    },
    {
      header: 'Phone',
      render: (row) => (
        <span className="text-xs text-slate-600 dark:text-slate-300 font-mono flex items-center gap-1">
          <Phone className="h-3 w-3 text-slate-400" />
          {row.phoneNumber || row.phone || row.alternatePhoneNumber || '—'}
        </span>
      )
    },
    {
      header: 'Blood Group',
      render: (row) => (
        <span className="px-2.5 py-0.5 rounded-full text-xs font-black bg-red-50 text-red-700 dark:bg-red-950/70 dark:text-red-300 border border-red-200 dark:border-red-800 inline-block">
          {(row.bloodGroup || 'N/A').replace('_POSITIVE', '+').replace('_NEGATIVE', '-')}
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
        const isAvailable = Boolean(row.availableForDonation);
        const isEmergency = Boolean(row.emergencyAvailable);
        return (
          <div className="flex flex-col gap-1 items-start">
            <span className={`px-2 py-0.5 rounded-md text-[10px] font-bold uppercase border ${
              isAvailable 
                ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800'
                : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400 border-slate-200 dark:border-slate-700'
            }`}>
              {isAvailable ? 'Available' : 'Unavailable'}
            </span>
            {isEmergency && (
              <span className="px-1.5 py-0.2 rounded text-[9px] font-extrabold bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300 border border-amber-200 dark:border-amber-800">
                Emergency Ready
              </span>
            )}
          </div>
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
        const donorId = row.id ?? row.donorId ?? row.userId;
        return (
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate(`/admin/donors/${donorId}`)}
              className="flex items-center gap-1.5 text-xs py-1 px-2.5 h-8 dark:border-slate-700 dark:hover:bg-slate-800"
              title="View Donor Details"
            >
              <Eye className="h-3.5 w-3.5 text-blue-600 dark:text-blue-400" />
              <span>View</span>
            </Button>

            <Button
              variant="danger"
              size="sm"
              onClick={() => setSelectedDonorToDelete(row)}
              className="flex items-center gap-1 text-xs py-1 px-2.5 h-8 bg-red-600 hover:bg-red-700 text-white"
              title="Permanently Delete Donor"
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
    return <ErrorState message={error} onRetry={() => fetchDonors(true)} />;
  }

  return (
    <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-6 animate-fadeIn transition-colors duration-150">
      {/* PAGE HEADER */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-slate-900 dark:text-white tracking-tight flex items-center gap-3">
            <Users className="h-8 w-8 text-red-600 dark:text-red-500" />
            Donor Management
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1 font-medium">
            Manage all registered blood donors in the BloodBridge AI system.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={() => fetchDonors(true)}
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
          title="Total Registered Donors"
          value={totalCount}
          subtitle="Registered donor profiles"
          icon={Users}
          variant="default"
        />
        <StatCard
          title="Available for Donation"
          value={availableCount}
          subtitle="Currently active & ready"
          icon={Heart}
          variant="success"
        />
        <StatCard
          title="Emergency Ready Donors"
          value={emergencyCount}
          subtitle="On-call for critical requests"
          icon={ShieldAlert}
          variant="warning"
        />
      </div>

      {/* DONOR TABLE CARD */}
      <Card className="border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl overflow-hidden">
        {/* TABLE FILTER CONTROLS */}
        <div className="p-5 border-b border-slate-100 dark:border-slate-800/80 bg-slate-50/50 dark:bg-slate-800/30">
          <div className="grid grid-cols-1 sm:grid-cols-12 gap-3">
            <div className="sm:col-span-6 relative">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 dark:text-slate-500" />
              <input
                type="text"
                placeholder="Search donors by name, email, phone, city..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-500 transition-colors"
              />
            </div>

            <div className="sm:col-span-3">
              <select
                value={selectedBloodGroup}
                onChange={(e) => setSelectedBloodGroup(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-500 transition-colors"
              >
                <option value="ALL">All Blood Groups</option>
                <option value="A+">A+</option>
                <option value="A-">A-</option>
                <option value="B+">B+</option>
                <option value="B-">B-</option>
                <option value="AB+">AB+</option>
                <option value="AB-">AB-</option>
                <option value="O+">O+</option>
                <option value="O-">O-</option>
              </select>
            </div>

            <div className="sm:col-span-3">
              <select
                value={selectedAvailability}
                onChange={(e) => setSelectedAvailability(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-500 transition-colors"
              >
                <option value="ALL">All Availabilities</option>
                <option value="AVAILABLE">Available Only</option>
                <option value="UNAVAILABLE">Unavailable Only</option>
                <option value="EMERGENCY">Emergency Ready Only</option>
              </select>
            </div>
          </div>
        </div>

        {/* DATA TABLE */}
        <div className="p-0">
          <DataTable
            columns={donorColumns}
            data={filteredDonors}
            keyField="id"
            emptyMessage="No registered donors found."
          />
        </div>
      </Card>

      {/* CONFIRMATION MODAL */}
      <ConfirmationModal
        isOpen={Boolean(selectedDonorToDelete)}
        onClose={() => {
          if (!isDeleting) setSelectedDonorToDelete(null);
        }}
        onConfirm={handleConfirmDelete}
        title="Delete Donor?"
        message={
          <div className="flex flex-col gap-2">
            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed font-medium">
              Are you sure you want to permanently delete{' '}
              <strong className="font-bold text-slate-900 dark:text-white">
                {selectedDonorToDelete?.fullName || selectedDonorToDelete?.donorName || selectedDonorToDelete?.name || 'this donor'}
              </strong>
              {selectedDonorToDelete?.email && (
                <span className="text-slate-500 dark:text-slate-400"> ({selectedDonorToDelete.email})</span>
              )}
              ?
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
              This will permanently delete this donor and all donor-specific data from BloodBridge. This action cannot be undone.
            </p>
          </div>
        }
        confirmText="Delete Donor"
        cancelText="Cancel"
        isLoading={isDeleting}
      />
    </div>
  );
}
