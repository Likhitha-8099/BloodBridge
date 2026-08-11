import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import Button from '../components/ui/Button';
import BloodBridgeLogo from '../components/common/BloodBridgeLogo';
import ThemeToggle from '../components/ui/ThemeToggle';
import {
  Heart,
  ShieldCheck,
  Hospital,
  Menu,
  X,
  CheckCircle2,
  Clock,
  Award,
  Shield,
  Droplets,
  Lock,
} from 'lucide-react';

/**
 * Public Homepage for BloodBridge.
 * Features a balanced, human-designed Hero Section:
 * - LEFT: Large "BloodBridge" Title, Headline, Description & CTAs
 * - RIGHT: Large Official BloodBridge Logo as the main visual element
 * Strictly preserves existing authentication, routing, and backend functionality.
 */
export default function Home() {
  const { isAuthenticated, role } = useAuthStore();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [activeStep, setActiveStep] = useState(0);
  const [selectedGroup, setSelectedGroup] = useState('B+');

  // Smooth section scroll
  const scrollToSection = (id) => {
    setMobileMenuOpen(false);
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  // ABO/Rh Blood Compatibility Rules
  const bloodRules = {
    'O-': {
      canReceiveFrom: ['O-'],
      canGiveTo: ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'],
      description: 'Universal Red Cell Donor. Can donate to all blood types in critical emergencies.',
      rarity: '7% of population',
    },
    'O+': {
      canReceiveFrom: ['O+', 'O-'],
      canGiveTo: ['O+', 'A+', 'B+', 'AB+'],
      description: 'Most common blood group. High demand across all hospital emergency rooms.',
      rarity: '37% of population',
    },
    'A-': {
      canReceiveFrom: ['A-', 'O-'],
      canGiveTo: ['A-', 'A+', 'AB-', 'AB+'],
      description: 'Crucial for A- and AB- patients. Can receive red cells from O- and A-.',
      rarity: '6% of population',
    },
    'A+': {
      canReceiveFrom: ['A+', 'A-', 'O+', 'O-'],
      canGiveTo: ['A+', 'AB+'],
      description: 'Second most common blood group. Can receive red cells from O and A types.',
      rarity: '34% of population',
    },
    'B-': {
      canReceiveFrom: ['B-', 'O-'],
      canGiveTo: ['B-', 'B+', 'AB-', 'AB+'],
      description: 'Rare blood type. Vital for B- negative transfusion requirements.',
      rarity: '2% of population',
    },
    'B+': {
      canReceiveFrom: ['B+', 'B-', 'O+', 'O-'],
      canGiveTo: ['B+', 'AB+'],
      description: 'Can receive red blood cells from B+, B-, O+, and O- donors.',
      rarity: '9% of population',
    },
    'AB-': {
      canReceiveFrom: ['AB-', 'A-', 'B-', 'O-'],
      canGiveTo: ['AB-', 'AB+'],
      description: 'Rarest blood group. Can receive red cells from all Rh-negative types.',
      rarity: '1% of population',
    },
    'AB+': {
      canReceiveFrom: ['AB+', 'AB-', 'A+', 'A-', 'B+', 'B-', 'O+', 'O-'],
      canGiveTo: ['AB+'],
      description: 'Universal Red Cell Recipient. Can safely receive red blood cells from any group.',
      rarity: '4% of population',
    },
  };

  const allBloodGroups = ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'];
  const currentRule = bloodRules[selectedGroup] || bloodRules['B+'];

  const journeySteps = [
    {
      number: '01',
      title: 'Register Profile',
      tag: 'Donors & Hospitals',
      desc: 'Donors and verified healthcare institutions register securely with blood group and contact parameters.',
      detail: 'Includes blood type, eligibility cooldown status, and emergency notification settings.',
    },
    {
      number: '02',
      title: 'Create/Find Request',
      tag: 'Urgent Dispatch',
      desc: 'Authorized hospitals issue urgent or standard blood requests specifying required units and blood group.',
      detail: 'Requests specify blood group, units, hospital location, and urgency severity.',
    },
    {
      number: '03',
      title: 'Smart Matching',
      tag: 'ABO/Rh + Proximity',
      desc: 'BloodBridge algorithms filter compatible, eligible donors within geographic proximity.',
      detail: 'Checks ABO/Rh compatibility, 90-day cooldown status, and active availability.',
    },
    {
      number: '04',
      title: 'Donor Responds',
      tag: '1-Click Acceptance',
      desc: 'Compatible donors receive instant push notifications and confirm emergency dispatch acceptance.',
      detail: 'Donors view hospital location, distance, and direct emergency directions.',
    },
    {
      number: '05',
      title: 'Donation Completed',
      tag: 'Certificate Generated',
      desc: 'Hospital staff verifies transfusion completion, issuing an official PDF donation certificate.',
      detail: 'Updates donor history, resets 90-day eligibility timer, and emails certificate.',
    },
  ];

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 flex flex-col font-sans transition-colors duration-200">
      
      {/* ─────────────────────────────────────────────
          1. NAVBAR
      ───────────────────────────────────────────── */}
      <header className="sticky top-0 z-50 bg-white/95 dark:bg-slate-900/95 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800/80 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          
          {/* LEFT: LOGO */}
          <Link to="/" className="flex items-center gap-2 hover:opacity-90 transition-opacity">
            <BloodBridgeLogo size="md" />
          </Link>

          {/* CENTER: NAVIGATION */}
          <nav className="hidden md:flex items-center gap-8 text-xs font-bold text-slate-600 dark:text-slate-300">
            <button
              onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
              className="hover:text-red-600 dark:hover:text-red-400 transition-colors"
            >
              Home
            </button>
            <button
              onClick={() => scrollToSection('how-it-works')}
              className="hover:text-red-600 dark:hover:text-red-400 transition-colors"
            >
              How It Works
            </button>
            <button
              onClick={() => scrollToSection('for-donors')}
              className="hover:text-red-600 dark:hover:text-red-400 transition-colors"
            >
              For Donors
            </button>
            <button
              onClick={() => scrollToSection('for-hospitals')}
              className="hover:text-teal-600 dark:hover:text-teal-400 transition-colors"
            >
              For Hospitals
            </button>
            <button
              onClick={() => scrollToSection('compatibility')}
              className="hover:text-red-600 dark:hover:text-red-400 transition-colors"
            >
              Compatibility
            </button>
            <button
              onClick={() => scrollToSection('about')}
              className="hover:text-red-600 dark:hover:text-red-400 transition-colors"
            >
              About
            </button>
          </nav>

          {/* RIGHT: ACTIONS */}
          <div className="hidden md:flex items-center gap-3">
            <ThemeToggle />

            {isAuthenticated && role ? (
              <Button
                variant="primary"
                onClick={() => navigate(`/${role.toLowerCase()}/dashboard`)}
                className="px-5 py-2 text-xs font-bold bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-700 hover:to-rose-700 shadow-sm"
              >
                Go to Dashboard
              </Button>
            ) : (
              <>
                <Link
                  to="/login"
                  className="px-4 py-2 rounded-xl text-xs font-bold text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                >
                  Login
                </Link>
                <Link to="/register">
                  <Button
                    variant="primary"
                    className="px-5 py-2 text-xs font-bold bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-700 hover:to-rose-700 shadow-md"
                  >
                    Get Started
                  </Button>
                </Link>
              </>
            )}
          </div>

          {/* MOBILE HAMBURGER BUTTON */}
          <div className="flex md:hidden items-center gap-2">
            <ThemeToggle />
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
              aria-label="Toggle menu"
            >
              {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>

        {/* MOBILE MENU DRAWER */}
        {mobileMenuOpen && (
          <div className="md:hidden bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 px-5 py-4 flex flex-col gap-3">
            <button
              onClick={() => { setMobileMenuOpen(false); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
              className="text-left py-2 text-sm font-bold text-slate-700 dark:text-slate-200 hover:text-red-600"
            >
              Home
            </button>
            <button
              onClick={() => scrollToSection('how-it-works')}
              className="text-left py-2 text-sm font-bold text-slate-700 dark:text-slate-200 hover:text-red-600"
            >
              How It Works
            </button>
            <button
              onClick={() => scrollToSection('for-donors')}
              className="text-left py-2 text-sm font-bold text-slate-700 dark:text-slate-200 hover:text-red-600"
            >
              For Donors
            </button>
            <button
              onClick={() => scrollToSection('for-hospitals')}
              className="text-left py-2 text-sm font-bold text-slate-700 dark:text-slate-200 hover:text-teal-600"
            >
              For Hospitals
            </button>
            <button
              onClick={() => scrollToSection('compatibility')}
              className="text-left py-2 text-sm font-bold text-slate-700 dark:text-slate-200 hover:text-red-600"
            >
              Compatibility
            </button>
            <button
              onClick={() => scrollToSection('about')}
              className="text-left py-2 text-sm font-bold text-slate-700 dark:text-slate-200 hover:text-red-600"
            >
              About
            </button>

            <div className="border-t border-slate-200 dark:border-slate-800 pt-3 flex flex-col gap-2">
              {isAuthenticated && role ? (
                <Button
                  variant="primary"
                  onClick={() => { setMobileMenuOpen(false); navigate(`/${role.toLowerCase()}/dashboard`); }}
                  className="w-full text-xs font-bold py-2.5 bg-red-600"
                >
                  Go to Dashboard
                </Button>
              ) : (
                <>
                  <Link to="/login" onClick={() => setMobileMenuOpen(false)}>
                    <Button variant="outline" className="w-full text-xs font-bold py-2.5">
                      Login
                    </Button>
                  </Link>
                  <Link to="/register" onClick={() => setMobileMenuOpen(false)}>
                    <Button variant="primary" className="w-full text-xs font-bold py-2.5 bg-red-600">
                      Get Started
                    </Button>
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </header>

      <main className="flex-1">
        
        {/* ─────────────────────────────────────────────
            2. HERO SECTION (MINIMALIST & BALANCED)
            LEFT: Large "BloodBridge" Title, Headline, Description & CTAs
            RIGHT: Large Official BloodBridge Logo as the main visual element
        ───────────────────────────────────────────── */}
        <section className="relative pt-4 pb-16 lg:pt-6 lg:pb-24 overflow-hidden bg-gradient-to-b from-white via-slate-50 to-slate-100 dark:from-slate-900 dark:via-slate-950 dark:to-slate-950 border-b border-slate-200/80 dark:border-slate-800/80">
          
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            
            {/* TWO-SIDED BALANCED COMPOSITION */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-16 items-center">
              
              {/* ─────────────────────────────────────────
                  LEFT SIDE: ORIGINAL "BLOODBRIDGE" TITLE & CONTENT (7 cols)
              ───────────────────────────────────────── */}
              <div className="lg:col-span-7 flex flex-col text-left">
                
                {/* LARGE PROMINENT BRANDING TITLE */}
                <div className="mb-4">
                  <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight text-slate-900 dark:text-white leading-none">
                    BloodBridge
                  </h1>
                  <span className="text-xs font-extrabold text-red-600 dark:text-red-400 uppercase tracking-widest mt-1.5 block">
                    Healthcare Emergency Network
                  </span>
                </div>

                {/* MAIN HEADLINE */}
                <h2 className="text-2xl sm:text-3xl lg:text-4xl font-extrabold text-slate-800 dark:text-slate-100 tracking-tight leading-snug mb-5">
                  Connecting Blood Donors with Hospitals <br className="hidden sm:inline" />
                  <span className="text-red-600 dark:text-red-500">
                    When Every Second Matters
                  </span>
                </h2>

                {/* SHORT CONCISE DESCRIPTION */}
                <p className="text-base sm:text-lg text-slate-600 dark:text-slate-300 mb-8 leading-relaxed font-normal max-w-xl">
                  BloodBridge connects compatible and available blood donors with hospitals during emergency blood requirements, helping make the right connection when every second matters.
                </p>

                {/* PRIMARY CTA BUTTONS */}
                <div className="flex flex-col sm:flex-row flex-wrap items-stretch sm:items-center gap-3.5 mb-8">
                  <Link to="/register/donor" className="w-full sm:w-auto">
                    <Button
                      variant="primary"
                      className="w-full sm:w-auto px-7 py-3 text-xs font-black bg-red-600 hover:bg-red-700 text-white shadow-xl shadow-red-600/20 flex items-center justify-center gap-2 rounded-xl"
                    >
                      <Heart className="h-4 w-4 fill-white" /> Become a Donor
                    </Button>
                  </Link>

                  <Link to="/auth/hospital" className="w-full sm:w-auto">
                    <Button
                      variant="outline"
                      className="w-full sm:w-auto px-7 py-3 text-xs font-extrabold border-teal-600 text-teal-700 dark:text-teal-400 hover:bg-teal-50 dark:hover:bg-teal-950/50 flex items-center justify-center gap-2 rounded-xl"
                    >
                      <Hospital className="h-4 w-4 text-teal-600 dark:text-teal-400" /> For Hospitals
                    </Button>
                  </Link>

                  <Link to="/login/admin" className="w-full sm:w-auto">
                    <Button
                      variant="outline"
                      className="w-full sm:w-auto px-6 py-3 text-xs font-extrabold border-indigo-500/60 dark:border-indigo-400/50 text-indigo-700 dark:text-indigo-300 hover:bg-indigo-50 dark:hover:bg-indigo-950/50 flex items-center justify-center gap-2 rounded-xl transition-all"
                    >
                      <Shield className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Admin Portal
                    </Button>
                  </Link>
                </div>

                <div className="flex items-center gap-6 text-xs text-slate-500 dark:text-slate-400 font-semibold pt-4 border-t border-slate-200/80 dark:border-slate-800">
                  <span className="flex items-center gap-1.5">
                    <ShieldCheck className="h-4 w-4 text-emerald-600" /> Verified Hospitals Only
                  </span>
                  <span className="flex items-center gap-1.5">
                    <Clock className="h-4 w-4 text-blue-600" /> 90-Day Cooldown Safe
                  </span>
                </div>
              </div>

              {/* ─────────────────────────────────────────
                  RIGHT SIDE: LARGE OFFICIAL BLOODBRIDGE LOGO (5 cols)
              ───────────────────────────────────────── */}
              <div className="lg:col-span-5 flex items-center justify-center text-center h-full">
                <div className="relative transition-transform duration-300 hover:scale-[1.02] flex items-center justify-center">
                  <BloodBridgeLogo size="hero" />
                </div>
              </div>

            </div>

            {/* BOTTOM IMPACTFUL QUOTE */}
            <div className="mt-4 lg:mt-5 pt-3.5 border-t border-slate-200/80 dark:border-slate-800/80 text-center max-w-3xl mx-auto">
              <div className="inline-flex items-center justify-center gap-2 mb-2.5 text-red-600 dark:text-red-400">
                <Heart className="h-4 w-4 fill-red-500 text-red-600 animate-pulse" />
              </div>
              <blockquote className="text-lg sm:text-xl lg:text-2xl font-black text-slate-900 dark:text-white tracking-tight italic">
                “One donation can become someone’s second chance at life.”
              </blockquote>
              <p className="text-xs sm:text-sm font-bold text-slate-500 dark:text-slate-400 mt-2 uppercase tracking-widest">
                Be the connection that makes every second count.
              </p>
            </div>

          </div>
        </section>

        {/* ─────────────────────────────────────────────
            3. HOW BLOODBRIDGE WORKS (MODULE 1)
        ───────────────────────────────────────────── */}
        <section id="how-it-works" className="py-16 lg:py-24 bg-slate-50 dark:bg-slate-950 border-b border-slate-200/60 dark:border-slate-800/60">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <div className="text-center max-w-2xl mx-auto mb-16">
              <span className="text-xs font-extrabold uppercase tracking-widest text-red-600 dark:text-red-400">
                End-to-End Workflow
              </span>
              <h2 className="text-3xl sm:text-4xl font-black text-slate-900 dark:text-white mt-2">
                How BloodBridge Works
              </h2>
              <p className="text-sm text-slate-500 dark:text-slate-400 mt-2">
                Click or hover over any stage below to inspect the step-by-step connection journey.
              </p>
            </div>

            <div className="relative mb-12 hidden md:block">
              <div className="absolute top-1/2 left-0 right-0 h-0.5 bg-slate-200 dark:bg-slate-800 -translate-y-1/2 z-0" />
              
              <div className="grid grid-cols-5 gap-4 relative z-10">
                {journeySteps.map((step, idx) => {
                  const isCurrent = activeStep === idx;
                  return (
                    <button
                      key={step.number}
                      onClick={() => setActiveStep(idx)}
                      className={`flex flex-col items-center gap-2 p-3 rounded-2xl transition-all ${
                        isCurrent
                          ? 'bg-slate-900 text-white shadow-xl scale-105 border border-red-500'
                          : 'bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-800'
                      }`}
                    >
                      <span className={`text-xs font-black px-2 py-0.5 rounded-full ${
                        isCurrent ? 'bg-red-600 text-white' : 'bg-slate-100 dark:bg-slate-800 text-slate-500'
                      }`}>
                        {step.number}
                      </span>
                      <span className="text-xs font-bold truncate max-w-[120px]">{step.title}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl p-8 border border-slate-200/80 dark:border-slate-800 shadow-xl max-w-3xl mx-auto">
              <div className="flex items-start justify-between gap-4 mb-4">
                <div className="flex items-center gap-3">
                  <div className="h-12 w-12 rounded-2xl bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 flex items-center justify-center font-black text-lg border border-red-200 dark:border-red-900/40">
                    {journeySteps[activeStep].number}
                  </div>
                  <div>
                    <span className="text-[10px] font-extrabold uppercase tracking-widest text-red-600 dark:text-red-400">
                      {journeySteps[activeStep].tag}
                    </span>
                    <h3 className="text-xl font-black text-slate-900 dark:text-white">
                      {journeySteps[activeStep].title}
                    </h3>
                  </div>
                </div>

                <div className="flex gap-1.5">
                  {journeySteps.map((_, i) => (
                    <button
                      key={i}
                      onClick={() => setActiveStep(i)}
                      className={`h-2.5 rounded-full transition-all ${
                        activeStep === i ? 'w-8 bg-red-600' : 'w-2.5 bg-slate-200 dark:bg-slate-700'
                      }`}
                      aria-label={`Go to step ${i + 1}`}
                    />
                  ))}
                </div>
              </div>

              <p className="text-sm font-semibold text-slate-800 dark:text-slate-200 mb-3">
                {journeySteps[activeStep].desc}
              </p>

              <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed bg-slate-50 dark:bg-slate-800/60 p-4 rounded-2xl border border-slate-100 dark:border-slate-700/60">
                💡 <span className="font-bold text-slate-700 dark:text-slate-300">Technical Details:</span> {journeySteps[activeStep].detail}
              </p>
            </div>

          </div>
        </section>

        {/* ─────────────────────────────────────────────
            4. FOR DONORS SECTION (MODULE 2)
        ───────────────────────────────────────────── */}
        <section id="for-donors" className="py-16 lg:py-24 bg-white dark:bg-slate-900 border-b border-slate-200/60 dark:border-slate-800/60">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
              
              <div>
                <span className="text-xs font-extrabold uppercase tracking-widest text-red-600 dark:text-red-400">
                  Donor Experience
                </span>
                <h2 className="text-3xl sm:text-4xl font-black text-slate-900 dark:text-white mt-2 mb-4">
                  Your blood group could be exactly what someone needs.
                </h2>
                <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed mb-6">
                  Register as a donor, keep your availability updated, and receive compatible emergency blood requests when someone needs your blood group.
                </p>

                <div className="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-2xl border border-slate-200 dark:border-slate-700 mb-8">
                  <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block mb-3">
                    Donor Lifesaving Journey
                  </span>
                  <div className="grid grid-cols-3 sm:grid-cols-6 gap-2 text-center text-[10px] font-bold">
                    <div className="p-2 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200">
                      1. Register
                    </div>
                    <div className="p-2 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200">
                      2. Availability
                    </div>
                    <div className="p-2 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-red-600 dark:text-red-400">
                      3. Match Alert
                    </div>
                    <div className="p-2 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200">
                      4. Respond
                    </div>
                    <div className="p-2 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200">
                      5. Donate
                    </div>
                    <div className="p-2 bg-slate-900 text-white rounded-xl border border-slate-800 font-black">
                      6. Impact ❤️
                    </div>
                  </div>
                </div>

                <Link to="/register/donor">
                  <Button variant="primary" className="px-7 py-3.5 text-xs font-black bg-red-600 hover:bg-red-700 text-white rounded-xl shadow-lg shadow-red-600/20">
                    Become a Donor →
                  </Button>
                </Link>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="bg-slate-50 dark:bg-slate-800/60 p-5 rounded-3xl border border-slate-200/80 dark:border-slate-700/60 flex flex-col gap-2">
                  <span className="text-[10px] font-bold text-slate-400 uppercase">Blood Group</span>
                  <span className="text-2xl font-black text-red-600">O+ Positive</span>
                  <span className="text-[11px] text-slate-500">Universal Red Cell Donor</span>
                </div>

                <div className="bg-slate-50 dark:bg-slate-800/60 p-5 rounded-3xl border border-slate-200/80 dark:border-slate-700/60 flex flex-col gap-2">
                  <span className="text-[10px] font-bold text-slate-400 uppercase">Status</span>
                  <span className="text-lg font-black text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                    <CheckCircle2 className="h-4 w-4" /> Ready to Donate
                  </span>
                  <span className="text-[11px] text-slate-500">0 Days Cooldown Remaining</span>
                </div>

                <div className="col-span-2 bg-slate-900 text-white p-5 rounded-3xl border border-slate-800 flex items-center justify-between">
                  <div>
                    <p className="text-xs font-extrabold text-slate-200">Official Donation Certificate</p>
                    <p className="text-[10px] text-slate-400 mt-0.5">Automated PDF download on completion</p>
                  </div>
                  <Award className="h-7 w-7 text-amber-400 shrink-0" />
                </div>
              </div>

            </div>
          </div>
        </section>

        {/* ─────────────────────────────────────────────
            5. FOR HOSPITALS SECTION (MODULE 2)
        ───────────────────────────────────────────── */}
        <section id="for-hospitals" className="py-16 lg:py-24 bg-slate-50 dark:bg-slate-950 border-b border-slate-200/60 dark:border-slate-800/60">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
              
              <div className="lg:col-span-7">
                <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200/80 dark:border-slate-800 shadow-2xl relative">
                  
                  <div className="flex items-center justify-between pb-4 mb-5 border-b border-slate-100 dark:border-slate-800">
                    <div className="flex items-center gap-2">
                      <span className="h-2.5 w-2.5 rounded-full bg-red-600 animate-ping" />
                      <span className="text-xs font-black text-slate-900 dark:text-white uppercase tracking-wider">
                        Product Visual Demonstration
                      </span>
                    </div>
                    <span className="text-[10px] font-bold text-slate-400 bg-slate-100 dark:bg-slate-800 px-2.5 py-1 rounded-full">
                      Static Demo UI
                    </span>
                  </div>

                  <div className="bg-slate-900 text-white rounded-2xl p-5 border border-slate-800 mb-6">
                    <div className="flex items-start justify-between gap-4 mb-3">
                      <div>
                        <span className="text-[10px] font-bold text-red-400 uppercase tracking-widest">
                          Emergency Blood Request
                        </span>
                        <h4 className="text-base font-black text-white mt-0.5">
                          SriSai Multi-speciality Hospital
                        </h4>
                      </div>
                      <span className="bg-red-500/20 text-red-400 text-[10px] font-black px-2.5 py-1 rounded-full border border-red-500/30 uppercase">
                        URGENCY: HIGH
                      </span>
                    </div>

                    <div className="grid grid-cols-2 gap-4 pt-3 border-t border-slate-800 text-xs">
                      <div>
                        <span className="text-[10px] text-slate-400 font-medium">Required Group</span>
                        <p className="text-lg font-black text-red-400">B+ Positive</p>
                      </div>
                      <div>
                        <span className="text-[10px] text-slate-400 font-medium">Units Needed</span>
                        <p className="text-lg font-black text-white">1 Unit</p>
                      </div>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4 text-xs">
                    <div className="p-4 bg-emerald-50 dark:bg-emerald-950/40 rounded-2xl border border-emerald-200 dark:border-emerald-800/60">
                      <span className="text-[10px] font-extrabold text-emerald-700 dark:text-emerald-300 uppercase">
                        Compatible Groups
                      </span>
                      <div className="flex gap-2 mt-2">
                        <span className="px-2 py-1 bg-emerald-100 dark:bg-emerald-900 text-emerald-800 dark:text-emerald-200 font-bold rounded">
                          B+ ✓
                        </span>
                        <span className="px-2 py-1 bg-emerald-100 dark:bg-emerald-900 text-emerald-800 dark:text-emerald-200 font-bold rounded">
                          O+ ✓
                        </span>
                      </div>
                    </div>

                    <div className="p-4 bg-red-50 dark:bg-red-950/40 rounded-2xl border border-red-200 dark:border-red-800/60">
                      <span className="text-[10px] font-extrabold text-red-700 dark:text-red-300 uppercase">
                        Not Compatible
                      </span>
                      <div className="flex gap-2 mt-2">
                        <span className="px-2 py-1 bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 font-bold rounded">
                          A+ ✕
                        </span>
                      </div>
                    </div>
                  </div>

                  <p className="text-[10px] text-slate-400 mt-4 text-center italic">
                    * Visual demonstration of BloodBridge emergency dispatch interface.
                  </p>
                </div>
              </div>

              <div className="lg:col-span-5">
                <span className="text-xs font-extrabold uppercase tracking-widest text-teal-600 dark:text-teal-400">
                  Hospital Portal
                </span>
                <h2 className="text-3xl sm:text-4xl font-black text-slate-900 dark:text-white mt-2 mb-4">
                  When every minute matters.
                </h2>
                <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed mb-6">
                  Hospitals can create emergency blood requests and connect with compatible available donors through BloodBridge.
                </p>

                <div className="bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 mb-8">
                  <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block mb-3">
                    Hospital Emergency Dispatch Workflow
                  </span>
                  <div className="flex flex-col gap-2 text-xs font-bold">
                    <div className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                      <span className="h-5 w-5 rounded-full bg-teal-100 text-teal-700 dark:bg-teal-950 dark:text-teal-300 text-[10px] font-black flex items-center justify-center shrink-0">1</span>
                      <span>Hospital Accreditation & Login</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                      <span className="h-5 w-5 rounded-full bg-teal-100 text-teal-700 dark:bg-teal-950 dark:text-teal-300 text-[10px] font-black flex items-center justify-center shrink-0">2</span>
                      <span>Create Urgent / Standard Request</span>
                    </div>
                    <div className="flex items-center gap-2 text-teal-600 dark:text-teal-400 font-extrabold">
                      <span className="h-5 w-5 rounded-full bg-teal-600 text-white text-[10px] font-black flex items-center justify-center shrink-0">3</span>
                      <span>Smart ABO/Rh Compatible Donor Filter</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                      <span className="h-5 w-5 rounded-full bg-teal-100 text-teal-700 dark:bg-teal-950 dark:text-teal-300 text-[10px] font-black flex items-center justify-center shrink-0">4</span>
                      <span>Donor Acceptance & Verification</span>
                    </div>
                    <div className="flex items-center gap-2 text-emerald-600 dark:text-emerald-400 font-extrabold">
                      <span className="h-5 w-5 rounded-full bg-emerald-600 text-white text-[10px] font-black flex items-center justify-center shrink-0">5</span>
                      <span>Donation Completion & Certificate Sign-Off</span>
                    </div>
                  </div>
                </div>

                <Link to="/register/hospital">
                  <Button variant="primary" className="px-7 py-3.5 text-xs font-black bg-teal-600 hover:bg-teal-700 text-white rounded-xl shadow-md">
                    Register Your Hospital →
                  </Button>
                </Link>
              </div>

            </div>
          </div>
        </section>

        {/* ─────────────────────────────────────────────
            6. INTERACTIVE BLOOD GROUP SELECTOR (MODULE 2)
        ───────────────────────────────────────────── */}
        <section id="compatibility" className="py-16 lg:py-24 bg-white dark:bg-slate-900 border-b border-slate-200/60 dark:border-slate-800/60">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-2xl mx-auto mb-12">
              <span className="text-xs font-extrabold uppercase tracking-widest text-red-600 dark:text-red-400">
                Medical Rules Engine
              </span>
              <h2 className="text-3xl sm:text-4xl font-black text-slate-900 dark:text-white mt-2">
                Blood Group Compatibility Interactive Tool
              </h2>
              <p className="text-sm text-slate-500 dark:text-slate-400 mt-2">
                Select a blood group to view authoritative medical transfusion compatibility rules.
              </p>
            </div>

            <div className="flex flex-wrap items-center justify-center gap-3 mb-10 max-w-3xl mx-auto">
              {allBloodGroups.map((bg) => {
                const isSelected = selectedGroup === bg;
                return (
                  <button
                    key={bg}
                    onClick={() => setSelectedGroup(bg)}
                    className={`px-5 py-3 rounded-2xl text-sm font-black transition-all ${
                      isSelected
                        ? 'bg-red-600 text-white shadow-xl shadow-red-600/30 ring-4 ring-red-500/20 scale-105'
                        : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700'
                    }`}
                  >
                    {bg}
                  </button>
                );
              })}
            </div>

            <div className="bg-slate-950 text-white rounded-3xl p-8 border border-slate-800 shadow-2xl max-w-4xl mx-auto">
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-800">
                <div>
                  <span className="text-[10px] font-extrabold uppercase tracking-widest text-red-400 bg-red-950 px-2.5 py-0.5 rounded border border-red-900">
                    Selected Group
                  </span>
                  <h3 className="text-3xl font-black text-white mt-1">
                    Blood Group {selectedGroup}
                  </h3>
                  <p className="text-xs text-slate-400 mt-1">{currentRule.description}</p>
                </div>
                <div className="px-4 py-2 bg-slate-900 rounded-xl border border-slate-800 text-xs font-bold text-amber-400">
                  Population Frequency: {currentRule.rarity}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800">
                  <div className="flex items-center gap-2 text-xs font-extrabold text-emerald-400 uppercase tracking-wider mb-3">
                    <CheckCircle2 className="h-4 w-4" /> Can Receive Red Cells From
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {currentRule.canReceiveFrom.map((g) => (
                      <span key={g} className="px-3 py-1.5 rounded-xl bg-emerald-950 text-emerald-300 border border-emerald-800 text-xs font-black">
                        {g} ✓
                      </span>
                    ))}
                  </div>
                </div>

                <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800">
                  <div className="flex items-center gap-2 text-xs font-extrabold text-blue-400 uppercase tracking-wider mb-3">
                    <Droplets className="h-4 w-4" /> Can Donate Red Cells To
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {currentRule.canGiveTo.map((g) => (
                      <span key={g} className="px-3 py-1.5 rounded-xl bg-blue-950 text-blue-300 border border-blue-800 text-xs font-black">
                        {g}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            </div>

          </div>
        </section>

        {/* ─────────────────────────────────────────────
            7. DONOR IMPACT LIFECYCLE (MODULE 2)
        ───────────────────────────────────────────── */}
        <section className="py-16 lg:py-24 bg-slate-50 dark:bg-slate-950 border-b border-slate-200/60 dark:border-slate-800/60">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center max-w-3xl">
            <span className="text-xs font-extrabold uppercase tracking-widest text-red-600 dark:text-red-400">
              Meaningful Contribution
            </span>
            <h2 className="text-3xl sm:text-4xl font-black text-slate-900 dark:text-white mt-2 mb-4">
              Every Donation Leaves an Impact.
            </h2>
            <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed mb-12">
              BloodBridge tracks the full life-cycle of your contribution safely without fabricated platform counters.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 text-left">
              <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex flex-col gap-2">
                <span className="text-xs font-black text-red-600">Step 1</span>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white">Donation Completed</h4>
                <p className="text-[11px] text-slate-500">Transfusion verified by hospital staff.</p>
              </div>

              <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex flex-col gap-2">
                <span className="text-xs font-black text-amber-500">Step 2</span>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white">Certificate Issued</h4>
                <p className="text-[11px] text-slate-500">Instant PDF download available in portal.</p>
              </div>

              <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex flex-col gap-2">
                <span className="text-xs font-black text-emerald-600">Step 3</span>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white">Impact Updated</h4>
                <p className="text-[11px] text-slate-500">Lives impacted stats incremented safely.</p>
              </div>

              <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex flex-col gap-2">
                <span className="text-xs font-black text-blue-600">Step 4</span>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white">Cooldown Tracked</h4>
                <p className="text-[11px] text-slate-500">Authoritative 90-day cooldown starts.</p>
              </div>
            </div>
          </div>
        </section>

        {/* ─────────────────────────────────────────────
            8. TRUST SECTION (MODULE 2)
        ───────────────────────────────────────────── */}
        <section id="about" className="py-16 lg:py-24 bg-white dark:bg-slate-900 border-b border-slate-200/60 dark:border-slate-800/60">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-2xl mx-auto mb-16">
              <span className="text-xs font-extrabold uppercase tracking-widest text-indigo-600 dark:text-indigo-400">
                System Capabilities
              </span>
              <h2 className="text-3xl sm:text-4xl font-black text-slate-900 dark:text-white mt-2">
                Platform Trust & Security Architecture
              </h2>
              <p className="text-sm text-slate-500 dark:text-slate-400 mt-2">
                BloodBridge operates strictly on verified institution accreditation and automated safety rules.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/60 flex flex-col gap-2">
                <Shield className="h-6 w-6 text-indigo-600 mb-1" />
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Hospital Approval</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                  Super-administrator oversight verifying medical institution credentials.
                </p>
              </div>

              <div className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/60 flex flex-col gap-2">
                <Lock className="h-6 w-6 text-blue-600 mb-1" />
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Secure Authentication</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                  BCrypt password hashing and JWT token authorization protect user accounts.
                </p>
              </div>

              <div className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/60 flex flex-col gap-2">
                <Heart className="h-6 w-6 text-red-600 mb-1" />
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Eligibility Safeguards</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                  Automated 90-day donation cooldown timer safeguarding donor safety.
                </p>
              </div>

              <div className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/60 flex flex-col gap-2">
                <Award className="h-6 w-6 text-amber-500 mb-1" />
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Official Certificates</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                  Official PDF donation certificates signed off directly by accredited hospital staff.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* ─────────────────────────────────────────────
            9. FINAL CALL TO ACTION (MODULE 2)
        ───────────────────────────────────────────── */}
        <section className="py-16 lg:py-24 bg-gradient-to-r from-red-600 via-rose-600 to-red-700 text-white relative overflow-hidden">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center max-w-3xl">
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight mb-4">
              Be the connection someone needs.
            </h2>

            <p className="text-sm sm:text-base text-red-100 mb-8 leading-relaxed font-medium max-w-2xl mx-auto">
              Join BloodBridge today as a donor or register your hospital to enable real-time emergency blood dispatch.
            </p>

            <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
              <Link to="/register/donor">
                <Button
                  variant="primary"
                  className="px-8 py-3.5 text-sm font-black bg-white text-red-600 hover:bg-red-50 shadow-2xl rounded-2xl"
                >
                  Become a Donor
                </Button>
              </Link>
              <Link to="/register/hospital">
                <Button
                  variant="outline"
                  className="px-8 py-3.5 text-sm font-black border-white/40 text-white hover:bg-white/10 rounded-2xl"
                >
                  Register Your Hospital
                </Button>
              </Link>
            </div>
          </div>
        </section>

      </main>

      {/* ─────────────────────────────────────────────
          10. FOOTER
      ───────────────────────────────────────────── */}
      <footer className="bg-slate-950 text-white border-t border-slate-800 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6 text-center md:text-left">
          
          <div className="flex flex-col items-center md:items-start gap-2">
            <BloodBridgeLogo size="md" />
            <p className="text-xs text-slate-400 max-w-sm mt-1">
              Connecting blood donors with hospitals when every second matters.
            </p>
          </div>

          <div className="flex flex-wrap justify-center gap-6 text-xs text-slate-400 font-bold">
            <Link to="/login/donor" className="hover:text-white transition">Donor Login</Link>
            <Link to="/login/hospital" className="hover:text-white transition">Hospital Login</Link>
            <Link to="/login/admin" className="hover:text-white transition">Admin Portal</Link>
            <Link to="/register/donor" className="hover:text-white transition">Register Donor</Link>
            <Link to="/register/hospital" className="hover:text-white transition">Register Hospital</Link>
          </div>

          <div className="text-xs text-slate-500">
            © 2026 BloodBridge Healthcare Platform. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}
