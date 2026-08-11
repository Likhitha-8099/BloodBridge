import React from 'react';
import { useLocation } from 'react-router-dom';
import DonorLogin from './DonorLogin';
import HospitalLogin from './HospitalLogin';
import AdminLogin from './AdminLogin';

/**
 * Unified Login Router Component.
 * Delegates rendering to specialized, creative & modern role login components:
 * - DonorLogin (/login, /login/donor)
 * - HospitalLogin (/login/hospital)
 * - AdminLogin (/login/admin)
 */
export default function Login() {
  const location = useLocation();
  const pathname = location.pathname.toLowerCase();

  if (pathname.includes('admin')) {
    return <AdminLogin />;
  }

  if (pathname.includes('hospital')) {
    return <HospitalLogin />;
  }

  // Default to Donor Login experience
  return <DonorLogin />;
}
