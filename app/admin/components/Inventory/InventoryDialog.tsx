/* eslint-disable @typescript-eslint/no-unused-vars */
'use client';

import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon, UserPlusIcon } from '@heroicons/react/24/outline';
import React from 'react';
import { InventoryItem, saveInventoryItem, updateInventoryItem } from '@/app/models/inventory';
import { Supplier, fetchSupplierNames } from '@/app/models/supplier';
import toast from 'react-hot-toast';
import { Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';

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
  const [expirationDate, setExpirationDate] = useState<string | undefined>(
    initialItem?.expirationDate ? initialItem.expirationDate.toISOString() : undefined
  );
  const [current, setCurrent] = useState(initialItem?.current || 0);
  const [minimum, setMinimum] = useState(initialItem?.minimum || 0);
  const [reorderLevel, setReorderLevel] = useState(initialItem?.reorderLevel || 0);
  const [imageUrl, setImageUrl] = useState(initialItem?.imageUrl || '');
  const [loading, setLoading] = useState(false);
  const [isSupplierDialogOpen, setIsSupplierDialogOpen] = useState(false);
  const [suppliers, setSuppliers] = useState<string[]>([]);
  const [selectedSupplier, setSelectedSupplier] = useState<string>('');
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const loadSuppliers = async () => {
      const supplierList = await fetchSupplierNames();
      console.log('Fetched suppliers:', supplierList);
      console.log('Type of fetched suppliers:', Array.isArray(supplierList));
      setSuppliers(supplierList);
    };
    loadSuppliers();
  }, []);

  useEffect(() => {
    console.log(initialItem?.supplier);
    if (initialItem) {
      setName(initialItem.name);
      setCategory(initialItem.category);
      setSupplier(initialItem.supplier);
      setSelectedSupplier(initialItem.supplier);
      setCost(initialItem.cost);
      setPrice(initialItem.price);
      setExpirationDate(initialItem.expirationDate ? initialItem.expirationDate.toISOString() : undefined);
      setCurrent(initialItem.current);
      setMinimum(initialItem.minimum);
      setReorderLevel(initialItem.reorderLevel);
      setImageUrl(initialItem.imageUrl || '');
    } else {
      setName('');
      setCategory('');
      setSupplier('');
      setSelectedSupplier('');
      setCost(0);
      setPrice(0);
      setExpirationDate(undefined);
      setCurrent(0);
      setMinimum(0);
      setReorderLevel(0);
      setImageUrl('');
    }
  }, [initialItem]);

  const handleSupplierSelect = (supplier: string) => {
    setSelectedSupplier(supplier);
    setIsSupplierDialogOpen(false);
  };

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
        supplier: selectedSupplier ?? supplier,
        cost,
        price,
        expirationDate: expirationDate ? new Date(expirationDate) : undefined,
        reorderLevel,
        imageUrl,
      };

      if (initialItem) {
        await updateInventoryItem(newItem);
      } else {
        await saveInventoryItem(newItem);
      }
      
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

  const filteredSuppliers = suppliers.filter(supplier =>
    supplier.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <>
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
                                  Item
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
                                <div className="mt-2 space-y-2">
                                  {['Textiles', 'Skincare', 'Food and Beverages', 'Face Masks', 'Body Wraps', 'Scrubs', 'Oils', 'Food & Beverages'].map((cat) => (
                                    <button
                                      type="button"
                                      key={cat}
                                      onClick={() => setCategory(cat)}
                                      className={`px-3 py-1 rounded-full text-sm font-medium ${category === cat ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'} hover:bg-blue-600 hover:text-white mr-2 mb-2`}
                                    >
                                      {cat}
                                    </button>
                                  ))}
                                </div>
                              </div>
                            </div>

                            {/* Second Column */}
                            <div className="space-y-6">
                              <div className="col-span-6 sm:col-span-3">
                                <label htmlFor="supplier" className="block text-sm font-medium text-gray-700">
                                  Supplier
                                </label>
                                <div className="mt-2 flex items-center relative">
                                  <input
                                    type="text"
                                    name="supplier"
                                    id="supplier"
                                    value={selectedSupplier}
                                    readOnly
                                    className=" block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                                    />
                                  <button
                                    type="button"
                                    onClick={() => setIsSupplierDialogOpen(true)}
                                    className="ml-2 inline-flex items-center rounded-md border border-gray-300 bg-white px-2.5 py-1.5 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                                  >
                                    <UserPlusIcon className="h-5 w-5 text-gray-400" aria-hidden="true" />
                                  </button>
                                  {isSupplierDialogOpen && (
                                    <div className="absolute z-10 bg-white shadow-lg rounded-md mt-2 p-4">
                                      <Input
                                        placeholder="Search suppliers..."
                                        value={searchTerm}
                                        onChange={(e) => setSearchTerm(e.target.value)}
                                        prefix={<SearchOutlined />}
                                        className="mb-2"
                                      />
                                      <ul className="max-h-60 overflow-y-auto">
                                        {filteredSuppliers.map((supplier) => (
                                          <li
                                            key={supplier}
                                            className="p-2 hover:bg-gray-200 cursor-pointer border-gray-300 rounded-md transition duration-200 ease-in-out"
                                            onClick={() => handleSupplierSelect(supplier)}
                                          >
                                            {supplier}
                                          </li>
                                        ))}
                                      </ul>
                                    </div>
                                  )}
                                </div>
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
                                  <br></br>
                                  <span className="text-xs text-gray-500">
                                    (The cost of the item to buy)
                                  </span>
                                </label>
                                <input
                                  type="text"
                                  id="cost"
                                  value={cost}
                                  onChange={(e) => setCost(Number(e.target.value))}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                                />
                              </div>
                              <div>
                                <label htmlFor="price" className="block text-sm font-medium text-gray-700">
                                  Price
                                  <br></br>
                                  <span className="text-xs text-gray-500">
                                    (The price of the item to sell)
                                  </span>
                                </label>
                                <input
                                  type="text"
                                  id="price"
                                  value={price}
                                  onChange={(e) => setPrice(Number(e.target.value))}
                                  className="mt-2 block w-full rounded-md border-0 px-3 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 sm:text-sm sm:leading-6"
                                />
                              </div>
                            </div>

                            {/* Third Column */}
                            <div className="space-y-6">
                              <div>
                                <label htmlFor="current" className="block text-sm font-medium text-gray-700">
                                  Current Stock<br></br>
                                  <span className="text-xs text-gray-500">
                                    (The number of items currently in stock)
                                  </span>
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
                                  Minimum Required<br></br>
                                  <span className="text-xs text-gray-500">
                                    (The minimum number of items that should be in stock)
                                  </span>
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
                                  Reorder Level<br></br>
                                  <span className="text-xs text-gray-500">
                                    (The level at which the item should be replenished)
                                  </span>
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
    </>
  );
};

export default InventoryDialog;