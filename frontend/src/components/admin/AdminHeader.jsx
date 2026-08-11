import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import NotificationBell from '../ui/NotificationBell';
import ThemeToggle from '../ui/ThemeToggle';
import BloodBridgeLogo from '../common/BloodBridgeLogo';
import {
  Menu,
  LogOut,
  ChevronDown,
  ShieldCheck,
  Shield,
  Activity,
  Award,
} from 'lucide-react';

const PAGE_CONFIG = {
  '/admin/dashboard': {
    title: 'Admin Operations Dashboard',
    subtitle: 'System Control Center',
    message: 'Platform Operations & Infrastructure Audit 🛡️',
  },
  '/admin/hospitals': {
    title: 'Hospitals & Approvals',
    subtitle: 'Admin Portal › Verification',
    message: 'Review and verify healthcare institution credentials 🏥',
  },
  '/admin/users': {
    title: 'User Demographics',
    subtitle: 'Admin Portal › Users',
    message: 'Registered donors, patients, and staff analytics 📊',
  },
  '/admin/requests': {
    title: 'Request Analytics',
    subtitle: 'Admin Portal › Requests',
    message: 'System-wide emergency & standard request metrics 🩸',
  },
  '/admin/donations': {
    title: 'Donation Tracking',
    subtitle: 'Admin Portal › Donations',
    message: 'Completed transfusions & certificate audit logs 📜',
  },
  '/admin/matching': {
    title: 'Smart Matching Engine',
    subtitle: 'Admin Portal › Matching',
    message: '10-Stage matching engine diagnostics & rules ⚡',
  },
  '/notifications': {
    title: 'Notifications',
    subtitle: 'Admin Portal › Notifications',
    message: 'System alerts, audit logs & system health updates 🔔',
  },
  '/admin/notifications': {
    title: 'Notification Analytics',
    subtitle: 'Admin Portal › Notification Analytics',
    message: 'STOMP & FCM push delivery success statistics 📈',
  },
};

const DEFAULT_CONFIG = {
  title: 'Admin Operations Portal',
  subtitle: 'Admin Portal',
  message: 'Securing BloodBridge Platform Operations 🛡️',
};

