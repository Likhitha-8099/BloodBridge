import React, { useState, useMemo } from 'react';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import { useDonationHistory } from '../../hooks/useDonationHistory';
import { useWebSocket } from '../../hooks/useWebSocket';
import useAuthStore from '../../store/authStore';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Timeline from '../../components/ui/Timeline';
import { Link } from 'react-router-dom';
import { ClipboardList, Award, Download, X, Wifi, WifiOff } from 'lucide-react';
import donationService from '../../services/donationService';

/**
 * Interactive Timeline & Donation History Page for Donors.
 * Real-time STOMP WebSocket listening ensures immediate zero-refresh updates on verified donations.
 */
export default function DonationHistory() {
  const { user } = useAuthStore();
  const { 
    profile, 
    isLoading: isProfileLoading, 
    error: profileError 
  } = useDonorProfile();

  const donorId = profile?.id;
  const { 
    data, 
    isLoading: isHistoryLoading, 
    error: historyError, 
    refetch 
  } = useDonationHistory(donorId);

  const [selectedCertificate, setSelectedCertificate] = useState(null);

  const topics = useMemo(() => {
    const list = [];
    if (user?.id) list.push(`/topic/notifications/${user.id}`);
    if (donorId) list.push(`/topic/donor/${donorId}`);
    return list;
  }, [user?.id, donorId]);

  const { isConnected, isFallback } = useWebSocket(topics, () => {
    console.log('⚡ Real-time Donation History update received, refetching history...');
    refetch();
  });

  const isLoading = isProfileLoading || isHistoryLoading;
  const error = profileError || historyError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed border-gray-200 rounded-3xl shadow-sm">
          <div className="p-4 bg-red-50 text-primary rounded-2xl border border-red-100">
            <ClipboardList className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">No Donor Profile Setup</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              Create a donor profile to track donation histories, download certificates, and manage compatibility schedules.
            </p>
          </div>
          <Link to="/donor/profile/edit">
            <Button variant="primary" className="px-6 py-2.5">Create Profile Now</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 font-sans">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-3">
            Donation Timeline & History
            {isConnected ? (
              <span className="flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400 font-semibold bg-emerald-50 dark:bg-emerald-950/50 px-2.5 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
                <Wifi className="h-3.5 w-3.5" /> Live
              </span>
            ) : (
              <span className="flex items-center gap-1.5 text-xs text-amber-600 dark:text-amber-400 font-semibold bg-amber-50 dark:bg-amber-950/50 px-2.5 py-1 rounded-full border border-amber-200 dark:border-amber-800">
                <WifiOff className="h-3.5 w-3.5" /> {isFallback ? 'REST Fallback' : 'Reconnecting...'}
              </span>
            )}
          </h1>
          <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">
            Track your life-saving blood donation milestones and official certificates.
          </p>
        </div>
      </div>

      {data && data.length > 0 ? (
        <div className="flex flex-col gap-6">
          <Card className="p-6 overflow-hidden">
            <h3 className="text-base font-bold text-gray-900 dark:text-white mb-4">Donation Records</h3>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="border-b border-slate-200 dark:border-slate-800 text-slate-400 font-bold uppercase tracking-wider bg-slate-50 dark:bg-slate-800/50">
                    <th className="py-3 px-4">Date</th>
                    <th className="py-3 px-4">Hospital</th>
                    <th className="py-3 px-4">Blood Group</th>
                    <th className="py-3 px-4">Units</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-center">Certificate</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {data.map((item) => {
                    const isCompleted = item.status === 'COMPLETED' || Boolean(item.certificateAvailable);
                    const formattedBg = item.bloodGroup ? item.bloodGroup.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : (profile?.bloodGroup?.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') || 'O+');
                    return (
                      <tr key={item.id} className="hover:bg-slate-50/60 dark:hover:bg-slate-800/40 transition">
                        <td className="py-4 px-4 font-semibold text-gray-900 dark:text-white">
                          {item.donationDate || item.date || 'Recent'}
                        </td>
                        <td className="py-4 px-4 text-gray-700 dark:text-slate-300 font-medium">
                          {item.hospitalName || item.hospital || 'Medical Center'}
                        </td>
                        <td className="py-4 px-4 font-extrabold text-red-600 dark:text-red-400">
                          {formattedBg}
                        </td>
                        <td className="py-4 px-4 font-bold text-gray-800 dark:text-slate-200">
                          {item.unitsDonated || item.units || 1} Unit(s)
                        </td>
                        <td className="py-4 px-4">
                          <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-bold ${
                            isCompleted ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/60 dark:text-emerald-400 border border-emerald-200' :
                            item.status === 'CONFIRMED' || item.status === 'ACCEPTED' ? 'bg-blue-50 text-blue-600 dark:bg-blue-950/60 dark:text-blue-400 border border-blue-200' :
                            item.status === 'CANCELLED' || item.status === 'REJECTED' ? 'bg-red-50 text-red-600 dark:bg-red-950/60 dark:text-red-400 border border-red-200' :
                            'bg-amber-50 text-amber-600 dark:bg-amber-950/60 dark:text-amber-400 border border-amber-200'
                          }`}>
                            {item.status || 'PENDING'}
                          </span>
                        </td>
                        <td className="py-4 px-4 text-center">
                          {isCompleted ? (
                            <Button 
                              variant="primary" 
                              size="xs"
                              className="font-bold flex items-center justify-center gap-1.5 mx-auto py-1.5 px-3"
                              onClick={() => donationService.downloadCertificate(item.id)}
                            >
                              <Download className="h-3.5 w-3.5" /> Download Certificate
                            </Button>
                          ) : (
                            <span className="text-gray-400 text-[11px] italic">Not Available</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="text-base font-bold text-gray-900 dark:text-white mb-4">Milestone Timeline</h3>
            <Timeline items={data} onCertificateClick={(item) => donationService.downloadCertificate(item.id)} />
          </Card>
        </div>
      ) : (
        <EmptyState
          message="You haven't completed any donation cycles yet."
          icon={ClipboardList}
        />
      )}

      {/* Official Certificate Modal */}
      {selectedCertificate && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 max-w-lg w-full border border-slate-100 dark:border-slate-800 shadow-2xl flex flex-col gap-6 relative">
            <button 
              onClick={() => setSelectedCertificate(null)}
              className="absolute top-4 right-4 p-2 rounded-xl text-gray-400 hover:text-gray-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition"
            >
              <X className="h-5 w-5" />
            </button>

            <div className="border-4 border-double border-red-200 dark:border-red-950/60 p-6 rounded-2xl flex flex-col items-center text-center gap-4 bg-gradient-to-b from-red-50/30 to-white dark:from-slate-900 dark:to-slate-900">
              <Award className="h-12 w-12 text-amber-500" />
              <div>
                <span className="text-[10px] font-extrabold uppercase tracking-widest text-red-500">Official Certificate of Appreciation</span>
                <h3 className="text-xl font-black text-gray-900 dark:text-white mt-1">Life Saver Recognition</h3>
              </div>
              <p className="text-xs text-gray-600 dark:text-slate-300 leading-relaxed max-w-sm">
                This certificate certifies that <strong>{profile.fullName}</strong> successfully donated blood at <strong>{selectedCertificate.hospitalName || 'Community Center'}</strong> on {selectedCertificate.donationDate || 'Recent Date'}.
              </p>
              <div className="text-[10px] text-gray-400 font-mono">
                Certificate ID: CERT-BB-{selectedCertificate.id || '99812'}
              </div>
            </div>

            <div className="flex justify-end gap-3">
              <Button variant="outline" size="sm" onClick={() => setSelectedCertificate(null)}>
                Close
              </Button>
              <Button variant="primary" size="sm" onClick={() => window.print()}>
                <Download className="h-4 w-4 mr-1.5" /> Download PDF
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
