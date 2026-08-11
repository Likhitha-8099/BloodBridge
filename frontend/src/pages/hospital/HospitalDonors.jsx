import React, { useState, useEffect, useCallback } from 'react';
import hospitalService from '../../services/hospitalService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import HospitalStatusBadge from '../../components/hospital/common/HospitalStatusBadge';
import HospitalEmptyState from '../../components/hospital/common/HospitalEmptyState';
import { 
  Search, 
  RefreshCw, 
  ChevronLeft, 
  ChevronRight, 
  CheckCircle2, 
  Clock, 
  MapPin, 
  Phone, 
  Mail, 
  Droplet,
  ArrowUpDown,
  Filter,
  Users
} from 'lucide-react';

const BLOOD_GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

export const HospitalDonors = () => {
  const [donors, setDonors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Filter states
  const [search, setSearch] = useState('');
  const [bloodGroup, setBloodGroup] = useState('');
  const [city, setCity] = useState('');
  const [availableOnly, setAvailableOnly] = useState(false);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  
  // Pagination states
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchDonors = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const params = {
        page,
        size: pageSize,
        sortBy,
        sortDir,
      };
      if (search.trim()) params.search = search.trim();
      if (bloodGroup) params.bloodGroup = bloodGroup;
      if (city.trim()) params.city = city.trim();
      if (availableOnly) params.available = true;

      const response = await hospitalService.getDonors(params);
      
      const data = response?.data || response;
      if (data?.content) {
        setDonors(data.content);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      } else if (Array.isArray(data)) {
        setDonors(data);
        setTotalPages(1);
        setTotalElements(data.length);
      } else {
        setDonors([]);
        setTotalPages(0);
        setTotalElements(0);
      }
    } catch (err) {
      console.error('Error fetching hospital donors:', err);
      setError(err?.response?.data?.message || 'Failed to load registered donors directory.');
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, search, bloodGroup, city, availableOnly, sortBy, sortDir]);

  useEffect(() => {
    fetchDonors();
  }, [fetchDonors]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchDonors();
  };

  const handleResetFilters = () => {
    setSearch('');
    setBloodGroup('');
    setCity('');
    setAvailableOnly(false);
    setSortBy('createdAt');
    setSortDir('desc');
    setPage(0);
  };

  const toggleSort = (field) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0);
  };

  const formatBloodGroup = (bg) => {
    if (!bg) return '?';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans">
      <HospitalPageHeader
        title="Registered Donors Directory"
        subtitle="Manage accepted and registered community blood donors in your area."
        icon={Users}
        badge="Community Network"
        breadcrumbs={[{ label: 'Registered Donors' }]}
        action={
          <button
            onClick={fetchDonors}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        }
      />

      {/* Filter Bar Card */}
      <HospitalCard bodyClassName="p-4 sm:p-6 flex flex-col gap-4">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {/* Search Input */}
          <div className="relative flex items-center">
            <Search className="absolute left-3.5 h-4 w-4 text-slate-400 pointer-events-none" />
            <input
              type="text"
              placeholder="Name, email, or phone..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 rounded-xl text-xs bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all"
            />
          </div>

          {/* Blood Group Select */}
          <select
            value={bloodGroup}
            onChange={(e) => {
              setBloodGroup(e.target.value);
              setPage(0);
            }}
            className="w-full px-4 py-2.5 rounded-xl text-xs bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all"
          >
            <option value="">All Blood Groups</option>
            {BLOOD_GROUPS.map((bg) => (
              <option key={bg} value={bg}>
                Group {bg}
              </option>
            ))}
          </select>

          {/* City Filter */}
          <input
            type="text"
            placeholder="Filter by city..."
            value={city}
            onChange={(e) => setCity(e.target.value)}
            className="w-full px-4 py-2.5 rounded-xl text-xs bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all"
          />

          {/* Action Buttons */}
          <div className="flex items-center gap-2">
            <button
              type="submit"
              className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center justify-center gap-1.5"
            >
              <Filter className="h-3.5 w-3.5" />
              <span>Apply Filters</span>
            </button>
            <button
              type="button"
              onClick={handleResetFilters}
              className="px-3 py-2.5 bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white font-bold text-xs rounded-xl transition-all"
            >
              Reset
            </button>
          </div>
        </form>

        {/* Checkbox for Available Only */}
        <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800/80 text-xs">
          <label className="flex items-center gap-2 cursor-pointer text-slate-600 dark:text-slate-400 font-medium select-none">
            <input
              type="checkbox"
              checked={availableOnly}
              onChange={(e) => {
                setAvailableOnly(e.target.checked);
                setPage(0);
              }}
              className="rounded border-slate-300 dark:border-slate-700 text-teal-600 focus:ring-teal-500 h-4 w-4"
            />
            <span>Show Available Donors Only</span>
          </label>

          <span className="text-slate-400">
            Total Donors Found: <strong className="text-slate-900 dark:text-white">{totalElements}</strong>
          </span>
        </div>
      </HospitalCard>

      {/* Main Table / List View */}
      {loading ? (
        <LoadingSpinner />
      ) : error ? (
        <ErrorState message={error} onRetry={fetchDonors} />
      ) : donors.length > 0 ? (
        <HospitalCard bodyClassName="p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider border-b border-slate-100 dark:border-slate-800">
                <tr>
                  <th className="px-5 py-4 cursor-pointer" onClick={() => toggleSort('fullName')}>
                    <div className="flex items-center gap-1">
                      <span>Donor Info</span>
                      <ArrowUpDown className="h-3 w-3" />
                    </div>
                  </th>
                  <th className="px-5 py-4 cursor-pointer" onClick={() => toggleSort('bloodGroup')}>
                    <div className="flex items-center gap-1">
                      <span>Blood Group</span>
                      <ArrowUpDown className="h-3 w-3" />
                    </div>
                  </th>
                  <th className="px-5 py-4">Contact</th>
                  <th className="px-5 py-4 cursor-pointer" onClick={() => toggleSort('city')}>
                    <div className="flex items-center gap-1">
                      <span>City & State</span>
                      <ArrowUpDown className="h-3 w-3" />
                    </div>
                  </th>
                  <th className="px-5 py-4">Last Donation</th>
                  <th className="px-5 py-4">Availability</th>
                  <th className="px-5 py-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200">
                {donors.map((donor) => {
                  const name = donor.fullName || donor.name || 'Anonymous Donor';
                  const email = donor.email || '';
                  const phone = donor.phoneNumber || donor.phone || '';

                  return (
                    <tr key={donor.id || donor.donorId} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors">
                      <td className="px-5 py-4">
                        <div className="flex flex-col gap-0.5">
                          <span className="font-bold text-slate-900 dark:text-white text-sm">{name}</span>
                          <span className="text-[11px] text-slate-400 dark:text-slate-500">ID: #{donor.id || donor.donorId}</span>
                        </div>
                      </td>

                      <td className="px-5 py-4">
                        <span className="inline-flex items-center gap-1 font-black px-3 py-1 text-xs rounded-full bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 border border-red-100 dark:border-red-900/40">
                          <Droplet className="h-3 w-3 fill-current" />
                          {formatBloodGroup(donor.bloodGroup)}
                        </span>
                      </td>

                      <td className="px-5 py-4 text-slate-500 dark:text-slate-400">
                        <div className="flex flex-col gap-1 text-[11px]">
                          {email && (
                            <span className="flex items-center gap-1.5">
                              <Mail className="h-3 w-3 text-slate-400 shrink-0" />
                              <span>{email}</span>
                            </span>
                          )}
                          {phone && (
                            <span className="flex items-center gap-1.5 font-mono">
                              <Phone className="h-3 w-3 text-slate-400 shrink-0" />
                              <span>{phone}</span>
                            </span>
                          )}
                        </div>
                      </td>

                      <td className="px-5 py-4 text-slate-500 dark:text-slate-400 font-medium">
                        {donor.city || donor.state ? (
                          <span className="flex items-center gap-1.5">
                            <MapPin className="h-3.5 w-3.5 text-teal-600 dark:text-teal-400 shrink-0" />
                            <span>{[donor.city, donor.state].filter(Boolean).join(', ')}</span>
                          </span>
                        ) : (
                          'N/A'
                        )}
                      </td>

                      <td className="px-5 py-4 text-slate-500 dark:text-slate-400 font-medium">
                        {donor.lastDonationDate
                          ? new Date(donor.lastDonationDate).toLocaleDateString(undefined, {
                              year: 'numeric',
                              month: 'short',
                              day: 'numeric',
                            })
                          : 'Never Donated'}
                      </td>

                      <td className="px-5 py-4">
                        {donor.availableForDonation ? (
                          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                            <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600 shrink-0" />
                            <span>Available</span>
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-amber-50 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800">
                            <Clock className="h-3.5 w-3.5 text-amber-600 shrink-0" />
                            <span>Unavailable</span>
                          </span>
                        )}
                      </td>

                      <td className="px-5 py-4">
                        <HospitalStatusBadge status={donor.verificationStatus || 'APPROVED'} />
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination Footer */}
          {totalPages > 1 && (
            <div className="p-4 bg-slate-50 dark:bg-slate-800/40 border-t border-slate-100 dark:border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500 dark:text-slate-400 font-medium">
              <div>
                Showing <strong className="text-slate-900 dark:text-white">{donors.length}</strong> of{' '}
                <strong className="text-slate-900 dark:text-white">{totalElements}</strong> registered donors
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setPage((prev) => Math.max(0, prev - 1))}
                  disabled={page === 0 || loading}
                  className="p-2 border border-slate-200 dark:border-slate-700 rounded-xl hover:bg-white dark:hover:bg-slate-800 disabled:opacity-40 transition-colors"
                >
                  <ChevronLeft className="h-4 w-4" />
                </button>
                <span className="px-2 font-bold text-slate-900 dark:text-white">
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  onClick={() => setPage((prev) => Math.min(totalPages - 1, prev + 1))}
                  disabled={page >= totalPages - 1 || loading}
                  className="p-2 border border-slate-200 dark:border-slate-700 rounded-xl hover:bg-white dark:hover:bg-slate-800 disabled:opacity-40 transition-colors"
                >
                  <ChevronRight className="h-4 w-4" />
                </button>
              </div>
            </div>
          )}
        </HospitalCard>
      ) : (
        <HospitalEmptyState
          title="No Donors Registered"
          description="There are currently no registered donors matching your search parameters."
          icon={Users}
          action={
            <button
              onClick={handleResetFilters}
              className="px-4 py-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 text-slate-700 dark:text-slate-200 font-bold text-xs rounded-xl transition-colors"
            >
              Reset Search Filters
            </button>
          }
        />
      )}
    </div>
  );
};

export default HospitalDonors;
