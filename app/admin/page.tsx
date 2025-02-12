'use client';

import { useState } from 'react';
import {
  CalendarIcon,
  UserGroupIcon,
  CubeIcon,
  WrenchScrewdriverIcon,
  ChartBarIcon,
} from '@heroicons/react/24/outline';

import TabNavigation from './components/Navigation/TabNavigation';
import Overview from './components/Overview';
import InventoryTable from './components/Inventory/InventoryTable';
import AppointmentTable from './components/Bookings/AppointmentTable';
import StaffTable from './components/Staff/StaffTable';
import ServiceTable from './components/Services/ServiceTable';
import { Service } from '../models/service';

// Mock data - replace with actual data fetching
const lowStockItems = [
  { id: 1, name: 'Massage Oil', current: 5, minimum: 10 },
  { id: 2, name: 'Towels', current: 15, minimum: 20 },
];

const upcomingAppointments = [
  { id: 1, client: 'John Doe', service: 'Swedish Massage', time: '2:00 PM', therapist: 'Sarah Smith' },
  { id: 2, client: 'Jane Smith', service: 'Deep Tissue', time: '3:30 PM', therapist: 'Mike Johnson' },
];

const therapists = [
  { id: 1, name: 'Sarah Smith', specialties: ['Swedish', 'Deep Tissue'], availability: 'Full-time' },
  { id: 2, name: 'Mike Johnson', specialties: ['Sports', 'Therapeutic'], availability: 'Part-time' },
];

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [services, setServices] = useState<Service[]>([
    { id: '1', name: 'Swedish Massage', duration: '60 min', price: '$80', status: 'active' },
    { id: '2', name: 'Deep Tissue', duration: '90 min', price: '$120', status: 'active' },
  ]);

  const tabs = [
    { id: 'overview', name: 'Overview', icon: ChartBarIcon },
    { id: 'inventory', name: 'Inventory', icon: CubeIcon },
    { id: 'bookings', name: 'Bookings', icon: CalendarIcon },
    { id: 'staff', name: 'Staff', icon: UserGroupIcon },
    { id: 'services', name: 'Services', icon: WrenchScrewdriverIcon },
  ];

  const handleAddService = () => {
    console.log('Service added');
    // You would typically make an API call here to save the service
  };

  const handleEditService = (id: string) => {
    console.log('Service edited:', id);
    // You would typically make an API call here to update the service
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <TabNavigation
        tabs={tabs}
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'overview' && (
          <Overview
            lowStockCount={lowStockItems.length}
            appointmentCount={upcomingAppointments.length}
            therapistCount={therapists.length}
          />
        )}

        {activeTab === 'inventory' && (
          <InventoryTable items={lowStockItems} />
        )}

        {activeTab === 'bookings' && (
          <AppointmentTable appointments={upcomingAppointments} />
        )}

        {activeTab === 'staff' && (
          <StaffTable therapists={therapists} />
        )}

        {activeTab === 'services' && (
          <ServiceTable
            services={services}
            onAddService={handleAddService}
            onEditService={handleEditService}
          />
        )}
      </div>
    </div>
  );
}
