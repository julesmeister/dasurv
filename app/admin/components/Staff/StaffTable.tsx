/* eslint-disable @typescript-eslint/no-unused-vars */
"use client";

import React, { useEffect, useState, useCallback } from "react";
import { Staff, fetchStaffs } from "@/app/models/staff";
import StaffDialog from "./StaffDialog";
import { QueryDocumentSnapshot, DocumentData } from "firebase/firestore";
import { classNames } from "@/app/lib/utils";
import { Tooltip } from '@/app/components/Tooltip';
import { ArrowPathIcon } from '@heroicons/react/24/outline';
import { toast } from "react-hot-toast";
import Table from "../Template/table";

const tabs = [
  { name: "Active", value: "Active" },
  { name: "Inactive", value: "Inactive" },
  { name: "Serviced", value: "Serviced" },
  { name: "Schedule", value: "Schedule" }
];

const StaffTable: React.FC = () => {
  const [staffs, setStaffs] = useState<Staff[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [lastDoc, setLastDoc] =
    useState<QueryDocumentSnapshot<DocumentData> | null>(null);
  const [editingStaff, setEditingStaff] = useState<Staff | undefined>(
    undefined
  );
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [selectedTab, setSelectedTab] = useState<string>("Active");
  const [counts, setCounts] = useState({ active: 0, inactive: 0 });
  const [totalCount, setTotalCount] = useState(0);
  const itemsPerPage = 10;

  const columns = [
    { header: "Name", accessor: "name" },
    { header: "Email", accessor: "email" },
    { header: "Phone", accessor: "phone" },
    {
      header: "Specialties",
      accessor: "specialties",
      render: (value: string[] | undefined) => value?.join(", "),
    },
    { header: "Availability", accessor: "availability" },
  ];

  const rowActions = (staff: React.SetStateAction<Staff | undefined>) => (
    <button type="button" onClick={() => setEditingStaff(staff)}>
      Edit
    </button>
  );

  const fetchData = async (
    pageSize: number,
    lastDoc: QueryDocumentSnapshot<DocumentData> | null,
    activeFilter?: boolean
  ) => {
    setLoading(true);
    try {
      const result = await fetchStaffs(pageSize, lastDoc, activeFilter);
      setStaffs(result.staffs);
      setTotalCount(result.totalCount);
      // Calculate counts
      const activeCount = result.staffs.filter((staff) => staff.active).length;
      const inactiveCount =
        result.staffs.length > 0 ? result.staffs.length - activeCount : 0;

      // Update counts state
      setCounts({ active: activeCount, inactive: inactiveCount });

      return {
        data: result.staffs,
        lastDoc: result.lastDoc,
        totalCount: result.totalCount,
      };
    } catch (error) {
      console.error("Error fetching staffs:", error);
      return { data: [], lastDoc: null, totalCount: 0 };
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(1, null, true); // Fetch active staff initially
  }, []);

  const handleTabChange = (tabValue: string) => {
    setSelectedTab(tabValue);
    if (staffs.length > 0) {
      const activeCount = staffs.filter((staff) => staff.active).length;
      const inactiveCount = staffs.length - activeCount;
      setCounts({ active: activeCount, inactive: inactiveCount });
    } else {
      if (tabValue === "Active") {
        fetchData(1, null, true);
      } else if (tabValue === "Inactive") {
        fetchData(1, null, false);
      } else {
        fetchData(1, null);
      }
    }
  };

  const handleAddStaff = () => {
    setIsAddDialogOpen(true);
  };

  return (
    <div className="mt-4 sm:mt-0 sm:flex-auto">
      <div className="hidden sm:block">
        <div className="flow-root">
          <div className="px-4 sm:px-6 lg:px-8">
            
            <div className="mt-4">
              <div className="sm:hidden">
                <label htmlFor="tabs" className="sr-only">
                  Select a tab
                </label>
                <select
                  id="tabs"
                  name="tabs"
                  className="block w-full rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500"
                  value={selectedTab}
                  onChange={(e) => setSelectedTab(e.target.value)}
                >
                  {tabs.map((tab) => (
                    <option key={tab.name} value={tab.value}>
                      {tab.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="hidden sm:block">
                <div className="border-b border-gray-200">
                  <nav className="-mb-px flex space-x-8" aria-label="Tabs">
                    {tabs.map((tab) => (
                      <button
                        key={tab.name}
                        onClick={() => setSelectedTab(tab.value)}
                        className={classNames(
                          tab.value === selectedTab
                            ? "border-indigo-500 text-indigo-600"
                            : "border-transparent text-gray-500 hover:border-gray-200 hover:text-gray-700",
                          "whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium"
                        )}
                      >
                        {tab.name}
                      </button>
                    ))}
                  </nav>
                </div>
              </div>
            </div>

            {(selectedTab === 'Active' || selectedTab === 'Inactive') && (
              <div className="mt-8 flow-root">
                <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                  <div className="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
                    <Table
                      title="Staff"
                      description="List of staff members"
                      columns={columns}
                      data={staffs.filter((staff) => staff.active === (selectedTab === 'Active'))}
                      initialTotalCount={totalCount}
                      fetchData={fetchData}
                      loading={loading}
                      rowActions={rowActions}
                      itemsPerPage={itemsPerPage}
                      actions={
                        <div className="flex space-x-2">
                          <button
                            type="button"
                            onClick={handleAddStaff}
                            className="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
                          >
                            Add Staff
                          </button>
                          <button
                            onClick={async () => {
                              try {
                                setLoading(true);
                                const { staffs: newStaffs, totalCount: newTotal } = await fetchStaffs(itemsPerPage, null, true);
                                setStaffs(newStaffs);
                                setTotalCount(newTotal);
                                setLastDoc(null);
                                setCurrentPage(1);
                                toast.success("Data refreshed successfully");
                              } catch (error) {
                                toast.error("Failed to refresh data");
                                console.error("Refresh error:", error);
                              } finally {
                                setLoading(false);
                              }
                            }}
                            className="inline-flex items-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                          >
                            <Tooltip content="Update list with latest bookings">
                              <ArrowPathIcon
                                className={`-ml-0.5 mr-1.5 h-5 w-5 ${
                                  loading ? "animate-spin" : ""
                                }`}
                                aria-hidden="true"
                              />
                            </Tooltip>
                            Refresh
                          </button>
                        </div>
                      }
                    />
                  </div>
                </div>
              </div>
            )}
            
            {selectedTab === 'Serviced' && (
              <div className="mt-8">
                {/* Add your Serviced content here */}
                <p>Serviced content coming soon...</p>
              </div>
            )}
            
            {selectedTab === 'Schedule' && (
              <div className="mt-8">
                {/* Add your Schedule content here */}
                <p>Schedule content coming soon...</p>
              </div>
            )}
          </div>
        </div>
      </div>

      <StaffDialog
        isOpen={isAddDialogOpen || !!editingStaff}
        onClose={() => {
          setIsAddDialogOpen(false);
          setEditingStaff(undefined);
        }}
        onStaffAdded={async () => {
          setLoading(true);
          await fetchData(1, null);
          setLoading(false);
          setIsAddDialogOpen(false);
          setEditingStaff(undefined);
        }}
        onStaffDeleted={async () => {
          setLoading(true);
          await fetchData(1, null);
          setLoading(false);
          setEditingStaff(undefined);
        }}
        staff={editingStaff}
      />
    </div>
  );
};

export default StaffTable;
