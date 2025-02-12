import { db } from '@/app/lib/firebase';
import { collection, addDoc, updateDoc, doc, getDocs, deleteDoc, getCountFromServer } from 'firebase/firestore';

export interface Supplier {
  id: string;
  name: string;
  contact: string;
  address: string;
}

export const addSupplier = async (supplier: Omit<Supplier, 'id'>): Promise<Supplier> => {
  const supplierRef = await addDoc(collection(db, 'suppliers'), supplier);
  return { id: supplierRef.id, ...supplier };
};

export const updateSupplier = async (supplierId: string, supplier: Partial<Supplier>): Promise<void> => {
  const supplierRef = doc(db, 'suppliers', supplierId);
  await updateDoc(supplierRef, supplier);
};

export const fetchSuppliers = async (): Promise<{ suppliers: Supplier[], totalCount: number }> => {
  const snapshot = await getDocs(collection(db, 'suppliers'));
  const suppliers = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Supplier));
  
  const countSnapshot = await getCountFromServer(collection(db, 'suppliers'));
  const totalCount = countSnapshot.data().count; 

  return { suppliers, totalCount };
};

export const deleteSupplier = async (supplierId: string): Promise<void> => {
  const supplierRef = doc(db, 'suppliers', supplierId);
  await deleteDoc(supplierRef);
};

export const getSupplierCount = async (): Promise<number> => {
  const snapshot = await getCountFromServer(collection(db, 'suppliers'));
  return snapshot.data().count;
};
