import React from 'react';
import logoImg from '../../assets/logo.png';

/**
 * Official BloodBridge Reusable Logo Component.
 * Ensures consistent branding across Home, Auth (Donor, Hospital, Admin), Navbar, Sidebar, and Dashboards.
 */
export default function BloodBridgeLogo({
  size = 'md', // 'sm' | 'md' | 'lg' | 'xl' | '2xl'
  className = '',
  showTagline = false,
  taglineColor = 'text-slate-500 dark:text-slate-400',
  ...props
}) {
  const sizeClasses = {
    sm: 'h-8 w-auto',
    md: 'h-10 w-auto',
    lg: 'h-12 w-auto',
    xl: 'h-16 w-auto',
    '2xl': 'h-24 w-auto',
    '3xl': 'h-36 w-auto',
    hero: 'h-56 sm:h-[340px] lg:h-[420px] w-auto',
  };

  const selectedSize = sizeClasses[size] || (typeof size === 'string' && size.includes('h-') ? size : 'h-10 w-auto');

  return (
    <div className={`inline-flex items-center gap-2.5 ${className}`} {...props}>
      <img
        src={logoImg}
        alt="BloodBridge Official Logo"
        className={`${selectedSize} object-cover rounded-full transition-transform duration-200 shrink-0`}
      />
      {showTagline && (
        <span className={`text-[10px] font-bold uppercase tracking-widest ${taglineColor}`}>
          Connecting Blood. Saving Lives.
        </span>
      )}
    </div>
  );
}
