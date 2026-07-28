import React from 'react';
import { useSystemHealth } from '../../hooks/useAdminStatistics';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/ui/ErrorState';
import { HardDrive, Cpu, Radio, Users, Layers } from 'lucide-react';

/**
 * Diagnostic screen showing database connection links, API states, and email dispatch status.
 */
export default function SystemHealth() {
  const { data: health, isLoading, error, refetch } = useSystemHealth();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={refetch} />;
  }

  const isDbUp = health?.databaseConnectivity === 'UP';
  const isApiHealthy = health?.apiHealth === 'HEALTHY' || health?.apiHealth === 'UP';
  const isQueueActive = health?.notificationQueueStatus === 'ACTIVE' || health?.notificationQueueStatus === 'UP';

  return (
    <div className="flex flex-col gap-6 max-w-4xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">System Health & Diagnostics</h1>
        <p className="text-xs text-gray-500 mt-1">
          Monitor server resources, database links, and notification channel queues.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Database status */}
        <Card className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-blue-50 text-blue-600 rounded-2xl">
                <HardDrive className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-gray-800 text-sm">Database Link</h3>
            </div>
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold ${
              isDbUp ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
            }`}>
              {health?.databaseConnectivity || 'DOWN'}
            </span>
          </div>
          <p className="text-xs text-gray-500 leading-relaxed">
            Main PostgreSQL repository connectivity link state and active transaction thread bounds.
          </p>
        </Card>

        {/* API Engine Health status */}
        <Card className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-teal-50 text-teal-600 rounded-2xl">
                <Cpu className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-gray-800 text-sm">Spring API Health</h3>
            </div>
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold ${
              isApiHealthy ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
            }`}>
              {health?.apiHealth || 'UNHEALTHY'}
            </span>
          </div>
          <p className="text-xs text-gray-500 leading-relaxed">
            API controller core thread pools status checking and servlet container response logs.
          </p>
        </Card>

        {/* Dispatch Queue status */}
        <Card className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-indigo-50 text-indigo-600 rounded-2xl">
                <Radio className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-gray-800 text-sm">SMTP Alert Dispatch</h3>
            </div>
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold ${
              isQueueActive ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
            }`}>
              {health?.notificationQueueStatus || 'INACTIVE'}
            </span>
          </div>
          <p className="text-xs text-gray-500 leading-relaxed">
            Java Mail Sender queue thread status checks.
          </p>
        </Card>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <StatCard
          title="Active Live Connections"
          value={health?.activeUsers}
          icon={Users}
          iconColor="text-blue-500 bg-blue-50"
        />

        <StatCard
          title="Total Repository Records"
          value={health?.totalRecords}
          icon={Layers}
          iconColor="text-slate-500 bg-slate-50"
        />
      </div>
    </div>
  );
}
