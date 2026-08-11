import api from '../api/axios';

/**
 * Service handling BloodBridge AI Assistant API interactions.
 */
export const sendChatMessage = async ({ message, currentPage, requestId }) => {
  try {
    const payload = {
      message,
      currentPage,
      ...(requestId ? { requestId: Number(requestId) } : {})
    };

    const response = await api.post('/ai/assistant/chat', payload);
    return response.data;
  } catch (error) {
    const fallbackMsg = error.response?.data?.message || 'BloodBridge Assistant is temporarily unavailable. Please try again later.';
    throw new Error(fallbackMsg);
  }
};

export default {
  sendChatMessage
};
