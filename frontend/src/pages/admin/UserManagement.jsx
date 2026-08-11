import React, { useState, useMemo } from 'react';
import { useUserStatistics, useTopDonors } from '../../hooks/useAdminStatistics';
import { useWebSocket } from '../../hooks/useWebSocket';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import PieChartCard from '../../components/ui/PieChartCard';
import BarChartCard from '../../components/ui/BarChartCard';
import LineChartCard from '../../components/ui/LineChartCard';
import DataTable from '../../components/ui/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import Button from '../../components/ui/Button';
import { 
  Users, User, Heart, Award, Wifi, WifiOff, Download, 
  Search, ShieldCheck, Zap, Lightbulb, MapPin, Activity
} from 'lucide-react';

/**
 * Complete Admin User Demographics & Real-time Platform Analytics Dashboard.
 * Integrates live STOMP subscriptions, real-time database aggregations,
 * multi-dimensional charts (Roles, Blood Groups, Availability, Locations, Age, Trends),
 * automated data insights, and CSV export.
 */
export default function UserManagement() {
  const { data: stats, isLoading: isStatsLoading, error: statsError, refetch: refetchStats } = useUserStatistics();
  const { data: topDonors, isLoading: isDonorsLoading, error: donorsError, refetch: refetchDonors } = useTopDonors();

  const userTopics = useMemo(() => ['/topic/admin/users', '/topic/admin/dashboard'], []);

  const { isConnected, isFallback } = useWebSocket(userTopics, () => {
    console.log('⚡ Real-time User Management event received, refreshing user stats...');
    refetchStats();
    refetchDonors();
  });

  // Table Filters State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedRole, setSelectedRole] = useState('ALL');
  const [selectedBloodGroup, setSelectedBloodGroup] = useState('ALL');

  const isLoading = isStatsLoading || isDonorsLoading;
  const error = statsError || donorsError;

  // Unwrapped stats payload
  const s = useMemo(() => {
    if (!stats) return null;
    return stats.data !== undefined ? stats.data : stats;
  }, [stats]);

  // Process Blood Group Data
  const bloodGroupData = useMemo(() => {
    if (!s?.bloodGroupDistribution) return [];
    return Object.entries(s.bloodGroupDistribution).map(([bg, count]) => ({
      group: bg.replace('_POSITIVE', '+').replace('_NEGATIVE', '-'),
      Donors: count
    })).filter(item => item.Donors > 0);
  }, [s]);

  // Process User Role Data
  const roleData = useMemo(() => {
    if (!s) return [];
    if (s.roleDistribution && Object.keys(s.roleDistribution).length > 0) {
      return Object.entries(s.roleDistribution).map(([role, count]) => ({
        name: role.charAt(0) + role.slice(1).toLowerCase(),
        value: count
      })).filter(r => r.value > 0);
    }
    return [
      { name: 'Donors', value: s.totalDonors || 0 },
      { name: 'Patients', value: s.totalPatients || 0 },
      { name: 'Hospitals', value: s.totalHospitals || 0 },
    ].filter(r => r.value > 0);
  }, [s]);

  // Process Availability Data
  const availabilityData = useMemo(() => {
    if (!s?.availabilityDistribution) return [];
    return Object.entries(s.availabilityDistribution).map(([label, count]) => ({
      name: label,
      value: count
    })).filter(a => a.value > 0);
  }, [s]);

  // Process Location City Data
  const locationCityData = useMemo(() => {
    if (!s?.locationCityDistribution) return [];
    return Object.entries(s.locationCityDistribution).map(([city, count]) => ({
      city: city,
      Donors: count
    })).filter(c => c.Donors > 0);
  }, [s]);

  // Process Age Group Data
  const ageGroupData = useMemo(() => {
    if (!s?.ageGroupDistribution) return [];
    return Object.entries(s.ageGroupDistribution).map(([ageGroup, count]) => ({
      ageGroup: ageGroup,
      Donors: count
    })).filter(a => a.Donors > 0);
  }, [s]);

  // Process Gender Data
  const genderData = useMemo(() => {
    if (!s?.genderDistribution) return [];
    return Object.entries(s.genderDistribution).map(([gender, count]) => ({
      name: gender.charAt(0) + gender.slice(1).toLowerCase(),
      value: count
    })).filter(g => g.value > 0);
  }, [s]);

  // Process Monthly Registration Trends
  const trendData = useMemo(() => {
    if (!s?.monthlyRegistrationTrends) return [];
    return s.monthlyRegistrationTrends;
  }, [s]);

  // Process Donor List for Table
  const donorList = useMemo(() => {
    return Array.isArray(topDonors) ? topDonors : (topDonors?.data || []);
  }, [topDonors]);

  // Filtered Donor List for Table
  const filteredDonors = useMemo(() => {
    return donorList.filter(row => {
      const searchLower = searchQuery.toLowerCase();
      const matchesSearch = !searchQuery || 
        (row.fullName && row.fullName.toLowerCase().includes(searchLower)) ||
        (row.email && row.email.toLowerCase().includes(searchLower)) ||
        (row.city && row.city.toLowerCase().includes(searchLower));

      const rowBg = (row.bloodGroup || '').replace('_POSITIVE', '+').replace('_NEGATIVE', '-');
      const matchesBg = selectedBloodGroup === 'ALL' || rowBg === selectedBloodGroup || row.bloodGroup === selectedBloodGroup;

      const matchesRole = selectedRole === 'ALL' || (row.role || 'DONOR').toUpperCase() === selectedRole.toUpperCase();

      return matchesSearch && matchesBg && matchesRole;
    });
  }, [donorList, searchQuery, selectedBloodGroup, selectedRole]);

  // CSV Export Handler
  const handleExportCSV = () => {
    if (!filteredDonors || filteredDonors.length === 0) return;
    const headers = ['Donor Name', 'Email', 'Role', 'Blood Group', 'City', 'State', 'Donations Count'];
    const rows = filteredDonors.map(row => [
      `"${row.fullName || row.name || ''}"`,
      `"${row.email || ''}"`,
      `"${row.role || 'DONOR'}"`,
      `"${(row.bloodGroup || 'N/A').replace('_POSITIVE', '+').replace('_NEGATIVE', '-')}"`,
      `"${row.city || ''}"`,
      `"${row.state || ''}"`,
      row.donationCount ?? row.totalDonations ?? 0
    ]);
    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `user_demographics_export_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message || "Failed to load user demographics analytics."} onRetry={() => { refetchStats(); refetchDonors(); }} />;
  }

  const donorTableColumns = [
    {
      header: 'User / Donor Name',
      field: 'fullName',
      render: (row) => (
        <div className="flex flex-col">
          <span className="font-bold text-slate-800 dark:text-white">{row.fullName || row.name || 'Registered User'}</span>
          <span className="text-[11px] text-slate-400 font-mono">{row.email}</span>
        </div>
      )
    },
    {
      header: 'Blood Group',
      render: (row) => (
        <span className="px-2.5 py-0.5 rounded-full text-xs font-black bg-red-50 text-red-700 dark:bg-red-950 dark:text-red-300 border border-red-200 dark:border-red-800">
          {(row.bloodGroup || 'N/A').replace('_POSITIVE', '+').replace('_NEGATIVE', '-')}
        </span>
      )
    },
    {
      header: 'Role',
      render: (row) => (
        <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 uppercase">
          {row.role || 'DONOR'}
        </span>
      )
    },
    {
      header: 'City & State',
      render: (row) => (
        <span className="text-xs text-slate-600 dark:text-slate-400 flex items-center gap-1">
          <MapPin className="h-3 w-3 text-slate-400" />
          {row.city || 'N/A'}{row.state ? `, ${row.state}` : ''}
        </span>
      )
    },
    {
      header: 'Completed Donations',
      render: (row) => (
        <span className="font-extrabold text-emerald-600 dark:text-emerald-400 text-xs">
          {row.donationCount ?? row.totalDonations ?? 0} Donations
        </span>
      )
    }
  ];

  return (
    <div className="flex flex-col gap-6 font-sans">
      {/* HEADER BANNER */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight flex items-center gap-3">
            User Demographics & Platform Analytics
            {isConnected ? (
              <span className="flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400 font-semibold bg-emerald-50 dark:bg-emerald-950/50 px-2.5 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
                <Wifi className="h-3.5 w-3.5" /> STOMP Live
              </span>
            ) : (
              <span className="flex items-center gap-1.5 text-xs text-amber-600 dark:text-amber-400 font-semibold bg-amber-50 dark:bg-amber-950/50 px-2.5 py-1 rounded-full border border-amber-200 dark:border-amber-800">
                <WifiOff className="h-3.5 w-3.5" /> {isFallback ? 'REST Fallback' : 'Reconnecting...'}
              </span>
            )}
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Real-time database statistics on user roles, donor availability, blood group distributions, location geography, and registration trends.
          </p>
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={handleExportCSV}
          className="flex items-center gap-1.5 font-bold border-slate-300 dark:border-slate-700"
        >
          <Download className="h-4 w-4" /> Export CSV
        </Button>
      </div>

      {/* ROW 1: TOP SUMMARY STATS CARDS */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <StatCard
          title="Total Users"
          value={s?.totalUsers}
          icon={Users}
          iconColor="text-blue-500 bg-blue-50 dark:bg-blue-950/60"
        />
        <StatCard
          title="Total Donors"
          value={s?.totalDonors}
          icon={Heart}
          iconColor="text-red-500 bg-red-50 dark:bg-red-950/60"
        />
        <StatCard
          title="Total Patients"
          value={s?.totalPatients}
          icon={User}
          iconColor="text-indigo-500 bg-indigo-50 dark:bg-indigo-950/60"
        />
        <StatCard
          title="Total Hospitals"
          value={s?.totalHospitals}
          icon={Award}
          iconColor="text-teal-500 bg-teal-50 dark:bg-teal-950/60"
        />
        <StatCard
          title="Active Donors"
          value={s?.availableDonors ?? s?.activeUsers}
          icon={ShieldCheck}
          iconColor="text-emerald-500 bg-emerald-50 dark:bg-emerald-950/60"
        />
        <StatCard
          title="Emergency Donors"
          value={s?.emergencyAvailableDonors ?? 0}
          icon={Zap}
          iconColor="text-amber-500 bg-amber-50 dark:bg-amber-950/60"
        />
      </div>

      {/* ROW 2: USER ROLE & BLOOD GROUP DISTRIBUTION */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="User Role Distribution">
          {roleData.length > 0 ? (
            <PieChartCard title="Platform Account Types" data={roleData} />
          ) : (
            <div className="h-64 flex items-center justify-center text-xs text-slate-400">
              No user role distribution logged.
            </div>
          )}
        </Card>

        <Card title="Donor Blood Group Inventory Distribution">
          {bloodGroupData.length > 0 ? (
            <BarChartCard 
              title="Donors by Blood Group" 
              data={bloodGroupData} 
              xKey="group" 
              yKey="Donors" 
              name="Registered Donors" 
              color="#E11D48" 
            />
          ) : (
            <div className="h-64 flex items-center justify-center text-xs text-slate-400">
              No blood group records found.
            </div>
          )}
        </Card>
      </div>

      {/* ROW 3: DONOR AVAILABILITY & LOCATION GEOGRAPHY */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="Donor Availability & Readiness Status">
          {availabilityData.length > 0 ? (
            <PieChartCard title="Donation Status Ratios" data={availabilityData} />
          ) : (
            <div className="h-64 flex items-center justify-center text-xs text-slate-400">
              No availability status data.
            </div>
          )}
        </Card>

        <Card title="Top Donor Cities & Geographical Demographics">
          {locationCityData.length > 0 ? (
            <BarChartCard 
              title="Donors Count by City" 
              data={locationCityData} 
              xKey="city" 
              yKey="Donors" 
              name="Active Donors" 
              color="#0D9488" 
            />
          ) : (
            <div className="h-64 flex items-center justify-center text-xs text-slate-400">
              No geographical location data recorded.
            </div>
          )}
        </Card>
      </div>

      {/* ROW 4: AGE DEMOGRAPHICS & GENDER BREAKDOWN */}
      {(ageGroupData.length > 0 || genderData.length > 0) && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {ageGroupData.length > 0 && (
            <Card title="Donor Age Group Demographics">
              <BarChartCard 
                title="Donors Count by Age Group" 
                data={ageGroupData} 
                xKey="ageGroup" 
                yKey="Donors" 
                name="Donors" 
                color="#6366F1" 
              />
            </Card>
          )}

          {genderData.length > 0 && (
            <Card title="Donor Gender Distribution">
              <PieChartCard title="Gender Ratios" data={genderData} />
            </Card>
          )}
        </div>
      )}

      {/* ROW 5: MONTHLY REGISTRATION TRENDS & AUTOMATED INSIGHTS */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <Card title="12-Month User Registration Volume Trend">
            {trendData.length > 0 ? (
              <LineChartCard 
                title="Monthly Registration Growth" 
                data={trendData} 
                xKey="month" 
                yKey="Registrations" 
                name="New Users Joined" 
                color="#2563EB" 
              />
            ) : (
              <div className="h-64 flex items-center justify-center text-xs text-slate-400">
                No registration trend data available.
              </div>
            )}
          </Card>
        </div>

        <div className="lg:col-span-1">
          <Card title="Platform Automated Insights">
            <div className="flex flex-col gap-3">
              {s?.automatedInsights && s.automatedInsights.length > 0 ? (
                s.automatedInsights.map((insight, idx) => (
                  <div key={idx} className="p-3.5 bg-slate-50 dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/60 flex items-start gap-3">
                    <div className="p-2 bg-amber-50 dark:bg-amber-950/60 text-amber-600 rounded-xl shrink-0">
                      <Lightbulb className="h-4 w-4" />
                    </div>
                    <p className="text-xs text-slate-700 dark:text-slate-300 font-medium leading-relaxed">
                      {insight}
                    </p>
                  </div>
                ))
              ) : (
                <div className="p-4 text-xs text-slate-400 text-center">
                  Calculating real-time database insights...
                </div>
              )}

              <div className="p-3.5 bg-emerald-50 dark:bg-emerald-950/40 rounded-2xl border border-emerald-100 dark:border-emerald-900/30 flex items-start gap-3 mt-1">
                <div className="p-2 bg-emerald-100 dark:bg-emerald-900/60 text-emerald-600 rounded-xl shrink-0">
                  <Activity className="h-4 w-4" />
                </div>
                <div>
                  <h4 className="font-bold text-emerald-800 dark:text-emerald-300 text-xs">Live Database Connection</h4>
                  <p className="text-[11px] text-emerald-700 dark:text-emerald-400 mt-0.5">
                    Demographic aggregations compiled directly from MySQL DB tables.
                  </p>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>

      {/* ROW 6: DETAILED USER & DONOR REGISTRY TABLE */}
      <Card title="User & Donor Registry Directory">
        {/* SEARCH & FILTER BAR */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
          <div className="relative">
            <Search className="h-4 w-4 absolute left-3.5 top-3 text-slate-400" />
            <input
              type="text"
              placeholder="Search by name, email, or city..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-slate-50 dark:bg-slate-800/50 text-slate-900 dark:text-white"
            />
          </div>

          <select
            value={selectedBloodGroup}
            onChange={(e) => setSelectedBloodGroup(e.target.value)}
            className="w-full px-4 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-slate-50 dark:bg-slate-800/50 text-slate-900 dark:text-white"
          >
            <option value="ALL">All Blood Groups</option>
            <option value="A+">A+</option>
            <option value="A-">A-</option>
            <option value="B+">B+</option>
            <option value="B-">B-</option>
            <option value="AB+">AB+</option>
            <option value="AB-">AB-</option>
            <option value="O+">O+</option>
            <option value="O-">O-</option>
          </select>

          <select
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value)}
            className="w-full px-4 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-xs bg-slate-50 dark:bg-slate-800/50 text-slate-900 dark:text-white"
          >
            <option value="ALL">All Roles</option>
            <option value="DONOR">Donor</option>
            <option value="PATIENT">Patient</option>
            <option value="HOSPITAL">Hospital</option>
          </select>
        </div>

        <DataTable
          columns={donorTableColumns}
          data={filteredDonors}
          keyField="id"
          emptyMessage="No matching user or donor records found for selected filters."
        />
      </Card>
    </div>
  );
}
