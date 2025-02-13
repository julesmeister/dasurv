/* eslint-disable @typescript-eslint/no-unused-vars */
'use client';

import { Fragment, useEffect, useState } from 'react';
import { Dialog, Transition, RadioGroup } from '@headlessui/react';
import { classNames } from '@/app/lib/utils';
import { DocumentData, QueryDocumentSnapshot } from 'firebase/firestore';

interface DialogProps<T> {
  open: boolean;
  setOpen: (open: boolean) => void;
  title: string;
  initialData?: T;
  onSubmit: (data: T) => Promise<void>;
  radioGroups?: {
    name: string;
    value: T;
    options: {
      label: string;
      value: T;
      color?: string;
    }[];
  }[];
  fields: {
    name: string;
    label: string;
    type: 'text' | 'email' | 'tel' | 'number' | 'date' | 'radio';
    placeholder?: string;
    required?: boolean;
    options?: {
      label: string;
      value: T;
      color?: string;
    }[];
  }[];
}

export default function StaffDialog<T extends Record<string, T>>({
  open,
  setOpen,
  title,
  initialData,
  onSubmit,
  radioGroups,
  fields
}: DialogProps<T>) {
  const [formData, setFormData] = useState<T>((initialData || {}) as T);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    } else {
      setFormData({} as T);
    }
  }, [initialData]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onSubmit(formData);
      setOpen(false);
    } catch (error) {
      console.error('Error submitting form:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (name: string, value: T) => {
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  return (
    <Transition.Root show={open} as={Fragment}>
      <Dialog as="div" className="relative z-10" onClose={setOpen}>
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
                      {title}
                    </Dialog.Title>
                    <div className="mt-2">
                      <form onSubmit={handleSubmit}>
                        <div className="space-y-4">
                          {fields.map((field) => (
                            <div key={field.name}>
                              <label className="block text-sm font-medium text-gray-700">
                                {field.label}
                              </label>
                              {field.type === 'radio' && field.options ? (
                                <RadioGroup
                                  value={formData[field.name]}
                                  onChange={(value) => handleInputChange(field.name, value)}
                                  className="mt-4"
                                >
                                  <div className="grid grid-cols-2 gap-4">
                                    {field.options.map((option) => (
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
                              ) : (
                                <input
                                  type={field.type}
                                  name={field.name}
                                  value={formData[field.name] || ''}
                                  onChange={(e) => handleInputChange(field.name, e.target.value)}
                                  placeholder={field.placeholder}
                                  required={field.required}
                                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                />
                              )}
                            </div>
                          ))}
                        </div>
                        <div className="mt-5 sm:mt-6 sm:grid sm:grid-flow-row-dense sm:grid-cols-2 sm:gap-3">
                          <button
                            type="submit"
                            disabled={loading}
                            className={`inline-flex w-full justify-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 sm:col-start-2 ${
                              loading ? 'opacity-50 cursor-not-allowed' : ''
                            }`}
                          >
                            {loading ? 'Saving...' : 'Save'}
                          </button>
                          <button
                            type="button"
                            className="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:col-start-1 sm:mt-0"
                            onClick={() => setOpen(false)}
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