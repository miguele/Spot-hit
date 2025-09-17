import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';

// TODO: Replace the following with your app's Firebase project configuration.
// See: https://firebase.google.com/docs/web/learn-more#config-object
const firebaseConfig = {
  apiKey: "AIzaSyATTwGm9LKc3PSCjG_epayXb2qo2g6APes",
  authDomain: "spot-hit-20534.firebaseapp.com",
  projectId: "spot-hit-20534",
  storageBucket: "spot-hit-20534.firebasestorage.app",
  messagingSenderId: "657400023218",
  appId: "1:657400023218:web:87afdd2c51ea3f4f9721ab",
  measurementId: "G-GQYF9ZRDR4"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Cloud Firestore and get a reference to the service
const db = getFirestore(app);

export { db };
