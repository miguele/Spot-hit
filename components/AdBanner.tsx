

import React, { useEffect, useState } from 'react';
// FIX: Import AdEventType to handle ad load errors correctly.
import { RewardedAd, RewardedAdEventType, TestIds, AdEventType } from 'react-native-google-mobile-ads';

// FIX: Replaced __DEV__ with process.env.NODE_ENV !== 'production' to fix "Cannot find name '__DEV__'" error.
const adUnitId = process.env.NODE_ENV !== 'production' ? TestIds.REWARDED : 'ca-app-pub-4001919766690685/9967881861';

const rewarded = RewardedAd.createForAdRequest(adUnitId, {
  requestNonPersonalizedAdsOnly: true,
});

interface AdBannerProps {
  onReward: () => void;
  children: (options: { showAd: () => void; loaded: boolean; error: any }) => React.ReactNode;
}

const AdBanner: React.FC<AdBannerProps> = ({ onReward, children }) => {
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    const unsubscribeLoaded = rewarded.addAdEventListener(RewardedAdEventType.LOADED, () => {
      setLoaded(true);
      console.log('Rewarded Ad: Loaded');
    });
    const unsubscribeEarned = rewarded.addAdEventListener(
      RewardedAdEventType.EARNED_REWARD,
      reward => {
        console.log('User earned reward of ', reward);
        onReward();
      },
    );
    // FIX: Replaced the incorrect string 'ad-load-failed' with AdEventType.ERROR.
     const unsubscribeError = rewarded.addAdEventListener(AdEventType.ERROR, (loadError) => {
        console.error('Rewarded Ad: Load failed', loadError);
        setError(loadError);
    });

    // Start loading the rewarded ad straight away
    rewarded.load();

    return () => {
      unsubscribeLoaded();
      unsubscribeEarned();
      unsubscribeError();
    };
  }, [onReward]);

  const showAd = () => {
    if (loaded) {
      rewarded.show();
    } else {
      console.log("Ad not loaded yet.");
      // Optionally reload
      rewarded.load();
    }
  };

  return <>{children({ showAd, loaded, error })}</>;
}

export default AdBanner;