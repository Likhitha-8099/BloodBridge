import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, Trash2, Building2, Mail, Phone, MapPin, 
  ShieldCheck, CheckCircle2, XCircle, Clock, 
  ExternalLink, UserCheck
} from 'lucide-react';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import useToastStore from '../../store/toastStore';
import adminService from '../../services/adminService';

/**
 * Dedicated Admin Hospital Details Page.
 * Displays full hospital registration data, license verification controls,
 * contact details, and permanent deletion.
 */
export default function HospitalDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToastStore();

  const [hospital, setHospital] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Deletion Modal State
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  // Verification Action State
  const [isVerifying, setIsVerifying] = useState(false);

  const fetchHospitalDetails = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getHospitalById(id);
      setHospital(data);
    } catch (err) {
      console.error('Failed to load hospital details:', err);
      setError(err?.response?.data?.message || err?.message || 'Unable to load hospital details. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchHospitalDetails();
    }
  }, [id]);

  const handleVerifyAction = async (targetStatus) => {
    if (!hospital) return;
    try {
      setIsVerifying(true);
      await adminService.verifyHospital(hospital.id, targetStatus, `Reviewed by Admin on ${new Date().toLocaleDateString()}`);
      addToast(`Hospital status updated to ${targetStatus} successfully.`, 'success');
      fetchHospitalDetails();
    } catch (err) {
      console.error('Verification error:', err);
      const errMsg = err?.response?.data?.message || err?.message || 'Failed to update verification status.';
      addToast(errMsg, 'error');
    } finally {
      setIsVerifying(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!hospital) return;
    const hospitalId = hospital.id ?? hospital.userId ?? id;
    const hospitalName = hospital.hospitalName || 'Hospital';

    console.log('[DELETE HOSPITAL] Deleting from Details page:', { hospitalId, hospitalName });

    if (!hospitalId || hospitalId === 'undefined' || hospitalId === 'null') {
      console.error('[DELETE HOSPITAL ERROR] Missing hospital ID:', hospital);
      addToast('Unable to delete hospital: hospital ID is missing.', 'error');
      return;
    }

    try {
      setIsDeleting(true);
      await adminService.deleteHospital(hospitalId);
      addToast(`Hospital "${hospitalName}" was permanently deleted successfully.`, 'success');
      setShowDeleteModal(false);
      navigate('/admin/hospitals');
    } catch (err) {
      console.error('Failed to delete hospital:', err);
      const errMsg = err?.response?.data?.message || err?.message || 'Unable to delete hospital.';
      addToast(errMsg, 'error');
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error || !hospital) {
    return (
      <div className="p-6 md:p-8 max-w-5xl mx-auto space-y-6">
        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate('/admin/hospitals')}
          className="flex items-center gap-2 text-xs"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Hospitals
        </Button>
        <ErrorState message={error || "Hospital profile not found."} onRetry={fetchHospitalDetails} />
      </div>
    );
  }

  const status = (hospital.verificationStatus || (hospital.verified ? 'APPROVED' : 'PENDING')).toUpperCase();

  return (
    <div className="p-6 md:p-8 max-w-6xl mx-auto space-y-6 animate-fadeIn transition-colors duration-150">
      {/* NAVIGATION & ACTION HEADER */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate('/admin/hospitals')}
          className="flex items-center gap-2 text-xs w-fit dark:border-slate-700 dark:bg-slate-800"
        >
          <ArrowLeft className="h-4 w-4 text-slate-500" />
          Back to Hospitals
        </Button>

        <div className="flex items-center gap-3">
          {status !== 'APPROVED' && (
            <Button
              variant="success"
              size="sm"
              onClick={() => handleVerifyAction('APPROVED')}
              isLoading={isVerifying}
              className="flex items-center gap-1.5 text-xs bg-emerald-600 hover:bg-emerald-700 text-white"
            >
              <CheckCircle2 className="h-4 w-4" />
              Approve Hospital
            </Button>
          )}

          {status !== 'REJECTED' && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleVerifyAction('REJECTED')}
              isLoading={isVerifying}
              className="flex items-center gap-1.5 text-xs border-rose-300 text-rose-600 hover:bg-rose-50 dark:border-rose-800 dark:text-rose-400 dark:hover:bg-rose-950/40"
            >
              <XCircle className="h-4 w-4" />
              Reject Hospital
            </Button>
          )}

          <Button
            variant="danger"
            size="sm"
            onClick={() => setShowDeleteModal(true)}
            className="flex items-center gap-2 text-xs bg-red-600 hover:bg-red-700 text-white"
          >
            <Trash2 className="h-4 w-4" />
            Delete Hospital
          </Button>
        </div>
      </div>

      {/* HOSPITAL HERO PROFILE CARD */}
      <Card className="p-6 md:p-8 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="flex items-start gap-4">
            <div className="h-16 w-16 rounded-2xl bg-blue-50 dark:bg-blue-950/70 border border-blue-200 dark:border-blue-800 flex items-center justify-center flex-shrink-0 text-blue-600 dark:text-blue-400">
              <Building2 className="h-8 w-8" />
            </div>
            <div className="space-y-1">
              <div className="flex items-center gap-2 flex-wrap">
                <h1 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight">
                  {hospital.hospitalName}
                </h1>
                <span className="font-mono text-xs font-bold text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded-md">
                  #{hospital.id ?? id}
                </span>
              </div>
              <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500 dark:text-slate-400 font-medium">
                {hospital.email && (
                  <span className="flex items-center gap-1.5 font-mono">
                    <Mail className="h-3.5 w-3.5 text-slate-400" />
                    {hospital.email}
                  </span>
                )}
                {hospital.phoneNumber && (
                  <span className="flex items-center gap-1.5 font-mono">
                    <Phone className="h-3.5 w-3.5 text-slate-400" />
                    {hospital.phoneNumber}
                  </span>
                )}
                {(hospital.city || hospital.state) && (
                  <span className="flex items-center gap-1.5">
                    <MapPin className="h-3.5 w-3.5 text-slate-400" />
                    {hospital.city || 'N/A'}{hospital.state ? `, ${hospital.state}` : ''}
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-bold uppercase border ${
              status === 'APPROVED' 
                ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800'
                : status === 'PENDING'
                ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300 border-amber-200 dark:border-amber-800'
                : 'bg-rose-50 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300 border-rose-200 dark:border-rose-800'
            }`}>
              {status === 'APPROVED' ? <CheckCircle2 className="h-3.5 w-3.5" /> : status === 'PENDING' ? <Clock className="h-3.5 w-3.5" /> : <XCircle className="h-3.5 w-3.5" />}
              {status}
            </span>
          </div>
        </div>
      </Card>

      {/* DETAILS GRID */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* 1. REGISTRATION & CREDENTIALS */}
        <Card className="p-6 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl space-y-4">
          <h2 className="text-sm font-extrabold text-slate-900 dark:text-white uppercase tracking-wider flex items-center gap-2 border-b border-slate-100 dark:border-slate-800 pb-3">
            <ShieldCheck className="h-4 w-4 text-blue-500" />
            Registration & License
          </h2>
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Hospital Type</span>
              <span className="font-bold text-slate-900 dark:text-white">{hospital.hospitalType || 'GENERAL'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Registration No.</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white">{hospital.registrationNumber || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">License No.</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white">{hospital.licenseNumber || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">License Document</span>
              {hospital.licenseDocumentUrl ? (
                <a
                  href={hospital.licenseDocumentUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="font-bold text-blue-600 dark:text-blue-400 flex items-center gap-1 hover:underline"
                >
                  View Document <ExternalLink className="h-3 w-3" />
                </a>
              ) : (
                <span className="text-slate-400 italic">Not Uploaded</span>
              )}
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Operating Hours</span>
              <span className="font-bold text-slate-900 dark:text-white">{hospital.operatingHours || '24/7'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Emergency Active</span>
              <span className="font-bold text-slate-900 dark:text-white">{hospital.emergencyAvailable ? 'Yes' : 'No'}</span>
            </div>
          </div>
        </Card>

        {/* 2. CONTACT & ADMINISTRATIVE */}
        <Card className="p-6 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl space-y-4">
          <h2 className="text-sm font-extrabold text-slate-900 dark:text-white uppercase tracking-wider flex items-center gap-2 border-b border-slate-100 dark:border-slate-800 pb-3">
            <UserCheck className="h-4 w-4 text-emerald-500" />
            Contact & Admin Info
          </h2>
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Contact Person</span>
              <span className="font-bold text-slate-900 dark:text-white">{hospital.contactPerson || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Official Email</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white truncate max-w-[180px]">{hospital.email || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Phone Number</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white">{hospital.phoneNumber || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Official Website</span>
              {hospital.website ? (
                <a
                  href={hospital.website.startsWith('http') ? hospital.website : `https://${hospital.website}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="font-bold text-blue-600 dark:text-blue-400 flex items-center gap-1 hover:underline truncate max-w-[180px]"
                >
                  {hospital.website} <ExternalLink className="h-3 w-3 flex-shrink-0" />
                </a>
              ) : (
                <span className="text-slate-400">—</span>
              )}
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Registered On</span>
              <span className="font-medium text-slate-900 dark:text-white">
                {hospital.createdAt ? new Date(hospital.createdAt).toLocaleDateString() : '—'}
              </span>
            </div>
          </div>
        </Card>

        {/* 3. LOCATION & GEO-COORDINATES */}
        <Card className="p-6 border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm rounded-2xl space-y-4">
          <h2 className="text-sm font-extrabold text-slate-900 dark:text-white uppercase tracking-wider flex items-center gap-2 border-b border-slate-100 dark:border-slate-800 pb-3">
            <MapPin className="h-4 w-4 text-purple-500" />
            Location & Coordinates
          </h2>
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">City / State</span>
              <span className="font-bold text-slate-900 dark:text-white">{hospital.city || '—'}{hospital.state ? `, ${hospital.state}` : ''}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Country</span>
              <span className="font-bold text-slate-900 dark:text-white">{hospital.country || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Postal Code</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white">{hospital.postalCode || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Address</span>
              <span className="font-medium text-slate-900 dark:text-white text-right max-w-[180px]">{hospital.address || '—'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-50 dark:border-slate-800/40">
              <span className="text-slate-500 dark:text-slate-400">Coordinates</span>
              <span className="font-mono text-slate-900 dark:text-white">
                {hospital.latitude ? `${hospital.latitude.toFixed(4)}, ${hospital.longitude?.toFixed(4)}` : '—'}
              </span>
            </div>
            {hospital.remarks && (
              <div className="pt-2">
                <span className="text-slate-500 dark:text-slate-400 block mb-1">Verification Remarks:</span>
                <p className="text-slate-800 dark:text-slate-200 bg-slate-50 dark:bg-slate-800/60 p-2 rounded-lg font-mono text-[11px]">
                  {hospital.remarks}
                </p>
              </div>
            )}
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
        title="Permanently Delete Hospital?"
        message={
          <div className="flex flex-col gap-2">
            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed font-medium">
              Are you sure you want to permanently delete{' '}
              <strong className="font-bold text-slate-900 dark:text-white">
                {hospital.hospitalName}
              </strong>
              {hospital.email && (
                <span className="text-slate-500 dark:text-slate-400"> ({hospital.email})</span>
              )}
              ?
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
              This will permanently remove the hospital profile, associated account credentials, blood inventory stock, and emergency request logs from the database. This action cannot be undone.
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
