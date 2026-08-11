import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useMatches } from '../../hooks/useMatches';
import { useRequestDetails } from '../../hooks/useRequestDetails';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import HospitalStatusBadge from '../../components/hospital/common/HospitalStatusBadge';
import HospitalEmptyState from '../../components/hospital/common/HospitalEmptyState';
import { 
  Heart, 
  Activity, 
  ArrowLeft, 
  Send, 
  ShieldAlert, 
  Droplet, 
  MapPin, 
  Sparkles 
} from 'lucide-react';

/**
 * Smart Blood Matching Engine View for Hospital Module.
 * Modern healthcare portal design preserving 100% of existing matching algorithms, hooks, and dispatch APIs.
 */
export default function DonorMatches() {
  const [searchParams] = useSearchParams();
  const requestId = searchParams.get('requestId');
  const navigate = useNavigate();

  const { data: request } = useRequestDetails(requestId);
  const {
    rankedDonors,
    isRankedLoading,
    rankedError,
    results,
    isResultsLoading,
    resultsError,
    generateMatches,
    isGenerating,
  } = useMatches(requestId);

  const [activeTab, setActiveTab] = useState('candidates'); // 'candidates' | 'generated'

  if (!requestId) {
    return (
      <div className="max-w-2xl mx-auto py-12 font-sans">
        <HospitalCard className="text-center p-8 sm:p-12">
          <div className="flex flex-col items-center gap-4">
            <div className="h-16 w-16 rounded-3xl bg-amber-50 dark:bg-amber-950/60 text-amber-600 dark:text-amber-400 flex items-center justify-center border border-amber-100 dark:border-amber-900/40">
              <ShieldAlert className="h-8 w-8" />
            </div>
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">No Request Selected</h2>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 max-w-md">
              Please select a verified blood request from your request list to execute the matching engine algorithm.
            </p>
            <button
              onClick={() => navigate('/hospital/requests')}
              className="mt-2 px-5 py-2.5 rounded-2xl bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs shadow-md transition-all"
            >
              View Active Blood Requests
            </button>
          </div>
        </HospitalCard>
      </div>
    );
  }

  const handleGenerate = async () => {
    try {
      await generateMatches();
      setActiveTab('generated');
    } catch (err) {
      alert(err.message || 'Failed to generate matches.');
    }
  };

  const formatBloodGroup = (bg) => {
    if (!bg) return '?';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  const isMatchesGenerated = results && results.length > 0;
  const sortedCandidates = rankedDonors 
    ? [...rankedDonors].sort((a, b) => b.compatibilityScore - a.compatibilityScore) 
    : [];

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans">
      <HospitalPageHeader
        title="Smart Donor Matching Engine"
        subtitle={
          request
            ? `Request #${request.id} • Group ${formatBloodGroup(request.bloodGroupNeeded)} Needed (${request.unitsRequired} Units)`
            : 'Evaluating compatible registered donor candidates...'
        }
        icon={Activity}
        badge="AI Matching Engine"
        breadcrumbs={[
          { label: 'Blood Requests', to: '/hospital/requests' },
          { label: `Matches #${requestId}` }
        ]}
        action={
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate(`/hospital/requests/${requestId}`)}
              className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
            >
              <ArrowLeft className="h-4 w-4" />
              <span>Back to Request</span>
            </button>

            {!isMatchesGenerated && sortedCandidates.length > 0 && (
              <button
                onClick={handleGenerate}
                disabled={isGenerating}
                className="flex items-center gap-2 px-5 py-2.5 rounded-2xl bg-gradient-to-r from-teal-600 to-emerald-600 text-white font-bold text-xs shadow-lg shadow-teal-500/20 hover:shadow-teal-500/35 transition-all transform hover:-translate-y-0.5"
              >
                <Send className="h-4 w-4" />
                <span>{isGenerating ? 'Dispatching...' : 'Dispatch Matches'}</span>
              </button>
            )}
          </div>
        }
      />

      {/* Tabs Bar */}
      <HospitalCard bodyClassName="p-4 sm:p-6 flex items-center justify-between gap-4">
        <div className="flex items-center gap-2 bg-slate-100 dark:bg-slate-800 p-1.5 rounded-2xl border border-slate-200/60 dark:border-slate-700/60">
          <button
            onClick={() => setActiveTab('candidates')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'candidates'
                ? 'bg-white dark:bg-slate-900 text-teal-600 dark:text-teal-400 shadow-sm'
                : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            Ranked Candidates ({sortedCandidates.length})
          </button>

          <button
            onClick={() => setActiveTab('generated')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'generated'
                ? 'bg-white dark:bg-slate-900 text-teal-600 dark:text-teal-400 shadow-sm'
                : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            Active Match Runs ({results ? results.length : 0})
          </button>
        </div>
      </HospitalCard>

      {/* Main Content View */}
      {isRankedLoading || isResultsLoading ? (
        <LoadingSpinner />
      ) : rankedError || resultsError ? (
        <ErrorState message="Failed to load matching engine records." />
      ) : activeTab === 'candidates' ? (
        sortedCandidates.length > 0 ? (
          <HospitalCard bodyClassName="p-0 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider border-b border-slate-100 dark:border-slate-800">
                  <tr>
                    <th className="px-5 py-4">Donor Name</th>
                    <th className="px-5 py-4">Blood Group</th>
                    <th className="px-5 py-4">Compatibility Score</th>
                    <th className="px-5 py-4">Location</th>
                    <th className="px-5 py-4">Total Donations</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200">
                  {sortedCandidates.map((row) => (
                    <tr key={row.donorId || row.id} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors">
                      <td className="px-5 py-4 font-bold text-slate-900 dark:text-white">
                        {row.donorName || 'Registered Donor'}
                      </td>
                      <td className="px-5 py-4">
                        <span className="inline-flex items-center gap-1 font-black px-3 py-1 text-xs rounded-full bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 border border-red-100 dark:border-red-900/40">
                          <Droplet className="h-3 w-3 fill-current" />
                          {formatBloodGroup(row.bloodGroup)}
                        </span>
                      </td>
                      <td className="px-5 py-4">
                        <span className="inline-flex items-center gap-1 font-black text-sm text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60 px-2.5 py-0.5 rounded-full border border-emerald-200 dark:border-emerald-800">
                          <Sparkles className="h-3.5 w-3.5" />
                          {row.compatibilityScore}% Match
                        </span>
                      </td>
                      <td className="px-5 py-4 text-slate-500 dark:text-slate-400 font-medium">
                        <span className="flex items-center gap-1.5">
                          <MapPin className="h-3.5 w-3.5 text-teal-600 dark:text-teal-400 shrink-0" />
                          {row.city || row.location || 'Local'}, {row.state || ''}
                        </span>
                      </td>
                      <td className="px-5 py-4 font-bold text-slate-900 dark:text-white">
                        {row.totalDonations || 0} Donations
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </HospitalCard>
        ) : (
          <HospitalEmptyState
            title="No Compatible Donors Found"
            description="No registered donors in the vicinity match the required blood group criteria."
            icon={Heart}
          />
        )
      ) : isMatchesGenerated ? (
        <HospitalCard bodyClassName="p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider border-b border-slate-100 dark:border-slate-800">
                <tr>
                  <th className="px-5 py-4">Match ID</th>
                  <th className="px-5 py-4">Donor Name</th>
                  <th className="px-5 py-4">Blood Group</th>
                  <th className="px-5 py-4">Compatibility</th>
                  <th className="px-5 py-4">City</th>
                  <th className="px-5 py-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200">
                {results.map((row) => (
                  <tr key={row.id} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors">
                    <td className="px-5 py-4 font-mono font-bold text-slate-900 dark:text-white">
                      #{row.id}
                    </td>
                    <td className="px-5 py-4 font-bold text-slate-900 dark:text-white">
                      {row.donorName}
                    </td>
                    <td className="px-5 py-4">
                      <span className="inline-flex items-center gap-1 font-black px-3 py-1 text-xs rounded-full bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 border border-red-100 dark:border-red-900/40">
                        <Droplet className="h-3 w-3 fill-current" />
                        {formatBloodGroup(row.donorBloodGroup)}
                      </span>
                    </td>
                    <td className="px-5 py-4 font-extrabold text-slate-900 dark:text-white">
                      {row.compatibilityScore}%
                    </td>
                    <td className="px-5 py-4 text-slate-500 dark:text-slate-400 font-medium">
                      {row.donorCity || 'N/A'}
                    </td>
                    <td className="px-5 py-4">
                      <HospitalStatusBadge status={row.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </HospitalCard>
      ) : (
        <HospitalEmptyState
          title="No Match Run Dispatched"
          description="Generate and dispatch persistent matching records to contact compatible candidates via SMS and push notifications."
          icon={Activity}
          action={
            sortedCandidates.length > 0 && (
              <button
                onClick={handleGenerate}
                disabled={isGenerating}
                className="px-5 py-2.5 rounded-2xl bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs shadow-md transition-all flex items-center gap-2"
              >
                <Send className="h-4 w-4" />
                <span>{isGenerating ? 'Dispatching...' : 'Generate & Dispatch Now'}</span>
              </button>
            )
          }
        />
      )}
    </div>
  );
}
