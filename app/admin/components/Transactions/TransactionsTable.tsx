/* eslint-disable @typescript-eslint/no-unused-vars */
"use client";

import React, { useEffect, useState, useCallback } from "react";
import {
  DocumentData,
  QueryDocumentSnapshot,
  Timestamp,
} from "firebase/firestore";
import { format } from "date-fns";
import Table from "../Template/table";
import { Booking, fetchBookingById } from "@/app/models/booking";
import { fetchFreshTransactions, Transaction, fetchTransactions, updateTransactionStatus } from "@/app/models/transaction";
import toast from "react-hot-toast";
import { EyeIcon, PencilIcon, ArrowPathIcon } from "@heroicons/react/24/outline";
import { Tooltip } from '@/app/components/Tooltip';
import { DatePicker, Select, Space, Button } from 'antd';
import { FilterOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

const formatFirebaseTimestamp = (timestamp: Timestamp | null) => {
  if (!timestamp || typeof timestamp.seconds !== 'number' || isNaN(timestamp.seconds)) {
    return "";
  }
  const date = timestamp.toDate();
  return format(date, "MMM d, yyyy h:mm a");
};

const ExpandableContent: React.FC<{ record: Transaction }> = React.memo(({ record }) => {
  const [bookingDetails, setBookingDetails] = useState<Booking | null>(null);

  useEffect(() => {
    async function fetchBooking() {
      if (!record.bookingId) {
        return;
      }
      if (record.bookingId) {
        console.log('Fetching booking details for ID:', record.bookingId);
        try {
          const bookingData = await fetchBookingById(record.bookingId);
          setBookingDetails(bookingData);
          console.log('Fetched booking details:', bookingData); // Log fetched data
          console.log('Updated bookingDetails state:', bookingData); // Log updated state
          console.log('Record structure:', JSON.stringify(record)); // Log record structure
        } catch (error) {
          console.error('Error fetching booking details:', error);
          setBookingDetails(null); // Reset booking details on error
        }
      } else {
        console.warn('No booking ID provided for record:', record);
      }
    };
    fetchBooking();
  }, [record.bookingId, record]);

  return (
    <div className="p-4 bg-gray-50">
      <h4 className="text-sm font-medium text-gray-900">Transaction Details</h4>
      <dl className="mt-2 grid grid-cols-2 gap-4">
        <div>
          <dt className="text-sm font-medium text-gray-500">Transaction ID</dt>
          <dd className="mt-1 text-sm text-gray-900">{record.id}</dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-gray-500">Created At</dt>
          <dd className="mt-1 text-sm text-gray-900">
            {formatFirebaseTimestamp(record.date)}
          </dd>
        </div>
      </dl>
      <h4 className="text-sm font-medium text-gray-900 mt-4">Booking Details</h4>
      <dl className="mt-2 grid grid-cols-2 gap-4">
        <div>
          <dt className="text-sm font-medium text-gray-500">Booking ID</dt>
          <dd className="mt-1 text-sm text-gray-900">{record.bookingId}</dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-gray-500">Therapist</dt>
          <dd className="mt-1 text-sm text-gray-900">
            {bookingDetails ? bookingDetails.therapist : 'Loading...'}
          </dd>
        </div>
      </dl>
    </div>
  );
});

ExpandableContent.displayName = 'ExpandableContent';

const TransactionsTable: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const itemsPerPage = 10;
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null] | null>(null);
  const [filterType, setFilterType] = useState<'all' | 'today' | 'week' | 'month' | '3months' | '6months' | 'year' | 'custom'>('all');

  const handleStatusChange = async (id: string, newStatus: 'completed' | 'pending' | 'failed') => {
    try {
      if(await updateTransactionStatus(id, newStatus)) {
        try {
          setLoading(true);
          const { transactions: newTransactions, totalCount: newTotal } = await fetchTransactions(itemsPerPage);
          // console.log('Fetched transactions:', newTransactions);
          // console.log('Transactions:', newTransactions);
          setTransactions(newTransactions);
          setTotalCount(newTotal);
          toast.success('Data refreshed successfully');
        } catch (error) {
          toast.error('Failed to refresh data');
          console.error('Refresh error:', error);
        } finally {
          setLoading(false);
        }
      }
        toast.success(`Transaction status updated to ${newStatus}`);
    } catch (error) {
      toast.error("Failed to update transaction status");
    }
  };

  const handleFilterChange = (value: 'all' | 'today' | 'week' | 'month' | '3months' | '6months' | 'year' | 'custom') => {
    setFilterType(value);
    if (value === 'custom') return; // Don't set date range if custom is selected

    const now = dayjs();
    let start: dayjs.Dayjs | null = null;
    let end: dayjs.Dayjs | null = now;

    switch (value) {
      case 'today':
        start = now.startOf('day');
        break;
      case 'week':
        start = now.subtract(1, 'week');
        break;
      case 'month':
        start = now.subtract(1, 'month');
        break;
      case '3months':
        start = now.subtract(3, 'month');
        break;
      case '6months':
        start = now.subtract(6, 'month');
        break;
      case 'year':
        start = now.subtract(1, 'year');
        break;
      case 'all':
        start = null;
        end = null;
        break;
    }

    setDateRange([start, end]);
  };

  const handleDateRangeChange = (dates: [dayjs.Dayjs | null, dayjs.Dayjs | null] | null) => {
    setDateRange(dates);
    if (dates) {
      setFilterType('custom');
    }
  };

  const columns = [
    {
      header: "Date",
      accessor: "date",
      dataIndex: "date",
      render: (value: Timestamp) => {
        // console.log('Value passed to render:', value);
        // console.log('Type of value:', typeof value);
        // console.log('Value structure:', JSON.stringify(value));
        if (!value || typeof value.seconds !== 'number' || isNaN(value.seconds)) {
          return 'N/A';
        }
        const date = value.toDate();
        return format(date, "MMM d, yyyy h:mm a");
      },
    },
    {
      header: "Customer",
      accessor: "customerName",
      dataIndex: "customerName",
    },
    {
      header: "Service",
      accessor: "serviceName",
      dataIndex: "serviceName",
    },
    {
      header: "Amount",
      accessor: "amount",
      dataIndex: "amount",
      render: (value: number, row: Transaction) => `₱${row.amount.toFixed(2)}`,
    },
    {
      header: "Payment Method",
      accessor: "paymentMethod",
      dataIndex: "paymentMethod",  
    },
    {
      header: "Status",
      accessor: "status",
      dataIndex: "status",
      render: (value: "completed" | "pending" | "failed", row: Transaction) => (
        <span className={`inline-flex rounded-full px-2 text-xs font-semibold leading-5 ${
          value === "completed"
            ? "bg-green-100 text-green-800"
            : value === "pending"
            ? "bg-yellow-100 text-yellow-800"
            : "bg-red-100 text-red-800"
        } group relative`}>  
          {value.charAt(0).toUpperCase() + value.slice(1)}
          <div className="hidden group-hover:flex absolute -right-2 top-0 transform -translate-y-full bg-white shadow-lg rounded-lg p-1 z-10">
            <button 
              className="p-1 hover:bg-green-100 rounded-l-lg mx-1" 
              title="Confirm"
              onClick={() => handleStatusChange(row.id!, 'completed')}
            >
              <span className="text-green-600">Completed</span>
            </button>
            <button 
              className="p-1 hover:bg-yellow-100 mx-1" 
              title="Pending"
              onClick={() => handleStatusChange(row.id!, 'pending')}
            >
              <span className="text-yellow-600">Pending</span>
            </button>
            <button 
              className="p-1 hover:bg-red-100 rounded-r-lg mx-1" 
              title="Cancel"
              onClick={() => handleStatusChange(row.id!, 'failed')}
            >
              <span className="text-red-600">Failed</span>
            </button>
          </div>
        </span>
      ),
    },
    
  ];

  const rowActions = undefined;

  useEffect(() => {
    let mounted = true;
    
    const loadTransactions = async () => {
      try {
        const result = await fetchTransactions(itemsPerPage, null);
        if (mounted) {
          setTransactions(result.transactions);
          setTotalCount(result.totalCount);
        }
      } catch (error) {
        console.error("Error loading transactions:", error);
        if (mounted) {
          toast.error("Failed to load transactions");
        }
      }
    };

    loadTransactions();
    
    return () => {
      mounted = false;
    };
  }, []); // Only run on mount

  const expandableContent = useCallback((record: Transaction) => <ExpandableContent record={record} />, []);

  return (
    <div className="space-y-4">
      <div className="flex items-center space-x-4 bg-white p-4 rounded-lg shadow-sm">
        <Space>
          <FilterOutlined className="text-gray-400" />
          <Select
            defaultValue="all"
            style={{ width: 150 }}
            onChange={handleFilterChange}
            value={filterType}
            options={[
              { value: 'all', label: 'All Time' },
              { value: 'today', label: 'Today' },
              { value: 'week', label: 'Last Week' },
              { value: 'month', label: 'Last Month' },
              { value: '3months', label: 'Last 3 Months' },
              { value: '6months', label: 'Last 6 Months' },
              { value: 'year', label: 'Last Year' },
              { value: 'custom', label: 'Custom Range' },
            ]}
          />
          <RangePicker
            value={dateRange}
            onChange={handleDateRangeChange}
            disabled={filterType !== 'custom'}
            className="w-64"
          />
          <Button type="primary" onClick={() => handleFilterChange(filterType)}>Go</Button>
        </Space>
      </div>

      <Table
        columns={columns}
        data={transactions}
        initialTotalCount={totalCount}
        fetchData={async (pageSize, lastDoc) => {
          // Convert the date range to timestamps for the API
          const startDate = dateRange?.[0] ? Timestamp.fromDate(dateRange[0].toDate()) : null;
          const endDate = dateRange?.[1] ? Timestamp.fromDate(dateRange[1].toDate()) : null;

          const result = await fetchTransactions(pageSize, lastDoc, startDate, endDate, filterType);
          return {
            data: result.transactions,
            lastDoc: result.lastDoc,
            totalCount: result.totalCount,
          };
        }}
        loading={loading}
        title="Transactions"
        description="A list of all transactions including their date, customer, service, amount, and status."
        actions={
          <button
            onClick={async () => {
              try {
                setLoading(true);
                const { transactions: newTransactions, totalCount: newTotal } = await fetchFreshTransactions(itemsPerPage);
                // console.log('Fetched transactions:', newTransactions);
                // console.log('Transactions:', newTransactions);
                setTransactions(newTransactions);
                setTotalCount(newTotal);
                toast.success('Data refreshed successfully');
              } catch (error) {
                toast.error('Failed to refresh data');
                console.error('Refresh error:', error);
              } finally {
                setLoading(false);
              }
            }}
            className="inline-flex items-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
          >
            <Tooltip content="Update list with latest transactions">
              <ArrowPathIcon className={`-ml-0.5 mr-1.5 h-5 w-5 ${loading ? 'animate-spin' : ''}`} aria-hidden="true" />
            </Tooltip>
            Refresh
          </button>
        }
        expandableContent={expandableContent}
        rowActions={rowActions}
        itemsPerPage={itemsPerPage}
      />
    </div>
  );
};

export default TransactionsTable;
