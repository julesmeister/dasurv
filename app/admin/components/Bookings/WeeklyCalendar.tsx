'use client';

/** @jsxImportSource react */

import React from 'react';
import { format, startOfWeek, addDays, isSameDay } from 'date-fns';
import { Booking } from '@/app/models/booking';

interface WeeklyCalendarProps {
  bookings: Booking[];
  currentDate: Date;
  onBookingClick?: (booking: Booking) => void;
}

const WeeklyCalendar: React.FC<WeeklyCalendarProps> = ({
  bookings,
  currentDate,
  onBookingClick,
}) => {
  // Get the start of the week (Sunday)
  const startDate = startOfWeek(currentDate);

  // Generate array of 7 days
  const weekDays = [...Array(7)].map((_, i) => addDays(startDate, i));

  // Group bookings by date
  const bookingsByDate = bookings.reduce((acc, booking) => {
    const bookingDate = booking.date;
    if (!acc[bookingDate]) {
      acc[bookingDate] = [];
    }
    acc[bookingDate].push(booking);
    return acc;
  }, {} as Record<string, Booking[]>);

  return (
    <div className="bg-white shadow ring-1 ring-black ring-opacity-5">
      <div className="grid grid-cols-7 gap-px border-b border-gray-300 bg-gray-200 text-center text-xs font-semibold leading-6 text-gray-700">
        {weekDays.map((date) => (
          <div key={date.toString()} className="bg-white py-2">
            {format(date, 'EEE')}
          </div>
        ))}
      </div>
      <div className="grid grid-cols-7 gap-px bg-gray-200">
        {weekDays.map((date) => {
          const dateStr = format(date, 'yyyy-MM-dd');
          const dayBookings = bookingsByDate[dateStr] || [];

          return (
            <div
              key={date.toString()}
              className={`min-h-[120px] bg-white ${
                isSameDay(date, currentDate) ? 'bg-blue-50' : ''
              }`}
            >
              <div className="px-2 py-2">
                <div className="font-medium">{format(date, 'd')}</div>
                <div className="space-y-1 mt-2">
                  {dayBookings.map((booking) => (
                    <div
                      key={booking.id}
                      onClick={() => onBookingClick?.(booking)}
                      className={`text-xs p-1 rounded cursor-pointer truncate ${
                        booking.status === 'confirmed'
                          ? 'bg-green-100 text-green-800'
                          : booking.status === 'pending'
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-red-100 text-red-800'
                      }`}
                    >
                      {format(new Date(`2000-01-01T${booking.time}`), 'h:mm a')} - {booking.customerName}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default WeeklyCalendar;
