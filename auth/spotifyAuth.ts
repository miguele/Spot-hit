
import type { Player } from '../types';

/**
 * Mocks the native Spotify login flow.
 * In a real application, this would open the Spotify app or a webview
 * for authentication and return the user's profile and token upon success.
 * @returns A promise that resolves to a mock Player object.
 */
export const loginWithSpotify = async (): Promise<{ player: Player; token: string }> => {
  console.log("Mocking Spotify native login...");
  
  // Simulate a network delay
  await new Promise(resolve => setTimeout(resolve, 500));

  const mockPlayer: Player = {
    id: 'mockspotifyuser',
    name: 'Mock Spotify User',
    avatarUrl: 'https://api.dicebear.com/7.x/bottts/svg?seed=spotify&backgroundColor=d1d5db',
    isPremium: true, // Assume premium for mock purposes, as it's required for playback control
  };
  
  const mockToken = 'mock-spotify-access-token';

  return { player: mockPlayer, token: mockToken };
};
