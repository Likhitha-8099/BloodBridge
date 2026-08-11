import React from 'react';
import { FileText, Clock, CheckCircle2, HeartHandshake, AlertTriangle, Users, Bell } from 'lucide-react';

/**
 * Hospital Dashboard KPI Statistics Cards.
 * Redesigned with teal/emerald hospital design system for consistency.
 */
export default function DashboardCards({ statistics = {} }) {
  const cards = [
    {
      id: 'total',
      title: 'Total Requests',
      value: statistics.totalRequests ?? 0,
      subtitle: 'All time logged',
      icon: FileText,
      accentColor: 'from-teal-500 to-cyan-600',
      iconBg: 'bg-teal-600',
      valueBg: 'text-teal-700 dark:text-teal-300',
    },
    {
      id: 'pending',
      title: 'Pending Requests',
      value: statistics.pendingRequests ?? 0,
      subtitle: 'Awaiting action',
      icon: Clock,
      accentColor: 'from-amber-500 to-orange-600',
      iconBg: 'bg-amber-500',
      valueBg: 'text-amber-700 dark:text-amber-300',
    },
    {
      id: 'accepted',
      title: 'Accepted Requests',
      value: statistics.acceptedRequests ?? 0,
      subtitle: 'Verified & active',
      icon: CheckCircle2,
      accentColor: 'from-indigo-500 to-purple-600',
      iconBg: 'bg-indigo-600',
      valueBg: 'text-indigo-700 dark:text-indigo-300',
    },
    {
      id: 'completed',
      title: 'Completed Donations',
      value: statistics.completedDonations ?? 0,
      subtitle: 'Transfusions logged',
      icon: HeartHandshake,
      accentColor: 'from-emerald-500 to-teal-600',
      iconBg: 'bg-emerald-600',
      valueBg: 'text-emerald-700 dark:text-emerald-300',
    },
    {
      id: 'emergency',
      title: 'Emergency Requests',
      value: statistics.emergencyRequests ?? 0,
      subtitle: 'Critical priority',
      icon: AlertTriangle,
      accentColor: 'from-red-600 to-rose-700',
      iconBg: 'bg-red-600',
      valueBg: 'text-red-700 dark:text-red-300',
    },
    {
      id: 'donors',
      title: 'Nearby Donors',
      value: statistics.nearbyDonors ?? 0,
      subtitle: 'Available radius',
      icon: Users,
      accentColor: 'from-teal-500 to-emerald-600',
      iconBg: 'bg-teal-600',
      valueBg: 'text-teal-700 dark:text-teal-300',
    },
    {
      id: 'unread',
      title: 'Unread Notifications',
      value: statistics.unreadNotifications ?? 0,
      subtitle: 'New alerts',
      icon: Bell,
      accentColor: 'from-violet-500 to-purple-600',
      iconBg: 'bg-violet-600',
      valueBg: 'text-violet-700 dark:text-violet-300',
    },
  ];

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-7 gap-3 my-2">
      {cards.map((card) => {
        const IconComponent = card.icon;
        return (
          <div
            key={card.id}
            className="group relative bg-white dark:bg-slate-900 rounded-2xl p-4 border border-slate-100 dark:border-slate-800 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 overflow-hidden flex flex-col justify-between cursor-default"
          >
            {/* Gradient Top Stripe */}
            <div className={`absolute top-0 left-0 right-0 h-1 bg-gradient-to-r ${card.accentColor}`} />

            <div className="flex items-start justify-between gap-2 mb-3 pt-1">
              <div className={`p-2.5 rounded-xl shadow-sm ${card.iconBg} text-white shrink-0`}>
                <IconComponent className="h-4 w-4" />
              </div>
              <span className={`text-2xl font-black tracking-tight group-hover:scale-110 transition-transform ${card.valueBg}`}>
                {card.value}
              </span>
            </div>

            <div>
              <h3 className="text-[11px] font-bold text-slate-700 dark:text-slate-300 leading-tight">{card.title}</h3>
              <p className="text-[10px] text-slate-400 dark:text-slate-500 font-medium mt-0.5">{card.subtitle}</p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
