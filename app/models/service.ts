/* eslint-disable @typescript-eslint/no-unused-vars */
import { db } from "@/app/lib/db";
import { db as firebaseDb } from "@/app/lib/firebase";
import {
  collection,
  getDocs,
  addDoc,
  updateDoc,
  deleteDoc,
  doc,
  DocumentData,
  QueryDocumentSnapshot
} from "firebase/firestore";

export interface Service {
  id?: string;
  name: string;
  description: string;
  price: number;
  status: string;
  duration: number;
  icon?: string;
  createdAt?: Date;
  updatedAt?: Date;
  lastDoc?: QueryDocumentSnapshot<DocumentData> | null;
  timestamp: number;
}

// Type for database operations
export type ServiceWithTimestamp = Service & {
  timestamp: number;
};

// Cache services in Dexie
export async function cacheServices() {
  try {
    const servicesRef = collection(firebaseDb, "services");
    const querySnapshot = await getDocs(servicesRef);
    const services = querySnapshot.docs.map((doc) => {
      const data = doc.data();
      // Only include serializable data
      return {
        id: doc.id,
        name: data.name,
        description: data.description,
        price: data.price,
        duration: data.duration,
        status: data.status,
        icon: data.icon,
        createdAt: data.createdAt?.toDate(),
        updatedAt: data.updatedAt?.toDate(),
        timestamp: Date.now(),
      } as ServiceWithTimestamp;
    });

    await db.transaction("rw", db.services, async () => {
      await db.services.clear();
      await db.services.bulkAdd(services);
    });

    return services;
  } catch (error) {
    console.error("Error caching services:", error);
    throw error;
  }
}

// Get all services (from cache first, then update cache from Firestore)
export async function getServices(
  pageSize?: number,
  lastDoc?: QueryDocumentSnapshot<DocumentData> | null
): Promise<{
  data: Service[];
  lastDoc: QueryDocumentSnapshot<DocumentData> | null;
  totalCount: number;
}> {
  try {
    // First try to get from cache
    let services = await db.services.toArray();

    // If cache is empty, fetch from Firestore and cache
    if (services.length === 0) {
      services = await cacheServices();
    }

    // Apply pagination if pageSize is provided
    if (pageSize) {
      services = services.slice(0, pageSize);
    }

    return {
      data: services,
      lastDoc: services.length > 0 ? null : null, // Since we're using client-side pagination with cache
      totalCount: services.length,
    };
  } catch (error) {
    console.error("Error getting services:", error);
    throw error;
  }
}

// Add a new service
export async function addService(service: Service) {
  try {
    const timestamp = Date.now();
    const serviceWithTimestamp = {
      ...service,
      timestamp,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    // Add to Firestore
    const docRef = await addDoc(collection(firebaseDb, "services"), serviceWithTimestamp);
    const newService = {
      ...serviceWithTimestamp,
      id: docRef.id,
    };

    // Add to cache
    await db.services.add(newService);

    return newService;
  } catch (error) {
    console.error("Error adding service:", error);
    throw error;
  }
}

// Update an existing service
export async function updateService(id: string, updates: Partial<Service>) {
  try {
    const timestamp = Date.now();
    const updatesWithTimestamp = {
      ...updates,
      timestamp,
      updatedAt: new Date(),
    };

    // Update in Firestore
    const serviceRef = doc(collection(firebaseDb, "services"), id);
    await updateDoc(serviceRef, updatesWithTimestamp);

    // Update in cache
    await db.services.update(id, updatesWithTimestamp);

    return { id, ...updatesWithTimestamp };
  } catch (error) {
    console.error("Error updating service:", error);
    throw error;
  }
}

// Delete a service
export async function deleteService(id: string) {
  try {
    // Delete from Firestore
    const serviceRef = doc(collection(firebaseDb, "services"), id);
    await deleteDoc(serviceRef);

    // Delete from cache
    await db.services.delete(id);
  } catch (error) {
    console.error("Error deleting service:", error);
    throw error;
  }
}

// Refresh services to clear cache and get new data
export async function refreshServices(
  itemsPerPage: number,
  lastDoc: QueryDocumentSnapshot<DocumentData> | null
): Promise<{
  data: Service[];
  lastDoc: QueryDocumentSnapshot<DocumentData> | null;
  totalCount: number;
}> {
  try {
    // Clear the existing cache
    await db.transaction("rw", db.services, async () => {
      await db.services.clear();
    });

    // Fetch new data from Firestore
    const servicesRef = collection(firebaseDb, "services");
    const querySnapshot = await getDocs(servicesRef);
    const services = querySnapshot.docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        name: data.name,
        description: data.description,
        price: data.price,
        duration: data.duration,
        status: data.status,
        icon: data.icon || "",
        createdAt: data.createdAt?.toDate(),
        updatedAt: data.updatedAt?.toDate(),
        timestamp: Date.now(), // Set timestamp to current time
      } as Service;
    });

    // Cache the new services
    await db.transaction("rw", db.services, async () => {
      await db.services.bulkAdd(services);
    });

    return {
      data: services,
      lastDoc: null, // You can set this based on your pagination logic
      totalCount: services.length,
    };
  } catch (error) {
    console.error("Error refreshing services:", error);
    throw error;
  }
}
