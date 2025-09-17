import React, { useState, useEffect, useCallback } from 'react';
import type { Game, Player, Song, TimelineSong } from '../types';
import { useNotification } from '../contexts/NotificationContext';
import Card from './Card';
import Button from './Button';
import Input from './Input';
import SpotifyPlayer from './SpotifyPlayer';
import { doc, updateDoc } from 'firebase/firestore';
import { db } from '../firebase/config';

interface GameScreenProps {
  game: Game;
  currentUser: Player;
  accessToken: string | null; // Can be null for guests
  onEndGame: (finalScores: Record<string, number>) => void;
  onAuthError: () => void;
}

const SongCard: React.FC<{ song: Song, revealed: boolean }> = ({ song, revealed }) => {
  return (
    <div className="relative aspect-square w-full max-w-[350px] mx-auto group">
      <div className={`absolute inset-0 transition-transform duration-700 ease-in-out ${revealed ? '[transform:rotateY(180deg)]' : ''} [transform-style:preserve-3d]`}>
        {/* Front of card */}
        <div className="absolute inset-0 bg-gradient-to-br from-[#1DB954] to-green-800 rounded-2xl flex items-center justify-center [backface-visibility:hidden]">
          <h2 className="text-6xl font-black text-black/50">?</h2>
        </div>
        {/* Back of card */}
        <div className="absolute inset-0 [transform:rotateY(180deg)] [backface-visibility:hidden]">
            <img src={song.albumArtUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${song.id}`} alt={song.title} className="w-full h-full object-cover rounded-2xl bg-gray-900" />
            <div className="absolute inset-0 bg-black/70 rounded-2xl flex flex-col items-center justify-center p-4">
                <h3 className="text-3xl font-bold text-center">{song.title}</h3>
                <p className="text-lg text-gray-300">{song.artist}</p>
                <p className="text-5xl font-black mt-4">{song.year}</p>
            </div>
        </div>
      </div>
    </div>
  );
};


const GameScreen: React.FC<GameScreenProps> = ({ game, currentUser, accessToken, onEndGame, onAuthError }) => {
  const [guess, setGuess] = useState('');
  const [revealed, setRevealed] = useState(false);
  const [message, setMessage] = useState('');
  const [turnState, setTurnState] = useState<'GUESSING' | 'REVEALED'>('GUESSING');
  const [playbackState, setPlaybackState] = useState<'IDLE' | 'PLAYING' | 'PAUSED'>('IDLE');
  const [isPlayerReady, setIsPlayerReady] = useState(false);
  
  const { addNotification } = useNotification();
  
  const currentPlayer = game.players.find(p => p.id === game.players[game.currentRound % game.players.length].id)!;
  const isMyTurn = currentPlayer.id === currentUser.id;
  const isHost = game.host.id === currentUser.id;
  const currentSong = game.currentSong;

  // Auto-play song for 20 seconds
  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    if (playbackState === 'PLAYING' && isHost) {
      timer = setTimeout(() => {
        setPlaybackState('PAUSED');
      }, 20000); // Play for 20 seconds
    }
    return () => clearTimeout(timer);
  }, [playbackState, isHost]);

  const nextTurn = useCallback(async () => {
    if (!isHost) return;

    const maxRounds = Math.min(game.totalRounds, game.songs.length);
    if (game.currentRound + 1 >= maxRounds) {
      onEndGame(game.scores);
      return;
    }
    
    const nextRound = game.currentRound + 1;
    const gameRef = doc(db, 'games', game.code);
    await updateDoc(gameRef, {
        currentRound: nextRound,
        currentSong: game.songs[nextRound],
    });

    setRevealed(false);
    setGuess('');
    setMessage('');
    setTurnState('GUESSING');
    setPlaybackState('PLAYING');

  }, [game, isHost, onEndGame]);

  const handleGuess = async () => {
    if (!currentSong || !guess) return;
    
    if (isHost) {
        setPlaybackState('PAUSED');
    }

    let points = 0;
    let resultMessage = '';
    
    const guessedYear = parseInt(guess, 10);
    const diff = Math.abs(guessedYear - currentSong.year);

    if (diff === 0) {
        points = 3;
        resultMessage = `Perfect! +3 points`;
    } else if (diff <= 2) {
        points = 2;
        resultMessage = `So close! +2 points`;
    } else if (diff <= 5) {
        points = 1;
        resultMessage = `Good guess! +1 point`;
    } else {
        resultMessage = `Not quite! The year was ${currentSong.year}.`;
    }

    const newScore = (game.scores[currentPlayer.id] || 0) + points;
    const gameRef = doc(db, 'games', game.code);
    await updateDoc(gameRef, {
        [`scores.${currentPlayer.id}`]: newScore
    });

    setMessage(resultMessage);
    setRevealed(true);
    setTurnState('REVEALED');
  };

  const onPlayerReady = useCallback(() => {
    if (isHost) {
        addNotification("Spotify Player connected!", "success");
        setIsPlayerReady(true);
    }
  }, [isHost, addNotification]);

  const handleInitialPlay = () => {
    if (isHost) {
      setPlaybackState('PLAYING');
    }
  };
  
  if (isHost && !game.host.isPremium) {
     return <div className="text-center text-2xl text-red-500">Error: The host must have a Spotify Premium account to play.</div>;
  }

  if (!currentSong) {
    return <div className="text-center text-2xl">Waiting for host to start the game...</div>;
  }
  
  return (
    <>
      {isHost && accessToken && (
        <SpotifyPlayer
            token={accessToken}
            songUri={currentSong.uri}
            onReady={onPlayerReady}
            playbackState={playbackState}
            onAuthError={onAuthError}
        />
      )}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <div className="lg:col-span-1 order-2 lg:order-1">
          <Card>
            <h2 className="text-2xl font-bold mb-4">Scoreboard</h2>
            <ul className="space-y-3">
              {game.players.map(p => (
                <li key={p.id} className={`flex justify-between items-center p-3 rounded-lg transition-all duration-300 ${p.id === currentPlayer.id ? 'bg-[#1DB954] text-black' : 'bg-gray-700'}`}>
                  <div className="flex items-center gap-3">
                    <img src={p.avatarUrl || `https://api.dicebear.com/7.x/bottts/svg?seed=${p.name}`} alt={p.name} className="w-10 h-10 rounded-full" />
                    <span className="font-semibold">{p.name}</span>
                  </div>
                  <span className="text-2xl font-bold">{game.scores[p.id] || 0}</span>
                </li>
              ))}
            </ul>
          </Card>
        </div>

        <div className="lg:col-span-3 order-1 lg:order-2">
          <Card className="text-center">
            <p className="text-gray-400">Round {game.currentRound + 1} / {Math.min(game.totalRounds, game.songs.length)}</p>
            <h2 className="text-3xl font-bold mt-1 mb-6">
              It's <span className="text-[#1DB954]">{isMyTurn ? "Your" : `${currentPlayer.name}'s`}</span> Turn!
            </h2>

            <SongCard song={currentSong} revealed={revealed} />
            
            <div className="mt-8 max-w-md mx-auto">
              {turnState === 'GUESSING' && (
                  <div className="space-y-4">
                      {isHost && !isPlayerReady && <p className="text-gray-300 animate-pulse">Connecting to Spotify player...</p>}
                      {isHost && isPlayerReady && playbackState === 'IDLE' && (
                          <div className="animate-fade-in-up">
                              <p className="text-gray-300 mb-4">Click to start the music for the first round.</p>
                              <Button onClick={handleInitialPlay}>Start Music</Button>
                          </div>
                      )}
                      {!isHost && <p className="text-gray-300">Listen to the host's broadcast to hear the song!</p>}

                      { (isHost ? playbackState !== 'IDLE' : true) && (
                          <>
                              <Input type="number" placeholder="YYYY" value={guess} onChange={(e) => setGuess(e.target.value)} disabled={!isMyTurn || revealed} />
                              <Button onClick={handleGuess} disabled={!isMyTurn || !guess || revealed}>
                                  Submit Guess
                              </Button>
                          </>
                      )}
                  </div>
              )}

              {turnState === 'REVEALED' && (
                  <div className="space-y-4">
                      <p className="text-xl font-semibold text-yellow-400 h-7">{message}</p>
                      {isHost && (
                        <Button onClick={nextTurn}>
                          Next Round
                        </Button>
                      )}
                      {!isHost && <p className="text-gray-300 animate-pulse">Waiting for host to start the next round...</p>}
                  </div>
              )}
              {!isMyTurn && turnState === 'GUESSING' && <p className="text-gray-400 mt-4">Waiting for {currentPlayer.name} to guess...</p>}
            </div>
          </Card>
        </div>
      </div>
    </>
  );
};

export default GameScreen;
