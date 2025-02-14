/* eslint-disable @typescript-eslint/no-unused-vars */
import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb, CACHE_DURATION } from '@/app/lib/db';
import { collection, addDoc, updateDoc, doc, getDocs, deleteDoc, getCountFromServer, QueryDocumentSnapshot, DocumentData } from 'firebase/firestore';

export interface Supplier {
  id: string;
  name: string;
  contact: string;
  address?: string;
  email?: string;
  notes?: string;
  category?: 'Product' | 'Service' | 'Both';
  status: 'Active' | 'Inactive' | 'Pending';
  paymentTerms?: string;
  createdAt: number;
  updatedAt: number;
  lastOrderDate?: number;
  preferredPaymentMethod?: 'Cash' | 'Wire' | 'Check' | 'Credit Card';
}

export const suppliersCollection = collection(firebaseDb, 'suppliers');

export const addSupplier = async (supplier: Omit<Supplier, 'id'>): Promise<Supplier> => {
  const supplierRef = await addDoc(suppliersCollection, supplier);
  const newSupplier = { id: supplierRef.id, ...supplier };
  
  // Cache the new supplier
  await dexieDb.suppliers.add({
    ...newSupplier,
    timestamp: Date.now()
  });

  return newSupplier;
};

export const updateSupplier = async (supplierId: string, supplier: Partial<Supplier>): Promise<void> => {
  const supplierRef = doc(firebaseDb, 'suppliers', supplierId);
  await updateDoc(supplierRef, supplier);

  // Update cache
  await dexieDb.suppliers.update(supplierId, {
    ...supplier,
    timestamp: Date.now()
  });
};

export const fetchSuppliers = async (
  pageLimit?: number,
  offset?: number
): Promise<{
  suppliers: Supplier[],
  lastDoc: QueryDocumentSnapshot<DocumentData> | undefined,
  totalCount: number,
  fromCache?: boolean
}> => {
  const now = Date.now();
  let totalCount = 0;
  let fromCache = false;

  // Try to get cached count first
  const cachedCount = await dexieDb.supplierCounts
    .where('timestamp')
    .above(now - CACHE_DURATION)
    .first();

  // Check if we have a valid cached count
  if (cachedCount) {
    totalCount = cachedCount.count;
    fromCache = true;
  } else {
    // Get total count from Firebase
    const snapshot = await getDocs(collection(firebaseDb, 'suppliers'));
    totalCount = snapshot.size;

    // Cache the count
    await dexieDb.supplierCounts.put({
      count: totalCount,
      timestamp: now
    });
  }

  // Fetch suppliers from Firebase
  const querySnapshot = await getDocs(collection(firebaseDb, 'suppliers'));
  const suppliers = querySnapshot.docs.map(doc => ({
    ...doc.data(),
    id: doc.id
  } as Supplier));

  // Cache the fetched suppliers
  await Promise.all(suppliers.map(supplier =>
    dexieDb.suppliers.put({
      ...supplier,
      timestamp: now
    })
  ));

  const lastDoc = querySnapshot.docs[querySnapshot.docs.length - 1] || undefined;
  return {
    suppliers,
    lastDoc,
    totalCount,
    fromCache
  };
};

export const deleteSupplier = async (supplierId: string): Promise<void> => {
  const supplierRef = doc(firebaseDb, 'suppliers', supplierId);
  await deleteDoc(supplierRef);

  // Remove from cache
  await dexieDb.suppliers.delete(supplierId);
};

export const getSupplierCount = async (): Promise<number> => {
  const now = Date.now();

  // Try to get cached count first
  const cachedCount = await dexieDb.supplierCounts
    .where('timestamp')
    .above(now - CACHE_DURATION)
    .first();

  if (cachedCount) {
    return cachedCount.count;
  }

  // If no cache or expired, fetch from Firebase
  const snapshot = await getCountFromServer(suppliersCollection);
  const count = snapshot.data().count;

  // Cache the result
  await dexieDb.supplierCounts.put({
    count,
    timestamp: now
  });

  return count;
};
