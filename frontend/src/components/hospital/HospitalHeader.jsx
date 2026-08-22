import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import hospitalService from '../../services/hospitalService';
import NotificationBell from '../ui/NotificationBell';
import ThemeToggle from '../ui/ThemeToggle';
import BloodBridgeLogo from '../common/BloodBridgeLogo';
import {
  Menu,
  AlertTriangle,
  User,
  LogOut,
  Settings,
  ChevronDown,
  ShieldCheck,
  Heart,
} from 'lucide-react';

// ─── Per-route context config ─────────────────────────────────────────────────
const PAGE_CONFIG = {
  '/hospital/dashboard': {
    title: 'Dashboard',
    subtitle: 'Hospital Control Center',
    message: 'Together, we\'re saving lives ❤️',
  },
  '/hospital/requests': {
    title: 'Blood Requests',
    subtitle: 'Hospital Portal › Blood Requests',
    message: 'Every request brings hope to a patient 🩸',
  },
  '/hospital/create-request': {
    title: 'Create Request',
    subtitle: 'Hospital Portal › Create Request',
    message: 'A new request — a new chance to save a life 🏥',
  },
  '/hospital/requests/create': {
    title: 'Create Request',
    subtitle: 'Hospital Portal › Create Request',
    message: 'A new request — a new chance to save a life 🏥',
  },
  '/hospital/matches': {
    title: 'Donor Matches',
    subtitle: 'Hospital Portal › Donor Matches',
    message: 'Connecting donors with those who need them most 🩸',
  },
  '/hospital/donors': {
    title: 'Accepted Donors',
    subtitle: 'Hospital Portal › Accepted Donors',
    message: 'These donors chose to make a difference ❤️',
  },
  '/hospital/donations': {
    title: 'Donation History',
    subtitle: 'Hospital Portal › Donation History',
    message: 'Every completed donation is a life touched 🌟',
  },
  '/hospital/users': {
    title: 'Staff Users',
    subtitle: 'Hospital Portal › Staff Users',
    message: 'Your team — the bridge between donors and lives 🏥',
  },
  '/hospital/profile': {
    title: 'Hospital Profile',
    subtitle: 'Hospital Portal › Profile',
    message: 'Your institution is the heart of this mission 💙',
  },
  '/hospital/profile/edit': {
    title: 'Edit Profile',
    subtitle: 'Hospital Portal › Edit Profile',
    message: 'Keep your profile up-to-date to serve better 🏥',
  },
  '/notifications': {
    title: 'Notifications',
    subtitle: 'Hospital Portal › Notifications',
    message: 'Stay updated with every life-saving action 🔔',
  },
};

const DEFAULT_CONFIG = {
  title: 'Hospital Portal',
  subtitle: 'Hospital Portal',
  message: 'Connecting Blood. Saving Lives. ❤️',
};

// Global rotating motivational messages
const GLOBAL_MESSAGES = [
  'Connecting Blood. Saving Lives. ❤️',
  'Together, we\'re saving lives.',
  'One request. One donor. One life saved.',
  'Your hospital is making a difference.',
  'Every donor matters.',
  'Together, we bridge the gap.',
];

/**
 * HospitalHeader — ONE UNIFIED sticky top header for the HOSPITAL role.
 * Contains:
 * TOP-LEFT: BloodBridge Logo, Hospital Portal label, Page Title & Breadcrumbs
 * CENTER: Subtle healthcare contextual message
 * RIGHT: Emergency indicator, Theme toggle, Notification Bell (with unread badge),
 *        Hospital Profile info + HOSPITAL role badge, and direct Logout button.
 */
