import React from 'react';
import { Bot, Sparkles } from 'lucide-react';

/**
 * Floating trigger button for opening the BloodBridge AI Assistant.
 */
export default function AiAssistantButton({ onClick, isOpen }) {
  return (
    <button
      onClick={onClick}
      aria-label="Open BloodBridge AI Assistant"
      className={`fixed bottom-6 right-6 z-50 group flex items-center gap-2.5 px-4 sm:px-5 py-3.5 rounded-full shadow-2xl transition-all duration-300 transform hover:scale-105 active:scale-95 cursor-pointer outline-none ${
        isOpen
          ? 'bg-slate-900 dark:bg-slate-100 text-white dark:text-slate-900 ring-2 ring-red-500/50'
          : 'bg-gradient-to-r from-red-600 via-rose-600 to-red-700 hover:from-red-700 hover:to-rose-800 text-white border border-red-400/30 shadow-red-600/30'
      }`}
    >
      {/* Bot Icon with glowing aura */}
      <div className="relative flex items-center justify-center">
        <Bot className="w-5 h-5 sm:w-6 sm:h-6 shrink-0 transition-transform group-hover:rotate-12" />
        <span className="absolute -top-1 -right-1 flex h-2.5 w-2.5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
        </span>
      </div>

      {/* Button Text */}
      <span className="text-sm font-bold tracking-wide hidden sm:inline-block">
        BloodBridge <span className="text-amber-300 font-extrabold">AI</span>
      </span>

      <Sparkles className="w-4 h-4 text-amber-300 animate-pulse hidden sm:inline-block" />
    </button>
  );
}
