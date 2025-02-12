// /Users/julesmeister/Documents/GitHub/dasurv/app/models/inventory.ts
import { db } from '@/app/lib/firebase';
import { collection, getDocs, doc, setDoc, updateDoc, query, orderBy, QueryDocumentSnapshot, DocumentData, startAfter, limit } from 'firebase/firestore';

export interface InventoryItem {
  id: number;
  name: string;
  current: number;
  minimum: number;
  category: string;
  supplier: string;
  cost: number;
  price: number;
  expirationDate?: Date;
  reorderLevel: number;
  imageUrl?: string;
}

const COLLECTION_NAME = 'inventory';

export const fetchInventoryItems = async (pageLimit?: number, startAfterDoc?: QueryDocumentSnapshot<DocumentData>): Promise<{
  items: InventoryItem[],
  lastDoc: QueryDocumentSnapshot<DocumentData> | null,
  totalCount: number
}> => {
  try {
    const inventoryRef = collection(db, COLLECTION_NAME);
    
    // Get total count
    const snapshot = await getDocs(collection(db, COLLECTION_NAME));
    const totalCount = snapshot.size;

    // Build query
    let q = query(
      inventoryRef,
      orderBy('name'),
      limit(pageLimit || 10)  // Default to 10 if no limit provided
    );

    // If we have a last document, start after it
    if (startAfterDoc) {
      q = query(q, startAfter(startAfterDoc));
    }

    const querySnapshot = await getDocs(q);
    const lastDoc = querySnapshot.docs[querySnapshot.docs.length - 1];
    
    const items = querySnapshot.docs.map(doc => ({ 
      ...doc.data(),
      id: parseInt(doc.id),
      expirationDate: doc.data().expirationDate?.toDate()
    } as InventoryItem));

    return {
      items,
      lastDoc,
      totalCount
    };
  } catch (error) {
    console.error('Error fetching inventory items:', error);
    throw error;
  }
};

export const saveInventoryItem = async (item: InventoryItem): Promise<void> => {
  try {
    const itemRef = doc(db, COLLECTION_NAME, item.id.toString());
    const itemData = {
      ...item,
      expirationDate: item.expirationDate ? item.expirationDate : null
    };
    await setDoc(itemRef, itemData);
  } catch (error) {
    console.error('Error saving inventory item:', error);
    throw error;
  }
};

export const updateInventoryItem = async (item: InventoryItem): Promise<void> => {
  try {
    const itemRef = doc(db, COLLECTION_NAME, item.id.toString());
    const itemData = {
      ...item,
      expirationDate: item.expirationDate ? item.expirationDate : null
    };
    await updateDoc(itemRef, itemData);
  } catch (error) {
    console.error('Error updating inventory item:', error);
    throw error;
  }
};

export const fetchLowStockCount = async (): Promise<number> => {
  try {
    const snapshot = await getDocs(collection(db, COLLECTION_NAME));
    const lowStockCount = snapshot.docs.reduce((count, doc) => {
      const data = doc.data();
      return data.current <= data.minimum ? count + 1 : count;
    }, 0);
    return lowStockCount;
  } catch (error) {
    console.error('Error fetching low stock count:', error);
    return 0;
  }
};