import React, { useState } from 'react';
import Navbar from '../components/layout/Navbar';
import HospitalHeader from '../components/hospital/HospitalHeader';
import DonorHeader from '../components/donor/DonorHeader';
import AdminHeader from '../components/admin/AdminHeader';
import Sidebar from '../components/layout/Sidebar';
import { Outlet, NavLink, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useUnreadNotifications } from '../hooks/useNotifications';
import BloodBridgeLogo from '../components/common/BloodBridgeLogo';
import ToastContainer from '../components/ui/Toast';
import AiAssistant from '../components/ai/AiAssistant';
import { AnimatePresence, motion } from 'framer-motion';
import { 
  X, 
  Heart, 
  LayoutDashboard, 
  User, 
  ClipboardList, 
  FileText, 
  Bell, 
  Users, 
  Shield, 
  Award,
  TrendingUp,
  Activity,
  PlusCircle,
  LogOut,
  Stethoscope,
  ShieldCheck,
  Droplets,
  Clock,
  Bot,
} from 'lucide-react';

// Admin nav items
const ADMIN_NAV = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/admin/hospitals', label: 'Hospitals & Approvals', icon: Award },
  { to: '/admin/users', label: 'User Demographics', icon: Users },
  { to: '/admin/requests', label: 'Request Analytics', icon: FileText },
  { to: '/admin/donations', label: 'Donation Tracking', icon: ClipboardList },
  { to: '/admin/matching', label: 'Smart Matching', icon: Shield },
  { to: '/notifications', label: 'Notifications', icon: Bell, hasBadge: true },
  { to: '/admin/notifications', label: 'Notification Analytics', icon: Activity },
  { to: '/admin/profile', label: 'Admin Profile', icon: User },
];

// Donor nav items
const DONOR_NAV = [
  { to: '/donor/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/donor/requests', label: 'Emergency Requests', icon: FileText },
  { to: '/donor/eligibility', label: 'Eligibility', icon: Clock },
  { to: '/donor/history', label: 'Donation History', icon: ClipboardList },
  { to: '/donor/impact', label: 'Impact', icon: TrendingUp },
  { to: '/donor/profile', label: 'Profile', icon: User },
  { to: '/notifications', label: 'Notifications', icon: Bell, hasBadge: true },
  { to: '/donor/ai-assistant', label: 'AI Assistant', icon: Bot },
];

// Hospital nav items (matches HospitalSidebar exactly)
const HOSPITAL_NAV = [
  { to: '/hospital/dashboard', label: 'Hospital Dashboard', icon: LayoutDashboard },
  { to: '/hospital/requests', label: 'Blood Requests', icon: FileText },
  { to: '/hospital/create-request', label: 'Create Request', icon: PlusCircle },
  { to: '/hospital/matches', label: 'Donor Matches', icon: Activity },
  { to: '/hospital/donors', label: 'Accepted Donors', icon: Heart },
  { to: '/hospital/donations', label: 'Donation History', icon: ClipboardList },
  { to: '/hospital/users', label: 'Staff Users', icon: Users },
  { to: '/notifications', label: 'Notifications', icon: Bell, hasBadge: true },
  { to: '/hospital/profile', label: 'Hospital Profile', icon: User },
  { to: '/hospital/ai-assistant', label: 'AI Assistant', icon: Bot },
];

/**
 * Main application layout wrapping navigation components, transition animations, and dark mode.
 * Hospital, Donor, and Admin roles receive dedicated headers and themed mobile drawers.
 */
