import { db } from '@/app/lib/db';
import { servicesCollection } from '@/app/lib/firebase';
import { getDocs, addDoc, updateDoc, doc, deleteDoc } from 'firebase/firestore';

export interface Service {
  id?: string;
  docId?: string;
  name: string;
  icon: string;
  description: string;
  duration: number;
  price: number;
  status: string;
  createdAt?: Date;
  updatedAt?: Date;
}

// Type for database operations
export type ServiceWithTimestamp = Service & {
  timestamp: number;
};

// Cache services in Dexie
export async function cacheServices() {
  try {
    const serviceSnapshot = await getDocs(servicesCollection);
    const services = serviceSnapshot.docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        docId: doc.id,
        name: data.name,
        icon: data.icon,
        description: data.description,
        duration: data.duration,
        price: data.price,
        status: data.status,
        timestamp: Date.now(),
        createdAt: data.createdAt?.toDate(),
        updatedAt: data.updatedAt?.toDate()
      } as ServiceWithTimestamp;
    });

    // Clear existing services and bulk add new ones
    await db.transaction('rw', db.services, async () => {
      await db.services.clear();
      await db.services.bulkAdd(services);
    });

    return services;
  } catch (error) {
    console.error('Error caching services:', error);
    throw error;
  }
}

// Get all services (from cache first, then update cache from Firestore)
export async function getServices() {
  try {
    // First try to get from cache
    let services = await db.services.toArray();

    // If cache is empty, fetch from Firestore and cache
    if (services.length === 0) {
      services = await cacheServices();
    }

    return services;
  } catch (error) {
    console.error('Error getting services:', error);
    throw error;
  }
}

// Add a new service
export async function addService(service: Omit<Service, 'id' | 'docId'>) {
  try {
    const timestamp = new Date();
    const serviceWithTimestamp = {
      ...service,
      timestamp: timestamp.getTime(),
      createdAt: timestamp,
      updatedAt: timestamp
    };

    // Add to Firestore
    const docRef = await addDoc(servicesCollection, serviceWithTimestamp);
    const newService = { ...serviceWithTimestamp, id: docRef.id, docId: docRef.id };

    // Add to cache
    await db.services.add(newService);

    return newService;
  } catch (error) {
    console.error('Error adding service:', error);
    throw error;
  }
}

// Update a service
export async function updateService(id: string, updates: Partial<Service>) {
  try {
    const timestamp = new Date();
    const updatesWithTimestamp = {
      ...updates,
      updatedAt: timestamp
    };

    // Update in Firestore
    const serviceRef = doc(servicesCollection, id);
    await updateDoc(serviceRef, updatesWithTimestamp);

    // Update in cache
    await db.services.update(id, updatesWithTimestamp);

    return { id, ...updatesWithTimestamp };
  } catch (error) {
    console.error('Error updating service:', error);
    throw error;
  }
}

// Delete a service
export async function deleteService(id: string) {
  try {
    // Delete from Firestore
    const serviceRef = doc(servicesCollection, id);
    await deleteDoc(serviceRef);

    // Delete from cache
    await db.services.delete(id);

    return id;
  } catch (error) {
    console.error('Error deleting service:', error);
    throw error;
  }
}
