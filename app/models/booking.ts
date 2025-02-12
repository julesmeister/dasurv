import { db } from '@/app/lib/firebase';
import { collection, getDocs, query, orderBy, limit, startAfter, getCountFromServer, DocumentData, QueryDocumentSnapshot } from 'firebase/firestore';

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

export interface BookingPaginationResult {
  bookings: Booking[];
  lastDoc: QueryDocumentSnapshot<DocumentData> | null;
  totalCount: number;
}

export const fetchBookings = async (
  pageSize: number = 10,
  lastDoc?: QueryDocumentSnapshot<DocumentData> | null
): Promise<BookingPaginationResult> => {
  const bookingsCollection = collection(db, 'bookings');
  
  // Get total count
  const snapshot = await getCountFromServer(bookingsCollection);
  const totalCount = snapshot.data().count;

  console.log('Total count:', totalCount);

  // Build query
  let q = query(
    bookingsCollection,
    orderBy('createdAt', 'desc'),
    limit(pageSize)
  );

  // If we have a last document, start after it
  if (lastDoc) {
    q = query(q, startAfter(lastDoc));
  }

  const bookingSnapshot = await getDocs(q);
  const lastVisible = bookingSnapshot.docs[bookingSnapshot.docs.length - 1] || null;

  const bookings = bookingSnapshot.docs.map(doc => {
    const data = doc.data();
    // Convert Firestore Timestamps to Date objects
    return {
      id: doc.id,
      ...data,
      createdAt: data.createdAt?.toDate(),
      updatedAt: data.updatedAt?.toDate()
    };
  }) as Booking[];

  console.log('Fetched bookings:', bookings);

  return {
    bookings,
    lastDoc: lastVisible,
    totalCount
  };
};

export const fetchBookingsFromFirestore = async (): Promise<Booking[]> => {
  const bookingsCollection = collection(db, 'bookings');
  const bookingSnapshot = await getDocs(bookingsCollection);
  return bookingSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })) as Booking[];
};
