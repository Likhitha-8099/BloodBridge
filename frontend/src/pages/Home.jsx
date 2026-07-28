import React from 'react';
import { Navigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { Heart, ArrowRight, ShieldCheck, Users, Activity } from 'lucide-react';
import Button from '../components/ui/Button';

/**
 * Platform Landing / Home page with role-based routing checks.
 */
export default function Home() {
  const { isAuthenticated, role } = useAuthStore();

  if (isAuthenticated) {
    const dashboardPath = `/${role.toLowerCase()}/dashboard`;
    return <Navigate to={dashboardPath} replace />;
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Top Banner Navigation */}
      <nav className="bg-white border-b border-gray-100 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-2">
          <Heart className="h-6 w-6 text-primary fill-primary" />
          <span className="font-bold text-xl tracking-tight text-gray-900">
            Blood<span className="text-primary">Bridge</span>
          </span>
        </div>
        <div className="flex items-center gap-4">
          <Link to="/login" className="text-sm font-semibold text-gray-600 hover:text-gray-950 transition">
            Log In
          </Link>
          <Link to="/register">
            <Button variant="primary" className="px-5 py-2">Get Started</Button>
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="flex-1 flex flex-col justify-center items-center px-6 py-16 text-center max-w-4xl mx-auto">
        <div className="inline-flex items-center gap-2 bg-red-50 text-primary px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider mb-6 border border-red-100">
          <Heart className="h-3.5 w-3.5" /> Connecting Donors & Hospitals
        </div>
        
        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 tracking-tight leading-tight mb-6">
          Bridging the Gap in <br />
          <span className="text-primary">Life-Saving Blood Donations</span>
        </h1>
        
        <p className="text-base sm:text-lg text-slate-600 mb-8 max-w-2xl leading-relaxed">
          Blood Bridge is a secure, automated platform matching available blood donors with patients in urgent need across verified hospitals.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 mb-16 w-full sm:w-auto">
          <Link to="/register">
            <Button variant="primary" className="w-full sm:w-auto px-8 py-3.5 flex items-center gap-2">
              Register as a Donor <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
          <Link to="/login">
            <Button variant="outline" className="w-full sm:w-auto px-8 py-3.5">
              Sign In
            </Button>
          </Link>
        </div>

        {/* Feature Highlights Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 w-full text-left">
          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col gap-3">
            <div className="p-3 bg-red-50 rounded-xl text-primary w-fit">
              <ShieldCheck className="h-6 w-6" />
            </div>
            <h3 className="font-bold text-slate-800">Verified Hospitals</h3>
            <p className="text-xs text-slate-500 leading-relaxed">
              Only authorized, registered hospitals can verify requests and schedule donations.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col gap-3">
            <div className="p-3 bg-red-50 rounded-xl text-primary w-fit">
              <Activity className="h-6 w-6" />
            </div>
            <h3 className="font-bold text-slate-800">Matching Engine</h3>
            <p className="text-xs text-slate-500 leading-relaxed">
              Automated matching algorithm analyzes blood compatibility, locations, and schedules.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col gap-3">
            <div className="p-3 bg-red-50 rounded-xl text-primary w-fit">
              <Users className="h-6 w-6" />
            </div>
            <h3 className="font-bold text-slate-800">Interactive Portals</h3>
            <p className="text-xs text-slate-500 leading-relaxed">
              Tailored workflows and dashboard experiences for Donors, Patients, and Administrators.
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-100 py-6 text-center text-xs text-slate-400">
        © 2026 Blood Bridge. All rights reserved.
      </footer>
    </div>
  );
}
