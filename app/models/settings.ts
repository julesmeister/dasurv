/* eslint-disable @typescript-eslint/no-unused-vars */
// /Users/julesmeister/Documents/GitHub/dasurv/app/models/settings.ts
import { db as firebaseDb } from '@/app/lib/firebase';
import { db as dexieDb } from '@/app/lib/db';
import { collection, getDocs, query, orderBy, limit, startAfter, getCountFromServer, DocumentData, QueryDocumentSnapshot, where, doc, updateDoc, addDoc, getDoc } from 'firebase/firestore';

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