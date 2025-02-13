/* eslint-disable @typescript-eslint/no-unused-vars */
import { collection, getDocs, limit, orderBy, query, startAfter, where, DocumentData, QueryDocumentSnapshot, Timestamp } from 'firebase/firestore';
import { db } from '@/app/lib/firebase';

export interface Transaction {
  id: string;
  date: Timestamp;
  customerName: string;
  serviceName: string;
  amount: number;
  paymentMethod: 'cash' | 'card' | 'gcash' | 'maya';
  status: 'completed' | 'pending' | 'failed';
}

export const fetchTransactions = async (
  pageSize: number = 10,
  lastDocument: QueryDocumentSnapshot<DocumentData> | null = null
) => {
  try {
    const transactionsRef = collection(db, 'transactions');
    
    // Query for paginated data
    let q = query(
      transactionsRef,
      orderBy('date', 'desc'),
      limit(pageSize)
    );

    if (lastDocument) {
      q = query(q, startAfter(lastDocument));
    }

    const snapshot = await getDocs(q);
    const transactions = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data(),
      // Ensure date is a Timestamp
      date: doc.data().date || Timestamp.now()
    })) as Transaction[];

    // Get total count using count() query
    const countSnapshot = await getDocs(query(transactionsRef));
    const totalCount = countSnapshot.size;

    return {
      transactions,
      totalCount,
      lastDoc: snapshot.docs[snapshot.docs.length - 1] || null
    };
  } catch (error) {
    console.error('Error fetching transactions:', error);
    throw error;
  }
};
