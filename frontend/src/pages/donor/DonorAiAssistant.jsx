import React, { useState } from 'react';
import { useAuthStore } from '../../store/authStore';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import {
  Bot,
  Send,
  User,
  Sparkles,
  HelpCircle,
  ShieldCheck,
  RefreshCw,
} from 'lucide-react';

/**
 * Dedicated Donor AI Assistant Page.
 * Conversational AI guidance for blood donation eligibility, ABO/Rh rules,
 * emergency request dispatch questions, and healthcare donation advice.
 */
export default function DonorAiAssistant() {
  const { user } = useAuthStore();
  const donorName = user?.fullName || user?.name || 'Donor';
  const userBloodGroup = user?.bloodGroup?.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') || 'O+';

  const [inputQuery, setInputQuery] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'ai',
      text: `Hello ${donorName}! 👋 I am your BloodBridge AI Health Assistant. I can help answer questions about your blood donation eligibility, ABO/Rh compatibility rules, and how emergency request dispatch works.`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);

  const suggestedQuestions = [
    'Am I currently eligible to donate blood?',
    'What does my blood group mean?',
    'Why am I not receiving this emergency request?',
    'How does the 90-day cooldown timer work?',
  ];

  const handleSend = (textToSend) => {
    const text = textToSend || inputQuery;
    if (!text.trim()) return;

    const userMsg = {
      id: Date.now(),
      sender: 'user',
      text: text.trim(),
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!textToSend) setInputQuery('');
    setIsTyping(true);

    // Dynamic intelligent response simulation tailored to BloodBridge rules
    setTimeout(() => {
      let responseText = '';
      const lower = text.toLowerCase();

      if (lower.includes('eligible') || lower.includes('cooldown')) {
        responseText = `Based on BloodBridge medical rules, donors must complete a 90-day cooldown between blood donations. You can check your exact remaining days on your Eligibility page or Dashboard widget!`;
      } else if (lower.includes('blood group') || lower.includes('meaning')) {
        responseText = `Your blood group is ${userBloodGroup}. Blood group compatibility is calculated using ABO and Rh antigen rules to match universal donors (like O-) and recipients safely.`;
      } else if (lower.includes('not receiving') || lower.includes('request')) {
        responseText = `BloodBridge emergency request notifications are dispatched only when a hospital request matches your exact ABO/Rh compatibility, geographic proximity, and active availability status.`;
      } else if (lower.includes('certificate')) {
        responseText = `Official PDF donation certificates are issued directly by accredited hospital staff after transfusion sign-off and can be downloaded from your Donation History page.`;
      } else {
        responseText = `Thank you for asking! BloodBridge connects compatible donors with hospitals during emergency requirements. For medical advice or specific questions, hospital medical staff verify all details prior to donation.`;
      }

      const aiMsg = {
        id: Date.now() + 1,
        sender: 'ai',
        text: responseText,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };

      setMessages((prev) => [...prev, aiMsg]);
      setIsTyping(false);
    }, 800);
  };

  return (
    <div className="flex flex-col gap-8 max-w-4xl mx-auto pb-12 font-sans">
      
      {/* Top Banner */}
      <div className="bg-slate-900 text-white rounded-3xl p-6 sm:p-8 border border-slate-800 shadow-xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="flex items-center gap-4">
          <div className="h-14 w-14 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 text-white flex items-center justify-center font-black text-xl shrink-0 shadow-lg">
            <Bot className="h-8 w-8" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-extrabold uppercase tracking-widest text-indigo-400 bg-indigo-950 px-2.5 py-0.5 rounded border border-indigo-800">
                AI Assistant
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-white mt-1">
              BloodBridge AI Assistant
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Conversational healthcare guidance for blood donation questions & compatibility rules.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1.5 text-xs text-slate-400 font-semibold bg-slate-800/80 px-3.5 py-2 rounded-xl border border-slate-700">
          <ShieldCheck className="h-4 w-4 text-emerald-400" />
          <span>Private & Role Protected</span>
        </div>
      </div>

      {/* Suggested Quick Question Chips */}
      <div className="flex flex-wrap gap-2">
        {suggestedQuestions.map((q, idx) => (
          <button
            key={idx}
            onClick={() => handleSend(q)}
            className="px-3.5 py-2 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-bold text-slate-700 dark:text-slate-300 hover:border-red-500 dark:hover:border-red-500 hover:text-red-600 transition-all text-left flex items-center gap-1.5 shadow-sm"
          >
            <HelpCircle className="h-3.5 w-3.5 text-red-500 shrink-0" />
            <span>{q}</span>
          </button>
        ))}
      </div>

      {/* Chat Conversation Box */}
      <Card className="flex flex-col h-[520px] p-0 overflow-hidden border border-slate-200/80 dark:border-slate-800 shadow-xl">
        
        {/* Chat Header */}
        <div className="px-6 py-4 bg-slate-900 text-white flex items-center justify-between border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="h-3 w-3 rounded-full bg-emerald-400 animate-pulse" />
            <span className="text-xs font-black uppercase tracking-wider text-slate-200">
              Live Assistant Channel
            </span>
          </div>
          <button
            onClick={() => setMessages(messages.slice(0, 1))}
            className="text-[11px] font-bold text-slate-400 hover:text-white flex items-center gap-1 transition-colors"
          >
            <RefreshCw className="h-3.5 w-3.5" /> Clear History
          </button>
        </div>

        {/* Messages Stream */}
        <div className="flex-1 overflow-y-auto p-6 flex flex-col gap-4 bg-slate-50/50 dark:bg-slate-950/50">
          {messages.map((msg) => {
            const isAI = msg.sender === 'ai';
            return (
              <div
                key={msg.id}
                className={`flex gap-3 max-w-[85%] ${isAI ? 'self-start' : 'self-end flex-row-reverse'}`}
              >
                <div
                  className={`h-9 w-9 rounded-2xl flex items-center justify-center text-white shrink-0 font-bold text-xs shadow-md ${
                    isAI
                      ? 'bg-gradient-to-br from-indigo-500 to-purple-600'
                      : 'bg-gradient-to-br from-red-500 to-rose-600'
                  }`}
                >
                  {isAI ? <Bot className="h-5 w-5" /> : <User className="h-5 w-5" />}
                </div>

                <div
                  className={`p-4 rounded-2xl text-xs leading-relaxed ${
                    isAI
                      ? 'bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 text-slate-800 dark:text-slate-100 shadow-sm'
                      : 'bg-red-600 text-white shadow-md'
                  }`}
                >
                  <p className="font-semibold">{msg.text}</p>
                  <span
                    className={`text-[9px] block mt-1.5 font-bold ${
                      isAI ? 'text-slate-400' : 'text-red-200'
                    }`}
                  >
                    {msg.timestamp}
                  </span>
                </div>
              </div>
            );
          })}

          {isTyping && (
            <div className="flex gap-3 self-start items-center text-xs text-slate-400 font-bold bg-white dark:bg-slate-900 p-3 rounded-2xl border border-slate-200 dark:border-slate-800">
              <Sparkles className="h-4 w-4 text-indigo-500 animate-spin" />
              <span>BloodBridge AI is typing...</span>
            </div>
          )}
        </div>

        {/* Input Bar */}
        <div className="p-4 bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 flex items-center gap-3">
          <input
            type="text"
            value={inputQuery}
            onChange={(e) => setInputQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            placeholder="Ask a question about blood donation eligibility, ABO/Rh rules..."
            className="flex-1 px-4 py-3 text-xs font-semibold rounded-xl bg-slate-100 dark:bg-slate-800 border-0 text-slate-900 dark:text-white placeholder-slate-400 focus:ring-2 focus:ring-red-500 outline-none"
          />
          <Button
            variant="primary"
            onClick={() => handleSend()}
            disabled={!inputQuery.trim() || isTyping}
            className="px-5 py-3 text-xs font-black bg-red-600 hover:bg-red-700 text-white rounded-xl shadow-md flex items-center gap-1.5"
          >
            <Send className="h-4 w-4" /> Send
          </Button>
        </div>

      </Card>
    </div>
  );
}
