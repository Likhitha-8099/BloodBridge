import React from 'react';
import useAiAssistant from '../../hooks/useAiAssistant';
import AiAssistantButton from './AiAssistantButton';
import AiAssistantWindow from './AiAssistantWindow';

/**
 * Main wrapper component for BloodBridge AI Assistant.
 * Combines the floating trigger button and the interactive chat window.
 */
export default function AiAssistant({ requestId = null }) {
  const assistant = useAiAssistant(requestId);

  // Render assistant for authenticated users
  if (!assistant.isAuthenticated) {
    return null;
  }

  return (
    <>
      <AiAssistantButton
        isOpen={assistant.isOpen}
        onClick={assistant.toggleOpen}
      />

      <AiAssistantWindow
        isOpen={assistant.isOpen}
        isMinimized={assistant.isMinimized}
        messages={assistant.messages}
        inputMessage={assistant.inputMessage}
        isLoading={assistant.isLoading}
        error={assistant.error}
        role={assistant.role}
        quickQuestions={assistant.quickQuestions}
        setInputMessage={assistant.setInputMessage}
        toggleMinimize={assistant.toggleMinimize}
        closeAssistant={assistant.closeAssistant}
        clearChat={assistant.clearChat}
        sendMessage={assistant.sendMessage}
      />
    </>
  );
}
