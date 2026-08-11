import React, { useState, useEffect, useCallback, useRef } from 'react';
import hospitalService from '../../../services/hospitalService';
import Badge from '../../ui/Badge';
import { UserCheck, Clock, CheckCircle2, MapPin, HeartHandshake, RefreshCw, Mail, Phone, Users } from 'lucide-react';
import { useWebSocket } from '../../../hooks/useWebSocket';
import { useAuthStore } from '../../../store/authStore';

/**
 * Hospital-side Component rendering live matched donor responses (ACCEPTED, PENDING, REJECTED)
 * and summary statistics for an emergency blood request.
 */
export default function EmergencyRequestResponses({ requestId }) {
  const [summaryData, setSummaryData] = useState(null);
  const [responses, setResponses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuthStore();
  const processedEventsRef = useRef(new Set());

  const fetchResponses = useCallback(async (isBackground = false) => {
    if (!requestId) return;
    try {
      if (!isBackground) setLoading(true);
      setError(null);
      const res = await hospitalService.getEmergencyRequestResponses(requestId);
      console.log("[HOSPITAL-RESPONSES]", res);

      let list = [];
      let summary = {
        totalMatchedDonors: 0,
        acceptedDonors: 0,
        pendingDonors: 0,
        rejectedDonors: 0,
      };

      if (res && Array.isArray(res.responses)) {
        list = res.responses;
        summary = {
          totalMatchedDonors: res.totalMatchedDonors ?? list.length,
          acceptedDonors: res.acceptedDonors ?? list.filter(r => r.responseStatus === 'ACCEPTED' || r.responseStatus === 'CONFIRMED').length,
          pendingDonors: res.pendingDonors ?? list.filter(r => r.responseStatus === 'PENDING' || r.responseStatus === 'VIEWED').length,
          rejectedDonors: res.rejectedDonors ?? list.filter(r => r.responseStatus === 'REJECTED').length,
        };
      } else if (Array.isArray(res)) {
        list = res;
        summary = {
          totalMatchedDonors: list.length,
          acceptedDonors: list.filter(r => r.responseStatus === 'ACCEPTED' || r.responseStatus === 'CONFIRMED').length,
          pendingDonors: list.filter(r => r.responseStatus === 'PENDING' || r.responseStatus === 'VIEWED').length,
          rejectedDonors: list.filter(r => r.responseStatus === 'REJECTED').length,
        };
      } else if (res && res.data && Array.isArray(res.data.responses)) {
        list = res.data.responses;
        summary = {
          totalMatchedDonors: res.data.totalMatchedDonors ?? list.length,
          acceptedDonors: res.data.acceptedDonors ?? list.filter(r => r.responseStatus === 'ACCEPTED' || r.responseStatus === 'CONFIRMED').length,
          pendingDonors: res.data.pendingDonors ?? list.filter(r => r.responseStatus === 'PENDING' || r.responseStatus === 'VIEWED').length,
          rejectedDonors: res.data.rejectedDonors ?? list.filter(r => r.responseStatus === 'REJECTED').length,
        };
      }

      setResponses(list);
      setSummaryData(summary);
    } catch (err) {
      console.error('Failed to fetch emergency donor responses:', err);
      if (!isBackground) {
        setError(err.message || 'Failed to load donor responses');
      }
    } finally {
      if (!isBackground) setLoading(false);
    }
  }, [requestId]);

  const hospitalTopics = React.useMemo(() => {
    const list = ['/topic/emergency-events'];
    if (requestId) {
      list.push(`/topic/emergency-events/request/${requestId}`);
    }
    if (user?.id) {
      list.push(`/topic/emergency-events/hospital/${user.id}`);
      list.push(`/topic/hospital/${user.id}`);
    }
    return list;
  }, [requestId, user?.id]);

  const { isConnected, isFallback } = useWebSocket(hospitalTopics, (eventData) => {
    if (!eventData) return;
    const reqId = eventData.requestId || eventData.entityId;
    if (requestId && reqId && Number(reqId) !== Number(requestId)) {
      return;
    }

    const eventKey = `${eventData.eventType}_${eventData.matchedDonorId}_${eventData.donorId}_${eventData.timestamp}`;
    if (processedEventsRef.current.has(eventKey)) return;
    processedEventsRef.current.add(eventKey);

    console.log('⚡ [HOSPITAL-REALTIME-EVENT] Received:', eventData);

    const type = eventData.eventType;
    if (type === 'DONOR_ACCEPTED_REQUEST' || type === 'DONOR_ACCEPTED' || type === 'DONOR_REJECTED_REQUEST' || type === 'DONOR_DECLINED' || type === 'DONOR_MATCHED' || type === 'EMERGENCY_REQUEST_UPDATED') {
      fetchResponses(true);
    }
  });

  // Reconnection synchronization & initial fetch
  useEffect(() => {
    fetchResponses();
  }, [fetchResponses]);

  useEffect(() => {
    if (isConnected) {
      console.log('🔄 STOMP WebSocket Connected/Reconnected — Syncing latest responses from REST');
      fetchResponses(true);
    }
  }, [isConnected, fetchResponses]);

  // Polling fallback when WebSocket disconnected
  useEffect(() => {
    let interval = null;
    if (!isConnected || isFallback) {
      console.log('⏱️ WebSocket offline/fallback mode — Polling responses every 8s');
      interval = setInterval(() => {
        fetchResponses(true);
      }, 8000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isConnected, isFallback, fetchResponses]);

  const formatBloodGroup = (bg) => {
    if (!bg) return 'N/A';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleTimeString(undefined, {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  };

  const acceptedList = responses.filter(r => r.responseStatus === 'ACCEPTED');
  const pendingList = responses.filter(r => r.responseStatus === 'PENDING' || r.responseStatus === 'VIEWED');

  const [confirmingId, setConfirmingId] = useState(null);
  const [completionTarget, setCompletionTarget] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);

  const handleConfirmDonor = async (matchedDonorId) => {
    try {
      setConfirmingId(matchedDonorId);
      setActionMessage(null);
      await hospitalService.confirmEmergencyDonor(requestId, matchedDonorId);
      setActionMessage({ type: 'success', text: 'Donor confirmed successfully! Request updated to Fulfillment in Progress.' });
      await fetchResponses();
    } catch (err) {
      console.error('Failed to confirm donor:', err);
      setActionMessage({ type: 'error', text: err.response?.data?.message || err.message || 'Failed to confirm donor' });
    } finally {
      setConfirmingId(null);
    }
  };

  const handleCompleteRequest = async () => {
    try {
      setLoading(true);
      setActionMessage(null);
      await hospitalService.completeEmergencyRequest(requestId);
      setActionMessage({ type: 'success', text: 'Emergency blood request completed successfully!' });
      await fetchResponses();
    } catch (err) {
      console.error('Failed to complete request:', err);
      setActionMessage({ type: 'error', text: err.response?.data?.message || err.message || 'Failed to complete request' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-100 dark:border-slate-800 shadow-sm flex flex-col gap-6 font-sans">
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-2 pb-4 border-b border-slate-100 dark:border-slate-800">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 rounded-2xl border border-red-100 dark:border-red-900/30">
            <HeartHandshake className="h-6 w-6" />
          </div>
          <div>
            <h3 className="font-bold text-lg text-gray-900 dark:text-white flex items-center gap-2">
              Donor Responses & Emergency Commitments
            </h3>
            <p className="text-xs text-gray-500 dark:text-slate-400">
              Live responses from medically compatible matched donors.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchResponses}
            disabled={loading}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-gray-600 dark:text-slate-300 hover:text-gray-900 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl transition-all"
            title="Refresh Responses"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>

          <button
            onClick={handleCompleteRequest}
            disabled={loading}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-sm transition-all"
          >
            <CheckCircle2 className="h-3.5 w-3.5" />
            Complete Request
          </button>
        </div>
      </div>

      {actionMessage && (
        <div className={`p-3.5 rounded-2xl text-xs font-medium border ${actionMessage.type === 'success' ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-300' : 'bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-300'}`}>
          {actionMessage.text}
        </div>
      )}

      {/* Summary Stats Row */}
      {summaryData && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="bg-slate-50 dark:bg-slate-800/60 p-3.5 rounded-2xl border border-slate-100 dark:border-slate-800 flex items-center gap-3">
            <div className="p-2 bg-slate-200/60 dark:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-xl">
              <Users className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[10px] uppercase font-extrabold text-slate-400">Total Matched</span>
              <p className="text-lg font-extrabold text-slate-900 dark:text-white">{summaryData.totalMatchedDonors}</p>
            </div>
          </div>

          <div className="bg-emerald-50/60 dark:bg-emerald-950/30 p-3.5 rounded-2xl border border-emerald-100 dark:border-emerald-900/40 flex items-center gap-3">
            <div className="p-2 bg-emerald-500 text-white rounded-xl">
              <CheckCircle2 className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[10px] uppercase font-extrabold text-emerald-600 dark:text-emerald-400">🟢 Accepted</span>
              <p className="text-lg font-extrabold text-emerald-700 dark:text-emerald-300">{summaryData.acceptedDonors}</p>
            </div>
          </div>

          <div className="bg-amber-50/60 dark:bg-amber-950/30 p-3.5 rounded-2xl border border-amber-100 dark:border-amber-900/40 flex items-center gap-3">
            <div className="p-2 bg-amber-500 text-white rounded-xl">
              <Clock className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[10px] uppercase font-extrabold text-amber-600 dark:text-amber-400">🟡 Pending</span>
              <p className="text-lg font-extrabold text-amber-700 dark:text-amber-300">{summaryData.pendingDonors}</p>
            </div>
          </div>

          <div className="bg-rose-50/60 dark:bg-rose-950/30 p-3.5 rounded-2xl border border-rose-100 dark:border-rose-900/40 flex items-center gap-3">
            <div className="p-2 bg-rose-500 text-white rounded-xl">
              <UserCheck className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[10px] uppercase font-extrabold text-rose-600 dark:text-rose-400">🔴 Rejected</span>
              <p className="text-lg font-extrabold text-rose-700 dark:text-rose-300">{summaryData.rejectedDonors}</p>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="py-8 text-center text-xs text-gray-400 animate-pulse">
          Loading donor responses...
        </div>
      ) : error ? (
        <div className="text-xs text-red-500 bg-red-50 dark:bg-red-950/30 p-3 rounded-2xl border border-red-100 dark:border-red-900/30">
          {error}
        </div>
      ) : (
        <div className="flex flex-col gap-6">
          {/* Accepted Donors Cards */}
          {acceptedList.length > 0 && (
            <div className="flex flex-col gap-3">
              <h4 className="text-xs font-extrabold uppercase tracking-wider text-emerald-600 dark:text-emerald-400 flex items-center gap-1.5">
                <CheckCircle2 className="h-4 w-4" /> Accepted Donors ({acceptedList.length})
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {acceptedList.map((res) => {
                  const isConfirmed = res.confirmed || res.responseStatus === 'CONFIRMED';
                  return (
                    <div
                      key={res.matchedDonorId || res.donorId}
                      className={`p-4 rounded-2xl border shadow-sm flex flex-col justify-between gap-3 ${isConfirmed ? 'bg-indigo-50/60 dark:bg-indigo-950/30 border-indigo-200 dark:border-indigo-900/50' : 'bg-emerald-50/50 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-900/40'}`}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-center gap-3">
                          <div className={`h-11 w-11 rounded-2xl font-black text-sm flex items-center justify-center shadow-sm ${isConfirmed ? 'bg-indigo-600 text-white' : 'bg-emerald-500 text-white'}`}>
                            {formatBloodGroup(res.bloodGroup)}
                          </div>
                          <div>
                            <h5 className="font-bold text-sm text-gray-900 dark:text-white">
                              👤 {res.donorName || 'Accepted Donor'}
                            </h5>
                            <div className="flex items-center gap-2 text-xs text-gray-500 dark:text-slate-400 mt-0.5">
                              <span className="flex items-center gap-1 text-red-500 font-medium">
                                <MapPin className="h-3 w-3 shrink-0" />
                                {res.distanceKm != null ? `${res.distanceKm} KM away` : 'Nearby'}
                              </span>
                              {(res.matchingGroup || res.tierGroup) && (
                                <span className="text-[10px] bg-emerald-100 dark:bg-emerald-900/40 text-emerald-800 dark:text-emerald-300 font-bold px-1.5 py-0.5 rounded">
                                  {res.matchingGroup || res.tierGroup}
                                </span>
                              )}
                            </div>
                          </div>
                        </div>

                        {isConfirmed ? (
                          <Badge variant="indigo" size="sm">
                            ⭐ CONFIRMED DONOR
                          </Badge>
                        ) : (
                          <Badge variant="success" size="sm">
                            ✅ ACCEPTED
                          </Badge>
                        )}
                      </div>

                      {/* Contact details & timestamp */}
                      <div className="flex flex-col gap-1.5 pt-3 border-t border-emerald-200/60 dark:border-emerald-900/40 text-xs text-slate-700 dark:text-slate-300">
                        {res.email && (
                          <div className="flex items-center gap-2 text-slate-600 dark:text-slate-400">
                            <Mail className="h-3.5 w-3.5 text-emerald-600 shrink-0" />
                            <span className="font-mono text-[11px] select-all">{res.email}</span>
                          </div>
                        )}
                        {res.phone && (
                          <div className="flex items-center gap-2 text-slate-600 dark:text-slate-400">
                            <Phone className="h-3.5 w-3.5 text-emerald-600 shrink-0" />
                            <span className="font-mono text-[11px] select-all">{res.phone}</span>
                          </div>
                        )}
                        {res.acceptedAt && (
                          <div className="flex items-center justify-between text-[10px] text-emerald-800 dark:text-emerald-400 font-semibold pt-1">
                            <span>Accepted At:</span>
                            <span className="font-mono">{formatTime(res.acceptedAt)}</span>
                          </div>
                        )}
                      </div>

                      {/* Action Buttons: Confirm & Complete */}
                      <div className="pt-2 flex flex-col gap-2">
                        {!isConfirmed && (
                          <button
                            onClick={() => handleConfirmDonor(res.matchedDonorId || res.donorId)}
                            disabled={confirmingId === (res.matchedDonorId || res.donorId)}
                            className="w-full py-2 px-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
                          >
                            {confirmingId === (res.matchedDonorId || res.donorId) ? (
                              <>
                                <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                                Confirming...
                              </>
                            ) : (
                              <>
                                <CheckCircle2 className="h-4 w-4" />
                                Select & Confirm Donor
                              </>
                            )}
                          </button>
                        )}

                        {res.responseStatus !== 'COMPLETED' && (
                          <button
                            onClick={() => setCompletionTarget(res)}
                            disabled={loading}
                            className="w-full py-2 px-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-sm transition-all flex items-center justify-center gap-2"
                          >
                            <CheckCircle2 className="h-4 w-4" />
                            Mark Donation Completed
                          </button>
                        )}
                        {res.responseStatus === 'COMPLETED' && (
                          <div className="w-full py-1.5 px-3 bg-emerald-100 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-300 font-bold text-xs rounded-xl text-center border border-emerald-300 dark:border-emerald-800">
                            ✓ Donation Completed
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Pending Donors Cards */}
          {pendingList.length > 0 && (
            <div className="flex flex-col gap-3">
              <h4 className="text-xs font-extrabold uppercase tracking-wider text-amber-600 dark:text-amber-400 flex items-center gap-1.5">
                <Clock className="h-4 w-4" /> Pending Responses ({pendingList.length})
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {pendingList.map((res) => (
                  <div
                    key={res.matchedDonorId || res.donorId}
                    className="p-3.5 rounded-2xl bg-amber-50/20 dark:bg-amber-950/10 border border-amber-200/60 dark:border-amber-900/30 flex items-center justify-between gap-3"
                  >
                    <div className="flex items-center gap-2.5">
                      <div className="h-9 w-9 rounded-xl bg-amber-100 dark:bg-amber-900/40 text-amber-800 dark:text-amber-300 font-extrabold text-xs flex items-center justify-center">
                        {formatBloodGroup(res.bloodGroup)}
                      </div>
                      <div>
                        <h5 className="font-bold text-xs text-gray-900 dark:text-white">
                          {res.donorName || 'Matched Donor'}
                        </h5>
                        <p className="text-[11px] text-gray-500">
                          {res.distanceKm != null ? `${res.distanceKm} KM away` : 'Nearby'}
                        </p>
                      </div>
                    </div>
                    <Badge variant="warning" size="sm">🟡 PENDING</Badge>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Empty state */}
          {responses.length === 0 && (
            <div className="py-8 text-center text-xs text-gray-400 italic">
              No matched donors found for this emergency request.
            </div>
          )}
        </div>
      )}

      {/* Confirmation Modal */}
      {completionTarget && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 max-w-md w-full border border-slate-100 dark:border-slate-800 shadow-2xl flex flex-col gap-5">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-emerald-100 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400 rounded-2xl">
                <CheckCircle2 className="h-6 w-6" />
              </div>
              <div>
                <h3 className="font-bold text-lg text-slate-900 dark:text-white">Confirm Donation Completion?</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400">Record actual physical blood donation completion.</p>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 flex flex-col gap-2 text-xs">
              <div className="flex justify-between">
                <span className="text-slate-400 font-medium">Donor:</span>
                <span className="font-bold text-slate-900 dark:text-white">{completionTarget.donorName || 'Accepted Donor'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400 font-medium">Blood Group:</span>
                <span className="font-bold text-red-600">{formatBloodGroup(completionTarget.bloodGroup)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400 font-medium">Emergency Request:</span>
                <span className="font-bold font-mono text-indigo-600">#{requestId}</span>
              </div>
            </div>

            <p className="text-[11px] text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/40 p-3 rounded-xl border border-amber-200 dark:border-amber-900/40">
              ⚠️ This will mark the donation as COMPLETED, generate the official Certificate of Appreciation, and start the donor's 90-day cooldown period from today.
            </p>

            <div className="flex justify-end gap-3 pt-2">
              <button
                onClick={() => setCompletionTarget(null)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-900 dark:text-slate-300 rounded-xl bg-slate-100 dark:bg-slate-800"
              >
                Cancel
              </button>
              <button
                onClick={async () => {
                  try {
                    setLoading(true);
                    await hospitalService.completeEmergencyRequest(requestId);
                    setActionMessage({ type: 'success', text: `Donation completed successfully for ${completionTarget.donorName || 'Donor'}! Certificate generated and cooldown started.` });
                    setCompletionTarget(null);
                    await fetchResponses();
                  } catch (err) {
                    console.error(err);
                    setActionMessage({ type: 'error', text: err.response?.data?.message || err.message || 'Failed to complete donation' });
                  } finally {
                    setLoading(false);
                  }
                }}
                className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-md flex items-center gap-1.5"
              >
                <CheckCircle2 className="h-4 w-4" />
                Confirm Completion
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
