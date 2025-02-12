'use client';

import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition, RadioGroup } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Service } from '@/app/models/service';
import { addDoc, doc, getDoc, updateDoc } from 'firebase/firestore';
import { servicesCollection } from '@/app/lib/firebase';
import { classNames } from '@/app/lib/utils';
import toast from 'react-hot-toast';

interface ServiceDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (service: Service) => void;
  fetchServices: () => Promise<void>;
  service?: Service;
}

const statuses = [
  { value: 'active', label: 'Active', color: 'bg-green-500' },
  { value: 'inactive', label: 'Inactive', color: 'bg-white' },
];

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export default function ServiceDialog({ isOpen, onClose, onSave, fetchServices, service }: ServiceDialogProps) {  const [formData, setFormData] = useState<Service>({
    name: '',
    icon: '',
    description: '',
    duration: '',
    price: '',
    status: 'active'
  });

  useEffect(() => {
    if (service) {
      setFormData(service);
    } else {
      setFormData({
        name: '',
        icon: '',
        description: '',
        duration: '',
        price: '',
        status: 'active'
      });
    }
  }, [service]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (service?.id) {
        // Update existing service
        const serviceRef = doc(servicesCollection, service.id);
        const updateData: Partial<Service> = {};
        if (formData.name) updateData.name = formData.name;
        if (formData.duration) updateData.duration = formData.duration;
        if (formData.price) updateData.price = formData.price;
        if (formData.status) updateData.status = formData.status;
        if (formData.icon !== undefined) updateData.icon = formData.icon;
        if (formData.description !== undefined) updateData.description = formData.description;

        const docSnapshot = await getDoc(serviceRef);
        if (docSnapshot.exists()) {
          await updateDoc(serviceRef, updateData);
          toast.success('Service updated successfully');
        } else {
          // Create a new document if it doesn't exist
          await addDoc(servicesCollection, { ...formData, id: service.id });
          toast.success('Service created successfully');
        }
      } else {
        // Add new service
        await addDoc(servicesCollection, formData);
        toast.success('Service added successfully');
      }
      onSave(formData);
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
          <div className="fixed inset-0 bg-gray-500 bg-opacity-75 backdrop-blur-sm transition-opacity" />
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
              <Dialog.Panel className="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-4xl">
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
                <div className="bg-white px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
                  <div className="sm:flex sm:items-start w-full">
                    <div className="w-full">
                      <Dialog.Title as="h3" className="text-base font-semibold leading-6 text-gray-900 mb-5">
                        {service ? 'Edit Service' : 'Add New Service'}
                      </Dialog.Title>
                      <form onSubmit={handleSubmit}>
                        <div className="grid grid-cols-2 gap-x-8 gap-y-6">
                          {/* Left Column */}
                          <div>
                            <div className="space-y-6">
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
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
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
                                  rows={4}
                                  value={formData.description}
                                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                />
                              </div>

                              <div>
                                <label htmlFor="duration" className="block text-sm font-medium text-gray-700">
                                  Duration
                                </label>
                                <input
                                  type="text"
                                  name="duration"
                                  id="duration"
                                  value={formData.duration}
                                  onChange={(e) => setFormData({ ...formData, duration: e.target.value })}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                  required
                                />
                              </div>
                            </div>
                          </div>

                          {/* Right Column */}
                          <div>
                            <div className="space-y-6">
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
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                />
                              </div>

                              <div>
                                <label htmlFor="price" className="block text-sm font-medium text-gray-700">
                                  Price
                                </label>
                                <input
                                  type="text"
                                  name="price"
                                  id="price"
                                  value={formData.price}
                                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                  required
                                />
                              </div>

                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Status
                                </label>
                                <RadioGroup value={formData.status} onChange={(value) => setFormData({ ...formData, status: value })} className="mt-2">
                                  <RadioGroup.Label className="sr-only">Choose a status</RadioGroup.Label>
                                  <div className="grid grid-cols-2 gap-3">
                                    {statuses.map((status) => (
                                      <RadioGroup.Option
                                        key={status.value}
                                        value={status.value}
                                        className={({ active, checked }) =>
                                          classNames(
                                            status.value === 'active' ? 'cursor-pointer' : 'cursor-pointer',
                                            active ? 'ring-2 ring-indigo-600 ring-offset-2' : '',
                                            checked
                                              ? status.value === 'active'
                                                ? 'bg-indigo-600 text-white hover:bg-indigo-500'
                                                : 'bg-gray-200 text-gray-900'
                                              : 'ring-1 ring-inset ring-gray-300 bg-white text-gray-900 hover:bg-gray-50',
                                            'flex items-center justify-center rounded-md py-3 px-3 text-sm font-semibold uppercase sm:flex-1'
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
                        </div>

                        <div className="mt-8 sm:flex sm:flex-row-reverse">
                          <button
                            type="submit"
                            className="inline-flex w-full justify-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 sm:ml-3 sm:w-auto"
                          >
                            {service ? 'Save Changes' : 'Add Service'}
                          </button>
                          <button
                            type="button"
                            className="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto"
                            onClick={onClose}
                          >
                            Cancel
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}
