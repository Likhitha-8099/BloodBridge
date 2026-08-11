import React from 'react';

/**
 * Hospital Dashboard Loading Skeleton.
 * Redesigned with hospital teal/emerald design system.
 */
export default function LoadingSkeleton() {
  return (
    <div className="flex flex-col gap-6 animate-pulse pb-12">
      {/* Top Header Skeleton */}
      <div className="flex items-center justify-between bg-gradient-to-r from-teal-600 to-emerald-600 p-6 rounded-2xl shadow-md">
        <div className="flex items-center gap-4">
          <div className="h-12 w-12 bg-white/20 rounded-full" />
          <div className="flex flex-col gap-2">
            <div className="h-6 w-52 bg-white/30 rounded-lg" />
            <div className="h-4 w-36 bg-white/20 rounded-md" />
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div className="h-9 w-24 bg-white/20 rounded-xl" />
          <div className="h-9 w-24 bg-white/20 rounded-xl" />
        </div>
      </div>

      {/* Stats Cards Skeleton */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-7 gap-3">
        {[...Array(7)].map((_, i) => (
          <div key={i} className="bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm flex flex-col justify-between h-28 overflow-hidden relative">
            <div className="absolute top-0 left-0 right-0 h-1 bg-slate-200 dark:bg-slate-700 rounded-t-2xl" />
            <div className="flex items-start justify-between pt-1">
              <div className="h-9 w-9 bg-slate-200 dark:bg-slate-700 rounded-xl" />
              <div className="h-7 w-10 bg-slate-200 dark:bg-slate-700 rounded-md" />
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="h-3 w-20 bg-slate-200 dark:bg-slate-700 rounded-md" />
              <div className="h-2.5 w-14 bg-slate-100 dark:bg-slate-800 rounded-md" />
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions Skeleton */}
      <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm p-5">
        <div className="h-4 w-32 bg-slate-200 dark:bg-slate-700 rounded mb-4" />
        <div className="grid grid-cols-5 gap-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-slate-200 dark:bg-slate-700 rounded-xl" />
          ))}
        </div>
      </div>

      {/* Main Grid Skeleton */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2/3 column */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          {/* Emergency Request Panel */}
          <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm h-48 p-5 flex flex-col gap-3">
            <div className="h-4 w-44 bg-slate-700 rounded" />
            <div className="flex-1 bg-slate-800 rounded-xl" />
          </div>
          {/* Recent Requests Table */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm h-56 p-5 flex flex-col gap-3">
            <div className="h-4 w-40 bg-slate-200 dark:bg-slate-700 rounded" />
            <div className="flex-1 bg-slate-50 dark:bg-slate-800 rounded-xl" />
          </div>
          {/* Recent Donations */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm h-52 p-5 flex flex-col gap-3">
            <div className="h-4 w-44 bg-slate-200 dark:bg-slate-700 rounded" />
            <div className="flex-1 bg-slate-50 dark:bg-slate-800 rounded-xl" />
          </div>
        </div>

        {/* Right 1/3 column */}
        <div className="flex flex-col gap-6">
          {/* Analytics Panel */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm h-72 p-5 flex flex-col gap-3">
            <div className="h-4 w-36 bg-slate-200 dark:bg-slate-700 rounded" />
            <div className="flex-1 bg-slate-50 dark:bg-slate-800 rounded-xl" />
          </div>
          {/* Nearby Donors */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm h-56 p-5 flex flex-col gap-3">
            <div className="h-4 w-32 bg-slate-200 dark:bg-slate-700 rounded" />
            <div className="flex-1 bg-slate-50 dark:bg-slate-800 rounded-xl" />
          </div>
        </div>
      </div>
    </div>
  );
}
