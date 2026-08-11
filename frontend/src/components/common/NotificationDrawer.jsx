import React from 'react';
import { X, Bell, CheckCheck, Clock, ShieldAlert, Heart } from 'lucide-react';

/**
 * Slide-over Notification Drawer displaying unified notification history.
 */
export default function NotificationDrawer({ isOpen, onClose, notifications = [], onMarkAllRead }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs transition-opacity"
        onClick={onClose}
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white dark:bg-slate-900 shadow-2xl border-l border-slate-100 dark:border-slate-800 flex flex-col">
          {/* Drawer Header */}
          <div className="p-5 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="h-9 w-9 rounded-xl bg-red-50 text-red-600 dark:bg-red-950/60 dark:text-red-400 flex items-center justify-center">
                <Bell className="h-5 w-5" />
              </div>
              <div>
                <h3 className="font-bold text-base text-gray-900 dark:text-white">Notification Center</h3>
                <p className="text-xs text-gray-400">Updates & Emergency Alerts</p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              {onMarkAllRead && (
                <button 
                  onClick={onMarkAllRead}
                  className="text-xs text-primary font-semibold hover:underline flex items-center gap-1"
                >
                  <CheckCheck className="h-3.5 w-3.5" /> Read All
                </button>
              )}
              <button 
                onClick={onClose}
                className="p-1.5 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-gray-400 hover:text-gray-600 transition"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
          </div>

          {/* Drawer List */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {notifications && notifications.length > 0 ? (
              notifications.map((item) => (
                <div 
                  key={item.id}
                  className={`p-4 rounded-2xl border transition-all ${
                    item.read 
                      ? 'bg-slate-50/60 dark:bg-slate-800/30 border-slate-100 dark:border-slate-800 opacity-80' 
                      : 'bg-white dark:bg-slate-900 border-red-100 dark:border-red-900/30 shadow-xs'
                  }`}
                >
                  <div className="flex items-start gap-3">
                    <div className={`h-8 w-8 rounded-xl shrink-0 flex items-center justify-center mt-0.5 ${
                      item.type === 'CRITICAL' ? 'bg-red-500 text-white' : 'bg-red-50 text-red-600 dark:bg-red-950/60 dark:text-red-400'
                    }`}>
                      {item.type === 'CRITICAL' ? <ShieldAlert className="h-4 w-4" /> : <Heart className="h-4 w-4" />}
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-2 mb-1">
                        <h4 className="font-bold text-xs text-gray-900 dark:text-white truncate">{item.title || item.message}</h4>
                        <span className="text-[10px] text-gray-400 shrink-0 flex items-center gap-1">
                          <Clock className="h-3 w-3" /> {item.createdAt ? new Date(item.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Now'}
                        </span>
                      </div>
                      <p className="text-xs text-gray-600 dark:text-slate-300 leading-relaxed">{item.content || item.message}</p>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-16 flex flex-col items-center gap-2">
                <Bell className="h-10 w-10 text-gray-300 dark:text-slate-700 stroke-[1.5]" />
                <span className="text-xs font-medium text-gray-400">No new notifications.</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
