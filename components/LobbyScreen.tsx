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
  curatedPlaylists: Playlist[];
  onCreateGame: (gameMode: GameMode) => void;
  onJoinGame: (gameCode: string) => void;
  onSelectPlaylist: (playlistId: string) => void;
  onStartGame: () => void;
  onLogout: () => void;
  onLeaveGame: () => void;
}

const LobbyScreen: React.FC<LobbyScreenProps> = ({ game, currentUser, playlists, curatedPlaylists, onCreateGame, onJoinGame, onSelectPlaylist, onStartGame, onLogout, onLeaveGame }) => {
  const [joinCode, setJoinCode] = useState('');
  const isHost = game?.host.id === currentUser.id;
  const [activeTab, setActiveTab] = useState<'my' | 'spothit'>(playlists.length > 0 ? 'my' : 'spothit');
  
  const UserProfileHeader = () => (
     <div className="absolute top-4 right-4 md:top-6 md:right-8">
        <div className="flex items-center gap-3 bg-black/30 backdrop-blur-sm p-2 rounded-full">
            <img src={currentUser.avatarUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${currentUser.name}`} alt={currentUser.name} className="w-10 h-10 rounded-full"/>
            <span className="font-semibold hidden sm:inline">{currentUser.name}</span>
             {game && (
                 <button onClick={onLeaveGame} className="bg-amber-600 hover:bg-amber-700 text-white font-bold p-2 rounded-full transition-colors" title="Leave Game">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-6 h-6">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9" />
                    </svg>
                 </button>
             )}
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
                  {isHost ? (
                    <>
                      <div className="flex border-b border-gray-600 mb-4">
                          <button 
                              onClick={() => setActiveTab('my')}
                              className={`py-2 px-4 font-semibold transition-colors ${activeTab === 'my' ? 'text-white border-b-2 border-[#1DB954]' : 'text-gray-400'}`}
                          >
                              My Playlists
                          </button>
                          <button 
                              onClick={() => setActiveTab('spothit')}
                              className={`py-2 px-4 font-semibold transition-colors ${activeTab === 'spothit' ? 'text-white border-b-2 border-[#1DB954]' : 'text-gray-400'}`}
                          >
                              Spot-Hit Picks
                          </button>
                      </div>
                      
                      <div className="space-y-3 max-h-60 overflow-y-auto pr-2">
                          {(activeTab === 'my' ? playlists : curatedPlaylists).map(p => (
                              <button
                                  key={p.id}
                                  onClick={() => onSelectPlaylist(p.id)}
                                  className={`w-full text-left flex items-center gap-4 p-3 rounded-lg transition-colors duration-200 ${
                                  game.playlist?.id === p.id 
                                      ? 'bg-[#1DB954] text-black ring-2 ring-white' 
                                      : 'bg-gray-700/50 hover:bg-gray-700'
                                  }`}
                              >
                                  <img src={p.coverUrl || `https://api.dicebear.com/7.x/shapes/svg?seed=${p.name}`} alt={p.name} className="w-12 h-12 rounded-md object-cover bg-gray-900" />
                                  <div>
                                  <p className="font-bold">{p.name}</p>
                                  <p className="text-sm opacity-80">{p.trackCount} songs</p>
                                  </div>
                              </button>
                          ))}
                          {activeTab === 'my' && playlists.length === 0 && (
                              <p className="text-gray-400">You don't have any playlists. Create one on Spotify or use Spot-Hit Picks.</p>
                          )}
                          {activeTab === 'spothit' && curatedPlaylists.length === 0 && (
                              <p className="text-gray-400 animate-pulse">Loading Spot-Hit playlists...</p>
                          )}
                      </div>
                    </>
                  ) : ( // Not the host
                    game.playlist ? (
                        <div className="flex items-center gap-4 p-3 rounded-lg bg-gray-700/50">
                            <img src={game.playlist.coverUrl || `https://api.dicebear.com/7.x/shapes/svg?seed=${game.playlist.name}`} alt={game.playlist.name} className="w-12 h-12 rounded-md object-cover bg-gray-900" />
                            <div>
                            <p className="font-bold">{game.playlist.name}</p>
                            <p className="text-sm opacity-80">{game.playlist.trackCount} songs</p>
                            </div>
                        </div>
                    ) : (
                        <p className="text-gray-400">Waiting for host to select a playlist...</p>
                    )
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
  
  return (
    <>
      <UserProfileHeader />
      <div className="flex flex-col items-center justify-center min-h-[80vh] pt-20">
        <h1 className="text-5xl font-black mb-10 text-center">Ready to Play?</h1>
        <div className="w-full max-w-md grid grid-cols-1 md:grid-cols-1 gap-8">
          <Card>
            <h2 className="text-3xl font-bold mb-4 text-center">Create Game</h2>
             <div className="p-4 border-2 rounded-xl border-[#1DB954] bg-[#1DB954]/10 mb-6">
                <h3 className="text-lg font-bold text-white">Guess the Year</h3>
                <p className="text-gray-400 mt-1 text-sm">The classic mode. Guess the release year of the song.</p>
            </div>
            <Button onClick={() => onCreateGame(GameMode.GuessTheYear)} className="w-full">
              Create New Game
            </Button>
          </Card>
           <div className="text-center text-gray-400 text-2xl font-bold my-4">OR</div>
          <Card>
            <h2 className="text-3xl font-bold mb-4 text-center">Join Game</h2>
            <form onSubmit={(e) => { e.preventDefault(); onJoinGame(joinCode.toUpperCase()); }}>
                <Input 
                    placeholder="ENTER GAME CODE" 
                    value={joinCode}
                    onChange={(e) => setJoinCode(e.target.value)}
                    className="mb-4 tracking-widest"
                    maxLength={6}
                />
                <Button type="submit" variant="secondary" className="w-full" disabled={!joinCode}>
                    Join with Code
                </Button>
            </form>
          </Card>
        </div>
      </div>
    </>
  );
};

export default LobbyScreen;