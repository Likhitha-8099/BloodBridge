import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifications, useMarkNotificationAsRead } from '../../hooks/useNotifications';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import NotificationBadge from '../../components/ui/NotificationBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import { Bell, Check, Eye } from 'lucide-react';

/**
 * Screen displaying notifications logs, allowing filter tabs, mark all read, and click routing.
 */
export default function NotificationCenter() {
  const navigate = useNavigate();
  const { data: list, isLoading, error, refetch } = useNotifications();
  const { mutateAsync: markAsRead } = useMarkNotificationAsRead();
  const [filter, setFilter] = useState('all'); // 'all' or 'unread'

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const handleMarkRead = async (id) => {
    try {
      await markAsRead(id);
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllRead = async () => {
    if (!list) return;
    const unread = list.filter(n => !n.readStatus);
    try {
      for (const notif of unread) {
        await markAsRead(notif.id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const filteredList = (list || []).filter(notif => {
    if (filter === 'unread') {
      return !notif.readStatus;
    }
    return true;
  });

  const handleNotificationClick = async (notif) => {
    try {
      if (!notif.readStatus) {
        await markAsRead(notif.id);
      }
    } catch (err) {
      console.error(err);
    }

    // Parse target route from text
    const match = notif.message?.match(/#(\d+)/) || notif.message?.match(/\b\d+\b/);
    const targetId = match ? match[1] || match[0] : null;

    if (targetId) {
      if (['BLOOD_REQUEST_CREATED', 'REQUEST_VERIFIED', 'REQUEST_REJECTED'].includes(notif.notificationType)) {
        navigate(`/patient/requests/${targetId}`);
        return;
      }
      if (notif.notificationType === 'DONOR_MATCHED') {
        navigate(`/hospital/matches?requestId=${targetId}`);
        return;
      }
      if (['DONATION_ACCEPTED', 'DONATION_CONFIRMED', 'DONATION_COMPLETED'].includes(notif.notificationType)) {
        navigate('/donor/history');
        return;
      }
    }

    navigate(`/notifications/${notif.id}`);
  };

  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return 'N/A';
    return new Date(dateTimeStr).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const unreadCount = list ? list.filter(n => !n.readStatus).length : 0;

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Notification Center</h1>
          <p className="text-xs text-gray-500 mt-1">
            Review alerts, matched donations confirmations, and compatibility updates.
          </p>
        </div>

        {unreadCount > 0 && (
          <Button
            variant="outline"
            onClick={handleMarkAllRead}
            className="flex items-center gap-2 text-xs py-2"
          >
            <Check className="h-4 w-4" /> Mark All as Read
          </Button>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center border-b border-gray-100 gap-6">
        <button
          onClick={() => setFilter('all')}
          className={`pb-3 text-sm font-semibold transition-all border-b-2 ${
            filter === 'all'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-400 hover:text-gray-600'
          }`}
        >
          All Notifications ({list ? list.length : 0})
        </button>
        <button
          onClick={() => setFilter('unread')}
          className={`pb-3 text-sm font-semibold transition-all border-b-2 ${
            filter === 'unread'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-400 hover:text-gray-600'
          }`}
        >
          Unread ({unreadCount})
        </button>
      </div>

      {filteredList.length > 0 ? (
        <div className="flex flex-col gap-4">
          {filteredList.map((notif) => (
            <Card
              key={notif.id}
              className={`flex items-start gap-4 p-4 border transition-all cursor-pointer hover:bg-slate-50/40 relative ${
                !notif.readStatus ? 'border-l-4 border-l-primary bg-slate-50/20' : 'border-gray-105'
              }`}
              onClick={() => handleNotificationClick(notif)}
            >
              {!notif.readStatus && (
                <span className="absolute top-4 right-4 h-2.5 w-2.5 rounded-full bg-primary" />
              )}

              <div className="flex-1 flex flex-col gap-2">
                <div className="flex items-center justify-between gap-4 flex-wrap">
                  <NotificationBadge type={notif.notificationType} />
                  <span className="text-[10px] text-gray-400 font-medium">
                    {formatDateTime(notif.createdAt)}
                  </span>
                </div>

                <div className="flex flex-col gap-1 pr-6">
                  <h3 className="font-bold text-gray-800 text-sm">{notif.title}</h3>
                  <p className="text-xs text-gray-500 leading-relaxed pr-6">{notif.message}</p>
                </div>

                <div className="flex items-center gap-3 mt-2 self-end sm:self-start">
                  {!notif.readStatus && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleMarkRead(notif.id);
                      }}
                      className="text-[10px] font-bold text-green-600 hover:text-green-850 flex items-center gap-0.5 border border-green-200 hover:bg-green-50/40 px-2 py-0.5 rounded-lg transition-all"
                    >
                      <Check className="h-3 w-3" /> Mark read
                    </button>
                  )}
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`/notifications/${notif.id}`);
                    }}
                    className="text-[10px] font-bold text-slate-500 hover:text-slate-800 flex items-center gap-0.5 border border-slate-200 hover:bg-slate-100/40 px-2 py-0.5 rounded-lg transition-all"
                  >
                    <Eye className="h-3 w-3" /> View Details
                  </button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <EmptyState
          message={
            filter === 'unread'
              ? 'You have read all of your notifications.'
              : 'You have no notifications.'
          }
          icon={Bell}
        />
      )}
    </div>
  );
}
