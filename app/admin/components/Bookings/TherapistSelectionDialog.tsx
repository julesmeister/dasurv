'use client';

import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Staff, fetchStaffs } from '@/app/models/staff';

interface TherapistSelectionDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (therapist: Staff) => void;
}

const TherapistSelectionDialog: React.FC<TherapistSelectionDialogProps> = ({ isOpen, onClose, onSelect }) => {
  const [therapists, setTherapists] = useState<Staff[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const loadTherapists = async () => {
      setLoading(true);
      try {
        const { staffs } = await fetchStaffs(50, null, true);
        setTherapists(staffs);
      } catch (error) {
        console.error('Error loading therapists:', error);
      } finally {
        setLoading(false);
      }
    };

    if (isOpen) {
      loadTherapists();
    }
  }, [isOpen]);

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
          <div className="fixed inset-0 bg-black/30 backdrop-blur-sm transition-opacity" />
        </Transition.Child>

        <div className="fixed inset-0 z-10 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 translate-y-4"
              enterTo="opacity-100 translate-y-0"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 translate-y-0"
              leaveTo="opacity-0 translate-y-4"
            >
              <Dialog.Panel className="relative w-full max-w-sm transform overflow-hidden rounded-xl bg-white p-6 shadow-xl transition-all">
                <button
                  type="button"
                  className="absolute right-4 top-4 text-gray-400 hover:text-gray-500"
                  onClick={onClose}
                >
                  <span className="sr-only">Close</span>
                  <XMarkIcon className="h-5 w-5" aria-hidden="true" />
                </button>

                <Dialog.Title as="h3" className="text-lg font-medium text-gray-900">
                  Select Therapist
                </Dialog.Title>

                <div className="mt-6">
                  {loading ? (
                    <div className="flex justify-center py-4">
                      <div className="h-5 w-5 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
                    </div>
                  ) : (
                    <div className="space-y-1">
                      {therapists.map((therapist) => (
                        <button
                          key={therapist.id}
                          onClick={() => onSelect(therapist)}
                          className="w-full rounded-lg px-3 py-2 text-left text-sm font-medium text-gray-900 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        >
                          {therapist.name}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
};

export default TherapistSelectionDialog;
