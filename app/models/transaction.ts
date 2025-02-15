/* eslint-disable @typescript-eslint/no-unused-vars */
import { collection, getDocs, limit, orderBy, query, startAfter, where, doc, updateDoc, DocumentData, QueryDocumentSnapshot, Timestamp } from 'firebase/firestore';
import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb, CACHE_DURATION } from '@/app/lib/db';
import dayjs from 'dayjs'; // Import dayjs

export interface Transaction {
  id: string;
  date: Timestamp;
  customerName: string;
  serviceName: string;
  amount: number;
  bookingId?: string;
  paymentMethod: 'cash' | 'card' | 'gcash' | 'maya';
  status: 'completed' | 'pending' | 'failed';
}

export interface IndexedDBTransaction extends Transaction {
  timestamp: number;
}

export const transactionsCollection = collection(firebaseDb, 'transactions');

export const fetchTransactions = async (
  pageSize: number = 10,
  lastDocument: QueryDocumentSnapshot<DocumentData> | null = null,
  startDate: Timestamp | null = null,
  endDate: Timestamp | null = null,
  filterType: string = 'all'
) => {
  try {
    const now = Date.now();

    // Adjust date ranges based on filterType
    if (filterType === 'today') {
      startDate = Timestamp.fromDate(new Date(new Date().setHours(0, 0, 0, 0)));
      endDate = Timestamp.fromDate(new Date(new Date().setHours(23, 59, 59, 999)));
    } else if (filterType === 'week') {
      startDate = Timestamp.fromDate(dayjs().startOf('week').toDate());
      endDate = Timestamp.fromDate(dayjs().endOf('week').toDate());
    } else if (filterType === 'month') {
      startDate = Timestamp.fromDate(dayjs().startOf('month').toDate());
      endDate = Timestamp.fromDate(dayjs().endOf('month').toDate());
    } else if (filterType === '3months') {
      startDate = Timestamp.fromDate(dayjs().subtract(3, 'month').startOf('month').toDate());
      endDate = Timestamp.fromDate(dayjs().endOf('month').toDate());
    } else if (filterType === '6months') {
      startDate = Timestamp.fromDate(dayjs().subtract(6, 'month').startOf('month').toDate());
      endDate = Timestamp.fromDate(dayjs().endOf('month').toDate());
    } else if (filterType === 'year') {
      startDate = Timestamp.fromDate(dayjs().subtract(1, 'year').startOf('year').toDate());
      endDate = Timestamp.fromDate(dayjs().endOf('year').toDate());
    }

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

    if (startDate) {
      q = query(q, where('date', '>=', startDate));
    }

    if (endDate) {
      q = query(q, where('date', '<=', endDate));
    }

    if (lastDocument) {
      q = query(q, startAfter(lastDocument));
    }

    const snapshot = await getDocs(q);
    console.log('Raw data fetched from Firestore:', snapshot.docs.map(doc => doc.data()));
    console.log('Raw data fetched from Firestore (with IDs):', snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    const transactions = snapshot.docs.map(doc => ({
      id: doc.id,
      date: doc.data().date,
      customerName: doc.data().customerName,
      serviceName: doc.data().serviceName,
      amount: doc.data().amount,
      paymentMethod: doc.data().paymentMethod,
      status: doc.data().status,
    })) as Transaction[];

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

export const fetchFreshTransactions = async (pageSize: number = 10, lastDocument: QueryDocumentSnapshot<DocumentData> | null = null) => {
  // Clear outdated cache entries
  const now = Date.now();
  await dexieDb.transactions.where('timestamp').below(now - CACHE_DURATION).delete();
  await dexieDb.transactionCounts.where('timestamp').below(now - CACHE_DURATION).delete();

  // Fetch fresh transactions from Firestore
  const transactionsRef = transactionsCollection;
  let q = query(transactionsRef, orderBy('date', 'desc'), limit(pageSize));

  if (lastDocument) {
    q = query(q, startAfter(lastDocument));
  }

  const querySnapshot = await getDocs(q);
  const transactions = querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })) as Transaction[];

  // Save fetched transactions to cache
  const timestamp = Date.now();
  await dexieDb.transactions.bulkPut(transactions.map(t => ({ ...t, timestamp })));
  await dexieDb.transactionCounts.put({ timestamp, count: transactions.length });

  return {
    transactions,
    totalCount: transactions.length,
    lastDoc: querySnapshot.docs[querySnapshot.docs.length - 1] || null,
  };
};

export const fetchTransactionData = async (filter: string) => {
  let transactionsQuery;

  const now = new Date();

  switch (filter) {
    case 'lastWeek':
      const lastWeek = new Date(now);
      lastWeek.setDate(now.getDate() - 7);
      transactionsQuery = query(
        transactionsCollection,
        where('date', '>=', lastWeek),
        orderBy('date', 'desc'),
        limit(10)
      );
      break;
    case 'last6Months':
      const last6Months = new Date(now);
      last6Months.setMonth(now.getMonth() - 6);
      transactionsQuery = query(
        transactionsCollection,
        where('date', '>=', last6Months),
        orderBy('date', 'desc'),
        limit(10)
      );
      break;
    case 'lastYear':
      const lastYear = new Date(now);
      lastYear.setFullYear(now.getFullYear() - 1);
      transactionsQuery = query(
        transactionsCollection,
        where('date', '>=', lastYear),
        orderBy('date', 'desc'),
        limit(10)
      );
      break;
    case 'yearly':
      // Implement logic for yearly data if needed
      transactionsQuery = query(transactionsCollection, orderBy('date', 'desc'), limit(10));
      break;
    default:
      transactionsQuery = query(transactionsCollection, orderBy('date', 'desc'), limit(10));
      break;
  }
  const querySnapshot = await getDocs(transactionsQuery);
  const transactions = querySnapshot.docs.map((doc) => ({
    id: doc.id,
    date: doc.data().date,
    amount: doc.data().amount, // Ensure amount is included
    ...doc.data()
  }));

  return {
    labels: transactions.map((transaction) => transaction.date.toDate().toLocaleDateString()),
    values: transactions.map((transaction) => transaction.amount),
  };
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
