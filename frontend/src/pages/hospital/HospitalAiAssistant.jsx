import React, { useState } from 'react';
import { useAuthStore } from '../../store/authStore';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import {
  Bot,
  Send,
  Sparkles,
  ShieldCheck,
  RefreshCw,
  Building,
} from 'lucide-react';

/**
 * Hospital AI Clinical & Administrative Assistant.
 * Intelligent guidance for emergency request creation, ABO/Rh matching rules,
 * donor verification, and transfusion logging.
 */
export default function HospitalAiAssistant() {
  const { user } = useAuthStore();
  const hospitalName = user?.hospitalName || user?.fullName || user?.name || 'Hospital Staff';

  const [inputQuery, setInputQuery] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'ai',
      text: `Welcome ${hospitalName}! 👋 I am your BloodBridge Hospital AI Clinical Assistant. How can I assist you with emergency blood requests, donor matching algorithms, or transfusion completion logging today?`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);

  const suggestedQuestions = [
    'How does 10-Stage Smart Matching work?',
    'What are the ABO/Rh compatibility rules for emergency requests?',
    'How do I mark a donation as COMPLETED and issue a certificate?',
    'Why is a request marked as PENDING or VERIFIED?',
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

    // Dynamic intelligent response simulation tailored to BloodBridge Hospital rules
    setTimeout(() => {
      let responseText = '';
      const lower = text.toLowerCase();

      if (lower.includes('smart matching') || lower.includes('matching algorithm')) {
        responseText = `BloodBridge's 10-Stage Smart Matching engine evaluates ABO/Rh blood group compatibility, donor 90-day cooldown status, geographic radius proximity (in KM), emergency availability flags, and response history to notify optimal donors.`;
      } else if (lower.includes('abo') || lower.includes('compatibility') || lower.includes('rules')) {
        responseText = `In emergency transfusion matching, O- is the universal red cell donor compatible with all recipient blood groups, while AB+ is the universal plasma recipient. The engine automatically filters compatible donors based on standard medical blood matrix rules.`;
      } else if (lower.includes('completed') || lower.includes('certificate') || lower.includes('mark')) {
        responseText = `To issue an official PDF certificate, navigate to Donation Management, locate the ACCEPTED or CONFIRMED donation run, click "Log Complete", specify the units donated, and submit sign-off. The certificate is immediately generated for the donor.`;
      } else if (lower.includes('pending') || lower.includes('verify') || lower.includes('verified')) {
        responseText = `Newly created blood requests start in PENDING status until verified by hospital staff or administrators. Clicking "Verify Request" promotes the request to VERIFIED/MATCHED, triggering automated donor matching notifications.`;
      } else {
        responseText = `Thank you for reaching out! As a verified hospital institution on BloodBridge, all emergency requests and donation completions logged in your portal directly update regional blood availability and donor records in real-time.`;
      }

      const aiMsg = {
        id: Date.now() + 1,
        sender: 'ai',
        text: responseText,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };

      setMessages((prev) => [...prev, aiMsg]);
      setIsTyping(false);
    }, 700);
  };

  const handleClearChat = () => {
    setMessages([
      {
        id: Date.now(),
        sender: 'ai',
        text: `Chat cleared. How else can I assist your medical team today, ${hospitalName}?`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
  };

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto pb-12 font-sans">
      <HospitalPageHeader
        title="Hospital AI Clinical Assistant"
        subtitle="24/7 intelligent assistance for blood transfusion protocols, donor matching, and portal navigation."
        icon={Bot}
        badge="Clinical AI"
        breadcrumbs={[{ label: 'AI Assistant' }]}
        action={
          <button
            onClick={handleClearChat}
            className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
          >
            <RefreshCw className="h-3.5 w-3.5" />
            <span>Reset Chat</span>
          </button>
        }
      />

      {/* Suggested Question Chips */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
        <span className="text-[10px] font-extrabold uppercase tracking-wider text-slate-400 dark:text-slate-500 shrink-0 flex items-center gap-1">
          <Sparkles className="h-3 w-3 text-teal-500" /> Quick Questions:
        </span>
        {suggestedQuestions.map((q, idx) => (
          <button
            key={idx}
            onClick={() => handleSend(q)}
            className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white dark:bg-slate-900 text-teal-700 dark:text-teal-300 border border-teal-200/80 dark:border-teal-800 hover:bg-teal-50 dark:hover:bg-teal-950/60 shadow-xs transition-all whitespace-nowrap shrink-0"
          >
            {q}
          </button>
        ))}
      </div>

      {/* Main Chat Interface */}
      <HospitalCard bodyClassName="p-0 overflow-hidden flex flex-col h-[560px]">
        {/* Chat Messages Body */}
        <div className="flex-1 p-6 overflow-y-auto flex flex-col gap-4 bg-slate-50/50 dark:bg-slate-950/40">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`flex items-start gap-3 max-w-[85%] ${
                msg.sender === 'user' ? 'ml-auto flex-row-reverse' : ''
              }`}
            >
              {/* Avatar */}
              <div
                className={`h-9 w-9 rounded-2xl flex items-center justify-center font-bold text-xs shrink-0 shadow-sm ${
                  msg.sender === 'user'
                    ? 'bg-teal-600 text-white'
                    : 'bg-gradient-to-br from-indigo-500 to-purple-600 text-white'
                }`}
              >
                {msg.sender === 'user' ? <Building className="h-4 w-4" /> : <Bot className="h-5 w-5" />}
              </div>

              {/* Message Bubble */}
              <div
                className={`p-4 rounded-3xl text-xs leading-relaxed shadow-sm ${
                  msg.sender === 'user'
                    ? 'bg-teal-600 text-white rounded-tr-xs'
                    : 'bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-100 border border-slate-200/80 dark:border-slate-800 rounded-tl-xs'
                }`}
              >
                <p className="whitespace-pre-wrap font-medium">{msg.text}</p>
                <span
                  className={`text-[9px] font-bold mt-1.5 block text-right ${
                    msg.sender === 'user' ? 'text-teal-100/80' : 'text-slate-400 dark:text-slate-500'
                  }`}
                >
                  {msg.timestamp}
                </span>
              </div>
            </div>
          ))}

          {/* Typing Indicator */}
          {isTyping && (
            <div className="flex items-center gap-3">
              <div className="h-9 w-9 rounded-2xl bg-indigo-600 text-white flex items-center justify-center font-bold text-xs shrink-0">
                <Bot className="h-5 w-5 animate-spin" />
              </div>
              <div className="p-3.5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs text-slate-400 font-medium flex items-center gap-1.5 shadow-sm">
                <span className="h-2 w-2 bg-teal-500 rounded-full animate-bounce" />
                <span className="h-2 w-2 bg-teal-500 rounded-full animate-bounce [animation-delay:0.2s]" />
                <span className="h-2 w-2 bg-teal-500 rounded-full animate-bounce [animation-delay:0.4s]" />
                <span className="ml-1">Analyzing BloodBridge clinical rules...</span>
              </div>
            </div>
          )}
        </div>

        {/* Input Bar Footer */}
        <div className="p-4 bg-white dark:bg-slate-900 border-t border-slate-100 dark:border-slate-800">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
            className="flex items-center gap-2"
          >
            <input
              type="text"
              placeholder="Ask about blood compatibility, emergency requests, or donation completion..."
              value={inputQuery}
              onChange={(e) => setInputQuery(e.target.value)}
              className="flex-1 px-4 py-3 rounded-2xl text-xs bg-slate-50 dark:bg-slate-800/80 text-slate-900 dark:text-white placeholder-slate-400 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all"
            />
            <button
              type="submit"
              disabled={!inputQuery.trim() || isTyping}
              className="p-3 bg-gradient-to-r from-teal-600 to-emerald-600 text-white rounded-2xl font-bold hover:shadow-lg hover:shadow-teal-500/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all shrink-0"
            >
              <Send className="h-4 w-4" />
            </button>
          </form>

          <p className="text-[10px] text-slate-400 dark:text-slate-500 text-center mt-2 flex items-center justify-center gap-1">
            <ShieldCheck className="h-3 w-3 text-teal-500" />
            <span>BloodBridge AI Assistant provides guidance on platform protocols and ABO/Rh rules. Medical sign-off requires qualified staff verification.</span>
          </p>
        </div>
      </HospitalCard>
    </div>
  );
}
