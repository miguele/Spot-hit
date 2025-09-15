import { CLIENT_ID, SCOPES } from '../config';

export const generateAuthUrl = (): string => {
    // The redirect URI must point to the dedicated callback handler page at the root of the domain.
    const REDIRECT_URI = `${window.location.origin}/callback.html`;
    
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