import React from 'react';
import { Bell, Clock, CheckCheck, X } from 'lucide-react';

/**
 * Notifications Slide-over Drawer for Hospital Dashboard.
 * Redesigned with consistent teal/emerald hospital design system.
 * Renders as an inline panel on the dashboard (not a modal) when isOpen is true,
 * and as a compact summary card when false.
 */
export default function Notifications({ isOpen, onClose, notifications = [], onRefresh }) {
  const formatTime = (timeStr) => {
    if (!timeStr) return 'Just now';
    const date = new Date(timeStr);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const formatBloodGroup = (bg) => {
    if (!bg) return '';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  const unreadCount = notifications.filter(n => !(n.isRead || n.readStatus)).length;

  if (!isOpen) return null;

  return (
    // Fixed overlay drawer
    <div className="fixed inset-0 z-50 flex justify-end">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Panel */}
      <div className="relative w-full max-w-md bg-white dark:bg-slate-950 shadow-2xl flex flex-col h-full animate-slide-in-right">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100 dark:border-slate-800 bg-gradient-to-r from-teal-600 to-emerald-600">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-white/20 text-white rounded-xl">
              <Bell className="h-4 w-4" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">Notifications</h2>
              <p className="text-[11px] text-teal-100/80">Donor acceptances & alerts</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            {unreadCount > 0 && (
              <span className="px-2 py-0.5 text-[10px] font-black bg-white/20 text-white rounded-full border border-white/30 uppercase">
                {unreadCount} Unread
              </span>
            )}
            <button
              onClick={onClose}
              className="p-2 text-white/70 hover:text-white hover:bg-white/20 rounded-xl transition-colors"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Notification List */}
        <div className="flex-1 overflow-y-auto divide-y divide-slate-50 dark:divide-slate-800/80">
          {notifications.length > 0 ? (
            notifications.map((n) => {
              const isUnread = !(n.isRead || n.readStatus);
              const isDonorAccepted = n.notificationType === 'DONOR_ACCEPTED' || (n.message && n.message.includes('accepted'));

              return (
                <div
                  key={n.id}
                  className={`px-5 py-4 flex items-start gap-3 transition-colors ${
                    isUnread
                      ? 'bg-teal-50/50 dark:bg-teal-950/20 border-l-2 border-teal-500'
                      : 'hover:bg-slate-50 dark:hover:bg-slate-800/30'
                  }`}
                >
                  {/* Read / Unread Dot */}
                  <div className="mt-1 shrink-0">
                    {isUnread ? (
                      <span className="h-2.5 w-2.5 rounded-full bg-teal-500 block shadow-sm shadow-teal-400/50 animate-pulse" />
                    ) : (
                      <CheckCheck className="h-4 w-4 text-slate-300 dark:text-slate-600" />
                    )}
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <div className="flex items-center gap-1.5 flex-1 min-w-0">
                        <h4 className={`text-xs font-bold truncate ${isUnread ? 'text-slate-900 dark:text-white' : 'text-slate-700 dark:text-slate-300'}`}>
                          {n.title || (isDonorAccepted ? 'Donor Accepted Request' : 'Notification')}
                        </h4>
                        {n.bloodGroup && (
                          <span className="text-[10px] font-black px-1.5 py-0.5 rounded bg-red-100 dark:bg-red-950/60 text-red-700 dark:text-red-400 shrink-0">
                            {formatBloodGroup(n.bloodGroup)}
                          </span>
                        )}
                      </div>
                      <span className="text-[10px] text-slate-400 dark:text-slate-500 font-medium flex items-center gap-1 shrink-0">
                        <Clock className="h-3 w-3" />
                        {formatTime(n.createdAt || n.sentAt || n.time)}
                      </span>
                    </div>

                    <p className="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-2 mt-1 leading-relaxed">
                      {n.message}
                    </p>

                    {(n.donorName || n.requestId) && (
                      <div className="mt-2 flex items-center justify-between text-[11px] bg-slate-50 dark:bg-slate-800/50 px-2.5 py-1.5 rounded-xl border border-slate-100 dark:border-slate-700">
                        <div className="flex items-center gap-2 text-slate-600 dark:text-slate-400">
                          {n.donorName && <span><strong>Donor:</strong> {n.donorName}</span>}
                          {n.requestId && <span className="text-slate-400">| #{n.requestId}</span>}
                        </div>
                        {n.donorId && (
                          <a
                            href={`/hospital/donors?id=${n.donorId}`}
                            className="text-[10px] font-bold text-teal-600 dark:text-teal-400 hover:underline"
                          >
                            View Profile
                          </a>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              );
            })
          ) : (
            <div className="flex flex-col items-center justify-center py-16 text-center gap-3 text-slate-400 dark:text-slate-500">
              <Bell className="h-10 w-10 text-slate-300 dark:text-slate-700 stroke-[1.5]" />
              <div>
                <p className="text-sm font-bold">No Notifications</p>
                <p className="text-[11px] mt-0.5">Real-time alerts will appear here</p>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        {onRefresh && (
          <div className="px-5 py-3 border-t border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900">
            <button
              onClick={onRefresh}
              className="w-full text-xs font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-800 transition-colors"
            >
              Refresh Notifications
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
