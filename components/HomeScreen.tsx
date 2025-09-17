import React, { useState } from 'react';
import Button from './Button';
import SpotifyLogo from './SpotifyLogo';
import Card from './Card';
import Input from './Input';
import { generateAuthUrlAndVerifier, exchangeCodeForToken } from '../auth/spotifyAuth';
import { useNotification } from '../contexts/NotificationContext';
import { CANONICAL_URL } from '../config';

interface HomeScreenProps {
  onTokenReceived: (token: string) => void;
  onJoinAsGuest: (gameCode: string, playerName: string) => void;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ onTokenReceived, onJoinAsGuest }) => {
  const { addNotification } = useNotification();
  const [joinCode, setJoinCode] = useState('');
  const [playerName, setPlayerName] = useState('');

  const redirectUri = CANONICAL_URL;

  const handleLogin = async () => {
    try {
      const { authUrl, verifier } = await generateAuthUrlAndVerifier();
      localStorage.setItem('spotify_code_verifier', verifier);

      const width = 500, height = 650;
      const left = window.screen.width / 2 - width / 2;
      const top = window.screen.height / 2 - height / 2;

      const popup = window.open(authUrl, 'spotifyLogin', `width=${width},height=${height},top=${top},left=${left}`);
      
      if (!popup) {
        addNotification("Popup blocked. Please allow popups for this site.", "error");
        return;
      }

      const handleAuthMessage = async (event: MessageEvent) => {
        if (event.origin !== new URL(CANONICAL_URL).origin || event.data.type !== 'spotify_auth_code') {
          return;
        }
        window.removeEventListener('message', handleAuthMessage);
        if (popup) popup.close();

        const { code, error } = event.data;
        if (error) {
          addNotification(`Login failed: ${error}. Please try again.`, 'error');
          localStorage.removeItem('spotify_code_verifier');
          return;
        }

        if (code) {
          try {
            const storedVerifier = localStorage.getItem('spotify_code_verifier');
            if (!storedVerifier) throw new Error("Authentication flow error: code verifier is missing.");
            const token = await exchangeCodeForToken(code, storedVerifier);
            onTokenReceived(token);
          } catch (err: any) {
            console.error("Token exchange failed:", err);
            addNotification(err.message || "Failed to finalize login.", "error");
          } finally {
            localStorage.removeItem('spotify_code_verifier');
          }
        }
      };
      
      window.addEventListener('message', handleAuthMessage);

      const checkPopupClosed = setInterval(() => {
        if (!popup || popup.closed) {
          clearInterval(checkPopupClosed);
          window.removeEventListener('message', handleAuthMessage);
          if (localStorage.getItem('spotify_code_verifier')) {
              localStorage.removeItem('spotify_code_verifier');
          }
        }
      }, 1000);

    } catch (error) {
      console.error("Failed to initiate login:", error);
      addNotification("Could not start the login process. Please refresh and try again.", "error");
    }
  };

  const handleGuestJoinSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onJoinAsGuest(joinCode.toUpperCase(), playerName);
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen text-center animate-fade-in-up">
        <h1 className="text-5xl md:text-7xl font-black tracking-tighter mb-4">
            Spot<span className="text-[#1DB954]">Hit</span>
        </h1>
        <p className="text-xl text-gray-300 mb-10 max-w-2xl">
            The ultimate music trivia game. Challenge your friends and prove your music knowledge.
        </p>
        <div className="w-full max-w-4xl grid grid-cols-1 md:grid-cols-2 gap-8">
            <Card className="flex flex-col">
                <h2 className="text-3xl font-bold mb-3">Create a Game</h2>
                <p className="text-gray-400 mb-6 flex-grow">Login with your Spotify account to host a game and choose the music.</p>
                <Button onClick={handleLogin} className="w-full mt-auto">
                    <SpotifyLogo className="w-6 h-6 mr-3"/>
                    Login with Spotify to Host
                </Button>
            </Card>
            <Card>
                <h2 className="text-3xl font-bold mb-4">Join a Game</h2>
                <form onSubmit={handleGuestJoinSubmit} className="space-y-4">
                    <Input 
                        placeholder="Your Name" 
                        value={playerName}
                        onChange={(e) => setPlayerName(e.target.value)}
                        required
                    />
                    <Input 
                        placeholder="Game Code" 
                        value={joinCode}
                        onChange={(e) => setJoinCode(e.target.value)}
                        className="tracking-widest"
                        maxLength={6}
                        required
                    />
                    <Button type="submit" variant="secondary" className="w-full" disabled={!joinCode || !playerName}>
                        Join as Guest
                    </Button>
                </form>
            </Card>
        </div>
    </div>
  );
};

export default HomeScreen;
