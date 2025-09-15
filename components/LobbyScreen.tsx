import React, { useState } from 'react';
import type { Game, Player, Playlist } from '../types';
import { GameMode } from '../types';
import Button from './Button';
import Card from './Card';
import Input from './Input';

interface LobbyScreenProps {
  game: Game | null;
  currentUser: Player;
  playlists: Playlist[];
  onCreateGame: (gameMode: GameMode) => void;
  onJoinGame: (code: string) => void;
  onSelectPlaylist: (playlistId: string) => void;
  onStartGame: () => void;
  onLogout: () => void;
}

const LobbyScreen: React.FC<LobbyScreenProps> = ({ game, currentUser, playlists, onCreateGame, onJoinGame, onSelectPlaylist, onStartGame, onLogout }) => {
  const [joinCode, setJoinCode] = useState('');
  const [selectedMode, setSelectedMode] = useState<GameMode | null>(null);

  const isHost = game?.host.id === currentUser.id;
  
  const UserProfileHeader = () => (
     <div className="absolute top-4 right-4 md:top-6 md:right-8">
        <div className="flex items-center gap-3 bg-black/30 backdrop-blur-sm p-2 rounded-full">
            <img src={currentUser.avatarUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${currentUser.name}`} alt={currentUser.name} className="w-10 h-10 rounded-full"/>
            <span className="font-semibold hidden sm:inline">{currentUser.name}</span>
             <button onClick={onLogout} className="bg-red-600 hover:bg-red-700 text-white font-bold p-2 rounded-full transition-colors" title="Logout">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
            </button>
        </div>
    </div>
  );

  if (game) {
    return (
      <>
        <UserProfileHeader />
        <div className="flex flex-col items-center justify-center min-h-[80vh] pt-20">
            <Card className="w-full max-w-4xl">
              <h2 className="text-3xl font-bold text-center mb-2">Game Lobby</h2>
              <p className="text-center text-gray-400 mb-6">Game Code: <span className="font-bold text-[#1DB954] tracking-widest bg-gray-900 px-3 py-1 rounded-md">{game.code}</span></p>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div>
                  <h3 className="text-xl font-semibold mb-4 border-b border-gray-600 pb-2">Players ({game.players.length})</h3>
                  <ul className="space-y-3">
                    {game.players.map(player => (
                      <li key={player.id} className="flex items-center gap-4 bg-gray-700/50 p-3 rounded-lg">
                        <img src={player.avatarUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${player.name}`} alt={player.name} className="w-12 h-12 rounded-full border-2 border-gray-500" />
                        <span className="font-medium text-lg">{player.name} {player.id === game.host.id && '(Host)'}</span>
                      </li>
                    ))}
                  </ul>
                </div>
                
                <div>
                  <h3 className="text-xl font-semibold mb-4 border-b border-gray-600 pb-2">Select Playlist</h3>
                  {playlists.length > 0 ? (
                    <div className="space-y-3 max-h-60 overflow-y-auto pr-2">
                      {playlists.map(p => (
                        <button
                          key={p.id}
                          onClick={() => isHost && onSelectPlaylist(p.id)}
                          disabled={!isHost}
                          className={`w-full text-left flex items-center gap-4 p-3 rounded-lg transition-colors duration-200 ${
                            game.playlist?.id === p.id 
                              ? 'bg-[#1DB954] text-black ring-2 ring-white' 
                              : 'bg-gray-700/50 hover:bg-gray-700'
                          } ${isHost ? 'cursor-pointer' : 'cursor-not-allowed'}`}
                        >
                          <img src={p.coverUrl || `https://api.dicebear.com/7.x/shapes/svg?seed=${p.name}`} alt={p.name} className="w-12 h-12 rounded-md object-cover bg-gray-900" />
                          <div>
                            <p className="font-bold">{p.name}</p>
                            <p className="text-sm opacity-80">{p.trackCount} songs</p>
                          </div>
                        </button>
                      ))}
                    </div>
                  ) : (
                    <p className="text-gray-400">You don't have any playlists. Create one on Spotify to play!</p>
                  )}
                </div>
              </div>

              <div className="mt-8 text-center">
                {isHost ? (
                  <Button onClick={onStartGame} disabled={!game.playlist}>
                    Start Game
                  </Button>
                ) : (
                  <p className="text-gray-300 text-lg">Waiting for host to start the game...</p>
                )}
              </div>
            </Card>
        </div>
      </>
    );
  }
  
  const GameModeCard: React.FC<{mode: GameMode, title: string, description: string}> = ({ mode, title, description }) => (
    <div 
        onClick={() => setSelectedMode(mode)}
        className={`p-6 border-2 rounded-xl cursor-pointer transition-all duration-200 ${selectedMode === mode ? 'border-[#1DB954] bg-[#1DB954]/10' : 'border-gray-700 hover:border-gray-500'}`}
    >
        <h3 className="text-xl font-bold text-white">{title}</h3>
        <p className="text-gray-400 mt-1">{description}</p>
    </div>
  );


  return (
    <>
        <UserProfileHeader />
        <div className="flex flex-col items-center justify-center min-h-[80vh] pt-20">
            <h1 className="text-5xl font-black mb-10 text-center">Join or Create a Game</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-4xl">
                <Card>
                    <h2 className="text-3xl font-bold mb-6 text-center">Create Game</h2>
                    <div className="space-y-4 mb-6">
                       <GameModeCard mode={GameMode.GuessTheYear} title="Guess the Year" description="The classic mode. Guess the release year of the song." />
                       <GameModeCard mode={GameMode.GuessTheSong} title="Guess the Song" description="Name that tune! Guess the song title and artist." />
                    </div>
                    <Button onClick={() => selectedMode && onCreateGame(selectedMode)} disabled={!selectedMode} className="w-full">Create New Game</Button>
                </Card>
                <Card>
                    <h2 className="text-3xl font-bold mb-6 text-center">Join Game</h2>
                    <div className="space-y-4">
                        <Input 
                            type="text" 
                            placeholder="Enter Game Code" 
                            value={joinCode}
                            onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
                        />
                        <Button onClick={() => onJoinGame(joinCode)} disabled={!joinCode} variant="secondary" className="w-full">Join with Code</Button>
                    </div>
                </Card>
            </div>
        </div>
    </>
  );
};

export default LobbyScreen;