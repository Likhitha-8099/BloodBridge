import React, { useState, useRef, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useUnreadNotifications, useMarkNotificationAsRead } from '../../hooks/useNotifications';
import { Bell, Check } from 'lucide-react';
import NotificationBadge from './NotificationBadge';

/**
 * Dropdown alert bell that polls unread notification logs, shows count badges, and lists previews.
 */
export default function NotificationBell() {
  const navigate = useNavigate();
  const { data: unreadList } = useUnreadNotifications();
  const { mutateAsync: markAsRead } = useMarkNotificationAsRead();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const count = unreadList ? unreadList.length : 0;

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
      if (!notif.readStatus) {
        await markAsRead(notif.id);
      }
    } catch (err) {
      console.error(err);
    }
    
    // Parse target route
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

  const handleMarkAllRead = async () => {
    if (!unreadList) return;
    try {
      for (const notif of unreadList) {
        await markAsRead(notif.id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2.5 text-gray-400 hover:text-primary hover:bg-red-50 rounded-xl transition-all"
        title="Notifications"
      >
        <Bell className="h-5 w-5" />
        {count > 0 && (
          <span className="absolute top-1 right-1 h-5 w-5 rounded-full bg-primary text-white text-[10px] font-black flex items-center justify-center border-2 border-white animate-pulse">
            {count}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-white border border-gray-150 rounded-3xl shadow-xl z-50 flex flex-col overflow-hidden">
          {/* Header */}
          <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between bg-slate-50/50">
            <span className="text-xs font-bold text-gray-800">Notifications ({count})</span>
            {count > 0 && (
              <button
                onClick={handleMarkAllRead}
                className="text-[10px] font-semibold text-primary hover:underline flex items-center gap-0.5"
              >
                <Check className="h-3 w-3" /> Mark all read
              </button>
            )}
          </div>

          {/* List preview */}
          <div className="max-h-64 overflow-y-auto flex flex-col divide-y divide-gray-50">
            {unreadList && unreadList.length > 0 ? (
              unreadList.map((notif) => (
                <button
                  key={notif.id}
                  onClick={() => handleNotificationClick(notif)}
                  className="px-4 py-3 flex flex-col gap-1.5 hover:bg-slate-50/60 text-left w-full transition-all"
                >
                  <div className="flex items-center justify-between gap-2">
                    <NotificationBadge type={notif.notificationType} />
                    <span className="text-[9px] text-gray-400">
                      {new Date(notif.createdAt).toLocaleTimeString(undefined, { 
                        hour: '2-digit', 
                        minute: '2-digit' 
                      })}
                    </span>
                  </div>
                  <h5 className="text-xs font-bold text-gray-800 line-clamp-1">{notif.title}</h5>
                  <p className="text-[11px] text-gray-500 line-clamp-2 leading-normal">{notif.message}</p>
                </button>
              ))
            ) : (
              <div className="py-8 text-center text-xs text-gray-450">No unread notifications.</div>
            )}
          </div>

          {/* Footer */}
          <Link
            to="/notifications"
            onClick={() => setIsOpen(false)}
            className="block text-center py-2.5 bg-slate-50 border-t border-gray-100 text-xs font-bold text-slate-700 hover:text-slate-900 transition-all hover:bg-slate-100/50"
          >
            View All Notifications
          </Link>
        </div>
      )}
    </div>
  );
}
export { NotificationBell };
