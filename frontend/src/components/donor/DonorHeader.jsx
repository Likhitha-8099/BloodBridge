import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import NotificationBell from '../ui/NotificationBell';
import ThemeToggle from '../ui/ThemeToggle';
import BloodBridgeLogo from '../common/BloodBridgeLogo';
import {
  Menu,
  User,
  LogOut,
  Settings,
  ChevronDown,
  ShieldCheck,
  Heart,
  Award,
} from 'lucide-react';

// ─── Per-route context config for Donor Portal ────────────────────────────────
const PAGE_CONFIG = {
  '/donor/dashboard': {
    title: 'Donor Dashboard',
    subtitle: 'Donor Portal',
    message: 'Every donation can save up to 3 lives ❤️',
  },
  '/donor/requests': {
    title: 'Emergency Requests',
    subtitle: 'Donor Portal › Blood Requests',
    message: 'Your immediate response brings hope to patients 🚨',
  },
  '/donor/history': {
    title: 'Donation History',
    subtitle: 'Donor Portal › Donation History',
    message: 'Track your life-saving blood donation milestones 🩸',
  },
  '/donor/impact': {
    title: 'My Impact',
    subtitle: 'Donor Portal › Impact',
    message: 'See the lives you have touched & milestones achieved 🌟',
  },
  '/donor/eligibility': {
    title: 'Eligibility Status',
    subtitle: 'Donor Portal › Eligibility',
    message: 'Track your 90-day cooldown timer & medical readiness ⏳',
  },
  '/donor/ai-assistant': {
    title: 'AI Assistant',
    subtitle: 'Donor Portal › AI Assistant',
    message: 'Conversational health support for donation & compatibility 🤖',
  },
  '/donor/profile': {
    title: 'My Profile',
    subtitle: 'Donor Portal › Profile',
    message: 'Keep your health metrics and availability up-to-date 🏥',
  },
  '/donor/profile/edit': {
    title: 'Edit Profile',
    subtitle: 'Donor Portal › Edit Profile',
    message: 'Update your contact and medical parameters ✏️',
  },
  '/notifications': {
    title: 'Notifications',
    subtitle: 'Donor Portal › Notifications',
    message: 'Stay informed on emergency requests & donation updates 🔔',
  },
};

const DEFAULT_CONFIG = {
  title: 'Donor Portal',
  subtitle: 'Donor Portal',
  message: 'Connecting Blood. Saving Lives. ❤️',
};

// Global rotating motivational messages
const GLOBAL_MESSAGES = [
  'Connecting Blood. Saving Lives. ❤️',
  'Every donation can save up to 3 lives.',
  'One request. One donor. One life saved.',
  'You are a hero in someone\'s story.',
  'Every donor matters.',
  'Together, we bridge the gap.',
];

/**
 * DonorHeader — ONE UNIFIED sticky top header for the DONOR role.
 * Contains:
 * TOP-LEFT: BloodBridge Logo, Donor Portal label, Page Title & Breadcrumbs
 * CENTER: Rotating healthcare motivational ticker
 * RIGHT: Theme toggle, Notification Bell (with unread count badge),
 *        Donor Profile info + DONOR role badge, and direct Logout button.
 */
