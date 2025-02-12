import { db } from '@/app/lib/firebase';
import { collection, getDocs } from 'firebase/firestore';

export interface Booking {
  id?: string; // Optional ID for Firestore document IDs
  service: string; // Required ID of the service being booked
  customerName: string; // Required name of the customer
  email: string; // Required email of the customer
  phone: string; // Required phone number of the customer
  date: string; // Required date of the booking
  time: string; // Required time of the booking
  duration: string; // Required duration of the service
  notes: string; // Optional notes from the customer
  status: 'confirmed' | 'pending' | 'canceled'; // Required status of the booking
  therapist: string; // Required ID of the therapist
  createdAt: Date; // Required timestamp for creation
  updatedAt: Date; // Required timestamp for last update
}



export const fetchBookings = async (): Promise<Booking[]> => {
  const bookingsCollection = collection(db, 'bookings');
  const bookingSnapshot = await getDocs(bookingsCollection);
  return bookingSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })) as Booking[];
};

export const fetchBookingsFromFirestore = async (): Promise<Booking[]> => {
  const bookingsCollection = collection(db, 'bookings');
  const bookingSnapshot = await getDocs(bookingsCollection);
  return bookingSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })) as Booking[];
};
