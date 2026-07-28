import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useNotificationDetails, useMarkNotificationAsRead } from '../../hooks/useNotifications';
import Card from '../../components/ui/Card';
import NotificationBadge from '../../components/ui/NotificationBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { ArrowLeft, Mail, Calendar, CheckSquare, ShieldCheck } from 'lucide-react';

/**
 * Screen showing complete detailed alert logs, metadata channels, and status markers.
 */
export default function NotificationDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: notif, isLoading, error, refetch } = useNotificationDetails(id);
  const { mutateAsync: markAsRead } = useMarkNotificationAsRead();

  // Auto mark notification read on load
  useEffect(() => {
    if (notif && !notif.readStatus) {
      markAsRead(notif.id).catch(console.error);
    }
  }, [notif, markAsRead]);

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  if (!notif) {
    return <ErrorState message="Notification alert details not found." />;
  }

  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return 'N/A';
    return new Date(dateTimeStr).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="flex flex-col gap-6 max-w-2xl mx-auto">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/notifications')}
          className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded-xl shadow-sm transition-all"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-gray-900">Notification Details</h1>
          <p className="text-xs text-gray-500 mt-0.5">
            Detailed information and SMTP delivery channels metadata.
          </p>
        </div>
      </div>

      <Card className="flex flex-col gap-5">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <NotificationBadge type={notif.notificationType} />
          <span className="text-xs text-gray-400 font-bold uppercase tracking-wider">
            ID #{notif.id}
          </span>
        </div>

        <div className="flex flex-col gap-2">
          <h2 className="text-base font-bold text-gray-805">{notif.title}</h2>
          <p className="text-xs text-gray-500 leading-relaxed bg-slate-50 p-4 rounded-xl border border-gray-100/50">
            {notif.message}
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs mt-2 pt-4 border-t border-gray-50">
          <div className="flex items-start gap-2.5">
            <Calendar className="h-4 w-4 text-gray-400 shrink-0 mt-0.5" />
            <div className="flex flex-col gap-0.5">
              <span className="font-semibold text-gray-700">Created Date</span>
              <span className="text-gray-450">{formatDateTime(notif.createdAt)}</span>
            </div>
          </div>

          <div className="flex items-start gap-2.5">
            <Mail className="h-4 w-4 text-gray-400 shrink-0 mt-0.5" />
            <div className="flex flex-col gap-0.5">
              <span className="font-semibold text-gray-700">Recipient Email</span>
              <span className="text-gray-450">{notif.recipientEmail || 'N/A'}</span>
            </div>
          </div>

          <div className="flex items-start gap-2.5">
            <ShieldCheck className="h-4 w-4 text-gray-400 shrink-0 mt-0.5" />
            <div className="flex flex-col gap-0.5">
              <span className="font-semibold text-gray-700">Delivery Status</span>
              <span className="text-gray-455 capitalize">
                {notif.status ? notif.status.toLowerCase() : 'N/A'}
              </span>
            </div>
          </div>

          <div className="flex items-start gap-2.5">
            <CheckSquare className="h-4 w-4 text-gray-400 shrink-0 mt-0.5" />
            <div className="flex flex-col gap-0.5">
              <span className="font-semibold text-gray-700">Channel</span>
              <span className="text-gray-450">{notif.deliveryChannel || 'INAPP'}</span>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
}
export { NotificationDetails };
