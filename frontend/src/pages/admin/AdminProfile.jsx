import React from 'react';
import { useAuthStore } from '../../store/authStore';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import { ShieldCheck, Mail, User, Shield, Key, Activity, Calendar } from 'lucide-react';

/**
 * Dedicated Admin Profile View for BloodBridge Administrator Role.
 * Displays administrator account details, security clearance, and access permissions.
 */
export default function AdminProfile() {
  const { user } = useAuthStore();
  const adminName = user?.fullName || user?.name || user?.email || 'Administrator';
  const email = user?.email || 'admin@bloodbridge.com';
  const role = user?.role || 'ADMIN';
  const initials = adminName
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'A';

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto pb-12 font-sans">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <Shield className="h-6 w-6 text-indigo-500" />
          Administrator Account Profile
        </h1>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Review system administrator credentials, security access levels, and audit permissions.
        </p>
      </div>

      <Card className="p-6 sm:p-8 flex flex-col gap-8">
        {/* Main Entity Banner */}
        <div className="flex items-center justify-between gap-5 pb-6 border-b border-slate-100 dark:border-slate-800 flex-wrap">
          <div className="flex items-center gap-4">
            <div className="h-16 w-16 rounded-3xl bg-gradient-to-br from-indigo-500 to-purple-600 text-white flex items-center justify-center font-black text-2xl shadow-lg shrink-0">
              {initials}
            </div>

            <div className="flex flex-col gap-1">
              <div className="flex items-center gap-3 flex-wrap">
                <h2 className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white">
                  {adminName}
                </h2>
                <Badge variant="indigo" size="md">
                  Super Administrator
                </Badge>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 font-mono">
                System Administrator ID: #{user?.id || 'ADMIN-001'}
              </p>
            </div>
          </div>

          <div className="shrink-0">
            <span className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full text-xs font-bold bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
              <ShieldCheck className="h-4 w-4 text-emerald-600 shrink-0" />
              <span>Status: ACTIVE CLEARANCE</span>
            </span>
          </div>
        </div>

        {/* Section 1: Account Information */}
        <div className="flex flex-col gap-3">
          <h3 className="text-xs font-extrabold uppercase tracking-wider text-slate-400 dark:text-slate-500 flex items-center gap-1.5">
            <User className="h-3.5 w-3.5 text-indigo-500" /> Account Details
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider">Full Name / Title</span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">{adminName}</span>
            </div>

            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider flex items-center gap-1">
                <Mail className="h-3 w-3" /> Email Address
              </span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">{email}</span>
            </div>

            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider flex items-center gap-1">
                <Key className="h-3 w-3" /> Platform Role
              </span>
              <span className="text-sm font-bold text-indigo-600 dark:text-indigo-400">{role}</span>
            </div>

            <div className="flex flex-col gap-1 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider flex items-center gap-1">
                <Calendar className="h-3 w-3" /> Session Security
              </span>
              <span className="text-sm font-bold text-slate-900 dark:text-white">JWT Authenticated (256-bit)</span>
            </div>
          </div>
        </div>

        {/* Section 2: Security & Permissions */}
        <div className="p-5 rounded-2xl bg-indigo-50/50 dark:bg-indigo-950/30 border border-indigo-100 dark:border-indigo-900/40 flex flex-col gap-3">
          <div className="flex items-center gap-2">
            <Activity className="h-4 w-4 text-indigo-600 dark:text-indigo-400" />
            <span className="text-xs font-bold text-indigo-900 dark:text-indigo-200">System Permissions & Administrative Capabilities</span>
          </div>
          <ul className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-indigo-800/80 dark:text-indigo-300/80">
            <li className="flex items-center gap-2">✓ Hospital Registration Verification & Approval</li>
            <li className="flex items-center gap-2">✓ System-wide Blood Request Oversight</li>
            <li className="flex items-center gap-2">✓ User Demographics & CSV Export Access</li>
            <li className="flex items-center gap-2">✓ STOMP Realtime Gateway & WebSocket Audit</li>
          </ul>
        </div>
      </Card>
    </div>
  );
}
