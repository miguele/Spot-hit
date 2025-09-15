import React, { useState, useEffect, useCallback } from 'react';
import type { Game, Player, Song, TimelineSong } from '../types';
import { getPlaylistTracks } from '../services/spotifyService';
import Card from './Card';
import Button from './Button';
import Input from './Input';

interface GameScreenProps {
  game: Game;
  currentUser: Player;
  accessToken: string;
  onEndGame: (finalScores: Record<string, number>) => void;
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


const GameScreen: React.FC<GameScreenProps> = ({ game, currentUser, accessToken, onEndGame }) => {
  const [songs, setSongs] = useState<Song[]>([]);
  const [currentSong, setCurrentSong] = useState<Song | null>(null);
  const [round, setRound] = useState(0);
  const [scores, setScores] = useState(game.scores);
  const [currentPlayerIndex, setCurrentPlayerIndex] = useState(0);
  const [guess, setGuess] = useState('');
  const [revealed, setRevealed] = useState(false);
  const [message, setMessage] = useState('');
  const [timeline, setTimeline] = useState<TimelineSong[]>([]);
  const [turnState, setTurnState] = useState<'GUESSING' | 'REVEALED'>('GUESSING');
  const [loading, setLoading] = useState(true);
  
  const currentPlayer = game.players[currentPlayerIndex];
  const isMyTurn = currentPlayer.id === currentUser.id;

  useEffect(() => {
    if (game.playlist) {
      setLoading(true);
      getPlaylistTracks(game.playlist.id, accessToken).then(playlistSongs => {
        const playableSongs = playlistSongs.filter(s => s.year); // Filter out songs without a year
        const shuffled = [...playableSongs].sort(() => 0.5 - Math.random());
        setSongs(shuffled);
        if (shuffled.length > 0) {
            setCurrentSong(shuffled[0]);
        } else {
            // Handle case where playlist has no playable songs
            alert("This playlist doesn't have any songs suitable for the game. Please pick another one.");
        }
        setLoading(false);
      }).catch(err => {
        console.error("Failed to load playlist tracks", err);
        setLoading(false);
        alert("Failed to load tracks for the selected playlist.");
      });
    }
  }, [game.playlist, accessToken]);

  const nextTurn = useCallback(() => {
    const maxRounds = Math.min(game.totalRounds, songs.length);
    if (round + 1 >= maxRounds) {
      onEndGame(scores);
      return;
    }
    const nextRound = round + 1;
    setRound(nextRound);
    setCurrentPlayerIndex((prev) => (prev + 1) % game.players.length);
    setCurrentSong(songs[nextRound]);
    setRevealed(false);
    setGuess('');
    setMessage('');
    setTurnState('GUESSING');
  }, [round, game.totalRounds, game.players.length, songs, onEndGame, scores]);

  const handleGuess = () => {
    if (!currentSong || !guess) return;

    let points = 0;
    let resultMessage = '';
    
    if (game.mode === 'GUESS_THE_YEAR') {
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
    } else if (game.mode === 'GUESS_THE_SONG') {
        const cleanGuess = guess.toLowerCase().trim();
        const titleMatch = currentSong.title.toLowerCase().includes(cleanGuess);
        const artistMatch = currentSong.artist.toLowerCase().includes(cleanGuess);

        if (titleMatch && artistMatch) {
            points = 3;
            resultMessage = 'Correct title and artist! +3 points';
        } else if (titleMatch) {
            points = 1;
            resultMessage = `You got the title! +1 point`;
        } else if (artistMatch) {
            points = 1;
            resultMessage = `You got the artist! +1 point`;
        } else {
            resultMessage = `The answer was ${currentSong.title} by ${currentSong.artist}.`;
        }
    }

    setScores(prev => ({ ...prev, [currentPlayer.id]: (prev[currentPlayer.id] || 0) + points }));
    setMessage(resultMessage);
    setRevealed(true);
    setTurnState('REVEALED');
  };

  if (loading) {
    return <div className="text-center text-2xl animate-pulse">Loading playlist...</div>;
  }

  if (!currentSong) {
    return <div className="text-center text-2xl">Could not load any songs from the playlist. Please go back and select another one.</div>;
  }
  
  const getGuessInput = () => {
    switch (game.mode) {
        case 'GUESS_THE_YEAR':
            return <Input type="number" placeholder="YYYY" value={guess} onChange={(e) => setGuess(e.target.value)} disabled={!isMyTurn || turnState === 'REVEALED'} />;
        case 'GUESS_THE_SONG':
            return <Input type="text" placeholder="Song Title or Artist" value={guess} onChange={(e) => setGuess(e.target.value)} disabled={!isMyTurn || turnState === 'REVEALED'}/>;
        default:
            return null;
    }
  }


  return (
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
                <span className="text-2xl font-bold">{scores[p.id]}</span>
              </li>
            ))}
          </ul>
        </Card>
      </div>

      <div className="lg:col-span-3 order-1 lg:order-2">
        <Card className="text-center">
          <p className="text-gray-400">Round {round + 1} / {Math.min(game.totalRounds, songs.length)}</p>
          <h2 className="text-3xl font-bold mt-1 mb-6">
            It's <span className="text-[#1DB954]">{isMyTurn ? "Your" : `${currentPlayer.name}'s`}</span> Turn!
          </h2>

          <SongCard song={currentSong} revealed={revealed} />
          
          <div className="mt-8 max-w-md mx-auto">
            {turnState === 'GUESSING' && (
                <div className="space-y-4">
                    {getGuessInput()}
                    <Button onClick={handleGuess} disabled={!isMyTurn || !guess}>
                        Submit Guess
                    </Button>
                </div>
            )}

            {turnState === 'REVEALED' && (
                 <div className="space-y-4">
                    <p className="text-xl font-semibold text-yellow-400 h-7">{message}</p>
                    <Button onClick={nextTurn} disabled={!isMyTurn}>
                       Next Round
                    </Button>
                </div>
            )}
             {!isMyTurn && turnState === 'GUESSING' && <p className="text-gray-400 mt-4">Waiting for {currentPlayer.name} to guess...</p>}
          </div>
        </Card>
      </div>
    </div>
  );
};

export default GameScreen;
