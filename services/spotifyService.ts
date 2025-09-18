import type { Player, Playlist, Song } from '../types';

const API_BASE = 'https://api.spotify.com/v1';

const fetchWebApi = async (endpoint: string, method: string, token: string, body?: any) => {
    const res = await fetch(`${API_BASE}/${endpoint}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
        method,
        body: body ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) {
        throw new Error(`Spotify API error: ${res.statusText}`);
    }
    // Handle cases where response might be empty
    const text = await res.text();
    return text ? JSON.parse(text) : {};
}

export const getUserProfile = async (token: string): Promise<Player> => {
    const data = await fetchWebApi('me', 'GET', token);
    return {
        id: data.id,
        name: data.display_name || data.id,
        avatarUrl: data.images?.[0]?.url,
        isPremium: data.product === 'premium',
    };
};

export const getUserPlaylists = async (token: string): Promise<Playlist[]> => {
    const data = await fetchWebApi('me/playlists?limit=50', 'GET', token);
    return data.items.map((p: any): Playlist => ({
        id: p.id,
        name: p.name,
        coverUrl: p.images?.[0]?.url,
        trackCount: p.tracks.total,
    }));
};

export const getPlaylistDetails = async (playlistId: string, token: string): Promise<Playlist> => {
    const p = await fetchWebApi(`playlists/${playlistId}?fields=id,name,images,tracks.total`, 'GET', token);
    return {
        id: p.id,
        name: p.name,
        coverUrl: p.images?.[0]?.url,
        trackCount: p.tracks.total,
    };
};

export const getPlaylistTracks = async (playlistId: string, token:string): Promise<Song[]> => {
    const data = await fetchWebApi(`playlists/${playlistId}/tracks?fields=items(track(id,name,uri,artists(name),album(images,release_date)))`, 'GET', token);
    return data.items
        .filter((item: any) => item.track && item.track.album && item.track.album.release_date) // Ensure basic track data exists
        .map((item: any): Song => ({
            id: item.track.id,
            title: item.track.name,
            artist: item.track.artists.map((a: any) => a.name).join(', '),
            year: parseInt(item.track.album.release_date.substring(0, 4), 10),
            albumArtUrl: item.track.album.images?.[0]?.url,
            uri: item.track.uri,
            previewUrl: null, // No longer using preview URLs
        }))
        .filter((song: Song) => song.id && song.uri && !isNaN(song.year)); // Filter out tracks without URI or invalid year
};