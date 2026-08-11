import React from 'react';
import { useAuthStore } from '../../store/authStore';
import { useUnreadNotifications } from '../../hooks/useNotifications';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  User, 
  Activity, 
  ClipboardList, 
  FileText, 
  Bell,
  Users,
  Shield,
  Award,
  TrendingUp
} from 'lucide-react';
import HospitalSidebar from '../hospital/HospitalSidebar';
import DonorSidebar from '../donor/DonorSidebar';
import AdminSidebar from '../admin/AdminSidebar';

/**
 * Sidebar Navigation rendering custom link items matching user roles with live notification badge.
 * Hospital, Donor, and Admin roles render their dedicated sidebars instead.
 */
export default function Sidebar() {
  const { role } = useAuthStore();

  // Always call hooks unconditionally before any conditional logic
  const { data: unreadList } = useUnreadNotifications();
  const notificationsArray = Array.isArray(unreadList) ? unreadList : (unreadList?.data || []);
  const unreadCount = notificationsArray.length;

  // Dedicated role-based sidebars
  if (role === 'HOSPITAL') {
    return <HospitalSidebar />;
  }

  if (role === 'DONOR') {
    return <DonorSidebar />;
  }

  if (role === 'ADMIN') {
    return <AdminSidebar />;
  }

  const getLinks = () => {
    switch (role) {
      case 'DONOR':
        return [
          { to: '/donor/dashboard', label: 'Donor Dashboard', icon: LayoutDashboard },
          { to: '/donor/impact', label: 'My Impact', icon: TrendingUp },
          { to: '/donor/profile', label: 'My Profile', icon: User },
          { to: '/donor/requests', label: 'Blood Requests', icon: FileText },
          { to: '/donor/history', label: 'Donation History', icon: ClipboardList },
          { to: '/notifications', label: 'Notifications', icon: Bell, hasBadge: true },
        ];
      case 'PATIENT':
        return [
          { to: '/patient/dashboard', label: 'Patient Dashboard', icon: LayoutDashboard },
          { to: '/patient/profile', label: 'My Profile', icon: User },
          { to: '/patient/create-request', label: 'Create Request', icon: ClipboardList },
          { to: '/patient/requests', label: 'My Requests', icon: FileText },
          { to: '/notifications', label: 'Notifications', icon: Bell, hasBadge: true },
        ];
      case 'ADMIN':
        return [
          { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
          { to: '/admin/hospitals', label: 'Hospitals & Approvals', icon: Award },
          { to: '/admin/users', label: 'User Demographics', icon: Users },
          { to: '/admin/requests', label: 'Request Analytics', icon: FileText },
          { to: '/admin/donations', label: 'Donation Tracking', icon: ClipboardList },
          { to: '/admin/matching', label: 'Smart Matching', icon: Shield },
          { to: '/notifications', label: 'Notifications', icon: Bell, hasBadge: true },
          { to: '/admin/notifications', label: 'Notification Analytics', icon: Activity },
        ];
      default:
        return [];
    }
  };

  const links = getLinks();

  // Active class varies by role for design consistency
  const getActiveClass = () => {
    switch (role) {
      case 'ADMIN':
        return 'bg-indigo-50 dark:bg-indigo-950/30 text-indigo-700 dark:text-indigo-400 shadow-sm';
      default:
        return 'bg-red-50/70 dark:bg-red-950/30 text-primary dark:text-red-400 shadow-sm';
    }
  };

  const activeClass = getActiveClass();

  return (
    <aside className="w-64 bg-white dark:bg-slate-900 border-r border-slate-100 dark:border-slate-800 py-6 px-4 flex flex-col gap-1 shadow-sm hidden md:flex transition-colors">
      {links.map((link) => {
        const IconComponent = link.icon;
        return (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `flex items-center justify-between px-4 py-3 rounded-xl text-sm font-semibold transition-all outline-none focus-visible:ring-2 focus-visible:ring-primary ${
                isActive
                  ? activeClass
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-50/50 dark:hover:bg-slate-800/40'
              }`
            }
          >
            <div className="flex items-center gap-3.5">
              <IconComponent className="h-4 w-4 shrink-0" />
              <span>{link.label}</span>
            </div>
            {link.hasBadge && unreadCount > 0 && (
              <span className="bg-primary text-white text-[10px] font-black px-2 py-0.5 rounded-full">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </NavLink>
        );
      })}
    </aside>
  );
}
