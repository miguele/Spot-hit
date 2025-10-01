

import React, { useContext } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { IAPContext } from '../contexts/IAPContext';

// Mock navigation prop
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
type RootStackParamList = { Lobby: undefined; Game: undefined; };
type Props = NativeStackScreenProps<RootStackParamList, 'Lobby'>;


const LobbyScreen: React.FC<Props> = ({ navigation }) => {
    const { isPremium, purchasePremium } = useContext(IAPContext);

    const handleStartGame = () => {
        navigation.navigate('Game');
    };

    return (
        <View className="flex-1 justify-center items-center p-4 bg-gray-900">
            <Text className="text-3xl text-white font-bold mb-4">Game Lobby</Text>
            
            {/* Player List, Playlist selector etc. would go here */}
            <View className="bg-gray-800 p-6 rounded-2xl w-full max-w-sm mb-8">
                <Text className="text-white text-center">Players will be listed here...</Text>
            </View>

            <TouchableOpacity onPress={handleStartGame} className="bg-[#1DB954] py-3 px-6 rounded-full mb-4">
                <Text className="text-black font-bold text-lg">Start Game</Text>
            </TouchableOpacity>

            {!isPremium && (
                 <TouchableOpacity onPress={purchasePremium} className="bg-blue-600 py-3 px-6 rounded-full">
                    <Text className="text-white font-bold text-lg">Remove Ads</Text>
                </TouchableOpacity>
            )}
        </View>
    );
};

export default LobbyScreen;