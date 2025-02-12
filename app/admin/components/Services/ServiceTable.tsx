'use client';

import { useState, useEffect } from 'react';
import { getDocs } from 'firebase/firestore';
import { servicesCollection } from '@/app/lib/firebase';
import { Service } from '@/app/models/service';
import ServiceDialog from './ServiceDialog';
import Image from 'next/image';

interface ServiceTableProps {
  services: Service[];
  onAddService: () => void;
  onEditService: (id: string) => void;
}

export default function ServiceTable({ onAddService, onEditService }: ServiceTableProps) {
  const [services, setServices] = useState<Service[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedService, setSelectedService] = useState<Service | undefined>(undefined);

  useEffect(() => {
    const fetchServices = async () => {
      const serviceSnapshot = await getDocs(servicesCollection);
      const serviceList = serviceSnapshot.docs.map(doc => {
        const data = doc.data();
        return {
          id: doc.id,
          name: data.name || '', // Provide a default value if necessary
          icon: data.icon || '', // Provide a default value if necessary
          description: data.description || '', // Provide a default value if necessary
          duration: data.duration || 0, // Provide a default value if necessary
          price: data.price || 0, // Provide a default value if necessary
          status: data.status || '' // Provide a default value if necessary
        };
      });
      setServices(serviceList);
    };
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

  const fetchServices = async () => {
    const serviceSnapshot = await getDocs(servicesCollection);
    const serviceList = serviceSnapshot.docs.map(doc => {
      const data = doc.data();
      return {
        id: doc.id,
        name: data.name || '', // Provide a default value if necessary
        icon: data.icon || '', // Provide a default value if necessary
        description: data.description || '', // Provide a default value if necessary
        duration: data.duration || 0, // Provide a default value if necessary
        price: data.price || 0, // Provide a default value if necessary
        status: data.status || '' // Provide a default value if necessary
      };
    });
    setServices(serviceList);
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
                          <Image 
                            src={service.icon} 
                            alt={service.name} 
                            width={24} 
                            height={24} 
                          />
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
        onSave={async (service: Service) => {
          if (service.id) {
            await onEditService(service.id);
          } else {
            await onAddService();
          }
          await fetchServices(); // Refresh the services after saving
          setIsDialogOpen(false);
          setSelectedService(undefined);
        }}
        fetchServices={fetchServices}
        service={selectedService}
      />
    </div>
  );
}
