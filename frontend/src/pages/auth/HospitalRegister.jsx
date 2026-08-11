import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link, Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import ToggleSwitch from '../../components/ui/ToggleSwitch';
import BloodBridgeLogo from '../../components/common/BloodBridgeLogo';
import { AlertCircle, CheckCircle2, ArrowLeft, ShieldCheck, UserPlus, Building, Phone, MapPin, Award } from 'lucide-react';

/**
 * Dedicated Hospital Registration Component.
 * Specifically designed for healthcare institution onboarding. Automatically sets role = 'HOSPITAL'.
 */
export default function HospitalRegister() {
  const { isAuthenticated, role } = useAuthStore();
  const navigate = useNavigate();
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [bloodBankAvailable, setBloodBankAvailable] = useState(true);

  const { 
    register, 
    handleSubmit, 
    formState: { errors } 
  } = useForm({
    defaultValues: {
      hospitalName: '',
      hospitalType: 'Private Multi-Speciality',
      registrationNumber: '',
      email: '',
      phoneNumber: '',
      emergencyPhoneNumber: '',
      address: '',
      country: 'India',
      state: '',
      district: '',
      city: '',
      postalCode: '',
      website: '',
      bloodBankLicenseNumber: '',
      storageCapacity: '',
      adminName: '',
      adminEmail: '',
      adminMobile: '',
      password: '',
      confirmPassword: '',
      termsAccepted: false
    }
  });

  if (isAuthenticated && role) {
    const dashboardPath = `/${role.toLowerCase()}/dashboard`;
    return <Navigate to={dashboardPath} replace />;
  }

  const onSubmit = async (data) => {
    setIsLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    if (data.password !== data.confirmPassword) {
      setErrorMsg('Passwords do not match. Please verify.');
      setIsLoading(false);
      return;
    }

    try {
      const registerPayload = {
        fullName: data.hospitalName,
        hospitalName: data.hospitalName,
        hospitalType: data.hospitalType,
        registrationNumber: data.registrationNumber,
        email: data.email,
        phoneNumber: data.phoneNumber,
        emergencyPhoneNumber: data.emergencyPhoneNumber,
        website: data.website,
        country: data.country,
        state: data.state,
        district: data.district,
        city: data.city,
        postalCode: data.postalCode,
        address: data.address,
        landmark: data.landmark,
        latitude: data.latitude ? parseFloat(data.latitude) : null,
        longitude: data.longitude ? parseFloat(data.longitude) : null,
        role: 'HOSPITAL',
        password: data.password
      };

      await authService.register(registerPayload);

      setSuccessMsg('Hospital Registration successful! Verification request submitted. Redirecting to Hospital Login...');
      setTimeout(() => {
        navigate('/login/hospital');
      }, 2000);
    } catch (err) {
      setErrorMsg(err.message || 'Hospital registration failed. Please check your details and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col items-center justify-center p-4 sm:p-6 py-12 font-sans">
      <div className="w-full max-w-3xl mb-4 flex items-center justify-between">
        <Link to="/auth/hospital" className="text-xs font-semibold text-gray-500 dark:text-slate-400 hover:text-teal-600 flex items-center gap-1.5 transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to Hospital Access Center
        </Link>
      </div>

      <Card className="w-full max-w-3xl p-6 sm:p-10 flex flex-col gap-6 shadow-2xl border-slate-100 dark:border-slate-800 bg-white dark:bg-slate-900 rounded-3xl">
        {/* Header */}
        <div className="flex flex-col items-center text-center gap-1">
          <Link to="/" className="mb-2 hover:opacity-90 transition-opacity">
            <BloodBridgeLogo size="lg" />
          </Link>
          <div className="inline-flex items-center gap-1.5 bg-teal-50 dark:bg-teal-950/40 text-teal-700 dark:text-teal-300 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider border border-teal-100 dark:border-teal-900/30">
            <ShieldCheck className="h-3.5 w-3.5" /> Institutional Enrollment
          </div>
          <h2 className="text-2xl font-black text-gray-900 dark:text-white">Hospital Registration</h2>
          <p className="text-xs text-gray-500 dark:text-slate-400 max-w-md">
            Register your medical center to post blood requests, manage blood bank inventory, and coordinate donor matches.
          </p>
        </div>

        {errorMsg && (
          <div className="flex items-start gap-2 bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-400 p-4 rounded-2xl text-xs border border-red-100 font-medium">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        {successMsg && (
          <div className="flex items-start gap-2 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 p-4 rounded-2xl text-xs border border-emerald-100 font-medium">
            <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{successMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-6">
          {/* SECTION 1: HOSPITAL IDENTITY */}
          <div className="space-y-4">
            <h3 className="font-bold text-xs text-teal-700 dark:text-teal-400 uppercase tracking-wider border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <Building className="h-4 w-4" /> 1. Hospital Identity & License
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Hospital Name"
                placeholder="e.g. Apollo Super Speciality Hospital"
                error={errors.hospitalName?.message}
                {...register('hospitalName', { required: 'Hospital name is required' })}
              />

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-gray-600 dark:text-slate-400">Hospital Type</label>
                <select className="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-slate-800 text-sm bg-white dark:bg-slate-900 text-gray-900 dark:text-white" {...register('hospitalType')}>
                  <option value="Government Hospital">Government Hospital</option>
                  <option value="Private Multi-Speciality">Private Multi-Speciality</option>
                  <option value="Trust / Charitable Hospital">Trust / Charitable Hospital</option>
                  <option value="Speciality Blood Bank Center">Speciality Blood Bank Center</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Input
                label="Registration / License Number"
                placeholder="REG-HOSP-2026-99"
                error={errors.registrationNumber?.message}
                {...register('registrationNumber', { required: 'Registration number is required' })}
              />
              <Input
                label="Official Hospital Email"
                type="email"
                placeholder="contact@apollohospital.com"
                error={errors.email?.message}
                {...register('email', { required: 'Hospital email is required' })}
              />
              <Input
                label="Hospital Phone Number"
                placeholder="+91 80 12345678"
                error={errors.phoneNumber?.message}
                {...register('phoneNumber', { required: 'Phone number is required' })}
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input label="Emergency Contact Line" placeholder="+91 9876543210" {...register('emergencyPhoneNumber')} />
              <Input label="Website URL (Optional)" placeholder="https://www.apollohospital.com" {...register('website')} />
            </div>
          </div>

          {/* SECTION 2: LOCATION & ADDRESS */}
          <div className="space-y-4">
            <h3 className="font-bold text-xs text-teal-700 dark:text-teal-400 uppercase tracking-wider border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <MapPin className="h-4 w-4" /> 2. Address & Geographical Location
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Input label="Country" placeholder="India" {...register('country')} />
              <Input label="State" placeholder="Karnataka" error={errors.state?.message} {...register('state', { required: 'State required' })} />
              <Input label="District" placeholder="Bengaluru Urban" {...register('district')} />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input label="City" placeholder="Bangalore" error={errors.city?.message} {...register('city', { required: 'City required' })} />
              <Input label="PIN Code" placeholder="560001" error={errors.postalCode?.message} {...register('postalCode', { required: 'PIN code required' })} />
            </div>

            <Input label="Full Hospital Premises Address" placeholder="Door / Building No, Main Road, Area" error={errors.address?.message} {...register('address', { required: 'Address required' })} />
          </div>

          {/* SECTION 3: BLOOD BANK CAPABILITIES */}
          <div className="space-y-4">
            <h3 className="font-bold text-xs text-teal-700 dark:text-teal-400 uppercase tracking-wider border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <Award className="h-4 w-4" /> 3. Blood Bank Facility & Storage
            </h3>

            <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 flex flex-col gap-4">
              <ToggleSwitch 
                label="Blood Bank Facility Available" 
                sublabel="Does this hospital operate an on-site licensed blood storage bank?" 
                checked={bloodBankAvailable} 
                onChange={setBloodBankAvailable} 
              />

              {bloodBankAvailable && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                  <Input label="Blood Bank License Number" placeholder="BB-LIC-55443" {...register('bloodBankLicenseNumber')} />
                  <Input label="Available Storage Capacity (Units)" type="number" placeholder="500" {...register('storageCapacity')} />
                </div>
              )}
            </div>
          </div>

          {/* SECTION 4: ADMINISTRATOR & CREDENTIALS */}
          <div className="space-y-4">
            <h3 className="font-bold text-xs text-teal-700 dark:text-teal-400 uppercase tracking-wider border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <Phone className="h-4 w-4" /> 4. Hospital Administrator & Login Passwords
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Input label="Administrator Full Name" placeholder="Dr. Sarah Jenkins" {...register('adminName')} />
              <Input label="Administrator Email" type="email" placeholder="sarah.j@apollohospital.com" {...register('adminEmail')} />
              <Input label="Administrator Mobile" placeholder="+91 9900000000" {...register('adminMobile')} />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Account Password"
                type="password"
                placeholder="••••••••"
                error={errors.password?.message}
                {...register('password', {
                  required: 'Password is required',
                  minLength: { value: 8, message: 'Min 8 characters' }
                })}
              />
              <Input
                label="Confirm Account Password"
                type="password"
                placeholder="••••••••"
                error={errors.confirmPassword?.message}
                {...register('confirmPassword', { required: 'Confirm password is required' })}
              />
            </div>
          </div>

          {/* SECTION 5: TERMS & SUBMIT */}
          <div className="space-y-4 pt-2">
            <label className="flex items-start gap-2.5 cursor-pointer font-medium text-xs text-gray-700 dark:text-slate-300">
              <input type="checkbox" className="h-4 w-4 rounded text-teal-600 focus:ring-teal-600 mt-0.5" {...register('termsAccepted', { required: true })} />
              <span>I confirm that this hospital is a legally recognized medical center and agree to the Blood Bridge Terms of Service.</span>
            </label>

            <Button type="submit" variant="primary" isLoading={isLoading} className="w-full bg-teal-600 hover:bg-teal-700 text-white font-bold py-3 text-sm rounded-2xl shadow-lg mt-2">
              <UserPlus className="h-4 w-4 mr-2" /> Complete Hospital Registration
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
