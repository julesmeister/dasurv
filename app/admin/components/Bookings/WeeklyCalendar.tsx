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

  // Generate time slots from 8 AM to 1 AM
  const timeSlots = [...Array(18)].map((_, i) => {
    const hour = i + 8; // Start from 8 AM
    return format(new Date(2000, 0, 1, hour), 'h:mm a');
  });

  // Group bookings by date and time
  const bookingsByDateTime = bookings.reduce((acc, booking) => {
    const bookingDate = booking.date;
    const bookingTime = format(new Date(`2000-01-01T${booking.time}`), 'h:mm a');
    
    if (!acc[`${bookingDate}-${bookingTime}`]) {
      acc[`${bookingDate}-${bookingTime}`] = [];
    }
    acc[`${bookingDate}-${bookingTime}`].push(booking);
    return acc;
  }, {} as Record<string, Booking[]>);

  return (
    <div className="bg-white shadow ring-1 ring-black ring-opacity-5">
      <div className="grid grid-cols-[80px_repeat(7,1fr)] gap-px border-b border-gray-300 bg-gray-200 text-center text-xs font-semibold leading-6 text-gray-700">
        <div className="bg-white py-2"></div>
        {weekDays.map((date) => (
          <div key={date.toString()} className="bg-white py-2">
            <div>{format(date, 'EEE')}</div>
            <div className="text-gray-500">{format(date, 'd')}</div>
          </div>
        ))}
      </div>
      <div className="grid grid-cols-[80px_repeat(7,1fr)] gap-px bg-gray-200">
        {timeSlots.map((timeSlot) => (
          <React.Fragment key={timeSlot}>
            <div className="bg-white px-2 py-2 text-xs text-gray-500 border-r border-gray-200">
              {timeSlot}
            </div>
            {weekDays.map((date) => {
              const dateStr = format(date, 'yyyy-MM-dd');
              const bookingsAtTime = bookingsByDateTime[`${dateStr}-${timeSlot}`] || [];

              return (
                <div
                  key={`${date.toString()}-${timeSlot}`}
                  className={`min-h-[60px] bg-white ${
                    isSameDay(date, currentDate) ? 'bg-blue-50' : ''
                  }`}
                >
                  <div className="h-full">
                    {bookingsAtTime.map((booking) => (
                      <div
                        key={booking.id}
                        onClick={() => onBookingClick?.(booking)}
                        className="h-full flex items-center justify-center px-1 py-0.5 text-xs font-semibold rounded cursor-pointer"
                        style={{
                          backgroundColor:
                            booking.status === 'confirmed'
                              ? '#dcfce8'
                              : booking.status === 'pending'
                              ? '#f7dc6f'
                              : '#fee2e2',
                          color:
                            booking.status === 'confirmed'
                              ? '#2dd490'
                              : booking.status === 'pending'
                              ? '#f97316'
                              : '#e02424',
                        }}
                      >
                        <div className="flex flex-col items-start justify-between">
                          <span className="truncate">{booking.customerName}</span>
                          <span className="text-xs text-gray-500">{booking.therapist}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
};

export default WeeklyCalendar;
