// /Users/julesmeister/Documents/GitHub/dasurv/app/models/inventory.ts
import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb, CACHE_DURATION } from '@/app/lib/db';
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
  lastDoc: QueryDocumentSnapshot<DocumentData> | undefined,
  totalCount: number,
  fromCache?: boolean
}> => {
  try {
    const now = Date.now();
    let totalCount = 0;

    // Try to get cached count first
    const cachedCount = await dexieDb.inventoryCounts
      .where('type')
      .equals('total')
      .first();

    // Check if we have a valid cached count
    if (cachedCount && (now - cachedCount.timestamp) < CACHE_DURATION) {
      totalCount = cachedCount.count;
    } else {
      // Get total count from Firebase
      const snapshot = await getDocs(collection(firebaseDb, COLLECTION_NAME));
      totalCount = snapshot.size;

      // Cache the count
      await dexieDb.inventoryCounts.put({
        type: 'total',
        count: totalCount,
        timestamp: now
      });
    }

    // Try to get cached inventory items if not paginating
    if (!startAfterDoc) {
      const cachedItems = await dexieDb.inventory
        .limit(pageLimit || 10)
        .toArray();

      if (cachedItems.length > 0 && (now - cachedItems[0].timestamp) < CACHE_DURATION) {
        return {
          items: cachedItems,
          lastDoc: undefined,
          totalCount,
          fromCache: true
        };
      }
    }

    // If cache miss or pagination, fetch from Firebase
    const inventoryRef = collection(firebaseDb, COLLECTION_NAME);
    
    // Build query
    let q = query(
      inventoryRef,
      orderBy('name'),
      limit(pageLimit || 10)
    );

    // If we have a last document, start after it
    if (startAfterDoc) {
      q = query(q, startAfter(startAfterDoc));
    }

    const querySnapshot = await getDocs(q);
    const lastDoc = querySnapshot.docs[querySnapshot.docs.length - 1] || undefined;
    
    const items = querySnapshot.docs.map(doc => ({ 
      ...doc.data(),
      id: parseInt(doc.id),
      expirationDate: doc.data().expirationDate?.toDate()
    } as InventoryItem));

    // Cache the fetched items
    if (!startAfterDoc) {
      await Promise.all(items.map(item => 
        dexieDb.inventory.put({
          ...item,
          timestamp: now
        })
      ));
    }

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
    const itemRef = doc(firebaseDb, COLLECTION_NAME, item.id.toString());
    const itemData = {
      ...item,
      expirationDate: item.expirationDate ? item.expirationDate : null
    };
    await setDoc(itemRef, itemData);
    
    // Update cache
    await dexieDb.inventory.put({
      ...item,
      timestamp: Date.now()
    });
  } catch (error) {
    console.error('Error saving inventory item:', error);
    throw error;
  }
};

export const updateInventoryItem = async (item: InventoryItem): Promise<void> => {
  try {
    const itemRef = doc(firebaseDb, COLLECTION_NAME, item.id.toString());
    const itemData = {
      ...item,
      expirationDate: item.expirationDate ? item.expirationDate : null
    };
    await updateDoc(itemRef, itemData);
    
    // Update cache
    await dexieDb.inventory.put({
      ...item,
      timestamp: Date.now()
    });
  } catch (error) {
    console.error('Error updating inventory item:', error);
    throw error;
  }
};

export const fetchLowStockCount = async (): Promise<number> => {
  try {
    const now = Date.now();

    // Try to get cached low stock count
    const cachedCount = await dexieDb.inventoryCounts
      .where('type')
      .equals('lowStock')
      .first();

    // Check if we have a valid cached count
    if (cachedCount && (now - cachedCount.timestamp) < CACHE_DURATION) {
      return cachedCount.count;
    }

    // If no valid cache, get from Firebase
    const snapshot = await getDocs(collection(firebaseDb, COLLECTION_NAME));
    const lowStockCount = snapshot.docs.reduce((count, doc) => {
      const data = doc.data();
      return data.current <= data.minimum ? count + 1 : count;
    }, 0);

    // Cache the count
    await dexieDb.inventoryCounts.put({
      type: 'lowStock',
      count: lowStockCount,
      timestamp: now
    });

    return lowStockCount;
  } catch (error) {
    console.error('Error fetching low stock count:', error);
    return 0;
  }
};