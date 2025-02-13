/* eslint-disable @typescript-eslint/no-unused-vars */
'use client';

import { ExclamationCircleIcon, ClockIcon, UserGroupIcon } from '@heroicons/react/24/outline';
import StatsCard from './StatsCard';
import { Line } from 'react-chartjs-2'; // Import the Line component
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement } from 'chart.js';
import { useEffect, useState } from 'react';
import { fetchTransactionData } from '@/app/models/transaction'; // Import your data fetching function

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement);

interface OverviewProps {
  lowStockCount: number;
  appointmentCount: number;
  therapistCount: number;
}

interface Dataset {
  label: string;
  data: number[]; // Assuming data is an array of numbers
  borderColor: string;
  backgroundColor: string;
  fill: boolean;
}

interface ChartData {
  labels: string[];
  datasets: Dataset[];
}

export default function Overview({ lowStockCount, appointmentCount, therapistCount }: OverviewProps) {
  const [chartData, setChartData] = useState<ChartData>({
    labels: [],
    datasets: [],
  });

  const [selectedFilter, setSelectedFilter] = useState<string>('');

  const handleFilterChange = (filter: string) => {
    setSelectedFilter(filter);
  };

  useEffect(() => {
    const getData = async () => {
      const data = await fetchTransactionData(); // Fetch your transaction data
      setChartData({
        labels: data.labels, // Assuming your data has labels
        datasets: [
          {
            label: 'Transactions',
            data: data.values, // Assuming your data has values
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            fill: true,
          },
        ],
      });
    };

    getData();
  }, []);

  useEffect(() => {
    if (!chartData.datasets.length) return;
    let filteredData: number[] = [];
    const now = new Date();
    switch (selectedFilter) {
      case 'lastWeek':
        const lastWeek = new Date(now);
        lastWeek.setDate(now.getDate() - 7);
        filteredData = chartData.datasets[0].data.filter((dataPoint, index) => {
          const date = new Date(chartData.labels[index]);
          return date >= lastWeek;
        });
        break;
      case 'last6Months':
        const last6Months = new Date(now);
        last6Months.setMonth(now.getMonth() - 6);
        filteredData = chartData.datasets[0].data.filter((dataPoint, index) => {
          const date = new Date(chartData.labels[index]);
          return date >= last6Months;
        });
        break;
      case 'lastYear':
        const lastYear = new Date(now);
        lastYear.setFullYear(now.getFullYear() - 1);
        filteredData = chartData.datasets[0].data.filter((dataPoint, index) => {
          const date = new Date(chartData.labels[index]);
          return date >= lastYear;
        });
        break;
      case 'yearly':
        // Implement logic for yearly chart if needed
        break;
      default:
        filteredData = chartData.datasets[0].data; // Default to all data
    }

    // Update the chart with the filtered data
    setChartData({
      ...chartData,
      datasets: [{ ...chartData.datasets[0], data: filteredData }],
    });
  }, [selectedFilter, chartData]);

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
       <div className="flex gap-3 mb-4 w-full col-span-3">
         <button
           onClick={() => handleFilterChange('lastWeek')}
           className={`px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 ease-in-out
             ${selectedFilter === 'lastWeek' ? 'bg-blue-600 text-white shadow-md' : 'bg-blue-200 text-gray-700 border border-gray-300 hover:bg-blue-300'}`}
         >
           Last Week
         </button>
         <button
           onClick={() => handleFilterChange('last6Months')}
           className={`px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 ease-in-out
             ${selectedFilter === 'last6Months' ? 'bg-green-600 text-white shadow-md' : 'bg-green-200 text-gray-700 border border-gray-300 hover:bg-green-300'}`}
         >
           Last 6 Months
         </button>
         <button
           onClick={() => handleFilterChange('lastYear')}
           className={`px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 ease-in-out
             ${selectedFilter === 'lastYear' ? 'bg-yellow-600 text-white shadow-md' : 'bg-yellow-200 text-gray-700 border border-gray-300 hover:bg-yellow-300'}`}
         >
           Last Year
         </button>
         <button
           onClick={() => handleFilterChange('yearly')}
           className={`px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 ease-in-out
             ${selectedFilter === 'yearly' ? 'bg-red-600 text-white shadow-md' : 'bg-red-200 text-gray-700 border border-gray-300 hover:bg-red-300'}`}
         >
           Yearly Chart
         </button>
       </div>
      {/* Line Graph Below Stats Cards */}
      <div className="col-span-1 sm:col-span-2 lg:col-span-3">
        <Line data={chartData} />
      </div>
    </div>
  );
}
