

import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, Image } from 'react-native';
// Mock navigation prop for demonstration
import type { NativeStackScreenProps } from '@react-navigation/native-stack';

// Assuming you have a RootStackParamList
type RootStackParamList = {
  Home: undefined;
  Lobby: undefined;
  // ... other screens
};

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

// Mock SpotifyLogo component for React Native
const SpotifyLogo = () => <View className="w-6 h-6 bg-green-500 rounded-full" />;

const HomeScreen: React.FC<Props> = ({ navigation }) => {
  const [joinCode, setJoinCode] = useState('');
  const [playerName, setPlayerName] = useState('');
  const [avatarSeed, setAvatarSeed] = useState('');
  
  useEffect(() => {
    randomizeAvatar();
  }, []);
  
  const randomizeAvatar = () => {
    setAvatarSeed(Math.random().toString(36).substring(2, 8));
  };
  
  const handleLogin = async () => {
    // Native Spotify Auth flow would be triggered here
    console.log("Initiating native Spotify login...");
    // On success: navigation.navigate('Lobby');
  };

  const handleGuestJoinSubmit = () => {
     if (!joinCode.trim() || !playerName.trim()) return;
     console.log(`Joining as guest ${playerName} with code ${joinCode}`);
     navigation.navigate('Lobby');
  };

  const avatarUrl = `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(avatarSeed)}&backgroundColor=d1d5db`;
  
  return (
    <View className="flex-1 justify-center items-center p-4 bg-gray-900">
      <Text className="text-5xl text-white font-bold mb-2">Spot<Text className="text-[#1DB954]">Hit</Text></Text>
      <Text className="text-lg text-gray-400 text-center mb-8">The ultimate music trivia game.</Text>

      <View className="w-full max-w-sm">
        {/* Create Game Card */}
        <View className="bg-gray-800 p-6 rounded-2xl mb-8">
            <Text className="text-2xl text-white font-bold text-center mb-2">Create a Game</Text>
            <Text className="text-gray-400 text-center mb-4">Login with Spotify to host.</Text>
            <TouchableOpacity onPress={handleLogin} className="bg-[#1DB954] py-3 px-6 rounded-full flex-row justify-center items-center">
                <SpotifyLogo />
                <Text className="text-black font-bold text-lg ml-2">Login with Spotify</Text>
            </TouchableOpacity>
        </View>

        {/* Join Game Card */}
        <View className="bg-gray-800 p-6 rounded-2xl">
            <Text className="text-2xl text-white font-bold text-center mb-4">Join a Game</Text>
            <Image source={{ uri: avatarUrl }} className="w-24 h-24 rounded-full self-center mb-4 border-2 border-gray-600" />
            
            <TextInput 
                placeholder="Your Name"
                placeholderTextColor="#9CA3AF"
                className="bg-gray-700 text-white text-lg rounded-full w-full py-3 px-6 mb-4 text-center"
                value={playerName}
                onChangeText={setPlayerName}
            />
             <TextInput 
                placeholder="Game Code"
                placeholderTextColor="#9CA3AF"
                className="bg-gray-700 text-white text-lg rounded-full w-full py-3 px-6 mb-4 text-center"
                value={joinCode}
                onChangeText={setJoinCode}
                maxLength={6}
                autoCapitalize="characters"
            />
            <TouchableOpacity onPress={handleGuestJoinSubmit} className="bg-gray-600 py-3 px-6 rounded-full">
                <Text className="text-white font-bold text-lg text-center">Join as Guest</Text>
            </TouchableOpacity>
        </View>
      </View>
    </View>
  );
};

export default HomeScreen;