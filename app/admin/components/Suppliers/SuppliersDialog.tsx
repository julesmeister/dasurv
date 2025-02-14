/* eslint-disable @typescript-eslint/no-unused-vars */
"use client";

import { Fragment, useEffect, useState } from "react";
import { Dialog, Transition, RadioGroup } from "@headlessui/react";
import { XMarkIcon } from "@heroicons/react/24/outline";
import { addSupplier, Supplier, deleteSupplier } from "@/app/models/supplier";
import toast from "react-hot-toast";
import { classNames } from "@/app/lib/utils";

interface DialogProps<T> {
  open: boolean;
  setOpen: (open: boolean) => void;
  title: string;
  initialData?: T;
  onSubmit: (data: T) => Promise<void>;
  refreshSuppliers: () => Promise<void>;
}

const statusOptions = [
  { value: "Active", label: "Active", color: "bg-green-600" },
  { value: "Inactive", label: "Inactive", color: "bg-red-600" },
  { value: "Pending", label: "Pending", color: "bg-yellow-600" },
];

const categoryOptions = [
  { value: "Product", label: "Product", color: "bg-blue-600" },
  { value: "Service", label: "Service", color: "bg-purple-600" },
  { value: "Both", label: "Both", color: "bg-indigo-600" },
];

const paymentMethodOptions = [
  { value: "Cash", label: "Cash", color: "bg-green-600" },
  { value: "Bank Transfer", label: "Bank Transfer", color: "bg-blue-600" },
  { value: "Check", label: "Check", color: "bg-yellow-600" },
  { value: "Credit Card", label: "Credit Card", color: "bg-purple-600" },
];
const handleAddSupplier = async (supplierData: Omit<Supplier, "id">) => {
  try {
    const newSupplier = await addSupplier(supplierData); // Call addSupplier to save the supplier
    toast.success("Supplier added successfully!"); // Notify success
    // Optionally refresh the supplier list or perform other actions
  } catch (error) {
    console.error("Error adding supplier:", error); // Log error
    toast.error("Failed to add supplier."); // Notify failure
  }
};
export default function SuppliersDialog<T extends Supplier>({
  open,
  setOpen,
  title,
  initialData,
  onSubmit,
  refreshSuppliers,
}: DialogProps<T>) {
  const [formData, setFormData] = useState<Omit<Supplier, "id">>({
    name: "",
    contact: "",
    category: "Product", // Default value
    status: "Active", // Default value
    notes: "",
    preferredPaymentMethod: "Cash", // Default value
    createdAt: Date.now(),
    updatedAt: Date.now(),
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (initialData) {
      console.log("Initial data:", initialData);
      setFormData(initialData);
    } else {
      setFormData({} as T);
    }
  }, [initialData]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    console.log("Submitting formData:", formData);

    try {
      await handleAddSupplier(formData);
      toast.success("Supplier saved successfully!");
      await refreshSuppliers();
      setOpen(false);
    } catch (error) {
      console.error("Error submitting form:", error);
      toast.error("Failed to save supplier.");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (name: keyof T, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [name]: value as T[keyof T],
    }));
  };

  const onDelete = async (id: string) => {
    try {
      await deleteSupplier(id); // Call the delete function
      toast.success("Supplier deleted successfully!");
      await refreshSuppliers();
      setOpen(false); // Optionally close the dialog
      // You may want to refresh the supplier list in the parent component
    } catch (error) {
      console.error("Error deleting supplier:", error);
      toast.error("Failed to delete supplier.");
    }
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
                    onClick={() => setOpen(false)}
                  >
                    <span className="sr-only">Close</span>
                    <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                  </button>
                </div>
                <div className="bg-white px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
                  <div className="sm:flex sm:items-start w-full">
                    <div className="w-full">
                      <Dialog.Title
                        as="h3"
                        className="text-base font-semibold leading-6 text-gray-900 mb-5"
                      >
                        {title}
                      </Dialog.Title>
                      <form onSubmit={handleSubmit}>
                        <div className="grid grid-cols-3 gap-x-8 gap-y-6">
                          {/* First Column */}
                          <div className="space-y-6">
                            <div>
                              <label
                                htmlFor="name"
                                className="block text-sm font-medium text-gray-700"
                              >
                                Name
                              </label>
                              <input
                                type="text"
                                id="name"
                                value={formData.name || ""}
                                onChange={(e) =>
                                  handleInputChange("name", e.target.value)
                                }
                                className="mt-2 block w-full rounded-md border-0 py-1.5 px-3 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                                required
                              />
                            </div>

                            <div>
                              <label
                                htmlFor="email"
                                className="block text-sm font-medium text-gray-700"
                              >
                                Email
                              </label>
                              <input
                                type="email"
                                id="email"
                                value={formData.email || ""}
                                onChange={(e) =>
                                  handleInputChange("email", e.target.value)
                                }
                                className="mt-2 block w-full rounded-md border-0 py-1.5 px-3 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                              />
                            </div>

                            <div>
                              <label className="block text-sm font-medium text-gray-700 mb-2">
                                Status
                              </label>
                              <RadioGroup
                                value={formData.status || "Active"}
                                onChange={(value) =>
                                  handleInputChange("status", value)
                                }
                              >
                                <RadioGroup.Label className="sr-only">
                                  Choose a status
                                </RadioGroup.Label>
                                <div className="grid grid-cols-2 gap-3">
                                  {statusOptions.map((option) => (
                                    <RadioGroup.Option
                                      key={option.value}
                                      value={option.value}
                                      className={({ active, checked }) =>
                                        classNames(
                                          "relative flex cursor-pointer rounded-lg px-5 py-3 border focus:outline-none",
                                          active &&
                                            "ring-2 ring-offset-2 ring-offset-white ring-indigo-500",
                                          checked
                                            ? `${option.color} border-transparent text-white`
                                            : "bg-white border-gray-200",
                                          "transition-all duration-200 ease-in-out transform hover:scale-[1.02]"
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
                                                    checked
                                                      ? "text-white"
                                                      : "text-gray-900"
                                                  }`}
                                                >
                                                  {option.label}
                                                </RadioGroup.Label>
                                              </div>
                                            </div>
                                            {checked && (
                                              <div className="shrink-0 text-white">
                                                <svg
                                                  className="h-6 w-6"
                                                  viewBox="0 0 24 24"
                                                  fill="none"
                                                >
                                                  <circle
                                                    cx="12"
                                                    cy="12"
                                                    r="12"
                                                    fill="white"
                                                    fillOpacity="0.2"
                                                  />
                                                  <path
                                                    d="M7 13l3 3 7-7"
                                                    stroke="white"
                                                    strokeWidth="1.5"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                  />
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

                          {/* Second Column */}
                          <div className="space-y-6">
                            <div>
                              <label
                                htmlFor="contact"
                                className="block text-sm font-medium text-gray-700"
                              >
                                Contact Number
                              </label>
                              <input
                                type="text"
                                id="contact"
                                value={formData.contact || ""}
                                onChange={(e) =>
                                  handleInputChange("contact", e.target.value)
                                }
                                className="mt-2 block w-full rounded-md border-0 py-1.5 px-3 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                                required
                              />
                            </div>

                            <div>
                              <label
                                htmlFor="address"
                                className="block text-sm font-medium text-gray-700"
                              >
                                Address
                              </label>
                              <textarea
                                id="address"
                                value={formData.address || ""}
                                onChange={(e) =>
                                  handleInputChange("address", e.target.value)
                                }
                                rows={2}
                                className="mt-2 block w-full rounded-md border-0 py-1.5 px-3 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                              />
                            </div>

                            <div>
                              <label className="block text-sm font-medium text-gray-700 mb-2">
                                Category
                              </label>
                              <RadioGroup
                                value={formData.category || "Product"}
                                onChange={(value) =>
                                  handleInputChange("category", value)
                                }
                              >
                                <RadioGroup.Label className="sr-only">
                                  Choose a category
                                </RadioGroup.Label>
                                <div className="grid grid-cols-2 gap-3">
                                  {categoryOptions.map((option) => (
                                    <RadioGroup.Option
                                      key={option.value}
                                      value={option.value}
                                      className={({ active, checked }) =>
                                        classNames(
                                          "relative flex cursor-pointer rounded-lg px-5 py-3 border focus:outline-none",
                                          active &&
                                            "ring-2 ring-offset-2 ring-offset-white ring-indigo-500",
                                          checked
                                            ? `${option.color} border-transparent text-white`
                                            : "bg-white border-gray-200",
                                          "transition-all duration-200 ease-in-out transform hover:scale-[1.02]"
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
                                                    checked
                                                      ? "text-white"
                                                      : "text-gray-900"
                                                  }`}
                                                >
                                                  {option.label}
                                                </RadioGroup.Label>
                                              </div>
                                            </div>
                                            {checked && (
                                              <div className="shrink-0 text-white">
                                                <svg
                                                  className="h-6 w-6"
                                                  viewBox="0 0 24 24"
                                                  fill="none"
                                                >
                                                  <circle
                                                    cx="12"
                                                    cy="12"
                                                    r="12"
                                                    fill="white"
                                                    fillOpacity="0.2"
                                                  />
                                                  <path
                                                    d="M7 13l3 3 7-7"
                                                    stroke="white"
                                                    strokeWidth="1.5"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                  />
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

                          {/* Third Column */}
                          <div className="space-y-6">
                            <div>
                              <label className="block text-sm font-medium text-gray-700 mb-2">
                                Payment Method
                              </label>
                              <RadioGroup
                                value={
                                  formData.preferredPaymentMethod || "Cash"
                                }
                                onChange={(value) =>
                                  handleInputChange(
                                    "preferredPaymentMethod",
                                    value
                                  )
                                }
                              >
                                <RadioGroup.Label className="sr-only">
                                  Choose a payment method
                                </RadioGroup.Label>
                                <div className="grid grid-cols-2 gap-3">
                                  {paymentMethodOptions.map((option) => (
                                    <RadioGroup.Option
                                      key={option.value}
                                      value={option.value}
                                      className={({ active, checked }) =>
                                        classNames(
                                          "relative flex cursor-pointer rounded-lg px-5 py-3 border focus:outline-none",
                                          active &&
                                            "ring-2 ring-offset-2 ring-offset-white ring-indigo-500",
                                          checked
                                            ? `${option.color} border-transparent text-white`
                                            : "bg-white border-gray-200",
                                          "transition-all duration-200 ease-in-out transform hover:scale-[1.02]"
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
                                                    checked
                                                      ? "text-white"
                                                      : "text-gray-900"
                                                  }`}
                                                >
                                                  {option.label}
                                                </RadioGroup.Label>
                                              </div>
                                            </div>
                                            {checked && (
                                              <div className="shrink-0 text-white">
                                                <svg
                                                  className="h-6 w-6"
                                                  viewBox="0 0 24 24"
                                                  fill="none"
                                                >
                                                  <circle
                                                    cx="12"
                                                    cy="12"
                                                    r="12"
                                                    fill="white"
                                                    fillOpacity="0.2"
                                                  />
                                                  <path
                                                    d="M7 13l3 3 7-7"
                                                    stroke="white"
                                                    strokeWidth="1.5"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                  />
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

                          {/* Full Width - Notes */}
                          <div className="col-span-3">
                            <label
                              htmlFor="notes"
                              className="block text-sm font-medium text-gray-700"
                            >
                              Notes
                            </label>
                            <textarea
                              id="notes"
                              value={formData.notes || ""}
                              onChange={(e) =>
                                handleInputChange("notes", e.target.value)
                              }
                              rows={1}
                              className="mt-2 block w-full rounded-md border-0 py-1.5 px-3 text-gray-900 shadow-sm sm:text-sm sm:leading-6"
                              placeholder="Any additional information about the supplier..."
                            />
                          </div>
                        </div>

                        <div className="mt-8 sm:mt-10 sm:grid sm:grid-flow-row-dense sm:grid-cols-3 sm:gap-3">
                          <button
                            type="submit"
                            disabled={loading}
                            className="inline-flex w-full justify-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 sm:col-start-2"
                          >
                            {loading ? "Saving..." : "Save"}
                          </button>
                          {initialData && (
                            <button
                              type="button"
                              onClick={() => onDelete(initialData.id)}
                              className="mt-3 inline-flex w-full justify-center rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-red-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600 sm:col-start-3 sm:mt-0"
                            >
                              Delete
                            </button>
                          )}
                          <button
                            type="button"
                            onClick={() => setOpen(false)}
                            className="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:col-start-1 sm:mt-0"
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