export default function MainLayout() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const { role, user, logout } = useAuthStore();
  const isHospital = role === 'HOSPITAL';
  const isDonor = role === 'DONOR';
  const isAdmin = role === 'ADMIN';

  // Notification count — used in hospital mobile drawer
  const { data: unreadList } = useUnreadNotifications();
  const notificationsArray = Array.isArray(unreadList) ? unreadList : (unreadList?.data || []);
  const unreadCount = notificationsArray.length;

  // Hospital name + initials for mobile drawer
  const hospitalName = user?.hospitalName || user?.name || user?.fullName || 'Hospital';
  const hospitalInitials = hospitalName
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'H';

  const handleHospitalLogout = () => {
    logout();
    // We can't use useNavigate here (not inside JSX), so we use window location
    window.location.href = '/login';
  };

  const getLinks = () => {
    switch (role) {
      case 'DONOR':
        return [
          { to: '/donor/dashboard', label: 'Donor Dashboard', icon: LayoutDashboard },
          { to: '/donor/impact', label: 'My Impact', icon: TrendingUp },
          { to: '/donor/profile', label: 'My Profile', icon: User },
          { to: '/donor/requests', label: 'Blood Requests', icon: FileText },
          { to: '/donor/history', label: 'Donation History', icon: ClipboardList },
          { to: '/notifications', label: 'Notifications', icon: Bell },
        ];
      case 'PATIENT':
        return [
          { to: '/patient/dashboard', label: 'Patient Dashboard', icon: LayoutDashboard },
          { to: '/patient/profile', label: 'My Profile', icon: User },
          { to: '/patient/create-request', label: 'Create Request', icon: ClipboardList },
          { to: '/patient/requests', label: 'My Requests', icon: FileText },
          { to: '/notifications', label: 'Notifications', icon: Bell },
        ];
      case 'HOSPITAL':
        return HOSPITAL_NAV;
      case 'ADMIN':
        return [
          { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
          { to: '/admin/users', label: 'Users', icon: Users },
          { to: '/admin/requests', label: 'Requests', icon: FileText },
          { to: '/admin/donations', label: 'Donations', icon: ClipboardList },
          { to: '/admin/matching', label: 'Matching', icon: Shield },
          { to: '/admin/hospitals', label: 'Hospitals', icon: Award },
          { to: '/notifications', label: 'Notifications', icon: Bell },
        ];
      default:
        return [];
    }
  };

  const links = getLinks();

  return (
    <div className="min-h-screen bg-background dark:bg-slate-950 text-slate-900 dark:text-slate-100 flex flex-col transition-colors duration-200">
      {isHospital ? (
        <HospitalHeader onMenuToggle={() => setIsMobileMenuOpen(!isMobileMenuOpen)} />
      ) : isDonor ? (
        <DonorHeader onMenuToggle={() => setIsMobileMenuOpen(!isMobileMenuOpen)} />
      ) : isAdmin ? (
        <AdminHeader onMenuToggle={() => setIsMobileMenuOpen(!isMobileMenuOpen)} />
      ) : (
        <Navbar onMenuToggle={() => setIsMobileMenuOpen(!isMobileMenuOpen)} />
      )}
      <div className="flex flex-1 relative">
        <Sidebar />
        
        {/* ─────────────────────────────────────────────
            MOBILE NAVIGATION DRAWER
        ───────────────────────────────────────────── */}
        <AnimatePresence>
          {isMobileMenuOpen && (
            <>
              {/* Backdrop */}
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: isHospital || isDonor || isAdmin ? 0.7 : 0.4 }}
                exit={{ opacity: 0 }}
                onClick={() => setIsMobileMenuOpen(false)}
                className="fixed inset-0 bg-black z-40 md:hidden"
              />
              
              {/* ── ADMIN Mobile Drawer (dark-indigo themed) ── */}
              {isAdmin ? (
                <motion.div
                  initial={{ x: '-100%' }}
                  animate={{ x: 0 }}
                  exit={{ x: '-100%' }}
                  transition={{ type: 'spring', damping: 28, stiffness: 220 }}
                  className="fixed top-0 bottom-0 left-0 w-72 bg-slate-950 border-r border-slate-800/60 z-50 flex flex-col md:hidden shadow-2xl"
                >
                  {/* Drawer Header */}
                  <div className="flex items-center justify-between px-5 py-4 border-b border-slate-800/60 shrink-0">
                    <div className="flex items-center gap-2.5">
                      <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-indigo-600 to-purple-700 flex items-center justify-center shadow-lg">
                        <Shield className="h-5 w-5 text-white" />
                      </div>
                      <div>
                        <p className="text-sm font-black text-white leading-tight truncate max-w-[160px]">{user?.fullName || 'Admin Operations'}</p>
                        <p className="text-[10px] text-indigo-400 font-semibold">Super Administrator</p>
                      </div>
                    </div>
                    <button
                      onClick={() => setIsMobileMenuOpen(false)}
                      className="p-2 text-slate-500 hover:text-white hover:bg-slate-800 rounded-xl transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
                      aria-label="Close menu"
                    >
                      <X className="h-5 w-5" />
                    </button>
                  </div>

                  {/* Section label */}
                  <div className="px-5 pt-4 pb-1.5">
                    <p className="text-[10px] font-bold text-slate-600 uppercase tracking-widest">Admin Menu</p>
                  </div>

                  {/* Admin Nav Links */}
                  <nav className="flex-1 overflow-y-auto px-3 pb-4 flex flex-col gap-0.5" aria-label="Admin mobile navigation">
                    {ADMIN_NAV.map((link) => {
                      const IconComponent = link.icon;
                      return (
                        <NavLink
                          key={link.to}
                          to={link.to}
                          onClick={() => setIsMobileMenuOpen(false)}
                          className={({ isActive }) =>
                            [
                              'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all duration-150 outline-none border-l-2',
                              'focus-visible:ring-2 focus-visible:ring-indigo-500',
                              isActive
                                ? 'bg-indigo-500/10 text-indigo-300 border-indigo-500 pl-2.5 font-bold'
                                : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/70 border-transparent pl-2.5',
                            ].join(' ')
                          }
                        >
                          {({ isActive }) => (
                            <>
                              <IconComponent
                                className={`h-4 w-4 shrink-0 ${isActive ? 'text-indigo-400' : 'text-slate-500'}`}
                              />
                              <span className="flex-1">{link.label}</span>
                              {link.hasBadge && unreadCount > 0 && (
                                <span className="bg-indigo-500 text-white text-[10px] font-black px-1.5 py-0.5 rounded-full animate-pulse">
                                  {unreadCount > 9 ? '9+' : unreadCount}
                                </span>
                              )}
                            </>
                          )}
                        </NavLink>
                      );
                    })}
                  </nav>

                  {/* Admin Profile Footer */}
                  <div className="shrink-0 border-t border-slate-800/60 py-3 px-3 flex flex-col gap-1">
                    <button
                      onClick={() => { setIsMobileMenuOpen(false); window.location.href = '/admin/dashboard'; }}
                      className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-slate-800/70 transition-all w-full text-left"
                    >
                      <div className="h-8 w-8 rounded-xl bg-gradient-to-br from-indigo-600 to-purple-700 flex items-center justify-center font-black text-xs text-white shrink-0">
                        {user?.fullName?.charAt(0) || 'A'}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-bold text-slate-200 truncate">{user?.fullName || 'Administrator'}</p>
                        <p className="text-[10px] text-slate-500 truncate">{user?.email || 'Platform Admin'}</p>
                      </div>
                      <ShieldCheck className="h-3.5 w-3.5 text-indigo-400 shrink-0" />
                    </button>
                    <button
                      onClick={handleHospitalLogout}
                      className="flex items-center gap-3 px-3 py-2 rounded-xl text-slate-500 hover:text-red-400 hover:bg-red-500/10 transition-all w-full text-sm font-semibold"
                      aria-label="Sign out"
                    >
                      <LogOut className="h-4 w-4 shrink-0" />
                      <span>Sign Out</span>
                    </button>
                  </div>
                </motion.div>
              ) : isDonor ? (
                <motion.div
                  initial={{ x: '-100%' }}
                  animate={{ x: 0 }}
                  exit={{ x: '-100%' }}
                  transition={{ type: 'spring', damping: 28, stiffness: 220 }}
                  className="fixed top-0 bottom-0 left-0 w-72 bg-white dark:bg-slate-950 border-r border-slate-200 dark:border-slate-800/60 z-50 flex flex-col md:hidden shadow-2xl"
                >
                  {/* Drawer Header */}
                  <div className="flex items-center justify-between px-5 py-4 border-b border-slate-200 dark:border-slate-800/60 shrink-0">
                    <div className="flex items-center gap-2.5">
                      <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-red-500 to-rose-600 flex items-center justify-center shadow-lg">
                        <Droplets className="h-5 w-5 text-white" />
                      </div>
                      <div>
                        <p className="text-sm font-black text-slate-900 dark:text-white leading-tight truncate max-w-[160px]">{user?.fullName || 'Donor Portal'}</p>
                        <p className="text-[10px] text-red-600 dark:text-red-400 font-semibold">Verified Donor</p>
                      </div>
                    </div>
                    <button
                      onClick={() => setIsMobileMenuOpen(false)}
                      className="p-2 text-slate-500 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500"
                      aria-label="Close menu"
                    >
                      <X className="h-5 w-5" />
                    </button>
                  </div>

                  {/* Section label */}
                  <div className="px-5 pt-4 pb-1.5">
                    <p className="text-[10px] font-bold text-slate-400 dark:text-slate-600 uppercase tracking-widest">Donor Menu</p>
                  </div>

                  {/* Donor Nav Links */}
                  <nav className="flex-1 overflow-y-auto px-3 pb-4 flex flex-col gap-0.5" aria-label="Donor mobile navigation">
                    {DONOR_NAV.map((link) => {
                      const IconComponent = link.icon;
                      return (
                        <NavLink
                          key={link.to}
                          to={link.to}
                          onClick={() => setIsMobileMenuOpen(false)}
                          className={({ isActive }) =>
                            [
                              'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all duration-150 outline-none border-l-2',
                              'focus-visible:ring-2 focus-visible:ring-red-500',
                              isActive
                                ? 'bg-red-50 text-red-600 border-red-600 dark:bg-red-500/10 dark:text-red-300 dark:border-red-500 pl-2.5 font-bold'
                                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-400 dark:hover:text-slate-100 dark:hover:bg-slate-800/70 border-transparent pl-2.5',
                            ].join(' ')
                          }
                        >
                          {({ isActive }) => (
                            <>
                              <IconComponent
                                className={`h-4 w-4 shrink-0 ${isActive ? 'text-red-600 dark:text-red-400' : 'text-slate-400 dark:text-slate-500'}`}
                              />
                              <span className="flex-1">{link.label}</span>
                              {link.hasBadge && unreadCount > 0 && (
                                <span className="bg-red-600 text-white text-[10px] font-black px-1.5 py-0.5 rounded-full animate-pulse">
                                  {unreadCount > 9 ? '9+' : unreadCount}
                                </span>
                              )}
                            </>
                          )}
                        </NavLink>
                      );
                    })}
                  </nav>

                  {/* Donor Profile Footer */}
                  <div className="shrink-0 border-t border-slate-200 dark:border-slate-800/60 py-3 px-3 flex flex-col gap-1">
                    <button
                      onClick={() => { setIsMobileMenuOpen(false); window.location.href = '/donor/profile'; }}
                      className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800/70 transition-all w-full text-left"
                    >
                      <div className="h-8 w-8 rounded-xl bg-gradient-to-br from-red-500 to-rose-600 flex items-center justify-center font-black text-xs text-white shrink-0">
                        {user?.fullName?.charAt(0) || 'D'}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate">{user?.fullName || 'Donor'}</p>
                        <p className="text-[10px] text-slate-500 truncate">{user?.email || 'Donor Account'}</p>
                      </div>
                      <ShieldCheck className="h-3.5 w-3.5 text-red-500 dark:text-red-400 shrink-0" />
                    </button>
                    <button
                      onClick={handleHospitalLogout}
                      className="flex items-center gap-3 px-3 py-2 rounded-xl text-slate-600 hover:text-red-600 hover:bg-red-50 dark:text-slate-500 dark:hover:text-red-400 dark:hover:bg-red-500/10 transition-all w-full text-sm font-semibold"
                      aria-label="Sign out"
                    >
                      <LogOut className="h-4 w-4 shrink-0" />
                      <span>Sign Out</span>
                    </button>
                  </div>
                </motion.div>
              ) : isHospital ? (
                /* ── HOSPITAL Mobile Drawer (dark-teal themed) ── */
                <motion.div
                  initial={{ x: '-100%' }}
                  animate={{ x: 0 }}
                  exit={{ x: '-100%' }}
                  transition={{ type: 'spring', damping: 28, stiffness: 220 }}
                  className="fixed top-0 bottom-0 left-0 w-72 bg-slate-950 border-r border-slate-800/60 z-50 flex flex-col md:hidden shadow-2xl"
                >
                  {/* Drawer Header */}
                  <div className="flex items-center justify-between px-5 py-4 border-b border-slate-800/60 shrink-0">
                    <div className="flex items-center gap-2.5">
                      <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-teal-600 to-emerald-700 flex items-center justify-center shadow-lg">
                        <Stethoscope className="h-5 w-5 text-white" />
                      </div>
                      <div>
                        <p className="text-sm font-black text-white leading-tight truncate max-w-[160px]">{hospitalName}</p>
                        <p className="text-[10px] text-teal-400 font-semibold">Hospital Portal</p>
                      </div>
                    </div>
                    <button
                      onClick={() => setIsMobileMenuOpen(false)}
                      className="p-2 text-slate-500 hover:text-white hover:bg-slate-800 rounded-xl transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-teal-500"
                      aria-label="Close menu"
                    >
                      <X className="h-5 w-5" />
                    </button>
                  </div>

                  {/* Section label */}
                  <div className="px-5 pt-4 pb-1.5">
                    <p className="text-[10px] font-bold text-slate-600 uppercase tracking-widest">Navigation</p>
                  </div>

                  {/* Hospital Nav Links */}
                  <nav className="flex-1 overflow-y-auto px-3 pb-4 flex flex-col gap-0.5" aria-label="Hospital mobile navigation">
                    {HOSPITAL_NAV.map((link) => {
                      const IconComponent = link.icon;
                      return (
                        <NavLink
                          key={link.to}
                          to={link.to}
                          onClick={() => setIsMobileMenuOpen(false)}
                          className={({ isActive }) =>
                            [
                              'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all duration-150 outline-none border-l-2',
                              'focus-visible:ring-2 focus-visible:ring-teal-500',
                              isActive
                                ? 'bg-teal-500/10 text-teal-300 border-teal-500 pl-2.5'
                                : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/70 border-transparent pl-2.5',
                            ].join(' ')
                          }
                        >
                          {({ isActive }) => (
                            <>
                              <IconComponent
                                className={`h-4 w-4 shrink-0 ${isActive ? 'text-teal-400' : 'text-slate-500'}`}
                              />
                              <span className="flex-1">{link.label}</span>
                              {link.hasBadge && unreadCount > 0 && (
                                <span className="bg-teal-500 text-white text-[10px] font-black px-1.5 py-0.5 rounded-full animate-pulse">
                                  {unreadCount > 9 ? '9+' : unreadCount}
                                </span>
                              )}
                            </>
                          )}
                        </NavLink>
                      );
                    })}
                  </nav>

                  {/* Hospital Profile Footer */}
                  <div className="shrink-0 border-t border-slate-800/60 py-3 px-3 flex flex-col gap-1">
                    <button
                      onClick={() => { setIsMobileMenuOpen(false); window.location.href = '/hospital/profile'; }}
                      className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-slate-800/70 transition-all w-full text-left"
                    >
                      <div className="h-8 w-8 rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center font-black text-xs text-white shrink-0">
                        {hospitalInitials}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-bold text-slate-200 truncate">{hospitalName}</p>
                        <p className="text-[10px] text-slate-500 truncate">{user?.email || 'Hospital Admin'}</p>
                      </div>
                      <ShieldCheck className="h-3.5 w-3.5 text-teal-500 shrink-0" />
                    </button>
                    <button
                      onClick={handleHospitalLogout}
                      className="flex items-center gap-3 px-3 py-2 rounded-xl text-slate-500 hover:text-red-400 hover:bg-red-500/10 transition-all w-full text-sm font-semibold"
                      aria-label="Sign out"
                    >
                      <LogOut className="h-4 w-4 shrink-0" />
                      <span>Sign Out</span>
                    </button>
                  </div>
                </motion.div>

              ) : (
                /* ── NON-HOSPITAL Mobile Drawer (standard light theme) ── */
                <motion.div
                  initial={{ x: '-100%' }}
                  animate={{ x: 0 }}
                  exit={{ x: '-100%' }}
                  transition={{ type: 'spring', damping: 25, stiffness: 200 }}
                  className="fixed top-0 bottom-0 left-0 w-72 bg-white dark:bg-slate-900 border-r border-slate-100 dark:border-slate-800 p-6 z-50 flex flex-col gap-6 md:hidden shadow-2xl"
                >
                  <div className="flex items-center justify-between">
                    <Link to="/" onClick={() => setIsMobileMenuOpen(false)}>
                      <BloodBridgeLogo size="md" />
                    </Link>
                    <button
                      onClick={() => setIsMobileMenuOpen(false)}
                      className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl"
                      aria-label="Close menu"
                    >
                      <X className="h-5 w-5" />
                    </button>
                  </div>
                  
                  <nav className="flex flex-col gap-1">
                    {links.map((link) => {
                      const IconComponent = link.icon;
                      return (
                        <NavLink
                          key={link.to}
                          to={link.to}
                          onClick={() => setIsMobileMenuOpen(false)}
                          className={({ isActive }) => {
                            const mobileActiveClass =
                              role === 'ADMIN'
                                ? 'bg-indigo-50 dark:bg-indigo-950/30 text-indigo-700 dark:text-indigo-400 shadow-sm'
                                : 'bg-red-50/70 dark:bg-red-950/30 text-primary dark:text-red-400 shadow-sm';
                            return `flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all outline-none ${
                              isActive
                                ? mobileActiveClass
                                : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-50/50 dark:hover:bg-slate-800/40'
                            }`;
                          }}
                        >
                          <IconComponent className="h-4 w-4 shrink-0" />
                          {link.label}
                        </NavLink>
                      );
                    })}
                  </nav>
                </motion.div>
              )}
            </>
          )}
        </AnimatePresence>

        <main className="flex-1 p-4 sm:p-6 md:p-8 overflow-y-auto w-full">
          <div className="max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
      <AiAssistant />
      <ToastContainer />
    </div>
  );
}
export { MainLayout };
