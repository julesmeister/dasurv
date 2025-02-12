import { db } from '@/app/lib/firebase';
import { collection, getDocs, addDoc, query, orderBy, limit, startAfter, DocumentData, QueryDocumentSnapshot, doc, deleteDoc, updateDoc, getDoc } from 'firebase/firestore';

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
  lastDocument: QueryDocumentSnapshot<DocumentData> | null = null
): Promise<StaffQueryResult> => {
  try {
    console.log('Fetching staffs with params:', { itemsPerPage, hasLastDoc: !!lastDocument });
    let q = query(staffsCollection, orderBy('createdAt', 'desc'), limit(itemsPerPage));

    if (lastDocument) {
      q = query(staffsCollection, orderBy('createdAt', 'desc'), startAfter(lastDocument), limit(itemsPerPage));
    }

    const totalQuery = await getDocs(staffsCollection);
    console.log('Total staff count:', totalQuery.size);

    const querySnapshot = await getDocs(q);
    console.log('Fetched staff count:', querySnapshot.docs.length);
    
    const staffs = querySnapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data(),
      createdAt: doc.data().createdAt?.toDate(),
      updatedAt: doc.data().updatedAt?.toDate()
    })) as Staff[];

    const result = {
      staffs,
      lastDoc: querySnapshot.docs[querySnapshot.docs.length - 1] || null,
      totalCount: totalQuery.size
    };

    console.log('Returning staff result:', result);
    return result;
  } catch (error) {
    console.error('Error in fetchStaffs:', error);
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

export const seedStaffData: Omit<Staff, 'id'>[] = [
  {
    name: 'Sarah Chen',
    specialties: ['Deep Tissue Massage', 'Sports Therapy', 'Rehabilitation'],
    availability: 'Full-time',
    email: 'sarah.chen@example.com',
    phone: '+1 (555) 123-4567',
    active: true,
    createdAt: new Date('2024-01-15'),
    updatedAt: new Date('2024-01-15')
  },
  {
    name: 'Michael Rodriguez',
    specialties: ['Swedish Massage', 'Aromatherapy', 'Hot Stone Therapy'],
    availability: 'Part-time',
    email: 'michael.r@example.com',
    phone: '+1 (555) 234-5678',
    active: true,
    createdAt: new Date('2024-01-20'),
    updatedAt: new Date('2024-01-20')
  },
  {
    name: 'Emma Thompson',
    specialties: ['Thai Massage', 'Reflexology', 'Prenatal Massage'],
    availability: 'Full-time',
    email: 'emma.t@example.com',
    phone: '+1 (555) 345-6789',
    active: true,
    createdAt: new Date('2024-02-01'),
    updatedAt: new Date('2024-02-01')
  },
  {
    name: 'David Kim',
    specialties: ['Shiatsu', 'Acupressure', 'Sports Massage'],
    availability: 'Full-time',
    email: 'david.kim@example.com',
    phone: '+1 (555) 456-7890',
    active: true,
    createdAt: new Date('2024-02-05'),
    updatedAt: new Date('2024-02-05')
  }
];
