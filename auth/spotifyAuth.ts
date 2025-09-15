import { CLIENT_ID, SCOPES, CANONICAL_URL } from '../config';

// --- PKCE Helper Functions ---

const generateRandomString = (length: number): string => {
  const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let text = '';
  for (let i = 0; i < length; i++) {
    text += possible.charAt(Math.floor(Math.random() * possible.length));
  }
  return text;
};

const sha256 = async (plain: string): Promise<ArrayBuffer> => {
  const encoder = new TextEncoder();
  const data = encoder.encode(plain);
  return window.crypto.subtle.digest('SHA-256', data);
};

const base64encode = (input: ArrayBuffer): string => {
  return btoa(String.fromCharCode(...new Uint8Array(input)))
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
};

// --- Auth URL Generation ---

export const generateAuthUrlAndVerifier = async (): Promise<{ authUrl: string; verifier: string }> => {
  const verifier = generateRandomString(128);
  const hashed = await sha256(verifier);
  const challenge = base64encode(hashed);
  
  const REDIRECT_URI = CANONICAL_URL;

  const params = new URLSearchParams();
  params.append("client_id", CLIENT_ID);
  params.append("response_type", "code");
  params.append("redirect_uri", REDIRECT_URI);
  params.append("scope", SCOPES);
  params.append("code_challenge_method", "S256");
  params.append("code_challenge", challenge);
  // Force the consent screen to appear, helping to bypass caching issues during debugging.
  params.append("show_dialog", "true");

  const authUrl = `https://accounts.spotify.com/authorize?${params.toString()}`;
  
  return { authUrl, verifier };
};

// --- Token Exchange ---

export const exchangeCodeForToken = async (code: string, verifier: string): Promise<string> => {
    const REDIRECT_URI = CANONICAL_URL;

    const params = new URLSearchParams();
    params.append("client_id", CLIENT_ID);
    params.append("grant_type", "authorization_code");
    params.append("code", code);
    params.append("redirect_uri", REDIRECT_URI);
    params.append("code_verifier", verifier);
    
    const result = await fetch("https://accounts.spotify.com/api/token", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params
    });

    if (!result.ok) {
        const errorData = await result.json();
        throw new Error(`Token exchange failed: ${errorData.error_description || 'Unknown error'}`);
    }

    const { access_token } = await result.json();
    if (!access_token) {
        throw new Error("Access token not found in response.");
    }
    return access_token;
};