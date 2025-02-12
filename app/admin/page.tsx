'use client';

import { useState, useEffect } from 'react';
import {
  CalendarIcon,
  UserGroupIcon,
  CubeIcon,
  WrenchScrewdriverIcon,
  ChartBarIcon,
  TruckIcon
} from '@heroicons/react/24/outline';

import TabNavigation from './components/Navigation/TabNavigation';
import Overview from './components/Overview';
import InventoryTable from './components/Inventory/InventoryTable';
import AppointmentTable from './components/Bookings/AppointmentTable';
import StaffTable from './components/Staff/StaffTable';
import ServiceTable from './components/Services/ServiceTable';
import SuppliersTable from './components/Suppliers/SuppliersTable';
import { Service } from '../models/service';
import { getTodayConfirmedBookingsCount } from '../models/booking';
import { getActiveTherapistsCount } from '../models/staff';
import { fetchLowStockCount } from '../models/inventory';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  const [todayBookings, setTodayBookings] = useState(0);
  const [activeTherapists, setActiveTherapists] = useState(0);
  const [lowStockCount, setLowStockCount] = useState(0);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [services, setServices] = useState<Service[]>([
    
  ]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [bookings, therapists, lowStock] = await Promise.all([
          getTodayConfirmedBookingsCount(),
          getActiveTherapistsCount(),
          fetchLowStockCount()
        ]);
        
        setTodayBookings(bookings);
        setActiveTherapists(therapists);
        setLowStockCount(lowStock);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      }
    };

    fetchData();
  }, []);

  const tabs = [
    { id: 'overview', name: 'Overview', icon: ChartBarIcon },
    { id: 'inventory', name: 'Inventory', icon: CubeIcon },
    { id: 'bookings', name: 'Bookings', icon: CalendarIcon },
    { id: 'staff', name: 'Staff', icon: UserGroupIcon },
    { id: 'services', name: 'Services', icon: WrenchScrewdriverIcon },
    { id: 'suppliers', name: 'Suppliers', icon: TruckIcon },
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
            lowStockCount={lowStockCount}
            therapistCount={activeTherapists}
            appointmentCount={todayBookings}
          />
        )}

        {activeTab === 'inventory' && (
          <InventoryTable />
        )}

        {activeTab === 'bookings' && (
          <AppointmentTable />
        )}

        {activeTab === 'staff' && (
          <StaffTable />
        )}

        {activeTab === 'services' && (
          <ServiceTable
            services={services}
            onAddService={handleAddService}
            onEditService={handleEditService}
          />
        )}

        {activeTab === 'suppliers' && (
          <SuppliersTable />
        )}
      </div>
    </div>
  );
}
