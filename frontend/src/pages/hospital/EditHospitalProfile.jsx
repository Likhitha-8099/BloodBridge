import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Save, ArrowLeft, AlertCircle } from 'lucide-react';

/**
 * Screen enabling hospitals to configure profile details.
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
    <div className="flex flex-col gap-6 max-w-2xl mx-auto">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/hospital/profile')}
          className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded-xl shadow-sm transition-all"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-gray-900">
            {profile ? 'Edit Profile' : 'Create Hospital Profile'}
          </h1>
          <p className="text-xs text-gray-500 mt-0.5">
            Configure license details, addresses, and contact details of your hospital.
          </p>
        </div>
      </div>

      {errorMsg && (
        <div className="flex items-start gap-2 bg-red-50 text-red-600 p-3.5 rounded-xl text-xs border border-red-100 font-medium">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{errorMsg}</span>
        </div>
      )}

      <Card>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Hospital Name"
              type="text"
              placeholder="e.g. City General Hospital"
              error={errors.hospitalName?.message}
              {...register('hospitalName', { required: 'Hospital name is required' })}
            />

            <Input
              label="Registration Number"
              type="text"
              placeholder="e.g. REG-12345"
              error={errors.registrationNumber?.message}
              {...register('registrationNumber', { required: 'Registration number is required' })}
            />

            <Input
              label="Contact Email Address"
              type="email"
              placeholder="e.g. bloodbank@citygeneral.com"
              error={errors.email?.message}
              {...register('email', { required: 'Contact email is required' })}
            />

            <Input
              label="Contact Phone Number"
              type="text"
              placeholder="e.g. +16175551234"
              error={errors.phoneNumber?.message}
              {...register('phoneNumber', { required: 'Contact phone number is required' })}
            />

            <Input
              label="Street Address"
              type="text"
              placeholder="e.g. 100 Medical Way"
              error={errors.address?.message}
              {...register('address', { required: 'Street address is required' })}
            />

            <Input
              label="City"
              type="text"
              placeholder="e.g. Boston"
              error={errors.city?.message}
              {...register('city', { required: 'City is required' })}
            />

            <Input
              label="State"
              type="text"
              placeholder="e.g. MA"
              error={errors.state?.message}
              {...register('state', { required: 'State is required' })}
            />
          </div>

          <Button type="submit" variant="primary" isLoading={isSubmitting} className="w-full sm:w-fit self-end mt-4 px-6">
            <Save className="h-4 w-4 mr-2" /> Save Profile
          </Button>
        </form>
      </Card>
    </div>
  );
}
