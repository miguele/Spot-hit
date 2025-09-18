import React from 'react';
import type { Game } from '../types';
import Button from './Button';
import Card from './Card';

interface ResultsScreenProps {
  game: Game;
  onPlayAgain: () => void;
}

const ResultsScreen: React.FC<ResultsScreenProps> = ({ game, onPlayAgain }) => {
  const sortedPlayers = [...game.players].sort((a, b) => (game.scores[b.id] || 0) - (game.scores[a.id] || 0));

  const getTrophy = (index: number) => {
    if (index === 0) return '🏆';
    if (index === 1) return '🥈';
    if (index === 2) return '🥉';
    return `#${index + 1}`;
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-[80vh]">
      <Card className="w-full max-w-2xl text-center">
        <h1 className="text-5xl font-black mb-2">Game Over!</h1>
        <p className="text-xl text-gray-300 mb-8">Here are the final results:</p>

        <ul className="space-y-4">
          {sortedPlayers.map((player, index) => (
            <li
              key={player.id}
              className={`flex items-center p-4 rounded-xl transition-transform duration-300 hover:scale-105 ${
                index === 0 ? 'bg-yellow-500/80' : 'bg-gray-700'
              }`}
            >
              <span className="text-4xl font-bold w-16">{getTrophy(index)}</span>
              <img src={player.avatarUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${player.name}&backgroundColor=d1d5db`} alt={player.name} className="w-16 h-16 rounded-full border-4 border-gray-600 mx-4" />
              <span className="text-xl font-semibold flex-grow text-left">{player.name}</span>
              <span className="text-3xl font-black">{game.scores[player.id] || 0} pts</span>
            </li>
          ))}
        </ul>

        <div className="mt-10">
          <Button onClick={onPlayAgain}>
            Play Again
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default ResultsScreen;