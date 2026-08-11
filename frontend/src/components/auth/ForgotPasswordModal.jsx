import React, { useState } from 'react';
import { KeyRound, X, CheckCircle2, ArrowRight } from 'lucide-react';
import LoginInput from './LoginInput';

/**
 * Reusable modal dialog for password recovery requests.
 */
export default function ForgotPasswordModal({ isOpen, onClose }) {
  const [resetEmail, setResetEmail] = useState('');
  const [isSent, setIsSent] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [emailError, setEmailError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!resetEmail || !resetEmail.includes('@')) {
      setEmailError('Please enter a valid email address');
      return;
    }

    setEmailError('');
    setIsSubmitting(true);

    // Simulate link delivery feedback
    setTimeout(() => {
      setIsSubmitting(false);
      setIsSent(true);
      setTimeout(() => {
        setIsSent(false);
        setResetEmail('');
        onClose();
      }, 2800);
    }, 800);
  };

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4 transition-all">
      <div 
        className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 max-w-md w-full border border-slate-100 dark:border-slate-800 shadow-2xl flex flex-col gap-5 relative animate-in fade-in zoom-in duration-200"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          aria-label="Close modal"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="flex items-center gap-3">
          <div className="h-12 w-12 rounded-2xl bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 flex items-center justify-center border border-red-100 dark:border-red-900/30">
            <KeyRound className="h-6 w-6" />
          </div>
          <div>
            <h3 id="modal-title" className="font-bold text-lg text-slate-900 dark:text-white">
              Reset Password
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Recover access to your BloodBridge account
            </p>
          </div>
        </div>

        {isSent ? (
          <div className="bg-emerald-50 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-300 p-4 rounded-2xl border border-emerald-200 dark:border-emerald-900/40 flex items-start gap-3">
            <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0 mt-0.5" />
            <div className="text-xs leading-relaxed">
              Password recovery link sent to <strong>{resetEmail}</strong>! Check your inbox shortly.
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              Enter your registered account email. We will send you a secure link to reset your password.
            </p>

            <LoginInput
              label="Account Email"
              type="email"
              placeholder="name@example.com"
              value={resetEmail}
              onChange={(e) => {
                setResetEmail(e.target.value);
                if (emailError) setEmailError('');
              }}
              error={emailError}
              required
            />

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 bg-red-600 hover:bg-red-700 text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
            >
              {isSubmitting ? (
                <span>Sending link...</span>
              ) : (
                <>
                  <span>Send Reset Link</span>
                  <ArrowRight className="h-4 w-4" />
                </>
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
