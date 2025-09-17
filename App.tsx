
import React, { useState, useCallback, useEffect, useRef } from 'react';
import type { Game, Player, Screen, GameMode, Playlist } from './types';
import HomeScreen from './components/HomeScreen';
import LobbyScreen from './components/LobbyScreen';
import GameScreen from './components/GameScreen';
import ResultsScreen from './components/ResultsScreen';
import * as spotifyService from './services/spotifyService';
import { useNotification } from './contexts/NotificationContext';
import { db } from './firebase/config';
import { doc, setDoc, getDoc, updateDoc, arrayUnion, onSnapshot, Unsubscribe } from 'firebase/firestore';


const App: React.FC = () => {
  const [screen, setScreen] = useState<Screen>('HOME');
  const [game, setGame] = useState<Game | null>(null);
  const [currentUser, setCurrentUser] = useState<Player | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { addNotification } = useNotification();
  
  const gameSubscription = useRef<Unsubscribe | null>(null);

  const subscribeToGameUpdates = useCallback((gameCode: string) => {
    // Unsubscribe from any existing listener
    if (gameSubscription.current) {
      gameSubscription.current();
    }
    
    const gameRef = doc(db, 'games', gameCode);
    gameSubscription.current = onSnapshot(gameRef, (docSnap) => {
      if (docSnap.exists()) {
        const gameData = docSnap.data() as Game;
        setGame(gameData);
        // If game state changes, update the screen
        if (gameData.gameState === 'IN_PROGRESS' && screen !== 'GAME') {
           setScreen('GAME');
        } else if (gameData.gameState === 'FINISHED' && screen !== 'RESULTS') {
           setScreen('RESULTS');
        }
      } else {
        addNotification("The game session has ended or could not be found.", "error");
        setGame(null);
        setScreen('LOBBY');
      }
    });
  }, [addNotification, screen]);

  useEffect(() => {
    // Cleanup subscription on component unmount
    return () => {
      if (gameSubscription.current) {
        gameSubscription.current();
      }
    };
  }, []);


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
    if (gameSubscription.current) {
        gameSubscription.current();
        gameSubscription.current = null;
    }
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

  const handleCreateGame = useCallback(async (gameMode: GameMode) => {
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
    
    try {
      await setDoc(doc(db, 'games', gameCode), newGame);
      subscribeToGameUpdates(gameCode);
    } catch (error) {
      console.error("Error creating game:", error);
      addNotification("Could not create game. Please try again.", "error");
    }
  }, [currentUser, subscribeToGameUpdates, addNotification]);
  
  const handleJoinGame = useCallback(async (gameCode: string) => {
    if (!currentUser) return;

    const gameRef = doc(db, 'games', gameCode);
    try {
        const gameSnap = await getDoc(gameRef);
        if (gameSnap.exists()) {
            const gameData = gameSnap.data();
            if (gameData.gameState !== 'WAITING') {
                addNotification("This game has already started.", "error");
                return;
            }
            // Check if user is already in the game
            if (gameData.players.some((p: Player) => p.id === currentUser.id)) {
                 addNotification("You are already in this game.", "info");
            } else {
                 await updateDoc(gameRef, {
                    players: arrayUnion(currentUser),
                    [`scores.${currentUser.id}`]: 0,
                });
                addNotification(`Successfully joined game ${gameCode}!`, "success");
            }
            subscribeToGameUpdates(gameCode);
        } else {
            addNotification("Invalid code. No game found with this code.", "error");
        }
    } catch(error) {
        console.error("Error joining game:", error);
        addNotification("Could not join game. Please check the code and try again.", "error");
    }
  }, [currentUser, addNotification, subscribeToGameUpdates]);
  
  const handleSelectPlaylist = useCallback(async (playlistId: string) => {
    if (!game) return;
    const playlist = playlists.find(p => p.id === playlistId);
    if (playlist) {
      const gameRef = doc(db, 'games', game.code);
      await updateDoc(gameRef, { playlist: playlist });
    }
  }, [playlists, game]);

  const handleStartGame = useCallback(async () => {
    if (game && game.playlist) {
      if (game.host.isPremium) {
        const gameRef = doc(db, 'games', game.code);
        await updateDoc(gameRef, { gameState: 'IN_PROGRESS' });
      } else {
        addNotification('The game host needs a Spotify Premium account to play.', 'error');
      }
    } else {
      addNotification('Please select a playlist first!', 'info');
    }
  }, [game, addNotification]);
  
  const handleEndGame = useCallback(async (finalScores: Record<string, number>) => {
    if (!game) return;
    const gameRef = doc(db, 'games', game.code);
    await updateDoc(gameRef, { 
      gameState: 'FINISHED', 
      scores: finalScores 
    });
  }, [game]);
  
  const handlePlayAgain = useCallback(() => {
    if (gameSubscription.current) {
        gameSubscription.current();
        gameSubscription.current = null;
    }
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