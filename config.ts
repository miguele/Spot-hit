
// IMPORTANT: Replace this with your own Spotify Client ID.
// Go to https://developer.spotify.com/dashboard/ to create an app.
export const CLIENT_ID = "a09e31c757704f4b94153b2ba8845c1b"; 

// This is the main, permanent URL for your application.
// All Spotify authentication requests will be redirected through this URL.
// IMPORTANT: Make sure this URL is added to your Redirect URIs in the Spotify Developer Dashboard.
// Example: https://your-project-name.vercel.app/
export const CANONICAL_URL = "https://spot-hit.vercel.app/";

export const SCOPES = [
    "user-read-private",
    "playlist-read-private",
    "streaming", // Required for Web Playback SDK
    "user-read-email", // Required for Web Playback SDK
].join(" ");