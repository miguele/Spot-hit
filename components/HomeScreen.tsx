

import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, Image } from 'react-native';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';
// Mock navigation prop for demonstration
import type { NativeStackScreenProps } from '@react-navigation/native-stack';

// Assuming you have a RootStackParamList
type RootStackParamList = {
  Home: undefined;
  Lobby: undefined;
  // ... other screens
};

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

// FIX: Create styled versions of components to accept the className prop.
const StyledView = styled(View);
const StyledText = styled(Text);
const StyledTextInput = styled(TextInput);
const StyledTouchableOpacity = styled(TouchableOpacity);
const StyledImage = styled(Image);


// Mock SpotifyLogo component for React Native
const SpotifyLogo = () => <StyledView className="w-6 h-6 bg-green-500 rounded-full" />;

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
    <StyledView className="flex-1 justify-center items-center p-4 bg-gray-900">
      <StyledText className="text-5xl text-white font-bold mb-2">Spot<StyledText className="text-[#1DB954]">Hit</StyledText></StyledText>
      <StyledText className="text-lg text-gray-400 text-center mb-8">The ultimate music trivia game.</StyledText>

      <StyledView className="w-full max-w-sm">
        {/* Create Game Card */}
        <StyledView className="bg-gray-800 p-6 rounded-2xl mb-8">
            <StyledText className="text-2xl text-white font-bold text-center mb-2">Create a Game</StyledText>
            <StyledText className="text-gray-400 text-center mb-4">Login with Spotify to host.</StyledText>
            <StyledTouchableOpacity onPress={handleLogin} className="bg-[#1DB954] py-3 px-6 rounded-full flex-row justify-center items-center">
                <SpotifyLogo />
                <StyledText className="text-black font-bold text-lg ml-2">Login with Spotify</StyledText>
            </StyledTouchableOpacity>
        </StyledView>

        {/* Join Game Card */}
        <StyledView className="bg-gray-800 p-6 rounded-2xl">
            <StyledText className="text-2xl text-white font-bold text-center mb-4">Join a Game</StyledText>
            <StyledImage source={{ uri: avatarUrl }} className="w-24 h-24 rounded-full self-center mb-4 border-2 border-gray-600" />
            
            <StyledTextInput 
                placeholder="Your Name"
                placeholderTextColor="#9CA3AF"
                className="bg-gray-700 text-white text-lg rounded-full w-full py-3 px-6 mb-4 text-center"
                value={playerName}
                onChangeText={setPlayerName}
            />
             <StyledTextInput 
                placeholder="Game Code"
                placeholderTextColor="#9CA3AF"
                className="bg-gray-700 text-white text-lg rounded-full w-full py-3 px-6 mb-4 text-center"
                value={joinCode}
                onChangeText={setJoinCode}
                maxLength={6}
                autoCapitalize="characters"
            />
            <StyledTouchableOpacity onPress={handleGuestJoinSubmit} className="bg-gray-600 py-3 px-6 rounded-full">
                <StyledText className="text-white font-bold text-lg text-center">Join as Guest</StyledText>
            </StyledTouchableOpacity>
        </StyledView>
      </StyledView>
    </StyledView>
  );
};

export default HomeScreen;