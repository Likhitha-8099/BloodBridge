import React from 'react';
import { Users, MapPin, CheckCircle2, ArrowRight, Droplet } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

/**
 * Nearby Donors Panel for Hospital Dashboard.
 * Redesigned with consistent hospital design system.
 */
export default function NearbyDonors({ donors = [], onViewAll }) {
  const navigate = useNavigate();

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
            <Users className="h-4 w-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-800 dark:text-white">Top Nearby Donors</h2>
            <p className="text-[11px] text-slate-400 dark:text-slate-500">Available in hospital radius</p>
          </div>
        </div>
        <button
          onClick={onViewAll || (() => navigate('/hospital/donors'))}
          className="text-[11px] font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-800 dark:hover:text-teal-200 flex items-center gap-1 hover:underline transition-all"
        >
          View All <ArrowRight className="h-3 w-3" />
        </button>
      </div>

      {/* Table / Empty State */}
      <div className="overflow-x-auto flex-1">
        {donors.length > 0 ? (
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider text-[10px] border-b border-slate-100 dark:border-slate-800">
              <tr>
                <th className="py-3 px-5">Donor Name</th>
                <th className="py-3 px-4">Blood Group</th>
                <th className="py-3 px-4">Distance</th>
                <th className="py-3 px-5 text-right">Availability</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50 dark:divide-slate-800/80 text-slate-700 dark:text-slate-300">
              {donors.slice(0, 5).map((donor) => (
                <tr key={donor.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors">
                  <td className="py-3 px-5 font-semibold text-slate-900 dark:text-white truncate max-w-[140px]">
                    {donor.name || donor.fullName || 'Registered Donor'}
                  </td>
                  <td className="py-3 px-4">
                    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-black bg-teal-50 dark:bg-teal-950/60 text-teal-700 dark:text-teal-400 border border-teal-100 dark:border-teal-900/40">
                      <Droplet className="h-3 w-3 fill-current" />
                      {formatBloodGroup(donor.bloodGroup)}
                    </span>
                  </td>
                  <td className="py-3 px-4 font-medium text-slate-600 dark:text-slate-400">
                    <span className="flex items-center gap-1">
                      <MapPin className="h-3 w-3 text-teal-500 shrink-0" />
                      {donor.distanceKm != null ? `${donor.distanceKm} km` : 'Near hospital'}
                    </span>
                  </td>
                  <td className="py-3 px-5 text-right">
                    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-black bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-900/40">
                      <CheckCircle2 className="h-3 w-3" />
                      {donor.availability || 'AVAILABLE'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="flex flex-col items-center justify-center py-10 text-center gap-2.5 text-slate-400 dark:text-slate-500">
            <Users className="h-9 w-9 text-slate-300 dark:text-slate-700 stroke-[1.5]" />
            <p className="text-xs font-semibold">No nearby registered donors found.</p>
          </div>
        )}
      </div>
    </div>
  );
}
