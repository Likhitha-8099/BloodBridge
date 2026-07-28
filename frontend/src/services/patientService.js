import api from '../api/axios';

/**
 * Service for managing patient profiles.
 */
export const patientService = {
  /**
   * Retrieves the profile of the currently logged-in patient.
   */
  getProfile: async () => {
    const response = await api.get('/patients/me');
    return response.data;
  },

  /**
   * Creates a new patient profile.
   */
  createProfile: async (profileData) => {
    const response = await api.post('/patients', profileData);
    return response.data;
  },

  /**
   * Updates the existing patient profile.
   */
  updateProfile: async (profileData) => {
    const response = await api.put('/patients/me', profileData);
    return response.data;
  },
};

export default patientService;
