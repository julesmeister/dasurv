/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-unused-vars */
"use client";

import { useState, useEffect, useCallback } from "react";
import {
  getServices,
  addService,
  updateService,
  deleteService,
  refreshServices,
} from "@/app/models/service";
import { Service } from "@/app/models/service";
import ServiceDialog from "./ServiceDialog";
import Image from "next/image";
import toast from "react-hot-toast";
import Table from "@/app/admin/components/Template/table";
import { DocumentData, QueryDocumentSnapshot } from "@firebase/firestore";
import { Tooltip } from "@/app/components/Tooltip";
import { ArrowPathIcon } from "@heroicons/react/24/solid";

interface ServiceTableProps {
  onAddService: () => void;
  onEditService: (id: string) => void;
}

export default function ServiceTable({
  onAddService,
  onEditService,
}: ServiceTableProps) {
  const [services, setServices] = useState<Service[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedService, setSelectedService] = useState<Service>();
  const [loading, setLoading] = useState(false);
  const [totalCount, setTotalCount] = useState(0);
  const [lastDoc, setLastDoc] =
    useState<QueryDocumentSnapshot<DocumentData> | null>(null);
  const itemsPerPage = 10;

  const fetchServices = useCallback(
    async (
      pageSize: number,
      lastDoc: QueryDocumentSnapshot<DocumentData> | null
    ): Promise<{
      data: Service[];
      lastDoc: QueryDocumentSnapshot<DocumentData> | null;
      totalCount: number;
    }> => {
      setLoading(true);
      try {
        const result = await getServices(pageSize, lastDoc);
        if (!result) {
          // If no result, return empty state
          return {
            data: [],
            lastDoc: null,
            totalCount: 0
          };
        }
        setServices(result.data);
        setTotalCount(result.totalCount);
        setLastDoc(result.lastDoc);
        return result;
      } catch (error) {
        console.error("Error fetching services:", error);
        toast.error("Failed to load services");
        // Return empty state on error
        return {
          data: [],
          lastDoc: null,
          totalCount: 0
        };
      } finally {
        setLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    fetchServices(itemsPerPage, null);
  }, [fetchServices]);

  const handleEdit = (row: Service) => {
    setSelectedService(row);
    setIsDialogOpen(true);
  };

  const handleAdd = () => {
    setSelectedService(undefined);
    setIsDialogOpen(true);
  };

  const handleSave = async (service: Service) => {
    try {
      if (service.id) {
        const { id, ...updates } = service;
        await updateService(id, updates);
      } else {
        await addService(service);
      }
      const updatedServices = await getServices(itemsPerPage, lastDoc);
      setServices(updatedServices.data);
      toast.success("Service saved successfully");
    } catch (error) {
      console.error("Error saving service:", error);
      toast.error("Failed to save service");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteService(id);
      const updatedServices = await getServices(itemsPerPage, lastDoc);
      setServices(updatedServices.data);
      toast.success("Service deleted successfully");
    } catch (error) {
      console.error("Error deleting service:", error);
      toast.error("Failed to delete service");
    }
  };

  const rowActions = (row: Service): React.ReactNode => {
    return (
      <div>
        <button onClick={() => handleEdit(row)} className="text-yellow-600 hover:text-yellow-900 ml-4">
          Edit
        </button>
        <button onClick={() => handleDelete(row.id!)} className="text-red-600 hover:text-red-900">
          Delete
        </button>
      </div>
    );
  };

  const columns = [
    { header: "Service Name", accessor: "name" },
    { header: "Description", accessor: "description" },
    { header: "Price", accessor: "price" },
    { header: "Duration", accessor: "duration" },
    { header: "Icon", accessor: "icon", render: (value: string, row: Service) => value ? <Image width={24} height={24} src={row.icon!} alt={row.name} /> : <div className="w-6 h-6 bg-gray-200 rounded-full" /> },
  ];

  const refresh = async () => {
    const result = await refreshServices(itemsPerPage, lastDoc);
    setServices(result.data);
    setTotalCount(result.totalCount);
    setLastDoc(result.lastDoc);
  };

  return (
    <div className="bg-white shadow rounded-lg">
      <Table<Service>
        initialTotalCount={totalCount}
        columns={columns}
        data={services}
        fetchData={fetchServices}
        itemsPerPage={itemsPerPage}
        title="Services"
        description="Manage your services here"
        rowActions={rowActions}
        loading={loading}
        actions={
          <div className="flex space-x-2">
            <button
              type="button"
              onClick={handleAdd}
              className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
            >
              Add Service
            </button>
            <button
              type="button"
              onClick={async () => {
                try {
                  setLoading(true);
                  console.log("Loading state:", loading);
                  await refresh();
                  toast.success('Data refreshed successfully');
                } catch (error) {
                  toast.error('Failed to refresh data');
                  console.error('Refresh error:', error);
                } finally {
                  setLoading(false);
                }
              }}
              className="inline-flex items-center justify-center rounded-md border border-transparent bg-white px-4 py-2 text-sm font-medium text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
            >
              <Tooltip content="Update list with latest services">
                <ArrowPathIcon className={`-ml-0.5 mr-1.5 h-5 w-5 ${loading ? "animate-spin" : ""}`} aria-hidden="true" />
              </Tooltip>
              Refresh
            </button>
          </div>
        }
      />

      <ServiceDialog
        isOpen={isDialogOpen}
        service={selectedService}
        fetchServices={refresh}
        onClose={() => setIsDialogOpen(false)}
        onSave={async (data) => {
          handleSave(data);
          setIsDialogOpen(false);
        }}
      />
    </div>
  );
}
