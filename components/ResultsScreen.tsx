

import React from 'react';
import { View, Text, FlatList, Image } from 'react-native';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import type { Game, Player } from '../types';
import Button from './Button';
import Card from './Card';

// This would be defined in a central navigation types file
type RootStackParamList = {
  Results: { game: Game };
  Home: undefined;
};

type Props = NativeStackScreenProps<RootStackParamList, 'Results'>;

// FIX: Create styled versions of components to accept the className prop.
const StyledView = styled(View);
const StyledText = styled(Text);
const StyledImage = styled(Image);
const StyledFlatList = styled(FlatList);

const ResultsScreen: React.FC<Props> = ({ route, navigation }) => {
  const { game } = route.params;

  const sortedPlayers = [...game.players].sort((a, b) => (game.scores[b.id] || 0) - (game.scores[a.id] || 0));

  const getTrophy = (index: number) => {
    if (index === 0) return '🏆';
    if (index === 1) return '🥈';
    if (index === 2) return '🥉';
    return `#${index + 1}`;
  };

  const renderPlayer = ({ item, index }: { item: Player; index: number }) => (
    <StyledView
      className={`flex-row items-center p-4 rounded-xl mb-4 ${
        index === 0 ? 'bg-yellow-500/80' : 'bg-gray-700'
      }`}
    >
      <StyledText className="text-4xl font-bold w-16 text-center">{getTrophy(index)}</StyledText>
      <StyledImage
        source={{ uri: item.avatarUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${item.name}&backgroundColor=d1d5db` }}
        className="w-16 h-16 rounded-full border-4 border-gray-600 mx-4"
      />
      <StyledText className="text-xl font-semibold flex-1 text-left text-white">{item.name}</StyledText>
      <StyledText className="text-3xl font-black text-white">{game.scores[item.id] || 0} pts</StyledText>
    </StyledView>
  );

  return (
    <StyledView className="flex-1 justify-center p-4 bg-gray-900">
      <Card className="w-full max-w-2xl text-center">
        <StyledText className="text-5xl font-black text-white text-center mb-2">Game Over!</StyledText>
        <StyledText className="text-xl text-gray-300 text-center mb-8">Here are the final results:</StyledText>

        <StyledFlatList
          data={sortedPlayers}
          renderItem={renderPlayer}
          keyExtractor={(item) => item.id}
          className="w-full"
        />

        <StyledView className="mt-10">
          <Button onPress={() => navigation.navigate('Home')}>
            Play Again
          </Button>
        </StyledView>
      </Card>
    </StyledView>
  );
};

export default ResultsScreen;