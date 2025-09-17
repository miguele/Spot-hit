

import React, { useState, useCallback, useEffect, useRef, lazy, Suspense } from 'react';
import type { Game, Player, Screen, GameMode, Playlist, Song } from './types';
import * as spotifyService from './services/spotifyService';
import { useNotification } from './contexts/NotificationContext';
import { db } from './firebase/config';
import { doc, setDoc, getDoc, updateDoc, arrayUnion, onSnapshot, Unsubscribe } from 'firebase/firestore';

// Lazy load screen components for better performance and smaller initial bundle size
const HomeScreen = lazy(() => import('./components/HomeScreen'));
const LobbyScreen = lazy(() => import('./components/LobbyScreen'));
const GameScreen = lazy(() => import('./components/GameScreen'));
const ResultsScreen = lazy(() => import('./components/ResultsScreen'));


const App: React.FC = () => {
  const [screen, setScreen] = useState<Screen>('HOME');
  const [game, setGame] = useState<Game | null>(null);
  const [player, setPlayer] = useState<Player | null>(null);
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
        if (gameData.gameState === 'IN_PROGRESS') {
           setScreen('GAME');
        } else if (gameData.gameState === 'FINISHED') {
           setScreen('RESULTS');
        } else if (gameData.gameState === 'WAITING' && player) {
            setScreen('LOBBY');
        }
      } else {
        addNotification("The game session has ended or could not be found.", "error");
        setGame(null);
        setScreen('LOBBY');
      }
    });
  }, [addNotification, player]);

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
      setPlayer(userProfile);
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
    setPlayer(null);
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
    if (!player) return;
    const gameCode = Math.random().toString(36).substring(2, 8).toUpperCase();
    
    const newGame: Game = {
      code: gameCode,
      host: player,
      players: [player],
      playlist: null,
      currentRound: 0,
      totalRounds: 10,
      mode: gameMode,
      scores: { [player.id]: 0 },
      gameState: 'WAITING',
      currentSong: null,
      timeline: [],
      songs: [],
    };
    
    try {
      await setDoc(doc(db, 'games', gameCode), newGame);
      subscribeToGameUpdates(gameCode);
    } catch (error) {
      console.error("Error creating game:", error);
      addNotification("Could not create game. Please try again.", "error");
    }
  }, [player, subscribeToGameUpdates, addNotification]);
  
  const handleJoinGame = useCallback(async (gameCode: string) => {
    if (!player) return;

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
            if (gameData.players.some((p: Player) => p.id === player.id)) {
                 addNotification("You are already in this game.", "info");
            } else {
                 await updateDoc(gameRef, {
                    players: arrayUnion(player),
                    [`scores.${player.id}`]: 0,
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
  }, [player, addNotification, subscribeToGameUpdates]);

  const handleJoinAsGuest = useCallback(async (gameCode: string, playerName: string) => {
    if (!playerName.trim()) {
        addNotification("Please enter your name.", "error");
        return;
    }
    if (!gameCode.trim()) {
        addNotification("Please enter a game code.", "error");
        return;
    }

    const guestPlayer: Player = {
        id: `guest_${Date.now()}`,
        name: playerName,
        avatarUrl: `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(playerName)}`,
        isPremium: false,
    };

    const gameRef = doc(db, 'games', gameCode);
    try {
        const gameSnap = await getDoc(gameRef);
        if (gameSnap.exists()) {
            const gameData = gameSnap.data();
            if (gameData.gameState !== 'WAITING') {
                addNotification("This game has already started.", "error");
                return;
            }
            await updateDoc(gameRef, {
                players: arrayUnion(guestPlayer),
                [`scores.${guestPlayer.id}`]: 0,
            });
            addNotification(`Successfully joined game ${gameCode} as ${playerName}!`, "success");
            setPlayer(guestPlayer);
            subscribeToGameUpdates(gameCode);
        } else {
            addNotification("Invalid code. No game found with this code.", "error");
        }
    } catch (error) {
        console.error("Error joining game as guest:", error);
        addNotification("Could not join game. Please check the code and try again.", "error");
    }
  }, [addNotification, subscribeToGameUpdates]);
  
  const handleSelectPlaylist = useCallback(async (playlistId: string) => {
    if (!game) return;
    const playlist = playlists.find(p => p.id === playlistId);
    if (playlist) {
      const gameRef = doc(db, 'games', game.code);
      await updateDoc(gameRef, { playlist: playlist });
    }
  }, [playlists, game]);

  const handleStartGame = useCallback(async () => {
    if (!game || !game.playlist || !accessToken || !player) return;
     if (game.host.id !== player.id) {
        addNotification("Only the host can start the game.", "error");
        return;
    }
    if (game.host.isPremium) {
      try {
        addNotification('Loading playlist...', 'info');
        const allSongs = await spotifyService.getPlaylistTracks(game.playlist.id, accessToken);
        if (allSongs.length < 1) {
            addNotification('This playlist is empty or contains no valid songs. Please select another.', 'error');
            return;
        }
        const shuffled = [...allSongs].sort(() => 0.5 - Math.random());
        const gameSongs = shuffled.slice(0, game.totalRounds);
        
        const gameRef = doc(db, 'games', game.code);
        await updateDoc(gameRef, { 
            gameState: 'IN_PROGRESS',
            songs: gameSongs,
            currentSong: gameSongs[0],
            currentRound: 0,
        });

      } catch (error) {
        console.error("Error starting game:", error);
        addNotification("Could not load playlist and start the game.", "error");
      }
    } else {
      addNotification('The game host needs a Spotify Premium account to play.', 'error');
    }
  }, [game, accessToken, player, addNotification]);
  
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
    switch (screen) {
      case 'HOME':
        return <HomeScreen onTokenReceived={handleTokenReceived} onJoinAsGuest={handleJoinAsGuest} />;
      case 'LOBBY':
        return player && (
          <LobbyScreen
            game={game}
            currentUser={player}
            playlists={playlists}
            onCreateGame={handleCreateGame}
            onJoinGame={handleJoinGame}
            onSelectPlaylist={handleSelectPlaylist}
            onStartGame={handleStartGame}
            onLogout={handleLogout}
          />
        );
      case 'GAME':
        return game && player && <GameScreen game={game} currentUser={player} onEndGame={handleEndGame} accessToken={accessToken} onAuthError={handleAuthError} />;
      case 'RESULTS':
        return game && <ResultsScreen game={game} onPlayAgain={handlePlayAgain} />;
      default:
        return <HomeScreen onTokenReceived={handleTokenReceived} onJoinAsGuest={handleJoinAsGuest} />;
    }
  };

  const LoadingIndicator = ({ message }: { message: string }) => (
    <div className="flex justify-center items-center min-h-screen">
      <p className="text-2xl animate-pulse">{message}</p>
    </div>
  );

  if (isLoading) {
    return <LoadingIndicator message="Loading Spot-Hit..." />;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-black to-gray-900 text-white p-4 sm:p-8">
      <div className="container mx-auto">
        <Suspense fallback={<LoadingIndicator message="Loading..." />}>
          {renderScreen()}
        </Suspense>
      </div>
    </div>
  );
};

export default App;