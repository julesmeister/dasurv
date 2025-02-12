import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
import { getAuth } from "firebase/auth";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { getFirestore, collection, query, where, getDocs, QuerySnapshot, DocumentSnapshot } from "firebase/firestore";
import { Service } from '../models/service';

const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
  measurementId: process.env.NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID
};

const app = initializeApp(firebaseConfig);

let analytics;
if (typeof window !== 'undefined') {
  analytics = getAnalytics(app);
}

const auth = getAuth(app);
export const db = getFirestore(app);
export const servicesCollection = collection(db, 'services');

async function fetchActiveServices(): Promise<Service[]> {
  const servicesRef = servicesCollection;
  const q = query(servicesRef, where('status', '==', 'active'));
  const snapshot = await getDocs(q);
  const services: Service[] = [];
  
  snapshot.forEach((doc: DocumentSnapshot) => {
    const data = doc.data();
    services.push({
      id: doc.id,
      ...data
    } as Service);
  });

  return services;
}

export { app, auth, analytics, fetchActiveServices };
