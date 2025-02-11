'use client';

import { useState, useRef } from 'react';
import { addDoc, collection } from 'firebase/firestore';
import { db } from '@/app/lib/firebase';
import { format, addDays, isSameDay } from 'date-fns';
import { 
  UserIcon, 
  EnvelopeIcon, 
  PhoneIcon, 
  ClipboardDocumentListIcon,
  ChevronLeftIcon,
  ChevronRightIcon
} from '@heroicons/react/24/solid';
import { RadioGroup } from '@headlessui/react';
import { CheckCircleIcon } from '@heroicons/react/24/solid';

// Predefined time slots
const timeSlots = [
  { time: '09:00', label: '9:00 AM' },
  { time: '10:00', label: '10:00 AM' },
  { time: '11:00', label: '11:00 AM' },
  { time: '13:00', label: '1:00 PM' },
  { time: '14:00', label: '2:00 PM' },
  { time: '15:00', label: '3:00 PM' },
  { time: '16:00', label: '4:00 PM' },
  { time: '17:00', label: '5:00 PM' }
];

function classNames(...classes: string[]) {
  return classes.filter(Boolean).join(' ');
}

// Generate next 14 days
const generateDateRange = () => {
  const dates = [];
  const today = new Date();
  for (let i = 0; i < 14; i++) {
    dates.push(addDays(today, i));
  }
  return dates;
};

