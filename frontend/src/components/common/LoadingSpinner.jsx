import React from 'react';

/**
 * Reusable full-page or inline loading spinner with backdrop blur.
 */
export default function LoadingSpinner({ fullScreen = false }) {
  const containerClasses = fullScreen
    ? "fixed inset-0 z-50 flex items-center justify-center bg-slate-900/10 backdrop-blur-sm"
    : "w-full py-12 flex items-center justify-center";

  return (
    <div className={containerClasses}>
      <div className="flex flex-col items-center gap-3 bg-white/80 p-5 rounded-2xl shadow-lg border border-white/40">
        <div className="relative w-10 h-10">
          <div className="absolute inset-0 rounded-full border-4 border-red-100"></div>
          <div className="absolute inset-0 rounded-full border-4 border-primary border-t-transparent animate-spin"></div>
        </div>
        <span className="text-xs font-semibold text-gray-500 tracking-wide">Loading details...</span>
      </div>
    </div>
  );
}
