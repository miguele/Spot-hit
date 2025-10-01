

import React from 'react';

// This is now a mock component for web compatibility,
// as react-native-google-mobile-ads is not available on the web.

interface AdBannerProps {
  onReward: () => void;
  children: (options: { showAd: () => void; loaded: boolean; error: any }) => React.ReactNode;
}

const AdBanner: React.FC<AdBannerProps> = ({ children }) => {
  // Mock the ad state: not loaded and with an error to disable the button.
  const showAd = () => {
    console.warn("Ads are not available in the web version.");
  };

  // By returning loaded: false and an error, we ensure that any UI
  // expecting ads will be gracefully disabled.
  return <>{children({ showAd, loaded: false, error: new Error("Ads not supported on web") })}</>;
}

export default AdBanner;