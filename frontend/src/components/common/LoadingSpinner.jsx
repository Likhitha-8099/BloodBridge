import BloodBridgeLogo from './BloodBridgeLogo';

/**
 * Reusable full-page or inline loading spinner with backdrop blur and brand logo.
 */
export default function LoadingSpinner({ fullScreen = false }) {
  const containerClasses = fullScreen
    ? "fixed inset-0 z-50 flex items-center justify-center bg-slate-900/10 dark:bg-slate-950/40 backdrop-blur-sm"
    : "w-full py-12 flex items-center justify-center";

  return (
    <div className={containerClasses}>
      <div className="flex flex-col items-center gap-3 bg-white/90 dark:bg-slate-900/90 p-6 rounded-3xl shadow-2xl border border-slate-100 dark:border-slate-800">
        {fullScreen && <BloodBridgeLogo size="lg" className="mb-1" />}
        <div className="relative w-10 h-10">
          <div className="absolute inset-0 rounded-full border-4 border-red-100 dark:border-red-950"></div>
          <div className="absolute inset-0 rounded-full border-4 border-red-600 dark:border-red-500 border-t-transparent animate-spin"></div>
        </div>
        <span className="text-xs font-bold text-slate-500 dark:text-slate-400 tracking-wide">Loading...</span>
      </div>
    </div>
  );
}
