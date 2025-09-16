import React, { useState, useEffect, useRef } from 'react';
import { useNotification } from '../contexts/NotificationContext';

declare global {
  interface Window {
    Spotify: any;
    onSpotifyWebPlaybackSDKReady: () => void;
  }
}

interface SpotifyPlayerProps {
  token: string;
  songUri: string;
  playbackState: 'IDLE' | 'PLAYING' | 'PAUSED';
  onReady: () => void;
  onAuthError: () => void;
}

const SpotifyPlayer: React.FC<SpotifyPlayerProps> = ({ token, songUri, playbackState, onReady, onAuthError }) => {
  const playerRef = useRef<any>(null);
  const [deviceId, setDeviceId] = useState<string | null>(null);
  const [isPlayerReady, setIsPlayerReady] = useState(false);
  const { addNotification } = useNotification();
  const currentSongUri = useRef<string | null>(null);

  useEffect(() => {
    const initializePlayer = () => {
        if (!window.Spotify || playerRef.current) {
            return;
        }

        const player = new window.Spotify.Player({
            name: 'Spot-Hit Game',
            getOAuthToken: (cb: (token: string) => void) => {
                cb(token);
            },
            volume: 0.5,
        });

        player.addListener('ready', ({ device_id }: { device_id: string }) => {
            console.log('Ready with Device ID', device_id);
            setDeviceId(device_id);
            setIsPlayerReady(true);
            onReady();
        });

        player.addListener('not_ready', ({ device_id }: { device_id: string }) => {
            console.log('Device ID has gone offline', device_id);
            addNotification("Spotify player disconnected.", "error");
        });

        player.addListener('initialization_error', ({ message }: { message: string }) => {
            console.error('Initialization Error:', message);
            addNotification(`Spotify Player Error: ${message}`, "error");
        });

        player.addListener('authentication_error', ({ message }: { message: string }) => {
            console.error('Authentication Error:', message);
            // The error message from the SDK is concise, e.g., "Invalid token scopes."
            if (message.toLowerCase().includes('invalid token scopes')) {
                onAuthError();
            } else {
                addNotification(`Spotify Auth Error: ${message}`, "error");
            }
        });

        player.addListener('account_error', ({ message }: { message: string }) => {
            console.error('Account Error:', message);
            addNotification(`Spotify Account Error: ${message}`, "error");
        });

        player.connect().then((success: boolean) => {
            if (success) {
                console.log('The Web Playback SDK successfully connected to Spotify!');
            }
        });

        playerRef.current = player;
    };

    // If the SDK is already loaded when this component mounts, initialize immediately.
    if (window.Spotify) {
      initializePlayer();
    } else {
      // Otherwise, wait for our custom event from index.html that signals SDK readiness.
      window.addEventListener('spotify-sdk-ready', initializePlayer);
    }

    return () => {
      // Clean up the event listener and disconnect the player when the component unmounts.
      window.removeEventListener('spotify-sdk-ready', initializePlayer);
      if (playerRef.current) {
        playerRef.current.disconnect();
        playerRef.current = null;
      }
    };
  }, [token, onReady, addNotification, onAuthError]);

  const playSong = async (uri: string) => {
      if (!deviceId) {
        addNotification("Spotify player device not ready.", "error");
        return;
      }
      try {
          const response = await fetch(`https://api.spotify.com/v1/me/player/play?device_id=${deviceId}`, {
              method: 'PUT',
              body: JSON.stringify({ uris: [uri] }),
              headers: {
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${token}`
              },
          });
          if (!response.ok) {
              // Try to parse error from Spotify, otherwise use a generic message
              let errorMessage = 'Failed to start playback.';
              try {
                  const errorData = await response.json();
                  if (errorData.error && errorData.error.message) {
                    errorMessage = errorData.error.message;
                  }
              } catch (e) {
                  // The response was not JSON, ignore
              }
              throw new Error(errorMessage);
          }
          currentSongUri.current = uri;
      } catch(e: any) {
          console.error("Failed to play song:", e);
          addNotification(`Could not play song: ${e.message}`, 'error');
      }
  };

  // Use the SDK's pause method for better state handling and robustness.
  const pauseSong = () => {
      if (playerRef.current) {
          playerRef.current.pause().catch((e: Error) => {
              console.error("Failed to pause song via SDK:", e);
          });
      }
  };

  useEffect(() => {
    if (!isPlayerReady || !songUri || !playerRef.current) return;
    
    if (playbackState === 'PLAYING') {
      // Only start playback if it's a new song
      if (currentSongUri.current !== songUri) {
          playSong(songUri);
      } else {
          // If it's the same song, just resume. Handle potential errors.
          playerRef.current.resume().catch((e: Error) => {
              console.error("Failed to resume song:", e);
              // If resume fails (e.g., player is in a weird state), try playing from start
              playSong(songUri);
          });
      }
    } else if (playbackState === 'PAUSED') {
      pauseSong();
    }
  }, [playbackState, songUri, isPlayerReady, deviceId, token]);


  // This component doesn't render anything to the DOM itself.
  return null;
};

export default SpotifyPlayer;