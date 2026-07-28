import React, { useState } from 'react';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useDonations } from '../../hooks/useDonations';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Card from '../../components/ui/Card';
import { Check, Clipboard, CheckCircle, Hospital, AlertCircle } from 'lucide-react';

/**
 * Screen enabling hospitals to trace matched donations, confirm schedules, and log final units.
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
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  // Profile setup check
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed rounded-3xl">
          <Hospital className="h-10 w-10 text-primary" />
          <h2 className="text-lg font-bold text-gray-800">Hospital Profile Required</h2>
          <p className="text-xs text-gray-500">
            Please configure your hospital profile to list and manage patient matching donations.
          </p>
        </Card>
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

  const columns = [
    {
      header: 'Donation ID',
      field: 'id',
    },
    {
      header: 'Donor Name',
      field: 'donorName',
    },
    {
      header: 'Patient Name',
      field: 'patientName',
    },
    {
      header: 'Donation Date',
      render: (row) => <span>{formatDate(row.donationDate)}</span>,
    },
    {
      header: 'Units',
      render: (row) => <span>{row.unitsDonated || 'Pending'}</span>,
    },
    {
      header: 'Status',
      render: (row) => {
        const colors = {
          SCHEDULED: 'bg-yellow-50 text-yellow-750 border-yellow-200',
          CONFIRMED: 'bg-blue-50 text-blue-750 border-blue-200',
          COMPLETED: 'bg-green-50 text-green-750 border-green-200',
          CANCELLED: 'bg-gray-100 text-gray-600 border-gray-200',
        };
        const style = colors[row.status] || 'bg-gray-50 text-gray-655';
        return (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border uppercase tracking-wider ${style}`}>
            {row.status}
          </span>
        );
      },
    },
    {
      header: 'Actions',
      render: (row) => {
        if (row.status === 'SCHEDULED') {
          return (
            <Button
              onClick={() => handleConfirm(row.id)}
              disabled={isConfirming}
              variant="outline"
              className="py-1 px-3 text-[10px] uppercase font-bold flex items-center gap-1"
            >
              <Check className="h-3 w-3" /> Confirm Appt
            </Button>
          );
        }
        if (row.status === 'CONFIRMED') {
          return (
            <Button
              onClick={() => handleOpenCompleteModal(row.id)}
              variant="primary"
              className="py-1 px-3 text-[10px] uppercase font-bold flex items-center gap-1"
            >
              <CheckCircle className="h-3 w-3" /> Complete Log
            </Button>
          );
        }
        return <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Completed</span>;
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Donation Tracking</h1>
        <p className="text-xs text-gray-500 mt-1">
          Track donor matches, confirm appointments, and log completed transfusions.
        </p>
      </div>

      {donations && donations.length > 0 ? (
        <DataTable
          columns={columns}
          data={donations}
          keyField="id"
          emptyMessage="No donations logged at this hospital."
        />
      ) : (
        <EmptyState
          message="No active donation runs found for your hospital."
          icon={Clipboard}
        />
      )}

      {/* Complete Donation modal popup */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full border border-gray-150 shadow-xl flex flex-col gap-4">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-50 pb-2">
              Log Donation Completion
            </h3>

            {actionError && (
              <div className="flex items-start gap-1 bg-red-50 text-red-650 p-2.5 rounded-lg text-[11px] border border-red-100">
                <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
                <span>{actionError}</span>
              </div>
            )}

            <div className="flex flex-col gap-3">
              <Input
                label="Units Donated (Bags)"
                type="number"
                value={unitsDonated}
                min={1}
                onChange={(e) => setUnitsDonated(e.target.value)}
              />

              <div className="flex flex-col gap-1.5 w-full">
                <label className="text-xs font-semibold text-gray-600 tracking-wide">Remarks / Lab Notes</label>
                <textarea
                  rows={2}
                  placeholder="e.g. Donation successful, blood checked."
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  className="w-full px-4 py-2 rounded-xl border border-gray-200 text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-red-100 bg-white"
                />
              </div>
            </div>

            <div className="flex items-center gap-3 justify-end mt-2">
              <Button 
                onClick={() => setIsModalOpen(false)} 
                variant="outline" 
                className="py-2 px-4 text-xs" 
                disabled={isCompleting}
              >
                Cancel
              </Button>
              <Button 
                onClick={handleConfirmComplete} 
                variant="primary" 
                className="py-2 px-4 text-xs" 
                isLoading={isCompleting}
              >
                Log Success
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
