import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { usePatientProfile } from '../../hooks/usePatientProfile';
import { bloodRequestService } from '../../services/bloodRequestService';
import api from '../../api/axios';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { FileText, Save, ArrowLeft, AlertCircle } from 'lucide-react';

/**
 * Screen where patients create and submit a blood request.
 */
export default function CreateBloodRequest() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { profile, isLoading: isProfileLoading, error: profileError } = usePatientProfile();
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

  // Query verified hospitals
  const { 
    data: hospitals, 
    isLoading: isHospitalsLoading, 
    error: hospitalsError 
  } = useQuery({
    queryKey: ['hospitalsList'],
    queryFn: async () => {
      const response = await api.get('/hospitals');
      return response.data;
    }
  });

  const isLoading = isProfileLoading || isHospitalsLoading;
  const error = profileError || hospitalsError;

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} />;
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
              You must set up your patient profile details before submitting blood transfusion requests to hospitals.
            </p>
          </div>
          <Link to="/patient/profile/edit">
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
        hospitalId: parseInt(data.hospitalId, 10),
        unitsRequired: parseInt(data.unitsRequired, 10),
      };
      await bloodRequestService.createRequest(payload);
      queryClient.invalidateQueries({ queryKey: ['myRequests'] });
      navigate('/patient/requests');
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
            {/* Blood Group selection */}
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

            {/* Units required */}
            <Input
              label="Units Required (Bags)"
              type="number"
              error={errors.unitsRequired?.message}
              {...register('unitsRequired', {
                required: 'Units required is required',
                min: { value: 1, message: 'Must request at least 1 unit' }
              })}
            />

            {/* Target hospital select dropdown */}
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

            {/* Urgency Level selection */}
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

            {/* Required by date picker */}
            <Input
              label="Required By Date"
              type="date"
              min={getMinDateStr()}
              error={errors.requiredByDate?.message}
              {...register('requiredByDate', { required: 'Required Date is required' })}
            />
          </div>

          {/* Reason for transfusion */}
          <div className="flex flex-col gap-1.5 w-full">
            <label className="text-xs font-semibold text-gray-600 tracking-wide">Reason for Request</label>
            <textarea
              rows={3}
              placeholder="Explain the clinical reason or operation context (e.g., scheduled heart surgery, accident trauma)."
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
