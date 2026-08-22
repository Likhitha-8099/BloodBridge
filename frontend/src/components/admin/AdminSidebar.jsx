import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { useUnreadNotifications } from '../../hooks/useNotifications';
import {
  LayoutDashboard,
  Users,
  FileText,
  ClipboardList,
  Shield,
  Bell,
  Activity,
  LogOut,
  ChevronLeft,
  ChevronRight,
  ShieldCheck,
  User,
  Heart,
  Building2,
} from 'lucide-react';

const COLLAPSE_KEY = 'bb-admin-sidebar-collapsed';

const NAV_ITEMS = [
  {
    to: '/admin/dashboard',
    label: 'Dashboard',
    icon: LayoutDashboard,
    description: 'System overview & metrics',
  },
  {
    to: '/admin/donors',
    label: 'Donors',
    icon: Heart,
    description: 'Manage registered donors',
  },
  {
    to: '/admin/hospitals',
    label: 'Hospitals',
    icon: Building2,
    description: 'Manage registered hospitals',
  },
  {
    to: '/admin/users',
    label: 'User Demographics',
    icon: Users,
    description: 'Donor & patient demographics',
  },
  {
    to: '/admin/requests',
    label: 'Request Analytics',
    icon: FileText,
    description: 'Emergency & standard requests',
  },
  {
    to: '/admin/donations',
    label: 'Donation Tracking',
    icon: ClipboardList,
    description: 'Transfusions & certificates',
  },
  {
    to: '/admin/matching',
    label: 'Smart Matching',
    icon: Shield,
    description: '10-Stage matching engine',
  },
  {
    to: '/notifications',
    label: 'Notifications',
    icon: Bell,
    hasBadge: true,
    description: 'System alerts & logs',
  },
  {
    to: '/admin/notifications',
    label: 'Notification Analytics',
    icon: Activity,
    description: 'STOMP & push metrics',
  },
  {
    to: '/admin/profile',
    label: 'Admin Profile',
    icon: User,
    description: 'Account settings & security',
  },
];

function NavItem({ item, collapsed, unreadCount }) {
  const IconComponent = item.icon;

  return (
    <div className="relative group/nav">
      <NavLink
        to={item.to}
        className={({ isActive }) =>
          [
            'flex items-center gap-3 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200',
            'outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:ring-offset-1 focus-visible:ring-offset-slate-950',
            'relative border-l-2',
            collapsed ? 'px-0 justify-center w-full' : 'px-3 pl-2.5',
            isActive
              ? 'bg-indigo-50 text-indigo-700 border-indigo-600 dark:bg-indigo-500/10 dark:text-indigo-300 dark:border-indigo-500 font-bold'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/80 dark:text-slate-400 dark:hover:text-slate-100 dark:hover:bg-slate-800/70 border-transparent',
          ].join(' ')
        }
      >
        {({ isActive }) => (
          <>
            <span
              className={[
                'shrink-0 transition-transform duration-200 group-hover/nav:scale-110',
                collapsed ? 'mx-auto' : '',
                isActive ? 'text-indigo-600 dark:text-indigo-400' : 'text-slate-400 dark:text-slate-500 group-hover/nav:text-slate-700 dark:group-hover/nav:text-slate-200',
              ].join(' ')}
            >
              <IconComponent className="h-[18px] w-[18px]" />
            </span>

            {!collapsed && (
              <span className="truncate flex-1 leading-none">{item.label}</span>
            )}

            {!collapsed && item.hasBadge && unreadCount > 0 && (
              <span className="bg-indigo-500 text-white text-[10px] font-black px-1.5 py-0.5 rounded-full shrink-0 leading-none animate-pulse">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}

            {collapsed && item.hasBadge && unreadCount > 0 && (
              <span className="absolute top-1 right-1.5 h-2 w-2 bg-indigo-400 rounded-full border-2 border-slate-950 animate-pulse" />
            )}
          </>
        )}
      </NavLink>

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
          <div className="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-slate-700/80" />
          <p className="text-xs font-bold text-white leading-none">{item.label}</p>
          <p className="text-[10px] text-slate-400 mt-0.5 leading-none">{item.description}</p>
          {item.hasBadge && unreadCount > 0 && (
            <span className="ml-0 mt-1 inline-flex bg-indigo-500 text-white text-[9px] font-black px-1.5 py-0.5 rounded-full">
              {unreadCount > 9 ? '9+' : unreadCount} unread
            </span>
          )}
        </div>
      )}
    </div>
  );
}

