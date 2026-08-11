import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  useNotifications, 
  useMarkNotificationAsRead,
  useMarkAllNotificationsAsRead,
  useDeleteNotification
} from '../../hooks/useNotifications';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import NotificationPreferencesModal from '../../components/notifications/NotificationPreferencesModal';
import { 
  Bell, Check, Trash2, ExternalLink, Settings, 
  AlertTriangle, Heart, CheckCircle2, XCircle, Clock, Award, ShieldAlert
} from 'lucide-react';

function getCategoryIcon(category) {
  switch (category) {
    case 'EMERGENCY':
      return <AlertTriangle className="h-4 w-4 text-red-500" />;
    case 'DONATION_APPROVED':
      return <CheckCircle2 className="h-4 w-4 text-emerald-500" />;
    case 'DONATION_COMPLETED':
      return <Heart className="h-4 w-4 text-pink-500 fill-pink-500" />;
    case 'REQUEST_CANCELLED':
      return <XCircle className="h-4 w-4 text-amber-500" />;
    case 'REMINDER':
      return <Clock className="h-4 w-4 text-indigo-500" />;
    case 'REWARD':
      return <Award className="h-4 w-4 text-yellow-500" />;
    case 'ADMIN':
    case 'SYSTEM':
    default:
      return <ShieldAlert className="h-4 w-4 text-blue-500" />;
  }
}

function getPriorityBadge(priority) {
  const p = (priority || 'NORMAL').toUpperCase();
  switch (p) {
    case 'CRITICAL':
      return <span className="bg-red-100 dark:bg-red-950 text-red-700 dark:text-red-300 font-black text-[9px] px-2 py-0.5 rounded-full uppercase tracking-wider animate-pulse">CRITICAL</span>;
    case 'HIGH':
      return <span className="bg-amber-100 dark:bg-amber-950 text-amber-700 dark:text-amber-300 font-bold text-[9px] px-2 py-0.5 rounded-full uppercase tracking-wider">HIGH</span>;
    case 'LOW':
      return <span className="bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 font-semibold text-[9px] px-2 py-0.5 rounded-full uppercase tracking-wider">LOW</span>;
    case 'NORMAL':
    default:
      return <span className="bg-blue-50 dark:bg-blue-950 text-blue-600 dark:text-blue-400 font-semibold text-[9px] px-2 py-0.5 rounded-full uppercase tracking-wider">NORMAL</span>;
  }
}

function groupNotifications(list) {
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  const groups = {
    Today: [],
    Yesterday: [],
    Earlier: []
  };

  (list || []).forEach(notif => {
    const d = new Date(notif.createdAt || notif.createdTime);
    if (d >= today) {
      groups.Today.push(notif);
    } else if (d >= yesterday) {
      groups.Yesterday.push(notif);
    } else {
      groups.Earlier.push(notif);
    }
  });

  return groups;
}

/**
 * Screen displaying complete notification logs, grouped by Today, Yesterday, Earlier,
 * supporting priority badges, category icons, preferences modal, and real-time status updates.
 */
