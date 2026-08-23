import React, { useState, useEffect } from 'react';
import { X, ShieldAlert, Moon, Bell, Mail, Smartphone, MessageSquare, Award, Clock } from 'lucide-react';
import Button from '../ui/Button';
import api from '../../api/axios';

/**
 * Modal dialog component for configuring Notification Delivery Preferences and Quiet Hours.
 */
export default function NotificationPreferencesModal({ isOpen, onClose }) {
  const [preferences, setPreferences] = useState({
    emailEnabled: true,
    pushEnabled: true,
    webSocketEnabled: true,
    emergencyAlertsEnabled: true, // Always ON
    rewardNotificationsEnabled: true,
    reminderNotificationsEnabled: true,
    adminMessagesEnabled: true,
    quietHoursEnabled: false,
    quietHoursStart: '22:00',
    quietHoursEnd: '07:00',
    timezone: 'UTC',
  });
  const [loading, setLoading] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      fetchPreferences();
    }
  }, [isOpen]);

  const fetchPreferences = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/preferences');
      const data = response.data?.data || response.data;
      if (data) {
        setPreferences({
          emailEnabled: data.emailEnabled ?? true,
          pushEnabled: data.pushEnabled ?? true,
          webSocketEnabled: data.webSocketEnabled ?? true,
          emergencyAlertsEnabled: true, // Lock to true always
          rewardNotificationsEnabled: data.rewardNotificationsEnabled ?? true,
          reminderNotificationsEnabled: data.reminderNotificationsEnabled ?? true,
          adminMessagesEnabled: data.adminMessagesEnabled ?? true,
          quietHoursEnabled: data.quietHoursEnabled ?? false,
          quietHoursStart: data.quietHoursStart || '22:00',
          quietHoursEnd: data.quietHoursEnd || '07:00',
          timezone: data.timezone || 'UTC',
        });
      }
    } catch (err) {
      console.error('Failed to load preferences:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setLoading(true);
    setSaved(false);
    setError(null);
    try {
      const payload = { ...preferences, emergencyAlertsEnabled: true };
      await api.put('/preferences', payload);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      console.error('Error saving preferences:', err);
      setError('Failed to save preferences.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl shadow-2xl max-w-lg w-full overflow-hidden flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50/70 dark:bg-slate-800/50">
          <div className="flex items-center gap-2">
            <Bell className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-bold text-slate-900 dark:text-white">Notification Preferences</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-full transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Modal Content */}
        <div className="p-6 overflow-y-auto flex flex-col gap-6">
          {saved && (
            <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 text-xs font-semibold rounded-xl">
              Preferences saved successfully!
            </div>
          )}

          {error && (
            <div className="p-3 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 text-xs font-semibold rounded-xl">
              {error}
            </div>
          )}

          {/* Delivery Channels */}
          <div>
            <h3 className="text-xs font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-3">
              Delivery Channels
            </h3>
            <div className="flex flex-col gap-3">
              <label className="flex items-center justify-between p-3 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 cursor-pointer">
                <span className="flex items-center gap-2.5 text-xs font-semibold text-slate-800 dark:text-slate-200">
                  <Mail className="h-4 w-4 text-blue-500" /> Email Notifications
                </span>
                <input
                  type="checkbox"
                  checked={preferences.emailEnabled}
                  onChange={(e) => setPreferences({ ...preferences, emailEnabled: e.target.checked })}
                  className="rounded text-primary focus:ring-primary h-4 w-4"
                />
              </label>

              <label className="flex items-center justify-between p-3 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 cursor-pointer">
                <span className="flex items-center gap-2.5 text-xs font-semibold text-slate-800 dark:text-slate-200">
                  <Smartphone className="h-4 w-4 text-emerald-500" /> Mobile Push Notifications
                </span>
                <input
                  type="checkbox"
                  checked={preferences.pushEnabled}
                  onChange={(e) => setPreferences({ ...preferences, pushEnabled: e.target.checked })}
                  className="rounded text-primary focus:ring-primary h-4 w-4"
                />
              </label>

              <label className="flex items-center justify-between p-3 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 cursor-pointer">
                <span className="flex items-center gap-2.5 text-xs font-semibold text-slate-800 dark:text-slate-200">
                  <MessageSquare className="h-4 w-4 text-amber-500" /> WebSocket Popups
                </span>
                <input
                  type="checkbox"
                  checked={preferences.webSocketEnabled}
                  onChange={(e) => setPreferences({ ...preferences, webSocketEnabled: e.target.checked })}
                  className="rounded text-primary focus:ring-primary h-4 w-4"
                />
              </label>
            </div>
          </div>

          {/* Categories */}
          <div>
            <h3 className="text-xs font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-3">
              Categories & Alerts
            </h3>
            <div className="flex flex-col gap-3">
              {/* Emergency Alerts - Locked Always ON */}
              <div className="flex items-center justify-between p-3 rounded-2xl border border-red-100 dark:border-red-950 bg-red-50/30 dark:bg-red-950/20">
                <span className="flex items-center gap-2.5 text-xs font-bold text-red-700 dark:text-red-400">
                  <ShieldAlert className="h-4 w-4" /> Emergency Blood Alerts
                </span>
                <span className="bg-red-100 dark:bg-red-900/60 text-red-800 dark:text-red-300 text-[10px] font-black px-2.5 py-1 rounded-full uppercase tracking-wider">
                  Always ON
                </span>
              </div>

              <label className="flex items-center justify-between p-3 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 cursor-pointer">
                <span className="flex items-center gap-2.5 text-xs font-semibold text-slate-800 dark:text-slate-200">
                  <Award className="h-4 w-4 text-amber-500" /> Reward Notifications
                </span>
                <input
                  type="checkbox"
                  checked={preferences.rewardNotificationsEnabled}
                  onChange={(e) => setPreferences({ ...preferences, rewardNotificationsEnabled: e.target.checked })}
                  className="rounded text-primary focus:ring-primary h-4 w-4"
                />
              </label>

              <label className="flex items-center justify-between p-3 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 cursor-pointer">
                <span className="flex items-center gap-2.5 text-xs font-semibold text-slate-800 dark:text-slate-200">
                  <Clock className="h-4 w-4 text-indigo-500" /> Reminder Notifications
                </span>
                <input
                  type="checkbox"
                  checked={preferences.reminderNotificationsEnabled}
                  onChange={(e) => setPreferences({ ...preferences, reminderNotificationsEnabled: e.target.checked })}
                  className="rounded text-primary focus:ring-primary h-4 w-4"
                />
              </label>

              <label className="flex items-center justify-between p-3 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 cursor-pointer">
                <span className="flex items-center gap-2.5 text-xs font-semibold text-slate-800 dark:text-slate-200">
                  <Bell className="h-4 w-4 text-slate-500" /> Admin & System Messages
                </span>
                <input
                  type="checkbox"
                  checked={preferences.adminMessagesEnabled}
                  onChange={(e) => setPreferences({ ...preferences, adminMessagesEnabled: e.target.checked })}
                  className="rounded text-primary focus:ring-primary h-4 w-4"
                />
              </label>
            </div>
          </div>

          {/* Quiet Hours */}
          <div className="border-t border-slate-100 dark:border-slate-800 pt-5">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <Moon className="h-4 w-4 text-purple-500" />
                <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200">Quiet Hours</h3>
              </div>
              <input
                type="checkbox"
                checked={preferences.quietHoursEnabled}
                onChange={(e) => setPreferences({ ...preferences, quietHoursEnabled: e.target.checked })}
                className="rounded text-primary focus:ring-primary h-4 w-4"
              />
            </div>
            <p className="text-[11px] text-slate-400 dark:text-slate-500 mb-3">
              Suppress non-emergency alerts (rewards, reminders, admin) during specified hours. Emergency requests strictly bypass quiet hours.
            </p>

            {preferences.quietHoursEnabled && (
              <div className="grid grid-cols-2 gap-3 pt-2">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase mb-1">
                    Start Time
                  </label>
                  <input
                    type="time"
                    value={preferences.quietHoursStart}
                    onChange={(e) => setPreferences({ ...preferences, quietHoursStart: e.target.value })}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase mb-1">
                    End Time
                  </label>
                  <input
                    type="time"
                    value={preferences.quietHoursEnd}
                    onChange={(e) => setPreferences({ ...preferences, quietHoursEnd: e.target.value })}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
                  />
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/50 flex items-center justify-end gap-3">
          <Button variant="outline" onClick={onClose} disabled={loading} className="text-xs py-2">
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={loading} className="text-xs py-2">
            {loading ? 'Saving...' : 'Save Preferences'}
          </Button>
        </div>
      </div>
    </div>
  );
}
