import { db } from '@/app/lib/firebase';
import { collection, getDocs, query, orderBy, limit, startAfter, getCountFromServer, DocumentData, QueryDocumentSnapshot, where } from 'firebase/firestore';

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
  lastDoc?: QueryDocumentSnapshot<DocumentData> | null,
  tab: 'upcoming' | 'history' = 'upcoming'
): Promise<BookingPaginationResult> => {
  const bookingsCollection = collection(db, 'bookings');
  
  // Get total count for the specific tab
  let baseQuery = query(bookingsCollection);
  
  const now = new Date().toISOString().split('T')[0]; // Convert to YYYY-MM-DD format
  if (tab === 'upcoming') {
    baseQuery = query(baseQuery, where('date', '>=', now));
  } else {
    baseQuery = query(baseQuery, where('date', '<', now));
  }
  
  const snapshot = await getCountFromServer(baseQuery);
  const totalCount = snapshot.data().count;

  console.log('Total count:', totalCount);

  // Build query with tab filter
  let q = query(
    bookingsCollection,
    where('date', tab === 'upcoming' ? '>=' : '<', now),
    orderBy('date', tab === 'upcoming' ? 'asc' : 'desc'),
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

export const getTodayConfirmedBookingsCount = async (): Promise<number> => {
  const bookingsCollection = collection(db, 'bookings');
  
  // Get today's date at midnight
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  // Get tomorrow's date at midnight
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  // Create a query for confirmed bookings for today
  const q = query(
    bookingsCollection,
    where('status', '==', 'confirmed'),
    where('date', '>=', today.toISOString().split('T')[0]),
    where('date', '<', tomorrow.toISOString().split('T')[0])
  );

  // Get the count
  const snapshot = await getCountFromServer(q);
  return snapshot.data().count;
};
