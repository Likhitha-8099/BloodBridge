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
  Heart, 
  ArrowLeft, 
  AlertCircle, 
  Mail, 
  Users, 
  Sparkles,
  ShieldCheck
} from 'lucide-react';

/**
 * Creative, Modern & Interactive Donor Login Page for BloodBridge.
 * Preserves 100% of existing authentication logic, state management, and API routes.
 */
export default function DonorLogin() {
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

  // Clear previous module authentication if navigating from Hospital or Admin
  useEffect(() => {
    if (isAuthenticated && role !== 'DONOR') {
      logout();
    }
  }, [isAuthenticated, role, logout]);

  if (isAuthenticated && role === 'DONOR') {
    return <Navigate to="/donor/dashboard" replace />;
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
      registerDevice().catch((err) => console.warn('[DonorLogin] FCM registration skipped:', err));

      setTimeout(() => {
        navigate(`/${userRole.toLowerCase()}/dashboard`);
      }, 500);
    } catch (err) {
      setErrorMsg(err.message || 'Invalid email or password.');
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col lg:flex-row font-sans selection:bg-red-500 selection:text-white relative overflow-hidden">
      {/* =================================================== */}
      {/* LEFT SIDE VISUAL HERO - Donor Life-Saving Theme */}
      {/* =================================================== */}
      <div className="w-full lg:w-1/2 bg-gradient-to-br from-red-600 via-rose-600 to-rose-900 text-white p-8 lg:p-14 flex flex-col justify-between relative overflow-hidden min-h-[380px] lg:min-h-screen">
        {/* Animated Background Blobs & Floating Shapes */}
        <motion.div
          animate={shouldReduceMotion ? {} : {
            scale: [1, 1.15, 1],
            opacity: [0.3, 0.5, 0.3],
            x: [0, 20, 0],
            y: [0, -20, 0]
          }}
          transition={{ duration: 12, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute -top-32 -left-32 w-96 h-96 bg-red-400/20 rounded-full blur-3xl pointer-events-none"
        />
        <motion.div
          animate={shouldReduceMotion ? {} : {
            scale: [1, 1.2, 1],
            opacity: [0.2, 0.45, 0.2],
            x: [0, -30, 0],
            y: [0, 30, 0]
          }}
          transition={{ duration: 14, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute -bottom-32 -right-32 w-[28rem] h-[28rem] bg-rose-500/20 rounded-full blur-3xl pointer-events-none"
        />

        {/* Floating Blood Drop Graphics */}
        {!shouldReduceMotion && (
          <>
            <motion.div
              animate={{ y: [0, -18, 0], rotate: [0, 5, 0] }}
              transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
              className="absolute top-1/4 right-12 w-12 h-12 rounded-full bg-white/10 backdrop-blur-md border border-white/20 flex items-center justify-center text-red-200 shadow-xl pointer-events-none hidden sm:flex"
            >
              🩸
            </motion.div>
            <motion.div
              animate={{ y: [0, 16, 0], rotate: [0, -5, 0] }}
              transition={{ duration: 7, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
              className="absolute bottom-1/3 left-10 w-14 h-14 rounded-2xl bg-white/10 backdrop-blur-md border border-white/20 flex items-center justify-center text-white shadow-xl pointer-events-none hidden sm:flex"
            >
              <Heart className="h-7 w-7 text-red-200 fill-red-200 animate-pulse" />
            </motion.div>
          </>
        )}

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
          <div className="inline-flex items-center gap-2 bg-white/15 backdrop-blur-md px-4 py-1.5 rounded-full text-xs font-semibold border border-white/20 mb-6 text-red-50">
            <ShieldCheck className="h-4 w-4 text-red-200" />
            <span>Donor Community Portal</span>
          </div>

          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black leading-tight tracking-tight mb-4">
            Save Lives. <br />
            <span className="text-red-200">Every Donation Matters.</span>
          </h1>

          <p className="text-sm sm:text-base text-red-100/90 leading-relaxed max-w-md mb-8">
            Your single contribution can bring hope and life to patients during critical emergency medical procedures.
          </p>

          {/* Three Life-Saving Visual Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
            <div className="bg-white/10 backdrop-blur-md p-3.5 rounded-2xl border border-white/15 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-red-500/30 flex items-center justify-center text-lg shrink-0">
                🩸
              </div>
              <div className="text-xs font-semibold text-white">Save Lives</div>
            </div>

            <div className="bg-white/10 backdrop-blur-md p-3.5 rounded-2xl border border-white/15 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-rose-500/30 flex items-center justify-center text-rose-200 shrink-0">
                <Heart className="h-5 w-5 fill-rose-200" />
              </div>
              <div className="text-xs font-semibold text-white">Every Donation Matters</div>
            </div>

            <div className="bg-white/10 backdrop-blur-md p-3.5 rounded-2xl border border-white/15 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-red-400/30 flex items-center justify-center text-red-100 shrink-0">
                <Users className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-white">Connect & Support</div>
            </div>
          </div>
        </div>

        {/* Hero Footer */}
        <div className="relative z-10 text-xs text-red-200/80 flex items-center justify-between pt-4 border-t border-white/15">
          <span>© 2026 BloodBridge Healthcare</span>
          <span className="flex items-center gap-1.5">
            <Sparkles className="h-3.5 w-3.5" /> Emergency Response Network
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
            className="text-xs font-semibold text-slate-500 dark:text-slate-400 hover:text-red-600 dark:hover:text-red-400 flex items-center gap-1.5 transition-colors group"
          >
            <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
            <span>Back to Home</span>
          </Link>

          <span className="text-[11px] font-bold text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/50 px-3 py-1 rounded-full border border-red-100 dark:border-red-900/40">
            Donor Portal
          </span>
        </div>

        <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-3xl p-8 sm:p-10 border border-slate-100 dark:border-slate-800/80 shadow-2xl shadow-slate-200/50 dark:shadow-none flex flex-col gap-6 relative">
          {/* Title Header */}
          <div className="flex flex-col items-center text-center gap-1.5">
            <div className="h-14 w-14 rounded-2xl bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 flex items-center justify-center mb-1 border border-red-100 dark:border-red-900/30 shadow-inner">
              <Heart className="h-7 w-7 fill-current" />
            </div>

            <h2 className="text-2xl sm:text-3xl font-black tracking-tight text-slate-900 dark:text-white">
              Welcome Back, Donor ❤️
            </h2>

            <p className="text-xs text-slate-500 dark:text-slate-400 font-medium max-w-xs">
              Your donation can save lives. Enter your registered email to sign in.
            </p>
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
              label="Email Address"
              type="email"
              placeholder="donor@example.com"
              icon={Mail}
              error={errors.email?.message}
              {...register('email', {
                required: 'Email address is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Invalid email address format'
                }
              })}
            />

            <PasswordInput
              label="Password"
              placeholder="••••••••"
              focusTheme="red"
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
                  className="rounded-md border-slate-300 dark:border-slate-700 text-red-600 focus:ring-red-500 h-4 w-4"
                />
                <span>Remember me</span>
              </label>

              <button
                type="button"
                onClick={() => setShowForgotModal(true)}
                className="text-red-600 dark:text-red-400 font-semibold hover:underline"
              >
                Forgot Password?
              </button>
            </div>

            <LoginButton
              theme="red"
              isLoading={isLoading}
              isSuccess={isSuccess}
              className="mt-3"
            >
              Sign In to Donor Portal
            </LoginButton>
          </form>

          {/* Footer Callout */}
          <div className="pt-4 border-t border-slate-100 dark:border-slate-800/80 text-center flex flex-col gap-2">
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Don't have an account?{' '}
              <Link to="/register/donor" className="text-red-600 dark:text-red-400 font-bold hover:underline">
                Register as Donor
              </Link>
            </p>
            <p className="text-[11px] text-slate-400 dark:text-slate-500 italic">
              "Your donation can save lives."
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
