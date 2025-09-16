
import React, { useState, useCallback, useEffect } from 'react';
import type { Game, Player, Screen, GameMode, Playlist } from './types';
import HomeScreen from './components/HomeScreen';
import LobbyScreen from './components/LobbyScreen';
import GameScreen from './components/GameScreen';
import ResultsScreen from './components/ResultsScreen';
import * as spotifyService from './services/spotifyService';
import { useNotification } from './contexts/NotificationContext';


const App: React.FC = () => {
  const [screen, setScreen] = useState<Screen>('HOME');
  const [game, setGame] = useState<Game | null>(null);
  const [currentUser, setCurrentUser] = useState<Player | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { addNotification } = useNotification();

  const handleTokenReceived = useCallback(async (token: string) => {
    try {
      setIsLoading(true);
      setAccessToken(token);
      localStorage.setItem('spotify_access_token', token);
      const userProfile = await spotifyService.getUserProfile(token);
      const userPlaylists = await spotifyService.getUserPlaylists(token);
      setCurrentUser(userProfile);
      setPlaylists(userPlaylists);
      addNotification(`Welcome, ${userProfile.name}!`, 'success');
      setScreen('LOBBY');
    } catch (err) {
      console.error("Token validation failed", err);
      addNotification("Your session is invalid. Please log in again.", "error");
      localStorage.removeItem('spotify_access_token');
      setScreen('HOME');
    } finally {
      setIsLoading(false);
    }
  }, [addNotification]);

  useEffect(() => {
    const checkStoredToken = async () => {
      const storedToken = localStorage.getItem('spotify_access_token');
      if (storedToken) {
        await handleTokenReceived(storedToken);
      } else {
        setIsLoading(false);
      }
    };
    checkStoredToken();
  }, [handleTokenReceived]);
  
  const handleLogout = useCallback(() => {
    localStorage.removeItem('spotify_access_token');
    setAccessToken(null);
    setCurrentUser(null);
    setPlaylists([]);
    setGame(null);
    setScreen('HOME');
    addNotification("You have been logged out.", "info");
  }, [addNotification]);

  const handleAuthError = useCallback(() => {
    handleLogout();
    addNotification("Permissions missing for playback. Please log in again to grant access.", "error");
  }, [handleLogout, addNotification]);

  const handleCreateGame = useCallback((gameMode: GameMode) => {
    if (!currentUser) return;
    const gameCode = Math.random().toString(36).substring(2, 8).toUpperCase();
    const newGame: Game = {
      code: gameCode,
      host: currentUser,
      players: [currentUser],
      playlist: null,
      currentRound: 0,
      totalRounds: 10,
      mode: gameMode,
      scores: { [currentUser.id]: 0 },
      gameState: 'WAITING',
      currentSong: null,
      timeline: [],
    };
    setGame(newGame);
  }, [currentUser]);

  const handleJoinGame = useCallback((code: string) => {
    // In a real app, this would query a backend. Here we just add the player.
    if (!currentUser || !game || game.code !== code) {
      addNotification('Invalid Game Code', 'error');
      return;
    }
    if(!game.players.some(p => p.id === currentUser.id)) {
        setGame(prevGame => {
            if (!prevGame) return null;
            const newScores = { ...prevGame.scores, [currentUser.id]: 0 };
            return {
                ...prevGame,
                players: [...prevGame.players, currentUser],
                scores: newScores,
            };
        });
    }
  }, [currentUser, game, addNotification]);
  
  const handleSelectPlaylist = useCallback((playlistId: string) => {
    const playlist = playlists.find(p => p.id === playlistId);
    if (playlist) {
      setGame(prevGame => prevGame ? { ...prevGame, playlist } : null);
    }
  }, [playlists]);

  const handleStartGame = useCallback(() => {
    if (game && game.playlist) {
      if (game.host.isPremium) {
        setGame(prevGame => prevGame ? { ...prevGame, gameState: 'IN_PROGRESS' } : null);
        setScreen('GAME');
      } else {
        addNotification('The game host needs a Spotify Premium account to play.', 'error');
      }
    } else {
      addNotification('Please select a playlist first!', 'info');
    }
  }, [game, addNotification]);
  
  const handleEndGame = useCallback((finalScores: Record<string, number>) => {
    if (!game) return;
    setGame(prevGame => prevGame ? { ...prevGame, gameState: 'FINISHED', scores: finalScores } : null);
    setScreen('RESULTS');
  }, [game]);
  
  const handlePlayAgain = useCallback(() => {
    // Reset game state for a new round but keep user logged in.
    setGame(null);
    setScreen('LOBBY');
  }, []);

  const renderScreen = () => {
    if (isLoading) {
      return (
        <div className="flex justify-center items-center min-h-screen">
          <p className="text-2xl animate-pulse">Loading Spot-Hit...</p>
        </div>
      );
    }

    switch (screen) {
      case 'HOME':
        return <HomeScreen onTokenReceived={handleTokenReceived} />;
      case 'LOBBY':
        return (
          <LobbyScreen
            game={game}
            currentUser={currentUser!}
            playlists={playlists}
            onCreateGame={handleCreateGame}
            onJoinGame={handleJoinGame}
            onSelectPlaylist={handleSelectPlaylist}
            onStartGame={handleStartGame}
            onLogout={handleLogout}
          />
        );
      case 'GAME':
        return game && currentUser && accessToken && <GameScreen game={game} currentUser={currentUser} onEndGame={handleEndGame} accessToken={accessToken} onAuthError={handleAuthError} />;
      case 'RESULTS':
        return game && <ResultsScreen game={game} onPlayAgain={handlePlayAgain} />;
      default:
        return <HomeScreen onTokenReceived={handleTokenReceived} />;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-black to-gray-900 text-white p-4 sm:p-8">
      <div className="container mx-auto">
        {renderScreen()}
      </div>
    </div>
  );
};

export default App;