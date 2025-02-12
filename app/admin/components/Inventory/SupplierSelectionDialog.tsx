'use client';

import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Supplier, fetchSuppliers } from '@/app/models/supplier';

interface SupplierSelectionDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (supplier: Supplier) => void;
}

const SupplierSelectionDialog: React.FC<SupplierSelectionDialogProps> = ({ isOpen, onClose, onSelect }) => {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const loadSuppliers = async () => {
      setLoading(true);
      try {
        const { suppliers } = await fetchSuppliers();
        setSuppliers(suppliers);
      } catch (error) {
        console.error('Error loading suppliers:', error);
      } finally {
        setLoading(false);
      }
    };

    if (isOpen) {
      loadSuppliers();
    }
  }, [isOpen]);

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" className="fixed inset-0 z-10 overflow-y-auto" onClose={onClose}>
        <div className="flex min-h-screen items-end justify-center px-4 pb-20 pt-4 text-center sm:block sm:p-0">
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

          <span className="hidden sm:inline-block sm:h-screen sm:align-middle" aria-hidden="true">
            &#8203;
          </span>

          <Transition.Child
            as={Fragment}
            enter="ease-out duration-300"
            enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            enterTo="opacity-100 translate-y-0 sm:scale-100"
            leave="ease-in duration-200"
            leaveFrom="opacity-100 translate-y-0 sm:scale-100"
            leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
          >
            <div className="inline-block transform overflow-hidden rounded-lg bg-white px-4 pb-4 pt-5 text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6 sm:align-middle">
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

              <div className="sm:flex sm:items-start">
                <div className="mt-3 w-full text-center sm:ml-4 sm:mt-0 sm:text-left">
                  <Dialog.Title as="h3" className="text-base font-semibold leading-6 text-gray-900">
                    Select Supplier
                  </Dialog.Title>
                  <div className="mt-4">
                    {loading ? (
                      <div className="flex justify-center py-4">
                        <div className="h-6 w-6 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
                      </div>
                    ) : (
                      <div className="max-h-60 overflow-y-auto">
                        {suppliers.map((supplier) => (
                          <button
                            key={supplier.id}
                            onClick={() => onSelect(supplier)}
                            className="w-full px-4 py-2 text-left hover:bg-gray-100 focus:outline-none"
                          >
                            <div className="font-medium text-gray-900">{supplier.name}</div>
                            <div className="text-sm text-gray-500">{supplier.contact}</div>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </Transition.Child>
        </div>
      </Dialog>
    </Transition.Root>
  );
};

export default SupplierSelectionDialog;
