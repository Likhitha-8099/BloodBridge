import React, { useEffect, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import ToggleSwitch from '../../components/ui/ToggleSwitch';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Save, ArrowLeft, AlertCircle, Stethoscope, MapPin, Bell, User } from 'lucide-react';

/**
 * Edit Donor Profile Screen for viewing and updating healthcare attributes.
 */
export default function EditDonorProfile() {
  const navigate = useNavigate();
  const { profile, isLoading, createProfile, updateProfile } = useDonorProfile();
  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { 
    register, 
    handleSubmit, 
    reset, 
    control,
    watch,
    formState: { errors, isValid } 
  } = useForm({
    mode: 'onChange',
    defaultValues: {
      fullName: '',
      email: '',
      phoneNumber: '',
      bloodGroup: 'O_POSITIVE',
      age: 25,
      gender: 'MALE',
      dateOfBirth: '',
      city: '',
      state: '',
      country: 'India',
      district: '',
      postalCode: '',
      address: '',
      weight: 65,
      height: 175,
      hemoglobin: 14.0,
      bloodPressure: '120/80',
      pulseRate: 72,
      medicalConditions: '',
      currentMedications: '',
      allergies: '',
      lastDonationDate: '',
      emergencyAvailable: true,
      availableForDonation: true,
      smoking: false,
      alcohol: false,
      drugUsage: false,
      recentSurgery: false,
      recentTattoo: false,
      recentVaccination: false,
      willingDonatePlatelets: true,
      willingDonatePlasma: true,
      rareBloodDonor: false,
      pushNotificationEnabled: true,
      preferredDonationRadius: 25
    }
  });

  // Prepopulate form values once loaded profile is available
  useEffect(() => {
    if (profile) {
      reset({
        fullName: profile.fullName || '',
        email: profile.email || '',
        phoneNumber: profile.phoneNumber || '',
        bloodGroup: profile.bloodGroup || 'O_POSITIVE',
        age: profile.age || 25,
        gender: profile.gender || 'MALE',
        dateOfBirth: profile.dateOfBirth || '',
        city: profile.city || '',
        state: profile.state || '',
        country: profile.country || 'India',
        district: profile.district || '',
        postalCode: profile.postalCode || '',
        address: profile.address || '',
        weight: profile.weight || 65,
        height: profile.height || 175,
        hemoglobin: profile.hemoglobin || 14.0,
        bloodPressure: profile.bloodPressure || '120/80',
        pulseRate: profile.pulseRate || 72,
        medicalConditions: profile.medicalConditions || '',
        currentMedications: profile.currentMedications || '',
        allergies: profile.allergies || '',
        lastDonationDate: profile.lastDonationDate || '',
        emergencyAvailable: profile.emergencyAvailable ?? true,
        availableForDonation: profile.availableForDonation ?? true,
        smoking: profile.smoking ?? false,
        alcohol: profile.alcohol ?? false,
        drugUsage: profile.drugUsage ?? false,
        recentSurgery: profile.recentSurgery ?? false,
        recentTattoo: profile.recentTattoo ?? false,
        recentVaccination: profile.recentVaccination ?? false,
        willingDonatePlatelets: profile.willingDonatePlatelets ?? true,
        willingDonatePlasma: profile.willingDonatePlasma ?? true,
        rareBloodDonor: profile.rareBloodDonor ?? false,
        pushNotificationEnabled: profile.pushNotificationEnabled ?? true,
        preferredDonationRadius: profile.preferredDonationRadius || 25
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
      const payload = {
        ...data,
        age: parseInt(data.age, 10),
        weight: parseFloat(data.weight),
        height: data.height ? parseFloat(data.height) : null,
        hemoglobin: data.hemoglobin ? parseFloat(data.hemoglobin) : null,
        pulseRate: data.pulseRate ? parseInt(data.pulseRate, 10) : null,
        lastDonationDate: data.lastDonationDate ? data.lastDonationDate : null,
      };

      if (profile) {
        await updateProfile(payload);
      } else {
        await createProfile(payload);
      }
      navigate('/donor/profile');
    } catch (err) {
      setErrorMsg(err.message || 'Failed to save donor profile details.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col gap-6 max-w-3xl mx-auto font-sans">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/donor/profile')}
          className="p-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 text-slate-500 rounded-xl shadow-xs transition-all"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-gray-900 dark:text-white">
            {profile ? 'Edit Donor Health Profile' : 'Create Donor Profile'}
          </h1>
          <p className="text-xs text-gray-500 dark:text-slate-400 mt-0.5">
            Update your medical parameters, location, and donation preferences.
          </p>
        </div>
      </div>

      {errorMsg && (
        <div className="flex items-start gap-2 bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-400 p-4 rounded-2xl text-xs border border-red-100 font-medium">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{errorMsg}</span>
        </div>
      )}

      <Card className="p-6 sm:p-8">
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-6">
          {/* SECTION 0: Identity & Contact Info */}
          <div className="space-y-4">
            <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <User className="h-4 w-4 text-primary" /> Identity & Contact Information
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Input 
                label="Full Name *" 
                placeholder="John Doe" 
                error={errors.fullName?.message} 
                {...register('fullName', { required: 'Full Name is required' })} 
              />
              <Input 
                label="Email Address *" 
                type="email" 
                placeholder="donor@example.com" 
                error={errors.email?.message} 
                {...register('email', { 
                  required: 'Email is required', 
                  pattern: { value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i, message: 'Please enter a valid email' } 
                })} 
              />
              <Input 
                label="Phone Number" 
                placeholder="+91 9876543210" 
                {...register('phoneNumber')} 
              />
            </div>
          </div>

          {/* SECTION 1: Vitals & Blood Group */}
          <div className="space-y-4">
            <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <Stethoscope className="h-4 w-4 text-primary" /> Medical Parameters & Vitals
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-gray-600 dark:text-slate-400">Blood Group</label>
                <select className="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-slate-800 text-sm bg-white dark:bg-slate-900 text-gray-900 dark:text-white font-bold" {...register('bloodGroup')}>
                  <option value="O_POSITIVE">O+</option>
                  <option value="O_NEGATIVE">O-</option>
                  <option value="A_POSITIVE">A+</option>
                  <option value="A_NEGATIVE">A-</option>
                  <option value="B_POSITIVE">B+</option>
                  <option value="B_NEGATIVE">B-</option>
                  <option value="AB_POSITIVE">AB+</option>
                  <option value="AB_NEGATIVE">AB-</option>
                </select>
              </div>

              <Input label="Age" type="number" error={errors.age?.message} {...register('age', { required: 'Age is required' })} />
              <Input label="Height (cm)" type="number" {...register('height')} />
              <Input label="Weight (kg)" type="number" step="0.1" error={errors.weight?.message} {...register('weight', { required: 'Weight is required' })} />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Input label="Hemoglobin (g/dL)" type="number" step="0.1" {...register('hemoglobin')} />
              <Input label="Blood Pressure (mmHg)" placeholder="120/80" {...register('bloodPressure')} />
              <Input label="Pulse Rate (bpm)" type="number" {...register('pulseRate')} />
            </div>
          </div>

          {/* SECTION 2: Address & Location */}
          <div className="space-y-4">
            <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <MapPin className="h-4 w-4 text-primary" /> Location & Residence
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Input label="City" error={errors.city?.message} {...register('city', { required: 'City required' })} />
              <Input label="State" error={errors.state?.message} {...register('state', { required: 'State required' })} />
              <Input label="PIN Code" {...register('postalCode')} />
            </div>
            <Input label="Full Address" {...register('address')} />
          </div>

          {/* SECTION 3: Fixed Toggle Switches */}
          <div className="space-y-4">
            <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2 flex items-center gap-2">
              <Bell className="h-4 w-4 text-primary" /> Lifestyle & Preference Toggles
            </h3>

            <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2">
              <Controller control={control} name="availableForDonation" render={({ field }) => (
                <ToggleSwitch label="General Availability" sublabel="Available for donation requests" checked={field.value} onChange={field.onChange} />
              )} />

              <Controller control={control} name="emergencyAvailable" render={({ field }) => (
                <ToggleSwitch label="Emergency Availability" sublabel="Urgent SMS/Push call alerts" checked={field.value} onChange={field.onChange} />
              )} />

              <Controller control={control} name="smoking" render={({ field }) => (
                <ToggleSwitch label="Smoking Habits" sublabel="Tobacco smoker" checked={field.value} onChange={field.onChange} />
              )} />

              <Controller control={control} name="alcohol" render={({ field }) => (
                <ToggleSwitch label="Alcohol Intake" sublabel="Regular alcohol intake" checked={field.value} onChange={field.onChange} />
              )} />

              <Controller control={control} name="willingDonatePlatelets" render={({ field }) => (
                <ToggleSwitch label="Donate Platelets" sublabel="Platelet apheresis" checked={field.value} onChange={field.onChange} />
              )} />

              <Controller control={control} name="willingDonatePlasma" render={({ field }) => (
                <ToggleSwitch label="Donate Plasma" sublabel="Plasma donation" checked={field.value} onChange={field.onChange} />
              )} />

              <Controller control={control} name="rareBloodDonor" render={({ field }) => (
                <ToggleSwitch label="Rare Blood Type Registry" sublabel="Listed on rare blood roster" checked={field.value} onChange={field.onChange} />
              )} />

              <Controller control={control} name="pushNotificationEnabled" render={({ field }) => (
                <ToggleSwitch label="Push Notifications" sublabel="Receive alerts on device" checked={field.value} onChange={field.onChange} />
              )} />
            </div>
          </div>

          <Button 
            type="submit" 
            variant="primary" 
            isLoading={isSubmitting} 
            disabled={!isValid || Boolean(errors.email) || !watch('email')}
            className="w-full sm:w-fit self-end px-8 font-bold"
          >
            <Save className="h-4 w-4 mr-2" /> Save Profile Parameters
          </Button>
        </form>
      </Card>
    </div>
  );
}
