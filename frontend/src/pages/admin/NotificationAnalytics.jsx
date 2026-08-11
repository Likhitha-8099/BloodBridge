import React from 'react';
import { useNotificationStatistics } from '../../hooks/useAdminStatistics';
import StatCard from '../../components/ui/StatCard';
import PieChartCard from '../../components/ui/PieChartCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Bell, CheckCircle, ShieldAlert, Clock } from 'lucide-react';

/**
 * Screen displaying notification logs, unread ratios, and delivery failures.
 */
export default function NotificationAnalytics() {
  const { data: stats, isLoading, error } = useNotificationStatistics();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  const notificationData = [
    { name: 'Sent Successfully', value: stats?.sentNotifications || 0 },
    { name: 'Failed Delivery', value: stats?.failedNotifications || 0 },
    { name: 'Unread Alert', value: stats?.unreadNotifications || 0 },
  ].filter(n => n.value > 0);

  return (
    <div className="flex flex-col gap-6 font-sans">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Notification Channels Analytics</h1>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Audit notification dispatch rates, delivery channel logs, and queue states.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Notifications"
          value={stats?.totalNotifications}
          icon={Bell}
          iconColor="text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/60"
        />
        <StatCard
          title="Sent Alerts"
          value={stats?.sentNotifications}
          icon={CheckCircle}
          iconColor="text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60"
        />
        <StatCard
          title="Failed Alerts"
          value={stats?.failedNotifications}
          icon={ShieldAlert}
          iconColor="text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/60"
        />
        <StatCard
          title="Unread Alerts"
          value={stats?.unreadNotifications}
          icon={Clock}
          iconColor="text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/60"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="md:col-span-1">
          {notificationData.length > 0 ? (
            <PieChartCard title="Delivery Status Distribution" data={notificationData} />
          ) : (
            <div className="h-64 flex items-center justify-center border border-slate-100 dark:border-slate-800 rounded-3xl text-xs text-slate-400 dark:text-slate-500 bg-white dark:bg-slate-900">
              No notification transactions logged.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
