import React, { useEffect, useState, useCallback } from 'react';
import { Supplier, fetchSuppliers } from '@/app/models/supplier';
import SuppliersDialog from './SuppliersDialog';

const itemsPerPage = 10;


const SuppliersTable: React.FC = () => {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [isDialogOpen, setDialogOpen] = useState(false);
  const [supplierToEdit, setSupplierToEdit] = useState<Supplier | undefined>(undefined);

  const loadSuppliers = useCallback(async () => {
    setLoading(true);
    try {
      const result = await fetchSuppliers();
      setSuppliers(result.suppliers);
      setTotalCount(result.totalCount);
    } catch (error) {
      console.error('Error loading suppliers:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSuppliers();
  }, [loadSuppliers]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  

  const handleDialogClose = () => {
    setDialogOpen(false);
    setSupplierToEdit(undefined);
  };

  const handleSupplierAdded = (newSupplier: Supplier) => {
    setSuppliers(prevSuppliers => [...prevSuppliers, newSupplier]);
    setTotalCount(prevCount => prevCount + 1);
  };

  const handleEditSupplier = (supplier: Supplier) => {
    setSupplierToEdit(supplier);
    setDialogOpen(true);
  };

  const handleAddSupplier = () => {
    setSupplierToEdit(undefined);
    setDialogOpen(true);
  };

  return (
    <div>
      <div className="bg-white shadow rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <div className="sm:flex sm:items-center">
            <div className="sm:flex-auto">
              <h3 className="text-lg font-medium leading-6 text-gray-900">Suppliers Management</h3>
            </div>
            <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
              <button
                type="button"
                onClick={handleAddSupplier}
                className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
              >
                Add Supplier
              </button>
            </div>
          </div>
          <div className="mt-8 flex flex-col">
            <div className="-my-2 -mx-4 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="inline-block min-w-full py-2 align-middle md:px-6 lg:px-8">
                <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
                  <table className="min-w-full leading-normal">
                    <thead>
                      <tr>
                        <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                        <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Email</th>
                        <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Phone</th>
                        <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {loading ? (
                        <tr>
                          <td colSpan={4} className="px-5 py-5 text-center">
                            <div className="flex justify-center">
                              <svg className="animate-spin h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                              </svg>
                            </div>
                          </td>
                        </tr>
                      ) : (suppliers && suppliers.length === 0) ? (
                        <tr>
                          <td colSpan={4} className="px-5 py-5 text-center text-sm text-gray-500">
                            No suppliers found
                          </td>
                        </tr>
                      ) : (
                        suppliers.map((supplier) => (
                          <tr key={supplier.id}>
                            <td className="px-5 py-5 whitespace-nowrap text-sm font-medium text-gray-900">{supplier.name}</td>
                            <td className="px-5 py-5 whitespace-nowrap text-sm text-gray-500">{supplier.address}</td>
                            <td className="px-5 py-5 whitespace-nowrap text-sm text-gray-500">{supplier.contact}</td>
                            <td className="px-5 py-5 whitespace-nowrap text-center">
                              <button onClick={() => handleEditSupplier(supplier)} className="text-indigo-600 hover:text-indigo-900">Edit</button>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
          <div className="px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
            <div className="flex-1 flex justify-between sm:hidden">
              <button
                onClick={() => handlePageChange(Math.max(1, currentPage - 1))}
                disabled={currentPage === 1 || loading}
                className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
              >
                Previous
              </button>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= Math.ceil(totalCount / itemsPerPage) || loading}
                className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
              >
                Next
              </button>
            </div>
            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
              <div>
                <p className="text-sm text-gray-700">
                  Showing <span className="font-medium">{((currentPage - 1) * itemsPerPage) + 1}</span> to{' '}
                  <span className="font-medium">
                    {Math.min(currentPage * itemsPerPage, totalCount)}
                  </span> of{' '}
                  <span className="font-medium">{totalCount}</span> results
                </p>
              </div>
              <div>
                <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                  <button
                    onClick={() => handlePageChange(Math.max(1, currentPage - 1))}
                    disabled={currentPage === 1 || loading}
                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                  >
                    <span className="sr-only">Previous</span>
                    <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                      <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </button>
                  {Array.from({ length: Math.min(5, Math.ceil(totalCount / itemsPerPage)) }, (_, i) => (
                    <button
                      key={i + 1}
                      onClick={() => handlePageChange(i + 1)}
                      className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                        currentPage === i + 1
                          ? 'z-10 bg-indigo-50 border-indigo-500 text-indigo-600'
                          : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage >= Math.ceil(totalCount / itemsPerPage) || loading}
                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                  >
                    <span className="sr-only">Next</span>
                    <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                      <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                    </svg>
                  </button>
                </nav>
              </div>
            </div>
          </div>
        </div>
      </div>
      <SuppliersDialog
        isOpen={isDialogOpen}
        onClose={handleDialogClose}
        onSupplierAdded={handleSupplierAdded}
        supplier={supplierToEdit}
      />
    </div>
  );
};

export default SuppliersTable;
