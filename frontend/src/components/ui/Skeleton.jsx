import React from 'react';

/**
 * Basic component providing animate pulse tags.
 */
export function SkeletonPulse({ className = '' }) {
  return (
    <div className={`animate-pulse bg-slate-200 dark:bg-slate-750 rounded-xl ${className}`} />
  );
}

/**
 * Skeleton mimicking statistical cards.
 */
export function CardSkeleton() {
  return (
    <div className="bg-white dark:bg-slate-800 border border-gray-100 dark:border-slate-750 p-6 rounded-3xl flex flex-col gap-4">
      <div className="flex justify-between items-start gap-4">
        <div className="flex flex-col gap-2 w-2/3">
          <SkeletonPulse className="h-3 w-16" />
          <SkeletonPulse className="h-6 w-24" />
        </div>
        <SkeletonPulse className="h-10 w-10 rounded-2xl" />
      </div>
    </div>
  );
}

/**
 * Skeleton mimicking tables.
 */
export function TableSkeleton({ rows = 4 }) {
  return (
    <div className="bg-white dark:bg-slate-800 border border-gray-100 dark:border-slate-750 rounded-3xl p-6 flex flex-col gap-4">
      <div className="flex gap-4">
        <SkeletonPulse className="h-4 w-1/4" />
        <SkeletonPulse className="h-4 w-1/4" />
        <SkeletonPulse className="h-4 w-1/4" />
        <SkeletonPulse className="h-4 w-1/4" />
      </div>
      <hr className="border-slate-50 dark:border-slate-700" />
      {Array.from({ length: rows }).map((_, idx) => (
        <div key={idx} className="flex gap-4 items-center py-2">
          <SkeletonPulse className="h-4 w-1/4" />
          <SkeletonPulse className="h-3 w-1/6" />
          <SkeletonPulse className="h-3 w-1/4" />
          <SkeletonPulse className="h-3 w-1/5" />
        </div>
      ))}
    </div>
  );
}

/**
 * Complete dashboard skeleton with stats and lists.
 */
export function DashboardSkeleton() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <SkeletonPulse className="h-7 w-48" />
        <SkeletonPulse className="h-3.5 w-96" />
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <CardSkeleton />
        <CardSkeleton />
        <CardSkeleton />
        <CardSkeleton />
      </div>
      <TableSkeleton rows={5} />
    </div>
  );
}

/**
 * Profile form layout skeleton.
 */
export function ProfileSkeleton() {
  return (
    <div className="bg-white dark:bg-slate-800 border border-gray-100 dark:border-slate-750 p-6 rounded-3xl flex flex-col gap-6 max-w-2xl mx-auto w-full">
      <div className="flex items-center gap-4">
        <SkeletonPulse className="h-16 w-16 rounded-full" />
        <div className="flex flex-col gap-2">
          <SkeletonPulse className="h-5 w-40" />
          <SkeletonPulse className="h-3.5 w-24" />
        </div>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mt-4">
        {Array.from({ length: 6 }).map((_, idx) => (
          <div key={idx} className="flex flex-col gap-2">
            <SkeletonPulse className="h-3 w-20" />
            <SkeletonPulse className="h-10 w-full" />
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * Notifications logs skeleton.
 */
export function NotificationSkeleton() {
  return (
    <div className="flex flex-col gap-4 max-w-4xl mx-auto w-full">
      {Array.from({ length: 4 }).map((_, idx) => (
        <div key={idx} className="bg-white dark:bg-slate-800 border border-gray-100 dark:border-slate-750 p-4 rounded-3xl flex flex-col gap-3">
          <div className="flex justify-between items-center">
            <SkeletonPulse className="h-5 w-24 rounded-full" />
            <SkeletonPulse className="h-3 w-16" />
          </div>
          <SkeletonPulse className="h-4 w-1/3" />
          <SkeletonPulse className="h-3 w-2/3" />
        </div>
      ))}
    </div>
  );
}
