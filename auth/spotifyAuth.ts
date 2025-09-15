import { CLIENT_ID, SCOPES, CANONICAL_URL } from '../config';

export const generateAuthUrl = (): string => {
    // The redirect URI must point to the dedicated callback handler page hosted at the canonical URL.
    // Using the URL constructor makes the path joining more robust.
    const REDIRECT_URI = new URL('callback.html', CANONICAL_URL).href;
    
    const params = new URLSearchParams();
    params.append("client_id", CLIENT_ID);
    // 'token' specifies the Implicit Grant Flow.
    params.append("response_type", "token");
    params.append("redirect_uri", REDIRECT_URI);
    params.append("scope", SCOPES);

    return `https://accounts.spotify.com/authorize?${params.toString()}`;
};

// The getAccessToken function is no longer needed with the Implicit Grant Flow
// because the access token is received directly in the redirect URL hash.
// This also removes the need for the CLIENT_SECRET on the client-side, which is more secure.