export default function HospitalHeader({ onMenuToggle }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, token, logout } = useAuthStore();
  const dropdownRef = useRef(null);

  // ── Emergency request count ─────────────────────────────────────────────────
  const [emergencyCount, setEmergencyCount] = useState(0);
  const fetchEmergencyCount = useCallback(async () => {
    if (!token) return;
    try {
      const res = await hospitalService.getEmergencyRequests(20);
      const list = Array.isArray(res) ? res : res?.data || [];
      const active = list.filter(
        (r) =>
          r.status === 'PENDING' ||
          r.status === 'OPEN' ||
          r.status === 'ACTIVE' ||
          r.status === 'MATCHED'
      );
      setEmergencyCount(active.length);
    } catch {
      setEmergencyCount(0);
    }
  }, [token]);

  useEffect(() => {
    if (!token) return;
    fetchEmergencyCount();
    const interval = setInterval(fetchEmergencyCount, 30000);
    return () => clearInterval(interval);
  }, [fetchEmergencyCount, token]);

  // ── Rotating motivational ticker ────────────────────────────────────────────
  const [tickerIdx, setTickerIdx] = useState(0);
  const [tickerVisible, setTickerVisible] = useState(true);
  useEffect(() => {
    const interval = setInterval(() => {
      setTickerVisible(false);
      setTimeout(() => {
        setTickerIdx((prev) => (prev + 1) % GLOBAL_MESSAGES.length);
        setTickerVisible(true);
      }, 400);
    }, 7000);
    return () => clearInterval(interval);
  }, []);

  // ── Profile dropdown ────────────────────────────────────────────────────────
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
    navigate('/', { replace: true });
  };

  // ── Page info ───────────────────────────────────────────────────────────────
  const path = location.pathname;
  const pageConfig =
    PAGE_CONFIG[path] ||
    (path.startsWith('/hospital/requests/')
      ? {
          title: 'Request Details',
          subtitle: 'Hospital Portal › Blood Requests',
          message: 'Managing every request with care 🏥',
        }
      : null) ||
    DEFAULT_CONFIG;

  const hospitalName =
    user?.hospitalName || user?.name || user?.fullName || 'Hospital';
  const initials = hospitalName
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'H';

  return (
    <header className="sticky top-0 z-40 bg-white/95 dark:bg-slate-950/95 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800/80 shadow-xs text-slate-900 dark:text-white transition-colors duration-200">
      <div className="flex items-center justify-between h-16 px-4 sm:px-6 gap-3">
        
        {/* ─────────────────────────────────────────────────────────────────
            LEFT SIDE: BloodBridge Logo + Hospital Portal Label + Page Title
        ───────────────────────────────────────────────────────────────── */}
        <div className="flex items-center gap-3.5 min-w-0">
          {/* Mobile hamburger button */}
          <button
            onClick={onMenuToggle}
            className="md:hidden p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-teal-500 shrink-0"
            aria-label="Toggle navigation menu"
          >
            <Menu className="h-5 w-5" />
          </button>

          {/* BloodBridge Logo Link */}
          <Link to="/hospital/dashboard" className="flex items-center gap-2 hover:opacity-90 transition-opacity shrink-0">
            <BloodBridgeLogo size="sm" />
            <span className="hidden sm:inline bg-teal-500/20 text-teal-300 text-[10px] font-extrabold px-2.5 py-0.5 rounded-full border border-teal-500/30 uppercase tracking-wide">
              Hospital Portal
            </span>
          </Link>

          {/* Vertical Divider */}
          <div className="hidden sm:block h-6 w-px bg-slate-800 shrink-0" />

          {/* Current Page Title & Subtitle */}
          <div className="flex flex-col min-w-0">
            <div className="flex items-center gap-1.5">
              <span className="text-xs sm:text-sm font-extrabold text-white truncate leading-tight">
                {pageConfig.title}
              </span>
              <span className="hidden lg:inline-flex items-center gap-1 text-[10px] text-teal-400 font-semibold">
                • {pageConfig.subtitle}
              </span>
            </div>
            <p className="hidden md:block text-[10px] text-slate-400 truncate leading-tight mt-0.5">
              {pageConfig.message}
            </p>
          </div>
        </div>

        {/* ─────────────────────────────────────────────────────────────────
            CENTER AREA: Rotating Healthcare Motivational Ticker (xl only)
        ───────────────────────────────────────────────────────────────── */}
        <div className="hidden xl:flex items-center gap-2 px-4 py-1.5 bg-slate-900/80 rounded-full border border-slate-800 shrink-0">
          <Heart className="h-3.5 w-3.5 text-teal-400 shrink-0" />
          <p
            className="text-[11px] text-slate-300 font-medium italic whitespace-nowrap"
            style={{
              opacity: tickerVisible ? 1 : 0,
              transition: 'opacity 0.4s ease-in-out',
            }}
          >
            {GLOBAL_MESSAGES[tickerIdx]}
          </p>
        </div>

        {/* ─────────────────────────────────────────────────────────────────
            RIGHT SIDE: Emergency Alert + Theme Toggle + Bell + Profile + Logout
        ───────────────────────────────────────────────────────────────── */}
        <div className="flex items-center gap-2 sm:gap-3 shrink-0">
          
          {/* Active Emergency Requests Alert Indicator (Real backend data) */}
          {emergencyCount > 0 && (
            <button
              onClick={() => navigate('/hospital/requests')}
              className="flex items-center gap-1.5 px-2.5 py-1.5 bg-red-500/20 hover:bg-red-500/30 border border-red-500/40 rounded-xl text-red-400 text-xs font-bold transition-all animate-pulse"
              title={`${emergencyCount} active emergency blood requests requiring attention`}
            >
              <AlertTriangle className="h-4 w-4 shrink-0" />
              <span className="hidden sm:inline">{emergencyCount} Active Emergency</span>
              <span className="sm:hidden">{emergencyCount}</span>
            </button>
          )}

          {/* Theme Toggle */}
          <ThemeToggle />

          {/* Notification Bell (Real live unread count) */}
          <NotificationBell />

          {/* Hospital Profile Badge & Dropdown */}
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-2.5 p-1.5 pr-3 bg-slate-900 hover:bg-slate-800/90 border border-slate-800 rounded-xl transition-all outline-none focus-visible:ring-2 focus-visible:ring-teal-500 cursor-pointer"
              aria-label={`Hospital Profile: ${hospitalName}`}
            >
              <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center font-black text-xs text-white uppercase shrink-0 shadow-sm">
                {initials}
              </div>

              <div className="hidden sm:flex flex-col text-left min-w-0">
                <span className="text-xs font-bold text-slate-200 truncate max-w-[130px] leading-tight">
                  {hospitalName}
                </span>
                <div className="flex items-center gap-1">
                  <span className="bg-teal-500/20 text-teal-300 text-[9px] font-extrabold px-1.5 py-0.2 rounded border border-teal-500/30 uppercase tracking-wide">
                    HOSPITAL
                  </span>
                </div>
              </div>

              <ChevronDown className="h-3.5 w-3.5 text-slate-400" />
            </button>

            {/* Dropdown Menu */}
            {dropdownOpen && (
              <div className="absolute right-0 mt-2 w-60 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl z-50 overflow-hidden py-1.5 animate-in fade-in slide-in-from-top-2">
                <div className="px-4 py-3 border-b border-slate-800 flex items-center gap-3">
                  <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center font-black text-sm text-white shrink-0">
                    {initials}
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs font-bold text-white truncate">{hospitalName}</p>
                    <p className="text-[10px] text-slate-400 truncate">{user?.email || 'Hospital Staff'}</p>
                    <div className="flex items-center gap-1 mt-1">
                      <ShieldCheck className="h-3 w-3 text-teal-400" />
                      <span className="text-[9px] text-teal-300 font-bold uppercase tracking-wide">
                        Verified Institution
                      </span>
                    </div>
                  </div>
                </div>

                <div className="py-1">
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      navigate('/hospital/profile');
                    }}
                    className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white flex items-center gap-2.5 transition-colors"
                  >
                    <User className="h-4 w-4 text-teal-400" />
                    Hospital Profile
                  </button>
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      navigate('/hospital/profile/edit');
                    }}
                    className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white flex items-center gap-2.5 transition-colors"
                  >
                    <Settings className="h-4 w-4 text-emerald-400" />
                    Settings & Verification
                  </button>
                </div>

                <div className="border-t border-slate-800 my-1" />

                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2.5 text-xs font-bold text-red-400 hover:bg-red-500/10 hover:text-red-300 flex items-center gap-2.5 transition-colors"
                >
                  <LogOut className="h-4 w-4" />
                  Sign Out
                </button>
              </div>
            )}
          </div>

          {/* Direct Logout Button right beside Hospital Profile */}
          <button
            onClick={handleLogout}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-slate-400 hover:text-red-400 hover:bg-red-500/10 transition-all text-xs font-bold outline-none focus-visible:ring-2 focus-visible:ring-red-500"
            title="Log Out"
            aria-label="Log Out"
          >
            <LogOut className="h-4 w-4" />
            <span className="hidden sm:inline">Logout</span>
          </button>

        </div>
      </div>

      {/* Subtle bottom border highlight */}
      <div className="h-px bg-gradient-to-r from-teal-500/10 via-teal-500/40 to-teal-500/10" />
    </header>
  );
}
