export type Screen = 'HOME' | 'LOBBY' | 'GAME' | 'RESULTS';
export type NotificationType = 'success' | 'error' | 'info';

export enum GameMode {
  GuessTheYear = 'GUESS_THE_YEAR',
  // GuessTheSong = 'GUESS_THE_SONG', // Removed AI mode
  // Chronological = 'CHRONOLOGICAL',
}

export interface Player {
  id: string;
  name: string;
  avatarUrl?: string;
  isPremium?: boolean;
}

export interface Song {
  id: string;
  title: string;
  artist: string;
  year: number;
  albumArtUrl?: string;
  previewUrl?: string; // Will be unused but kept for structure
  uri: string; // Spotify URI is needed for playback SDK
}

export interface Playlist {
  id: string;
  name: string;
  coverUrl?: string;
  trackCount: number;
}

export interface TimelineSong extends Song {
    placement: 'CORRECT' | 'INCORRECT' | 'PENDING';
}

export interface Game {
  code: string;
  host: Player;
  players: Player[];
  playlist: Playlist | null;
  mode: GameMode;
  currentRound: number;
  totalRounds: number;
  scores: Record<string, number>;
  gameState: 'WAITING' | 'IN_PROGRESS' | 'FINISHED';
  currentSong: Song | null;
  timeline: TimelineSong[];
  songs: Song[];
}
