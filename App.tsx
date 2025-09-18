import React, { useState, useCallback, useEffect, useRef, lazy, Suspense } from 'react';
import type { Game, Player, Screen, GameMode, Playlist, Song } from './types';
import * as spotifyService from './services/spotifyService';
import { useNotification } from './contexts/NotificationContext';
import { db } from './firebase/config';
import { doc, setDoc, getDoc, updateDoc, arrayUnion, onSnapshot, Unsubscribe, deleteDoc, arrayRemove, deleteField } from 'firebase/firestore';

// Lazy load screen components for better performance and smaller initial bundle size
const HomeScreen = lazy(() => import('./components/HomeScreen'));
const LobbyScreen = lazy(() => import('./components/LobbyScreen'));
const GameScreen = lazy(() => import('./components/GameScreen'));
const ResultsScreen = lazy(() => import('./components/ResultsScreen'));

const curatedPlaylistIds: Record<string, string> = {
    'Feel Good Classics': '37i9dQZF1DX4fpCWaHOned',
    'Rock Classics': '37i9dQZF1DWXRqgorJj26U',
    'Pop Classics': '37i9dQZF1DX6aTaZa0K6VA',
    'Songs to Sing in the Car': '37i9dQZF1DWWMOmoBO0Mvs',
    'Classic Road Trip': '37i9dQZF1DX9_IYLsqpCmw',
    'Timeless Love Songs': '37i9dQZF1DWXbttAJcbphz',
};

const App: React.FC = () => {
  const [screen, setScreen] = useState<Screen>('HOME');
  const [game, setGame] = useState<Game | null>(null);
  const [player, setPlayer] = useState<Player | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [curatedPlaylists, setCuratedPlaylists] = useState<Playlist[]>([]);
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
        } else if (gameData.gameState === 'WAITING') {
            // Check if current player is part of the game before moving to lobby
            const currentPlayerId = player?.id;
            if (currentPlayerId && gameData.players.some(p => p.id === currentPlayerId)) {
               setScreen('LOBBY');
            }
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
      setPlayer(userProfile);

      // Fetch user playlists and curated playlists in parallel for hosts
      if (userProfile.isPremium) {
          const [userPlaylistsResult, curatedPlaylistsResult] = await Promise.allSettled([
              spotifyService.getUserPlaylists(token),
              Promise.all(Object.values(curatedPlaylistIds).map(id => spotifyService.getPlaylistDetails(id, token)))
          ]);

          if (userPlaylistsResult.status === 'fulfilled') {
              setPlaylists(userPlaylistsResult.value);
          } else {
              console.error("Failed to fetch user playlists:", userPlaylistsResult.reason);
              addNotification("Could not load your personal playlists.", "error");
          }

          if (curatedPlaylistsResult.status === 'fulfilled') {
              setCuratedPlaylists(curatedPlaylistsResult.value);
          } else {
              console.error("Failed to fetch curated playlists:", curatedPlaylistsResult.reason);
              addNotification("Could not load Spot-Hit playlists.", "error");
          }
      }
      
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
    setCuratedPlaylists([]);
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
      turnState: 'GUESSING',
      lastGuessResult: null,
      turnStartTime: null,
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
            setScreen('LOBBY'); // Explicitly move to lobby for guest
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
    const allPlaylists = [...playlists, ...curatedPlaylists];
    const playlist = allPlaylists.find(p => p.id === playlistId);
    if (playlist) {
      const gameRef = doc(db, 'games', game.code);
      await updateDoc(gameRef, { playlist: playlist });
    }
  }, [playlists, curatedPlaylists, game]);

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
        
        // Fisher-Yates shuffle for better randomness
        const shuffled = [...allSongs];
        for (let i = shuffled.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
        }
        
        const gameSongs = shuffled.slice(0, game.totalRounds);
        
        const gameRef = doc(db, 'games', game.code);
        await updateDoc(gameRef, { 
            gameState: 'IN_PROGRESS',
            songs: gameSongs,
            currentSong: gameSongs[0],
            currentRound: 0,
            turnState: 'GUESSING',
            lastGuessResult: null,
            turnStartTime: Date.now(),
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

  const handleLeaveGame = useCallback(async () => {
    if (!game || !player) return;

    const gameRef = doc(db, 'games', game.code);

    if (game.host.id === player.id) {
        // Host is leaving, delete the game.
        try {
            await deleteDoc(gameRef);
            addNotification("You left. As you were the host, the game has ended.", "info");
        } catch (error) {
            console.error("Error ending game:", error);
            addNotification("Could not leave game. Please try again.", "error");
            return;
        }
    } else {
        // A regular player is leaving.
        try {
            const playerToRemove = game.players.find(p => p.id === player.id);
            if (playerToRemove) {
                 await updateDoc(gameRef, {
                    players: arrayRemove(playerToRemove),
                    [`scores.${player.id}`]: deleteField(),
                });
                addNotification("You have left the game.", "info");
            }
        } catch (error) {
            console.error("Error leaving game:", error);
            addNotification("Could not leave game. Please try again.", "error");
            return;
        }
    }

    // Unsubscribe and reset local state
    if (gameSubscription.current) {
        gameSubscription.current();
        gameSubscription.current = null;
    }
    setGame(null);
    setScreen('LOBBY');
  }, [game, player, addNotification]);
  
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
            curatedPlaylists={curatedPlaylists}
            onCreateGame={handleCreateGame}
            onJoinGame={handleJoinGame}
            onSelectPlaylist={handleSelectPlaylist}
            onStartGame={handleStartGame}
            onLogout={handleLogout}
            onLeaveGame={handleLeaveGame}
          />
        );
      case 'GAME':
        return game && player && <GameScreen game={game} currentUser={player} onEndGame={handleEndGame} accessToken={accessToken} onAuthError={handleAuthError} onLeaveGame={handleLeaveGame} />;
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