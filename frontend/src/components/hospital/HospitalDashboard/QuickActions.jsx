import React from 'react';
import { PlusCircle, AlertCircle, Users, History, Building2, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

/**
 * Hospital Quick Actions Launch Bar.
 * Redesigned with unified teal/emerald hospital portal design.
 */
export default function QuickActions({ onCreateRequest, onCreateEmergency }) {
  const navigate = useNavigate();

  const actions = [
    {
      id: 'create-request',
      label: 'New Blood Request',
      description: 'Log patient transfusion',
      icon: PlusCircle,
      gradient: 'from-teal-600 to-emerald-600 hover:from-teal-700 hover:to-emerald-700',
      shadow: 'shadow-teal-500/25 hover:shadow-teal-500/40',
      onClick: onCreateRequest || (() => navigate('/hospital/create-request')),
    },
    {
      id: 'create-emergency',
      label: 'Emergency Request',
      description: 'Critical priority alert',
      icon: AlertCircle,
      gradient: 'from-red-600 to-rose-600 hover:from-red-700 hover:to-rose-700',
      shadow: 'shadow-red-500/25 hover:shadow-red-500/40',
      onClick: onCreateEmergency || (() => navigate('/hospital/create-request?emergency=true')),
    },
    {
      id: 'view-donors',
      label: 'Nearby Donors',
      description: 'Search available donors',
      icon: Users,
      gradient: 'from-indigo-600 to-violet-600 hover:from-indigo-700 hover:to-violet-700',
      shadow: 'shadow-indigo-500/25 hover:shadow-indigo-500/40',
      onClick: () => navigate('/hospital/donors'),
    },
    {
      id: 'donation-history',
      label: 'Donation Logs',
      description: 'Transfusion tracking',
      icon: History,
      gradient: 'from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700',
      shadow: 'shadow-emerald-500/25 hover:shadow-emerald-500/40',
      onClick: () => navigate('/hospital/donations'),
    },
    {
      id: 'hospital-profile',
      label: 'Hospital Profile',
      description: 'Manage institution data',
      icon: Building2,
      gradient: 'from-slate-700 to-slate-900 hover:from-slate-800 hover:to-black',
      shadow: 'shadow-slate-500/20 hover:shadow-slate-500/30',
      onClick: () => navigate('/hospital/profile'),
    },
  ];

  return (
    <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm my-2 overflow-hidden">
      <div className="px-5 pt-4 pb-3.5 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="h-2 w-2 rounded-full bg-teal-500" />
          <h2 className="text-xs font-bold text-slate-800 dark:text-slate-200 tracking-wide uppercase">
            Quick Actions
          </h2>
        </div>
        <span className="text-[11px] text-slate-400 dark:text-slate-500 font-medium">Hospital Portal Controls</span>
      </div>

      <div className="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3">
          {actions.map((action) => {
            const IconComponent = action.icon;
            return (
              <button
                key={action.id}
                onClick={action.onClick}
                className={`group flex items-center justify-between p-3.5 rounded-xl bg-gradient-to-r ${action.gradient} text-white shadow-md ${action.shadow} hover:shadow-lg hover:-translate-y-0.5 active:translate-y-0 transition-all duration-200 text-left`}
              >
                <div className="flex items-center gap-2.5">
                  <IconComponent className="h-4 w-4 shrink-0 group-hover:scale-110 transition-transform" />
                  <div>
                    <p className="text-xs font-bold leading-tight">{action.label}</p>
                    <p className="text-[10px] opacity-70 leading-tight mt-0.5">{action.description}</p>
                  </div>
                </div>
                <ChevronRight className="h-3.5 w-3.5 opacity-60 group-hover:opacity-100 group-hover:translate-x-0.5 transition-all shrink-0" />
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
