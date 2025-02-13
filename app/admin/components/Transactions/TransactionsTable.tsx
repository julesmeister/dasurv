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
import { fetchTransactions } from "@/app/models/transaction";
import toast from "react-hot-toast";
import { EyeIcon, PencilIcon, ArrowPathIcon } from "@heroicons/react/24/outline";
import { Tooltip } from '@/app/components/Tooltip';

const TransactionsTable: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const itemsPerPage = 10;

  const formatFirebaseTimestamp = (timestamp: Timestamp | null) => {
    if (!timestamp) return "";
    const date = timestamp.toDate();
    return format(date, "MMM d, yyyy h:mm a");
  };

  const handleStatusChange = async (id: string, newStatus: string) => {
    // Implement status change logic here
    toast.promise(
      Promise.resolve(), // Replace with actual status update function
      {
        loading: "Updating transaction status...",
        success: `Transaction status updated to ${newStatus}`,
        error: "Failed to update transaction status",
      }
    );
  };

  const columns = [
    {
      header: "Date",
      accessor: "date",
      render: (value: Timestamp, row: Transaction) =>
        formatFirebaseTimestamp(row.date),
    },
    {
      header: "Customer",
      accessor: "customerName",
    },
    {
      header: "Service",
      accessor: "serviceName",
    },
    {
      header: "Amount",
      accessor: "amount",
      render: (value: number, row: Transaction) => `₱${row.amount.toFixed(2)}`,
    },
    {
      header: "Payment Method",
      accessor: "paymentMethod",
    },
    {
      header: "Status",
      accessor: "status",
      render: (value: "completed" | "pending" | "failed", row: Transaction) => (
        <span
          className={`inline-flex rounded-full px-2 text-xs font-semibold leading-5 ${
            value === "completed"
              ? "bg-green-100 text-green-800"
              : value === "pending"
              ? "bg-yellow-100 text-yellow-800"
              : "bg-red-100 text-red-800"
          }`}
        >
          {value.charAt(0).toUpperCase() + value.slice(1)}
        </span>
      ),
    },
  ];

  const rowActions = [
    {
      icon: <EyeIcon className="h-4 w-4" />,
      label: "View",
      action: (row: Transaction) => {
        toast.success(`Viewing transaction for ${row.customerName}`);
      },
    },
    {
      icon: <PencilIcon className="h-4 w-4" />,
      label: "Edit",
      action: (row: Transaction) => {
        toast.success(`Editing transaction for ${row.customerName}`);
      },
      showCondition: (row: Transaction) => row.status !== "completed",
    },
  ];

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
    const loadTransactions = async () => {
      try {
        const result = await fetchTransactions(itemsPerPage, null);
        setTransactions(result.transactions);
        setTotalCount(result.totalCount);
      } catch (error) {
        console.error("Error loading transactions:", error);
        toast.error("Failed to load transactions");
      }
    };

    loadTransactions();
  }, [itemsPerPage]);

  return (
    <Table
      title="Transactions"
      description="A list of all transactions including their date, customer, service, amount, and status."
      data={transactions}
      columns={columns}
      initialTotalCount={totalCount}
      itemsPerPage={itemsPerPage}
      fetchData={async (pageSize, lastDoc) => {
        const result = await fetchTransactions(pageSize, lastDoc);
        return {
          data: result.transactions,
          lastDoc: result.lastDoc,
          totalCount: result.totalCount,
        };
      }}
      expandableContent={expandableContent}
      onStatusChange={handleStatusChange}
      statusOptions={["pending", "completed", "cancelled"]}
      rowActions={rowActions}
      actions={
        <button
          onClick={async () => {
            try {
              setLoading(true);
              const { transactions: newTransactions, totalCount: newTotal } = await fetchTransactions(itemsPerPage);
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
    />
  );
};

export default TransactionsTable;
