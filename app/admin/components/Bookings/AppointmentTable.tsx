'use client';

import React from 'react';
import { useEffect, useState } from 'react';
import { fetchBookings } from '@/app/models/booking';
import { Booking } from '@/app/models/booking';
import { doc, updateDoc } from 'firebase/firestore';
import { db } from '@/app/lib/firebase';
import toast from 'react-hot-toast';
import { format, parse } from 'date-fns';
import { UserPlusIcon } from '@heroicons/react/24/outline';

interface AppointmentTableProps {
  bookings: Booking[];
  onRefresh: () => void;
}

const AppointmentTable: React.FC<AppointmentTableProps> = ({ bookings, onRefresh }) => {
  const [openRow, setOpenRow] = useState<number | null>(null);

  const toggleRow = (index: number) => {
    setOpenRow(openRow === index ? null : index);
  };

  const handleStatusChange = async (bookingId: string, newStatus: 'confirmed' | 'pending' | 'canceled') => {
    const updatePromise = new Promise(async (resolve, reject) => {
      try {
        const bookingRef = doc(db, 'bookings', bookingId);
        await updateDoc(bookingRef, {
          status: newStatus,
          updatedAt: new Date()
        });
        resolve('Status updated successfully');
        onRefresh(); // Refresh the data after successful update
      } catch (error) {
        console.error('Error updating booking status:', error);
        reject(error);
      }
    });

    toast.promise(
      updatePromise,
      {
        loading: 'Updating status...',
        success: `Booking status updated to ${newStatus}`,
        error: 'Failed to update status'
      }
    );
  };

  return (
    <div className="bg-white shadow rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg font-medium leading-6 text-gray-900">Upcoming Appointments</h3>
        <div className="mt-4">
          <div className="flex flex-col">
            <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Customer Name</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Service</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Therapist</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {bookings.map((booking, index) => (
                        <React.Fragment key={booking.id}>
                          <tr>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{booking.customerName}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{booking.service}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{booking.date}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                              {format(parse(booking.time, 'HH:mm', new Date()), 'h:mm a')}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 justify-center">
                              {booking.therapist ? booking.therapist : (
                                <div className="flex justify-center">
                                  <UserPlusIcon className="h-5 w-5 text-gray-400" aria-hidden="true" />
                                </div>
                              )}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm relative group">
                              <span className={`${
                                booking.status === 'confirmed' ? 'text-green-600' :
                                booking.status === 'pending' ? 'text-yellow-600' :
                                'text-red-600'
                              }`}>
                                {booking.status}
                              </span>
                              <div className="hidden group-hover:flex absolute -right-2 top-1/2 transform -translate-y-1/2 bg-white shadow-lg rounded-lg p-1 z-10">
                                <button 
                                  className="p-1 hover:bg-green-100 rounded-l-lg" 
                                  title="Confirm"
                                  onClick={() => handleStatusChange(booking.id!, 'confirmed')}>
                                  <svg className="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                  </svg>
                                </button>
                                <button 
                                  className="p-1 hover:bg-yellow-100" 
                                  title="Pending"
                                  onClick={() => handleStatusChange(booking.id!, 'pending')}>
                                  <svg className="w-5 h-5 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                  </svg>
                                </button>
                                <button 
                                  className="p-1 hover:bg-red-100 rounded-r-lg" 
                                  title="Cancel"
                                  onClick={() => handleStatusChange(booking.id!, 'canceled')}>
                                  <svg className="w-5 h-5 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                  </svg>
                                </button>
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                              <button 
                                onClick={(e) => {
                                  e.stopPropagation();
                                  toggleRow(index);
                                }} 
                                className="flex items-center text-blue-500 hover:text-blue-700"
                              >
                                <svg className="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                                View Details
                              </button>
                            </td>
                          </tr>
                          {openRow === index && (
                            <tr>
                              <td colSpan={8} className="px-6 py-4">
                                <div className="bg-gray-100 p-4 rounded">
                                  <p><strong>Email:</strong> {booking.email}</p>
                                  <p><strong>Phone:</strong> {booking.phone}</p>
                                  <p><strong>Notes:</strong> {booking.notes ? booking.notes : 'No notes available'}</p>
                                  <p><strong>Created At:</strong> {new Date(booking.createdAt.seconds * 1000).toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: 'numeric' })}</p>
                                  <p><strong>Updated At:</strong> {new Date(booking.updatedAt.seconds * 1000).toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: 'numeric' })}</p>
                                  <p><strong>Service:</strong> {booking.service}</p>
                                  <p><strong>Booking ID:</strong> {booking.id}</p>
                                </div>
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default function AppointmentTableContainer() {
  const [bookings, setBookings] = useState<Booking[]>([]);

  const getBookings = async () => {
    const bookings = await fetchBookings();
    setBookings(bookings);
  };

  useEffect(() => {
    getBookings();
  }, []);

  return <AppointmentTable bookings={bookings} onRefresh={getBookings} />;
}
