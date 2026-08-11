import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { useUnreadNotifications } from '../../hooks/useNotifications';
import {
  LayoutDashboard,
  FileText,
  PlusCircle,
  Activity,
  Heart,
  ClipboardList,
  Users,
  Bell,
  User,
  LogOut,
  ChevronLeft,
  ChevronRight,
  ShieldCheck,
  Stethoscope,
  Bot,
} from 'lucide-react';

const COLLAPSE_KEY = 'bb-hospital-sidebar-collapsed';

/**
 * Hospital Control Center — dedicated sidebar for HOSPITAL role.
 * Features: collapsible, tooltips, real notification badge, hospital avatar, smooth transitions.
 * Desktop only — mobile handled by MainLayout drawer.
 */

const NAV_ITEMS = [
  {
    to: '/hospital/dashboard',
    label: 'Hospital Dashboard',
    icon: LayoutDashboard,
    description: 'Overview & analytics',
  },
  {
    to: '/hospital/requests',
    label: 'Blood Requests',
    icon: FileText,
    description: 'Manage blood requests',
  },
  {
    to: '/hospital/create-request',
    label: 'Create Request',
    icon: PlusCircle,
    description: 'New blood request',
  },
  {
    to: '/hospital/matches',
    label: 'Donor Matches',
    icon: Activity,
    description: 'Matched donor list',
  },
  {
    to: '/hospital/donors',
    label: 'Accepted Donors',
    icon: Heart,
    description: 'Confirmed donations',
  },
  {
    to: '/hospital/donations',
    label: 'Donation History',
    icon: ClipboardList,
    description: 'Completed transfusions',
  },
  {
    to: '/hospital/users',
    label: 'Staff Users',
    icon: Users,
    description: 'Manage hospital staff',
  },
  {
    to: '/notifications',
    label: 'Notifications',
    icon: Bell,
    hasBadge: true,
    description: 'Alerts & updates',
  },
  {
    to: '/hospital/profile',
    label: 'Hospital Profile',
    icon: User,
    description: 'Institution settings',
  },
  {
    to: '/hospital/ai-assistant',
    label: 'AI Assistant',
    icon: Bot,
    description: 'Clinical & admin AI assistant',
  },
];

