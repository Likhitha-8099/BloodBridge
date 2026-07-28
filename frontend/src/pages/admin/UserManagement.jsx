import React from 'react';
import { useUserStatistics } from '../../hooks/useAdminStatistics';
import StatCard from '../../components/ui/StatCard';
import PieChartCard from '../../components/ui/PieChartCard';
import BarChartCard from '../../components/ui/BarChartCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { Users, User, ShieldAlert, Heart, Award } from 'lucide-react';

/**
 * Screen showing user statistics, account roles distribution, and activity graphs.
 */
export default function UserManagement() {
  const { data: stats, isLoading, error } = useUserStatistics();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  const roleData = [
    { name: 'Donors', value: stats?.totalDonors || 0 },
    { name: 'Patients', value: stats?.totalPatients || 0 },
    { name: 'Hospitals', value: stats?.totalHospitals || 0 },
  ].filter(r => r.value > 0);

  const activeData = [
    { name: 'Active', value: stats?.activeUsers || 0 },
    { name: 'Inactive', value: stats?.inactiveUsers || 0 },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">User Demographics & Registry</h1>
        <p className="text-xs text-gray-500 mt-1">
          Detailed statistics on user registrations, role distributions, and activity rates.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
        <StatCard
          title="Total Users"
          value={stats?.totalUsers}
          icon={Users}
          iconColor="text-blue-500 bg-blue-50"
        />
        <StatCard
          title="Total Donors"
          value={stats?.totalDonors}
          icon={Heart}
          iconColor="text-red-500 bg-red-50"
        />
        <StatCard
          title="Total Patients"
          value={stats?.totalPatients}
          icon={User}
          iconColor="text-indigo-500 bg-indigo-50"
        />
        <StatCard
          title="Total Hospitals"
          value={stats?.totalHospitals}
          icon={Award}
          iconColor="text-teal-500 bg-teal-50"
        />
        <StatCard
          title="Active Users"
          value={stats?.activeUsers}
          icon={Users}
          iconColor="text-green-500 bg-green-50"
        />
        <StatCard
          title="Inactive Users"
          value={stats?.inactiveUsers}
          icon={ShieldAlert}
          iconColor="text-slate-500 bg-slate-50"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <PieChartCard title="User Roles Distribution" data={roleData} />
        <BarChartCard 
          title="User Activity Status" 
          data={activeData} 
          xKey="name" 
          yKey="value" 
          name="Users Count" 
          color="#10B981" 
        />
      </div>
    </div>
  );
}
