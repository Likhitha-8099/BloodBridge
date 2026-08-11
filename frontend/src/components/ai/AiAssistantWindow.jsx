import React, { useRef, useEffect } from 'react';
import { Bot, X, Minus, Trash2, Send, Sparkles, HelpCircle, Loader2 } from 'lucide-react';
import AiAssistantMessage from './AiAssistantMessage';

/**
 * Chat window modal interface for BloodBridge AI Assistant.
 */
export default function AiAssistantWindow({
  isOpen,
  isMinimized,
  messages,
  inputMessage,
  isLoading,
  error: _error,
  role,
  quickQuestions,
  setInputMessage,
  toggleMinimize,
  closeAssistant,
  clearChat,
  sendMessage
}) {
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Auto-scroll to bottom on new message
  useEffect(() => {
    if (isOpen && !isMinimized) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isLoading, isOpen, isMinimized]);

  // Focus input when opened
  useEffect(() => {
    if (isOpen && !isMinimized) {
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [isOpen, isMinimized]);

  if (!isOpen) return null;

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleQuickQuestionClick = (question) => {
    sendMessage(question);
  };

  return (
    <div
      className={`fixed right-4 sm:right-6 z-50 transition-all duration-300 transform ${
        isMinimized
          ? 'bottom-20 w-80 h-14 rounded-2xl shadow-xl'
          : 'bottom-20 w-[92vw] sm:w-[420px] h-[580px] max-h-[82vh] rounded-3xl shadow-2xl'
      } bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 flex flex-col overflow-hidden backdrop-blur-xl`}
    >
      {/* ── HEADER ────────────────────────────────────────────────────────── */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-red-950 px-4 py-3 text-white flex items-center justify-between shrink-0 shadow-md">
        <div className="flex items-center gap-2.5">
          <div className="relative p-2 bg-red-600/30 rounded-xl border border-red-500/30">
            <Bot className="w-5 h-5 text-amber-300" />
            <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-emerald-400"></span>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-sm tracking-wide">BloodBridge AI</h3>
              {role && (
                <span className="px-2 py-0.5 text-[10px] font-extrabold uppercase tracking-wider rounded-full bg-red-500/20 text-red-300 border border-red-400/30">
                  {role}
                </span>
              )}
            </div>
            <p className="text-[11px] text-slate-300 flex items-center gap-1">
              <Sparkles className="w-3 h-3 text-amber-300 inline" /> Smart Assistant
            </p>
          </div>
        </div>

        {/* Window Action Controls */}
        <div className="flex items-center gap-1">
          {!isMinimized && messages.length > 1 && (
            <button
              onClick={clearChat}
              title="Clear chat history"
              className="p-1.5 hover:bg-slate-800 rounded-lg text-slate-300 hover:text-white transition-colors"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}

          <button
            onClick={toggleMinimize}
            title={isMinimized ? 'Expand window' : 'Minimize window'}
            className="p-1.5 hover:bg-slate-800 rounded-lg text-slate-300 hover:text-white transition-colors"
          >
            <Minus className="w-4 h-4" />
          </button>

          <button
            onClick={closeAssistant}
            title="Close assistant"
            className="p-1.5 hover:bg-slate-800 rounded-lg text-slate-300 hover:text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Minimized Content Preview */}
      {isMinimized && (
        <div
          onClick={toggleMinimize}
          className="flex-1 px-4 flex items-center justify-between cursor-pointer text-xs font-semibold text-slate-700 dark:text-slate-200"
        >
          <span>Chat minimized ({messages.length} messages)</span>
          <span className="text-primary hover:underline">Expand</span>
        </div>
      )}

      {/* ── EXPANDED BODY & MESSAGES ───────────────────────────────────────── */}
      {!isMinimized && (
        <>
          <div className="flex-1 p-4 overflow-y-auto bg-slate-50/50 dark:bg-slate-950/50 space-y-3">
            {/* Quick Questions Chips Header */}
            {quickQuestions && quickQuestions.length > 0 && (
              <div className="mb-3">
                <div className="flex items-center gap-1.5 text-xs font-bold text-slate-500 dark:text-slate-400 mb-2">
                  <HelpCircle className="w-3.5 h-3.5 text-red-500" /> Suggested Questions
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {quickQuestions.map((q, idx) => (
                    <button
                      key={idx}
                      onClick={() => handleQuickQuestionClick(q)}
                      disabled={isLoading}
                      className="text-xs px-3 py-1.5 rounded-full bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-200 border border-slate-200 dark:border-slate-700 hover:border-red-400 dark:hover:border-red-500 hover:text-red-600 dark:hover:text-red-400 transition-all text-left shadow-2xs font-medium outline-none disabled:opacity-50"
                    >
                      {q}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Message Feed */}
            {messages.map((msg) => (
              <AiAssistantMessage key={msg.id} message={msg} />
            ))}

            {/* Loading Typing Dots Indicator */}
            {isLoading && (
              <div className="flex items-center gap-2 my-2 p-3 bg-slate-100 dark:bg-slate-800 rounded-2xl w-fit rounded-tl-none border border-slate-200 dark:border-slate-700">
                <Bot className="w-4 h-4 text-amber-500 animate-bounce" />
                <div className="flex gap-1 items-center text-xs text-slate-500 dark:text-slate-400">
                  <span className="font-semibold">BloodBridge AI is thinking</span>
                  <span className="flex gap-1 ml-1">
                    <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-pulse"></span>
                    <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-pulse delay-150"></span>
                    <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-pulse delay-300"></span>
                  </span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* ── FOOTER INPUT BAR ────────────────────────────────────────────── */}
          <div className="p-3 bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 shrink-0">
            <div className="relative flex items-center bg-slate-100 dark:bg-slate-800/80 rounded-2xl border border-slate-200 dark:border-slate-700/80 focus-within:border-red-500 dark:focus-within:border-red-500 transition-all p-1.5">
              <textarea
                ref={inputRef}
                rows={1}
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Ask BloodBridge AI..."
                disabled={isLoading}
                className="w-full bg-transparent px-3 py-1.5 text-sm text-slate-800 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none resize-none max-h-24"
              />

              <button
                onClick={() => sendMessage()}
                disabled={!inputMessage.trim() || isLoading}
                aria-label="Send message"
                className="p-2.5 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold transition-all disabled:opacity-40 disabled:cursor-not-allowed shrink-0 ml-1 shadow-sm"
              >
                {isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
              </button>
            </div>
            <p className="text-[10px] text-center text-slate-400 dark:text-slate-500 mt-1.5">
              AI provides platform guidance. For medical emergencies, contact local health services.
            </p>
          </div>
        </>
      )}
    </div>
  );
}
