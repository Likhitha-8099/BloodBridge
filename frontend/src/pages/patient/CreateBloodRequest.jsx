import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { usePatientProfile } from '../../hooks/usePatientProfile';
import { useHospitalProfile } from '../../hooks/useHospitalProfile';
import { useAuthStore } from '../../store/authStore';
import { bloodRequestService } from '../../services/bloodRequestService';
import api from '../../api/axios';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import LoginInput from '../../components/auth/LoginInput';
import { FileText, Save, ArrowLeft, AlertCircle, PlusCircle, Building } from 'lucide-react';

/**
 * Screen where patients and hospitals create and submit blood requests.
 * Enhanced with Healthcare Portal UI for Hospital role.
 */
export default function CreateBloodRequest() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { role } = useAuthStore();
  const isHospital = role === 'HOSPITAL';

  const patientQuery = usePatientProfile();
  const hospitalQuery = useHospitalProfile();

  const profile = isHospital ? hospitalQuery.profile : patientQuery.profile;
  const isProfileLoading = isHospital ? hospitalQuery.isLoading : patientQuery.isLoading;
  const profileError = isHospital ? hospitalQuery.error : patientQuery.error;

  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { 
    register, 
    handleSubmit, 
    formState: { errors } 
  } = useForm({
    defaultValues: {
      bloodGroupNeeded: '',
      unitsRequired: 1,
      hospitalId: '',
      urgencyLevel: 'MEDIUM',
      requiredByDate: '',
      reason: '',
    }
  });

  // Query verified hospitals (for patients)
  const { 
    data: hospitals, 
    isLoading: isHospitalsLoading, 
    error: hospitalsError 
  } = useQuery({
    queryKey: ['hospitalsList'],
    queryFn: async () => {
      try {
        const response = await api.get('/admin/analytics/top-hospitals');
        return response.data;
      } catch {
        return [];
      }
    },
    enabled: !isHospital,
  });

  const isLoading = isProfileLoading || (!isHospital && isHospitalsLoading);
  const error = profileError || (!isHospital && hospitalsError);

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message || 'Failed to load profile.'} />;
  }

  // Profile setup check
  if (!profile) {
    return (
      <div className="flex flex-col gap-6 max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-5 border border-dashed border-gray-200">
          <div className="p-4 bg-red-50 text-primary rounded-full border border-red-100">
            <FileText className="h-10 w-10" />
          </div>
          <div className="flex flex-col gap-2 max-w-md">
            <h2 className="text-lg font-bold text-gray-800">Profile Completion Required</h2>
            <p className="text-xs text-gray-500 leading-relaxed">
              {isHospital 
                ? 'You must set up your hospital profile details before logging emergency blood transfusion requests.'
                : 'You must set up your patient profile details before submitting blood transfusion requests to hospitals.'}
            </p>
          </div>
          <Link to={isHospital ? '/hospital/profile/edit' : '/patient/profile/edit'}>
            <Button variant="primary" className="px-6 py-2.5">Create Profile Now</Button>
          </Link>
        </Card>
      </div>
    );
  }

  const onSubmit = async (data) => {
    setIsSubmitting(true);
    setErrorMsg('');
    try {
      const payload = {
        ...data,
        hospitalId: data.hospitalId ? parseInt(data.hospitalId, 10) : profile?.id,
        unitsRequired: parseInt(data.unitsRequired, 10),
      };

      if (isHospital) {
        await api.post('/hospital/requests', payload);
        queryClient.invalidateQueries({ queryKey: ['recentRequests'] });
        navigate('/hospital/requests');
      } else {
        await bloodRequestService.createRequest(payload);
        queryClient.invalidateQueries({ queryKey: ['myRequests'] });
        navigate('/patient/requests');
      }
    } catch (err) {
      setErrorMsg(err.message || 'Failed to submit blood request.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const getMinDateStr = () => {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  };

  // Hospital Role Layout
  if (isHospital) {
    return (
      <div className="flex flex-col gap-6 pb-12 font-sans max-w-3xl mx-auto">
        <HospitalPageHeader
          title="Create Emergency Blood Request"
          subtitle="Log a new blood transfusion request for an admitted emergency patient."
          icon={PlusCircle}
          badge="Emergency Dispatch"
          breadcrumbs={[
            { label: 'Blood Requests', to: '/hospital/requests' },
            { label: 'Create Request' }
          ]}
          action={
            <button
              onClick={() => navigate('/hospital/requests')}
              className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
            >
              <ArrowLeft className="h-4 w-4" />
              <span>Back to Requests</span>
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
              {/* Blood Group */}
              <div className="flex flex-col gap-1.5 w-full">
                <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                  Blood Group Needed
                </label>
                <select
                  className={`w-full px-4 py-3 rounded-2xl text-sm bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white border transition-all focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 ${
                    errors.bloodGroupNeeded
                      ? 'border-red-400 dark:border-red-500/80 bg-red-50/20'
                      : 'border-slate-200 dark:border-slate-700'
                  }`}
                  {...register('bloodGroupNeeded', { required: 'Please select blood group needed' })}
                >
                  <option value="">Select Blood Group...</option>
                  <option value="A_POSITIVE">A+ (A Positive)</option>
                  <option value="A_NEGATIVE">A- (A Negative)</option>
                  <option value="B_POSITIVE">B+ (B Positive)</option>
                  <option value="B_NEGATIVE">B- (B Negative)</option>
                  <option value="AB_POSITIVE">AB+ (AB Positive)</option>
                  <option value="AB_NEGATIVE">AB- (AB Negative)</option>
                  <option value="O_POSITIVE">O+ (O Positive)</option>
                  <option value="O_NEGATIVE">O- (O Negative)</option>
                </select>
                {errors.bloodGroupNeeded && (
                  <span className="text-[11px] text-red-500 dark:text-red-400 font-medium pl-1">
                    {errors.bloodGroupNeeded.message}
                  </span>
                )}
              </div>

              {/* Units required */}
              <LoginInput
                label="Units Required (Bags)"
                type="number"
                min="1"
                error={errors.unitsRequired?.message}
                {...register('unitsRequired', {
                  required: 'Units required is required',
                  min: { value: 1, message: 'Must request at least 1 unit' }
                })}
              />

              {/* Requesting Hospital */}
              <div className="flex flex-col gap-1.5 w-full sm:col-span-2">
                <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                  Requesting Institution
                </label>
                <div className="w-full px-4 py-3 rounded-2xl border border-teal-200 dark:border-teal-900/50 bg-teal-50/60 dark:bg-teal-950/40 text-teal-900 dark:text-teal-200 font-bold text-xs flex items-center justify-between">
                  <span className="flex items-center gap-2">
                    <Building className="h-4 w-4 text-teal-600 dark:text-teal-400" />
                    {profile?.hospitalName || 'Authenticated Hospital'}
                  </span>
                  <span className="text-[10px] font-extrabold uppercase tracking-wider bg-teal-600 text-white px-2.5 py-0.5 rounded-full shadow-xs">
                    Verified Portal
                  </span>
                </div>
              </div>

              {/* Urgency Level */}
              <div className="flex flex-col gap-1.5 w-full">
                <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                  Urgency Priority
                </label>
                <select
                  className="w-full px-4 py-3 rounded-2xl text-sm bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500"
                  {...register('urgencyLevel')}
                >
                  <option value="LOW">Low / Routine</option>
                  <option value="MEDIUM">Medium / Urgent</option>
                  <option value="HIGH">High Priority</option>
                  <option value="CRITICAL">Critical Emergency</option>
                </select>
              </div>

              {/* Required by date */}
              <LoginInput
                label="Required By Date"
                type="date"
                min={getMinDateStr()}
                error={errors.requiredByDate?.message}
                {...register('requiredByDate', { required: 'Required Date is required' })}
              />
            </div>

            {/* Reason */}
            <div className="flex flex-col gap-1.5 w-full">
              <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                Clinical Reason & Operation Context
              </label>
              <textarea
                rows={3}
                placeholder="Explain the clinical emergency or surgery context..."
                className={`w-full px-4 py-3 rounded-2xl text-xs bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 border transition-all focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 ${
                  errors.reason
                    ? 'border-red-400 dark:border-red-500/80 bg-red-50/20'
                    : 'border-slate-200 dark:border-slate-700'
                }`}
                {...register('reason', { required: 'Reason for request is required' })}
              />
              {errors.reason && (
                <span className="text-[11px] text-red-500 dark:text-red-400 font-medium pl-1">
                  {errors.reason.message}
                </span>
              )}
            </div>

            <div className="flex items-center justify-end pt-4 border-t border-slate-100 dark:border-slate-800">
              <button
                type="submit"
                disabled={isSubmitting}
                className="px-6 py-3 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-bold text-xs rounded-2xl shadow-lg shadow-teal-500/20 transition-all flex items-center gap-2"
              >
                {isSubmitting ? (
                  <span>Submitting Request...</span>
                ) : (
                  <>
                    <Save className="h-4 w-4" />
                    <span>Submit Emergency Request</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </HospitalCard>
      </div>
    );
  }

  // Patient Role Layout (Standard)
  return (
    <div className="flex flex-col gap-6 max-w-2xl mx-auto">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/patient/requests')}
          className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded-xl shadow-sm transition-all"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-gray-900">Create Blood Request</h1>
          <p className="text-xs text-gray-500 mt-0.5">
            Submit a compatibility donation match request to a local hospital.
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
            <div className="flex flex-col gap-1.5 w-full">
              <label className="text-xs font-semibold text-gray-600 tracking-wide">Blood Group Needed</label>
              <select
                className={`w-full px-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-0 ${
                  errors.bloodGroupNeeded
                    ? 'border-red-400 focus:border-red-500 focus:ring-red-100 bg-red-50/10'
                    : 'border-gray-200 focus:border-primary focus:ring-red-100'
                } bg-white text-gray-900`}
                {...register('bloodGroupNeeded', { required: 'Please select blood group needed' })}
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
              {errors.bloodGroupNeeded && <span className="text-[11px] text-red-500 font-medium pl-1">{errors.bloodGroupNeeded.message}</span>}
            </div>

            <Input
              label="Units Required (Bags)"
              type="number"
              error={errors.unitsRequired?.message}
              {...register('unitsRequired', {
                required: 'Units required is required',
                min: { value: 1, message: 'Must request at least 1 unit' }
              })}
            />

            <div className="flex flex-col gap-1.5 w-full">
              <label className="text-xs font-semibold text-gray-600 tracking-wide">Target Hospital</label>
              <select
                className={`w-full px-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-0 ${
                  errors.hospitalId
                    ? 'border-red-400 focus:border-red-500 focus:ring-red-100 bg-red-50/10'
                    : 'border-gray-200 focus:border-primary focus:ring-red-100'
                } bg-white text-gray-900`}
                {...register('hospitalId', { required: 'Please select target hospital' })}
              >
                <option value="">Select Hospital...</option>
                {hospitals && hospitals.map(h => (
                  <option key={h.id} value={h.id}>
                    {h.hospitalName} ({h.city}, {h.state})
                  </option>
                ))}
              </select>
              {errors.hospitalId && <span className="text-[11px] text-red-500 font-medium pl-1">{errors.hospitalId.message}</span>}
            </div>

            <div className="flex flex-col gap-1.5 w-full">
              <label className="text-xs font-semibold text-gray-600 tracking-wide">Urgency Level</label>
              <select
                className="w-full px-4 py-2.5 rounded-xl border border-gray-200 text-sm bg-white text-gray-900"
                {...register('urgencyLevel')}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>

            <Input
              label="Required By Date"
              type="date"
              min={getMinDateStr()}
              error={errors.requiredByDate?.message}
              {...register('requiredByDate', { required: 'Required Date is required' })}
            />
          </div>

          <div className="flex flex-col gap-1.5 w-full">
            <label className="text-xs font-semibold text-gray-600 tracking-wide">Reason for Request</label>
            <textarea
              rows={3}
              placeholder="Explain the clinical reason or operation context..."
              className={`w-full px-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none ${
                errors.reason
                  ? 'border-red-400 focus:border-red-500 focus:ring-red-100 bg-red-50/10'
                  : 'border-gray-200 focus:border-primary focus:ring-red-100 bg-white text-gray-900 placeholder-gray-400'
              }`}
              {...register('reason', { required: 'Reason for request is required' })}
            />
            {errors.reason && <span className="text-[11px] text-red-500 font-medium pl-1">{errors.reason.message}</span>}
          </div>

          <Button type="submit" variant="primary" isLoading={isSubmitting} className="w-full sm:w-fit self-end mt-4 px-6">
            <Save className="h-4 w-4 mr-2" /> Submit Request
          </Button>
        </form>
      </Card>
    </div>
  );
}
