'use client';

import { Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import React, { useState } from 'react';
import { InventoryItem } from '../../../models/inventory';
import toast from 'react-hot-toast';

interface InventoryDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (item: InventoryItem) => void;
  initialItem?: InventoryItem;
}

const InventoryDialog: React.FC<InventoryDialogProps> = ({ isOpen, onClose, onSave, initialItem }) => {
  const [name, setName] = useState(initialItem?.name || '');
  const [category, setCategory] = useState(initialItem?.category || '');
  const [supplier, setSupplier] = useState(initialItem?.supplier || '');
  const [cost, setCost] = useState(initialItem?.cost || 0);
  const [price, setPrice] = useState(initialItem?.price || 0);
  const [expirationDate, setExpirationDate] = useState(initialItem?.expirationDate || '');
  const [current, setCurrent] = useState(initialItem?.current || 0);
  const [minimum, setMinimum] = useState(initialItem?.minimum || 0);
  const [reorderLevel, setReorderLevel] = useState(initialItem?.reorderLevel || 0);
  const [imageUrl, setImageUrl] = useState(initialItem?.imageUrl || '');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const newItem: InventoryItem = {
        id: initialItem ? initialItem.id : Date.now(),
        name,
        current,
        minimum,
        category,
        supplier,
        cost,
        price,
        expirationDate: expirationDate ? new Date(expirationDate) : undefined,
        reorderLevel,
        imageUrl,
      };
      onSave(newItem);
      toast.success(initialItem ? 'Item updated successfully' : 'Item added successfully');
      onClose();
    } catch (error) {
      console.error('Error saving inventory item:', error);
      toast.error('Failed to save inventory item');
    } finally {
      setLoading(false);
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
                        {initialItem ? 'Edit Inventory Item' : 'Add New Inventory Item'}
                      </Dialog.Title>
                      <form onSubmit={handleSubmit}>
                        <div className="grid grid-cols-3 gap-x-4 gap-y-6">
                          {/* First Column */}
                          <div className="space-y-6">
                            <div>
                              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                                Name
                              </label>
                              <input
                                type="text"
                                id="name"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                                required
                              />
                            </div>

                        

                            <div>
                              <label htmlFor="category" className="block text-sm font-medium text-gray-700">
                                Category
                              </label>
                              <input
                                type="text"
                                id="category"
                                value={category}
                                onChange={(e) => setCategory(e.target.value)}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                              />
                            </div>
                          </div>

                          {/* Second Column */}
                          <div className="space-y-6">
                            <div>
                              <label htmlFor="supplier" className="block text-sm font-medium text-gray-700">
                                Supplier
                              </label>
                              <input
                                type="text"
                                id="supplier"
                                value={supplier}
                                onChange={(e) => setSupplier(e.target.value)}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                              />
                            </div>

                            <div>
                              <label htmlFor="imageUrl" className="block text-sm font-medium text-gray-700">
                                Image URL
                              </label>
                              <input
                                type="text"
                                id="imageUrl"
                                value={imageUrl}
                                onChange={(e) => setImageUrl(e.target.value)}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                              />
                            </div>

                            <div>
                              <label htmlFor="cost" className="block text-sm font-medium text-gray-700">
                                Cost
                              </label>
                              <input
                                type="text"
                                id="cost"
                                value={cost}
                                onChange={(e) => setCost(Number(e.target.value))}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                              />
                            </div>
                          </div>

                          {/* Third Column */}
                          <div className="space-y-6">
                            <div>
                              <label htmlFor="price" className="block text-sm font-medium text-gray-700">
                                Price
                              </label>
                              <input
                                type="text"
                                id="price"
                                value={price}
                                onChange={(e) => setPrice(Number(e.target.value))}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                              />
                            </div>

                            <div>
                              <label htmlFor="current" className="block text-sm font-medium text-gray-700">
                                Current Stock
                              </label>
                              <input
                                type="text"
                                id="current"
                                value={current}
                                onChange={(e) => setCurrent(Number(e.target.value))}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                                required
                              />
                            </div>

                            <div>
                              <label htmlFor="minimum" className="block text-sm font-medium text-gray-700">
                                Minimum Required
                              </label>
                              <input
                                type="text"
                                id="minimum"
                                value={minimum}
                                onChange={(e) => setMinimum(Number(e.target.value))}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                                required
                              />
                            </div>

                            <div>
                              <label htmlFor="reorderLevel" className="block text-sm font-medium text-gray-700">
                                Reorder Level
                              </label>
                              <input
                                type="text"
                                id="reorderLevel"
                                value={reorderLevel}
                                onChange={(e) => setReorderLevel(Number(e.target.value))}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                              />
                            </div>

                            <div>
                              <label htmlFor="expirationDate" className="block text-sm font-medium text-gray-700">
                                Expiration Date
                              </label>
                              <input
                                type="date"
                                id="expirationDate"
                                value={expirationDate}
                                onChange={(e) => setExpirationDate(e.target.value)}
                                className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                              />
                            </div>
                          </div>
                        </div>

                        <div className="mt-8 flex justify-end gap-x-3">
                          <button
                            type="button"
                            onClick={onClose}
                            className="rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                          >
                            Cancel
                          </button>
                          <button
                            type="submit"
                            disabled={loading}
                            className="rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
                          >
                            {loading ? 'Saving...' : (initialItem ? 'Update' : 'Save')}
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

export default InventoryDialog;