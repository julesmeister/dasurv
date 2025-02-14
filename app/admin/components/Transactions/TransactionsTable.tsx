/* eslint-disable @typescript-eslint/no-unused-vars */
"use client";

import React, { useEffect, useState } from "react";
import {
  DocumentData,
  QueryDocumentSnapshot,
  Timestamp,
} from "firebase/firestore";
import { format } from "date-fns";
import Table from "../Template/table";
import { Transaction } from "@/app/models/transaction";
import { fetchTransactions, updateTransactionStatus } from "@/app/models/transaction";
import toast from "react-hot-toast";
import { EyeIcon, PencilIcon, ArrowPathIcon } from "@heroicons/react/24/outline";
import { Tooltip } from '@/app/components/Tooltip';

const TransactionsTable: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const itemsPerPage = 10;

  const formatFirebaseTimestamp = (timestamp: Timestamp | null) => {
    if (!timestamp || typeof timestamp.seconds !== 'number' || isNaN(timestamp.seconds)) {
      return "";
    }
    const date = timestamp.toDate();
    return format(date, "MMM d, yyyy h:mm a");
  };

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

  const expandableContent = (row: Transaction) => (
    <div className="p-4 bg-gray-50">
      <h4 className="text-sm font-medium text-gray-900">Transaction Details</h4>
      <dl className="mt-2 grid grid-cols-2 gap-4">
        <div>
          <dt className="text-sm font-medium text-gray-500">Transaction ID</dt>
          <dd className="mt-1 text-sm text-gray-900">{row.id}</dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-gray-500">Created At</dt>
          <dd className="mt-1 text-sm text-gray-900">
            {formatFirebaseTimestamp(row.date)}
          </dd>
        </div>
        {/* Add more details as needed */}
      </dl>
    </div>
  );

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

  return (
    <Table
      columns={columns}
      data={transactions}
      initialTotalCount={totalCount}
      fetchData={async (pageSize, lastDoc) => {
        const result = await fetchTransactions(pageSize, lastDoc);

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
  );
};

export default TransactionsTable;
