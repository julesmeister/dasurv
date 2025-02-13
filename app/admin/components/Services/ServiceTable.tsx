/* eslint-disable @typescript-eslint/no-unused-vars */
'use client';

import { useState, useEffect } from 'react';
import { getServices, addService, updateService, deleteService } from '@/app/lib/services';
import { Service } from '@/app/models/service';
import ServiceDialog from './ServiceDialog';
import Image from 'next/image';
import toast from 'react-hot-toast';

interface ServiceTableProps {
  onAddService: () => void;
  onEditService: (id: string) => void;
}

export default function ServiceTable({ onAddService, onEditService }: ServiceTableProps) {
  const [services, setServices] = useState<Service[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedService, setSelectedService] = useState<Service>();

  const fetchServices = async () => {
    try {
      const serviceList = await getServices();
      setServices(serviceList);
    } catch (error) {
      console.error('Error fetching services:', error);
      toast.error('Failed to load services');
    }
  };

  useEffect(() => {
    fetchServices();
  }, []);

  const handleEdit = (service: Service) => {
    setSelectedService(service);
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
      const updatedServices = await getServices();
      setServices(updatedServices);
      toast.success('Service saved successfully');
    } catch (error) {
      console.error('Error saving service:', error);
      toast.error('Failed to save service');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteService(id);
      const updatedServices = await getServices();
      setServices(updatedServices);
      toast.success('Service deleted successfully');
    } catch (error) {
      console.error('Error deleting service:', error);
      toast.error('Failed to delete service');
    }
  };

  return (
    <div className="bg-white shadow rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h3 className="text-lg font-medium leading-6 text-gray-900">Service Management</h3>
          </div>
          <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
            <button
              type="button"
              onClick={handleAdd}
              className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
            >
              Add Service
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
                      <th scope="col" className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-6">Service</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Description</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Duration</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Price</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Icon</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Status</th>
                      <th scope="col" className="relative py-3.5 pl-3 pr-4 sm:pr-6">
                        <span className="sr-only">Edit</span>
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white">
                    {services.map(service => (
                      <tr key={service.id}>
                        <td className="py-4 pl-4 pr-3 text-sm text-gray-900 sm:pl-6">{service.name}</td>
                        <td className="px-3 py-4 text-sm text-gray-900">{service.description}</td>
                        <td className="px-3 py-4 text-sm text-gray-900">{service.duration}</td>
                        <td className="px-3 py-4 text-sm text-gray-900">{service.price}</td>
                        <td className="px-3 py-4 text-sm text-gray-900">
                          {service.icon ? (
                            <Image 
                              src={service.icon}
                              alt={service.name}
                              width={24}
                              height={24}
                            />
                          ) : (
                            <div className="w-6 h-6 bg-gray-200 rounded-full" />
                          )}
                        </td>
                        <td className="px-3 py-4 text-sm text-gray-900">{service.status}</td>
                        <td className="relative py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-6">
                          <button onClick={() => handleEdit(service)} className="text-indigo-600 hover:text-indigo-900">Edit</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      <ServiceDialog
        isOpen={isDialogOpen}
        onClose={() => {
          setIsDialogOpen(false);
          setSelectedService(undefined);
        }}
        onSave={handleSave}
        service={selectedService}
        fetchServices={fetchServices}
      />
    </div>
  );
}