export default function NotificationCenter() {
  const navigate = useNavigate();
  const { data: rawData, isLoading, error, refetch } = useNotifications();
  const { mutateAsync: markAsRead } = useMarkNotificationAsRead();
  const { mutateAsync: markAllAsRead } = useMarkAllNotificationsAsRead();
  const { mutateAsync: deleteNotification } = useDeleteNotification();
  
  const [filter, setFilter] = useState('all'); // 'all', 'unread', 'EMERGENCY', 'DONATION_APPROVED', etc.
  const [isPrefModalOpen, setIsPrefModalOpen] = useState(false);

  const list = useMemo(() => {
    if (!rawData) return [];
    if (Array.isArray(rawData)) return rawData;
    if (Array.isArray(rawData.notifications)) return rawData.notifications;
    if (Array.isArray(rawData.items)) return rawData.items;
    if (Array.isArray(rawData.data?.notifications)) return rawData.data.notifications;
    if (Array.isArray(rawData.data?.items)) return rawData.data.items;
    if (Array.isArray(rawData.data)) return rawData.data;
    return [];
  }, [rawData]);

  const filteredList = useMemo(() => {
    return list.filter(notif => {
      const isUnread = !notif.isRead && !notif.readStatus;
      if (filter === 'unread') return isUnread;
      if (filter !== 'all') return notif.category === filter || notif.notificationType === filter;
      return true;
    });
  }, [list, filter]);

  const grouped = useMemo(() => groupNotifications(filteredList), [filteredList]);

  const handleNotificationClick = async (notif) => {
    try {
      if (!notif.isRead && !notif.readStatus) {
        await markAsRead(notif.id);
      }
    } catch (err) {
      console.error(err);
    }

    if (notif.actionUrl) {
      navigate(notif.actionUrl);
      return;
    }

    const match = notif.message?.match(/#(\d+)/) || notif.message?.match(/\b\d+\b/);
    const targetId = match ? match[1] || match[0] : null;

    if (targetId) {
      if (['BLOOD_REQUEST_CREATED', 'REQUEST_VERIFIED', 'REQUEST_REJECTED'].includes(notif.notificationType)) {
        navigate(`/patient/requests/${targetId}`);
        return;
      }
      if (notif.notificationType === 'DONOR_MATCHED' || notif.notificationType === 'DONOR_ACCEPTED') {
        navigate('/hospital/requests');
        return;
      }
      if (['DONATION_ACCEPTED', 'DONATION_CONFIRMED', 'DONATION_COMPLETED'].includes(notif.notificationType)) {
        navigate('/donor/history');
        return;
      }
    }

    navigate('/notifications');
  };

  const handleMarkRead = async (e, id) => {
    e.stopPropagation();
    try {
      await markAsRead(id);
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    try {
      await deleteNotification(id);
    } catch (err) {
      console.error(err);
    }
  };

  const formatTime = (dateTimeStr) => {
    if (!dateTimeStr) return '';
    return new Date(dateTimeStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const unreadCount = list.filter(n => !n.isRead && !n.readStatus).length;

  if (isLoading) return <LoadingSpinner fullScreen />;
  if (error) return <ErrorState message={error.message} onRetry={refetch} />;

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto pb-12">
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Bell className="h-6 w-6 text-primary" /> Enterprise Notification Center
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Real-time categorized alerts, quiet hours rules, and priority notification delivery.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => setIsPrefModalOpen(true)}
            className="flex items-center gap-2 text-xs py-2"
          >
            <Settings className="h-4 w-4" /> Preferences
          </Button>

          {unreadCount > 0 && (
            <Button
              onClick={handleMarkAllRead}
              className="flex items-center gap-2 text-xs py-2"
            >
              <Check className="h-4 w-4" /> Mark All Read
            </Button>
          )}
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center border-b border-slate-100 dark:border-slate-800 gap-4 overflow-x-auto pb-1">
        {[
          { key: 'all', label: `All (${list.length})` },
          { key: 'unread', label: `Unread (${unreadCount})` },
          { key: 'EMERGENCY', label: 'Emergency' },
          { key: 'DONATION_APPROVED', label: 'Approved' },
          { key: 'DONATION_COMPLETED', label: 'Completed' },
          { key: 'ADMIN', label: 'Admin' },
        ].map((t) => (
          <button
            key={t.key}
            onClick={() => setFilter(t.key)}
            className={`pb-3 text-xs font-semibold whitespace-nowrap transition-all border-b-2 ${
              filter === t.key
                ? 'border-primary text-primary'
                : 'border-transparent text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Grouped Lists: Today, Yesterday, Earlier */}
      {filteredList.length > 0 ? (
        <div className="flex flex-col gap-8">
          {Object.entries(grouped).map(([groupTitle, notifs]) => {
            if (notifs.length === 0) return null;
            return (
              <div key={groupTitle} className="flex flex-col gap-3">
                <h3 className="text-xs font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider flex items-center gap-2">
                  <span>{groupTitle}</span>
                  <span className="bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 px-2 py-0.5 rounded-full text-[10px]">
                    {notifs.length}
                  </span>
                </h3>

                <div className="flex flex-col gap-3">
                  {notifs.map((notif) => {
                    const isUnread = !notif.isRead && !notif.readStatus;
                    return (
                      <Card
                        key={notif.id}
                        className={`flex items-start gap-4 p-4 border transition-all cursor-pointer hover:bg-slate-50/50 dark:hover:bg-slate-800/50 relative ${
                          isUnread 
                            ? 'border-l-4 border-l-primary bg-slate-50/40 dark:bg-slate-800/40 border-slate-200 dark:border-slate-700' 
                            : 'border-slate-200 dark:border-slate-800'
                        }`}
                        onClick={() => handleNotificationClick(notif)}
                      >
                        {/* Category Icon */}
                        <div className="p-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center shrink-0 mt-0.5">
                          {getCategoryIcon(notif.category)}
                        </div>

                        <div className="flex-1 flex flex-col gap-1.5 min-w-0">
                          <div className="flex items-center justify-between gap-3 flex-wrap pr-6">
                            <div className="flex items-center gap-2 flex-wrap">
                              {getPriorityBadge(notif.priority || notif.priorityEnum)}
                              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                                {notif.category || 'SYSTEM'}
                              </span>
                            </div>
                            <span className="text-[10px] text-slate-400 dark:text-slate-500 font-medium">
                              {formatTime(notif.createdAt || notif.createdTime)}
                            </span>
                          </div>

                          <h4 className="font-bold text-slate-800 dark:text-slate-100 text-sm truncate pr-6">
                            {notif.title}
                          </h4>
                          <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed line-clamp-2 pr-6">
                            {notif.message || notif.body}
                          </p>

                          <div className="flex items-center justify-between mt-2 pt-2 border-t border-slate-100 dark:border-slate-800/80">
                            <div className="flex items-center gap-2">
                              {isUnread && (
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleMarkRead(notif.id);
                                  }}
                                  className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400 flex items-center gap-1 border border-emerald-200 dark:border-emerald-800 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 px-2 py-0.5 rounded-md transition-all"
                                >
                                  <Check className="h-3 w-3" /> Mark read
                                </button>
                              )}
                              {notif.actionUrl && (
                                <span className="text-[10px] font-bold text-primary dark:text-red-400 flex items-center gap-1">
                                  <ExternalLink className="h-3 w-3" /> Action
                                </span>
                              )}
                            </div>

                            <button
                              onClick={(e) => handleDelete(e, notif.id)}
                              className="text-slate-300 dark:text-slate-600 hover:text-red-500 dark:hover:text-red-400 p-1 rounded-lg transition-colors"
                              title="Delete notification"
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        </div>
                      </Card>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <EmptyState
          message={
            filter === 'unread'
              ? 'You have read all of your notifications.'
              : 'No notifications found for selected filter.'
          }
          icon={Bell}
        />
      )}

      {/* Preferences Modal */}
      <NotificationPreferencesModal
        isOpen={isPrefModalOpen}
        onClose={() => setIsPrefModalOpen(false)}
      />
    </div>
  );
}
