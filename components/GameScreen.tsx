

import React, { useContext, useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import AdBanner from './AdBanner'; // Import the new AdBanner component
import { IAPContext } from '../contexts/IAPContext';

const GameScreen: React.FC = () => {
    const { isPremium } = useContext(IAPContext);
    const [showHint, setShowHint] = useState(false);

    // Mock song data
    const currentSong = { year: 1985 };

    const handleShowHint = () => {
        // For premium users, the hint is shown instantly.
        setShowHint(true);
        Alert.alert("Hint", `This song is from the ${Math.floor(currentSong.year / 10) * 10}s!`);
    };

    const handleAdReward = () => {
        // This function is called after the user successfully watches an ad.
        setShowHint(true);
        Alert.alert("Hint", `This song is from the ${Math.floor(currentSong.year / 10) * 10}s!`);
    };

    return (
        <View className="flex-1 justify-center items-center p-4 bg-gray-900">
            <Text className="text-3xl text-white font-bold mb-8">Guess the Year!</Text>
            
            {/* The main game card would be here */}
            <View className="bg-gray-800 p-10 rounded-2xl w-full max-w-sm items-center">
                 <Text className="text-white text-6xl font-bold">?</Text>
            </View>

            {/* This AdBanner component handles the rewarded ad logic */}
            <AdBanner onReward={handleAdReward}>
                {({ showAd, loaded, error }) => (
                     <TouchableOpacity
                        // The button is disabled if an ad is already shown, or if ads are loading/error
                        disabled={showHint || (!isPremium && (!loaded || error))}
                        onPress={isPremium ? handleShowHint : showAd}
                        className={`py-3 px-6 rounded-full mt-8 ${showHint ? 'bg-gray-600' : 'bg-yellow-500'}`}
                    >
                        <Text className="text-black font-bold text-lg">
                            {showHint ? 'Hint Used' : 'Get a Hint'}
                        </Text>
                    </TouchableOpacity>
                )}
            </AdBanner>
            {!isPremium && <Text className="text-gray-500 text-xs mt-2">Watch an ad to reveal the decade</Text>}
        </View>
    );
};

export default GameScreen;