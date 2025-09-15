import React from 'react';
import Button from './Button';
import SpotifyLogo from './SpotifyLogo';
import Card from './Card';
import { generateAuthUrl } from '../auth/spotifyAuth';
import { useNotification } from '../contexts/NotificationContext';
import { CANONICAL_URL } from '../config';

interface HomeScreenProps {
  onTokenReceived: (token: string) => void;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ onTokenReceived }) => {
  const { addNotification } = useNotification();
  // Show the developer note on any non-production looking URL.
  const isDevEnvironment = window.location.hostname !== new URL(CANONICAL_URL).hostname;
  
  // The redirect URI now points to the dedicated callback.html file at the canonical URL.
  const redirectUri = `${CANONICAL_URL}/callback.html`;


  const handleLogin = () => {
    try {
      const authUrl = generateAuthUrl();
      const width = 500, height = 650;
      const left = window.screen.width / 2 - width / 2;
      const top = window.screen.height / 2 - height / 2;

      const popup = window.open(authUrl, 'spotifyLogin', `width=${width},height=${height},top=${top},left=${left}`);
      
      if (!popup) {
        addNotification("Popup blocked. Please allow popups for this site.", "error");
        return;
      }

      const handleAuthMessage = (event: MessageEvent) => {
        // We expect the message to come from our canonical origin, where callback.html is hosted.
        if (event.origin !== new URL(CANONICAL_URL).origin || event.data.type !== 'spotify_auth') {
          return;
        }

        // Clean up the event listener
        window.removeEventListener('message', handleAuthMessage);
        
        if (popup) {
          popup.close();
        }

        const { token, error } = event.data;
        if (token) {
          onTokenReceived(token);
        } else if (error) {
          addNotification(`Login failed: ${error}. Please try again.`, 'error');
        }
      };
      
      window.addEventListener('message', handleAuthMessage);

      // Check if the popup was closed manually by the user to clean up the listener
      const checkPopupClosed = setInterval(() => {
        if (!popup || popup.closed) {
          clearInterval(checkPopupClosed);
          window.removeEventListener('message', handleAuthMessage);
        }
      }, 1000);

    } catch (error) {
      console.error("Failed to initiate login:", error);
      addNotification("Could not start the login process. Please refresh and try again.", "error");
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen text-center">
        <Card className="max-w-xl w-full animate-fade-in-up">
            <h1 className="text-5xl md:text-7xl font-black tracking-tighter mb-4">
                Spot<span className="text-[#1DB954]">Hit</span>
            </h1>
            <p className="text-xl text-gray-300 mb-8">
                The ultimate music trivia game. Challenge your friends and prove your music knowledge.
            </p>
            <Button onClick={handleLogin} className="w-full max-w-sm">
                <SpotifyLogo className="w-8 h-8 mr-3"/>
                Login with Spotify
            </Button>
            <p className="text-xs text-gray-500 mt-4">
                A Spotify account is required to play.
            </p>
            {isDevEnvironment && (
                <div className="mt-6 text-xs text-yellow-300 bg-yellow-900/50 border border-yellow-700 p-3 rounded-lg max-w-sm mx-auto text-left">
                    <p className="font-bold text-sm mb-1">Developer Note:</p>
                    <p>For Spotify login to work, you must add this exact URL to your app's "Redirect URIs" in the Spotify Developer Dashboard:</p>
                    <p className="font-mono bg-black/50 px-2 py-1 rounded mt-2 break-all select-all">{redirectUri}</p>
                </div>
            )}
        </Card>
    </div>
  );
};

export default HomeScreen;