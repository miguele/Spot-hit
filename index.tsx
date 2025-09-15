import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { NotificationProvider } from './contexts/NotificationContext';

const params = new URLSearchParams(window.location.search);
const code = params.get('code');
const error = params.get('error');

// If the URL has 'code' or 'error', this is a callback from Spotify's auth
if (window.opener && (code || error)) {
  // Send the message to the main window that opened the popup
  window.opener.postMessage({
    type: 'spotify_auth_code',
    code: code,
    error: error
  }, window.opener.origin);
  // Do not render the app, this window will be closed by the opener
} else {
  // This is a normal page load, render the full React application.
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