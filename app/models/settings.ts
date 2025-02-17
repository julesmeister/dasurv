/* eslint-disable prefer-const */
/* eslint-disable @typescript-eslint/no-unused-vars */
// /Users/julesmeister/Documents/GitHub/dasurv/app/models/settings.ts
import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb } from '@/app/lib/db';
import { collection, getDocs, query, orderBy, limit, startAfter, getCountFromServer, DocumentData, QueryDocumentSnapshot, where, doc, updateDoc, addDoc, getDoc, deleteDoc } from 'firebase/firestore';
import { signOut } from 'firebase/auth';
import { auth } from '@/app/lib/firebase';

const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds


export interface UserRoleSettings {
  admin: boolean;
  editor: boolean;
  viewer: boolean;
}

export interface AppSettings {
  googleSignInVisible: boolean;
  userRoles: UserRoleSettings;
}

// Example of default settings
export const defaultSettings: AppSettings = {
  googleSignInVisible: true,
  userRoles: {
    admin: false,
    editor: false,
    viewer: false,
  },
};

export const settingsCollection = collection(firebaseDb, 'settings');

export const fetchSettings = async (): Promise<AppSettings> => {
  const now = Date.now();
  let cachedSettings = await dexieDb.settings.toArray();

  if (cachedSettings.length > 0 && (now - cachedSettings[0].timestamp) < CACHE_DURATION) {
    return cachedSettings[0]; // Return the cached settings directly
  }

  const settingsSnapshot = await getDocs(settingsCollection);
  const settingsData = settingsSnapshot.docs.map(doc => ({
    ...doc.data()
  }));

  const settings = settingsData[0]; // Assuming only one settings document

  await dexieDb.settings.put({
    ...settings,
    timestamp: now,
    googleSignInVisible: false,
    userRoles: { admin: false, editor: false, viewer: false }
  });

  // Ensure settings includes required properties
  return {
    googleSignInVisible: settings.googleSignInVisible ?? true,
    userRoles: settings.userRoles ?? { admin: false, editor: false, viewer: false },
  };
};

export const fetchGoogleSignInVisible = async (): Promise<boolean> => {
  const now = Date.now();
  let cachedSettings = await dexieDb.settings.toArray();

  if (cachedSettings.length > 0 && (now - cachedSettings[0].timestamp) < CACHE_DURATION) {
    return cachedSettings[0].googleSignInVisible ?? true; // Return cached value
  }

  const settingsSnapshot = await getDocs(settingsCollection);
  const settingsData = settingsSnapshot.docs.map(doc => ({
    ...doc.data()
  }));

  const settings = settingsData[0]; // Assuming only one settings document

  // Ensure to return the googleSignInVisible property
  return settings.googleSignInVisible ?? true;
};

export const addSettings = async (settingsData: AppSettings): Promise<AppSettings> => {
  const docRef = await addDoc(settingsCollection, settingsData);
  const newSettings = { id: docRef.id, ...settingsData };

  await dexieDb.settings.put({
    ...newSettings,
    timestamp: Date.now(),
    googleSignInVisible: false,
    userRoles: { admin: false, editor: false, viewer: false }
  });

  return newSettings;
};

export const updateSettings = async (settingsId: string, settingsData: Partial<AppSettings>): Promise<void> => {
  await dexieDb.settings.clear();

  const settingsSnapshot = await getDocs(settingsCollection);
  const settingsDoc = settingsSnapshot.docs[0]; // Assuming only one settings document

  if (settingsDoc) {
    await updateDoc(settingsDoc.ref, settingsData);

    const existingSettings = await dexieDb.settings.get(settingsDoc.id);
    if (existingSettings) {
      await dexieDb.settings.put({
        ...existingSettings,
        ...settingsData,
        timestamp: Date.now()
      });
    }
  } else {
    await addSettings({
      ...defaultSettings,
      ...settingsData,
    });
  }
};

export const deleteSettings = async (settingsId: string): Promise<void> => {
  const settingsRef = doc(settingsCollection, settingsId);
  await deleteDoc(settingsRef);
  await dexieDb.settings.delete(settingsId);
};

export const refreshSettings = async (): Promise<AppSettings> => {
  // Clear cached settings
  await dexieDb.settings.clear();

  // Fetch new settings
  return fetchSettings();
};

export const logout = async (): Promise<void> => {
  try {
    await signOut(auth);
    console.log('User logged out successfully.');
  } catch (error) {
    console.error('Error logging out:', error);
  }
};