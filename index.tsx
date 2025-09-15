
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { NotificationProvider } from './contexts/NotificationContext';

// This logic handles the Spotify authentication callback.
// It checks if the window was opened as a popup and has an access token in the URL hash.
if (window.opener && window.location.hash.includes('access_token')) {
  const params = new URLSearchParams(window.location.hash.substring(1));
  const token = params.get('access_token');
  const error = params.get('error');
  
  // Send the result back to the main window that opened this popup.
  window.opener.postMessage({
    type: 'spotify_auth',
    token: token,
    error: error
  }, window.location.origin);
  
  // The main window will close this popup, but we can render a simple message in the meantime.
  document.body.innerHTML = `<div style="display: flex; justify-content: center; align-items: center; height: 100vh; font-family: sans-serif; color: white;">Authenticating... This window will close automatically.</div>`;

} else {
  // This is a normal page load, so render the full React application.
  const rootElement = document.getElementById('root');
  if (!rootElement) {
    throw new Error("Could not find root element to mount to");
  }

  const root = ReactDOM.createRoot(rootElement);
  root.render(
    <React.StrictMode>
      <NotificationProvider>
        <App />
      </NotificationProvider>
    </React.StrictMode>
  );
}
