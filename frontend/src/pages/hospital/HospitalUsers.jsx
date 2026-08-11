import React, { useState, useEffect, useCallback } from 'react';
import hospitalService from '../../services/hospitalService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import HospitalPageHeader from '../../components/hospital/common/HospitalPageHeader';
import HospitalCard from '../../components/hospital/common/HospitalCard';
import HospitalEmptyState from '../../components/hospital/common/HospitalEmptyState';
import { 
  Search, 
  Users, 
  RefreshCw, 
  ChevronLeft, 
  ChevronRight, 
  UserCheck, 
  MapPin, 
  Phone, 
  Mail, 
  Droplet,
  Filter
} from 'lucide-react';

const BLOOD_GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

export const HospitalUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Filter states
  const [search, setSearch] = useState('');
  const [bloodGroup, setBloodGroup] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  
  // Pagination states
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const params = {
        page,
        size: pageSize,
      };
      if (search.trim()) params.search = search.trim();
      if (bloodGroup) params.bloodGroup = bloodGroup;
      if (city.trim()) params.city = city.trim();
      if (state.trim()) params.state = state.trim();

      const response = await hospitalService.getUsers(params);
      
      const data = response?.data || response;
      if (data?.content) {
        setUsers(data.content);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      } else if (Array.isArray(data)) {
        setUsers(data);
        setTotalPages(1);
        setTotalElements(data.length);
      } else {
        setUsers([]);
        setTotalPages(0);
        setTotalElements(0);
      }
    } catch (err) {
      console.error('Error fetching hospital users:', err);
      setError(err?.response?.data?.message || 'Failed to load users directory.');
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, search, bloodGroup, city, state]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchUsers();
  };

  const handleResetFilters = () => {
    setSearch('');
    setBloodGroup('');
    setCity('');
    setState('');
    setPage(0);
  };

  const formatBloodGroup = (bg) => {
    if (!bg) return '?';
    return bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
  };

  return (
    <div className="flex flex-col gap-6 pb-12 font-sans">
      <HospitalPageHeader
        title="Staff & Patient Users Directory"
        subtitle="Manage hospital staff, medical officers, and associated patient records."
        icon={Users}
        badge="Directory Management"
        breadcrumbs={[{ label: 'Users Directory' }]}
        action={
          <button
            onClick={fetchUsers}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs shadow-xs hover:bg-slate-50 transition-all"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        }
      />

      {/* Filter Bar */}
      <HospitalCard bodyClassName="p-4 sm:p-6 flex flex-col gap-4">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
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

          <input
            type="text"
            placeholder="City..."
            value={city}
            onChange={(e) => setCity(e.target.value)}
            className="w-full px-4 py-2.5 rounded-xl text-xs bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 border border-slate-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all"
          />

          <div className="flex items-center gap-2">
            <button
              type="submit"
              className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center justify-center gap-1.5"
            >
              <Filter className="h-3.5 w-3.5" />
              <span>Apply</span>
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

        <div className="pt-2 border-t border-slate-100 dark:border-slate-800 text-xs text-slate-400 text-right">
          Total Users Found: <strong className="text-slate-900 dark:text-white">{totalElements}</strong>
        </div>
      </HospitalCard>

      {/* Main Table / Directory List */}
      {loading ? (
        <LoadingSpinner />
      ) : error ? (
        <ErrorState message={error} onRetry={fetchUsers} />
      ) : users.length > 0 ? (
        <HospitalCard bodyClassName="p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 font-bold uppercase tracking-wider border-b border-slate-100 dark:border-slate-800">
                <tr>
                  <th className="px-5 py-4">User Info</th>
                  <th className="px-5 py-4">Role</th>
                  <th className="px-5 py-4">Blood Group</th>
                  <th className="px-5 py-4">Contact</th>
                  <th className="px-5 py-4">Location</th>
                  <th className="px-5 py-4">Joined Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200">
                {users.map((user) => {
                  const name = user.fullName || user.name || 'Registered User';
                  const email = user.email || '';
                  const phone = user.phoneNumber || user.phone || '';

                  return (
                    <tr key={user.id} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors">
                      <td className="px-5 py-4">
                        <div className="flex flex-col gap-0.5">
                          <span className="font-bold text-slate-900 dark:text-white text-sm">{name}</span>
                          <span className="text-[11px] text-slate-400 dark:text-slate-500">ID: #{user.id}</span>
                        </div>
                      </td>

                      <td className="px-5 py-4">
                        <span className="inline-flex items-center gap-1 font-bold text-[10px] uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-teal-50 dark:bg-teal-950/60 text-teal-700 dark:text-teal-300 border border-teal-200 dark:border-teal-800">
                          <UserCheck className="h-3 w-3" />
                          {user.role || 'USER'}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        {user.bloodGroup ? (
                          <span className="inline-flex items-center gap-1 font-black px-3 py-1 text-xs rounded-full bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400 border border-red-100 dark:border-red-900/40">
                            <Droplet className="h-3 w-3 fill-current" />
                            {formatBloodGroup(user.bloodGroup)}
                          </span>
                        ) : (
                          <span className="text-slate-400 dark:text-slate-500 font-medium">N/A</span>
                        )}
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
                        {user.city || user.state ? (
                          <span className="flex items-center gap-1.5">
                            <MapPin className="h-3.5 w-3.5 text-teal-600 dark:text-teal-400 shrink-0" />
                            <span>{[user.city, user.state].filter(Boolean).join(', ')}</span>
                          </span>
                        ) : (
                          'N/A'
                        )}
                      </td>

                      <td className="px-5 py-4 text-slate-500 dark:text-slate-400 font-medium">
                        {user.createdAt
                          ? new Date(user.createdAt).toLocaleDateString(undefined, {
                              year: 'numeric',
                              month: 'short',
                              day: 'numeric',
                            })
                          : 'N/A'}
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
                Showing <strong className="text-slate-900 dark:text-white">{users.length}</strong> of{' '}
                <strong className="text-slate-900 dark:text-white">{totalElements}</strong> registered users
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
          title="No Users Registered"
          description="There are currently no users matching your filter parameters."
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

export default HospitalUsers;
