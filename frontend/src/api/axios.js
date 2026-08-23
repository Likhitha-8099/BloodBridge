import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const getBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api/v1';
  let cleaned = envUrl.trim().replace(/\/+$/, '');
  // Normalize base URL: if root URL is provided (e.g. https://bloodbridge-backend-cun1.onrender.com),
  // ensure /api/v1 is appended so endpoints like '/auth/login' resolve to '/api/v1/auth/login'.
  if (!cleaned.endsWith('/api/v1') && !cleaned.endsWith('/api')) {
    cleaned = `${cleaned}/api/v1`;
  }
  return cleaned;
};

const api = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach token if it exists
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle auth failures globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      useAuthStore.getState().logout();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    const errorMessage = error.response?.data?.message || error.message || 'An error occurred';
    const err = new Error(errorMessage);
    if (error.response) {
      err.response = error.response;
      err.data = error.response.data;
      err.errors = error.response.data?.errors;
    }
    return Promise.reject(err);
  }
);

export default api;
