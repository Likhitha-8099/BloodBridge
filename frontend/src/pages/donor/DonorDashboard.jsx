import React, { useState, useMemo } from 'react';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import { useDonationHistory } from '../../hooks/useDonationHistory';
import { useBloodRequests } from '../../hooks/useBloodRequests';
import { useWebSocket } from '../../hooks/useWebSocket';
import useAuthStore from '../../store/authStore';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EligibilityWidget from '../../components/donor/EligibilityWidget';
import AchievementCard from '../../components/donor/AchievementCard';
import { Link } from 'react-router-dom';
import { Heart, Activity, Calendar, User, Award, MapPin, Clock, CheckCircle2, Wifi, WifiOff, Check, X } from 'lucide-react';
import api from '../../api/axios';

/**
 * Premium Redesigned Donor Dashboard for Blood Bridge.
 * Integrates 10-Stage Smart Donor Matching Engine results & STOMP WebSocket listening.
 */
export default function DonorDashboard() {
  const { user } = useAuthStore();
  const { 
    profile, 
    isLoading: isProfileLoading, 
    error: profileError, 
    refetch: refetchProfile 
  } = useDonorProfile();
  
  const donorId = profile?.id;
  const { 
    data: donations, 
  } = useDonationHistory(donorId);

  const {
    data: activeRequests,
    isLoading: isRequestsLoading,
    refetch: refetchRequests
  } = useBloodRequests();

  const [activeEmergencyPopup, setActiveEmergencyPopup] = useState(null);
  const [etaMinutes, setEtaMinutes] = useState(15);
  const [isResponding, setIsResponding] = useState(false);

  const donorTopics = useMemo(() => {
    const list = ['/topic/emergency-events'];
    if (user?.id) {
      list.push(`/topic/notifications/${user.id}`);
    }
    if (donorId) {
      list.push(`/topic/donor/${donorId}`);
      list.push(`/topic/emergency-events/donor/${donorId}`);
      list.push(`/topic/donors/${donorId}/emergency-alert`);
    }
    list.push('/topic/donors/emergency');
    return list;
  }, [user?.id, donorId]);

  const { isConnected, isFallback } = useWebSocket(donorTopics, (eventData) => {
    console.log('⚡ Real-time STOMP event received in Donor Dashboard:', eventData);
    if (eventData?.eventType === 'EMERGENCY_REQUEST_ALERT' || eventData?.emergencyRequestId) {
      const popupData = eventData.payload || eventData;
      setActiveEmergencyPopup((prev) => prev || popupData);
    } else if (eventData === 'EMERGENCY_CLOSED' || eventData?.eventType === 'EMERGENCY_CLOSED') {
      setActiveEmergencyPopup(null);
    }
    refetchProfile();
    if (refetchRequests) refetchRequests();
  });

  // Foreground FCM Push Notification Listener
  React.useEffect(() => {
    let unsubscribe = null;
    import('../../firebase/firebase-messaging').then(({ onForegroundMessage }) => {
      onForegroundMessage((payload) => {
        console.log('📬 [FCM-Foreground] Received message in DonorDashboard:', payload);
        const data = payload?.data || {};
        if (data.requestId || data.notificationType === 'EMERGENCY_REQUEST') {
          setActiveEmergencyPopup((prev) => prev || {
            emergencyRequestId: data.requestId,
            hospitalName: data.hospitalName || 'Emergency Medical Center',
            bloodGroupNeeded: data.bloodGroupNeeded || data.bloodGroup || 'N/A',
            unitsRequired: parseInt(data.unitsRequired || '1', 10),
            reason: 'Emergency Blood Need',
            urgencyLevel: data.priority || 'HIGH',
            distanceKm: parseFloat(data.distanceKm || '5.0'),
          });
        }
      }).then((unsub) => {
        unsubscribe = unsub;
      });
    }).catch(err => console.warn('[FCM-Foreground] Failed to subscribe:', err));

    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, []);

  const handleAcceptCard = async (req) => {
    setIsResponding(true);
    try {
      const reqId = req.requestId || req.id;
      await api.post(`/donor/emergency-requests/${reqId}/accept`);
      refetchProfile();
      if (refetchRequests) refetchRequests();
    } catch (err) {
      alert(err.message || 'Failed to accept emergency blood request');
    } finally {
      setIsResponding(false);
    }
  };

  const handleRejectCard = async (req) => {
    setIsResponding(true);
    try {
      const reqId = req.requestId || req.id;
      await api.post(`/donor/emergency-requests/${reqId}/reject`);
      if (refetchRequests) refetchRequests();
    } catch (err) {
      console.error('Failed to decline emergency request:', err);
    } finally {
      setIsResponding(false);
    }
  };


  const handleAcceptEmergencyPopup = async () => {
    if (!activeEmergencyPopup) return;
    setIsResponding(true);
    try {
      const reqId = activeEmergencyPopup.emergencyRequestId || activeEmergencyPopup.requestId || activeEmergencyPopup.id;
      await api.post(`/donor/emergency-requests/${reqId}/accept`);
      setActiveEmergencyPopup(null);
      refetchProfile();
      if (refetchRequests) refetchRequests();
    } catch (err) {
      alert(err.message || 'Failed to accept emergency request');
    } finally {
      setIsResponding(false);
    }
  };

  const handleRejectEmergencyPopup = async () => {
    if (!activeEmergencyPopup) return;
    setIsResponding(true);
    try {
      const reqId = activeEmergencyPopup.emergencyRequestId || activeEmergencyPopup.requestId || activeEmergencyPopup.id;
      await api.post(`/donor/emergency-requests/${reqId}/reject`);
      setActiveEmergencyPopup(null);
      if (refetchRequests) refetchRequests();
    } catch (err) {
      console.error(err);
    } finally {
      setIsResponding(false);
    }
  };

  const isLoading = isProfileLoading;
  const error = profileError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    console.error('[DONOR-DASHBOARD-ERROR] Profile loading error:', error);
    return <ErrorState message={error.message || 'Unable to fetch donor profile'} onRetry={refetchProfile} />;
  }

  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-12">
        <Card className="flex flex-col items-center justify-center text-center p-10 gap-5 border border-dashed border-gray-200 rounded-3xl shadow-sm">
          <div className="p-4 bg-red-50 text-primary rounded-2xl border border-red-100 dark:bg-red-950/60 dark:text-red-400">
            <User className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">Initialize Donor Profile</h2>
            <p className="text-xs text-gray-500 dark:text-slate-400 leading-relaxed">
              Complete your health details and blood group profile to join our active donor network and respond to emergency blood requests.
            </p>
          </div>
          <Link to="/donor/profile/edit">
            <Button variant="primary" className="px-8 py-3 text-sm font-bold shadow-md">Get Started</Button>
          </Link>
        </Card>
      </div>
    );
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatBloodGroup = (bg) => {
    if (!bg) return 'N/A';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  const rawBloodGroup = profile.bloodGroup || profile.data?.bloodGroup;
  const formattedBloodGroup = formatBloodGroup(rawBloodGroup);
  const totalDonationsCount = profile.totalDonations || (Array.isArray(donations) ? donations.length : 0);
  const livesSavedCount = profile.livesSaved || totalDonationsCount * 3;
  const donorScore = profile.donorScore || 100;

  const donorCity = (profile?.city || user?.city || '').trim();
  const donorState = (profile?.state || user?.state || '').trim();

  // Requests returned from GET /api/v1/donor/emergency-requests
  const matchedEmergencyRequests = Array.isArray(activeRequests) ? activeRequests : (activeRequests?.data || []);

  return (
    <div className="flex flex-col gap-8 font-sans pb-12">
      {/* Top Banner & Header */}
      <div className="relative overflow-hidden bg-gradient-to-r from-red-600 via-rose-600 to-pink-700 rounded-3xl p-8 text-white shadow-xl">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-md border border-white/30 flex items-center justify-center text-white font-extrabold text-2xl shadow-inner">
              {formattedBloodGroup}
            </div>

            <div className="flex flex-col gap-1">
              <div className="flex items-center gap-2">
                <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight">
                  Welcome back, {profile.fullName || user?.fullName || 'Hero'}!
                </h1>
                {isConnected ? (
                  <span className="flex items-center gap-1 text-[10px] text-emerald-300 font-semibold bg-emerald-950/60 px-2 py-0.5 rounded-full border border-emerald-400/40">
                    <Wifi className="h-3 w-3" /> Live STOMP
                  </span>
                ) : (
                  <span className="flex items-center gap-1 text-[10px] text-amber-300 font-semibold bg-amber-950/60 px-2 py-0.5 rounded-full border border-amber-400/40">
                    <WifiOff className="h-3 w-3" /> {isFallback ? 'REST Fallback' : 'Connecting...'}
                  </span>
                )}
              </div>
              <p className="text-xs text-red-100 flex items-center gap-2">
                <MapPin className="h-3.5 w-3.5" /> {donorCity || 'Location N/A'}{donorState ? `, ${donorState}` : ''} &bull; Registered Hero
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3 w-full md:w-auto">
            <Link to="/donor/profile/edit" className="w-full md:w-auto">
              <button className="w-full md:w-auto px-5 py-2.5 bg-white/10 hover:bg-white/20 text-white border border-white/30 rounded-xl text-xs font-bold transition-all backdrop-blur-sm flex items-center justify-center gap-2">
                <User className="h-4 w-4" /> Edit Health Profile
              </button>
            </Link>
            <Link to="/donor/history" className="w-full md:w-auto">
              <button className="w-full md:w-auto px-5 py-2.5 bg-white text-red-600 rounded-xl text-xs font-bold hover:bg-red-50 transition-all shadow-md flex items-center justify-center gap-2">
                <Calendar className="h-4 w-4" /> Donation History
              </button>
            </Link>
          </div>
        </div>
      </div>

      {/* Main Grid: Eligibility & KPIs */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 flex flex-col gap-8">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
            <StatCard
              title="Total Donations"
              value={totalDonationsCount}
              icon={Heart}
              iconColor="text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/60"
            />
            <StatCard
              title="Estimated Lives Saved"
              value={livesSavedCount}
              icon={Activity}
              iconColor="text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/60"
            />
            <StatCard
              title="Donor Honor Score"
              value={donorScore}
              icon={Award}
              iconColor="text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/60"
            />
          </div>

          {/* Emergency Requests Section */}
          <Card title="Emergency Requests Assigned to You" subtitle={`Matched for ${formattedBloodGroup} by Smart Matching Engine`}>
            {isRequestsLoading ? (
              <LoadingSpinner />
            ) : matchedEmergencyRequests.length > 0 ? (
              <div className="flex flex-col divide-y divide-slate-100 dark:divide-slate-800">
                {matchedEmergencyRequests.map((req) => (
                  <div key={req.matchedDonorId || req.requestId || req.id} className="py-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div className="flex items-start gap-3">
                      <div className="w-12 h-12 rounded-2xl bg-red-50 dark:bg-red-950/50 text-red-600 font-black flex items-center justify-center border border-red-200 dark:border-red-800 shrink-0 text-base shadow-sm">
                        {formatBloodGroup(req.bloodGroup || req.bloodGroupNeeded)}
                      </div>
                      <div className="flex flex-col gap-1">
                        <h4 className="text-sm font-extrabold text-slate-900 dark:text-slate-100 flex items-center gap-2">
                          {req.hospitalName || 'Emergency Center'}
                          <Badge variant={(req.priority === 'CRITICAL' || req.urgencyLevel === 'CRITICAL') ? 'danger' : 'warning'}>
                            {req.priority || req.urgencyLevel || 'URGENT'}
                          </Badge>
                          {req.matchingGroup && (
                            <span className="text-[10px] font-bold uppercase tracking-wider bg-indigo-50 dark:bg-indigo-950 text-indigo-600 dark:text-indigo-300 px-2 py-0.5 rounded-md border border-indigo-200 dark:border-indigo-800">
                              {req.matchingGroup}
                            </span>
                          )}
                        </h4>
                        <p className="text-xs text-slate-500 dark:text-slate-400 flex flex-wrap items-center gap-x-3 gap-y-1">
                          <span className="font-medium">{req.hospitalAddress || 'Hospital Location'}</span>
                          &bull;
                          <span className="font-semibold text-slate-700 dark:text-slate-300">{req.unitsRequired || 1} Units Needed</span>
                          &bull;
                          <span className="font-semibold text-indigo-600 dark:text-indigo-400 flex items-center gap-1">
                            <MapPin className="h-3 w-3 inline" /> {req.distanceKm ? `${req.distanceKm} KM away` : 'Nearby'}
                          </span>
                        </p>
                        <p className="text-[11px] text-slate-400 dark:text-slate-500 flex items-center gap-1 mt-0.5">
                          <Clock className="h-3 w-3" /> Created: {formatDate(req.createdAt)}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0 sm:self-center">
                      {req.status === 'ACCEPTED' ? (
                        <span className="px-3 py-1.5 bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800 rounded-xl text-xs font-extrabold flex items-center gap-1">
                          <Check className="h-4 w-4" /> Accepted
                        </span>
                      ) : (
                        <>
                          <button
                            onClick={() => handleRejectCard(req)}
                            disabled={isResponding}
                            className="px-3 py-2 text-xs font-bold text-slate-600 hover:text-slate-800 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 transition-all flex items-center gap-1"
                          >
                            <X className="h-3.5 w-3.5" /> Reject
                          </button>
                          <Button
                            variant="primary"
                            size="sm"
                            disabled={isResponding}
                            onClick={() => handleAcceptCard(req)}
                            className="px-4 py-2 text-xs font-bold shrink-0 shadow-md flex items-center gap-1 bg-red-600 hover:bg-red-700 text-white"
                          >
                            <Check className="h-3.5 w-3.5" /> Accept Request
                          </Button>
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-10 text-center flex flex-col items-center gap-2">
                <CheckCircle2 className="h-10 w-10 text-emerald-500" />
                <p className="text-xs text-slate-500 font-medium">No active emergency blood requests matched for {formattedBloodGroup} in {donorCity || 'your region'} right now.</p>
              </div>
            )}
          </Card>
        </div>

        {/* Right Column: Eligibility & Achievement */}
        <div className="flex flex-col gap-8">
          <EligibilityWidget profile={profile} />

          <AchievementCard
            donationsCount={totalDonationsCount}
            livesSavedCount={livesSavedCount}
            donorScore={donorScore}
          />
        </div>
      </div>

      {/* Real-Time Emergency Popup Modal */}
      {activeEmergencyPopup && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md p-4 animate-in fade-in duration-200">
          <div className="bg-white dark:bg-slate-900 rounded-3xl max-w-lg w-full p-6 shadow-2xl border-2 border-red-500/40 flex flex-col gap-5">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-red-600 text-white rounded-2xl animate-bounce">
                <Activity className="h-7 w-7" />
              </div>
              <div>
                <span className="text-xs font-black tracking-wider uppercase text-red-600 bg-red-50 dark:bg-red-950/60 px-2.5 py-1 rounded-md border border-red-200">
                  🚨 EMERGENCY BLOOD REQUEST
                </span>
                <h3 className="text-xl font-extrabold text-gray-900 dark:text-white mt-1">
                  {activeEmergencyPopup.hospitalName || 'Medical Emergency'}
                </h3>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs bg-slate-50 dark:bg-slate-800/60 p-4 rounded-2xl border border-slate-200 dark:border-slate-700">
              <div>
                <span className="text-gray-500 font-medium">Blood Group Needed:</span>
                <p className="text-lg font-black text-red-600">{activeEmergencyPopup.bloodGroupNeeded || activeEmergencyPopup.bloodGroup}</p>
              </div>
              <div>
                <span className="text-gray-500 font-medium">Units Required:</span>
                <p className="text-base font-extrabold text-gray-900 dark:text-white">{activeEmergencyPopup.unitsRequired || 1} Units</p>
              </div>
              <div>
                <span className="text-gray-500 font-medium">Distance from You:</span>
                <p className="text-sm font-bold text-indigo-600 dark:text-indigo-400">{activeEmergencyPopup.distanceKm ? `${activeEmergencyPopup.distanceKm} KM` : 'Nearby'}</p>
              </div>
              <div>
                <span className="text-gray-500 font-medium">Reason / Urgency:</span>
                <p className="text-sm font-semibold text-gray-800 dark:text-slate-200">{activeEmergencyPopup.reason || activeEmergencyPopup.urgencyLevel || 'Emergency Need'}</p>
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-xs font-bold text-gray-700 dark:text-slate-300">
                Your Estimated Time of Arrival (ETA in minutes):
              </label>
              <input
                type="number"
                min="1"
                max="180"
                value={etaMinutes}
                onChange={(e) => setEtaMinutes(parseInt(e.target.value) || 15)}
                className="w-full px-4 py-2.5 text-sm font-semibold border rounded-xl dark:bg-slate-800 dark:border-slate-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-red-500 outline-none"
              />
            </div>

            <div className="flex gap-3 mt-2">
              <button
                onClick={handleRejectEmergencyPopup}
                disabled={isResponding}
                className="flex-1 py-3 px-4 rounded-xl border border-gray-300 dark:border-slate-700 text-gray-700 dark:text-slate-300 font-bold hover:bg-gray-100 dark:hover:bg-slate-800 transition-colors"
              >
                Reject
              </button>
              <button
                onClick={handleAcceptEmergencyPopup}
                disabled={isResponding}
                className="flex-1 py-3 px-4 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold shadow-lg shadow-red-600/30 transition-all flex items-center justify-center gap-2"
              >
                {isResponding ? 'Accepting...' : 'Accept Emergency'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
