import React from 'react';
import { useAuthStore } from '../../store/authStore';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  User, 
  Activity, 
  ClipboardList, 
  FileText, 
  Bell,
  Heart,
  Users,
  Shield,
  Award
} from 'lucide-react';

/**
 * Sidebar Navigation rendering custom link items matching user roles.
 */
export default function Sidebar() {
  const { role } = useAuthStore();

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
    <aside className="w-64 bg-white dark:bg-slate-900 border-r border-slate-100 dark:border-slate-800 py-6 px-4 flex flex-col gap-1 shadow-sm hidden md:flex transition-colors">
      {links.map((link) => {
        const IconComponent = link.icon;
        return (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all outline-none focus-visible:ring-2 focus-visible:ring-primary ${
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
    </aside>
  );
}
