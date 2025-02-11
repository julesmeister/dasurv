import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "***REMOVED***",
  authDomain: "dasurv-353d2.firebaseapp.com",
  projectId: "dasurv-353d2",
  storageBucket: "dasurv-353d2.firebasestorage.app",
  messagingSenderId: "***REMOVED***",
  appId: "1:***REMOVED***:web:ce4c578f9c25a67ceb072f",
  measurementId: "***REMOVED***"
};

const app = initializeApp(firebaseConfig);

let analytics;
if (typeof window !== 'undefined') {
  analytics = getAnalytics(app);
}

const auth = getAuth(app);
const db = getFirestore(app);

export { app, db, auth, analytics };
