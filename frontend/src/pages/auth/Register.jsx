import React, { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { useNavigate, Link, Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import ProgressBar from '../../components/ui/ProgressBar';
import ToggleSwitch from '../../components/ui/ToggleSwitch';
import BloodBridgeLogo from '../../components/common/BloodBridgeLogo';
import { 
  UserPlus, AlertCircle, CheckCircle2, ArrowLeft, ArrowRight, 
  User, Stethoscope, MapPin, Bell, ShieldCheck, Save
} from 'lucide-react';

const DRAFT_KEY = 'blood_bridge_registration_draft';

/**
 * World-Class 5-Step Healthcare Donor/Hospital Registration Wizard.
 * Pre-selects role based on route path.
 */
export default function Register() {
  const { isAuthenticated, role } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const pathname = location.pathname;
  const isHospital = pathname.includes('hospital');

  const [step, setStep] = useState(1);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [draftSaved, setDraftSaved] = useState(false);

  const totalSteps = 5;

  // Saved Draft Recovery
  const loadDraft = () => {
    try {
      const saved = localStorage.getItem(DRAFT_KEY);
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  };

  const initialValues = loadDraft() || {
    fullName: '',
    email: '',
    phoneNumber: '',
    alternatePhoneNumber: '',
    password: '',
    confirmPassword: '',
    role: isHospital ? 'HOSPITAL' : 'DONOR',
    dateOfBirth: '',
    age: '',
    gender: 'MALE',
    aadhaarNumber: '',
    govtIdType: 'Aadhaar Card',
    govtIdNumber: '',
    occupation: '',
    emergencyContactName: '',
    emergencyContactNumber: '',
    emergencyContactRelationship: '',
    
    // Step 2 Address
    country: 'India',
    state: '',
    district: '',
    city: '',
    postalCode: '',
    address: '',
    landmark: '',
    latitude: null,
    longitude: null,

    // Step 3 Medical & Vitals
    bloodGroup: 'O_POSITIVE',
    height: '',
    weight: '',
    bmi: '',
    lastDonationDate: '',
    totalDonations: 0,
    hemoglobin: '',
    bloodPressure: '',
    pulseRate: '',

    // Medical Conditions Checkboxes
    diabetes: false,
    hypertension: false,
    heartDisease: false,
    asthma: false,
    cancer: false,
    kidneyDisease: false,
    liverDisease: false,
    epilepsy: false,
    thyroid: false,
    tuberculosis: false,
    otherConditions: '',

    // Lifestyle Toggles
    smoking: false,
    alcohol: false,
    drugUsage: false,
    pregnancy: false,
    breastfeeding: false,
    recentSurgery: false,
    recentTattoo: false,
    recentVaccination: false,
    recentFever: false,
    currentMedications: '',
    allergies: '',
    covidHistory: '',
    travelHistory: '',

    // Step 4 Preferences
    emergencyAvailable: true,
    preferredDonationRadius: 25,
    preferredHospitals: '',
    preferredContactMethod: 'EMAIL',
    availableDays: '',
    availableTimeSlots: '',
    willingDonatePlatelets: true,
    willingDonatePlasma: true,
    rareBloodDonor: false,
    pushNotificationEnabled: true,

    // Step 5 Consent Checkboxes
    consentCorrect: false,
    consentVoluntary: false,
    consentPrivacy: false,
    consentNotifications: false,
    consentRules: false,
    digitalSignature: ''
  };

  const { 
    register, 
    handleSubmit, 
    trigger,
    getValues,
    setValue,
    watch,
    control,
    formState: { errors, isValid } 
  } = useForm({
    defaultValues: initialValues,
    mode: 'onChange'
  });

  const watchDob = watch('dateOfBirth');
  const watchHeight = watch('height');
  const watchWeight = watch('weight');
  const allValues = watch();

  // Auto Calculate Age from Date of Birth
  useEffect(() => {
    if (watchDob) {
      const birthDate = new Date(watchDob);
      const today = new Date();
      let calculatedAge = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        calculatedAge--;
      }
      if (!isNaN(calculatedAge) && calculatedAge > 0) {
        setValue('age', calculatedAge);
      }
    }
  }, [watchDob, setValue]);

  // Auto Calculate BMI from Height (cm) & Weight (kg)
  useEffect(() => {
    if (watchHeight > 0 && watchWeight > 0) {
      const heightInMeters = watchHeight / 100;
      const calculatedBmi = (watchWeight / (heightInMeters * heightInMeters)).toFixed(1);
      if (!isNaN(calculatedBmi)) {
        setValue('bmi', parseFloat(calculatedBmi));
      }
    }
  }, [watchHeight, watchWeight, setValue]);

  // Draft Auto-Save feature
  const saveDraft = () => {
    try {
      const currentForm = getValues();
      localStorage.setItem(DRAFT_KEY, JSON.stringify(currentForm));
      setDraftSaved(true);
      setTimeout(() => setDraftSaved(false), 2000);
    } catch (e) {
      console.error('Failed to save draft:', e);
    }
  };

  if (isAuthenticated && role) {
    return <Navigate to={`/${role.toLowerCase()}/dashboard`} replace />;
  }

  const nextStep = async () => {
    let fieldsToValidate = [];
    if (step === 1) {
      fieldsToValidate = ['fullName', 'email', 'phoneNumber', 'password', 'confirmPassword'];
    } else if (step === 2) {
      fieldsToValidate = ['country', 'state', 'city', 'postalCode', 'address'];
    } else if (step === 3) {
      fieldsToValidate = ['bloodGroup', 'height', 'weight'];
    } else if (step === 4) {
      fieldsToValidate = ['preferredDonationRadius'];
    }

    const isValid = await trigger(fieldsToValidate);
    if (isValid) {
      setStep((prev) => Math.min(totalSteps, prev + 1));
      setErrorMsg('');
      saveDraft();
    }
  };

  const prevStep = () => {
    setStep((prev) => Math.max(1, prev - 1));
    setErrorMsg('');
  };

  const onSubmit = async (data) => {
    setIsLoading(true);
    setErrorMsg('');
    setSuccessMsg('');
    try {
      // Validate Password match
      if (data.password !== data.confirmPassword) {
        setErrorMsg('Passwords do not match. Please verify.');
        setIsLoading(false);
        return;
      }

      // Compile Medical Conditions String from Checkboxes
      const conditionsList = [];
      if (data.diabetes) conditionsList.push('Diabetes');
      if (data.hypertension) conditionsList.push('Hypertension');
      if (data.heartDisease) conditionsList.push('Heart Disease');
      if (data.asthma) conditionsList.push('Asthma');
      if (data.cancer) conditionsList.push('Cancer');
      if (data.kidneyDisease) conditionsList.push('Kidney Disease');
      if (data.liverDisease) conditionsList.push('Liver Disease');
      if (data.epilepsy) conditionsList.push('Epilepsy');
      if (data.thyroid) conditionsList.push('Thyroid');
      if (data.tuberculosis) conditionsList.push('Tuberculosis');
      if (data.otherConditions) conditionsList.push(data.otherConditions);

      const medicalConditionsStr = conditionsList.length > 0 ? conditionsList.join(', ') : 'None';

      const registerPayload = {
        fullName: data.fullName,
        email: data.email,
        phoneNumber: data.phoneNumber,
        role: 'DONOR',
        password: data.password,
        
        // Donor Profile & Health Fields
        dateOfBirth: data.dateOfBirth,
        age: Number(data.age),
        gender: data.gender,
        bloodGroup: data.bloodGroup,
        weight: Number(data.weight),
        height: data.height ? Number(data.height) : null,
        bmi: data.bmi ? Number(data.bmi) : null,
        
        // Address & Location
        country: data.country,
        state: data.state,
        district: data.district,
        city: data.city,
        postalCode: data.postalCode,
        address: data.address,
        landmark: data.landmark,
        latitude: data.latitude ? Number(data.latitude) : null,
        longitude: data.longitude ? Number(data.longitude) : null,
        
        // Identification & Emergency Contact
        alternatePhoneNumber: data.alternatePhoneNumber,
        aadhaarNumber: data.aadhaarNumber,
        govtIdType: data.govtIdType,
        govtIdNumber: data.govtIdNumber,
        occupation: data.occupation,
        emergencyContactName: data.emergencyContactName,
        emergencyContactNumber: data.emergencyContactNumber,
        emergencyContactRelationship: data.emergencyContactRelationship,
        
        // Vitals & Health History
        hemoglobin: data.hemoglobin ? Number(data.hemoglobin) : null,
        bloodPressure: data.bloodPressure,
        pulseRate: data.pulseRate ? Number(data.pulseRate) : null,
        medicalConditions: medicalConditionsStr,
        currentMedications: data.currentMedications,
        allergies: data.allergies,
        covidHistory: data.covidHistory,
        travelHistory: data.travelHistory,
        
        // Lifestyle Flags
        smoking: Boolean(data.smoking),
        alcohol: Boolean(data.alcohol),
        drugUsage: Boolean(data.drugUsage),
        pregnancy: Boolean(data.pregnancy),
        breastfeeding: Boolean(data.breastfeeding),
        recentSurgery: Boolean(data.recentSurgery),
        recentTattoo: Boolean(data.recentTattoo),
        recentVaccination: Boolean(data.recentVaccination),
        recentFever: Boolean(data.recentFever),
        
        // Preferences
        emergencyAvailable: Boolean(data.emergencyAvailable),
        preferredDonationRadius: data.preferredDonationRadius ? Number(data.preferredDonationRadius) : 25,
        preferredHospitals: data.preferredHospitals,
        preferredContactMethod: data.preferredContactMethod,
        availableDays: data.availableDays,
        availableTimeSlots: data.availableTimeSlots,
        willingDonatePlatelets: Boolean(data.willingDonatePlatelets),
        willingDonatePlasma: Boolean(data.willingDonatePlasma),
        rareBloodDonor: Boolean(data.rareBloodDonor),
        pushNotificationEnabled: Boolean(data.pushNotificationEnabled)
      };

      await authService.register(registerPayload);
      
      // Clear saved draft on success
      localStorage.removeItem(DRAFT_KEY);

      setSuccessMsg('Donor registration completed successfully! Account created & profile configured. Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setErrorMsg(err.message || 'Registration failed. Please check your details and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const stepTitles = [
    { title: 'Personal Info', icon: User },
    { title: 'Address & Location', icon: MapPin },
    { title: 'Medical & Vitals', icon: Stethoscope },
    { title: 'Preferences', icon: Bell },
    { title: 'Consent & Review', icon: ShieldCheck }
  ];

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col items-center justify-center p-4 sm:p-6 py-12 font-sans">
      <div className="w-full max-w-3xl mb-4 flex items-center justify-between">
        <Link to="/" className="text-xs font-semibold text-gray-500 dark:text-slate-400 hover:text-primary flex items-center gap-1.5 transition-colors">
          <ArrowLeft className="h-4 w-4" /> Back to Home
        </Link>
        <button 
          type="button" 
          onClick={saveDraft}
          className="text-xs font-bold text-slate-600 dark:text-slate-400 hover:text-primary flex items-center gap-1 bg-white dark:bg-slate-900 px-3 py-1.5 rounded-full border border-slate-200 dark:border-slate-800 shadow-xs"
        >
          <Save className="h-3.5 w-3.5 text-primary" /> {draftSaved ? 'Draft Saved!' : 'Save Progress'}
        </button>
      </div>

      <Card className="w-full max-w-3xl p-6 sm:p-10 flex flex-col gap-6 shadow-2xl border-slate-100 dark:border-slate-800 bg-white dark:bg-slate-900 rounded-3xl">
        {/* Header */}
        <div className="flex flex-col items-center text-center gap-1">
          <Link to="/" className="flex items-center gap-2 mb-2 hover:opacity-90 transition-opacity">
            <BloodBridgeLogo size="lg" />
          </Link>
          <h2 className="text-2xl font-black text-gray-900 dark:text-white">Donor Healthcare Onboarding</h2>
          <p className="text-xs text-gray-400">Step {step} of {totalSteps}: {stepTitles[step - 1].title}</p>
        </div>

        {/* Stepper Header */}
        <div className="flex items-center justify-between gap-2 border-b border-slate-100 dark:border-slate-800 pb-4 overflow-x-auto">
          {stepTitles.map((s, idx) => {
            const StepIcon = s.icon;
            const isCompleted = step > idx + 1;
            const isCurrent = step === idx + 1;
            return (
              <div key={idx} className="flex flex-col items-center gap-1 flex-1 min-w-[70px]">
                <div className={`h-9 w-9 rounded-2xl flex items-center justify-center text-xs font-bold transition-all ${
                  isCompleted 
                    ? 'bg-emerald-500 text-white shadow-sm' 
                    : isCurrent 
                      ? 'bg-primary text-white ring-4 ring-red-100 dark:ring-red-950/40 shadow-sm' 
                      : 'bg-slate-100 dark:bg-slate-800 text-slate-400'
                }`}>
                  {isCompleted ? <CheckCircle2 className="h-5 w-5" /> : <StepIcon className="h-4 w-4" />}
                </div>
                <span className={`text-[11px] font-bold text-center line-clamp-1 ${isCurrent ? 'text-primary' : 'text-gray-400'}`}>
                  {s.title}
                </span>
              </div>
            );
          })}
        </div>

        <ProgressBar value={step} max={totalSteps} showValue={false} height="h-2" />

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
          {/* STEP 1: PERSONAL INFORMATION */}
          {step === 1 && (
            <div className="space-y-4">
              <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2">
                1. Personal & Contact Identity
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Full Legal Name"
                  placeholder="John Doe"
                  error={errors.fullName?.message}
                  {...register('fullName', { required: 'Full name is required', minLength: { value: 3, message: 'Min 3 chars' } })}
                />
                <Input
                  label="Email Address *"
                  type="email"
                  placeholder="john.doe@example.com"
                  error={errors.email?.message}
                  {...register('email', { 
                    required: 'Email is required', 
                    pattern: { value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i, message: 'Please enter a valid email' } 
                  })}
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Mobile Number"
                  placeholder="+91 9876543210"
                  error={errors.phoneNumber?.message}
                  {...register('phoneNumber', { required: 'Mobile number is required' })}
                />
                <Input
                  label="Alternate Mobile Number (Optional)"
                  placeholder="+91 9876543211"
                  {...register('alternatePhoneNumber')}
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <Input
                  label="Date of Birth"
                  type="date"
                  error={errors.dateOfBirth?.message}
                  {...register('dateOfBirth', { required: 'Date of Birth is required' })}
                />
                
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-gray-600 dark:text-slate-400 flex items-center justify-between">
                    <span>Calculated Age</span>
                    <span className="text-[10px] text-primary font-bold">Auto</span>
                  </label>
                  <input
                    type="number"
                    readOnly
                    className="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 text-sm font-bold text-gray-900 dark:text-white"
                    {...register('age')}
                  />
                </div>

                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-gray-600 dark:text-slate-400">Gender</label>
                  <select className="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-slate-800 text-sm bg-white dark:bg-slate-900 text-gray-900 dark:text-white font-medium" {...register('gender')}>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Password"
                  type="password"
                  placeholder="••••••••"
                  error={errors.password?.message}
                  {...register('password', {
                    required: 'Password is required',
                    minLength: { value: 8, message: 'Password must be at least 8 characters' }
                  })}
                />
                <Input
                  label="Confirm Password"
                  type="password"
                  placeholder="••••••••"
                  error={errors.confirmPassword?.message}
                  {...register('confirmPassword', { required: 'Please confirm password' })}
                />
              </div>

              <h4 className="font-bold text-xs text-gray-700 dark:text-slate-300 pt-2">Identity & Emergency Contact</h4>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-gray-600 dark:text-slate-400">Govt ID Type</label>
                  <select className="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-slate-800 text-sm bg-white dark:bg-slate-900 text-gray-900 dark:text-white" {...register('govtIdType')}>
                    <option value="Aadhaar Card">Aadhaar Card</option>
                    <option value="Passport">Passport</option>
                    <option value="Driving License">Driving License</option>
                    <option value="Voter ID">Voter ID</option>
                    <option value="PAN Card">PAN Card</option>
                  </select>
                </div>
                <Input label="Govt ID Number (Optional)" placeholder="e.g. 1234 5678 9012" {...register('govtIdNumber')} />
                <Input label="Occupation" placeholder="e.g. Engineer, Doctor" {...register('occupation')} />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <Input label="Emergency Contact Name" placeholder="Contact Name" {...register('emergencyContactName')} />
                <Input label="Emergency Contact Phone" placeholder="+91 9000000000" {...register('emergencyContactNumber')} />
                <Input label="Relationship" placeholder="e.g. Spouse, Parent" {...register('emergencyContactRelationship')} />
              </div>
            </div>
          )}

          {/* STEP 2: ADDRESS & LOCATION */}
          {step === 2 && (
            <div className="space-y-4">
              <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2">
                2. Residential & Geo-Location Details
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <Input label="Country" placeholder="India" error={errors.country?.message} {...register('country', { required: 'Country required' })} />
                <Input label="State" placeholder="Karnataka" error={errors.state?.message} {...register('state', { required: 'State required' })} />
                <Input label="District" placeholder="Bengaluru Urban" {...register('district')} />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="City / Town" placeholder="Bangalore" error={errors.city?.message} {...register('city', { required: 'City required' })} />
                <Input label="PIN Code" placeholder="560001" error={errors.postalCode?.message} {...register('postalCode', { required: 'PIN code required' })} />
              </div>

              <Input label="Full Residential Address" placeholder="Door No, Street Name, Area" error={errors.address?.message} {...register('address', { required: 'Address required' })} />
              <Input label="Landmark (Optional)" placeholder="e.g. Near Main Market" {...register('landmark')} />
            </div>
          )}

          {/* STEP 3: MEDICAL INFORMATION & VITALS */}
          {step === 3 && (
            <div className="space-y-6">
              <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2">
                3. Medical Parameters & Health History
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

                <Input label="Height (cm)" type="number" error={errors.height?.message} {...register('height', { required: 'Height required', min: { value: 100, message: 'Min 100cm' } })} />
                <Input label="Weight (kg)" type="number" step="0.1" error={errors.weight?.message} {...register('weight', { required: 'Weight required', min: { value: 50, message: 'Must be 50kg+' } })} />
                
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-gray-600 dark:text-slate-400 flex items-center justify-between">
                    <span>Auto BMI</span>
                    <span className="text-[10px] text-primary font-bold">Calculated</span>
                  </label>
                  <input type="number" step="0.1" readOnly className="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 text-sm font-black text-primary" {...register('bmi')} />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <Input label="Hemoglobin (g/dL)" type="number" step="0.1" placeholder="13.5" {...register('hemoglobin')} />
                <Input label="Blood Pressure (mmHg)" placeholder="120/80" {...register('bloodPressure')} />
                <Input label="Pulse Rate (bpm)" type="number" placeholder="72" {...register('pulseRate')} />
              </div>

              {/* Medical History Checkboxes */}
              <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800">
                <h4 className="font-bold text-xs text-gray-800 dark:text-slate-200 mb-3">Existing Medical Conditions (Check all that apply)</h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                  {['diabetes', 'hypertension', 'heartDisease', 'asthma', 'cancer', 'kidneyDisease', 'liverDisease', 'epilepsy', 'thyroid', 'tuberculosis'].map((cond) => (
                    <label key={cond} className="flex items-center gap-2 cursor-pointer font-medium text-gray-700 dark:text-slate-300">
                      <input type="checkbox" className="h-4 w-4 rounded text-primary focus:ring-primary" {...register(cond)} />
                      <span className="capitalize">{cond.replace(/([A-Z])/g, ' $1')}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* FIXED LIFESTYLE TOGGLE SWITCHES */}
              <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 flex flex-col gap-2">
                <h4 className="font-bold text-xs text-gray-800 dark:text-slate-200 mb-1">Lifestyle & Eligibility Toggles</h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1">
                  <Controller control={control} name="smoking" render={({ field }) => (
                    <ToggleSwitch label="Smoking Habits" sublabel="Do you smoke tobacco?" checked={field.value} onChange={field.onChange} />
                  )} />
                  <Controller control={control} name="alcohol" render={({ field }) => (
                    <ToggleSwitch label="Alcohol Consumption" sublabel="Regular alcohol intake?" checked={field.value} onChange={field.onChange} />
                  )} />
                  <Controller control={control} name="recentSurgery" render={({ field }) => (
                    <ToggleSwitch label="Recent Surgery" sublabel="Surgery in last 6 months?" checked={field.value} onChange={field.onChange} />
                  )} />
                  <Controller control={control} name="recentTattoo" render={({ field }) => (
                    <ToggleSwitch label="Recent Tattoo / Piercing" sublabel="In last 6 months?" checked={field.value} onChange={field.onChange} />
                  )} />
                  <Controller control={control} name="recentVaccination" render={({ field }) => (
                    <ToggleSwitch label="Recent Vaccination" sublabel="In last 30 days?" checked={field.value} onChange={field.onChange} />
                  )} />
                  <Controller control={control} name="recentFever" render={({ field }) => (
                    <ToggleSwitch label="Recent Fever / Illness" sublabel="In last 14 days?" checked={field.value} onChange={field.onChange} />
                  )} />
                </div>
              </div>
            </div>
          )}

          {/* STEP 4: DONATION PREFERENCES */}
          {step === 4 && (
            <div className="space-y-6">
              <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2">
                4. Donation & Communication Preferences
              </h3>

              <div className="bg-slate-50 dark:bg-slate-800/40 p-4 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-4">
                <Controller control={control} name="emergencyAvailable" render={({ field }) => (
                  <ToggleSwitch 
                    label="Emergency Availability" 
                    sublabel="Allow urgent blood call alerts via SMS & Push" 
                    checked={field.value} 
                    onChange={field.onChange} 
                  />
                )} />

                <Controller control={control} name="willingDonatePlatelets" render={({ field }) => (
                  <ToggleSwitch 
                    label="Willing to Donate Platelets" 
                    sublabel="Apheresis platelet donations" 
                    checked={field.value} 
                    onChange={field.onChange} 
                  />
                )} />

                <Controller control={control} name="willingDonatePlasma" render={({ field }) => (
                  <ToggleSwitch 
                    label="Willing to Donate Plasma" 
                    sublabel="Convalescent & regular plasma" 
                    checked={field.value} 
                    onChange={field.onChange} 
                  />
                )} />

                <Controller control={control} name="rareBloodDonor" render={({ field }) => (
                  <ToggleSwitch 
                    label="Rare Blood Donor Registry" 
                    sublabel="List on national rare blood type roster" 
                    checked={field.value} 
                    onChange={field.onChange} 
                  />
                )} />

                <Controller control={control} name="pushNotificationEnabled" render={({ field }) => (
                  <ToggleSwitch 
                    label="Push Notifications" 
                    sublabel="Receive real-time match alerts on device" 
                    checked={field.value} 
                    onChange={field.onChange} 
                  />
                )} />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="Preferred Donation Radius (KM)" type="number" {...register('preferredDonationRadius')} />
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-gray-600 dark:text-slate-400">Preferred Contact Method</label>
                  <select className="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-slate-800 text-sm bg-white dark:bg-slate-900 text-gray-900 dark:text-white" {...register('preferredContactMethod')}>
                    <option value="EMAIL">Email</option>
                    <option value="SMS">SMS Message</option>
                    <option value="PUSH">Push Notification</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="Available Days" placeholder="e.g. Weekends, Any Day" {...register('availableDays')} />
                <Input label="Available Time Slots" placeholder="e.g. Morning (8 AM - 12 PM)" {...register('availableTimeSlots')} />
              </div>
            </div>
          )}

          {/* STEP 5: CONSENT & REVIEW SUMMARY */}
          {step === 5 && (
            <div className="space-y-6">
              <h3 className="font-bold text-sm text-gray-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-2">
                5. Mandatory Consent & Summary Verification
              </h3>

              <div className="bg-slate-50 dark:bg-slate-800/40 p-5 rounded-2xl border border-slate-100 dark:border-slate-800 flex flex-col gap-3 text-xs">
                <h4 className="font-bold text-sm text-gray-900 dark:text-white">Form Summary</h4>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-2">
                  <div><span className="text-gray-400 block">Name</span><strong>{allValues.fullName}</strong></div>
                  <div><span className="text-gray-400 block">Email</span><strong>{allValues.email}</strong></div>
                  <div><span className="text-gray-400 block">Blood Group</span><strong className="text-primary">{allValues.bloodGroup}</strong></div>
                  <div><span className="text-gray-400 block">Age / Gender</span><strong>{allValues.age} yrs / {allValues.gender}</strong></div>
                  <div><span className="text-gray-400 block">BMI</span><strong>{allValues.bmi}</strong></div>
                  <div><span className="text-gray-400 block">Location</span><strong>{allValues.city}, {allValues.state}</strong></div>
                </div>
              </div>

              <div className="space-y-3 bg-red-50/40 dark:bg-red-950/20 p-5 rounded-2xl border border-red-100 dark:border-red-900/30 text-xs">
                <h4 className="font-bold text-xs text-red-700 dark:text-red-400">Legal & Health Declarations (Mandatory)</h4>
                
                {[
                  { name: 'consentCorrect', text: 'I confirm all information provided is accurate and true.' },
                  { name: 'consentVoluntary', text: 'I agree to donate blood voluntarily without monetary expectation.' },
                  { name: 'consentPrivacy', text: 'I agree to Blood Bridge Privacy Policy and terms.' },
                  { name: 'consentNotifications', text: 'I agree to receive emergency blood request calls & notifications.' },
                  { name: 'consentRules', text: 'I understand donor eligibility recovery interval rules (56 days).' }
                ].map((c) => (
                  <label key={c.name} className="flex items-start gap-2.5 cursor-pointer font-medium text-gray-800 dark:text-slate-200">
                    <input type="checkbox" className="h-4 w-4 rounded text-primary focus:ring-primary mt-0.5" {...register(c.name, { required: true })} />
                    <span>{c.text}</span>
                  </label>
                ))}
              </div>
            </div>
          )}

          {/* Stepper Controls */}
          <div className="flex items-center justify-between gap-3 mt-6 pt-4 border-t border-slate-100 dark:border-slate-800">
            {step > 1 ? (
              <Button type="button" variant="outline" size="sm" onClick={prevStep}>
                <ArrowLeft className="h-4 w-4 mr-1.5" /> Previous
              </Button>
            ) : <div />}

            {step < totalSteps ? (
              <Button 
                type="button" 
                variant="primary" 
                size="sm" 
                onClick={nextStep} 
                className="px-6"
                disabled={step === 1 && (Boolean(errors.email) || !watch('email'))}
              >
                Next Step <ArrowRight className="h-4 w-4 ml-1.5" />
              </Button>
            ) : (
              <Button 
                type="submit" 
                variant="primary" 
                isLoading={isLoading} 
                disabled={!isValid || Boolean(errors.email) || !watch('email')}
                size="sm" 
                className="px-8 shadow-md"
              >
                <UserPlus className="h-4 w-4 mr-1.5" /> Complete Healthcare Onboarding
              </Button>
            )}
          </div>
        </form>
      </Card>
    </div>
  );
}
