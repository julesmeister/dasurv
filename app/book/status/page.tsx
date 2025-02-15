'use client';

import React, { Suspense, useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { doc, getDoc } from 'firebase/firestore';
import { db } from '@/app/lib/firebase';
import { QRCodeSVG } from 'qrcode.react';
import { format } from 'date-fns';
import { DownloadOutlined } from '@ant-design/icons';

interface BookingData {
  customerName: string;
  email: string;
  phone: string;
  service: string;
  date: string;
  time: string;
  status: 'pending' | 'confirmed' | 'cancelled';
}

export default function BookingStatus() {
  const [booking, setBooking] = useState<BookingData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  return (
    <Suspense fallback={<div>Loading...</div>}>
      <BookingStatusInner booking={booking} setBooking={setBooking} loading={loading} setLoading={setLoading} error={error} setError={setError} />
    </Suspense>
  );
}

function BookingStatusInner({ booking, setBooking, loading, setLoading, error, setError }: { booking: BookingData | null; setBooking: React.Dispatch<React.SetStateAction<BookingData | null>>; loading: boolean; setLoading: React.Dispatch<React.SetStateAction<boolean>>; error: string | null; setError: React.Dispatch<React.SetStateAction<string | null>>; }) {
  const searchParams = useSearchParams();
  const bookingId = searchParams.get('id');
  const selectedFilter = searchParams.get('filter');

  useEffect(() => {
    async function fetchBooking() {
      if (!bookingId) {
        setError('No booking ID provided');
        setLoading(false);
        return;
      }

      try {
        const bookingRef = doc(db, 'bookings', bookingId);
        const bookingSnap = await getDoc(bookingRef);

        if (bookingSnap.exists()) {
          setBooking(bookingSnap.data() as BookingData);
        } else {
          setError('Booking not found');
        }
      } catch (err) {
        setError('Error fetching booking details');
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    fetchBooking();
  }, [bookingId, selectedFilter]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-gray-900 mb-4">Error</h2>
          <p className="text-gray-600">{error || 'Something went wrong'}</p>
        </div>
      </div>
    );
  }

  const formattedDate = format(new Date(booking.date), 'MMMM d, yyyy');
  const formattedTime = format(new Date(`2000-01-01T${booking.time}`), 'h:mm a');

  const downloadQRCode = () => {
    const svg = document.getElementById('qr-code-canvas') as SVGSVGElement | null;
    if (!svg) {
      console.error('SVG element not found');
      return;
    }
    const svgData = new XMLSerializer().serializeToString(svg);
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();

    img.onload = () => {
      canvas.width = img.width;
      canvas.height = img.height;
      ctx?.drawImage(img, 0, 0);
      const link = document.createElement('a');
      link.href = canvas.toDataURL('image/png');
      link.download = 'booking-qr-code.png';
      link.click();
    };

    img.src = 'data:image/svg+xml;base64,' + btoa(svgData);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <div className="px-4 py-5 sm:p-6">
            <div className="text-center mb-8">
              <h2 className="text-3xl font-bold text-gray-900 mb-2">Booking Status</h2>
              <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium
                ${booking.status === 'confirmed' ? 'bg-green-100 text-green-800' : 
                  booking.status === 'cancelled' ? 'bg-red-100 text-red-800' : 
                  'bg-yellow-100 text-yellow-800'}`}> 
                {booking.status.charAt(0).toUpperCase() + booking.status.slice(1)}
                {booking.status === 'pending' && <span className="ml-2 text-xs text-gray-500">(Client has not paid yet; appointment is reserved.)</span>}
              </span>
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 mb-8">
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">Booking Details</h3>
                <dl className="space-y-3">
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Name</dt>
                    <dd className="mt-1 text-sm text-gray-900">{booking.customerName}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Email</dt>
                    <dd className="mt-1 text-sm text-gray-900">{booking.email}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Phone</dt>
                    <dd className="mt-1 text-sm text-gray-900">{booking.phone}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Service</dt>
                    <dd className="mt-1 text-sm text-gray-900">{booking.service}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Date & Time</dt>
                    <dd className="mt-1 text-sm text-gray-900">{formattedDate} at {formattedTime}</dd>
                  </div>
                </dl>
              </div>

              {bookingId ? (
                <div className="flex flex-col items-center justify-center">
                  <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
                    Booking QR Code
                    <DownloadOutlined onClick={downloadQRCode} className="ml-6 cursor-pointer text-blue-600 hover:text-blue-800" />
                  </h3>
                  <div className="p-1 bg-white border rounded-xl shadow-sm border-gray-200">
                    <QRCodeSVG
                      id="qr-code-canvas"
                      value={window.location.href}
                      size={200}
                      level="H"
                      includeMargin={true}
                    />
                  </div>
                </div>
              ) : null}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}