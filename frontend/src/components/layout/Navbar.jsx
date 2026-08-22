import React, { useState, useRef, useEffect } from 'react';
import { useAuthStore } from '../../store/authStore';
import { LogOut, User, Menu, ChevronDown } from 'lucide-react';
import { useNavigate, Link } from 'react-router-dom';
import NotificationBell from '../ui/NotificationBell';
import ThemeToggle from '../ui/ThemeToggle';
import BloodBridgeLogo from '../common/BloodBridgeLogo';

/**
 * Global Header Navbar displaying platform branding, dark theme toggle, notifications, and mobile hamburger controls.
 */
export default function Navbar({ onMenuToggle }) {
  const { user, role, login, logout, isAuthenticated } = useAuthStore();
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/', { replace: true });
  };

  const handleRoleSwitch = async (newRole) => {
    try {
      const response = await authService.switchRole(newRole);
      // Unwrap ApiResponse<AuthResponse> wrapper if present
      const authData = response?.data ?? response;
      const { token, role: switchedRole, user: switchedUser } = authData;
      if (token && switchedRole) {
        login(token, switchedRole, switchedUser);
        navigate(`/${switchedRole.toLowerCase()}/dashboard`);
      }
      setIsDropdownOpen(false);
    } catch (err) {
      console.error('Failed to switch role:', err);
    }
  };

  const otherRoles = user?.roles ? user.roles.filter(r => r !== role) : [];

  const formatRoleLabel = (r) => {
    if (r === 'HOSPITAL') return 'Hospital';
    if (r === 'DONOR') return 'Donor';
    if (r === 'PATIENT') return 'Patient';
    if (r === 'ADMIN') return 'Admin';
    return r;
  };

  return (
    <header className="sticky top-0 z-40 bg-white dark:bg-slate-900 border-b border-slate-100 dark:border-slate-800 px-6 py-4 flex items-center justify-between shadow-sm transition-colors">
      <div className="flex items-center gap-3">
        {isAuthenticated && (
          <button
            onClick={onMenuToggle}
            className="p-2 md:hidden text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-xl transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
            aria-label="Toggle menu"
          >
            <Menu className="h-5 w-5" />
          </button>
        )}
        <Link to="/" className="flex items-center gap-2 hover:opacity-90 transition-opacity">
          <BloodBridgeLogo size="md" />
        </Link>
      </div>
      
      {isAuthenticated && user && (
        <div className="flex items-center gap-4">
          <ThemeToggle />
          <NotificationBell />
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={() => otherRoles.length > 0 && setIsDropdownOpen(!isDropdownOpen)}
              disabled={otherRoles.length === 0}
              className={`flex items-center gap-2 text-xs text-slate-700 dark:text-slate-300 bg-slate-50 dark:bg-slate-800 px-3.5 py-2 rounded-full border border-slate-100 dark:border-slate-700 transition-colors focus:outline-none ${
                otherRoles.length > 0 ? 'hover:bg-slate-100 dark:hover:bg-slate-700 cursor-pointer' : ''
              }`}
            >
              <User className="h-3.5 w-3.5 text-slate-400" />
              <span className="font-medium">{user.fullName || user.email}</span>
              <span className="bg-red-100 dark:bg-red-950 text-primary dark:text-red-400 px-2 py-0.5 rounded-full font-bold uppercase text-[9px] tracking-wide flex items-center gap-1">
                {formatRoleLabel(role)}
                {otherRoles.length > 0 && (
                  <ChevronDown className="h-2.5 w-2.5 opacity-70" />
                )}
              </span>
            </button>

            {isDropdownOpen && otherRoles.length > 0 && (
              <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 rounded-2xl shadow-lg py-2 z-50">
                <div className="px-4 py-1.5 text-[9px] font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
                  Switch Role To
                </div>
                {otherRoles.map((r) => (
                  <button
                    key={r}
                    onClick={() => handleRoleSwitch(r)}
                    className="w-full text-left px-4 py-2 text-xs font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
                  >
                    {formatRoleLabel(r)} Dashboard
                  </button>
                ))}
              </div>
            )}
          </div>
          <button 
            onClick={handleLogout} 
            className="p-2 text-slate-450 dark:text-slate-400 hover:text-primary dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/40 rounded-xl transition-all focus:outline-none focus:ring-2 focus:ring-primary"
            title="Log Out"
          >
            <LogOut className="h-5 w-5" />
          </button>
        </div>
      )}
    </header>
  );
}
