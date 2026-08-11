import React, { useState, useRef, useEffect, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { 
  useUnreadNotifications, 
  useMarkNotificationAsRead, 
  useMarkAllNotificationsAsRead, 
  useDeleteNotification 
} from '../../hooks/useNotifications';
import { useWebSocket } from '../../hooks/useWebSocket';
import useAuthStore from '../../store/authStore';
import { Bell, Check, Trash2, ExternalLink, Wifi, WifiOff } from 'lucide-react';
import NotificationBadge from './NotificationBadge';

function timeAgo(dateString) {
  if (!dateString) return '';
  const now = new Date();
  const date = new Date(dateString);
  const seconds = Math.floor((now - date) / 1000);
  if (seconds < 60) return 'Just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

/**
 * Production notification dropdown bell component that displays live unread notifications,
 * relative timestamps, mark-as-read, delete action, route navigation, and STOMP WebSocket real-time updates.
 */
export default function NotificationBell() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuthStore();
  const { data: unreadList } = useUnreadNotifications();
  const { mutateAsync: markAsRead } = useMarkNotificationAsRead();
  const { mutateAsync: markAllAsRead } = useMarkAllNotificationsAsRead();
  const { mutateAsync: deleteNotification } = useDeleteNotification();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const topics = useMemo(() => {
    if (!user?.id) return [];
    return [
      `/topic/notifications/${user.id}`,
      `/topic/notifications/${user.id}/unread-count`
    ];
  }, [user?.id]);

  const { isConnected } = useWebSocket(topics, (eventData) => {
    console.log('🔔 Realtime STOMP Notification received in Bell:', eventData);
    queryClient.invalidateQueries(['unreadNotifications']);
    queryClient.invalidateQueries(['notifications']);
    queryClient.invalidateQueries(['notificationCount']);
  });

  const notificationsArray = Array.isArray(unreadList) ? unreadList : (unreadList?.data || []);
  const unreadCount = notificationsArray.length;

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleNotificationClick = async (notif) => {
    setIsOpen(false);
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

  const handleMarkAllRead = async (e) => {
    e.stopPropagation();
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

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2.5 text-slate-500 dark:text-slate-300 hover:text-primary dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-slate-800 rounded-xl transition-all outline-none focus-visible:ring-2 focus-visible:ring-primary flex items-center gap-1"
        title={isConnected ? 'Real-time WebSocket Connected' : 'WebSocket Reconnecting...'}
        aria-label="Notifications"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 h-5 w-5 rounded-full bg-primary text-white text-[10px] font-black flex items-center justify-center border-2 border-white dark:border-slate-900 animate-pulse">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl shadow-2xl z-50 flex flex-col overflow-hidden animate-in fade-in slide-in-from-top-2">
          {/* Header */}
          <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50/70 dark:bg-slate-800/50">
            <span className="text-xs font-bold text-slate-800 dark:text-slate-100 flex items-center gap-2">
              Notifications
              {unreadCount > 0 && (
                <span className="bg-primary/10 text-primary text-[10px] font-bold px-2 py-0.5 rounded-full">
                  {unreadCount} new
                </span>
              )}
              {isConnected ? (
                <span className="flex items-center gap-1 text-[10px] text-emerald-600 dark:text-emerald-400 font-semibold bg-emerald-50 dark:bg-emerald-950/40 px-1.5 py-0.5 rounded-md">
                  <Wifi className="h-3 w-3" /> Live
                </span>
              ) : (
                <span className="flex items-center gap-1 text-[10px] text-amber-600 dark:text-amber-400 font-semibold bg-amber-50 dark:bg-amber-950/40 px-1.5 py-0.5 rounded-md">
                  <WifiOff className="h-3 w-3" /> Offline
                </span>
              )}
            </span>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllRead}
                className="text-[11px] font-semibold text-primary dark:text-red-400 hover:underline flex items-center gap-1 transition-colors"
              >
                <Check className="h-3.5 w-3.5" /> Mark all read
              </button>
            )}
          </div>

          {/* List preview */}
          <div className="max-h-80 overflow-y-auto flex flex-col divide-y divide-slate-100 dark:divide-slate-800">
            {notificationsArray.length > 0 ? (
              notificationsArray.map((notif) => (
                <div
                  key={notif.id}
                  onClick={() => handleNotificationClick(notif)}
                  className="px-4 py-3.5 flex flex-col gap-1.5 hover:bg-slate-50 dark:hover:bg-slate-800/60 text-left w-full transition-all cursor-pointer group relative"
                >
                  <div className="flex items-center justify-between gap-2">
                    <NotificationBadge type={notif.notificationType} />
                    <span className="text-[10px] text-slate-400 dark:text-slate-500 font-medium">
                      {timeAgo(notif.createdAt)}
                    </span>
                  </div>

                  <h5 className="text-xs font-bold text-slate-800 dark:text-slate-100 line-clamp-1 pr-6">
                    {notif.title}
                  </h5>

                  <p className="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-2 leading-relaxed">
                    {notif.message}
                  </p>

                  <div className="flex items-center justify-between mt-1 pt-1">
                    <span className="text-[10px] text-primary dark:text-red-400 font-bold flex items-center gap-1 group-hover:underline">
                      View details <ExternalLink className="h-2.5 w-2.5" />
                    </span>
                    
                    <button
                      onClick={(e) => handleDelete(e, notif.id)}
                      className="text-slate-300 dark:text-slate-600 hover:text-red-500 dark:hover:text-red-400 p-1 rounded-lg transition-colors"
                      title="Delete notification"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <div className="py-10 text-center flex flex-col items-center gap-2">
                <Bell className="h-8 w-8 text-slate-300 dark:text-slate-700 stroke-[1.5]" />
                <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">No unread notifications</p>
              </div>
            )}
          </div>

          {/* Footer */}
          <Link
            to="/notifications"
            onClick={() => setIsOpen(false)}
            className="block text-center py-3 bg-slate-50 dark:bg-slate-800/80 border-t border-slate-100 dark:border-slate-800 text-xs font-bold text-slate-700 dark:text-slate-200 hover:text-primary dark:hover:text-red-400 transition-all hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            View All Notifications
          </Link>
        </div>
      )}
    </div>
  );
}

export { NotificationBell };
