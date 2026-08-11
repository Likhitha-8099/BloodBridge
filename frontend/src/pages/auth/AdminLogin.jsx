import React, { useState } from 'react';
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
  Lock, 
  ArrowLeft, 
  AlertCircle, 
  Mail, 
  BarChart3, 
  CheckSquare, 
  Users, 
  Bell, 
  FileText,
  KeyRound,
  Cpu
} from 'lucide-react';

/**
 * Premium Control-Center Oriented Admin Login Page for BloodBridge.
 * Preserves 100% of existing authentication logic, state management, and API routes.
 */
export default function AdminLogin() {
  const { login, isAuthenticated, role } = useAuthStore();
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

  if (isAuthenticated && role) {
    const dashboardPath = `/${role.toLowerCase()}/dashboard`;
    return <Navigate to={dashboardPath} replace />;
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
      registerDevice().catch((err) => console.warn('[AdminLogin] FCM registration skipped:', err));

      setTimeout(() => {
        navigate(`/${userRole.toLowerCase()}/dashboard`);
      }, 500);
    } catch (err) {
      setErrorMsg(err.message || 'Invalid email or password.');
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col lg:flex-row font-sans selection:bg-indigo-500 selection:text-white relative overflow-hidden">
      {/* =================================================== */}
      {/* LEFT SIDE VISUAL HERO - Control Center Theme */}
      {/* =================================================== */}
      <div className="w-full lg:w-1/2 bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 text-white p-8 lg:p-14 flex flex-col justify-between relative overflow-hidden min-h-[380px] lg:min-h-screen border-r border-slate-800">
        {/* Futuristic Telemetry Grid & Ambient Glows */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b15_1px,transparent_1px),linear-gradient(to_bottom,#1e293b15_1px,transparent_1px)] bg-[size:4rem_4rem] pointer-events-none" />

        <motion.div
          animate={shouldReduceMotion ? {} : {
            scale: [1, 1.25, 1],
            opacity: [0.2, 0.4, 0.2]
          }}
          transition={{ duration: 10, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute -top-32 -left-32 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none"
        />
        <motion.div
          animate={shouldReduceMotion ? {} : {
            scale: [1, 1.15, 1],
            opacity: [0.15, 0.35, 0.15]
          }}
          transition={{ duration: 12, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
          className="absolute -bottom-32 -right-32 w-[28rem] h-[28rem] bg-blue-600/20 rounded-full blur-3xl pointer-events-none"
        />

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
          <div className="inline-flex items-center gap-2 bg-indigo-500/15 backdrop-blur-md px-4 py-1.5 rounded-full text-xs font-semibold border border-indigo-500/30 mb-6 text-indigo-300">
            <Cpu className="h-4 w-4 text-indigo-400" />
            <span>BloodBridge Control Center</span>
          </div>

          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black leading-tight tracking-tight mb-4">
            Platform Security & <br />
            <span className="text-indigo-400">Control Center.</span>
          </h1>

          <p className="text-sm sm:text-base text-slate-400 leading-relaxed max-w-md mb-8">
            Administrative governance hub for system metrics, hospital verification, user management, and compliance auditing.
          </p>

          {/* Control Center Features Grid */}
          <div className="grid grid-cols-2 gap-3 pt-2 max-w-md">
            <div className="bg-slate-900/80 backdrop-blur-md p-3.5 rounded-2xl border border-slate-800 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-indigo-500/20 flex items-center justify-center text-indigo-400 shrink-0">
                <BarChart3 className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">📊 Analytics</div>
            </div>

            <div className="bg-slate-900/80 backdrop-blur-md p-3.5 rounded-2xl border border-slate-800 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
                <CheckSquare className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">🏥 Hospital Verification</div>
            </div>

            <div className="bg-slate-900/80 backdrop-blur-md p-3.5 rounded-2xl border border-slate-800 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-blue-500/20 flex items-center justify-center text-blue-400 shrink-0">
                <Users className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">👥 User Management</div>
            </div>

            <div className="bg-slate-900/80 backdrop-blur-md p-3.5 rounded-2xl border border-slate-800 flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-amber-500/20 flex items-center justify-center text-amber-400 shrink-0">
                <Bell className="h-5 w-5" />
              </div>
              <div className="text-xs font-semibold text-slate-200">🔔 Real-time Alerts</div>
            </div>
          </div>
        </div>

        {/* Hero Footer */}
        <div className="relative z-10 text-xs text-slate-500 flex items-center justify-between pt-4 border-t border-slate-800/80">
          <span>© 2026 BloodBridge Administrative Suite</span>
          <span className="flex items-center gap-1.5 text-indigo-400">
            <FileText className="h-3.5 w-3.5" /> Audit Logs & Governance
          </span>
        </div>
      </div>

      {/* =================================================== */}
      {/* RIGHT SIDE - Modern Interactive Login Form */}
      {/* =================================================== */}
      <div className="w-full lg:w-1/2 flex flex-col items-center justify-center p-6 sm:p-12 lg:p-16 relative bg-slate-950 text-white">
        <div className="w-full max-w-md mb-6 flex items-center justify-between">
          <Link
            to="/"
            className="text-xs font-semibold text-slate-400 hover:text-indigo-400 flex items-center gap-1.5 transition-colors group"
          >
            <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
            <span>Back to Home</span>
          </Link>

          <span className="text-[11px] font-bold text-indigo-300 bg-indigo-950/60 px-3 py-1 rounded-full border border-indigo-900/50">
            Admin Access
          </span>
        </div>

        <div className="w-full max-w-md bg-slate-900 rounded-3xl p-8 sm:p-10 border border-slate-800/80 shadow-2xl flex flex-col gap-6 relative">
          {/* Title Header */}
          <div className="flex flex-col items-center text-center gap-1.5">
            <div className="h-14 w-14 rounded-2xl bg-indigo-950/80 text-indigo-400 flex items-center justify-center mb-1 border border-indigo-900/40 shadow-inner">
              <Lock className="h-7 w-7" />
            </div>

            <h2 className="text-2xl sm:text-3xl font-black tracking-tight text-white">
              BloodBridge Admin Portal
            </h2>

            <p className="text-xs text-slate-400 font-medium max-w-xs">
              Secure administrative access for system controllers.
            </p>
          </div>

          {/* Inline Error Alert */}
          {errorMsg && (
            <div className="flex items-start gap-3 bg-red-950/60 text-red-400 p-4 rounded-2xl text-xs border border-red-900/50 font-medium animate-in fade-in duration-200">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <LoginInput
              label="Admin Email Address"
              type="email"
              placeholder="admin@bloodbridge.com"
              icon={Mail}
              error={errors.email?.message}
              {...register('email', {
                required: 'Admin email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Invalid email address format'
                }
              })}
            />

            <PasswordInput
              label="Password"
              placeholder="••••••••"
              focusTheme="slate"
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
              <label className="flex items-center gap-2 cursor-pointer text-slate-400 select-none">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="rounded-md border-slate-700 bg-slate-800 text-indigo-600 focus:ring-indigo-500 h-4 w-4"
                />
                <span>Remember me</span>
              </label>

              <button
                type="button"
                onClick={() => setShowForgotModal(true)}
                className="text-indigo-400 font-semibold hover:underline"
              >
                Forgot Password?
              </button>
            </div>

            <LoginButton
              theme="indigo"
              isLoading={isLoading}
              isSuccess={isSuccess}
              className="mt-3"
            >
              Sign In to Admin Portal
            </LoginButton>
          </form>

          {/* Footer Callout */}
          <div className="pt-4 border-t border-slate-800 text-center">
            <p className="text-[11px] text-slate-500 flex items-center justify-center gap-1">
              <KeyRound className="h-3 w-3" /> Encrypted Administrative Gateway
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
