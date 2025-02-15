/* eslint-disable @typescript-eslint/no-unused-vars */
'use client';

import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition, RadioGroup } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Service, addService, updateService } from '@/app/models/service';
import { classNames } from '@/app/lib/utils';
import toast from 'react-hot-toast';

interface ServiceDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (service: Service) => void;
  fetchServices: () => Promise<void>;
  service?: Service;
}

interface ServiceFormData {
  name: string;
  description: string;
  price: string;
  status: string;
  icon?: string;
}

const statuses = [
  { value: 'active', label: 'Active', color: 'bg-green-500' },
  { value: 'inactive', label: 'Inactive', color: 'bg-gray-500' },
];

export default function ServiceDialog({ isOpen, onClose, onSave, fetchServices, service }: ServiceDialogProps) {
  const [formData, setFormData] = useState<ServiceFormData>({
    name: '',
    description: '',
    price: '',
    status: 'active',
    icon: ''
  });

  useEffect(() => {
    if (service) {
      setFormData({
        name: service.name,
        description: service.description,
        price: service.price.toString(),
        status: service.status,
        icon: service.icon
      });
    } else {
      setFormData({
        name: '',
        description: '',
        price: '',
        status: 'active',
        icon: ''
      });
    }
  }, [service]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Convert string values to numbers for price
    const serviceData: Service = {
      name: formData.name,
      description: formData.description,
      price: Number(formData.price),
      status: formData.status,
      icon: formData.icon,
      timestamp: 0
    };

    try {
      if (service?.id) {
        // Update existing service
        const updateData: Partial<Service> = {};
        if (formData.name) updateData.name = formData.name;
        if (formData.price) updateData.price = Number(formData.price);
        if (formData.status) updateData.status = formData.status;
        if (formData.icon) updateData.icon = formData.icon;
        if (formData.description !== undefined) updateData.description = formData.description;

        await updateService(service.id, updateData);
        toast.success('Service updated successfully');
      } else {
        // Add new service
        await addService(serviceData);
        toast.success('Service added successfully');
      }
      onSave(serviceData);
      onClose();
    } catch (error) {
      console.error('Error saving service:', error);
      toast.error(error instanceof Error ? error.message : 'An error occurred while saving the service');
    }
  };

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-10" onClose={onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
        </Transition.Child>

        <div className="fixed inset-0 z-10 overflow-y-auto">
          <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
              enterTo="opacity-100 translate-y-0 sm:scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 translate-y-0 sm:scale-100"
              leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            >
              <Dialog.Panel className="relative transform overflow-hidden rounded-lg bg-white px-4 pb-4 pt-5 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6">
                <div className="absolute right-0 top-0 hidden pr-4 pt-4 sm:block">
                  <button
                    type="button"
                    className="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                    onClick={onClose}
                  >
                    <span className="sr-only">Close</span>
                    <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                  </button>
                </div>

                <form onSubmit={handleSubmit}>
                  <div className="space-y-6">
                    <div>
                      <h3 className="text-base font-semibold leading-6 text-gray-900">
                        {service ? 'Edit Service' : 'Add New Service'}
                      </h3>
                      <p className="mt-1 text-sm text-gray-500">
                        {service ? 'Update the service details below.' : 'Fill in the service details below.'}
                      </p>
                    </div>

                    <div className="space-y-4">
                      <div>
                        <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                          Name
                        </label>
                        <input
                          type="text"
                          name="name"
                          id="name"
                          value={formData.name}
                          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                          required
                        />
                      </div>

                      <div>
                        <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                          Description
                        </label>
                        <textarea
                          name="description"
                          id="description"
                          value={formData.description}
                          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                          required
                        />
                      </div>

                      <div>
                        <label htmlFor="price" className="block text-sm font-medium text-gray-700">
                          Price
                        </label>
                        <input
                          type="number"
                          name="price"
                          id="price"
                          value={formData.price}
                          onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                          required
                        />
                      </div>

                      <div>
                        <label htmlFor="icon" className="block text-sm font-medium text-gray-700">
                          Icon
                        </label>
                        <input
                          type="text"
                          name="icon"
                          id="icon"
                          value={formData.icon}
                          onChange={(e) => setFormData({ ...formData, icon: e.target.value })}
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                        />
                      </div>

                      <div>
                        <label className="text-base font-medium text-gray-900">Status</label>
                        <RadioGroup value={formData.status} onChange={(status) => setFormData({ ...formData, status })}>
                          <div className="mt-4 grid grid-cols-2 gap-4">
                            {statuses.map((status) => (
                              <RadioGroup.Option
                                key={status.value}
                                value={status.value}
                                className={({ active, checked }) =>
                                  classNames(
                                    active ? 'ring-2 ring-indigo-600 ring-offset-2' : '',
                                    checked
                                      ? 'bg-indigo-600 text-white hover:bg-indigo-500'
                                      : 'bg-white text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50',
                                    'flex items-center justify-center rounded-md py-3 px-3 text-sm font-semibold uppercase sm:flex-1 cursor-pointer focus:outline-none'
                                  )
                                }
                              >
                                <RadioGroup.Label as="span">{status.label}</RadioGroup.Label>
                              </RadioGroup.Option>
                            ))}
                          </div>
                        </RadioGroup>
                      </div>
                    </div>
                  </div>

                  <div className="mt-6 flex justify-end gap-x-6">
                    <button
                      type="button"
                      onClick={onClose}
                      className="text-sm font-semibold leading-6 text-gray-900"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="inline-flex justify-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                    >
                      Save
                    </button>
                  </div>
                </form>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}
