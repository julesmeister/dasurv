'use client';

import React, { useState, useEffect, useCallback } from 'react';
import InventoryDialog from "./InventoryDialog";
import { InventoryItem, fetchInventoryItems } from '@/app/models/inventory';
import Image from 'next/image';
import { DocumentData, QueryDocumentSnapshot } from 'firebase/firestore';

export default function InventoryTable() {
  const [editingItem, setEditingItem] = useState<InventoryItem | undefined>(undefined);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [openRow, setOpenRow] = useState<number | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(0);
  const [lastDoc, setLastDoc] = useState<QueryDocumentSnapshot<DocumentData> | undefined>(undefined);
  const [loading, setLoading] = useState(false);
  const itemsPerPage = 10;

  const loadPage = useCallback(async (page: number) => {
    try {
      setLoading(true);
      const result = await fetchInventoryItems(itemsPerPage, page === 1 ? undefined : lastDoc);
      setItems(result.items);
      setLastDoc(result.lastDoc);
      setTotalCount(result.totalCount);
      setCurrentPage(page);
    } catch (error) {
      console.error('Error loading inventory items:', error);
    } finally {
      setLoading(false);
    }
  }, [lastDoc]);

  useEffect(() => {
    loadPage(1);
  }, [loadPage]);

  useEffect(() => {
    if (editingItem) {
      setIsDialogOpen(true);
    }
  }, [editingItem]);

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setEditingItem(undefined);
  };

  const handleSave = async () => {
    await loadPage(currentPage); // Refresh the current page
    handleCloseDialog();
  };

  const handleAdd = () => {
    setEditingItem(undefined);
    setIsDialogOpen(true);
  };

  const toggleRow = (index: number) => {
    setOpenRow(openRow === index ? null : index);
  };

  const totalPages = Math.ceil(totalCount / itemsPerPage);

  return (
    <div className="bg-white shadow rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h3 className="text-lg font-medium leading-6 text-gray-900">Inventory Management</h3>
            <p className="mt-2 text-sm text-gray-700">
              A list of all inventory items in your organization.
            </p>
          </div>
          <div className="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
            <button
              type="button"
              onClick={handleAdd}
              className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
            >
              Add Item
            </button>
          </div>
        </div>

        <div className="mt-8 flex flex-col">
          <div className="-my-2 -mx-4 overflow-x-auto sm:-mx-6 lg:-mx-8">
            <div className="inline-block min-w-full py-2 align-middle md:px-6 lg:px-8">
              <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
                <table className="min-w-full divide-y divide-gray-300">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Item</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Current Stock</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Minimum Required</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cost</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="relative px-6 py-3">
                        <span className="sr-only">Actions</span>
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white">
                    {items.map((item, index) => (
                      <React.Fragment key={item.id}>
                        <tr>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{item.name}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.current}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.minimum}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.cost}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.price}</td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span
                              className={`inline-flex rounded-full px-2 text-xs font-semibold leading-5 ${
                                item.current <= item.minimum
                                  ? 'bg-red-100 text-red-800'
                                  : 'bg-green-100 text-green-800'
                              }`}
                            >
                              {item.current <= item.minimum ? 'Low Stock' : 'In Stock'}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                            <div className="flex items-center justify-end space-x-2">
                              <button
                                onClick={() => toggleRow(index)}
                                className="flex items-center text-blue-500 hover:text-blue-700"
                              >
                                <svg className="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                                {openRow === index ? 'Hide Details' : 'View Details'}
                              </button>
                              <button
                                onClick={() => setEditingItem(item)}
                                className="text-yellow-500 hover:text-yellow-900"
                              >
                                Edit
                              </button>
                            </div>
                          </td>
                        </tr>
                        {openRow === index && (
                          <tr>
                            <td colSpan={7} className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <p><strong>Category:</strong> {item.category}</p>
                                  <p><strong>Supplier:</strong> {item.supplier}</p>
                                  <p><strong>Reorder Level:</strong> {item.reorderLevel}</p>
                                  {item.expirationDate && (
                                    <p><strong>Expiration Date:</strong> {item.expirationDate.toLocaleDateString()}</p>
                                  )}
                                </div>
                                {item.imageUrl && (
                                  <div className="relative h-32 w-32">
                                    <Image
                                      src={item.imageUrl}
                                      alt={item.name}
                                      fill
                                      className="object-cover rounded-lg"
                                    />
                                  </div>
                                )}
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* Pagination */}
        <div className="mt-4 flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6">
          <div className="flex flex-1 justify-between sm:hidden">
            <button
              onClick={() => loadPage(currentPage - 1)}
              disabled={currentPage === 1 || loading}
              className={`relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 ${
                currentPage === 1 || loading
                  ? 'opacity-50 cursor-not-allowed'
                  : 'hover:bg-gray-50'
              }`}
            >
              Previous
            </button>
            <button
              onClick={() => loadPage(currentPage + 1)}
              disabled={currentPage === totalPages || loading}
              className={`relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 ${
                currentPage === totalPages || loading
                  ? 'opacity-50 cursor-not-allowed'
                  : 'hover:bg-gray-50'
              }`}
            >
              Next
            </button>
          </div>
          <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-gray-700">
                Showing <span className="font-medium">{((currentPage - 1) * itemsPerPage) + 1}</span> to{' '}
                <span className="font-medium">
                  {Math.min(currentPage * itemsPerPage, totalCount)}
                </span>{' '}
                of <span className="font-medium">{totalCount}</span> results
              </p>
            </div>
            <div>
              <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                <button
                  onClick={() => loadPage(currentPage - 1)}
                  disabled={currentPage === 1 || loading}
                  className={`relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 ${
                    currentPage === 1 || loading
                      ? 'opacity-50 cursor-not-allowed'
                      : 'hover:bg-gray-50'
                  }`}
                >
                  <span className="sr-only">Previous</span>
                  <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path fillRule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clipRule="evenodd" />
                  </svg>
                </button>
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => (
                  <button
                    key={i + 1}
                    onClick={() => loadPage(i + 1)}
                    className={`relative inline-flex items-center px-4 py-2 text-sm font-medium ring-1 ring-inset ring-gray-300 ${
                      currentPage === i + 1
                        ? 'z-10 bg-indigo-50 text-indigo-600 ring-indigo-500'
                        : 'text-gray-900 hover:bg-gray-50'
                    }`}
                  >
                    {i + 1}
                  </button>
                ))}
                <button
                  onClick={() => loadPage(currentPage + 1)}
                  disabled={currentPage === totalPages || loading}
                  className={`relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 ${
                    currentPage === totalPages || loading
                      ? 'opacity-50 cursor-not-allowed'
                      : 'hover:bg-gray-50'
                  }`}
                >
                  <span className="sr-only">Next</span>
                  <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path fillRule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clipRule="evenodd" />
                  </svg>
                </button>
              </nav>
            </div>
          </div>
        </div>
      </div>

      <InventoryDialog
        isOpen={isDialogOpen}
        onClose={handleCloseDialog}
        onSave={handleSave}
        initialItem={editingItem}
      />
    </div>
  );
}
