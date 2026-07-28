import React, { useState } from 'react';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';
import { Outlet, NavLink } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useThemeStore } from '../store/themeStore';
import ToastContainer from '../components/ui/Toast';
import { AnimatePresence, motion } from 'framer-motion';
import { 
  X, 
  Heart, 
  LayoutDashboard, 
  User, 
  Activity, 
  ClipboardList, 
  FileText, 
  Bell, 
  Users, 
  Shield, 
  Award 
} from 'lucide-react';

/**
 * Main application layout wrapping navigation components, transition animations, and dark mode.
 */
export default function MainLayout() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const { role } = useAuthStore();
  const { theme } = useThemeStore();

  const getLinks = () => {
    switch (role) {
      case 'DONOR':
        return [
          { to: '/donor/dashboard', label: 'Donor Dashboard', icon: LayoutDashboard },
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
        return [
          { to: '/hospital/dashboard', label: 'Hospital Dashboard', icon: LayoutDashboard },
          { to: '/hospital/profile', label: 'Hospital Profile', icon: User },
          { to: '/hospital/requests', label: 'Blood Requests', icon: FileText },
          { to: '/hospital/matches', label: 'Donor Matches', icon: Heart },
          { to: '/hospital/donations', label: 'Donations', icon: ClipboardList },
          { to: '/notifications', label: 'Notifications', icon: Bell },
        ];
      case 'ADMIN':
        return [
          { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
          { to: '/admin/users', label: 'Users', icon: Users },
          { to: '/admin/requests', label: 'Requests', icon: FileText },
          { to: '/admin/donations', label: 'Donations', icon: ClipboardList },
          { to: '/admin/matching', label: 'Matching', icon: Shield },
          { to: '/admin/hospitals', label: 'Hospitals', icon: Award },
          { to: '/notifications', label: 'Notifications', icon: Bell },
          { to: '/admin/system-health', label: 'System Health', icon: Activity },
        ];
      default:
        return [];
    }
  };

  const links = getLinks();

  return (
    <div className="min-h-screen bg-background dark:bg-slate-950 text-slate-900 dark:text-slate-100 flex flex-col transition-colors duration-200">
      <Navbar onMenuToggle={() => setIsMobileMenuOpen(!isMobileMenuOpen)} />
      <div className="flex flex-1 relative">
        <Sidebar />
        
        {/* Mobile Navigation Drawer Overlay */}
        <AnimatePresence>
          {isMobileMenuOpen && (
            <>
              {/* Backdrop */}
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 0.4 }}
                exit={{ opacity: 0 }}
                onClick={() => setIsMobileMenuOpen(false)}
                className="fixed inset-0 bg-black z-40 md:hidden"
              />
              
              {/* Drawer Content */}
              <motion.div
                initial={{ x: '-100%' }}
                animate={{ x: 0 }}
                exit={{ x: '-100%' }}
                transition={{ type: 'spring', damping: 25, stiffness: 200 }}
                className="fixed top-0 bottom-0 left-0 w-72 bg-white dark:bg-slate-900 border-r border-slate-100 dark:border-slate-800 p-6 z-50 flex flex-col gap-6 md:hidden shadow-2xl"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Heart className="h-6 w-6 text-primary fill-primary" />
                    <span className="font-bold text-lg tracking-tight text-slate-900 dark:text-white">
                      Blood<span className="text-primary">Bridge</span>
                    </span>
                  </div>
                  <button
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl"
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
                        className={({ isActive }) =>
                          `flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all outline-none ${
                            isActive 
                              ? 'bg-red-50/70 dark:bg-red-950/30 text-primary dark:text-red-400 shadow-sm' 
                              : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-50/50 dark:hover:bg-slate-800/40'
                          }`
                        }
                      >
                        <IconComponent className="h-4 w-4 shrink-0" />
                        {link.label}
                      </NavLink>
                    );
                  })}
                </nav>
              </motion.div>
            </>
          )}
        </AnimatePresence>

        <main className="flex-1 p-4 sm:p-6 md:p-8 overflow-y-auto w-full">
          <div className="max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
      <ToastContainer />
    </div>
  );
}
export { MainLayout };
