import React from 'react';
import { AlertTriangle, Clock, ArrowRight, ShieldAlert, Droplet } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import HospitalStatusBadge from '../common/HospitalStatusBadge';

/**
 * Emergency Blood Requests Panel for Hospital Dashboard.
 * Dark emergency theme, redesigned with consistent hospital design system.
 */
export default function EmergencyRequests({ requests = [], onViewAll }) {
  const navigate = useNavigate();

  const formatBloodGroup = (bg) => {
    if (!bg) return 'N/A';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  return (
    <div className="bg-white dark:bg-slate-900 text-slate-900 dark:text-white rounded-2xl border border-red-200/80 dark:border-red-900/50 shadow-md overflow-hidden relative flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100 dark:border-red-900/50 bg-red-50/40 dark:bg-red-950/20">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-red-100 text-red-600 dark:bg-red-600/25 dark:text-red-400 rounded-xl border border-red-200 dark:border-red-500/30 animate-pulse">
            <AlertTriangle className="h-4 w-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold tracking-wide text-slate-900 dark:text-white flex items-center gap-2">
              Critical Emergency Alerts
              {requests.length > 0 && (
                <span className="px-2 py-0.5 text-[10px] font-extrabold bg-red-600 text-white rounded-full animate-pulse">
                  {requests.length} ACTIVE
                </span>
              )}
            </h2>
            <p className="text-[11px] text-slate-500 dark:text-red-300/70">Immediate blood transfusions required</p>
          </div>
        </div>
        <button
          onClick={onViewAll || (() => navigate('/hospital/requests'))}
          className="text-[11px] font-semibold text-red-600 dark:text-red-300 hover:underline flex items-center gap-1 transition-all"
        >
          View All <ArrowRight className="h-3 w-3" />
        </button>
      </div>

      {/* Table or Empty State */}
      <div className="overflow-x-auto flex-1">
        {requests.length > 0 ? (
          <table className="w-full text-left text-xs">
            <thead className="text-slate-400 dark:text-red-300/60 font-bold uppercase tracking-wider text-[10px] border-b border-slate-100 dark:border-red-900/40 bg-slate-50 dark:bg-slate-800/40">
              <tr>
                <th className="py-3 px-5">Patient</th>
                <th className="py-3 px-4">Group</th>
                <th className="py-3 px-4">Units</th>
                <th className="py-3 px-4">Priority</th>
                <th className="py-3 px-4">Time Left</th>
                <th className="py-3 px-5 text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-red-900/20 text-slate-800 dark:text-slate-200">
              {requests.slice(0, 5).map((req) => (
                <tr
                  key={req.id}
                  onClick={() => navigate(`/hospital/requests/${req.id}`)}
                  className="hover:bg-red-50/50 dark:hover:bg-red-950/40 cursor-pointer transition-colors"
                >
                  <td className="py-3 px-5 font-bold text-slate-900 dark:text-white truncate max-w-[130px]">
                    {req.patientName || 'Emergency Patient'}
                  </td>
                  <td className="py-3 px-4">
                    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-black bg-red-50 text-red-600 border border-red-200 dark:bg-red-500/20 dark:text-red-300 dark:border-red-500/30">
                      <Droplet className="h-3 w-3 fill-current" />
                      {formatBloodGroup(req.bloodGroup || req.bloodGroupNeeded)}
                    </span>
                  </td>
                  <td className="py-3 px-4 font-semibold">{req.units || req.unitsRequired || 1} Bags</td>
                  <td className="py-3 px-4">
                    <span className="inline-flex px-2 py-0.5 text-[10px] font-black rounded-full bg-red-600 text-white uppercase tracking-wider animate-pulse">
                      {req.priority || req.urgencyLevel || 'CRITICAL'}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-amber-600 dark:text-amber-300 font-mono text-[11px]">
                    <span className="flex items-center gap-1">
                      <Clock className="h-3 w-3 text-amber-500 shrink-0" />
                      {req.timeRemaining || 'Immediate'}
                    </span>
                  </td>
                  <td className="py-3 px-5 text-right">
                    <HospitalStatusBadge status={req.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="flex flex-col items-center justify-center p-10 text-center gap-3 text-red-300/60">
            <ShieldAlert className="h-10 w-10 text-red-400/30" />
            <div>
              <p className="text-sm font-bold text-red-300/60">No Active Emergency Alerts</p>
              <p className="text-[11px] text-red-400/40 mt-0.5">Emergency blood requests will appear here in real-time</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
