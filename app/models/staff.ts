import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb, CACHE_DURATION } from '@/app/lib/db';
import { collection, getDocs, addDoc, query, orderBy, limit, startAfter, DocumentData, QueryDocumentSnapshot, doc, deleteDoc, updateDoc, getDoc, where, getCountFromServer } from 'firebase/firestore';

export interface Staff {
  id?: string;
  name: string;
  specialties: string[];
  availability: 'Full-time' | 'Part-time';
  email: string;
  phone: string;
  active: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface StaffQueryResult {
  staffs: Staff[];
  lastDoc: QueryDocumentSnapshot<DocumentData> | null;
  totalCount: number;
  fromCache?: boolean;
}

export const staffsCollection = collection(firebaseDb, 'staffs');

export const fetchStaffs = async (
  itemsPerPage: number = 10,
  lastDocument: QueryDocumentSnapshot<DocumentData> | null = null,
  activeFilter?: boolean
): Promise<StaffQueryResult> => {
  try {
    console.log('Fetching staffs with params:', { itemsPerPage, hasLastDoc: !!lastDocument, activeFilter });
    const now = Date.now();
    let totalCount = 0;

    // Try to get cached count first
    let cachedCount;
    if (activeFilter !== undefined) {
      cachedCount = await dexieDb.staffCounts
        .where('active')
        .equals(activeFilter ? 1 : 0)
        .and(item => (now - item.timestamp) < CACHE_DURATION)
        .first();
    } else {
      cachedCount = await dexieDb.staffCounts
        .where('timestamp')
        .above(now - CACHE_DURATION)
        .first();
    }

    if (cachedCount) {
      totalCount = cachedCount.count;
    } else {
      // Get total count from Firebase
      const totalQueryConstraints = activeFilter !== undefined ? [where('active', '==', activeFilter)] : [];
      const totalQuery = query(staffsCollection, ...totalQueryConstraints);
      const countSnapshot = await getCountFromServer(totalQuery);
      totalCount = countSnapshot.data().count;

      // Cache the count
      await dexieDb.staffCounts.put({
        count: totalCount,
        timestamp: now,
        active: activeFilter ? 1 : 0
      });
    }

    // Try to get cached staff if not paginating
    if (!lastDocument) {
      const cachedStaffQuery = dexieDb.staffs
        .orderBy('createdAt')
        .reverse()
        .filter(staff => activeFilter === undefined || Boolean(staff.active) === activeFilter);

      const cachedStaffs = await cachedStaffQuery
        .limit(itemsPerPage)
        .toArray();

      if (cachedStaffs.length > 0 && (now - cachedStaffs[0].timestamp) < CACHE_DURATION) {
        return {
          staffs: cachedStaffs.map(staff => ({
            ...staff,
            createdAt: new Date(staff.createdAt),
            updatedAt: new Date(staff.updatedAt)
          })),
          lastDoc: null,
          totalCount,
          fromCache: true
        };
      }
    }
    
    // Create the base query first with where clause if needed
    let baseQuery = query(staffsCollection);
    
    if (activeFilter !== undefined) {
      baseQuery = query(baseQuery, where('active', '==', activeFilter));
    }

    // Add orderBy after where clause
    baseQuery = query(baseQuery, orderBy('createdAt', 'desc'));

    // Create the final query with pagination
    let q = query(baseQuery, limit(itemsPerPage));

    if (lastDocument) {
      q = query(baseQuery, startAfter(lastDocument), limit(itemsPerPage));
    }

    const querySnapshot = await getDocs(q);
    const staffs: Staff[] = [];
    querySnapshot.forEach((doc) => {
      const data = doc.data();
      staffs.push({
        id: doc.id,
        ...data,
        createdAt: data.createdAt.toDate(),
        updatedAt: data.updatedAt.toDate(),
      } as Staff);
    });

    // Cache the fetched staffs if not paginating
    if (!lastDocument) {
      await Promise.all(staffs.map(staff =>
        dexieDb.staffs.put({
          ...staff,
          timestamp: now,
          createdAt: staff.createdAt.getTime(),
          updatedAt: staff.updatedAt.getTime()
        } as Staff & { timestamp: number; createdAt: number; updatedAt: number })
      ));
    }

    return {
      staffs,
      lastDoc: querySnapshot.docs[querySnapshot.docs.length - 1] || null,
      totalCount
    };
  } catch (error) {
    console.error('Error fetching staffs:', error);
    throw error;
  }
};

export const addStaff = async (staffData: Omit<Staff, 'id' | 'createdAt' | 'updatedAt'>): Promise<Staff> => {
  const now = new Date();
  
  const docRef = await addDoc(staffsCollection, {
    ...staffData,
    createdAt: now,
    updatedAt: now
  });

  const newStaff = {
    id: docRef.id,
    ...staffData,
    createdAt: now,
    updatedAt: now
  };

  // Update cache
  await dexieDb.staffs.put({
    ...newStaff,
    timestamp: Date.now(),
    createdAt: newStaff.createdAt.getTime(),
    updatedAt: newStaff.updatedAt.getTime()
  } as Staff & { timestamp: number; createdAt: number; updatedAt: number });

  // Invalidate counts cache
  await dexieDb.staffCounts.clear();

  return newStaff;
};

export const deleteStaff = async (staffId: string): Promise<void> => {
  try {
    const staffRef = doc(staffsCollection, staffId);
    await deleteDoc(staffRef);

    // Remove from cache
    await dexieDb.staffs.delete(staffId);
    // Invalidate counts cache
    await dexieDb.staffCounts.clear();
  } catch (error) {
    console.error('Error deleting staff:', error);
    throw error;
  }
};

export const updateStaff = async (staffId: string, staffData: Partial<Staff>): Promise<void> => {
  try {
    const staffRef = doc(staffsCollection, staffId);
    const docSnapshot = await getDoc(staffRef);
    
    if (docSnapshot.exists()) {
      await updateDoc(staffRef, staffData);

      // Update cache
      const existingStaff = await dexieDb.staffs.get(staffId);
      if (existingStaff) {
        await dexieDb.staffs.put({
          ...existingStaff,
          ...staffData,
          timestamp: Date.now(),
          createdAt: existingStaff.createdAt.getTime(),
          updatedAt: staffData.updatedAt ? staffData.updatedAt.getTime() : existingStaff.updatedAt.getTime()
        } as Staff & { timestamp: number; createdAt: number; updatedAt: number });
      }

      // Invalidate counts cache if active status changed
      if ('active' in staffData) {
        await dexieDb.staffCounts.clear();
      }
    } else {
      throw new Error('Staff member not found');
    }
  } catch (error) {
    console.error('Error updating staff:', error);
    throw error;
  }
};

export const getActiveTherapistsCount = async (): Promise<number> => {
  try {
    const now = Date.now();

    // Try to get cached count first
    let cachedCount;
    if (true !== undefined) {
      cachedCount = await dexieDb.staffCounts
        .where('active')
        .equals(true ? 1 : 0)
        .and(item => (now - item.timestamp) < CACHE_DURATION)
        .first();
    } else {
      cachedCount = await dexieDb.staffCounts
        .where('timestamp')
        .above(now - CACHE_DURATION)
        .first();
    }

    if (cachedCount) {
      return cachedCount.count;
    }

    // If no valid cache, get from Firebase
    const q = query(
      staffsCollection,
      where('active', '==', true)
    );

    const snapshot = await getCountFromServer(q);
    const count = snapshot.data().count;

    // Cache the count
    await dexieDb.staffCounts.put({
      count,
      timestamp: now,
      active: true ? 1 : 0
    });

    return count;
  } catch (error) {
    console.error('Error getting active therapists count:', error);
    return 0;
  }
};
