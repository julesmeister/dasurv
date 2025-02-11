'use client';

import { useEffect, useState } from 'react';
import { collection, query, orderBy, onSnapshot } from 'firebase/firestore';
import { db } from '@/app/lib/firebase';
import type { Reservation } from '@/app/types';

export default function AdminDashboard() {
  const [reservations, setReservations] = useState<Reservation[]>([]);

  useEffect(() => {
    const q = query(
      collection(db, 'reservations'),
      orderBy('date', 'desc')
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const reservationList = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as Reservation[];
      setReservations(reservationList);
    });

    return () => unsubscribe();
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Admin Dashboard</h1>
      <div className="bg-white rounded-lg shadow">
        <div className="px-4 py-5 sm:p-6">
          <h2 className="text-lg font-medium">Recent Reservations</h2>
          {reservations.length === 0 ? (
            <p className="text-gray-500">No reservations found.</p>
          ) : (
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {reservations.map((reservation) => (
                  <tr key={reservation.id}>
                    <td className="px-6 py-4 whitespace-nowrap">{reservation.customerName}</td>
                    <td className="px-6 py-4 whitespace-nowrap">{reservation.date instanceof Date ? reservation.date.toLocaleDateString() : reservation.date}</td>
                    <td className="px-6 py-4 whitespace-nowrap">{reservation.time}</td>
                    <td className="px-6 py-4 whitespace-nowrap">{reservation.status || 'Pending'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
