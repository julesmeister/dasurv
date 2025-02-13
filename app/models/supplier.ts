/* eslint-disable @typescript-eslint/no-unused-vars */
import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb, CACHE_DURATION } from '@/app/lib/db';
import { collection, addDoc, updateDoc, doc, getDocs, deleteDoc, getCountFromServer } from 'firebase/firestore';

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
  preferredPaymentMethod?: 'Cash' | 'Bank Transfer' | 'Check' | 'Credit Card';
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

export const fetchSuppliers = async (): Promise<{ suppliers: Supplier[], totalCount: number }> => {
  const now = Date.now();

  // Try to get cached suppliers first
  const cachedSuppliers = await dexieDb.suppliers
    .where('timestamp')
    .above(now - CACHE_DURATION)
    .toArray();

  const cachedCount = await dexieDb.supplierCounts
    .where('timestamp')
    .above(now - CACHE_DURATION)
    .first();

  if (cachedSuppliers.length > 0 && cachedCount) {
    return {
      suppliers: cachedSuppliers.map(({ timestamp, ...supplier }) => supplier),
      totalCount: cachedCount.count
    };
  }

  // If no cache or expired, fetch from Firebase
  const snapshot = await getDocs(suppliersCollection);
  const suppliers = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Supplier));
  
  const countSnapshot = await getCountFromServer(suppliersCollection);
  const totalCount = countSnapshot.data().count;

  // Cache the results
  const supplierPromises = suppliers.map(supplier =>
    dexieDb.suppliers.put({
      ...supplier,
      timestamp: now
    })
  );

  await Promise.all([
    ...supplierPromises,
    dexieDb.supplierCounts.put({
      count: totalCount,
      timestamp: now
    })
  ]);

  return { suppliers, totalCount };
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
