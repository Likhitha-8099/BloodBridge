import React, { Suspense, lazy } from 'react';
import { Routes, Route } from 'react-router-dom';
import Home from '../pages/Home';
import ProtectedRoute from './ProtectedRoute';
import LoadingSpinner from '../components/common/LoadingSpinner';

// Lazy load MainLayout (for protected authenticated dashboards)
const MainLayout = lazy(() => import('../layouts/MainLayout'));

// Lazy load Auth Pages
const DonorLogin = lazy(() => import('../pages/auth/DonorLogin'));
const HospitalLogin = lazy(() => import('../pages/auth/HospitalLogin'));
const AdminLogin = lazy(() => import('../pages/auth/AdminLogin'));
const Register = lazy(() => import('../pages/auth/Register'));

// Donor Pages
const DonorDashboard = lazy(() => import('../pages/donor/DonorDashboard'));
const DonorProfile = lazy(() => import('../pages/donor/DonorProfile'));
const EditDonorProfile = lazy(() => import('../pages/donor/EditDonorProfile'));
const BloodRequests = lazy(() => import('../pages/donor/BloodRequests'));
const DonationHistory = lazy(() => import('../pages/donor/DonationHistory'));
const DonorImpactDashboard = lazy(() => import('../pages/donor/DonorImpactDashboard'));
const DonorEligibility = lazy(() => import('../pages/donor/DonorEligibility'));
const DonorAiAssistant = lazy(() => import('../pages/donor/DonorAiAssistant'));

// Patient Pages
const PatientDashboard = lazy(() => import('../pages/patient/PatientDashboard'));
const PatientProfile = lazy(() => import('../pages/patient/PatientProfile'));
const EditPatientProfile = lazy(() => import('../pages/patient/EditPatientProfile'));
const CreateBloodRequest = lazy(() => import('../pages/patient/CreateBloodRequest'));
const MyRequests = lazy(() => import('../pages/patient/MyRequests'));
const RequestDetails = lazy(() => import('../pages/patient/RequestDetails'));

// Hospital Pages
const HospitalDashboard = lazy(() => import('../pages/hospital/HospitalDashboard'));
const HospitalProfile = lazy(() => import('../pages/hospital/HospitalProfile'));
const EditHospitalProfile = lazy(() => import('../pages/hospital/EditHospitalProfile'));
const HospitalRequests = lazy(() => import('../pages/hospital/BloodRequests'));
const HospitalRequestDetails = lazy(() => import('../pages/hospital/RequestDetails'));
const DonorMatches = lazy(() => import('../pages/hospital/DonorMatches'));
const DonationManagement = lazy(() => import('../pages/hospital/DonationManagement'));
const HospitalUsers = lazy(() => import('../pages/hospital/HospitalUsers'));
const HospitalDonors = lazy(() => import('../pages/hospital/HospitalDonors'));
const HospitalAiAssistant = lazy(() => import('../pages/hospital/HospitalAiAssistant'));

// Admin Pages
const AdminDashboard = lazy(() => import('../pages/admin/AdminDashboard'));
const DonorManagement = lazy(() => import('../pages/admin/DonorManagement'));
const DonorDetails = lazy(() => import('../pages/admin/DonorDetails'));
const HospitalManagement = lazy(() => import('../pages/admin/HospitalManagement'));
const HospitalDetails = lazy(() => import('../pages/admin/HospitalDetails'));
const UserManagement = lazy(() => import('../pages/admin/UserManagement'));
const RequestAnalytics = lazy(() => import('../pages/admin/RequestAnalytics'));
const DonationAnalytics = lazy(() => import('../pages/admin/DonationAnalytics'));
const MatchingAnalytics = lazy(() => import('../pages/admin/MatchingAnalytics'));
const HospitalAnalytics = lazy(() => import('../pages/admin/HospitalAnalytics'));
const NotificationAnalytics = lazy(() => import('../pages/admin/NotificationAnalytics'));
const AdminProfile = lazy(() => import('../pages/admin/AdminProfile'));

// Notifications
const NotificationCenter = lazy(() => import('../pages/notifications/NotificationCenter'));
const NotificationDetails = lazy(() => import('../pages/notifications/NotificationDetails'));

// Error Pages
const NotFoundPage = lazy(() => import('../pages/ErrorPages').then(module => ({ default: module.NotFoundPage })));
const ForbiddenPage = lazy(() => import('../pages/ErrorPages').then(module => ({ default: module.ForbiddenPage })));
const ServerErrorPage = lazy(() => import('../pages/ErrorPages').then(module => ({ default: module.ServerErrorPage })));
const HospitalAuth = lazy(() => import('../pages/auth/HospitalAuth'));
const HospitalRegister = lazy(() => import('../pages/auth/HospitalRegister'));

