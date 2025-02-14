/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useEffect, useState, useCallback } from "react";
import { Supplier, fetchSuppliers } from "@/app/models/supplier";
import SuppliersDialog from "./SuppliersDialog";
import Table from "../Template/table";
import { DocumentData, QueryDocumentSnapshot } from "@firebase/firestore";

const itemsPerPage = 10;

const SuppliersTable: React.FC = () => {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [isDialogOpen, setDialogOpen] = useState(false);
  const [supplierToEdit, setSupplierToEdit] = useState<Supplier | undefined>(
    undefined
  );
  const [selectedSupplier, setSelectedSupplier] = useState<Supplier | null>(null);

  const loadSuppliers = useCallback(async () => {
    setLoading(true);
    try {
      const result = await fetchSuppliers();
      setSuppliers(result.suppliers);
      setTotalCount(result.totalCount);
    } catch (error) {
      console.error("Error loading suppliers:", error);
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
    setSuppliers((prevSuppliers) => [...prevSuppliers, newSupplier]);
    setTotalCount((prevCount) => prevCount + 1);
  };

  const handleEditSupplier = (supplier: Supplier) => {
    setSelectedSupplier(supplier);
    setDialogOpen(true);
  };

  const handleAddSupplier = () => {
    setSupplierToEdit(undefined);
    setDialogOpen(true);
  };

  const columns = [
    { header: "Name", accessor: "name" },
    { header: "Contact", accessor: "contact" },
    { header: "Email", accessor: "email" },
    { header: "Category", accessor: "category" },
    { header: "Status", accessor: "status" },
  ];

  const fetchData = async (
    pageSize: number,
    lastDoc: QueryDocumentSnapshot<DocumentData> | null
  ): Promise<{
    data: Supplier[];
    lastDoc: QueryDocumentSnapshot<DocumentData> | null;
    totalCount: number;
  }> => {
    if (lastDoc !== null) {
      const numericValue = lastDoc.data().someNumericField; // Replace with the actual field
      setLoading(true);
      try {
        const result = await fetchSuppliers(pageSize, numericValue);
        console.log('Fetched suppliers:', result.suppliers);
        console.log('Total count:', result.totalCount);
        return {
          data: result.suppliers,
          lastDoc: result.lastDoc !== undefined ? result.lastDoc : null,
          totalCount: result.totalCount,
        };
      } catch (error) {
        console.error("Error loading suppliers:", error);
        return {
          data: [],
          lastDoc: null,
          totalCount: 0,
        };
      } finally {
        setLoading(false);
      }
    }
    setLoading(true);
    try {
      const result = await fetchSuppliers(pageSize);
      console.log('Fetched suppliers:', result.suppliers);
      console.log('Total count:', result.totalCount);
      setSuppliers(result.suppliers);
      setTotalCount(result.totalCount);

      return {
        data: result.suppliers,
        lastDoc: result.lastDoc !== undefined ? result.lastDoc : null,
        totalCount: result.totalCount,
      };
    } finally {
      setLoading(false);
    }
  };

  const rowActions = (supplier: Supplier) => (
    <button
      type="button"
      onClick={() => handleEditSupplier(supplier)}
      className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
    >
      Edit
    </button>
  );

  const expandableContent = (supplier: Supplier) => (
    <div className="p-4 bg-gray-50">
      <h4 className="text-sm font-medium text-gray-900">Supplier Details</h4>
      <dl className="mt-2 grid grid-cols-2 gap-4">
        <div>
          <dt className="text-sm font-medium text-gray-500">Category</dt>
          <dd className="mt-1 text-sm text-gray-900">{supplier.category}</dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-gray-500">Supplier</dt>
          <dd className="mt-1 text-sm text-gray-900">{supplier.name}</dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-gray-500">Contact</dt>
          <dd className="mt-1 text-sm text-gray-900">{supplier.contact}</dd>
        </div>
      </dl>
    </div>
  );

  return (
    <div>
      <Table
        title="Suppliers"
        description="List of all suppliers"
        columns={columns}
        data={suppliers}
        initialTotalCount={totalCount}
        fetchData={fetchData}
        loading={loading}
        rowActions={rowActions}
        itemsPerPage={itemsPerPage}
        actions={
          <button
            type="button"
            onClick={handleAddSupplier}
            className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
          >
            Add Supplier
          </button>
        }
        expandableContent={expandableContent}
      />

      <SuppliersDialog
        open={isDialogOpen}
        setOpen={setDialogOpen}
        title={supplierToEdit ? "Edit Supplier" : "Add Supplier"}
        initialData={supplierToEdit}
        onSubmit={async (data) => {
          await handleSupplierAdded(data as Supplier);
          setDialogOpen(false);
        }}
      />
    </div>
  );
};

export default SuppliersTable;
