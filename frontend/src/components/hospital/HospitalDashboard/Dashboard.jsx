import React, { useState, useEffect, useMemo, useCallback } from 'react';
import hospitalService from '../../../services/hospitalService';
import useAuthStore from '../../../store/authStore';
import { useWebSocket } from '../../../hooks/useWebSocket';
import Header from './Header';
import DashboardCards from './DashboardCards';
import QuickActions from './QuickActions';
import RecentRequests from './RecentRequests';
import EmergencyRequests from './EmergencyRequests';
import EmergencyRequestResponses from './EmergencyRequestResponses';
import RecentDonations from './RecentDonations';
import NearbyDonors from './NearbyDonors';
import Notifications from './Notifications';
import Analytics from './Analytics';
import LoadingSkeleton from './LoadingSkeleton';
import ErrorState from '../../ui/ErrorState';
import { RefreshCw, Wifi, WifiOff } from 'lucide-react';

export default function Dashboard() {
  const { user } = useAuthStore();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showNotificationDrawer, setShowNotificationDrawer] = useState(false);

  const fetchDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await hospitalService.getDashboardData();
      if (res && res.data) {
        setData(res.data);
      } else {
        setData(res || {});
      }
    } catch (err) {
      console.warn('Hospital Dashboard API fetch warning, attempting modular fallbacks:', err);
      try {
        const [statsRes, requestsRes, emergencyRes, donationsRes, donorsRes, notificationsRes, analyticsRes] =
          await Promise.allSettled([
            hospitalService.getDashboardData(),
            hospitalService.getRecentRequests(5),
            hospitalService.getEmergencyRequests(5),
            hospitalService.getRecentDonations(5),
            hospitalService.getNearbyDonors(5),
            hospitalService.getNotifications(5),
            hospitalService.getAnalytics(),
          ]);

        const fallbackData = {
          hospitalName: user?.fullName || user?.name || 'Registered Hospital',
          verificationStatus: 'PENDING',
          statistics: statsRes.status === 'fulfilled' ? statsRes.value?.data : {},
          recentRequests: requestsRes.status === 'fulfilled' ? requestsRes.value?.data || [] : [],
          emergencyRequests: emergencyRes.status === 'fulfilled' ? emergencyRes.value?.data || [] : [],
          recentDonations: donationsRes.status === 'fulfilled' ? donationsRes.value?.data || [] : [],
          nearbyDonors: donorsRes.status === 'fulfilled' ? donorsRes.value?.data || [] : [],
          notifications: notificationsRes.status === 'fulfilled' ? notificationsRes.value?.data || [] : [],
          analytics: analyticsRes.status === 'fulfilled' ? analyticsRes.value?.data || {} : {},
        };

        setData(fallbackData);
      } catch {
        setError(err?.response?.data?.message || err.message || 'Failed to load hospital dashboard.');
      }
    } finally {
      setLoading(false);
    }
  }, [user?.fullName, user?.name]);

  const hospitalTopics = useMemo(() => {
    const list = [];
    if (user?.id) {
      list.push(`/topic/notifications/${user.id}`);
      list.push(`/topic/hospital/${user.id}`);
      list.push(`/topic/hospitals/${user.id}/emergency-updates`);
    }
    list.push('/topic/hospitals/live-responses');
    return list;
  }, [user?.id]);

  const { isConnected, isFallback } = useWebSocket(hospitalTopics, (eventData) => {
    console.log('⚡ Real-time STOMP event received in Hospital Dashboard:', eventData);
    fetchDashboard();
  });

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  if (loading) {
    return <LoadingSkeleton />;
  }

  if (error && !data) {
    return <ErrorState message={error} onRetry={fetchDashboard} />;
  }

  const {
    hospitalName,
    verificationStatus,
    statistics = {},
    recentRequests = [],
    emergencyRequests = [],
    recentDonations = [],
    nearbyDonors = [],
    notifications = [],
    analytics = {},
  } = data || {};

  return (
    <div className="flex flex-col gap-8 pb-12 font-sans">
      <div className="flex items-center justify-between">
        <Header
          hospitalName={hospitalName}
          verificationStatus={verificationStatus}
          onOpenNotifications={() => setShowNotificationDrawer(true)}
          unreadNotificationsCount={notifications.filter(n => !n.readStatus).length}
        />
        <div className="flex items-center gap-3">
          {isConnected ? (
            <span className="flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400 font-semibold bg-emerald-50 dark:bg-emerald-950/50 px-2.5 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
              <Wifi className="h-3.5 w-3.5" /> STOMP Live
            </span>
          ) : (
            <span className="flex items-center gap-1.5 text-xs text-amber-600 dark:text-amber-400 font-semibold bg-amber-50 dark:bg-amber-950/50 px-2.5 py-1 rounded-full border border-amber-200 dark:border-amber-800">
              <WifiOff className="h-3.5 w-3.5" /> {isFallback ? 'REST Fallback' : 'Reconnecting...'}
            </span>
          )}
          <button
            onClick={fetchDashboard}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-bold text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-sm hover:bg-slate-50 transition-all"
          >
            <RefreshCw className="h-3.5 w-3.5" /> Refresh
          </button>
        </div>
      </div>

      <QuickActions />

      <DashboardCards statistics={statistics} />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 flex flex-col gap-8">
          <EmergencyRequests requests={emergencyRequests} onRefresh={fetchDashboard} />
          {emergencyRequests.length > 0 && emergencyRequests[0].id && (
            <EmergencyRequestResponses requestId={emergencyRequests[0].id} />
          )}
          <RecentRequests requests={recentRequests} onRefresh={fetchDashboard} />
          <RecentDonations donations={recentDonations} />
        </div>

        <div className="flex flex-col gap-8">
          <Analytics analytics={analytics} />
          <NearbyDonors donors={nearbyDonors} />
        </div>
      </div>

      <Notifications
        isOpen={showNotificationDrawer}
        onClose={() => setShowNotificationDrawer(false)}
        notifications={notifications}
        onRefresh={fetchDashboard}
      />
    </div>
  );
}
