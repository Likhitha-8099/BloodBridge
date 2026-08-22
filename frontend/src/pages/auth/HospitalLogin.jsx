import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link, Navigate } from 'react-router-dom';
import { motion, useReducedMotion } from 'framer-motion';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import { useDeviceRegistration } from '../../hooks/useDeviceRegistration';
import LoginInput from '../../components/auth/LoginInput';
import PasswordInput from '../../components/auth/PasswordInput';
import LoginButton from '../../components/auth/LoginButton';
import ForgotPasswordModal from '../../components/auth/ForgotPasswordModal';
import BloodBridgeLogo from '../../components/common/BloodBridgeLogo';
import { 
  Hospital, 
  ArrowLeft, 
  AlertCircle, 
  Mail, 
  Siren, 
  MapPin, 
  Activity, 
  ShieldCheck,
  Building2,
  Stethoscope
} from 'lucide-react';

/**
 * Professional Hospital Operations & Emergency Response Login Page for BloodBridge.
 * Preserves 100% of existing authentication logic, state management, and API routes.
 */
export default function HospitalLogin() {
  const { login, logout, isAuthenticated, role } = useAuthStore();
  const { registerDevice } = useDeviceRegistration();
  const navigate = useNavigate();
  const shouldReduceMotion = useReducedMotion();

  const [errorMsg, setErrorMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [showForgotModal, setShowForgotModal] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm({
    defaultValues: {
      email: '',
      password: ''
    }
  });

  // Clear previous module authentication if navigating from Donor or Admin
  useEffect(() => {
    if (isAuthenticated && role !== 'HOSPITAL') {
      logout();
    }
  }, [isAuthenticated, role, logout]);

  if (isAuthenticated && role === 'HOSPITAL') {
    return <Navigate to="/hospital/dashboard" replace />;
  }

  const onSubmit = async (data) => {
    setIsLoading(true);
    setErrorMsg('');
    try {
      const authData = await authService.login(data);
      const { token, role: userRole, user } = authData;

      if (!token || !userRole) {
        setErrorMsg('Login failed: incomplete response from server. Please try again.');
        setIsLoading(false);
        return;
      }

      setIsSuccess(true);
      login(token, userRole, user);

      // Non-blocking FCM device registration
      registerDevice().catch((err) => console.warn('[HospitalLogin] FCM registration skipped:', err));

      setTimeout(() => {
        navigate(`/${userRole.toLowerCase()}/dashboard`);
      }, 500);
    } catch (err) {
      setErrorMsg(err.message || 'Invalid email or password.');
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col lg:flex-row font-sans selection:bg-teal-500 selection:text-white relative overflow-hidden">
      {/* =================================================== */}
      {/* LEFT SIDE VISUAL HERO - Hospital Operations Theme */}
      {/* =================================================== */}
      <div className="w-full lg:w-1/2 bg-gradient-to-br from-slate-900 via-teal-950 to-emerald-950 text-white p-8 lg:p-14 flex flex-col justify-between relative overflow-hidden min-h-[380px] lg:min-h-screen border-r border-teal-900/30">
        {/* Animated Background Pulse Waves */}
        <motion.div
          animate={shouldReduceMotion ? {} : {
            scale: [1, 1.2, 1],
            opacity: [0.15, 0.35, 0.15]
          }}
          transition={{ duration: 8, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute -top-24 -left-24 w-96 h-96 bg-teal-500/20 rounded-full blur-3xl pointer-events-none"
        />
        <motion.div
          animate={shouldReduceMotion ? {} : {
            scale: [1, 1.15, 1],
            opacity: [0.1, 0.3, 0.1]
          }}
          transition={{ duration: 10, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
          className="absolute -bottom-24 -right-24 w-[28rem] h-[28rem] bg-emerald-500/20 rounded-full blur-3xl pointer-events-none"
        />

        {/* ECG Radar Pulse Wave SVG Animation */}
        <div className="absolute inset-0 flex items-center justify-center opacity-15 pointer-events-none">
          <svg className="w-full h-48" viewBox="0 0 1200 200" fill="none" stroke="currentColor">
            <motion.path
              d="M0,100 L400,100 L420,40 L440,160 L460,80 L480,120 L500,100 L1200,100"
              stroke="rgb(45 212 191)"
              strokeWidth="3"
              initial={{ pathLength: 0 }}
              animate={shouldReduceMotion ? { pathLength: 1 } : { pathLength: [0, 1] }}
              transition={{ duration: 3, repeat: Infinity, ease: 'easeInOut' }}
            />
          </svg>
        </div>

        {/* Brand Header */}
        <div className="relative z-10">
          <Link to="/" className="flex items-center gap-3 group w-fit hover:opacity-90 transition-opacity">
            <div className="p-2 rounded-2xl bg-white/90 backdrop-blur-md shadow-lg border border-white/40">
              <BloodBridgeLogo size="md" />
            </div>
          </Link>
        </div>

        {/* Hero Central Content */}
        <div className="relative z-10 my-auto py-8">
          <div className="inline-flex items-center gap-2 bg-teal-500/15 backdrop-blur-md px-4 py-1.5 rounded-full text-xs font-semibold border border-teal-500/30 mb-6 text-teal-300">
            <ShieldCheck className="h-4 w-4 text-teal-400" />
            <span>Verified Healthcare Representative Portal</span>
          </div>

          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black leading-tight tracking-tight mb-4">
            Manage Emergency <br />
            <span className="text-teal-400">Blood Requests & Matches.</span>
          </h1>

          <p className="text-sm sm:text-base text-slate-300 leading-relaxed max-w-md mb-8">
            Real-time blood stock management, urgent patient requests, and automated matching with verified donors in your vicinity.
          </p>

          {/* Four Concept Badges */}
          <div className="grid grid-cols-2 gap-3 pt-2 max-w-md">
            <div className="bg-slate-900/60 backdrop-blur-md p-3.5 rounded-2xl border border-teal-900/50 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-teal-500/20 flex items-center justify-center text-teal-300 shrink-0">
                <Building2 className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">🏥 Hospital Portal</div>
            </div>

            <div className="bg-slate-900/60 backdrop-blur-md p-3.5 rounded-2xl border border-teal-900/50 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-rose-500/20 flex items-center justify-center text-rose-400 shrink-0">
                <Siren className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">🚨 Emergency Response</div>
            </div>

            <div className="bg-slate-900/60 backdrop-blur-md p-3.5 rounded-2xl border border-teal-900/50 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
                <Activity className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">🩸 Blood Matching</div>
            </div>

            <div className="bg-slate-900/60 backdrop-blur-md p-3.5 rounded-2xl border border-teal-900/50 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-cyan-500/20 flex items-center justify-center text-cyan-300 shrink-0">
                <MapPin className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">📍 Nearby Donors</div>
            </div>
          </div>
        </div>

        {/* Hero Footer */}
        <div className="relative z-10 text-xs text-slate-400 flex items-center justify-between pt-4 border-t border-slate-800">
          <span>© 2026 BloodBridge Hospital Network</span>
          <span className="flex items-center gap-1.5 text-teal-400">
            <Stethoscope className="h-3.5 w-3.5" /> Clinical Operations Portal
          </span>
        </div>
      </div>

      {/* =================================================== */}
      {/* RIGHT SIDE - Modern Interactive Login Form */}
      {/* =================================================== */}
      <div className="w-full lg:w-1/2 flex flex-col items-center justify-center p-6 sm:p-12 lg:p-16 relative">
        <div className="w-full max-w-md mb-6 flex items-center justify-between">
          <Link
            to="/"
            className="text-xs font-semibold text-slate-500 dark:text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 flex items-center gap-1.5 transition-colors group"
          >
            <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
            <span>Back to Home</span>
          </Link>

          <span className="text-[11px] font-bold text-teal-700 dark:text-teal-300 bg-teal-50 dark:bg-teal-950/60 px-3 py-1 rounded-full border border-teal-100 dark:border-teal-900/40">
            Hospital Access
          </span>
        </div>

        <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-3xl p-8 sm:p-10 border border-slate-100 dark:border-slate-800/80 shadow-2xl shadow-slate-200/50 dark:shadow-none flex flex-col gap-6 relative">
          {/* Title Header */}
          <div className="flex flex-col items-center text-center gap-1.5">
            <div className="h-14 w-14 rounded-2xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center mb-1 border border-teal-100 dark:border-teal-900/30 shadow-inner">
              <Hospital className="h-7 w-7" />
            </div>

            <h2 className="text-2xl sm:text-3xl font-black tracking-tight text-slate-900 dark:text-white">
              Hospital Portal
            </h2>

            <p className="text-xs text-slate-500 dark:text-slate-400 font-medium max-w-xs">
              Secure access for hospital staff & emergency medical officers.
            </p>
          </div>

          {/* Info Banner */}
          <div className="bg-teal-50/70 dark:bg-teal-950/40 text-teal-800 dark:text-teal-300 px-4 py-3 rounded-2xl text-xs border border-teal-100 dark:border-teal-900/40 font-medium flex items-center gap-2">
            <Activity className="h-4 w-4 shrink-0 text-teal-600 dark:text-teal-400" />
            <span>Manage emergency blood requests and donor responses in real-time.</span>
          </div>

          {/* Inline Error Alert */}
          {errorMsg && (
            <div className="flex items-start gap-3 bg-red-50 dark:bg-red-950/50 text-red-600 dark:text-red-400 p-4 rounded-2xl text-xs border border-red-100 dark:border-red-900/40 font-medium animate-in fade-in duration-200">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <LoginInput
              label="Hospital Email Address"
              type="email"
              placeholder="hospital@medical.org"
              icon={Mail}
              error={errors.email?.message}
              {...register('email', {
                required: 'Hospital email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Invalid email address format'
                }
              })}
            />

            <PasswordInput
              label="Password"
              placeholder="••••••••"
              focusTheme="teal"
              error={errors.password?.message}
              {...register('password', {
                required: 'Password is required',
                minLength: {
                  value: 6,
                  message: 'Password must be at least 6 characters'
                }
              })}
            />

            {/* Remember Me & Forgot Password */}
            <div className="flex items-center justify-between text-xs pt-1">
              <label className="flex items-center gap-2 cursor-pointer text-slate-600 dark:text-slate-400 select-none">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="rounded-md border-slate-300 dark:border-slate-700 text-teal-600 focus:ring-teal-500 h-4 w-4"
                />
                <span>Remember me</span>
              </label>

              <button
                type="button"
                onClick={() => setShowForgotModal(true)}
                className="text-teal-600 dark:text-teal-400 font-semibold hover:underline"
              >
                Forgot Password?
              </button>
            </div>

            <LoginButton
              theme="teal"
              isLoading={isLoading}
              isSuccess={isSuccess}
              className="mt-3"
            >
              Sign In to Hospital Portal
            </LoginButton>
          </form>

          {/* Footer Callout */}
          <div className="pt-4 border-t border-slate-100 dark:border-slate-800/80 text-center">
            <p className="text-xs text-slate-500 dark:text-slate-400">
              New hospital?{' '}
              <Link to="/register/hospital" className="text-teal-600 dark:text-teal-400 font-bold hover:underline">
                Register Hospital
              </Link>
            </p>
          </div>
        </div>
      </div>

      {/* Forgot Password Modal */}
      <ForgotPasswordModal
        isOpen={showForgotModal}
        onClose={() => setShowForgotModal(false)}
      />
    </div>
  );
}
