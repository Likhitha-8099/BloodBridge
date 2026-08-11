import React from 'react';
import { Eye, ArrowRight, FileText, Droplet } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import HospitalStatusBadge from '../common/HospitalStatusBadge';

/**
 * Recent Blood Requests Mini-Table for Hospital Dashboard.
 * Redesigned with consistent hospital design system.
 */
export default function RecentRequests({ requests = [], onViewAll }) {
  const navigate = useNavigate();

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const formatBloodGroup = (bg) => {
    if (!bg) return 'N/A';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  return (
    <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm overflow-hidden flex flex-col">
      {/* Header */}
      <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50/50 dark:bg-slate-800/30">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 rounded-xl border border-teal-100 dark:border-teal-900/40">
            <FileText className="h-4 w-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-800 dark:text-white">Recent Blood Requests</h2>
            <p className="text-[11px] text-slate-400 dark:text-slate-500">Latest 5 registered requests</p>
          </div>
        </div>
        <button
          onClick={onViewAll || (() => navigate('/hospital/requests'))}
          className="text-[11px] font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-800 dark:hover:text-teal-200 flex items-center gap-1 hover:underline transition-all"
        >
          View All <ArrowRight className="h-3 w-3" />
        </button>
      </div>

      {/* Table / Empty State */}
      <div className="overflow-x-auto flex-1">
        {requests.length > 0 ? (
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider text-[10px] border-b border-slate-100 dark:border-slate-800">
              <tr>
                <th className="py-3 px-5">Patient Name</th>
                <th className="py-3 px-4">Blood Group</th>
                <th className="py-3 px-4">Units</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Date</th>
                <th className="py-3 px-5 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50 dark:divide-slate-800/80 text-slate-700 dark:text-slate-300">
              {requests.slice(0, 5).map((req) => (
                <tr key={req.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors">
                  <td className="py-3 px-5 font-semibold text-slate-900 dark:text-white truncate max-w-[140px]">
                    {req.patientName || 'Emergency Patient'}
                  </td>
                  <td className="py-3 px-4">
                    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-black bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 border border-red-100 dark:border-red-900/40">
                      <Droplet className="h-3 w-3 fill-current" />
                      {formatBloodGroup(req.bloodGroup || req.bloodGroupNeeded)}
                    </span>
                  </td>
                  <td className="py-3 px-4 font-medium text-slate-700 dark:text-slate-300">
                    {req.units || req.unitsRequired || 1} Bags
                  </td>
                  <td className="py-3 px-4">
                    <HospitalStatusBadge status={req.status} />
                  </td>
                  <td className="py-3 px-4 text-slate-400 dark:text-slate-500 text-[11px] font-medium">
                    {formatDate(req.createdDate || req.createdAt)}
                  </td>
                  <td className="py-3 px-5 text-right">
                    <button
                      onClick={() => navigate(`/hospital/requests/${req.id}`)}
                      className="p-2 text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 hover:bg-teal-50 dark:hover:bg-teal-950/50 rounded-xl transition-colors"
                      title="View Request Details"
                    >
                      <Eye className="h-3.5 w-3.5" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="flex flex-col items-center justify-center py-10 text-center gap-2.5 text-slate-400 dark:text-slate-500">
            <FileText className="h-9 w-9 text-slate-300 dark:text-slate-700 stroke-[1.5]" />
            <p className="text-xs font-semibold">No recent blood requests found.</p>
          </div>
        )}
      </div>
    </div>
  );
}
