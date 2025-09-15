
// IMPORTANT: Replace this with your own Spotify Client ID.
// Go to https://developer.spotify.com/dashboard/ to create an app.
// Make sure to add a Redirect URI to your app settings that matches this app's URL (e.g., http://localhost:5173/ or your deployed URL).
export const CLIENT_ID = "a09e31c757704f4b94153b2ba8845c1b"; 

// The CLIENT_SECRET is no longer needed for the Implicit Grant Flow
// and has been removed to improve security.

export const SCOPES = [
    "user-read-private",
    "playlist-read-private",
].join(" ");
