import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link, Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import { Heart, UserPlus, AlertCircle, CheckCircle, ArrowLeft } from 'lucide-react';

/**
 * Registration screen enabling signups for Donors, Patients, and Hospitals.
 */
export default function Register() {
  const { isAuthenticated, role } = useAuthStore();
  const navigate = useNavigate();
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { 
    register, 
    handleSubmit, 
    formState: { errors } 
  } = useForm({
    defaultValues: {
      fullName: '',
      email: '',
      phoneNumber: '',
      role: '',
      password: ''
    }
  });

  if (isAuthenticated) {
    return <Navigate to={`/${role.toLowerCase()}/dashboard`} replace />;
  }

  const onSubmit = async (data) => {
    setIsLoading(true);
    setErrorMsg('');
    setSuccessMsg('');
    try {
      const registerPayload = {
        fullName: data.fullName,
        email: data.email,
        phoneNumber: data.phoneNumber,
        role: data.role ? data.role.toUpperCase() : '',
        password: data.password
      };
      console.log('registerPayload', registerPayload);
      await authService.register(registerPayload);
      setSuccessMsg('Registration completed successfully! Redirecting to login page...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setErrorMsg(err.message || 'Registration failed. Please check details and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-6 py-12">
      <div className="w-full max-w-lg mb-4">
        <Link to="/" className="text-sm font-medium text-gray-500 hover:text-primary flex items-center gap-1 w-fit transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to Home
        </Link>
      </div>
      <Card className="w-full max-w-lg p-8 flex flex-col gap-6">
        <div className="flex flex-col items-center gap-1 text-center">
          <Link to="/" className="flex items-center gap-2">
            <Heart className="h-7 w-7 text-primary fill-primary animate-pulse" />
            <span className="font-extrabold text-xl tracking-tight text-gray-900">
              Blood<span className="text-primary">Bridge</span>
            </span>
          </Link>
          <h2 className="text-lg font-bold text-gray-800 mt-4">Create your account</h2>
          <p className="text-xs text-gray-400">Join our donor network and save lives</p>
        </div>

        {errorMsg && (
          <div className="flex items-start gap-2 bg-red-50 text-red-600 p-3.5 rounded-xl text-xs border border-red-100 font-medium">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        {successMsg && (
          <div className="flex items-start gap-2 bg-green-50 text-green-600 p-3.5 rounded-xl text-xs border border-green-100 font-medium">
            <CheckCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{successMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          <Input
            label="Full Name"
            type="text"
            placeholder="John Doe"
            error={errors.fullName?.message}
            {...register('fullName', {
              required: 'Full name is required',
              minLength: {
                value: 3,
                message: 'Name must be at least 3 characters'
              }
            })}
          />

          <Input
            label="Email Address"
            type="email"
            placeholder="name@example.com"
            error={errors.email?.message}
            {...register('email', {
              required: 'Email address is required',
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                message: 'Invalid email address format'
              }
            })}
          />

          <Input
            label="Phone Number"
            type="text"
            placeholder="e.g. +1234567890"
            error={errors.phoneNumber?.message}
            {...register('phoneNumber', {
              required: 'Phone number is required'
            })}
          />

          <div className="flex flex-col gap-1.5 w-full">
            <label className="text-xs font-semibold text-gray-600 tracking-wide">Register As</label>
            <select
              className={`w-full px-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-0 ${
                errors.role
                  ? 'border-red-400 focus:border-red-500 focus:ring-red-100 bg-red-50/10'
                  : 'border-gray-200 focus:border-primary focus:ring-red-100'
              } bg-white text-gray-900`}
              {...register('role', { required: 'Please select a profile role' })}
            >
              <option value="">Select Role...</option>
              <option value="DONOR">Donor (Donate Blood)</option>
              <option value="PATIENT">Patient (Request Blood)</option>
              <option value="HOSPITAL">Hospital Representative</option>
            </select>
            {errors.role && <span className="text-[11px] text-red-500 font-medium pl-1">{errors.role.message}</span>}
          </div>

          <Input
            label="Password"
            type="password"
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password', {
              required: 'Password is required',
              minLength: {
                value: 8,
                message: 'Password must be at least 8 characters'
              },
              pattern: {
                value: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/,
                message: 'Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)'
              }
            })}
          />

          <Button type="submit" variant="primary" isLoading={isLoading} className="w-full mt-2">
            <UserPlus className="h-4 w-4 mr-2" /> Sign Up
          </Button>
        </form>

        <p className="text-xs text-center text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="text-primary font-bold hover:underline">
            Sign In
          </Link>
        </p>
      </Card>
    </div>
  );
}
