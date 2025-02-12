'use client';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { Supplier, addSupplier, updateSupplier, fetchSuppliers, deleteSupplier, getSupplierCount } from '../../../models/supplier';
import toast from 'react-hot-toast';

interface SuppliersDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSupplierAdded: () => void;
  supplier?: Supplier;
}

const SuppliersDialog: React.FC<SuppliersDialogProps> = ({ isOpen, onClose, onSupplierAdded, supplier }) => {
  const [name, setName] = useState(supplier?.name || '');
  const [contact, setContact] = useState(supplier?.contact || '');
  const [address, setAddress] = useState<string>(
    supplier?.address !== undefined ? String(supplier.address.toString()) : ''
  );

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const newSupplier: Supplier = {
      id: supplier ? supplier.id! : '',
      name,
      contact,
      address,
    };

    try {
      if (supplier) {
        await updateSupplier(supplier.id, newSupplier);
      } else {
        await addSupplier(newSupplier);
      }
      toast.success('Supplier saved successfully!');
      onSupplierAdded();
      onClose();
    } catch (error) {
      toast.error('Failed to save supplier.');
      console.log(error);
    }
  };

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" onClose={onClose} className="fixed inset-0 z-10 overflow-y-auto">
        <div className="flex items-center justify-center min-h-screen">
          <Transition.Child as={Fragment} enter="ease-out duration-300" enterFrom="opacity-0" enterTo="opacity-100" leave="ease-in duration-200" leaveFrom="opacity-100" leaveTo="opacity-0">
            <div className="fixed inset-0 bg-black opacity-30" />
          </Transition.Child>
          <Transition.Child as={Fragment} enter="ease-out duration-300" enterFrom="transform scale-95" enterTo="transform scale-100" leave="ease-in duration-200" leaveFrom="transform scale-100" leaveTo="transform scale-95">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <Dialog.Title className="text-lg font-medium leading-6 text-gray-900">Supplier</Dialog.Title>
              <form onSubmit={handleSubmit}>
                <div className="mt-2">
                  <input type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="Supplier Name" className="border rounded w-full" />
                  <input type="text" value={contact} onChange={(e) => setContact(e.target.value)} placeholder="Contact Info" className="border rounded w-full mt-2" />
                  <input type="text" value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Address" className="border rounded w-full mt-2" />
                </div>
                <div className="mt-4">
                  <button type="button" onClick={onClose} className="mr-2">Cancel</button>
                  <button type="submit" className="bg-blue-500 text-white rounded px-4 py-2">Save</button>
                </div>
              </form>
              <button className="absolute top-0 right-0 p-2" onClick={onClose}><XMarkIcon className="h-6 w-6 text-gray-500" /></button>
            </div>
          </Transition.Child>
        </div>
      </Dialog>
    </Transition.Root>
  );
};

export default SuppliersDialog;