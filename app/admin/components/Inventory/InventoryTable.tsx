"use client";

import React, { useState } from "react";
import Table from "../Template/table";
import InventoryDialog from "./InventoryDialog";
import { InventoryItem, fetchInventoryItems } from "@/app/models/inventory";
import Image from "next/image";

export default function InventoryTable() {
  const [editingItem, setEditingItem] = useState<InventoryItem | undefined>(undefined);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setEditingItem(undefined);
  };

  const handleSave = async () => {
    // The table will handle its own refresh
    handleCloseDialog();
  };

  const handleAdd = () => {
    setEditingItem(undefined);
    setIsDialogOpen(true);
  };

  const columns = [
    { header: "Item", accessor: "name" },
    { header: "Current Stock", accessor: "current" },
    { header: "Minimum Required", accessor: "minimum" },
    { header: "Cost", accessor: "cost" },
    { header: "Price", accessor: "price" },
    {
      header: "Status",
      accessor: "status",
      render: (status: string, row: InventoryItem) => (
        <span
          className={`inline-flex rounded-full px-2 text-xs font-semibold leading-5 ${
            row.current <= row.minimum
              ? "bg-red-100 text-red-800"
              : "bg-green-100 text-green-800"
          }`}
        >
          {row.current <= row.minimum ? "Low Stock" : "In Stock"}
        </span>
      ),
    },
  ];

  const expandableContent = (item: InventoryItem) => (
    <div className="p-4 bg-gray-50">
      <h4 className="text-sm font-medium text-gray-900">Inventory Details</h4>
      <dl className="mt-2 grid grid-cols-2 gap-4">
        <div>
          <dt className="text-sm font-medium text-gray-500">Category</dt>
          <dd className="mt-1 text-sm text-gray-900">{item.category}</dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-gray-500">Supplier</dt>
          <dd className="mt-1 text-sm text-gray-900">{item.supplier}</dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-gray-500">Reorder Level</dt>
          <dd className="mt-1 text-sm text-gray-900">{item.reorderLevel}</dd>
        </div>
        {item.expirationDate && (
          <div>
            <dt className="text-sm font-medium text-gray-500">Expiration Date</dt>
            <dd className="mt-1 text-sm text-gray-900">{item.expirationDate.toLocaleDateString()}</dd>
          </div>
        )}
      </dl>
    </div>
  );

  const rowActions = [
    {
      icon: (
        <svg
          className="w-4 h-4"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
          />
        </svg>
      ),
      label: "Edit",
      action: (row: InventoryItem) => setEditingItem(row),
      hideText: false,
    },
  ];

  return (
    <>
      <Table<InventoryItem>
        columns={columns}
        data={[]} // Initial empty data, will be populated by fetchData
        initialTotalCount={0}
        fetchData={async (pageSize, lastDoc) => {
          if (lastDoc !== null) {
            if (lastDoc !== null) {
              const result = await fetchInventoryItems(pageSize, lastDoc);
              return {
                data: result.items,
                lastDoc: result.lastDoc ?? null,
                totalCount: result.totalCount,
              };
            } else {
              const result = await fetchInventoryItems(pageSize, undefined);
              return {
                data: result.items,
                lastDoc: result.lastDoc ?? null,
                totalCount: result.totalCount,
              };
            }
          } else {
            const result = await fetchInventoryItems(pageSize, undefined);
            return {
              data: result.items,
              lastDoc: result.lastDoc ?? null,
              totalCount: result.totalCount,
            };
          }
        }}
        itemsPerPage={10}
        title="Inventory Management"
        description="A list of all inventory items in your organization."
        expandableContent={expandableContent}
        rowActions={rowActions}
        actions={
          <button
            type="button"
            onClick={handleAdd}
            className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
          >
            Add Item
          </button>
        }
      />

      <InventoryDialog
        isOpen={isDialogOpen}
        onClose={handleCloseDialog}
        onSave={handleSave}
        initialItem={editingItem}
      />
    </>
  );
}
