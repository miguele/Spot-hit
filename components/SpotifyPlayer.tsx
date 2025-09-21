
import React, { useEffect, useRef } from 'react';

interface MockSpotifyPlayerProps {
  token: string;
  songUri: string;
  playbackState: 'IDLE' | 'PLAYING' | 'PAUSED';
  onReady: () => void;
  // These props are kept for API consistency but won't be used in the mock
  onAuthError: () => void; 
}

/**
 * A mock Spotify player for React Native.
 * This component does not play any audio. Instead, it simulates the passage
 * of time for a song to allow the game logic (like round timers) to function
 * as if music were playing.
 */
const SpotifyPlayer: React.FC<MockSpotifyPlayerProps> = ({ songUri, playbackState, onReady }) => {
  const playbackTimer = useRef<NodeJS.Timeout | null>(null);

  // The player is "ready" as soon as it mounts in this mock version.
  useEffect(() => {
    console.log("Mock Spotify Player: Ready.");
    onReady();
  }, [onReady]);

  useEffect(() => {
    // Clear any existing timer when state or song changes
    if (playbackTimer.current) {
      clearTimeout(playbackTimer.current);
      playbackTimer.current = null;
    }

    if (playbackState === 'PLAYING' && songUri) {
      console.log(`Mock Spotify Player: Simulating playback of ${songUri} for 30 seconds.`);
      // In a real app, you'd start the native playback here.
      // Here, we just set a timer to represent the song clip ending.
      playbackTimer.current = setTimeout(() => {
        console.log(`Mock Spotify Player: Finished simulating playback of ${songUri}.`);
        // The game logic will handle moving to the 'REVEALED' state,
        // which will in turn set the playbackState to 'PAUSED'.
      }, 30000); // 30-second clip duration
    } else if (playbackState === 'PAUSED' || playbackState === 'IDLE') {
        console.log("Mock Spotify Player: Playback paused or idle.");
        // In a real app, you'd pause native playback here.
        // The timer is already cleared above.
    }

    // Cleanup function to clear timer on unmount
    return () => {
      if (playbackTimer.current) {
        clearTimeout(playbackTimer.current);
      }
    };
  }, [playbackState, songUri]);

  // This is a headless component; it renders nothing.
  return null;
};

export default SpotifyPlayer;
