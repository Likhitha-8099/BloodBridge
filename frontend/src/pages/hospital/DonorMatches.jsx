import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useMatches } from '../../hooks/useMatches';
import { useRequestDetails } from '../../hooks/useRequestDetails';
import Card from '../../components/ui/Card';
import DataTable from '../../components/ui/DataTable';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import { Heart, Activity, ArrowLeft, Send, ShieldAlert } from 'lucide-react';

/**
 * Screen running the donor matching algorithm, sorting by compatibility scores, and showing active dispatch runs.
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

  const [activeTab, setActiveTab] = useState('candidates'); // 'candidates' or 'generated'

  if (!requestId) {
    return (
      <div className="max-w-2xl mx-auto py-8">
        <Card className="flex flex-col items-center justify-center text-center p-12 gap-4 border border-dashed rounded-3xl">
          <ShieldAlert className="h-10 w-10 text-primary" />
          <h2 className="text-lg font-bold text-gray-800">No Request ID Specified</h2>
          <p className="text-xs text-gray-500">
            Please run the matching engine from the details page of a verified blood request.
          </p>
          <Button onClick={() => navigate('/hospital/requests')} variant="outline" className="text-xs">
            View Requests
          </Button>
        </Card>
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

  const isMatchesGenerated = results && results.length > 0;

  const candidateColumns = [
    {
      header: 'Donor Name',
      field: 'donorName',
    },
    {
      header: 'Blood Group',
      render: (row) => (
        <span className="inline-flex items-center justify-center font-extrabold px-2.5 py-1 text-xs rounded-full bg-red-50 text-primary border border-red-100">
          {row.bloodGroup ? row.bloodGroup.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : '?'}
        </span>
      ),
    },
    {
      header: 'Compatibility Score',
      render: (row) => (
        <span className="font-extrabold text-green-600 text-sm">
          {row.compatibilityScore}%
        </span>
      ),
    },
    {
      header: 'Location',
      render: (row) => <span>{row.city}, {row.state}</span>,
    },
    {
      header: 'Total Donations',
      field: 'totalDonations',
    },
  ];

  const generatedColumns = [
    {
      header: 'Match ID',
      field: 'id',
    },
    {
      header: 'Donor Name',
      field: 'donorName',
    },
    {
      header: 'Blood Group',
      render: (row) => (
        <span className="inline-flex items-center justify-center font-extrabold px-2.5 py-1 text-xs rounded-full bg-red-50 text-primary border border-red-100">
          {row.donorBloodGroup ? row.donorBloodGroup.replace('_POSITIVE', '+').replace('_NEGATIVE', '-') : '?'}
        </span>
      ),
    },
    {
      header: 'Compatibility Score',
      render: (row) => <span className="font-bold text-gray-805">{row.compatibilityScore}%</span>,
    },
    {
      header: 'City',
      field: 'donorCity',
    },
    {
      header: 'Status',
      render: (row) => {
        const colors = {
          PENDING: 'bg-yellow-50 text-yellow-750 border-yellow-200',
          ACCEPTED: 'bg-green-50 text-green-750 border-green-200',
          REJECTED: 'bg-red-50 text-red-750 border-red-200',
        };
        const style = colors[row.status] || 'bg-gray-50 text-gray-655';
        return (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold border uppercase tracking-wider ${style}`}>
            {row.status}
          </span>
        );
      },
    },
  ];

  // Sort candidates by compatibility score descending
  const sortedCandidates = rankedDonors 
    ? [...rankedDonors].sort((a, b) => b.compatibilityScore - a.compatibilityScore) 
    : [];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(`/hospital/requests/${requestId}`)}
            className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded-xl shadow-sm transition-all"
          >
            <ArrowLeft className="h-4 w-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-gray-900">Blood Matching Engine</h1>
            <p className="text-xs text-gray-500 mt-0.5">
              {request 
                ? `Request #${request.id} - Group ${request.bloodGroupNeeded?.replace('_POSITIVE', '+').replace('_NEGATIVE', '-')} Needed (${request.unitsRequired} Bags)` 
                : 'Loading request context...'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {!isMatchesGenerated && sortedCandidates.length > 0 && (
            <Button
              onClick={handleGenerate}
              isLoading={isGenerating}
              variant="primary"
              className="flex items-center gap-2 text-xs py-2"
            >
              <Send className="h-4 w-4" /> Dispatch Matches
            </Button>
          )}
        </div>
      </div>

      {/* Tabs list */}
      <div className="flex items-center border-b border-gray-100 gap-6">
        <button
          onClick={() => setActiveTab('candidates')}
          className={`pb-3 text-sm font-semibold transition-all border-b-2 ${
            activeTab === 'candidates'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-400 hover:text-gray-600'
          }`}
        >
          Ranked Candidates ({sortedCandidates.length})
        </button>
        <button
          onClick={() => setActiveTab('generated')}
          className={`pb-3 text-sm font-semibold transition-all border-b-2 ${
            activeTab === 'generated'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-400 hover:text-gray-600'
          }`}
        >
          Active Match Run ({results ? results.length : 0})
        </button>
      </div>

      {isRankedLoading || isResultsLoading ? (
        <LoadingSpinner />
      ) : rankedError || resultsError ? (
        <ErrorState message="Failed to load matching engine records." />
      ) : activeTab === 'candidates' ? (
        sortedCandidates.length > 0 ? (
          <DataTable
            columns={candidateColumns}
            data={sortedCandidates}
            keyField="donorId"
            emptyMessage="No compatible donors found."
          />
        ) : (
          <EmptyState
            message="No compatible donors registered in the database."
            icon={Heart}
          />
        )
      ) : (
        isMatchesGenerated ? (
          <DataTable
            columns={generatedColumns}
            data={results}
            keyField="id"
            emptyMessage="No match runs recorded."
          />
        ) : (
          <div className="flex flex-col items-center justify-center p-12 text-center border border-dashed rounded-3xl gap-4 bg-white">
            <Activity className="h-10 w-10 text-gray-400" />
            <div>
              <h4 className="font-bold text-gray-800 text-sm">No Match Run Generated</h4>
              <p className="text-xs text-gray-400 max-w-sm mt-1">
                Generate and persist matching records to contact candidates via SMS/Email notifications.
              </p>
            </div>
            {sortedCandidates.length > 0 && (
              <Button 
                onClick={handleGenerate} 
                isLoading={isGenerating} 
                variant="primary" 
                className="text-xs py-2"
              >
                Generate and Dispatch Now
              </Button>
            )}
          </div>
        )
      )}
    </div>
  );
}
