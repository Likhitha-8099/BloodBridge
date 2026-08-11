import React from 'react';
import { HeartHandshake, ArrowRight, CheckCircle2, Droplet } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

/**
 * Recent Completed Donations Mini-Table for Hospital Dashboard.
 * Redesigned with consistent hospital design system.
 */
export default function RecentDonations({ donations = [], onViewAll }) {
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
          <div className="p-2 bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 rounded-xl border border-emerald-100 dark:border-emerald-900/40">
            <HeartHandshake className="h-4 w-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-800 dark:text-white">Recent Completed Donations</h2>
            <p className="text-[11px] text-slate-400 dark:text-slate-500">Transfusions & completion logs</p>
          </div>
        </div>
        <button
          onClick={onViewAll || (() => navigate('/hospital/donations'))}
          className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400 hover:text-emerald-800 dark:hover:text-emerald-200 flex items-center gap-1 hover:underline transition-all"
        >
          View All <ArrowRight className="h-3 w-3" />
        </button>
      </div>

      {/* Table / Empty State */}
      <div className="overflow-x-auto flex-1">
        {donations.length > 0 ? (
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider text-[10px] border-b border-slate-100 dark:border-slate-800">
              <tr>
                <th className="py-3 px-5">Donor Name</th>
                <th className="py-3 px-4">Blood Group</th>
                <th className="py-3 px-4">Donation Date</th>
                <th className="py-3 px-5 text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50 dark:divide-slate-800/80 text-slate-700 dark:text-slate-300">
              {donations.slice(0, 5).map((item) => (
                <tr key={item.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors">
                  <td className="py-3 px-5 font-semibold text-slate-900 dark:text-white truncate max-w-[150px]">
                    {item.donorName || 'Anonymous Donor'}
                  </td>
                  <td className="py-3 px-4">
                    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-black bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-900/40">
                      <Droplet className="h-3 w-3 fill-current" />
                      {formatBloodGroup(item.bloodGroup)}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-slate-500 dark:text-slate-400 text-[11px] font-medium">
                    {formatDate(item.donationDate || item.completedAt)}
                  </td>
                  <td className="py-3 px-5 text-right">
                    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-black bg-emerald-100 dark:bg-emerald-950/80 text-emerald-800 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-900/60 uppercase">
                      <CheckCircle2 className="h-3 w-3" />
                      {item.status || 'COMPLETED'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="flex flex-col items-center justify-center py-10 text-center gap-2.5 text-slate-400 dark:text-slate-500">
            <HeartHandshake className="h-9 w-9 text-slate-300 dark:text-slate-700 stroke-[1.5]" />
            <p className="text-xs font-semibold">No recent donation logs recorded.</p>
          </div>
        )}
      </div>
    </div>
  );
}
