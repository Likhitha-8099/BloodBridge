import React, { useState } from 'react';
import { Bot, User, Copy, Check, AlertCircle } from 'lucide-react';

/**
 * Message bubble component for user and assistant messages in the chat window.
 */
export default function AiAssistantMessage({ message }) {
  const [copied, setCopied] = useState(false);
  const isUser = message.sender === 'user';
  const isError = message.isError;

  const handleCopy = () => {
    if (!message.text) return;
    navigator.clipboard.writeText(message.text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Helper to format line breaks, lists, and bold text cleanly
  const formatText = (text) => {
    if (!text) return null;
    const lines = text.split('\n');
    return lines.map((line, idx) => {
      // Bold syntax **text**
      const formattedLine = line.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

      if (line.trim().startsWith('- ') || line.trim().startsWith('* ')) {
        return (
          <li key={idx} className="ml-4 list-disc my-0.5" dangerouslySetInnerHTML={{ __html: formattedLine.replace(/^[-*]\s+/, '') }} />
        );
      }
      if (/^\d+\.\s+/.test(line.trim())) {
        return (
          <li key={idx} className="ml-4 list-decimal my-0.5" dangerouslySetInnerHTML={{ __html: formattedLine.replace(/^\d+\.\s+/, '') }} />
        );
      }

      if (line.trim() === '') {
        return <div key={idx} className="h-2" />;
      }

      return (
        <p key={idx} className="my-1 leading-relaxed" dangerouslySetInnerHTML={{ __html: formattedLine }} />
      );
    });
  };

  return (
    <div className={`flex gap-3 my-3 text-sm ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
      {/* Avatar Icon */}
      <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 shadow-sm ${
        isUser
          ? 'bg-red-600 text-white'
          : isError
            ? 'bg-amber-100 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400 border border-amber-300 dark:border-amber-800'
            : 'bg-gradient-to-tr from-slate-800 to-slate-700 dark:from-slate-700 dark:to-slate-600 text-amber-300 border border-slate-600'
      }`}>
        {isUser ? <User className="w-4 h-4" /> : isError ? <AlertCircle className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
      </div>

      {/* Message Content Bubble */}
      <div className={`group relative max-w-[82%] sm:max-w-[78%] px-4 py-3 rounded-2xl shadow-sm text-sm ${
        isUser
          ? 'bg-gradient-to-r from-red-600 to-rose-600 text-white rounded-tr-none'
          : isError
            ? 'bg-amber-50 dark:bg-amber-950/40 text-amber-900 dark:text-amber-200 border border-amber-200 dark:border-amber-800/60 rounded-tl-none'
            : 'bg-slate-100 dark:bg-slate-800/90 text-slate-800 dark:text-slate-100 border border-slate-200/80 dark:border-slate-700/60 rounded-tl-none'
      }`}>
        {/* Text rendering */}
        <div className="break-words space-y-1">
          {formatText(message.text)}
        </div>

        {/* Footer timestamp & copy action */}
        <div className={`flex items-center justify-between gap-2 mt-1.5 pt-1 text-[11px] ${
          isUser ? 'text-red-100' : 'text-slate-400 dark:text-slate-400 border-t border-slate-200/50 dark:border-slate-700/50'
        }`}>
          <span>{message.timestamp}</span>

          {!isUser && !isError && (
            <button
              onClick={handleCopy}
              title="Copy message"
              className="opacity-0 group-hover:opacity-100 transition-opacity p-1 hover:bg-slate-200 dark:hover:bg-slate-700 rounded text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
            >
              {copied ? <Check className="w-3 h-3 text-emerald-500" /> : <Copy className="w-3 h-3" />}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
