import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link, Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import { Heart, LogIn, AlertCircle, ArrowLeft } from 'lucide-react';

/**
 * Platform login screen verifying user access.
 */
export default function Login() {
  const { login, isAuthenticated, role } = useAuthStore();
  const navigate = useNavigate();
  const [errorMsg, setErrorMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);

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

  if (isAuthenticated) {
    const dashboardPath = `/${role.toLowerCase()}/dashboard`;
    return <Navigate to={dashboardPath} replace />;
  }

  const onSubmit = async (data) => {
    setIsLoading(true);
    setErrorMsg('');
    try {
      const response = await authService.login(data);
      login(response.token, response.role, response.user);
      navigate(`/${response.role.toLowerCase()}/dashboard`);
    } catch (err) {
      setErrorMsg(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-6">
      <div className="w-full max-w-md mb-4">
        <Link to="/" className="text-sm font-medium text-gray-500 hover:text-primary flex items-center gap-1 w-fit transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to Home
        </Link>
      </div>
      <Card className="w-full max-w-md p-8 flex flex-col gap-6">
        <div className="flex flex-col items-center gap-1 text-center">
          <Link to="/" className="flex items-center gap-2">
            <Heart className="h-7 w-7 text-primary fill-primary animate-pulse" />
            <span className="font-extrabold text-xl tracking-tight text-gray-900">
              Blood<span className="text-primary">Bridge</span>
            </span>
          </Link>
          <h2 className="text-lg font-bold text-gray-800 mt-4">Welcome back</h2>
          <p className="text-xs text-gray-400">Enter your credentials to access your account</p>
        </div>

        {errorMsg && (
          <div className="flex items-start gap-2 bg-red-50 text-red-600 p-3.5 rounded-xl text-xs border border-red-100 font-medium">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
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
            label="Password"
            type="password"
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password', {
              required: 'Password is required',
              minLength: {
                value: 6,
                message: 'Password must be at least 6 characters'
              }
            })}
          />

          <Button type="submit" variant="primary" isLoading={isLoading} className="w-full mt-2">
            <LogIn className="h-4 w-4 mr-2" /> Sign In
          </Button>
        </form>

        <p className="text-xs text-center text-gray-500">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary font-bold hover:underline">
            Sign Up
          </Link>
        </p>
      </Card>
    </div>
  );
}
