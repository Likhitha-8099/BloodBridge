import { useState, useCallback, useMemo } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { sendChatMessage } from '../services/aiAssistantService';

const INITIAL_GREETING = {
  id: 'welcome-1',
  sender: 'assistant',
  text: "Hello! I am your BloodBridge AI Assistant. How can I help you today?",
  timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
};

export function useAiAssistant(requestId = null) {
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [messages, setMessages] = useState([INITIAL_GREETING]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const location = useLocation();
  const { role, isAuthenticated } = useAuthStore();

  const toggleOpen = useCallback(() => {
    setIsOpen((prev) => {
      if (!prev) setIsMinimized(false);
      return !prev;
    });
  }, []);

  const toggleMinimize = useCallback(() => {
    setIsMinimized((prev) => !prev);
  }, []);

  const closeAssistant = useCallback(() => {
    setIsOpen(false);
    setIsMinimized(false);
  }, []);

  const clearChat = useCallback(() => {
    setMessages([INITIAL_GREETING]);
    setError(null);
  }, []);

  const sendMessage = useCallback(async (customText = null) => {
    const textToSend = (customText || inputMessage).trim();
    if (!textToSend || isLoading) return;

    const userMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: textToSend,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputMessage('');
    setIsLoading(true);
    setError(null);

    try {
      const response = await sendChatMessage({
        message: textToSend,
        currentPage: location.pathname,
        requestId
      });

      const replyText = response?.data?.reply || response?.message || 'Thank you for your question.';
      const aiMessage = {
        id: `assistant-${Date.now()}`,
        sender: 'assistant',
        text: replyText,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };

      setMessages((prev) => [...prev, aiMessage]);
    } catch (err) {
      const errorMessageText = err.message || 'BloodBridge Assistant is temporarily unavailable. Please try again later.';
      setError(errorMessageText);
      
      const errorAiMsg = {
        id: `err-${Date.now()}`,
        sender: 'assistant',
        text: errorMessageText,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        isError: true
      };

      setMessages((prev) => [...prev, errorAiMsg]);
    } finally {
      setIsLoading(false);
    }
  }, [inputMessage, isLoading, location.pathname, requestId]);

  const quickQuestions = useMemo(() => {
    switch (role) {
      case 'ADMIN':
        return [
          'How does hospital approval work?',
          'Explain the BloodBridge workflow',
          'How does donor matching work?'
        ];
      case 'HOSPITAL':
        return [
          'How do I create an emergency request?',
          'How are donors matched?',
          'What happens when a donor accepts?',
          'Explain donor matching groups'
        ];
      case 'DONOR':
        return [
          'Why did I receive this request?',
          'How does blood compatibility work?',
          'What happens after I accept?',
          'How do I reject a request?'
        ];
      case 'PATIENT':
        return [
          'How do I create a blood request?',
          'How does donor matching work for patients?',
          'What happens when a donor accepts my request?'
        ];
      default:
        return [
          'How does BloodBridge work?',
          'How does blood compatibility work?',
          'How to get started?'
        ];
    }
  }, [role]);

  return {
    isOpen,
    isMinimized,
    messages,
    inputMessage,
    isLoading,
    error,
    role,
    isAuthenticated,
    quickQuestions,
    setInputMessage,
    toggleOpen,
    toggleMinimize,
    closeAssistant,
    clearChat,
    sendMessage
  };
}

export default useAiAssistant;
