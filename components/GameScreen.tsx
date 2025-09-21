

import React, { useContext, useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import AdBanner from './AdBanner'; // Import the new AdBanner component
import { IAPContext } from '../contexts/IAPContext';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';

// FIX: Create styled versions of components to accept the className prop.
const StyledView = styled(View);
const StyledText = styled(Text);
const StyledTouchableOpacity = styled(TouchableOpacity);

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
        <StyledView className="flex-1 justify-center items-center p-4 bg-gray-900">
            <StyledText className="text-3xl text-white font-bold mb-8">Guess the Year!</StyledText>
            
            {/* The main game card would be here */}
            <StyledView className="bg-gray-800 p-10 rounded-2xl w-full max-w-sm items-center">
                 <StyledText className="text-white text-6xl font-bold">?</StyledText>
            </StyledView>

            {/* This AdBanner component handles the rewarded ad logic */}
            <AdBanner onReward={handleAdReward}>
                {({ showAd, loaded, error }) => (
                     <StyledTouchableOpacity
                        // The button is disabled if an ad is already shown, or if ads are loading/error
                        disabled={showHint || (!isPremium && (!loaded || error))}
                        onPress={isPremium ? handleShowHint : showAd}
                        className={`py-3 px-6 rounded-full mt-8 ${showHint ? 'bg-gray-600' : 'bg-yellow-500'}`}
                    >
                        <StyledText className="text-black font-bold text-lg">
                            {showHint ? 'Hint Used' : 'Get a Hint'}
                        </StyledText>
                    </StyledTouchableOpacity>
                )}
            </AdBanner>
            {!isPremium && <StyledText className="text-gray-500 text-xs mt-2">Watch an ad to reveal the decade</StyledText>}
        </StyledView>
    );
};

export default GameScreen;