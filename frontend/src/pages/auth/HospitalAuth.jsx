import React, { useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import BloodBridgeLogo from '../../components/common/BloodBridgeLogo';
import { LogIn, UserPlus, ArrowLeft, ShieldCheck } from 'lucide-react';

/**
 * Dedicated Hospital Authentication Landing Page for Blood Bridge.
 * Displays exclusively two choices: Hospital Registration and Hospital Login.
 */
export default function HospitalAuth() {
  const { isAuthenticated, role, logout } = useAuthStore();

  // Clear previous module authentication if navigating from Donor or Admin
  useEffect(() => {
    if (isAuthenticated && role !== 'HOSPITAL') {
      logout();
    }
  }, [isAuthenticated, role, logout]);

  if (isAuthenticated && role === 'HOSPITAL') {
    return <Navigate to="/hospital/dashboard" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col items-center justify-center p-6 font-sans">
      <div className="w-full max-w-xl mb-6">
        <Link to="/" className="text-xs font-semibold text-gray-500 dark:text-slate-400 hover:text-primary flex items-center gap-1.5 w-fit transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to Home
        </Link>
      </div>

      <Card className="w-full max-w-xl p-8 sm:p-10 flex flex-col items-center text-center gap-8 shadow-2xl border-slate-100 dark:border-slate-800 bg-white dark:bg-slate-900 rounded-3xl">
        {/* Branding & Header */}
        <div className="flex flex-col items-center gap-2">
          <Link to="/" className="mb-2 hover:opacity-90 transition-opacity">
            <BloodBridgeLogo size="xl" />
          </Link>
          <div className="inline-flex items-center gap-1.5 bg-teal-50 dark:bg-teal-950/40 text-teal-700 dark:text-teal-300 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider border border-teal-100 dark:border-teal-900/30">
            <ShieldCheck className="h-3.5 w-3.5" /> Healthcare Representative Portal
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-gray-900 dark:text-white mt-1">
            Hospital Access Center
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-slate-400 max-w-md leading-relaxed">
            Authorized hospital portal for verifying blood requests, managing inventory, and coordinating real-time donor matches.
          </p>
        </div>

        {/* Dual Options Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 w-full pt-2">
          {/* Hospital Registration Option */}
          <div className="bg-slate-50 dark:bg-slate-800/50 p-6 rounded-2xl border border-slate-100 dark:border-slate-800 flex flex-col justify-between gap-4 hover:border-teal-200 dark:hover:border-teal-900 transition-all text-left">
            <div className="flex flex-col gap-2">
              <div className="h-10 w-10 rounded-xl bg-teal-100 dark:bg-teal-900/40 text-teal-700 dark:text-teal-300 flex items-center justify-center">
                <UserPlus className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-base text-gray-900 dark:text-white">Hospital Registration</h3>
              <p className="text-xs text-gray-500 dark:text-slate-400 leading-relaxed">
                Register a new hospital entity for verification and administrative approval.
              </p>
            </div>
            <Link to="/register/hospital">
              <Button variant="primary" className="w-full bg-teal-600 hover:bg-teal-700 text-white font-bold py-2.5 text-xs">
                Register Hospital
              </Button>
            </Link>
          </div>

          {/* Hospital Login Option */}
          <div className="bg-slate-50 dark:bg-slate-800/50 p-6 rounded-2xl border border-slate-100 dark:border-slate-800 flex flex-col justify-between gap-4 hover:border-teal-200 dark:hover:border-teal-900 transition-all text-left">
            <div className="flex flex-col gap-2">
              <div className="h-10 w-10 rounded-xl bg-teal-100 dark:bg-teal-900/40 text-teal-700 dark:text-teal-300 flex items-center justify-center">
                <LogIn className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-base text-gray-900 dark:text-white">Hospital Login</h3>
              <p className="text-xs text-gray-500 dark:text-slate-400 leading-relaxed">
                Sign in with registered hospital representative credentials.
              </p>
            </div>
            <Link to="/login/hospital">
              <Button variant="outline" className="w-full border-teal-200 text-teal-700 dark:text-teal-300 hover:bg-teal-50 dark:hover:bg-teal-950/40 font-bold py-2.5 text-xs">
                Hospital Sign In
              </Button>
            </Link>
          </div>
        </div>

        <p className="text-[11px] text-gray-400 dark:text-slate-500">
          Need assistance? Contact <span className="font-semibold text-teal-600">support@bloodbridge.com</span>
        </p>
      </Card>
    </div>
  );
}
