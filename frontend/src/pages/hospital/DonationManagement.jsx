import React, { useState } from 'react';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useDonations } from '../../hooks/useDonations';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import HospitalStatusBadge from '../../components/hospital/common/HospitalStatusBadge';
import HospitalEmptyState from '../../components/hospital/common/HospitalEmptyState';
import { 
  Check, 
  Clipboard, 
  CheckCircle2, 
  Hospital, 
  AlertCircle, 
  RefreshCw, 
  X,
  Droplet,
  User
} from 'lucide-react';

/**
 * Hospital Donation Management Screen.
 * Modern healthcare portal design preserving 100% of existing donation confirmation and completion APIs.
 */
export default function DonationManagement() {
  const { profile, isLoading: isProfileLoading } = useHospitalProfile();
  const hospitalId = profile?.id;
  const {
    donations,
    isLoading: isDonationsLoading,
    error,
    refetch,
    confirmDonation,
    completeDonation,
    isConfirming,
    isCompleting,
  } = useDonations(hospitalId);

  const [selectedDonationId, setSelectedDonationId] = useState(null);
  const [unitsDonated, setUnitsDonated] = useState(1);
  const [remarks, setRemarks] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [actionError, setActionError] = useState('');

  if (isProfileLoading || isDonationsLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message || 'Failed to load donations.'} onRetry={refetch} />;
  }

  if (!profile) {
    return (
      <div className="max-w-2xl mx-auto py-12 font-sans">
        <HospitalCard className="text-center p-8 sm:p-12">
          <div className="flex flex-col items-center gap-4">
            <div className="h-16 w-16 rounded-3xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center border border-teal-100 dark:border-teal-900/40">
              <Hospital className="h-8 w-8" />
            </div>
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">Hospital Profile Required</h2>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 max-w-md">
              Please complete your hospital profile configuration to manage patient transfusion runs and donation records.
            </p>
          </div>
        </HospitalCard>
      </div>
    );
  }

  const handleConfirm = async (id) => {
    try {
      await confirmDonation(id);
    } catch (err) {
      alert(err.message || 'Failed to confirm donation appointment.');
    }
  };

  const handleOpenCompleteModal = (id) => {
    setSelectedDonationId(id);
    setUnitsDonated(1);
    setRemarks('');
    setIsModalOpen(true);
    setActionError('');
  };

  const handleConfirmComplete = async () => {
    setActionError('');
    try {
      await completeDonation({
        donationId: selectedDonationId,
        payload: {
          unitsDonated: parseInt(unitsDonated, 10),
          remarks,
        },
      });
      setIsModalOpen(false);
      setSelectedDonationId(null);
    } catch (err) {
      setActionError(err.message || 'Failed to complete donation.');
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

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans">
      <HospitalPageHeader
        title="Donation Tracking & Completion"
        subtitle="Confirm scheduled appointments, track transfusion logs, and issue certificates."
        icon={Clipboard}
        badge="Transfusion Management"
        breadcrumbs={[{ label: 'Donation Management' }]}
        action={
          <button
            onClick={refetch}
            disabled={isDonationsLoading}
            className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
          >
            <RefreshCw className={`h-4 w-4 ${isDonationsLoading ? 'animate-spin' : ''}`} />
            <span>Refresh Logs</span>
          </button>
        }
      />

      {/* Main Table / List */}
      {donations && donations.length > 0 ? (
        <HospitalCard bodyClassName="p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider border-b border-slate-100 dark:border-slate-800">
                <tr>
                  <th className="px-5 py-4">Donation ID</th>
                  <th className="px-5 py-4">Donor Name</th>
                  <th className="px-5 py-4">Patient Name</th>
                  <th className="px-5 py-4">Donation Date</th>
                  <th className="px-5 py-4">Units Logged</th>
                  <th className="px-5 py-4">Status</th>
                  <th className="px-5 py-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200">
                {donations.map((row) => (
                  <tr key={row.id} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors">
                    <td className="px-5 py-4 font-mono font-bold text-slate-900 dark:text-white">
                      #{row.id}
                    </td>

                    <td className="px-5 py-4 font-bold text-slate-900 dark:text-white">
                      <span className="flex items-center gap-1.5">
                        <User className="h-3.5 w-3.5 text-teal-600 dark:text-teal-400" />
                        {row.donorName || 'Registered Donor'}
                      </span>
                    </td>

                    <td className="px-5 py-4 text-slate-600 dark:text-slate-300 font-medium">
                      {row.patientName || 'Emergency Patient'}
                    </td>

                    <td className="px-5 py-4 text-slate-500 dark:text-slate-400 font-medium">
                      {formatDate(row.donationDate)}
                    </td>

                    <td className="px-5 py-4 font-bold text-slate-900 dark:text-white">
                      {row.unitsDonated ? (
                        <span className="inline-flex items-center gap-1 text-emerald-600 dark:text-emerald-400">
                          <Droplet className="h-3.5 w-3.5 fill-current" />
                          {row.unitsDonated} {row.unitsDonated === 1 ? 'Unit' : 'Units'}
                        </span>
                      ) : (
                        <span className="text-slate-400 dark:text-slate-500 italic">Pending Log</span>
                      )}
                    </td>

                    <td className="px-5 py-4">
                      <HospitalStatusBadge status={row.status} />
                    </td>

                    <td className="px-5 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        {row.status === 'SCHEDULED' && (
                          <button
                            onClick={() => handleConfirm(row.id)}
                            disabled={isConfirming}
                            className="px-3.5 py-1.5 rounded-xl bg-teal-50 dark:bg-teal-950/60 text-teal-700 dark:text-teal-300 border border-teal-200 dark:border-teal-800 font-bold text-[11px] hover:bg-teal-100 transition-colors flex items-center gap-1"
                          >
                            <Check className="h-3.5 w-3.5" />
                            <span>Confirm Appt</span>
                          </button>
                        )}

                        {row.status === 'CONFIRMED' && (
                          <button
                            onClick={() => handleOpenCompleteModal(row.id)}
                            className="px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold text-[11px] shadow-sm hover:shadow-md transition-all flex items-center gap-1"
                          >
                            <CheckCircle2 className="h-3.5 w-3.5" />
                            <span>Log Complete</span>
                          </button>
                        )}

                        {row.status === 'COMPLETED' && (
                          <span className="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/50 px-2.5 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
                            <CheckCircle2 className="h-3.5 w-3.5" /> Certificate Issued
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </HospitalCard>
      ) : (
        <HospitalEmptyState
          title="No Active Donation Runs"
          description="No confirmed or pending donation runs found for your hospital entity."
          icon={Clipboard}
        />
      )}

      {/* Completion Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 max-w-md w-full border border-slate-100 dark:border-slate-800 shadow-2xl flex flex-col gap-5 relative">
            <button
              onClick={() => setIsModalOpen(false)}
              className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <X className="h-5 w-5" />
            </button>

            <div className="flex items-center gap-3">
              <div className="h-12 w-12 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center border border-emerald-100 dark:border-emerald-900/40">
                <CheckCircle2 className="h-6 w-6" />
              </div>
              <div>
                <h3 className="font-bold text-lg text-slate-900 dark:text-white">
                  Log Donation Completion
                </h3>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  Record collected units & issue donor certificate
                </p>
              </div>
            </div>

            {actionError && (
              <div className="flex items-start gap-2 bg-red-50 dark:bg-red-950/50 text-red-600 dark:text-red-400 p-3 rounded-2xl text-xs border border-red-100 dark:border-red-900/40 font-medium">
                <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
                <span>{actionError}</span>
              </div>
            )}

            <div className="flex flex-col gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                  Units Donated (Blood Bags)
                </label>
                <input
                  type="number"
                  min={1}
                  max={5}
                  value={unitsDonated}
                  onChange={(e) => setUnitsDonated(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl text-sm bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                  Lab Notes / Clinical Remarks
                </label>
                <textarea
                  rows={3}
                  placeholder="e.g. Donation successful, screening passed."
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl text-xs bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>
            </div>

            <div className="flex items-center gap-3 justify-end pt-2">
              <button
                onClick={() => setIsModalOpen(false)}
                disabled={isCompleting}
                className="px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 font-bold text-xs hover:bg-slate-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmComplete}
                disabled={isCompleting}
                className="px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-md transition-all flex items-center gap-2"
              >
                {isCompleting ? (
                  <span>Logging...</span>
                ) : (
                  <>
                    <CheckCircle2 className="h-4 w-4" />
                    <span>Confirm & Issue Certificate</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