export default function DonorHeader({ onMenuToggle }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const dropdownRef = useRef(null);

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
  const pageConfig = PAGE_CONFIG[path] || DEFAULT_CONFIG;

  const donorName = user?.fullName || user?.name || user?.email || 'Donor';
  const initials = donorName
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'D';

  return (
    <header className="sticky top-0 z-40 bg-white/95 dark:bg-slate-950/95 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800/80 shadow-xs text-slate-900 dark:text-white transition-colors duration-200">
      <div className="flex items-center justify-between h-16 px-4 sm:px-6 gap-3">
        
        {/* ─────────────────────────────────────────────────────────────────
            LEFT SIDE: BloodBridge Logo + Donor Portal Label + Page Title
        ───────────────────────────────────────────────────────────────── */}
        <div className="flex items-center gap-3.5 min-w-0">
          {/* Mobile hamburger button */}
          <button
            onClick={onMenuToggle}
            className="md:hidden p-2 text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500 shrink-0"
            aria-label="Toggle navigation menu"
          >
            <Menu className="h-5 w-5" />
          </button>

          {/* BloodBridge Logo Link */}
          <Link to="/donor/dashboard" className="flex items-center gap-2 hover:opacity-90 transition-opacity shrink-0">
            <BloodBridgeLogo size="sm" />
            <span className="hidden sm:inline bg-red-50 text-red-600 border border-red-200 dark:bg-red-500/20 dark:text-red-300 text-[10px] font-extrabold px-2.5 py-0.5 rounded-full dark:border-red-500/30 uppercase tracking-wide">
              Donor Portal
            </span>
          </Link>

          {/* Vertical Divider */}
          <div className="hidden sm:block h-6 w-px bg-slate-200 dark:bg-slate-800 shrink-0" />

          {/* Current Page Title & Subtitle */}
          <div className="flex flex-col min-w-0">
            <div className="flex items-center gap-1.5">
              <span className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-white truncate leading-tight">
                {pageConfig.title}
              </span>
              <span className="hidden lg:inline-flex items-center gap-1 text-[10px] text-red-600 dark:text-red-400 font-semibold">
                • {pageConfig.subtitle}
              </span>
            </div>
            <p className="hidden md:block text-[10px] text-slate-500 dark:text-slate-400 truncate leading-tight mt-0.5">
              {pageConfig.message}
            </p>
          </div>
        </div>

        {/* ─────────────────────────────────────────────────────────────────
            CENTER AREA: Rotating Healthcare Motivational Ticker (xl only)
        ───────────────────────────────────────────────────────────────── */}
        <div className="hidden xl:flex items-center gap-2 px-4 py-1.5 bg-slate-100/80 dark:bg-slate-900/80 rounded-full border border-slate-200 dark:border-slate-800 shrink-0">
          <Heart className="h-3.5 w-3.5 text-red-500 dark:text-red-400 shrink-0" />
          <p
            className="text-[11px] text-slate-700 dark:text-slate-300 font-medium italic whitespace-nowrap"
            style={{
              opacity: tickerVisible ? 1 : 0,
              transition: 'opacity 0.4s ease-in-out',
            }}
          >
            {GLOBAL_MESSAGES[tickerIdx]}
          </p>
        </div>

        {/* ─────────────────────────────────────────────────────────────────
            RIGHT SIDE: Theme Toggle + Bell + Profile + Logout
        ───────────────────────────────────────────────────────────────── */}
        <div className="flex items-center gap-2 sm:gap-3 shrink-0">

          {/* Theme Toggle */}
          <ThemeToggle />

          {/* Notification Bell (Real live unread count) */}
          <NotificationBell />

          {/* Donor Profile Badge & Dropdown */}
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-2.5 p-1.5 pr-3 bg-slate-100 hover:bg-slate-200/80 dark:bg-slate-900 dark:hover:bg-slate-800/90 border border-slate-200 dark:border-slate-800 rounded-xl transition-all outline-none focus-visible:ring-2 focus-visible:ring-red-500 cursor-pointer"
              aria-label={`Donor Profile: ${donorName}`}
            >
              <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-red-500 to-rose-600 flex items-center justify-center font-black text-xs text-white uppercase shrink-0 shadow-xs">
                {initials}
              </div>

              <div className="hidden sm:flex flex-col text-left min-w-0">
                <span className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate max-w-[130px] leading-tight">
                  {donorName}
                </span>
                <div className="flex items-center gap-1">
                  <span className="bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-300 text-[9px] font-extrabold px-1.5 py-0.2 rounded border border-red-200 dark:border-red-500/30 uppercase tracking-wide">
                    DONOR
                  </span>
                </div>
              </div>

              <ChevronDown className="h-3.5 w-3.5 text-slate-400" />
            </button>

            {/* Dropdown Menu */}
            {dropdownOpen && (
              <div className="absolute right-0 mt-2 w-60 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl z-50 overflow-hidden py-1.5 animate-in fade-in slide-in-from-top-2">
                <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-800 flex items-center gap-3">
                  <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-red-500 to-rose-600 flex items-center justify-center font-black text-sm text-white shrink-0">
                    {initials}
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{donorName}</p>
                    <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate">{user?.email || 'Donor Account'}</p>
                    <div className="flex items-center gap-1 mt-1">
                      <ShieldCheck className="h-3 w-3 text-red-500 dark:text-red-400" />
                      <span className="text-[9px] text-red-600 dark:text-red-300 font-bold uppercase tracking-wide">
                        Verified Donor
                      </span>
                    </div>
                  </div>
                </div>

                <div className="py-1">
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      navigate('/donor/profile');
                    }}
                    className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white flex items-center gap-2.5 transition-colors"
                  >
                    <User className="h-4 w-4 text-red-500" />
                    My Health Profile
                  </button>
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      navigate('/donor/impact');
                    }}
                    className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white flex items-center gap-2.5 transition-colors"
                  >
                    <Award className="h-4 w-4 text-amber-500" />
                    My Impact Dashboard
                  </button>
                  <button
                    onClick={() => {
                      setDropdownOpen(false);
                      navigate('/donor/profile/edit');
                    }}
                    className="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white flex items-center gap-2.5 transition-colors"
                  >
                    <Settings className="h-4 w-4 text-emerald-500" />
                    Edit Profile Settings
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

          {/* Direct Logout Button right beside Donor Profile */}
          <button
            onClick={handleLogout}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-slate-600 hover:text-red-600 hover:bg-red-50 dark:text-slate-400 dark:hover:text-red-400 dark:hover:bg-red-500/10 transition-all text-xs font-bold outline-none focus-visible:ring-2 focus-visible:ring-red-500"
            title="Log Out"
            aria-label="Log Out"
          >
            <LogOut className="h-4 w-4" />
            <span className="hidden sm:inline">Logout</span>
          </button>

        </div>
      </div>

      {/* Subtle bottom border highlight */}
      <div className="h-px bg-gradient-to-r from-red-500/10 via-red-500/40 to-red-500/10" />
    </header>
  );
}
