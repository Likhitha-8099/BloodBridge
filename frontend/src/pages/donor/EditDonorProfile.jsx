import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useDonorProfile } from '../../hooks/useDonorProfile';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Save, ArrowLeft, AlertCircle } from 'lucide-react';

/**
 * Page view allowing users to create or edit their donor profiles.
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
    formState: { errors } 
  } = useForm({
    defaultValues: {
      bloodGroup: '',
      age: '',
      gender: '',
      city: '',
      state: '',
      weight: '',
      medicalConditions: '',
      lastDonationDate: '',
      availableForDonation: true,
    }
  });

  // Prepopulate form values once loaded profile is available
  useEffect(() => {
    if (profile) {
      reset({
        bloodGroup: profile.bloodGroup || '',
        age: profile.age || '',
        gender: profile.gender || '',
        city: profile.city || '',
        state: profile.state || '',
        weight: profile.weight || '',
        medicalConditions: profile.medicalConditions || '',
        lastDonationDate: profile.lastDonationDate || '',
        availableForDonation: profile.availableForDonation !== undefined ? profile.availableForDonation : true,
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
    <div className="flex flex-col gap-6 max-w-2xl mx-auto">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/donor/profile')}
          className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded-xl shadow-sm transition-all"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-gray-900">
            {profile ? 'Edit Profile' : 'Create Donor Profile'}
          </h1>
          <p className="text-xs text-gray-500 mt-0.5">
            Provide medical details and location info for donor eligibility matching.
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
            {/* Blood Group Selection */}
            <div className="flex flex-col gap-1.5 w-full">
              <label className="text-xs font-semibold text-gray-600 tracking-wide">Blood Group</label>
              <select
                className={`w-full px-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-0 ${
                  errors.bloodGroup
                    ? 'border-red-400 focus:border-red-500 focus:ring-red-100 bg-red-50/10'
                    : 'border-gray-200 focus:border-primary focus:ring-red-100'
                } bg-white text-gray-900`}
                {...register('bloodGroup', { required: 'Blood Group selection is required' })}
              >
                <option value="">Select...</option>
                <option value="A_POSITIVE">A+ (A Positive)</option>
                <option value="A_NEGATIVE">A- (A Negative)</option>
                <option value="B_POSITIVE">B+ (B Positive)</option>
                <option value="B_NEGATIVE">B- (B Negative)</option>
                <option value="AB_POSITIVE">AB+ (AB Positive)</option>
                <option value="AB_NEGATIVE">AB- (AB Negative)</option>
                <option value="O_POSITIVE">O+ (O Positive)</option>
                <option value="O_NEGATIVE">O- (O Negative)</option>
              </select>
              {errors.bloodGroup && <span className="text-[11px] text-red-500 font-medium pl-1">{errors.bloodGroup.message}</span>}
            </div>

            {/* Age Validation */}
            <Input
              label="Age"
              type="number"
              placeholder="e.g. 28"
              error={errors.age?.message}
              {...register('age', {
                required: 'Age is required',
                min: { value: 18, message: 'Must be at least 18 years old' },
                max: { value: 65, message: 'Must be at most 65 years old' }
              })}
            />

            {/* Gender Selection */}
            <div className="flex flex-col gap-1.5 w-full">
              <label className="text-xs font-semibold text-gray-600 tracking-wide">Gender</label>
              <select
                className={`w-full px-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-0 ${
                  errors.gender
                    ? 'border-red-400 focus:border-red-500 focus:ring-red-100 bg-red-50/10'
                    : 'border-gray-200 focus:border-primary focus:ring-red-100'
                } bg-white text-gray-900`}
                {...register('gender', { required: 'Gender selection is required' })}
              >
                <option value="">Select...</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
              </select>
              {errors.gender && <span className="text-[11px] text-red-500 font-medium pl-1">{errors.gender.message}</span>}
            </div>

            {/* Weight Validation */}
            <Input
              label="Weight (kg)"
              type="number"
              step="0.1"
              placeholder="e.g. 68.5"
              error={errors.weight?.message}
              {...register('weight', {
                required: 'Weight is required',
                min: { value: 45, message: 'Weight must be at least 45 kg' }
              })}
            />

            {/* Location Fields */}
            <Input
              label="City"
              type="text"
              placeholder="e.g. Los Angeles"
              error={errors.city?.message}
              {...register('city', { required: 'City name is required' })}
            />

            <Input
              label="State"
              type="text"
              placeholder="e.g. CA"
              error={errors.state?.message}
              {...register('state', { required: 'State code/name is required' })}
            />

            {/* Last Donation Date Selector */}
            <Input
              label="Last Donation Date (Optional)"
              type="date"
              error={errors.lastDonationDate?.message}
              {...register('lastDonationDate')}
            />

            {/* Availability Checkbox */}
            <div className="flex flex-col gap-1.5 w-full justify-center">
              <span className="text-xs font-semibold text-gray-600 tracking-wide">Availability Status</span>
              <div className="flex items-center gap-2 py-2">
                <input
                  type="checkbox"
                  id="availableForDonation"
                  className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                  {...register('availableForDonation')}
                />
                <label htmlFor="availableForDonation" className="text-sm font-semibold text-gray-700 select-none">
                  Available immediately for donation matches
                </label>
              </div>
            </div>
          </div>

          {/* Medical Conditions Textarea */}
          <div className="flex flex-col gap-1.5 w-full">
            <label className="text-xs font-semibold text-gray-600 tracking-wide">
              Medical Conditions / Allergies (Optional)
            </label>
            <textarea
              rows={3}
              placeholder="List any medical situations, diseases, or allergy histories. Leave blank if none."
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 text-sm transition-all focus:outline-none focus:border-primary focus:ring-2 focus:ring-red-100 bg-white text-gray-900 placeholder-gray-400"
              {...register('medicalConditions')}
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
