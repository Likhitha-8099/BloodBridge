import api from '../api/axios';

/**
 * Service for retrieving donation histories.
 */
export const donationService = {
  /**
   * Fetches donation histories for a specific donor ID.
   */
  getDonationHistory: async (donorId) => {
    try {
      const response = await api.get('/donor/donations');
      const data = response.data?.data !== undefined ? response.data.data : response.data;
      if (Array.isArray(data)) return data;
      if (donorId) {
        const fallback = await api.get(`/donations/donor/${donorId}`);
        const fbData = fallback.data?.data !== undefined ? fallback.data.data : fallback.data;
        return Array.isArray(fbData) ? fbData : [];
      }
      return [];
    } catch {
      if (donorId) {
        try {
          const fallback = await api.get(`/donations/donor/${donorId}`);
          const fbData = fallback.data?.data !== undefined ? fallback.data.data : fallback.data;
          return Array.isArray(fbData) ? fbData : [];
        } catch (e) {
          console.warn('[DONATION-SERVICE] Failed to fetch donation history:', e.message);
        }
      }
      return [];
    }
  },

  downloadCertificate: async (donationId) => {
    try {
      const response = await api.get(`/donor/donations/${donationId}/certificate`, {
        responseType: 'blob'
      });

      let filename = `BloodBridge_Certificate_${donationId}.pdf`;
      const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition'];
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename="?([^";]+)"?/);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1];
        }
      }

      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('[CERTIFICATE-DOWNLOAD-ERROR]', err);
      let errorMsg = 'Failed to download donation certificate. Please ensure the donation is marked COMPLETED.';
      if (err.response && err.response.data) {
        if (err.response.data instanceof Blob) {
          try {
            const text = await err.response.data.text();
            const json = JSON.parse(text);
            if (json.message) errorMsg = json.message;
          } catch {
            // silent parse fallback
          }
        } else if (err.response.data.message) {
          errorMsg = err.response.data.message;
        }
      }
      alert(errorMsg);
    }
  },
  completeDonation: async (donationId, payload = {}) => {
    const response = await api.patch(`/donations/${donationId}/complete`, payload);
    return response.data;
  },
};

export default donationService;
