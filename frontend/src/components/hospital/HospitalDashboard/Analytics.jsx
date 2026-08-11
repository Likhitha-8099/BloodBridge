import React from 'react';
import { BarChart2, PieChart, TrendingUp, Info } from 'lucide-react';

/**
 * Hospital Analytics Chart Panel.
 * Redesigned with consistent teal/emerald hospital design system.
 * Renders CSS-based bar/progress charts — no external charting dependencies.
 */
export default function Analytics({ analytics = {} }) {
  const monthlyRequests = analytics?.monthlyRequests || [];
  const bloodGroupDistribution = analytics?.bloodGroupDistribution || [];

  const maxMonthlyCount = Math.max(...monthlyRequests.map((m) => m.count || 0), 1);
  const totalGroupCount = bloodGroupDistribution.reduce((acc, curr) => acc + (curr.count || 0), 0) || 1;

  const bloodGroupColors = [
    { bg: 'bg-red-500', text: 'text-red-600 dark:text-red-400' },
    { bg: 'bg-teal-500', text: 'text-teal-600 dark:text-teal-400' },
    { bg: 'bg-emerald-500', text: 'text-emerald-600 dark:text-emerald-400' },
    { bg: 'bg-amber-500', text: 'text-amber-600 dark:text-amber-400' },
    { bg: 'bg-violet-500', text: 'text-violet-600 dark:text-violet-400' },
    { bg: 'bg-indigo-500', text: 'text-indigo-600 dark:text-indigo-400' },
    { bg: 'bg-rose-500', text: 'text-rose-600 dark:text-rose-400' },
    { bg: 'bg-cyan-500', text: 'text-cyan-600 dark:text-cyan-400' },
  ];

  return (
    <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/30">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 rounded-xl border border-teal-100 dark:border-teal-900/40">
            <TrendingUp className="h-4 w-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-800 dark:text-white">Transfusion Analytics</h2>
            <p className="text-[11px] text-slate-400 dark:text-slate-500">Monthly trends & demand distribution</p>
          </div>
        </div>
        <span className="text-[11px] text-teal-600 dark:text-teal-400 font-bold bg-teal-50 dark:bg-teal-950/60 px-2.5 py-1 rounded-full border border-teal-100 dark:border-teal-900/40">
          Phase 1
        </span>
      </div>

      <div className="p-5 grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Chart 1: Monthly Blood Requests Bar Chart */}
        <div className="bg-slate-50/70 dark:bg-slate-800/40 p-4 rounded-xl border border-slate-100 dark:border-slate-700 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-2">
              <BarChart2 className="h-4 w-4 text-teal-600 dark:text-teal-400" />
              Monthly Blood Requests
            </h3>
            <span className="text-[10px] text-slate-400 font-medium">Last 6 Months</span>
          </div>

          {monthlyRequests.length > 0 ? (
            <div className="flex items-end justify-between gap-2 h-32 pt-4">
              {monthlyRequests.map((item, idx) => {
                const heightPct = Math.round(((item.count || 0) / maxMonthlyCount) * 100);
                return (
                  <div key={idx} className="flex-1 flex flex-col items-center gap-1.5 group">
                    <div className="text-[10px] font-bold text-slate-600 dark:text-slate-400 group-hover:text-teal-600 dark:group-hover:text-teal-400 transition-colors">
                      {item.count}
                    </div>
                    <div className="w-full bg-slate-200 dark:bg-slate-700 rounded-t-lg flex items-end overflow-hidden" style={{ height: '80px' }}>
                      <div
                        style={{ height: `${Math.max(heightPct, 8)}%` }}
                        className="w-full bg-gradient-to-t from-teal-600 to-emerald-500 group-hover:from-teal-700 group-hover:to-emerald-600 rounded-t-lg transition-all duration-500 shadow-sm"
                      />
                    </div>
                    <span className="text-[10px] font-medium text-slate-400 dark:text-slate-500">{item.month}</span>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center border border-dashed border-slate-200 dark:border-slate-700 rounded-xl p-6 text-center text-slate-400 dark:text-slate-500 gap-2">
              <Info className="h-6 w-6 text-slate-300 dark:text-slate-600" />
              <p className="text-xs font-medium">Monthly request data currently unavailable.</p>
            </div>
          )}
        </div>

        {/* Chart 2: Blood Group Demand Distribution */}
        <div className="bg-slate-50/70 dark:bg-slate-800/40 p-4 rounded-xl border border-slate-100 dark:border-slate-700 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-2">
              <PieChart className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
              Blood Group Demand
            </h3>
            <span className="text-[10px] text-slate-400 font-medium">By Request Volume</span>
          </div>

          {bloodGroupDistribution.length > 0 ? (
            <div className="flex flex-col gap-2.5 flex-1 justify-center">
              {bloodGroupDistribution.map((item, idx) => {
                const pct = Math.round(((item.count || 0) / totalGroupCount) * 100);
                const color = bloodGroupColors[idx % bloodGroupColors.length];

                return (
                  <div key={idx} className="flex flex-col gap-1">
                    <div className="flex items-center justify-between text-xs font-medium">
                      <span className={`font-bold flex items-center gap-1.5 ${color.text}`}>
                        <span className={`h-2 w-2 rounded-full ${color.bg}`} />
                        {item.bloodGroup}
                      </span>
                      <span className="text-slate-500 dark:text-slate-400">
                        {item.count} bags{' '}
                        <span className="text-slate-400 dark:text-slate-500 font-normal">({pct}%)</span>
                      </span>
                    </div>
                    <div className="h-2 w-full bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                      <div
                        style={{ width: `${pct}%` }}
                        className={`h-full ${color.bg} rounded-full transition-all duration-500`}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center border border-dashed border-slate-200 dark:border-slate-700 rounded-xl p-6 text-center text-slate-400 dark:text-slate-500 gap-2">
              <Info className="h-6 w-6 text-slate-300 dark:text-slate-600" />
              <p className="text-xs font-medium">Blood group distribution data currently unavailable.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
