'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { DocumentData, QueryDocumentSnapshot } from 'firebase/firestore';
import toast from 'react-hot-toast';

interface Column<T> {
  header: string;
  accessor: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  render?: (value: any, row: T) => React.ReactNode;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  initialTotalCount: number;
  itemsPerPage?: number;
  fetchData: (pageSize: number, lastDoc: QueryDocumentSnapshot<DocumentData> | null) => Promise<{
    data: T[];
    lastDoc: QueryDocumentSnapshot<DocumentData> | null;
    totalCount: number;
  }>;
  onRowAction?: (row: T, action: string) => void;
  loading?: boolean;
  title?: string;
  expandableContent?: (row: T) => React.ReactNode;
  onStatusChange?: (id: string, newStatus: string) => Promise<void>;
  statusOptions?: string[];
  rowActions?: {
    icon: React.ReactNode;
    label: string;
    action: (row: T) => void;
    showCondition?: (row: T) => boolean;
    hideText?: boolean;
  }[];
}

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
const Table = <T extends {}>({
  columns,
  data: initialData,
  initialTotalCount,
  itemsPerPage = 10,
  fetchData,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  onRowAction,
  loading: externalLoading,
  title,
  expandableContent,
  onStatusChange,
  statusOptions,
  rowActions
}: TableProps<T>) => {
  const [currentPage, setCurrentPage] = useState(1);
  const [data, setData] = useState<T[]>(initialData);
  const [totalCount, setTotalCount] = useState(initialTotalCount);
  const [lastDoc, setLastDoc] = useState<QueryDocumentSnapshot<DocumentData> | null>(null);
  const [loading, setLoading] = useState(false);
  const [openRow, setOpenRow] = useState<number | null>(null);

  const loadPage = useCallback(async (page: number) => {
    try {
      setLoading(true);
      const result = await fetchData(itemsPerPage, page === 1 ? null : lastDoc);
      setData(result.data);
      setLastDoc(result.lastDoc);
      setTotalCount(result.totalCount);
      setCurrentPage(page);
    } catch (error) {
      console.error('Error loading data:', error);
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [itemsPerPage, lastDoc, fetchData]);

  useEffect(() => {
    loadPage(1);
  }, [loadPage]);

  const handlePageChange = (page: number) => {
    loadPage(page);
  };

  const toggleRow = (index: number) => {
    setOpenRow(openRow === index ? null : index);
  };

  const handleStatusChange = async (id: string, newStatus: string) => {
    if (onStatusChange) {
      const updatePromise = onStatusChange(id, newStatus);
      toast.promise(
        updatePromise,
        {
          loading: 'Updating status...',
          success: `Status updated to ${newStatus}`,
          error: 'Failed to update status'
        }
      );
    }
  };

  const totalPages = Math.ceil(totalCount / itemsPerPage);

  return (
    <div className="bg-white shadow rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        {title && <h3 className="text-lg font-medium leading-6 text-gray-900">{title}</h3>}
        <div className="mt-4">
          <div className="flex flex-col">
            <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8" style={{ overscrollBehavior: 'auto' }}>
              <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        {expandableContent && (
                          <th scope="col" className="w-10 px-6 py-3"></th>
                        )}
                        {columns.map((column, index) => (
                          <th
                            key={index}
                            scope="col"
                            className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                          >
                            {column.header}
                          </th>
                        ))}
                        {(statusOptions || rowActions) && (
                          <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Actions
                          </th>
                        )}
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {(loading || externalLoading) ? (
                        <tr>
                          <td colSpan={columns.length + (expandableContent ? 1 : 0) + ((statusOptions || rowActions) ? 1 : 0)} className="px-6 py-4 text-center">
                            <div className="flex justify-center">
                              <svg className="animate-spin h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                              </svg>
                            </div>
                          </td>
                        </tr>
                      ) : (
                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                        data.map((row: any, rowIndex) => (
                          <React.Fragment key={rowIndex}>
                            <tr className={openRow === rowIndex ? 'bg-gray-50' : undefined}>
                              {expandableContent && (
                                <td className="px-6 py-4 whitespace-nowrap">
                                  <button
                                    onClick={() => toggleRow(rowIndex)}
                                    className="text-gray-400 hover:text-gray-500"
                                  >
                                    {openRow === rowIndex ? '−' : '+'}
                                  </button>
                                </td>
                              )}
                              {columns.map((column, colIndex) => (
                                <td key={colIndex} className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                  {column.render ? column.render(row[column.accessor], row) : row[column.accessor]}
                                </td>
                              ))}
                              {(statusOptions || rowActions) && (
                                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                  <div className="flex justify-end space-x-2">
                                    {statusOptions && (
                                      <div className="relative group">
                                        <span className={`${
                                          row.status === 'confirmed' ? 'text-green-600' :
                                          row.status === 'pending' ? 'text-yellow-600' :
                                          'text-red-600'
                                        }`}>
                                          {row.status}
                                        </span>
                                        <div className="hidden group-hover:flex absolute right-0 top-1/2 transform -translate-y-1/2 bg-white shadow-lg rounded-lg p-1 z-10">
                                          {statusOptions.map((status) => (
                                            <button
                                              key={status}
                                              className="p-1 hover:bg-gray-100 rounded"
                                              onClick={() => handleStatusChange(row.id, status)}
                                            >
                                              {status}
                                            </button>
                                          ))}
                                        </div>
                                      </div>
                                    )}
                                    {rowActions && rowActions.map((action, actionIndex) => (
                                      action.showCondition?.(row) !== false && (
                                        <div key={actionIndex} className="group flex justify-center">
                                          {!action.hideText && (
                                            <span className="group-hover:hidden">{action.label}</span>
                                          )}
                                          <button
                                            onClick={() => action.action(row)}
                                            className={`${action.hideText ? '' : 'invisible group-hover:visible'} hover:text-blue-500`}
                                            title={action.label}
                                          >
                                            {action.icon}
                                          </button>
                                        </div>
                                      )
                                    ))}
                                  </div>
                                </td>
                              )}
                            </tr>
                            {expandableContent && openRow === rowIndex && (
                              <tr>
                                <td colSpan={columns.length + 1} className="px-6 py-4">
                                  {expandableContent(row)}
                                </td>
                              </tr>
                            )}
                          </React.Fragment>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
        {totalPages > 1 && (
          <div className="mt-4 flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6">
            <div className="flex flex-1 justify-between sm:hidden">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1 || loading || externalLoading}
                className={`relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 ${
                  currentPage === 1 || loading || externalLoading
                    ? 'opacity-50 cursor-not-allowed'
                    : 'hover:bg-gray-50'
                }`}
              >
                Previous
              </button>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages || loading || externalLoading}
                className={`relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 ${
                  currentPage === totalPages || loading || externalLoading
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
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1 || loading || externalLoading}
                    className={`relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 ${
                      currentPage === 1 || loading || externalLoading
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
                      onClick={() => handlePageChange(i + 1)}
                      disabled={loading || externalLoading}
                      className={`relative inline-flex items-center px-4 py-2 text-sm font-medium ring-1 ring-inset ring-gray-300 ${
                        currentPage === i + 1
                          ? 'z-10 bg-indigo-50 text-indigo-600 ring-indigo-500'
                          : 'text-gray-900 hover:bg-gray-50'
                      } ${(loading || externalLoading) ? 'opacity-50 cursor-not-allowed' : ''}`}
                    >
                      {i + 1}
                    </button>
                  ))}
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === totalPages || loading || externalLoading}
                    className={`relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 ${
                      currentPage === totalPages || loading || externalLoading
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
        )}
      </div>
    </div>
  );
};

export default Table;