'use client';

import { ExclamationCircleIcon, ClockIcon, UserGroupIcon } from '@heroicons/react/24/outline';
import StatsCard from './StatsCard';

interface OverviewProps {
  lowStockCount: number;
  appointmentCount: number;
  therapistCount: number;
}

export default function Overview({ lowStockCount, appointmentCount, therapistCount }: OverviewProps) {
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
      <StatsCard
        title="Low Stock Items"
        value={lowStockCount}
        icon={ExclamationCircleIcon}
        iconColor="text-yellow-400"
        linkText="View all"
        linkHref="#inventory"
      />
      <StatsCard
        title="Today's Appointments"
        value={appointmentCount}
        icon={ClockIcon}
        iconColor="text-indigo-600"
        linkText="View schedule"
        linkHref="#bookings"
      />
      <StatsCard
        title="Active Therapists"
        value={therapistCount}
        icon={UserGroupIcon}
        iconColor="text-green-500"
        linkText="View staff"
        linkHref="#staff"
      />
    </div>
  );
}
