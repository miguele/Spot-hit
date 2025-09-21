

import React, { useContext } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { IAPContext } from '../contexts/IAPContext';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';

// Mock navigation prop
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
type RootStackParamList = { Lobby: undefined; Game: undefined; };
type Props = NativeStackScreenProps<RootStackParamList, 'Lobby'>;

// FIX: Create styled versions of components to accept the className prop.
const StyledView = styled(View);
const StyledText = styled(Text);
const StyledTouchableOpacity = styled(TouchableOpacity);


const LobbyScreen: React.FC<Props> = ({ navigation }) => {
    const { isPremium, purchasePremium } = useContext(IAPContext);

    const handleStartGame = () => {
        navigation.navigate('Game');
    };

    return (
        <StyledView className="flex-1 justify-center items-center p-4 bg-gray-900">
            <StyledText className="text-3xl text-white font-bold mb-4">Game Lobby</StyledText>
            
            {/* Player List, Playlist selector etc. would go here */}
            <StyledView className="bg-gray-800 p-6 rounded-2xl w-full max-w-sm mb-8">
                <StyledText className="text-white text-center">Players will be listed here...</StyledText>
            </StyledView>

            <StyledTouchableOpacity onPress={handleStartGame} className="bg-[#1DB954] py-3 px-6 rounded-full mb-4">
                <StyledText className="text-black font-bold text-lg">Start Game</StyledText>
            </StyledTouchableOpacity>

            {!isPremium && (
                 <StyledTouchableOpacity onPress={purchasePremium} className="bg-blue-600 py-3 px-6 rounded-full">
                    <StyledText className="text-white font-bold text-lg">Remove Ads</StyledText>
                </StyledTouchableOpacity>
            )}
        </StyledView>
    );
};

export default LobbyScreen;