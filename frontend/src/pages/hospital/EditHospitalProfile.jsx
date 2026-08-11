import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import LoginInput from '../../components/auth/LoginInput';
import { Save, ArrowLeft, AlertCircle, Building } from 'lucide-react';

/**
 * Edit Hospital Profile Screen for Hospital Module.
 * Modern healthcare portal design preserving 100% of existing profile hooks and form validation.
 */
export default function EditHospitalProfile() {
  const navigate = useNavigate();
  const { profile, isLoading, createProfile, updateProfile } = useHospitalProfile();
  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { 
    register, 
    handleSubmit, 
    reset, 
    formState: { errors } 
  } = useForm({
    defaultValues: {
      hospitalName: '',
      registrationNumber: '',
      email: '',
      phoneNumber: '',
      address: '',
      city: '',
      state: '',
    }
  });

  useEffect(() => {
    if (profile) {
      reset({
        hospitalName: profile.hospitalName || '',
        registrationNumber: profile.registrationNumber || '',
        email: profile.email || '',
        phoneNumber: profile.phoneNumber || '',
        address: profile.address || '',
        city: profile.city || '',
        state: profile.state || '',
      });
    }
  }, [profile, reset]);

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  const onSubmit = async (data) => {
    setIsSubmitting(true);
    setErrorMsg('');
    try {
      if (profile) {
        await updateProfile(data);
      } else {
        await createProfile(data);
      }
      navigate('/hospital/profile');
    } catch (err) {
      setErrorMsg(err.message || 'Failed to save hospital profile.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans max-w-3xl mx-auto">
      <HospitalPageHeader
        title={profile ? 'Edit Hospital Profile' : 'Configure Hospital Profile'}
        subtitle="Configure institution license details, contact numbers, and physical facility address."
        icon={Building}
        badge="Settings"
        breadcrumbs={[
          { label: 'Hospital Profile', to: '/hospital/profile' },
          { label: 'Edit Profile' }
        ]}
        action={
          <button
            onClick={() => navigate('/hospital/profile')}
            className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Cancel</span>
          </button>
        }
      />

      {errorMsg && (
        <div className="flex items-start gap-3 bg-red-50 dark:bg-red-950/50 text-red-600 dark:text-red-400 p-4 rounded-2xl text-xs border border-red-100 dark:border-red-900/40 font-medium">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{errorMsg}</span>
        </div>
      )}

      <HospitalCard bodyClassName="p-6 sm:p-8">
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <LoginInput
              label="Hospital Name"
              type="text"
              placeholder="e.g. City Multi-Speciality Hospital"
              error={errors.hospitalName?.message}
              {...register('hospitalName', { required: 'Hospital name is required' })}
            />

            <LoginInput
              label="Registration / License Number"
              type="text"
              placeholder="e.g. HOSP-REG-98765"
              error={errors.registrationNumber?.message}
              {...register('registrationNumber', { required: 'Registration number is required' })}
            />

            <LoginInput
              label="Official Contact Email"
              type="email"
              placeholder="hospital@medical.org"
              error={errors.email?.message}
              {...register('email', { required: 'Contact email is required' })}
            />

            <LoginInput
              label="Emergency Phone Number"
              type="text"
              placeholder="e.g. +91 9876543210"
              error={errors.phoneNumber?.message}
              {...register('phoneNumber', { required: 'Contact phone number is required' })}
            />

            <div className="sm:col-span-2">
              <LoginInput
                label="Street Address & Area"
                type="text"
                placeholder="e.g. 100 Healthcare Boulevard, Suite 4"
                error={errors.address?.message}
                {...register('address', { required: 'Street address is required' })}
              />
            </div>

            <LoginInput
              label="City"
              type="text"
              placeholder="e.g. Hyderabad"
              error={errors.city?.message}
              {...register('city', { required: 'City is required' })}
            />

            <LoginInput
              label="State"
              type="text"
              placeholder="e.g. Telangana"
              error={errors.state?.message}
              {...register('state', { required: 'State is required' })}
            />
          </div>

          <div className="flex items-center justify-end pt-4 border-t border-slate-100 dark:border-slate-800">
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-6 py-3 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-bold text-xs rounded-2xl shadow-lg shadow-teal-500/20 transition-all flex items-center gap-2"
            >
              {isSubmitting ? (
                <span>Saving Profile...</span>
              ) : (
                <>
                  <Save className="h-4 w-4" />
                  <span>Save Hospital Profile</span>
                </>
              )}
            </button>
          </div>
        </form>
      </HospitalCard>
    </div>
  );
}
