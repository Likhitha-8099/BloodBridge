import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, Trash2, User, Mail, Phone, MapPin, Activity
} from 'lucide-react';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import useToastStore from '../../store/toastStore';
import adminService from '../../services/adminService';

/**
 * Dedicated Admin Donor Details Page.
 * Displays comprehensive medical, contact, location, and donation stats for a specific donor.
 */
export default function DonorDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToastStore();

  const [donor, setDonor] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Deletion Modal State
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchDonorDetails = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getDonorById(id);
      setDonor(data);
    } catch (err) {
      console.error('Failed to load donor details:', err);
      setError(err?.response?.data?.message || err?.message || 'Unable to load donor details. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchDonorDetails();
    }
  }, [id]);

  const handleConfirmDelete = async () => {
    if (!donor) return;
    const donorId = donor.id ?? donor.donorId ?? donor.userId ?? id;
    const donorName = donor.fullName || donor.donorName || donor.name || 'Donor';

    console.log('[DELETE DONOR] Deleting from Details page:', { donorId, donorName });

    if (!donorId || donorId === 'undefined' || donorId === 'null') {
      console.error('[DELETE DONOR ERROR] Missing donor ID:', donor);
      addToast('Unable to delete donor: donor ID is missing.', 'error');
      return;
    }

    try {
      setIsDeleting(true);
      await adminService.deleteDonor(donorId);
      addToast(`Donor "${donorName}" was permanently deleted successfully.`, 'success');
      setShowDeleteModal(false);
      navigate('/admin/donors');
    } catch (err) {
      console.error('Failed to delete donor:', err);
      const errMsg = err?.response?.data?.message || err?.message || 'Unable to delete donor.';
      addToast(errMsg, 'error');
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error || !donor) {
    return (
      <div className="p-6 md:p-8 max-w-5xl mx-auto space-y-6">
        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate('/admin/donors')}
          className="flex items-center gap-2 text-xs"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Donors
        </Button>
        <ErrorState message={error || "Donor profile not found."} onRetry={fetchDonorDetails} />
      </div>
    );
  }

  const bgFormatted = (donor.bloodGroup || 'N/A').replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  const displayName = donor.fullName || donor.donorName || donor.name || 'Registered Donor';
  const isAvailable = Boolean(donor.availableForDonation);
  const isEmergency = Boolean(donor.emergencyAvailable);

  return (
    <div className="p-6 md:p-8 max-w-6xl mx-auto space-y-6 animate-fadeIn transition-colors duration-150">
      {/* NAVIGATION & ACTION HEADER */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate('/admin/donors')}
          className="flex items-center gap-2 text-xs w-fit dark:border-slate-700 dark:bg-slate-800"
        >
          <ArrowLeft className="h-4 w-4 text-slate-500" />
          Back to Donors
        </Button>

        <Button
          variant="danger"
          size="sm"
          onClick={() => setShowDeleteModal(true)}
          className="flex items-center gap-2 text-xs bg-red-600 hover:bg-red-700 text-white w-fit"
        >
          <Trash2 className="h-4 w-4" />
          Delete Donor
        </Button>
      </div>

      {/* DONOR HERO PROFILE CARD */}
      <Card className="p-6 md:p-8 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="flex items-start gap-4">
            <div className="h-16 w-16 rounded-2xl bg-red-100 dark:bg-red-950/70 border border-red-200 dark:border-red-800 flex items-center justify-center flex-shrink-0 text-red-600 dark:text-red-400 font-extrabold text-2xl">
              {bgFormatted}
            </div>
            <div className="space-y-1">
              <div className="flex items-center gap-2 flex-wrap">
                <h1 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight">
                  {displayName}
                </h1>
                <span className="font-mono text-xs font-bold text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded-md">
                  #{donor.id ?? donor.donorId ?? id}
                </span>
              </div>
              <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500 dark:text-slate-400 font-medium">
                {donor.email && (
                  <span className="flex items-center gap-1.5 font-mono">
                    <Mail className="h-3.5 w-3.5 text-slate-400" />
                    {donor.email}
                  </span>
                )}
                {(donor.phoneNumber || donor.phone) && (
                  <span className="flex items-center gap-1.5 font-mono">
                    <Phone className="h-3.5 w-3.5 text-slate-400" />
                    {donor.phoneNumber || donor.phone}
                  </span>
                )}
                {(donor.city || donor.state) && (
                  <span className="flex items-center gap-1.5">
                    <MapPin className="h-3.5 w-3.5 text-slate-400" />
                    {donor.city || 'N/A'}{donor.state ? `, ${donor.state}` : ''}
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <span className={`px-3 py-1 rounded-lg text-xs font-bold uppercase border ${
              isAvailable 
                ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800'
                : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400 border-slate-200 dark:border-slate-700'
            }`}>
              {isAvailable ? 'Available for Donation' : 'Unavailable'}
            </span>

            {isEmergency && (
              <span className="px-3 py-1 rounded-lg text-xs font-extrabold bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300 border border-amber-200 dark:border-amber-800">
                Emergency Ready
              </span>
            )}
          </div>
        </div>
      </Card>

      {/* PROFILE DETAILS GRID */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* 1. PERSONAL INFORMATION */}
        <Card className="p-6 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl space-y-4">
          <h2 className="text-sm font-extrabold text-slate-900 dark:text-white uppercase tracking-wider flex items-center gap-2 border-b border-slate-100 dark:border-slate-800 pb-3">
            <User className="h-4 w-4 text-red-500" />
            Personal Details
          </h2>
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Full Name</span>
              <span className="font-bold text-slate-900 dark:text-white">{displayName}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Gender</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.gender || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Age</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.age ? `${donor.age} yrs` : '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Date of Birth</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.dateOfBirth || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Occupation</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.occupation || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Govt ID Type</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.govtIdType || '—'}</span>
            </div>
          </div>
        </Card>

        {/* 2. CONTACT & LOCATION */}
        <Card className="p-6 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl space-y-4">
          <h2 className="text-sm font-extrabold text-slate-900 dark:text-white uppercase tracking-wider flex items-center gap-2 border-b border-slate-100 dark:border-slate-800 pb-3">
            <MapPin className="h-4 w-4 text-blue-500" />
            Contact & Address
          </h2>
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Email</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white truncate max-w-[180px]">{donor.email || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Primary Phone</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white">{donor.phoneNumber || donor.phone || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Alternate Phone</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white">{donor.alternatePhoneNumber || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">City / State</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.city || '—'}{donor.state ? `, ${donor.state}` : ''}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Postal Code</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white">{donor.postalCode || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Address</span>
              <span className="font-medium text-slate-900 dark:text-white text-right max-w-[180px]">{donor.address || '—'}</span>
            </div>
          </div>
        </Card>

        {/* 3. HEALTH & DONATION METRICS */}
        <Card className="p-6 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl space-y-4">
          <h2 className="text-sm font-extrabold text-slate-900 dark:text-white uppercase tracking-wider flex items-center gap-2 border-b border-slate-100 dark:border-slate-800 pb-3">
            <Activity className="h-4 w-4 text-emerald-500" />
            Vitals & Stats
          </h2>
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Total Donations</span>
              <span className="font-black text-emerald-600 dark:text-emerald-400">{donor.totalDonations ?? 0}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Estimated Lives Saved</span>
              <span className="font-black text-red-600 dark:text-red-400">{donor.livesSaved ?? 0}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Weight / Height</span>
              <span className="font-bold text-slate-900 dark:text-white">
                {donor.weight ? `${donor.weight} kg` : '—'} / {donor.height ? `${donor.height} cm` : '—'}
              </span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Hemoglobin</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.hemoglobin ? `${donor.hemoglobin} g/dL` : '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Blood Pressure</span>
              <span className="font-bold text-slate-900 dark:text-white">{donor.bloodPressure || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Registered On</span>
              <span className="font-medium text-slate-900 dark:text-white">
                {donor.createdAt ? new Date(donor.createdAt).toLocaleDateString() : '—'}
              </span>
            </div>
          </div>
        </Card>
      </div>

      {/* CONFIRMATION MODAL */}
      <ConfirmationModal
        isOpen={showDeleteModal}
        onClose={() => {
          if (!isDeleting) setShowDeleteModal(false);
        }}
        onConfirm={handleConfirmDelete}
        title="Delete Donor?"
        message={
          <div className="flex flex-col gap-2">
            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed font-medium">
              Are you sure you want to permanently delete{' '}
              <strong className="font-bold text-slate-900 dark:text-white">
                {displayName}
              </strong>
              {donor.email && (
                <span className="text-slate-500 dark:text-slate-400"> ({donor.email})</span>
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
