import { collection, getDocs, limit, orderBy, query, startAfter, where, DocumentData, QueryDocumentSnapshot, Timestamp } from 'firebase/firestore';
import { db } from '@/app/lib/firebase';

export interface Appointment {
  id: string;
  customerName: string;
  service: string;
  date: Timestamp;
  time: string;
  therapist: string;
  status: 'confirmed' | 'pending' | 'cancelled' | 'completed';
}

export const fetchAppointments = async (
  pageSize: number = 10,
  lastDocument: QueryDocumentSnapshot<DocumentData> | null = null,
  type: 'upcoming' | 'history' = 'upcoming'
) => {
  try {
    const appointmentsRef = collection(db, 'appointments');
    const now = Timestamp.now();
    
    // Query for paginated data
    let q = query(
      appointmentsRef,
      where('date', type === 'upcoming' ? '>=' : '<', now),
      orderBy('date', type === 'upcoming' ? 'asc' : 'desc'),
      limit(pageSize)
    );

    if (lastDocument) {
      q = query(q, startAfter(lastDocument));
    }

    const snapshot = await getDocs(q);
    const appointments = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data(),
    })) as Appointment[];

    // Get total count for the current type (upcoming/history)
    const countQuery = query(
      appointmentsRef,
      where('date', type === 'upcoming' ? '>=' : '<', now)
    );
    const countSnapshot = await getDocs(countQuery);
    const totalCount = countSnapshot.size;

    return {
      appointments,
      totalCount,
      lastDoc: snapshot.docs[snapshot.docs.length - 1] || null
    };
  } catch (error) {
    console.error('Error fetching appointments:', error);
    throw error;
  }
};