// Individual nav item with tooltip support
function NavItem({ item, collapsed, unreadCount }) {
  const IconComponent = item.icon;

  return (
    <div className="relative group/nav">
      <NavLink
        to={item.to}
        className={({ isActive }) =>
          [
            'flex items-center gap-3 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200',
            'outline-none focus-visible:ring-2 focus-visible:ring-teal-500 focus-visible:ring-offset-1 focus-visible:ring-offset-slate-950',
            'relative border-l-2',
            collapsed ? 'px-0 justify-center w-full' : 'px-3 pl-2.5',
            isActive
              ? 'bg-teal-50 text-teal-700 border-teal-600 dark:bg-teal-500/10 dark:text-teal-300 dark:border-teal-500 font-bold'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/80 dark:text-slate-400 dark:hover:text-slate-100 dark:hover:bg-slate-800/70 border-transparent',
          ].join(' ')
        }
      >
        {({ isActive }) => (
          <>
            {/* Icon */}
            <span
              className={[
                'shrink-0 transition-transform duration-200 group-hover/nav:scale-110',
                collapsed ? 'mx-auto' : '',
                isActive ? 'text-teal-600 dark:text-teal-400' : 'text-slate-400 dark:text-slate-500 group-hover/nav:text-slate-700 dark:group-hover/nav:text-slate-200',
              ].join(' ')}
            >
              <IconComponent className="h-[18px] w-[18px]" />
            </span>

            {/* Label — hidden when collapsed */}
            {!collapsed && (
              <span className="truncate flex-1 leading-none">{item.label}</span>
            )}

            {/* Notification badge — full when expanded */}
            {!collapsed && item.hasBadge && unreadCount > 0 && (
              <span className="bg-teal-500 text-white text-[10px] font-black px-1.5 py-0.5 rounded-full shrink-0 leading-none animate-pulse">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}

            {/* Notification dot — collapsed */}
            {collapsed && item.hasBadge && unreadCount > 0 && (
              <span className="absolute top-1 right-1.5 h-2 w-2 bg-teal-400 rounded-full border-2 border-slate-950 animate-pulse" />
            )}
          </>
        )}
      </NavLink>

      {/* Tooltip — shown in collapsed mode on hover */}
      {collapsed && (
        <div
          className={[
            'absolute left-full ml-3 top-1/2 -translate-y-1/2 z-[60]',
            'px-3 py-2 bg-slate-800 border border-slate-700/80 rounded-xl shadow-2xl',
            'opacity-0 group-hover/nav:opacity-100 pointer-events-none',
            'transition-all duration-150 ease-out',
            'translate-x-1 group-hover/nav:translate-x-0',
            'whitespace-nowrap',
          ].join(' ')}
          role="tooltip"
        >
          {/* Arrow */}
          <div className="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-slate-700/80" />
          <p className="text-xs font-bold text-white leading-none">{item.label}</p>
          <p className="text-[10px] text-slate-400 mt-0.5 leading-none">{item.description}</p>
          {item.hasBadge && unreadCount > 0 && (
            <span className="ml-0 mt-1 inline-flex bg-teal-500 text-white text-[9px] font-black px-1.5 py-0.5 rounded-full">
              {unreadCount > 9 ? '9+' : unreadCount} unread
            </span>
          )}
        </div>
      )}
    </div>
  );
}

export default function HospitalSidebar() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const [collapsed, setCollapsed] = useState(() => {
    try {
      return localStorage.getItem(COLLAPSE_KEY) === 'true';
    } catch {
      return false;
    }
  });

  const { data: unreadList } = useUnreadNotifications();
  const notificationsArray = Array.isArray(unreadList)
    ? unreadList
    : unreadList?.data || [];
  const unreadCount = notificationsArray.length;

  const hospitalName =
    user?.hospitalName || user?.name || user?.fullName || 'Hospital';
  const initials = hospitalName
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'H';

  const toggleCollapse = () => {
    const next = !collapsed;
    setCollapsed(next);
    try {
      localStorage.setItem(COLLAPSE_KEY, String(next));
    } catch {
      // ignore storage errors
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside
      className={[
        // Layout
        'relative hidden md:flex flex-col shrink-0',
        // Colors
        'bg-white dark:bg-slate-950',
        // Right border
        'border-r border-slate-200/80 dark:border-slate-800/60',
        // Text
        'text-slate-800 dark:text-slate-200',
        // Width transition — respects prefers-reduced-motion via CSS
        'transition-[width] duration-300 ease-in-out motion-reduce:transition-none',
        collapsed ? 'w-[72px]' : 'w-[260px]',
      ].join(' ')}
    >
      {/* ────────────────────────────────────────
          TOP: Sidebar Collapse/Expand Toggle Header
      ──────────────────────────────────────── */}
      <div
        className={[
          'flex items-center border-b border-slate-800/60 shrink-0 relative py-3.5',
          collapsed ? 'justify-center px-2' : 'justify-between px-4',
        ].join(' ')}
      >
        {/* Expanded Header Title */}
        {!collapsed && (
          <div className="flex items-center gap-2 overflow-hidden">
            <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center text-white shadow-sm">
              <Stethoscope className="h-4 w-4" />
            </div>
            <span className="text-xs font-black text-slate-200 uppercase tracking-wider">
              Navigation
            </span>
          </div>
        )}

        {/* Collapsed Icon Badge */}
        {collapsed && (
          <div className="flex items-center justify-center">
            <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center text-white shadow-sm">
              <Stethoscope className="h-4.5 w-4.5" />
            </div>
          </div>
        )}

        {/* Collapse toggle button */}
        <button
          onClick={toggleCollapse}
          className={[
            'flex items-center gap-1 text-[11px] font-bold text-slate-400 hover:text-teal-300',
            'px-2 py-1 rounded-lg hover:bg-slate-800/80 transition-all',
            'focus:outline-none focus-visible:ring-2 focus-visible:ring-teal-500',
          ].join(' ')}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? (
            <ChevronRight className="h-4 w-4" />
          ) : (
            <>
              <ChevronLeft className="h-4 w-4" />
              <span className="text-[10px] text-slate-500 uppercase tracking-wide">Collapse</span>
            </>
          )}
        </button>
      </div>

      {/* ────────────────────────────────────────
          SECTION LABEL — expanded only
      ──────────────────────────────────────── */}
      {!collapsed && (
        <div className="px-4 pt-4 pb-1.5">
          <p className="text-[10px] font-bold text-slate-600 uppercase tracking-widest">
            Control Center
          </p>
        </div>
      )}

      {/* ────────────────────────────────────────
          NAVIGATION
      ──────────────────────────────────────── */}
      <nav
        className={[
          'flex-1 overflow-y-auto overflow-x-visible flex flex-col',
          collapsed ? 'px-2 py-3 gap-1' : 'px-3 pb-4 gap-0.5',
        ].join(' ')}
        aria-label="Hospital navigation"
      >
        {NAV_ITEMS.map((item) => (
          <NavItem
            key={item.to}
            item={item}
            collapsed={collapsed}
            unreadCount={unreadCount}
          />
        ))}
      </nav>

      {/* ────────────────────────────────────────
          BOTTOM: Hospital Profile + Logout
      ──────────────────────────────────────── */}
      <div className="shrink-0 border-t border-slate-800/60 py-3 px-2 flex flex-col gap-1">
        {/* Hospital Profile Button */}
        <div className="relative group/profile">
          <button
            onClick={() => navigate('/hospital/profile')}
            className={[
              'flex items-center gap-3 py-2.5 rounded-xl w-full text-left',
              'hover:bg-slate-800/70 transition-all duration-200',
              'focus:outline-none focus-visible:ring-2 focus-visible:ring-teal-500',
              'group/profile-btn',
              collapsed ? 'justify-center px-2' : 'px-3',
            ].join(' ')}
            aria-label={`Hospital profile: ${hospitalName}`}
          >
            {/* Avatar */}
            <div
              className={[
                'rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600',
                'flex items-center justify-center font-black text-white shrink-0',
                'shadow-md group-hover/profile-btn:shadow-teal-700/50 transition-shadow duration-200',
                collapsed ? 'h-9 w-9 text-sm mx-auto' : 'h-8 w-8 text-xs',
              ].join(' ')}
            >
              {initials}
            </div>

            {/* Name + email — expanded only */}
            {!collapsed && (
              <>
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-bold text-slate-200 truncate leading-tight">
                    {hospitalName}
                  </p>
                  <p className="text-[10px] text-slate-500 truncate leading-tight mt-0.5">
                    {user?.email || 'Hospital Admin'}
                  </p>
                </div>
                <ShieldCheck className="h-3.5 w-3.5 text-teal-500 shrink-0" />
              </>
            )}
          </button>

          {/* Tooltip — collapsed mode */}
          {collapsed && (
            <div
              className={[
                'absolute left-full ml-3 top-1/2 -translate-y-1/2 z-[60]',
                'px-3 py-2 bg-slate-800 border border-slate-700/80 rounded-xl shadow-2xl',
                'opacity-0 group-hover/profile:opacity-100 pointer-events-none',
                'transition-all duration-150 ease-out translate-x-1 group-hover/profile:translate-x-0',
                'whitespace-nowrap',
              ].join(' ')}
            >
              <div className="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-slate-700/80" />
              <p className="text-xs font-bold text-white">{hospitalName}</p>
              <p className="text-[10px] text-slate-400 mt-0.5">
                {user?.email || 'Hospital Admin'}
              </p>
            </div>
          )}
        </div>

        {/* Logout Button */}
        <div className="relative group/logout">
          <button
            onClick={handleLogout}
            className={[
              'flex items-center gap-3 py-2 rounded-xl w-full',
              'text-slate-500 hover:text-red-400 hover:bg-red-500/10',
              'transition-all duration-200 text-sm font-semibold',
              'focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500',
              'group/logout-btn',
              collapsed ? 'justify-center px-2' : 'px-3',
            ].join(' ')}
            aria-label="Sign out of hospital portal"
          >
            <LogOut className="h-4 w-4 shrink-0 group-hover/logout-btn:scale-110 transition-transform duration-200" />
            {!collapsed && <span>Sign Out</span>}
          </button>

          {/* Tooltip — collapsed */}
          {collapsed && (
            <div
              className={[
                'absolute left-full ml-3 top-1/2 -translate-y-1/2 z-[60]',
                'px-3 py-1.5 bg-slate-800 border border-slate-700/80 rounded-xl shadow-2xl',
                'opacity-0 group-hover/logout:opacity-100 pointer-events-none',
                'transition-all duration-150 ease-out translate-x-1 group-hover/logout:translate-x-0',
                'whitespace-nowrap',
              ].join(' ')}
            >
              <div className="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-slate-700/80" />
              <p className="text-xs font-bold text-red-400">Sign Out</p>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
}