export default function BookingPage() {
  const [formData, setFormData] = useState({
    customerName: '',
    email: '',
    phone: '',
    date: format(new Date(), 'yyyy-MM-dd'),
    time: '',
    service: '',
    notes: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitStatus, setSubmitStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const dateRange = generateDateRange();
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const handleScroll = (direction: 'left' | 'right') => {
    if (scrollContainerRef.current) {
      const scrollAmount = direction === 'left' ? -200 : 200;
      scrollContainerRef.current.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setSubmitStatus('idle');
    
    try {
      const reservation = {
        ...formData,
        status: 'pending',
        createdAt: new Date(),
        updatedAt: new Date()
      };

      await addDoc(collection(db, 'reservations'), reservation);
      
      // Reset form and show success message
      setFormData({
        customerName: '',
        email: '',
        phone: '',
        date: format(new Date(), 'yyyy-MM-dd'),
        time: '',
        service: '',
        notes: ''
      });
      setSubmitStatus('success');
    } catch (error) {
      console.error('Error submitting reservation:', error);
      setSubmitStatus('error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <div className="relative isolate bg-white px-6 py-24 sm:py-32 lg:px-8">
      <div 
        className="absolute inset-x-0 top-[-10rem] -z-10 transform-gpu overflow-hidden blur-3xl sm:top-[-20rem]" 
        aria-hidden="true"
      >
        <div 
          className="relative left-[calc(50%-11rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-tr from-[#ff80b5] to-[#9089fc] opacity-30 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]"
          style={{
            clipPath: 'polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%)'
          }}
        />
      </div>
      <div className="mx-auto max-w-2xl text-center">
        <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-6xl">Book Your Wellness Journey</h1>
        <p className="mt-6 text-lg leading-8 text-gray-600">
          Personalize your spa experience. Select your preferred service, date, and time.
        </p>
      </div>
      <div className="mx-auto mt-16 max-w-xl sm:mt-20">
        <div className="bg-white/80 shadow-lg ring-1 ring-gray-900/5 sm:rounded-xl">
          <form onSubmit={handleSubmit} className="px-6 py-8 sm:p-10">
            {submitStatus === 'success' && (
              <div className="mb-4 p-4 bg-green-50 border border-green-200 text-green-800 rounded-md">
                Reservation submitted successfully! We&lsquo;ll confirm your booking soon.
              </div>
            )}
            {submitStatus === 'error' && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 text-red-800 rounded-md">
                Error submitting reservation. Please try again or contact support.
              </div>
            )}

            <div className="space-y-4">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <UserIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  type="text"
                  name="customerName"
                  id="customerName"
                  placeholder="Full Name"
                  required
                  value={formData.customerName}
                  onChange={handleChange}
                  className="block w-full rounded-md border-0 py-2 pl-10 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                />
              </div>

              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <EnvelopeIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  type="email"
                  name="email"
                  id="email"
                  placeholder="Email Address"
                  required
                  value={formData.email}
                  onChange={handleChange}
                  className="block w-full rounded-md border-0 py-2 pl-10 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                />
              </div>

              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <PhoneIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  type="tel"
                  name="phone"
                  id="phone"
                  placeholder="Phone Number"
                  required
                  value={formData.phone}
                  onChange={handleChange}
                  className="block w-full rounded-md border-0 py-2 pl-10 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                />
              </div>

              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <ClipboardDocumentListIcon className="h-5 w-5 text-gray-400" />
                </div>
                <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 3a1 1 0 01.707.293l3 3a1 1 0 01-1.414 1.414L10 5.414 7.707 7.707a1 1 0 01-1.414-1.414l3-3A1 1 0 0110 3zm-3.707 9.293a1 1 0 011.414 0L10 14.586l2.293-2.293a1 1 0 011.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" />
                  </svg>
                </div>
                <select
                  name="service"
                  id="service"
                  required
                  value={formData.service}
                  onChange={handleChange}
                  className="block w-full rounded-md border-0 py-2 pl-10 pr-10 text-gray-900 ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6 appearance-none"
                >
                  <option value="">Select a service</option>
                  <option value="massage">Massage Therapy</option>
                  <option value="facial">Facial Treatment</option>
                  <option value="body">Body Treatment</option>
                  <option value="wellness">Wellness Package</option>
                </select>
              </div>

              <div className="relative">
                <div className="flex items-center justify-between mb-2">
                  <span className="block text-sm font-medium text-gray-700">Select Date</span>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => handleScroll('left')}
                      className="p-1 rounded-full hover:bg-gray-100"
                    >
                      <ChevronLeftIcon className="h-5 w-5 text-gray-500" />
                    </button>
                    <button
                      type="button"
                      onClick={() => handleScroll('right')}
                      className="p-1 rounded-full hover:bg-gray-100"
                    >
                      <ChevronRightIcon className="h-5 w-5 text-gray-500" />
                    </button>
                  </div>
                </div>
                <div 
                  ref={scrollContainerRef}
                  className="flex overflow-x-auto hide-scrollbar gap-2 pb-2 pt-2 pl-2"
                  style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
                >
                  {dateRange.map((date) => (
                    <button
                      key={format(date, 'yyyy-MM-dd')}
                      type="button"
                      onClick={() => setFormData(prev => ({ ...prev, date: format(date, 'yyyy-MM-dd') }))}
                      className={classNames(
                        'flex-none w-20 py-2 px-3 rounded-lg text-center focus:outline-none transition-colors',
                        isSameDay(new Date(formData.date), date)
                          ? 'bg-indigo-600 text-white ring-2 ring-offset-2 ring-indigo-600'
                          : 'bg-white text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50'
                      )}
                    >
                      <div className="text-xs font-medium">
                        {format(date, 'EEE')}
                      </div>
                      <div className="text-lg font-semibold">
                        {format(date, 'd')}
                      </div>
                      <div className="text-xs">
                        {format(date, 'MMM')}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Time Slot Selection */}
              <RadioGroup 
                value={formData.time} 
                onChange={(value) => setFormData(prev => ({ ...prev, time: value }))}
                className="w-full"
              >
                <div className="space-y-2">
                  <RadioGroup.Label className="block text-sm font-medium text-gray-700 mb-2">Select Time</RadioGroup.Label>
                  <div className="grid grid-cols-3 gap-2">
                    {timeSlots.map((slot) => (
                      <RadioGroup.Option
                        key={slot.time}
                        value={slot.time}
                        className={({ checked }) =>
                          classNames(
                            'relative cursor-pointer focus:outline-none',
                            checked 
                              ? 'bg-indigo-600 text-white' 
                              : 'bg-white text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50',
                            'flex items-center justify-center rounded-md py-2 px-3 text-sm font-semibold uppercase'
                          )
                        }
                      >
                        {({ checked }) => (
                          <>
                            <RadioGroup.Label as="span">{slot.label}</RadioGroup.Label>
                            {checked && (
                              <span
                                className="absolute top-0 right-0 transform translate-x-1/2 -translate-y-1/2"
                              >
                                <CheckCircleIcon className="h-5 w-5 text-white" aria-hidden="true" />
                              </span>
                            )}
                          </>
                        )}
                      </RadioGroup.Option>
                    ))}
                  </div>
                </div>
              </RadioGroup>

              <div>
                <textarea
                  name="notes"
                  id="notes"
                  placeholder="Special Requests or Notes (Optional)"
                  rows={3}
                  value={formData.notes}
                  onChange={handleChange}
                  className="block w-full rounded-md border-0 py-2 px-3 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                />
              </div>
            </div>

            <div className="mt-6">
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex justify-center rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isSubmitting ? 'Submitting...' : 'Book Appointment'}
              </button>
            </div>
          </form>
        </div>
      </div>
      
    </div>
  );
}