export default function AdminHeader({ onMenuToggle }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const dropdownRef = useRef(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const path = location.pathname;
  const pageConfig = PAGE_CONFIG[path] || DEFAULT_CONFIG;

  const adminName = user?.fullName || user?.name || user?.email || 'Administrator';
  const initials = adminName
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'A';

  return (
    <header className="sticky top-0 z-40 bg-white/95 dark:bg-slate-950/95 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800/80 shadow-xs text-slate-900 dark:text-white transition-colors duration-200">
      <div className="flex items-center justify-between h-16 px-4 sm:px-6 gap-3">
        
        {/* LEFT SIDE: Logo + Title */}
        <div className="flex items-center gap-3.5 min-w-0">
          <button
            onClick={onMenuToggle}
            className="md:hidden p-2 text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 shrink-0"
            aria-label="Toggle navigation menu"
          >
            <Menu className="h-5 w-5" />
          </button>

          <Link to="/admin/dashboard" className="flex items-center gap-2 hover:opacity-90 transition-opacity shrink-0">
            <BloodBridgeLogo size="sm" />
            <span className="hidden sm:inline bg-indigo-50 text-indigo-700 border border-indigo-200 dark:bg-indigo-500/20 dark:text-indigo-300 text-[10px] font-extrabold px-2.5 py-0.5 rounded-full dark:border-indigo-500/30 uppercase tracking-wide">
              Admin Portal
            </span>
          </Link>

          <div className="hidden sm:block h-6 w-px bg-slate-200 dark:bg-slate-800 shrink-0" />

          <div className="flex flex-col min-w-0">
            <div className="flex items-center gap-1.5">
              <span className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-white truncate leading-tight">
                {pageConfig.title}
              </span>
              <span className="hidden lg:inline-flex items-center gap-1 text-[10px] text-indigo-600 dark:text-indigo-400 font-semibold">
                • {pageConfig.subtitle}
              </span>
            </div>
            <p className="hidden md:block text-[10px] text-slate-500 dark:text-slate-400 truncate leading-tight mt-0.5">
              {pageConfig.message}
            </p>
          </div>
        </div>

        {/* RIGHT SIDE: System Health + Theme Toggle + Bell + Admin Profile + Logout */}
        <div className="flex items-center gap-2 sm:gap-3 shrink-0">
          <div className="hidden xl:flex items-center gap-1.5 px-3 py-1 bg-emerald-50 text-emerald-700 border border-emerald-200 dark:bg-emerald-950/50 dark:text-emerald-300 dark:border-emerald-800 rounded-full text-[11px] font-bold">
            <ShieldCheck className="h-3.5 w-3.5 text-emerald-600 dark:text-emerald-400" />
            <span>System Active</span>
          </div>

          <ThemeToggle />
          <NotificationBell />

          {/* Admin Profile Dropdown */}
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-2.5 p-1.5 pr-3 bg-slate-100 hover:bg-slate-200/80 dark:bg-slate-900 dark:hover:bg-slate-800/90 border border-slate-200 dark:border-slate-800 rounded-xl transition-all outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 cursor-pointer"
              aria-label={`Admin Profile: ${adminName}`}
            >
              <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center font-black text-xs text-white uppercase shrink-0 shadow-xs">
                {initials}
              </div>

              <div className="hidden sm:flex flex-col text-left min-w-0">
                <span className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate max-w-[130px] leading-tight">
                  {adminName}
                </span>
                <div className="flex items-center gap-1">
                  <span className="bg-indigo-100 text-indigo-700 dark:bg-indigo-500/20 dark:text-indigo-300 text-[9px] font-extrabold px-1.5 py-0.2 rounded border border-indigo-200 dark:border-indigo-500/30 uppercase tracking-wide">
                    ADMIN
                  </span>
                </div>
              </div>

              <ChevronDown className="h-3.5 w-3.5 text-slate-400" />
            </button>

            {dropdownOpen && (
              <div className="absolute right-0 mt-2 w-60 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl z-50 overflow-hidden py-1.5 animate-in fade-in slide-in-from-top-2">
                <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-800 flex items-center gap-3">
                  <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center font-black text-sm text-white shrink-0">
                    {initials}
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{adminName}</p>
                    <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate">{user?.email || 'System Admin'}</p>
                    <div className="flex items-center gap-1 mt-1">
                      <Shield className="h-3 w-3 text-indigo-500 dark:text-indigo-400" />
                      <span className="text-[9px] text-indigo-600 dark:text-indigo-300 font-bold uppercase tracking-wide">
                        Super Administrator
                      </span>
                    </div>
                  </div>
                </div>

                <div className="py-1">
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      navigate('/admin/dashboard');
                    }}
                    className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white flex items-center gap-2.5 transition-colors"
                  >
                    <Activity className="h-4 w-4 text-indigo-500" />
                    Admin Operations
                  </button>
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      navigate('/admin/users');
                    }}
                    className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white flex items-center gap-2.5 transition-colors"
                  >
                    <Award className="h-4 w-4 text-purple-500" />
                    User Management
                  </button>
                </div>

                <div className="border-t border-slate-100 dark:border-slate-800 my-1" />

                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2.5 text-xs font-bold text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-500/10 dark:hover:text-red-300 flex items-center gap-2.5 transition-colors"
                >
                  <LogOut className="h-4 w-4" />
                  Sign Out
                </button>
              </div>
            )}
          </div>

          <button
            onClick={handleLogout}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-slate-600 hover:text-indigo-600 hover:bg-indigo-50 dark:text-slate-400 dark:hover:text-indigo-400 dark:hover:bg-indigo-500/10 transition-all text-xs font-bold outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
            title="Log Out"
            aria-label="Log Out"
          >
            <LogOut className="h-4 w-4" />
            <span className="hidden sm:inline">Logout</span>
          </button>
        </div>
      </div>

      <div className="h-px bg-gradient-to-r from-indigo-500/10 via-indigo-500/40 to-indigo-500/10" />
    </header>
  );
}