/**
 * Global Routing System definitions mapping endpoints to UI views with route-based chunk splitting.
 */
export default function AppRoutes() {
  return (
    <Suspense fallback={<LoadingSpinner fullScreen />}>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Home />} />
        <Route path="/auth/hospital" element={<HospitalAuth />} />
        <Route path="/login" element={<DonorLogin />} />
        <Route path="/login/donor" element={<DonorLogin />} />
        <Route path="/login/hospital" element={<HospitalLogin />} />
        <Route path="/login/admin" element={<AdminLogin />} />
        <Route path="/register" element={<Register />} />
        <Route path="/register/donor" element={<Register />} />
        <Route path="/register/hospital" element={<HospitalRegister />} />

        {/* Protected Routes nested in MainLayout */}
        <Route element={<MainLayout />}>
          {/* Donor Protected Area */}
          <Route element={<ProtectedRoute allowedRoles={['DONOR']} />}>
            <Route path="/donor/dashboard" element={<DonorDashboard />} />
            <Route path="/donor/profile" element={<DonorProfile />} />
            <Route path="/donor/profile/edit" element={<EditDonorProfile />} />
            <Route path="/donor/requests" element={<BloodRequests />} />
            <Route path="/donor/history" element={<DonationHistory />} />
            <Route path="/donor/impact" element={<DonorImpactDashboard />} />
            <Route path="/donor/eligibility" element={<DonorEligibility />} />
            <Route path="/donor/ai-assistant" element={<DonorAiAssistant />} />
          </Route>

          {/* Patient Protected Area */}
          <Route element={<ProtectedRoute allowedRoles={['PATIENT']} />}>
            <Route path="/patient/dashboard" element={<PatientDashboard />} />
            <Route path="/patient/profile" element={<PatientProfile />} />
            <Route path="/patient/profile/edit" element={<EditPatientProfile />} />
            <Route path="/patient/create-request" element={<CreateBloodRequest />} />
            <Route path="/patient/requests" element={<MyRequests />} />
            <Route path="/patient/requests/:id" element={<RequestDetails />} />
          </Route>

          {/* Hospital Protected Area */}
          <Route element={<ProtectedRoute allowedRoles={['HOSPITAL']} />}>
            <Route path="/hospital/dashboard" element={<HospitalDashboard />} />
            <Route path="/hospital/profile" element={<HospitalProfile />} />
            <Route path="/hospital/profile/edit" element={<EditHospitalProfile />} />
            <Route path="/hospital/create-request" element={<CreateBloodRequest />} />
            <Route path="/hospital/requests/create" element={<CreateBloodRequest />} />
            <Route path="/hospital/requests" element={<HospitalRequests />} />
            <Route path="/hospital/requests/:id" element={<HospitalRequestDetails />} />
            <Route path="/hospital/users" element={<HospitalUsers />} />
            <Route path="/hospital/donors" element={<HospitalDonors />} />
            <Route path="/hospital/matches" element={<DonorMatches />} />
            <Route path="/hospital/donations" element={<DonationManagement />} />
            <Route path="/hospital/ai-assistant" element={<HospitalAiAssistant />} />
          </Route>

          {/* Admin Protected Area */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/donors" element={<DonorManagement />} />
            <Route path="/admin/donors/:id" element={<DonorDetails />} />
            <Route path="/admin/hospitals" element={<HospitalManagement />} />
            <Route path="/admin/hospitals/:id" element={<HospitalDetails />} />
            <Route path="/admin/hospitals-approvals" element={<HospitalAnalytics />} />
            <Route path="/admin/users" element={<UserManagement />} />
            <Route path="/admin/requests" element={<RequestAnalytics />} />
            <Route path="/admin/donations" element={<DonationAnalytics />} />
            <Route path="/admin/matching" element={<MatchingAnalytics />} />
            <Route path="/admin/notifications" element={<NotificationAnalytics />} />
            <Route path="/admin/profile" element={<AdminProfile />} />
          </Route>

          {/* Unified Notifications Area */}
          <Route element={<ProtectedRoute allowedRoles={['DONOR', 'PATIENT', 'HOSPITAL', 'ADMIN']} />}>
            <Route path="/notifications" element={<NotificationCenter />} />
            <Route path="/notifications/:id" element={<NotificationDetails />} />
          </Route>

          {/* Specific Error Mappings */}
          <Route path="/403" element={<ForbiddenPage />} />
          <Route path="/500" element={<ServerErrorPage />} />
        </Route>

        {/* Wildcard Fallback */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}
