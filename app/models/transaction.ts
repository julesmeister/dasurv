/* eslint-disable @typescript-eslint/no-unused-vars */
import { collection, getDocs, limit, orderBy, query, startAfter, where, doc, updateDoc, DocumentData, QueryDocumentSnapshot, Timestamp } from 'firebase/firestore';
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

    if (cachedTransactions.length > 0) {
      console.log('Using cached transactions from Dexie.');
      console.log('Cached transaction dates:', cachedTransactions.map(t => t.date));
      return {
        transactions: cachedTransactions.map(t => {
          const currentDate = Timestamp.now();
          if (typeof t.date === 'number' && !isNaN(t.date)) {
            const transformedDate = new Timestamp(Math.floor(t.date / 1000), 0);
            return {
              ...t,
              date: transformedDate
            };
          } else {
            return {
              ...t,
              date: currentDate
            };
          }
        }) as Transaction[],
        totalCount: cachedCount && cachedCount.count ? cachedCount.count : 0,
        lastDoc: null // Reset pagination when using cache
      };
    }
    console.log('Fetching transactions from Firestore.');

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
    console.log('Raw data fetched from Firestore:', snapshot.docs.map(doc => doc.data()));
    console.log('Raw data fetched from Firestore (with IDs):', snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    const transactions = snapshot.docs.map(doc => {
      const fetchedDate = doc.data().date;
      const currentDate = Timestamp.now();
      console.log('Fetched date:', fetchedDate);
      console.log('Current date (Timestamp.now()):', currentDate);
      if (fetchedDate instanceof Timestamp) {
        return {
          id: doc.id,
          ...doc.data(),
          date: fetchedDate
        };
      } else {
        console.error('Invalid date fetched from Firestore:', fetchedDate);
        return {
          id: doc.id,
          ...doc.data(),
          date: currentDate
        };
      }
    }) as Transaction[];

    console.log('Transaction date before caching:', transactions.map(transaction => transaction.date));
    // Get total count
    const countSnapshot = await getDocs(query(transactionsRef));
    const totalCount = countSnapshot.size;

    // Cache the results
    console.log('Transactions date coming from Dexie:', transactions.map(transaction => transaction.date));
    console.log('Caching transactions with dates:', transactions.map(transaction => transaction.date));
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

export const updateTransactionStatus = async (id: string, newStatus: 'completed' | 'pending' | 'failed'): Promise<boolean> => {
  try {
    const transactionRef = doc(firebaseDb, 'transactions', id);
    await updateDoc(transactionRef, { status: newStatus });
    return true;
  } catch (error) {
    console.error('Error updating transaction status:', error);
    return false;
  }
};