export default function AdminSidebar() {
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

  const adminName = user?.fullName || user?.name || user?.email || 'Administrator';
  const initials = adminName
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase() || 'A';

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
    navigate('/', { replace: true });
  };

  return (
    <aside
      className={[
        'relative hidden md:flex flex-col shrink-0',
        'bg-white dark:bg-slate-950',
        'border-r border-slate-200/80 dark:border-slate-800/60',
        'text-slate-800 dark:text-slate-200',
        'transition-[width] duration-300 ease-in-out motion-reduce:transition-none',
        collapsed ? 'w-[72px]' : 'w-[260px]',
      ].join(' ')}
    >
      <div
        className={[
          'flex items-center border-b border-slate-800/60 shrink-0 relative py-3.5',
          collapsed ? 'justify-center px-2' : 'justify-between px-4',
        ].join(' ')}
      >
        {!collapsed && (
          <div className="flex items-center gap-2 overflow-hidden">
            <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-700 flex items-center justify-center text-white shadow-sm">
              <Shield className="h-4 w-4" />
            </div>
            <span className="text-xs font-black text-slate-200 uppercase tracking-wider">
              Admin Menu
            </span>
          </div>
        )}

        {collapsed && (
          <div className="flex items-center justify-center">
            <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-700 flex items-center justify-center text-white shadow-sm">
              <Shield className="h-4.5 w-4.5" />
            </div>
          </div>
        )}

        <button
          onClick={toggleCollapse}
          className={[
            'flex items-center gap-1 text-[11px] font-bold text-slate-400 hover:text-indigo-300',
            'px-2 py-1 rounded-lg hover:bg-slate-800/80 transition-all',
            'focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500',
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

      {!collapsed && (
        <div className="px-4 pt-4 pb-1.5">
          <p className="text-[10px] font-bold text-slate-600 uppercase tracking-widest">
            Platform Controls
          </p>
        </div>
      )}

      <nav
        className={[
          'flex-1 overflow-y-auto overflow-x-visible flex flex-col',
          collapsed ? 'px-2 py-3 gap-1' : 'px-3 pb-4 gap-0.5',
        ].join(' ')}
        aria-label="Admin navigation"
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

      <div className="shrink-0 border-t border-slate-800/60 py-3 px-2 flex flex-col gap-1">
        <div className="relative group/profile">
          <button
            onClick={() => navigate('/admin/dashboard')}
            className={[
              'flex items-center gap-3 py-2.5 rounded-xl w-full text-left',
              'hover:bg-slate-800/70 transition-all duration-200',
              'focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500',
              'group/profile-btn',
              collapsed ? 'justify-center px-2' : 'px-3',
            ].join(' ')}
            aria-label={`Admin profile: ${adminName}`}
          >
            <div
              className={[
                'rounded-xl bg-gradient-to-br from-indigo-600 to-purple-700',
                'flex items-center justify-center font-black text-white shrink-0',
                'shadow-md group-hover/profile-btn:shadow-indigo-700/50 transition-shadow duration-200',
                collapsed ? 'h-9 w-9 text-sm mx-auto' : 'h-8 w-8 text-xs',
              ].join(' ')}
            >
              {initials}
            </div>

            {!collapsed && (
              <>
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-bold text-slate-200 truncate leading-tight">
                    {adminName}
                  </p>
                  <p className="text-[10px] text-slate-500 truncate leading-tight mt-0.5">
                    {user?.email || 'Platform Admin'}
                  </p>
                </div>
                <ShieldCheck className="h-3.5 w-3.5 text-indigo-400 shrink-0" />
              </>
            )}
          </button>

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
              <p className="text-xs font-bold text-white">{adminName}</p>
              <p className="text-[10px] text-slate-400 mt-0.5">
                {user?.email || 'Platform Admin'}
              </p>
            </div>
          )}
        </div>

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
            aria-label="Sign out of admin portal"
          >
            <LogOut className="h-4 w-4 shrink-0 group-hover/logout-btn:scale-110 transition-transform duration-200" />
            {!collapsed && <span>Sign Out</span>}
          </button>

          {collapsed && (
            <div
              className={[
                'absolute left-full ml-3 top-1/2 -translate-y-1/2 z-[60]',
                'px-3 py-2 bg-slate-800 border border-slate-700/80 rounded-xl shadow-2xl',
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
