import { db } from './firebase';
import { collection, getDocs, addDoc, updateDoc, doc, deleteDoc } from 'firebase/firestore';
import { Service } from '../models/service';

const servicesCollection = collection(db, 'services');

export const getServices = async (): Promise<Service[]> => {
  const snapshot = await getDocs(servicesCollection);
  return snapshot.docs.map(doc => ({
    id: doc.id,
    name: doc.data().name,
    icon: doc.data().icon || '',
    description: doc.data().description,
    duration: doc.data().duration,
    price: doc.data().price,
    status: doc.data().status,
  }));
};

export const addService = async (service: Omit<Service, 'id'>): Promise<string> => {
  const docRef = await addDoc(servicesCollection, {
    ...service,
    createdAt: new Date(),
    updatedAt: new Date(),
  });
  return docRef.id;
};

export const updateService = async (id: string, updates: Partial<Service>): Promise<void> => {
  const serviceRef = doc(db, 'services', id);
  await updateDoc(serviceRef, {
    ...updates,
    updatedAt: new Date(),
  });
};

export const deleteService = async (id: string): Promise<void> => {
  const serviceRef = doc(db, 'services', id);
  await deleteDoc(serviceRef);
};
