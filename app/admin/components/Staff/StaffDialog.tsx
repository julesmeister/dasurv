/* eslint-disable @typescript-eslint/no-unused-vars */
'use client';

import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition, RadioGroup } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Staff, addStaff, deleteStaff, updateStaff } from '@/app/models/staff';
import toast from 'react-hot-toast';
import { classNames } from '@/app/lib/utils';

interface StaffDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onStaffAdded: () => void;
  onStaffDeleted?: () => void;
  staff?: Staff;
}

const availabilityOptions = [
  { value: 'Full-time', label: 'Full-time', color: 'bg-green-500' },
  { value: 'Part-time', label: 'Part-time', color: 'bg-yellow-500' },
];

const statusOptions = [
  { value: true, label: 'Active', color: 'bg-green-500' },
  { value: false, label: 'Inactive', color: 'bg-gray-500' },
];

const StaffDialog: React.FC<StaffDialogProps> = ({ isOpen, onClose, onStaffAdded, onStaffDeleted, staff }) => {
  const [formData, setFormData] = useState<Omit<Staff, 'id' | 'createdAt' | 'updatedAt'>>({
    name: '',
    email: '',
    phone: '',
    active: true,
    specialties: [],
    availability: 'Full-time'
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (staff) {
      setFormData({
        name: staff.name,
        email: staff.email,
        phone: staff.phone,
        active: staff.active,
        specialties: staff.specialties,
        availability: staff.availability
      });
    } else {
      setFormData({
        name: '',
        email: '',
        phone: '',
        active: true,
        specialties: [],
        availability: 'Full-time'
      });
    }
  }, [staff]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (staff?.id) {
        // Update existing staff
        await updateStaff(staff.id, {
          ...formData,
          updatedAt: new Date()
        });
        toast.success('Staff member updated successfully');
      } else {
        // Add new staff
        await addStaff(formData);
        toast.success('Staff member added successfully');
      }
      onStaffAdded();
      onClose();
    } catch (error) {
      console.error('Error saving staff:', error);
      toast.error('Failed to save staff member');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!staff?.id) return;
    
    try {
      await deleteStaff(staff.id);
      toast.success('Staff member deleted successfully');
      onStaffDeleted?.();
      onClose();
    } catch (error) {
      console.error('Error deleting staff:', error);
      toast.error('Failed to delete staff member');
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
                    className="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-1 focus:ring-offset-2"
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
                        {staff ? 'Edit Staff Member' : 'Add New Staff Member'}
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
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                                  required
                                />
                              </div>

                              <div>
                                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                                  Email
                                </label>
                                <input
                                  type="email"
                                  name="email"
                                  id="email"
                                  value={formData.email}
                                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                                  required
                                />
                              </div>

                              <div>
                                <label htmlFor="phone" className="block text-sm font-medium text-gray-700">
                                  Phone
                                </label>
                                <input
                                  type="tel"
                                  name="phone"
                                  id="phone"
                                  value={formData.phone}
                                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                                  required
                                />
                              </div>
                            </div>
                          </div>

                          {/* Right Column */}
                          <div>
                            <div className="space-y-6">
                              <div>
                                <label htmlFor="specialties" className="block text-sm font-medium text-gray-700">
                                  Specialties (comma-separated)
                                </label>
                                <input
                                  type="text"
                                  name="specialties"
                                  id="specialties"
                                  value={formData.specialties.join(', ')}
                                  onChange={(e) => setFormData({ ...formData, specialties: e.target.value.split(',').map(s => s.trim()) })}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                                  placeholder="e.g., Massage, Therapy, Rehabilitation"
                                  required
                                />
                              </div>

                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Status
                                </label>
                                <RadioGroup value={formData.active} onChange={(value) => setFormData({ ...formData, active: value })} className="mt-4">
                                  <div className="grid grid-cols-2 gap-4">
                                    {statusOptions.map((option) => (
                                      <RadioGroup.Option
                                        key={String(option.value)}
                                        value={option.value}
                                        className={({ active, checked }) =>
                                          classNames(
                                            'relative flex cursor-pointer rounded-lg px-5 py-4 border focus:outline-none',
                                            active && 'ring-2 ring-offset-2 ring-offset-white ring-indigo-500',
                                            checked
                                              ? `${option.color} border-transparent bg-opacity-90 text-white`
                                              : 'bg-white border-gray-200',
                                            'transition-all duration-200 ease-in-out transform hover:scale-[1.02]'
                                          )
                                        }
                                      >
                                        {({ active, checked }) => (
                                          <>
                                            <div className="flex w-full items-center justify-between">
                                              <div className="flex items-center">
                                                <div className="text-sm">
                                                  <RadioGroup.Label
                                                    as="p"
                                                    className={`font-medium ${
                                                      checked ? 'text-white' : 'text-gray-900'
                                                    }`}
                                                  >
                                                    {option.label}
                                                  </RadioGroup.Label>
                                                </div>
                                              </div>
                                              {checked && (
                                                <div className="shrink-0 text-white">
                                                  <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none">
                                                    <circle cx="12" cy="12" r="12" fill="white" fillOpacity="0.2" />
                                                    <path d="M7 13l3 3 7-7" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                                                  </svg>
                                                </div>
                                              )}
                                            </div>
                                          </>
                                        )}
                                      </RadioGroup.Option>
                                    ))}
                                  </div>
                                </RadioGroup>
                              </div>

                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Availability
                                </label>
                                <RadioGroup value={formData.availability} onChange={(value) => setFormData({ ...formData, availability: value })} className="mt-4">
                                  <div className="grid grid-cols-2 gap-4">
                                    {availabilityOptions.map((option) => (
                                      <RadioGroup.Option
                                        key={option.value}
                                        value={option.value}
                                        className={({ active, checked }) =>
                                          classNames(
                                            'relative flex cursor-pointer rounded-lg px-5 py-4 border focus:outline-none',
                                            active && 'ring-2 ring-offset-2 ring-offset-white ring-indigo-500',
                                            checked
                                              ? `${option.color} border-transparent bg-opacity-90 text-white`
                                              : 'bg-white border-gray-200',
                                            'transition-all duration-200 ease-in-out transform hover:scale-[1.02]'
                                          )
                                        }
                                      >
                                        {({ active, checked }) => (
                                          <>
                                            <div className="flex w-full items-center justify-between">
                                              <div className="flex items-center">
                                                <div className="text-sm">
                                                  <RadioGroup.Label
                                                    as="p"
                                                    className={`font-medium ${
                                                      checked ? 'text-white' : 'text-gray-900'
                                                    }`}
                                                  >
                                                    {option.label}
                                                  </RadioGroup.Label>
                                                </div>
                                              </div>
                                              {checked && (
                                                <div className="shrink-0 text-white">
                                                  <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none">
                                                    <circle cx="12" cy="12" r="12" fill="white" fillOpacity="0.2" />
                                                    <path d="M7 13l3 3 7-7" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                                                  </svg>
                                                </div>
                                              )}
                                            </div>
                                          </>
                                        )}
                                      </RadioGroup.Option>
                                    ))}
                                  </div>
                                </RadioGroup>
                              </div>
                            </div>
                          </div>
                        </div>

                        <div className="mt-8 flex justify-end space-x-3">
                        {staff && (
                            <button
                              type="button"
                              className="inline-flex justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-1 focus:ring-offset-2"
                              onClick={handleDelete}
                            >
                              Delete Staff
                            </button>
                          )}
                           <button
                            type="button"
                            onClick={onClose}
                            className="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-1 focus:ring-offset-2"
                          >
                            Cancel
                          </button>
                          <button
                            type="submit"
                            disabled={loading}
                            className="inline-flex justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-1 focus:ring-offset-2"
                          >
                            {loading ? 'Saving...' : (staff ? 'Save Changes' : 'Add Staff')}
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
};

export default StaffDialog;
