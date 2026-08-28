import React, { useState } from 'react';
import { useBloodRequests } from '../../hooks/useBloodRequests';
import api from '../../api/axios';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import { FileText, MapPin, Heart, ShieldAlert, CheckCircle2 } from 'lucide-react';

import useWebSocket from '../../hooks/useWebSocket';
import useAuthStore from '../../store/authStore';
import useToastStore from '../../store/toastStore';

/**
 * Page displaying active emergency blood requests with card layout & 1-click response.
 * Strictly adheres to React Rules of Hooks with all hooks declared at the top level.
 */
export default function BloodRequests() {
  // All Hooks declared at top level before any early return statements
  const { data, isLoading, error, refetch } = useBloodRequests();
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [responseSuccess, setResponseSuccess] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState(null);

  const { user } = useAuthStore();
  const { addToast } = useToastStore();
  const processedEventsRef = React.useRef(new Set());

  const donorTopics = React.useMemo(() => {
    const list = ['/topic/emergency-events'];
    if (user?.id) {
      list.push(`/topic/emergency-events/donor/${user.id}`);
      list.push(`/topic/donor/${user.id}`);
    }
    return list;
  }, [user?.id]);

  const { isConnected } = useWebSocket(donorTopics, (eventData) => {
    if (!eventData) return;
    const type = eventData.eventType;

    const eventKey = `${type}_${eventData.requestId}_${eventData.matchedDonorId}_${eventData.timestamp}`;
    if (processedEventsRef.current.has(eventKey)) return;
    processedEventsRef.current.add(eventKey);

    console.log('⚡ [DONOR-REALTIME-EVENT] Received:', eventData);

    if (type === 'DONOR_MATCHED' || type === 'EMERGENCY_REQUEST_CREATED' || type === 'EMERGENCY_REQUEST_ALERT') {
      refetch();
      addToast('⚡ New Emergency Blood Request Matched!', 'info');
    } else if (type === 'DONOR_ACCEPTED_REQUEST' || type === 'DONOR_REJECTED_REQUEST' || type === 'EMERGENCY_REQUEST_UPDATED') {
      refetch();
    }
  });

  React.useEffect(() => {
    if (isConnected) {
      refetch();
    }
  }, [isConnected, refetch]);

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const formatBloodGroup = (bg) => {
    if (!bg) return '?';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'Today';
    return new Date(dateStr).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
  };

  const handleDonateClick = (req) => {
    setSelectedRequest(req);
    setResponseSuccess(false);
    setActionError(null);
  };

  const handleConfirmResponse = async () => {
    if (!selectedRequest) return;
    try {
      setActionLoading(true);
      setActionError(null);
      const reqId = selectedRequest.requestId || selectedRequest.id;
      await api.post(`/donor/emergency-requests/${reqId}/accept`);
      setResponseSuccess(true);
      refetch();
      setTimeout(() => {
        setSelectedRequest(null);
        setResponseSuccess(false);
      }, 2000);
    } catch (err) {
      setActionError(err.response?.data?.message || err.message || 'Failed to accept blood request');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectClick = async (reqId) => {
    try {
      await api.post(`/donor/emergency-requests/${reqId}/reject`);
      refetch();
    } catch (err) {
      console.error('Failed to decline request:', err);
    }
  };

  return (
    <div className="flex flex-col gap-6 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <ShieldAlert className="h-6 w-6 text-red-500" /> Emergency Blood Requests Feed
          </h1>
          <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">
            Matching requests based on your blood group & location sorted by urgency.
          </p>
        </div>
        <Badge variant="critical" size="lg">
          Live Urgent Feed
        </Badge>
      </div>

      {data && data.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {data.map((req) => {
            const reqId = req.requestId || req.id;
            const bloodGroupNeeded = req.bloodGroupNeeded || req.bloodGroup;
            const urgencyLevel = req.urgencyLevel || req.priority;
            const requiredByDate = req.requiredByDate || req.expiryTime;
            const isConfirmed = req.confirmed || req.status === 'CONFIRMED';
            const isAccepted = req.status === 'ACCEPTED';
            const isCompleted = req.status === 'COMPLETED' || req.requestStatus === 'COMPLETED';
            const isInProgress = req.status === 'FULFILLMENT_IN_PROGRESS' || req.requestStatus === 'FULFILLMENT_IN_PROGRESS';

            return (
              <Card key={reqId} className={`p-6 flex flex-col justify-between gap-5 transition-all rounded-3xl ${isConfirmed ? 'border-2 border-indigo-500 bg-indigo-50/20 dark:bg-indigo-950/20 shadow-md' : 'border-slate-100 dark:border-slate-800 hover:shadow-lg'}`}>
                <div className="flex items-start justify-between gap-2">
                  {isConfirmed ? (
                    <Badge variant="indigo" size="md">
                      ⭐ SELECTED FOR EMERGENCY
                    </Badge>
                  ) : isAccepted ? (
                    <Badge variant="warning" size="md">
                      ⏳ WAITING FOR HOSPITAL CONFIRMATION
                    </Badge>
                  ) : isInProgress ? (
                    <Badge variant="urgent" size="md">
                      🚨 FULFILLMENT IN PROGRESS
                    </Badge>
                  ) : isCompleted ? (
                    <Badge variant="success" size="md">
                      ✅ COMPLETED
                    </Badge>
                  ) : (
                    <Badge variant={urgencyLevel === 'CRITICAL' ? 'critical' : urgencyLevel === 'HIGH' ? 'urgent' : 'info'}>
                      {urgencyLevel || 'URGENT'}
                    </Badge>
                  )}

                  <div className="h-10 w-10 rounded-2xl bg-red-50 text-red-600 dark:bg-red-950/60 dark:text-red-400 font-black text-sm flex items-center justify-center border border-red-100 dark:border-red-900/30">
                    {formatBloodGroup(bloodGroupNeeded)}
                  </div>
                </div>

                {isConfirmed && (
                  <div className="p-3 bg-indigo-100/70 dark:bg-indigo-900/40 rounded-2xl border border-indigo-200 dark:border-indigo-800 text-indigo-900 dark:text-indigo-200 text-xs font-bold flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-indigo-600 dark:text-indigo-400 shrink-0" />
                    <span>You have been selected for this emergency request!</span>
                  </div>
                )}

                <div className="flex flex-col gap-1.5">
                  <h3 className="font-bold text-base text-gray-900 dark:text-white line-clamp-1">
                    🏥 {req.hospitalName || 'Medical Center'}
                  </h3>
                  {req.hospitalAddress && (
                    <p className="text-xs text-gray-500 dark:text-slate-400">
                      📍 {req.hospitalAddress}
                    </p>
                  )}
                  {req.hospitalPhone && (
                    <p className="text-xs text-indigo-600 dark:text-indigo-400 font-semibold">
                      📞 Phone: <a href={`tel:${req.hospitalPhone}`} className="underline font-mono">{req.hospitalPhone}</a>
                    </p>
                  )}
                  <div className="flex items-center gap-1 text-xs text-gray-500 dark:text-slate-400 mt-1">
                    <MapPin className="h-3.5 w-3.5 text-primary shrink-0" />
                    <span>{req.hospitalCity || req.patientCity || 'City'}{req.hospitalState || req.patientState ? `, ${req.hospitalState || req.patientState}` : ''}</span>
                    {req.distanceKm != null && <span className="ml-1 text-red-500 font-semibold">({req.distanceKm} km away)</span>}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3 text-xs bg-slate-50 dark:bg-slate-800/50 p-3 rounded-2xl border border-slate-100 dark:border-slate-800">
                  <div>
                    <span className="text-gray-400 block text-[10px]">Units Required</span>
                    <strong className="text-gray-900 dark:text-white font-bold">{req.unitsRequired || 1} Unit(s)</strong>
                  </div>
                  <div>
                    <span className="text-gray-400 block text-[10px]">Required By</span>
                    <strong className="text-gray-900 dark:text-white font-bold">{formatDate(requiredByDate)}</strong>
                  </div>
                </div>

                {req.fulfillmentInstructions && isConfirmed && (
                  <div className="text-xs text-indigo-800 dark:text-indigo-300 bg-indigo-50/50 dark:bg-indigo-950/40 p-2.5 rounded-xl border border-indigo-100 font-medium">
                    📋 {req.fulfillmentInstructions}
                  </div>
                )}

                {req.reason && !isConfirmed && (
                  <p className="text-xs text-gray-500 dark:text-slate-400 bg-red-50/30 dark:bg-red-950/20 p-2.5 rounded-xl border border-red-100/50 dark:border-red-900/20 italic line-clamp-2">
                    "{req.reason}"
                  </p>
                )}

                <div className="flex gap-2">
                  {isConfirmed || isAccepted || isInProgress || isCompleted ? (
                    <div className="w-full py-2.5 px-4 bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 rounded-xl text-xs font-bold text-center flex items-center justify-center gap-1.5 border border-slate-200 dark:border-slate-700">
                      <CheckCircle2 className="h-4 w-4 text-emerald-500 shrink-0" />
                      <span>{isCompleted ? 'Donation Completed' : isConfirmed ? 'Selected by Hospital' : 'Request Accepted'}</span>
                    </div>
                  ) : (
                    <>
                      <Button variant="primary" size="sm" onClick={() => handleDonateClick(req)} className="flex-1 py-2.5 shadow-sm">
                        <Heart className="h-4 w-4 mr-1.5 fill-white" /> Accept Request
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleRejectClick(reqId)} className="py-2.5 text-gray-500 hover:text-red-600">
                        Decline
                      </Button>
                    </>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      ) : (
        <EmptyState
          title="No Matching Emergency Requests"
          message="No matching emergency requests right now. BloodBridge will notify you when an eligible request matches your profile."
          icon={FileText}
        />
      )}

      {/* Response Confirmation Modal */}
      {selectedRequest && (
        <ConfirmationModal
          isOpen={!!selectedRequest}
          onClose={() => setSelectedRequest(null)}
          onConfirm={handleConfirmResponse}
          title={`Accept Emergency Request #${selectedRequest.requestId || selectedRequest.id}`}
          message={
            actionError
              ? `Error: ${actionError}`
              : responseSuccess
              ? 'Thank you! Your acceptance has been sent to the hospital.'
              : `Confirm your commitment to donate ${selectedRequest.unitsRequired || 1} unit(s) of ${formatBloodGroup(selectedRequest.bloodGroupNeeded || selectedRequest.bloodGroup)} blood at ${selectedRequest.hospitalName || 'the hospital'}?`
          }
          confirmText={responseSuccess ? 'Done' : actionLoading ? 'Processing...' : 'Confirm Acceptance'}
          variant="primary"
        />
      )}
    </div>
  );
}
