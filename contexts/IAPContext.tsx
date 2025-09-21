
import React, { createContext, useState, useEffect, ReactNode } from 'react';
import { Alert } from 'react-native';
// In a real app, you would use react-native-iap here
// import { initConnection, getProducts, requestPurchase, Purchase } from 'react-native-iap';

interface IAPContextType {
  isPremium: boolean;
  purchasePremium: () => void;
}

export const IAPContext = createContext<IAPContextType>({
  isPremium: false,
  purchasePremium: () => {},
});

const PREMIUM_SKU = 'com.spothit.adfree'; // Your product ID from Google Play Console

export const IAPProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isPremium, setIsPremium] = useState(false);

  // This effect would run once to check for previous purchases
  useEffect(() => {
    const checkForPremium = async () => {
      // --- REAL IMPLEMENTATION USING react-native-iap ---
      // try {
      //   await initConnection();
      //   const purchases = await getPurchases();
      //   const hasPremium = purchases.some(p => p.productId === PREMIUM_SKU);
      //   setIsPremium(hasPremium);
      // } catch (error) {
      //   console.error("IAP Init Error:", error);
      // }
      
      // --- MOCK IMPLEMENTATION ---
      console.log("Checking for premium purchase (mocked)...");
    };

    checkForPremium();
  }, []);

  const purchasePremium = async () => {
    // --- REAL IMPLEMENTATION ---
    // try {
    //   await requestPurchase({ skus: [PREMIUM_SKU] });
    //   // After a successful purchase, a listener would update the state.
    //   setIsPremium(true);
    //   Alert.alert("Purchase Successful!", "Enjoy Spot-Hit ad-free!");
    // } catch (error) {
    //   console.error("IAP Purchase Error:", error);
    //   Alert.alert("Purchase Failed", "Something went wrong. Please try again.");
    // }
    
    // --- MOCK IMPLEMENTATION ---
    Alert.alert(
      "Purchase Ad-Free",
      "This would open the Google Play purchase screen. For now, we'll unlock the ad-free version.",
      [
        { text: "Cancel" },
        { text: "OK", onPress: () => {
            setIsPremium(true);
            Alert.alert("Success!", "You now have the ad-free version.");
        }}
      ]
    );
  };
  
  return (
    <IAPContext.Provider value={{ isPremium, purchasePremium }}>
      {children}
    </IAPContext.Provider>
  );
};
