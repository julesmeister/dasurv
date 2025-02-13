import { db } from '@/app/lib/firebase';
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
}

export const staffsCollection = collection(db, 'staffs');

export const fetchStaffs = async (
  itemsPerPage: number = 10,
  lastDocument: QueryDocumentSnapshot<DocumentData> | null = null,
  activeFilter?: boolean
): Promise<StaffQueryResult> => {
  try {
    console.log('Fetching staffs with params:', { itemsPerPage, hasLastDoc: !!lastDocument, activeFilter });
    
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

    // For total count, use the same constraints except for ordering and pagination
    const totalQueryConstraints = activeFilter !== undefined ? [where('active', '==', activeFilter)] : [];
    const totalQuery = query(staffsCollection, ...totalQueryConstraints);
    const countSnapshot = await getCountFromServer(totalQuery);
    console.log('Total staff count:', countSnapshot.data().count);

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

    return {
      staffs,
      lastDoc: querySnapshot.docs[querySnapshot.docs.length - 1] || null,
      totalCount: countSnapshot.data().count,
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

  return {
    id: docRef.id,
    ...staffData,
    createdAt: now,
    updatedAt: now
  };
};

export const deleteStaff = async (staffId: string): Promise<void> => {
  try {
    const staffRef = doc(staffsCollection, staffId);
    await deleteDoc(staffRef);
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
    } else {
      throw new Error('Staff member not found');
    }
  } catch (error) {
    console.error('Error updating staff:', error);
    throw error;
  }
};

export const getActiveTherapistsCount = async (): Promise<number> => {
  const q = query(
    staffsCollection,
    where('active', '==', true)
  );

  const snapshot = await getCountFromServer(q);
  return snapshot.data().count;
};
