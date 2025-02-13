import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb } from '@/app/lib/db';
import { collection, getDocs, query, orderBy, limit, startAfter, getCountFromServer, DocumentData, QueryDocumentSnapshot, where } from 'firebase/firestore';

const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

export interface Booking {
  id?: string;
  service: string;
  customerName: string;
  email: string;
  phone: string;
  date: string;
  time: string;
  duration: string;
  notes: string;
  status: 'confirmed' | 'pending' | 'canceled' | 'completed';
  therapist: string;
  createdAt: Date;
  updatedAt: Date;
  type?: 'upcoming' | 'history'; // Added for caching purposes
}

export interface BookingPaginationResult {
  bookings: Booking[];
  lastDoc: QueryDocumentSnapshot<DocumentData> | null;
  totalCount: number;
  fromCache?: boolean;
}

const getTotalCount = async (tab: 'upcoming' | 'history') => {
  const bookingsCollection = collection(firebaseDb, 'bookings');
  const now = new Date().toISOString().split('T')[0];
  const q = query(bookingsCollection, where('date', tab === 'upcoming' ? '>=' : '<', now));
  const snapshot = await getCountFromServer(q);
  return snapshot.data().count;
};

export const fetchBookings = async (
  pageSize: number = 10,
  lastDoc?: QueryDocumentSnapshot<DocumentData> | null,
  tab: 'upcoming' | 'history' = 'upcoming'
): Promise<BookingPaginationResult> => {
  try {
    // Try to get cached count first
    const cachedCount = await dexieDb.appointmentCounts
      .where('type')
      .equals(tab)
      .first();
    
    const now = Date.now();
    let totalCount = 0; // Initialize with a default value

    // Check if we have a valid cached count
    if (cachedCount && (now - cachedCount.timestamp) < CACHE_DURATION) {
      totalCount = cachedCount.count;
    }

    // Try to get cached bookings
    if (!lastDoc) {
      const cachedBookings = await dexieDb.appointments
        .where('type')
        .equals(tab)
        .limit(pageSize)
        .toArray();

      if (cachedBookings.length > 0) {
        totalCount = await getTotalCount(tab);
        return {
          bookings: cachedBookings,
          totalCount,
          lastDoc: null,
          fromCache: true
        };
      }
    }

    // If cache miss or pagination, fetch from Firebase
    const bookingsCollection = collection(firebaseDb, 'bookings');
    const currentDate = new Date().toISOString().split('T')[0];
    
    let q = query(
      bookingsCollection,
      where('date', tab === 'upcoming' ? '>=' : '<', currentDate),
      orderBy('date', tab === 'upcoming' ? 'asc' : 'desc'),
      limit(pageSize)
    );

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
        updatedAt: data.updatedAt?.toDate(),
        type: tab // Add type for caching
      };
    }) as Booking[];

    // Get total count if not already cached
    if (!totalCount) {
      totalCount = await getTotalCount(tab);

      // Cache the count
      await dexieDb.appointmentCounts.put({
        type: tab,
        count: totalCount,
        timestamp: now
      });
    }

    // Cache the bookings if this is the first page
    if (!lastDoc) {
      await dexieDb.appointments.bulkPut(bookings);
    }

    return {
      bookings,
      lastDoc: lastVisible,
      totalCount
    };
  } catch (error) {
    console.error('Error fetching bookings:', error);
    throw error;
  }
};

export const fetchBookingsFromFirestore = async (): Promise<Booking[]> => {
  const bookingsCollection = collection(firebaseDb, 'bookings');
  const bookingSnapshot = await getDocs(bookingsCollection);
  return bookingSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })) as Booking[];
};

export const getTodayConfirmedBookingsCount = async (): Promise<number> => {
  const bookingsCollection = collection(firebaseDb, 'bookings');
  
  // Get today's date at midnight
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  // Get tomorrow's date at midnight
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  // Create a query for confirmed bookings for today
  const q = query(
    bookingsCollection,
    where('date', '>=', today.toISOString().split('T')[0]),
    where('date', '<', tomorrow.toISOString().split('T')[0]),
    where('status', '==', 'confirmed')
  );

  const snapshot = await getCountFromServer(q);
  return snapshot.data().count;
};

// Function to clear any cached booking data
export const clearBookingsCache = async (): Promise<void> => {
  await dexieDb.appointments.clear();
  await dexieDb.appointmentCounts.clear();
};
