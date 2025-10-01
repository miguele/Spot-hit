

import React from 'react';
import { View, Text, FlatList, Image } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import type { Game, Player } from '../types';
import { GameMode } from '../types'; // Import GameMode for mock data
import Button from './Button';
import Card from './Card';

// This would be defined in a central navigation types file
type RootStackParamList = {
  Results: { game: Game };
  Home: undefined;
};

type Props = NativeStackScreenProps<RootStackParamList, 'Results'>;


// Mock data to prevent crash when route.params is undefined on initial load.
const mockGame: Game = {
    code: 'DEMO',
    host: { id: 'host1', name: 'Host Player' },
    players: [
      { id: 'p2', name: 'Player Two', avatarUrl: 'https://api.dicebear.com/7.x/bottts/svg?seed=p2&backgroundColor=d1d5db' },
      { id: 'p1', name: 'Player One', avatarUrl: 'https://api.dicebear.com/7.x/bottts/svg?seed=p1&backgroundColor=d1d5db' },
      { id: 'p3', name: 'Player Three', avatarUrl: 'https://api.dicebear.com/7.x/bottts/svg?seed=p3&backgroundColor=d1d5db' },
    ],
    playlist: null,
    mode: GameMode.GuessTheYear,
    currentRound: 10,
    totalRounds: 10,
    scores: {
      'p1': 8500,
      'p2': 9200,
      'p3': 7300,
    },
    gameState: 'FINISHED',
    currentSong: null,
    timeline: [],
    songs: [],
    turnState: 'REVEALED',
    lastGuessResult: null,
    turnStartTime: null,
};


const ResultsScreen: React.FC<Props> = ({ route, navigation }) => {
  // FIX: Use game data from route params if available, otherwise fall back to mock data.
  const game = route.params?.game || mockGame;

  const sortedPlayers = [...game.players].sort((a, b) => (game.scores[b.id] || 0) - (game.scores[a.id] || 0));

  const getTrophy = (index: number) => {
    if (index === 0) return '🏆';
    if (index === 1) return '🥈';
    if (index === 2) return '🥉';
    return `#${index + 1}`;
  };

  const renderPlayer = ({ item, index }: { item: Player; index: number }) => (
    <View
      className={`flex-row items-center p-4 rounded-xl mb-4 ${
        index === 0 ? 'bg-yellow-500/80' : 'bg-gray-700'
      }`}
    >
      <Text className="text-4xl font-bold w-16 text-center">{getTrophy(index)}</Text>
      <Image
        source={{ uri: item.avatarUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${item.name}&backgroundColor=d1d5db` }}
        className="w-16 h-16 rounded-full border-4 border-gray-600 mx-4"
      />
      <Text className="text-xl font-semibold flex-1 text-left text-white">{item.name}</Text>
      <Text className="text-3xl font-black text-white">{game.scores[item.id] || 0} pts</Text>
    </View>
  );

  return (
    <View className="flex-1 justify-center p-4 bg-gray-900">
      <Card className="w-full max-w-2xl text-center">
        <Text className="text-5xl font-black text-white text-center mb-2">Game Over!</Text>
        <Text className="text-xl text-gray-300 text-center mb-8">Here are the final results:</Text>

        <FlatList
          data={sortedPlayers}
          renderItem={renderPlayer}
          keyExtractor={(item) => item.id}
          className="w-full"
        />

        <View className="mt-10">
          <Button onPress={() => navigation.navigate('Home')}>
            Play Again
          </Button>
        </View>
      </Card>
    </View>
  );
};

export default ResultsScreen;