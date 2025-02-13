import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { getServices } from '@/app/lib/services';
import { Service } from '@/app/models/service';

interface ConfirmBookingDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (totalAmount: number) => void;
  serviceName: string;
}

export default function ConfirmBookingDialog({
  isOpen,
  onClose,
  onConfirm,
  serviceName,
}: ConfirmBookingDialogProps) {
  const [service, setService] = useState<Service | null>(null);
  const [additionalCost, setAdditionalCost] = useState(0);
  const [reduceCost, setReduceCost] = useState(0);
  const [totalAmount, setTotalAmount] = useState(0);

  useEffect(() => {
    const fetchService = async () => {
      const services = await getServices();
      const matchingService = services.find(s => s.name === serviceName);
      if (matchingService) {
        setService(matchingService);
        setTotalAmount(Number(matchingService.price));
      }
    };
    
    if (isOpen) {
      fetchService();
    }
  }, [isOpen, serviceName]);

  const handleAdditionalCostChange = (value: string) => {
    const cost = parseFloat(value) || 0;
    setAdditionalCost(cost);
    if (service) {
      setTotalAmount(Number(service.price) + cost - reduceCost);
    }
  };

  const handleReduceCostChange = (value: string) => {
    const cost = parseFloat(value) || 0;
    setReduceCost(cost);
    if (service) {
      setTotalAmount(Number(service.price) + additionalCost - cost);
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
                <div>
                  <div className="mt-3 text-center sm:mt-5">
                    <Dialog.Title as="h3" className="text-base font-semibold leading-6 text-gray-900">
                      Confirm Booking and Create Transaction
                    </Dialog.Title>
                    <div className="mt-4">
                      <div className="space-y-4">
                        <div className="text-left">
                          <label className="block text-sm font-medium text-gray-700">Service</label>
                          <p className="mt-1 text-sm text-gray-900">{serviceName}</p>
                        </div>
                        <div className="text-left">
                          <label className="block text-sm font-medium text-gray-700">Base Price</label>
                          <p className="mt-1 text-sm text-gray-900">₱{service ? Number(service.price).toFixed(2) : '0.00'}</p>
                        </div>
                        <div className="grid grid-cols-2 gap-3 text-left">
                          <div>
                            <label className="block text-sm font-medium text-gray-700">Additional Cost</label>
                            <input
                              type="text"
                              value={additionalCost}
                              onChange={(e) => handleAdditionalCostChange(e.target.value)}
                              className="mt-1 block w-full rounded-md border-gray-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                              placeholder="0.00"
                              step="0.01"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-gray-700">Reduce Cost</label>
                            <input
                              type="text"
                              value={reduceCost}
                              onChange={(e) => handleReduceCostChange(e.target.value)}
                              className="mt-1 block w-full rounded-md border-gray-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                              placeholder="0.00"
                              step="0.01"
                            />
                          </div>
                        </div>
                        <div className="text-left">
                          <label className="block text-sm font-medium text-gray-700">Total Amount</label>
                          <p className="mt-1 text-lg font-semibold text-gray-900">₱{totalAmount.toFixed(2)}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="mt-5 sm:mt-6 sm:grid sm:grid-flow-row-dense sm:grid-cols-2 sm:gap-3">
                  <button
                    type="button"
                    className="inline-flex w-full justify-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 sm:col-start-2"
                    onClick={() => onConfirm(totalAmount)}
                  >
                    Confirm
                  </button>
                  <button
                    type="button"
                    className="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:col-start-1 sm:mt-0"
                    onClick={onClose}
                  >
                    Cancel
                  </button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}
