/* eslint-disable @typescript-eslint/no-unused-vars */
import { collection, getDocs, limit, orderBy, query, startAfter, where, DocumentData, QueryDocumentSnapshot, Timestamp } from 'firebase/firestore';
import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb, CACHE_DURATION } from '@/app/lib/db';

export interface Transaction {
  id: string;
  date: Timestamp;
  customerName: string;
  serviceName: string;
  amount: number;
  paymentMethod: 'cash' | 'card' | 'gcash' | 'maya';
  status: 'completed' | 'pending' | 'failed';
}

export interface IndexedDBTransaction extends Transaction {
  timestamp: number;
}

export const transactionsCollection = collection(firebaseDb, 'transactions');

export const fetchTransactions = async (
  pageSize: number = 10,
  lastDocument: QueryDocumentSnapshot<DocumentData> | null = null
) => {
  try {
    const now = Date.now();

    // Try to get cached transactions first
    const cachedTransactions = await dexieDb.transactions
      .where('timestamp')
      .above(now - CACHE_DURATION)
      .toArray();

    const cachedCount = await dexieDb.transactionCounts
      .where('timestamp')
      .above(now - CACHE_DURATION)
      .first();

    if (cachedTransactions.length > 0 && cachedCount) {
      return {
        transactions: cachedTransactions.map(t => ({
          ...t,
          date: new Timestamp(typeof t.date === 'number' ? Math.floor(t.date / 1000) : Math.floor(Number(t.date) / 1000), 0)
        })) as Transaction[],
        totalCount: cachedCount.count,
        lastDoc: null // Reset pagination when using cache
      };
    }

    // If no cache, fetch from Firestore
    const transactionsRef = transactionsCollection;
    
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
      date: doc.data().date || Timestamp.now()
    })) as Transaction[];

    // Get total count
    const countSnapshot = await getDocs(query(transactionsRef));
    const totalCount = countSnapshot.size;

    // Cache the results
    await dexieDb.transactions.bulkPut(
      transactions.map(transaction => ({
        ...transaction,
        timestamp: now
      }))
    );

    await dexieDb.transactionCounts.put({
      count: totalCount,
      timestamp: now
    });

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
